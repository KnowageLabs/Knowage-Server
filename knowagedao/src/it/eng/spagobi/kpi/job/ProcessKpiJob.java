package it.eng.spagobi.kpi.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.KpiScheduler;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.SchedulerFilter;
import it.eng.spagobi.kpi.dao.IKpiDAO;
import it.eng.spagobi.tools.dataset.bo.ConfigurableDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDatasetFactory;
import it.eng.spagobi.tools.dataset.bo.MongoDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.scheduler.jobs.AbstractSpagoBIJob;

@SuppressWarnings("rawtypes")
public class ProcessKpiJob extends AbstractSpagoBIJob implements Job {
	// Only SQL is supported.
	// Getters/setters omitted are from inner classes for readability.
	// TODO: handle temporal attributes, handle non-string placeholders, test and debug

	// =============================
	// ======= INNER CLASSES =======
	// =============================

	private static class ParsedMeasure {
		public TreeSet<String> attributes;
		public int ruleId;
		public int ruleVersion;
	}

	private static class ParsedKpi {
		private int id;
		private int version;
		public String formula;
		public List<String> measuresNames;
		public List<String> measuresFunctions;
		public List<ParsedMeasure> measures;

		private static List<String> jsonArrayToStringList(JSONArray array) throws JSONException {
			List<String> result = new ArrayList<String>();
			for (int i = 0; i < array.length(); i++) {
				result.add(array.getString(i));
			}
			return result;
		}

		ParsedKpi(Kpi kpi) throws JSONException {
			id = kpi.getId();
			version = kpi.getVersion();
			JSONObject definition = new JSONObject(kpi.getDefinition());
			formula = definition.getString("formula");
			measuresNames = jsonArrayToStringList(definition.getJSONArray("measures"));
			measuresFunctions = jsonArrayToStringList(definition.getJSONArray("functions"));
			measures = new ArrayList<ParsedMeasure>();
			JSONObject cardinality = new JSONObject(kpi.getCardinality());
			JSONArray measureList = cardinality.getJSONArray("measureList");
			for (int m = 0; m < measureList.length(); m++) {
				JSONObject unparsedMeasure = measureList.getJSONObject(m);
				ParsedMeasure parsedMeasure = new ParsedMeasure();
				parsedMeasure.ruleId = unparsedMeasure.getInt("ruleId");
				parsedMeasure.ruleVersion = unparsedMeasure.getInt("ruleVersion");
				parsedMeasure.attributes = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
				Iterator<String> ait = unparsedMeasure.getJSONObject("attributes").keys();
				while (ait.hasNext()) {
					String attributeName = ait.next();
					if (unparsedMeasure.getJSONObject("attributes").getBoolean(attributeName)) {
						parsedMeasure.attributes.add(attributeName);
					}
				}
				measures.add(parsedMeasure);
			}
		}
	}

	private static class AggregateMeasureQuery {
		protected int dataSourceId;
		protected String innerSql;
		protected String aggregateMeasureName;
		protected String aggregateMeasureFunction;
		protected Set<String> attributesNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		protected Map<String, String> quotedParameters = new HashMap<>();
		protected IMetaData preloadedMetaData = null;
		protected List<LinkedHashMap<String, Comparable>> preloadedData = null; // This could be moved elsewhere, e.g. into a local db table

		public AggregateMeasureQuery(int dataSourceId, String innerSql, String aggregateMeasureName, String aggregateMeasureFunction,
				Set<String> attributesNames, Map<String, String> placeholders) {
			this.dataSourceId = dataSourceId;
			this.innerSql = innerSql;
			this.aggregateMeasureName = aggregateMeasureName;
			this.aggregateMeasureFunction = aggregateMeasureFunction;
			this.attributesNames.addAll(attributesNames);

			// Placeholders -> Quoted parameters
			if (placeholders != null) {
				for (String placeholderName : placeholders.keySet()) {
					String placeholderValue = placeholders.get(placeholderName);
					if (placeholderValue != null) {
						// To execute query through IDataSet, parameters must be quoted manually
						placeholderValue = "'" + placeholderValue.trim().replace("'", "") + "'";
					}
					quotedParameters.put(placeholderName, placeholderValue);
				}
			}

			// Replacing parameters from "@name" to "$P{name}" notation as expected by IDataSet
			for (String paramName : quotedParameters.keySet()) {
				innerSql = innerSql.replaceAll("\\@\\b" + paramName + "\\b", "\\$P{" + paramName + "}");
			}
		}

		@Override
		public String toString() {
			return toString(false);
		}

		public String toString(boolean replacePlaceholders) {
			StringBuffer sb = new StringBuffer();
			StringBuffer groupByColumns = new StringBuffer();
			sb.append("SELECT ").append(aggregateMeasureFunction).append("(").append(aggregateMeasureName).append(") AS ").append(aggregateMeasureName);
			for (String attributeName : attributesNames) {
				sb.append(", ").append(attributeName);
				if (groupByColumns.length() > 0)
					groupByColumns.append(", ");
				groupByColumns.append(attributeName);
			}
			sb.append(" FROM (").append(innerSql).append(")");
			String sqlPart1 = sb.toString();
			int i = 0;
			String derivedTableAlias;
			do {
				derivedTableAlias = "dtable" + (++i);
			} while (sqlPart1.contains(derivedTableAlias));
			String sqlPart2 = (groupByColumns.length() != 0) ? " GROUP BY " + groupByColumns + " ORDER BY " + groupByColumns : "";
			String sql = sqlPart1 + " AS " + derivedTableAlias + sqlPart2;
			if (replacePlaceholders) {
				for (String paramName : quotedParameters.keySet()) {
					sql = sql.replaceAll("\\$P{" + paramName + "}", quotedParameters.get(paramName));
				}
			}
			return sql;
		}

		@Override
		public boolean equals(Object o) {
			// Alternative equals criteria: ruleId|measureName|aggregateFunction|placeholders
			if (!(o instanceof AggregateMeasureQuery))
				return false;
			AggregateMeasureQuery amq = (AggregateMeasureQuery) o;
			return dataSourceId == amq.dataSourceId && toString(true).equals(amq.toString(true));
		}

		@Override
		public int hashCode() {
			return (dataSourceId + "|||" + toString(true)).hashCode();
		}

		public void preload() throws EMFUserError, EMFInternalError, JSONException {
			if (preloadedData != null)
				return;
			List<LinkedHashMap<String, Comparable>> preloadedData = new LinkedList<LinkedHashMap<String, Comparable>>();
			QueryResult qr = execute();
			while (qr.iterator.hasNext()) {
				preloadedData.add(qr.iterator.next());
			}
			// No exceptions thrown: assign result
			this.preloadedMetaData = qr.metaData;
			this.preloadedData = preloadedData;
		}

		public QueryResult execute() throws JSONException, EMFUserError, EMFInternalError {
			if (preloadedData != null) {
				return new QueryResult(preloadedMetaData, preloadedData.iterator());
			}
			return executeQuery(dataSourceId, toString(), quotedParameters);
		}

		private static QueryResult executeQuery(int dataSourceId, String sql, Map<String, String> quotedParameters)
				throws JSONException, EMFUserError, EMFInternalError {
			// Read measure value
			int maxItem = 0;
			IEngUserProfile profile = null;

			IDataSet dataSet = null;
			String queryScript = "";
			String queryScriptLanguage = "";

			JSONObject jsonDsConfig = new JSONObject();
			jsonDsConfig.put(DataSetConstants.QUERY, sql);
			jsonDsConfig.put(DataSetConstants.QUERY_SCRIPT, "");
			jsonDsConfig.put(DataSetConstants.QUERY_SCRIPT_LANGUAGE, "");
			jsonDsConfig.put(DataSetConstants.DATA_SOURCE, dataSourceId);

			IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByID(dataSourceId);
			if (dataSource != null) {
				if (dataSource.getHibDialectClass().toLowerCase().contains("mongo")) {
					dataSet = new MongoDataSet();
				} else {
					dataSet = JDBCDatasetFactory.getJDBCDataSet(dataSource);
				}
				if (quotedParameters != null)
					dataSet.setParamsMap(quotedParameters);
				((ConfigurableDataSet) dataSet).setDataSource(dataSource);
				((ConfigurableDataSet) dataSet).setQuery(sql);
				((ConfigurableDataSet) dataSet).setQueryScript(queryScript);
				((ConfigurableDataSet) dataSet).setQueryScriptLanguage(queryScriptLanguage);
			} else {
				throw new EMFInternalError(EMFErrorSeverity.BLOCKING, "A datasource with id " + dataSourceId + " could not be found");
			}

			dataSet.setConfiguration(jsonDsConfig.toString());
			// dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));

			IDataStore dataStore = dataSet.test(0, maxItem, maxItem);
			return new QueryResult(dataStore.getMetaData(), new DataStoreIterator(dataStore.getMetaData(), dataStore.iterator()));
		}
	}

	private static class DataStoreIterator implements Iterator<LinkedHashMap<String, Comparable>> {
		private IMetaData metaData;
		private Iterator<IRecord> iterator;

		public DataStoreIterator(IMetaData metaData, Iterator<IRecord> iterator) {
			this.metaData = metaData;
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public LinkedHashMap<String, Comparable> next() {
			IRecord row = iterator.next();
			LinkedHashMap<String, Comparable> rowValues = new LinkedHashMap<String, Comparable>();
			for (int i = 0; i < metaData.getFieldCount(); i++) {
				IField field = row.getFieldAt(metaData.getFieldIndex(metaData.getFieldMeta(i)));
				rowValues.put(metaData.getFieldMeta(i).getName(), (Comparable) field.getValue());
			}
			return rowValues;
		}
	}

	private static class QueryResult {
		public IMetaData metaData; // Currently unused, but it keeps data types info
		public Iterator<LinkedHashMap<String, Comparable>> iterator;

		public QueryResult(IMetaData metaData, Iterator<LinkedHashMap<String, Comparable>> iterator) {
			this.metaData = metaData;
			this.iterator = iterator;
		}
	}

	// =======================
	// ======= METHODS =======
	// =======================

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		int kpiSchedulerId = Integer.parseInt(arg0.getJobDetail().getJobDataMap().getString("kpiSchedulerId"));
		// TODO: uncomment after debug
		// computeKpis(kpiSchedulerId);
	}

	public static String computeKpis(Integer kpiSchedulerId) throws JobExecutionException {
		StringBuffer sb = new StringBuffer(); // For debug only
		try {
			IModalitiesValueDAO modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			IKpiDAO kpiDao = DAOFactory.getNewKpiDAO();
			KpiScheduler kpiScheduler = kpiDao.loadKpiScheduler(kpiSchedulerId);

			// Data neeeded for each kpi computation
			boolean replaceMode = !kpiScheduler.getDelta();
			List<ParsedKpi> parsedKpis = new ArrayList<ParsedKpi>();
			List<List<AggregateMeasureQuery>> kpisQueries = new ArrayList<List<AggregateMeasureQuery>>();
			List<Integer> kpisMainMeasures = new ArrayList<Integer>();

			// For each kpi, prepare the queries and find the main measure (i.e. the one with highest cardinality)
			Map<AggregateMeasureQuery, AggregateMeasureQuery> queriesCache = new HashMap<AggregateMeasureQuery, AggregateMeasureQuery>();
			for (Kpi kpi : kpiScheduler.getKpis()) {
				// Prepare placeholders
				Map<String, String> placeholdersMap = new HashMap<>();
				for (SchedulerFilter schedulerFilter : kpiScheduler.getFilters()) {
					if (!kpi.getName().equalsIgnoreCase(schedulerFilter.getKpiName()))
						continue;
					if ("FIXED_VALUE".equals(schedulerFilter.getType())) {
						placeholdersMap.put(schedulerFilter.getPlaceholderName(), schedulerFilter.getValue());
					} else if ("LOV".equals(schedulerFilter.getType())) {
						ModalitiesValue modalitiesvalue = modalitiesValueDAO.loadModalitiesValueByLabel(schedulerFilter.getValue());
						placeholdersMap.put(schedulerFilter.getPlaceholderName(), "1"); // TODO
					} else if ("TEMPORAL_FUNCTIONS".equals(schedulerFilter.getType())) {
						if ("EXECUTION_DAY".equals(schedulerFilter.getValue())) {
							placeholdersMap.put(schedulerFilter.getPlaceholderName(), "" + (new Date().getDate()));
						} else if ("EXECUTION_MONTH".equals(schedulerFilter.getValue())) {
							placeholdersMap.put(schedulerFilter.getPlaceholderName(), "" + (new Date().getMonth() + 1));
						} else if ("EXECUTION_WEEK".equals(schedulerFilter.getValue())) {
							placeholdersMap.put(schedulerFilter.getPlaceholderName(), "1"); // TODO
						} else if ("EXECUTION_QUARTER".equals(schedulerFilter.getValue())) {
							placeholdersMap.put(schedulerFilter.getPlaceholderName(), "" + (new Date().getMonth() / 4 + 1));
						} else if ("EXECUTION_YEAR".equals(schedulerFilter.getValue())) {
							placeholdersMap.put(schedulerFilter.getPlaceholderName(), "" + (new Date().getYear() + 1900));
						}
					}
				}

				// Read all kpi fields
				kpi = kpiDao.loadKpi(kpi.getId(), kpi.getVersion());

				// Parse the KPI
				ParsedKpi parsedKpi = new ParsedKpi(kpi);

				// Create a query for each measure and find the main measure
				// (the highest cardinality, i.e. the one with most attributes in the group-by section)
				List<AggregateMeasureQuery> queries = new ArrayList<AggregateMeasureQuery>();
				int mainMeasure = 0;
				for (int m = 0; m < parsedKpi.measures.size(); m++) {
					// Get measure rule
					ParsedMeasure measure = parsedKpi.measures.get(m);
					Rule rule = kpiDao.loadRule(measure.ruleId, measure.ruleVersion);

					// Find temporal attributes (if any)
					for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
						if ("TEMPORAL_ATTRIBUTE".equals(ruleOutput.getType())) {
							String h = ruleOutput.getHierarchy().getValueCd();
							// TODO: YEAR, MONTH, DAY, QUARTER
						}
					}

					// Build the measure query
					String ruleSql = rule.getDefinition();
					String aggregateMeasureName = parsedKpi.measuresNames.get(m);
					String aggregateMeasureFunction = parsedKpi.measuresFunctions.get(m);
					Set<String> groupByAttributes = measure.attributes;
					AggregateMeasureQuery query = new AggregateMeasureQuery(rule.getDataSourceId(), ruleSql, aggregateMeasureName, aggregateMeasureFunction,
							groupByAttributes, placeholdersMap);
					if (queriesCache.containsKey(query)) {
						// The query will be used more than once:
						// reuse the previous instance of the query, discarding the new one
						// and preload all the tuples
						query = queriesCache.get(query); // The previous instance cannot be retrieved efficiently via a Set, so we are forced to use a Map
						query.preload(); // This will have no effect after the first call
					} else {
						// Add the query instance to the cache
						queriesCache.put(query, query);
					}
					queries.add(query);

					// Update the current main measure
					if (queries.size() == 0 || query.attributesNames.size() > queries.get(mainMeasure).attributesNames.size()) {
						mainMeasure = m;
					}
				}

				// Keep data for later computation
				parsedKpis.add(parsedKpi);
				kpisQueries.add(queries);
				kpisMainMeasures.add(mainMeasure);
			}

			// For each kpi, compute values and save them
			for (int k = 0; k < parsedKpis.size(); k++) {
				String result = computeKpi(parsedKpis.get(k), kpisQueries.get(k), kpisMainMeasures.get(k), replaceMode);
				sb.append(result).append("@@@");
			}
		} catch (Exception e) {
			throw new JobExecutionException(e);
		}
		return sb.toString();
	}

	private static String computeKpi(ParsedKpi parsedKpi, List<AggregateMeasureQuery> queries, Integer mainMeasure, boolean replaceMode)
			throws JobExecutionException {
		StringBuffer sb = new StringBuffer(); // For debug only
		try {
			System.out.println(DateFormat.getInstance().format(new Date()) + " Processing Kpi Job...");

			// Read main measure data, preparing a formula for each future insert/update
			QueryResult mqr = queries.get(mainMeasure).execute();
			List<String> rowsFormulae = new ArrayList<String>();
			List<String> attributesNames = new ArrayList<String>();
			List<List<Comparable>> rowsAttributesValues = new ArrayList<List<Comparable>>();
			while (mqr.iterator.hasNext()) {
				Number measureValue = null;
				List<Comparable> rowAttributesValues = new ArrayList<Comparable>();
				LinkedHashMap<String, Comparable> row = mqr.iterator.next();
				for (String columnName : row.keySet()) {
					if (columnName.equalsIgnoreCase(queries.get(mainMeasure).aggregateMeasureName)) {
						measureValue = (Number) row.get(columnName);
					} else {
						rowAttributesValues.add(row.get(columnName));
						if (rowsAttributesValues.size() == 0)
							attributesNames.add(columnName);
					}
					// sb.append("[" + field.getValue() + "]");
				}
				String rowFormula = parsedKpi.formula.replaceFirst("M" + mainMeasure + "([^\\d].*)*$", measureValue + "$1");
				rowsFormulae.add(rowFormula);
				rowsAttributesValues.add(rowAttributesValues);
				// sb.append("###");
			}

			// For each measure read its values to complete the formulae
			for (int m = 0; m < parsedKpi.measures.size(); m++) {
				if (m == mainMeasure)
					continue;
				QueryResult qr = queries.get(m).execute();
				Number measureValue = null;
				LinkedHashMap<String, Comparable> row = null;
				for (int r = 0; r < rowsFormulae.size(); r++) {
					if (row == null && qr.iterator.hasNext())
						row = qr.iterator.next();
					if (row != null) {
						while (true) {
							int cmp = compareAttributes(rowsAttributesValues.get(r), attributesNames, row);
							if (cmp < 0) {
								measureValue = null;
								break;
							}
							if (cmp == 0) {
								measureValue = (Number) row.get(queries.get(m).aggregateMeasureName);
								break;
							}
							if (!qr.iterator.hasNext()) {
								measureValue = null;
								break;
							}
							row = qr.iterator.next();
						}
					}
					String rowFormula = rowsFormulae.get(r);
					rowsFormulae.set(r, rowFormula.replaceFirst("M" + m + "([^\\d].*)*$", measureValue + "$1"));
				}
			}

			// sb.append("|Formulae:" + new ObjectMapper().writeValueAsString(rowsFormulae));
			// TODO run INSERT/UPDATE queries based on formulae

			/*
			 * TODO Replace sbi_kpi_value after debug: CREATE TABLE tmp_kpi_value ( id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, kpi_id INTEGER NOT NULL,
			 * kpi_version INTEGER NOT NULL, logical_key VARCHAR(4096) NOT NULL, time_run DATETIME NOT NULL, value FLOAT NOT NULL, value_day VARCHAR(3) NOT
			 * NULL, value_week VARCHAR(3) NOT NULL, value_month VARCHAR(3) NOT NULL, value_q VARCHAR(3) NOT NULL, value_year VARCHAR(4) NOT NULL )
			 */

			Date now = new Date();
			// DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			String isoNow = df.format(now);
			// long tsNow = now.getTime();

			Session session = HibernateSessionManager.getCurrentSession();
			for (int r = 0; r < rowsFormulae.size(); r++) {
				String value = rowsFormulae.get(r);
				StringBuffer logicalKey = new StringBuffer();
				List<Comparable> rowAttributesValues = rowsAttributesValues.get(r);
				for (int a = 0; a < attributesNames.size(); a++) {
					if (logicalKey.length() > 0)
						logicalKey.append(",");
					logicalKey.append(attributesNames.get(a).toUpperCase()).append("=").append(rowAttributesValues.get(a));
				}
				String insertSql = "INSERT INTO tmp_kpi_value (kpi_id, kpi_version, logical_key, time_run, value,"
						+ " value_day, value_week, value_month, value_q, value_year) VALUES (" + parsedKpi.id + "," + parsedKpi.version + ",'"
						+ logicalKey.toString().replaceAll("'", "''") + "','" + isoNow + "'," + value + ",'ALL','ALL','ALL','ALL','ALL')";
				String whereCondition = "kpi_id = " + parsedKpi.id + " AND kpi_version = " + parsedKpi.version + " AND logical_key = '"
						+ logicalKey.toString().replaceAll("'", "''") + "'" + " AND value_day = 'ALL' AND value_week = 'ALL'"
						+ " AND value_month = 'ALL' AND value_q = 'ALL' AND value_year = 'ALL'";
				String deleteSql = "DELETE tmp_kpi_value WHERE " + whereCondition;
				String updateSql = "UPDATE tmp_kpi_value SET value = " + value + ", time_run = '" + isoNow + "' WHERE " + whereCondition; // Currently unused
				sb.append(insertSql + "|" + deleteSql + "|" + updateSql);

				session.beginTransaction();
				if (replaceMode)
					session.createSQLQuery(deleteSql).executeUpdate();
				session.createSQLQuery(insertSql).executeUpdate();
				session.getTransaction().commit();
				sb.append(" ### INSERT EXECUTED!");
				break; // TODO remove after debug
			}
			session.close();
		} catch (Exception e) {
			throw new JobExecutionException(e);
		}
		return sb.toString();
	}

	private static int compareAttributes(List<Comparable> firstRowAttributesValues, List<String> firstRowAttributesNames,
			LinkedHashMap<String, Comparable> secondRow) {
		for (int i = 0; i < firstRowAttributesNames.size(); i++) {
			String attributeName = firstRowAttributesNames.get(i);
			if (!secondRow.containsKey(attributeName))
				continue;
			int cmp = firstRowAttributesValues.get(i).compareTo(secondRow.get(attributeName));
			if (cmp != 0)
				return cmp;
		}
		return 0;
	}

}
