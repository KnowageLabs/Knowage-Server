package it.eng.spagobi.kpi.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
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
	// TODO: handle LOV and EXECUTION_WEEK placeholders, test and debug

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
		protected List<String> orderByAttributesNames;
		protected Map<String, String> quotedParameters = new HashMap<>();
		protected IMetaData preloadedMetaData = null;

		// This could be moved elsewhere, e.g. into a local db table
		protected List<LinkedHashMap<String, Comparable>> preloadedData = null;

		public AggregateMeasureQuery(int dataSourceId, String innerSql, String aggregateMeasureName, String aggregateMeasureFunction,
				Set<String> attributesNames, List<String> orderByAttributesNames, Map<String, String> placeholders) {
			this.dataSourceId = dataSourceId;
			this.innerSql = innerSql;
			this.aggregateMeasureName = aggregateMeasureName;
			this.aggregateMeasureFunction = aggregateMeasureFunction;
			this.attributesNames.addAll(attributesNames);
			this.orderByAttributesNames = orderByAttributesNames;

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

			// Replacing parameters from "@name" to "$P{name}" notation as
			// expected by IDataSet
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
			StringBuffer orderByColumns = new StringBuffer();
			for (String attributeName : orderByAttributesNames) {
				if (orderByColumns.length() > 0)
					orderByColumns.append(", ");
				orderByColumns.append(attributeName);
			}
			String sqlPart2 = (groupByColumns.length() != 0) ? " GROUP BY " + groupByColumns + " ORDER BY " + orderByColumns : "";
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
			// Alternative equals criteria:
			// ruleId|measureName|aggregateFunction|placeholders
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

		@Override
		public void remove() {
			iterator.remove();
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

	private static class KpiComputationUnit {
		ParsedKpi parsedKpi;
		List<AggregateMeasureQuery> queries;
		List<Map<String, String>> queriesAttributesTemporalTypes;
		List<Set<String>> queriesIgnoredAttributes;
		int mainMeasure;
		boolean replaceMode;

		KpiComputationUnit(ParsedKpi parsedKpi, List<AggregateMeasureQuery> queries, List<Map<String, String>> queriesAttributesTemporalTypes,
				List<Set<String>> queriesIgnoredAttributes, int mainMeasure, boolean replaceMode) {
			this.parsedKpi = parsedKpi;
			this.queries = queries;
			this.queriesAttributesTemporalTypes = queriesAttributesTemporalTypes;
			this.queriesIgnoredAttributes = queriesIgnoredAttributes;
			this.mainMeasure = mainMeasure;
			this.replaceMode = replaceMode;
		}
	}

	// =========================
	// ======= CONSTANTS =======
	// =========================

	public static final boolean EXCLUDE_TEMPORAL_ATTRIBUTES_FROM_KPI_VALUE_LOGICAL_KEY = true;
	public static final boolean INCLUDE_IGNORED_NON_TEMPORAL_ATTRIBUTES_INTO_KPI_VALUE_LOGICAL_KEY = true;

	// =======================
	// ======= METHODS =======
	// =======================

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		int kpiSchedulerId = Integer.parseInt(arg0.getJobDetail().getJobDataMap().getString("kpiSchedulerId"));
		computeKpis(kpiSchedulerId);
	}

	public static String computeKpis(Integer kpiSchedulerId) throws JobExecutionException {
		StringBuffer sb = new StringBuffer(); // For debug only
		Map<String, Integer> temporalTypesPriorities = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
		temporalTypesPriorities.put("YEAR", 1);
		temporalTypesPriorities.put("QUARTER", 2);
		temporalTypesPriorities.put("MONTH", 3);
		temporalTypesPriorities.put("WEEK", 4);
		temporalTypesPriorities.put("DAY", 5);
		try {
			IModalitiesValueDAO modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			IKpiDAO kpiDao = DAOFactory.getNewKpiDAO();
			KpiScheduler kpiScheduler = kpiDao.loadKpiScheduler(kpiSchedulerId);

			// Data needed for each kpi computation
			boolean replaceMode = !kpiScheduler.getDelta();
			List<KpiComputationUnit> kpiComputationUnits = new ArrayList<KpiComputationUnit>();

			// For each kpi, prepare the queries and find the main measure (i.e.
			// the one with highest cardinality)
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

				// For the "order by" section of the query, sort the attributes
				// by their number of occurrences among the measures
				// (from the most used attributes to the least used ones)
				Map<String, Integer> attributesOccurrences = new HashMap<String, Integer>();
				for (ParsedMeasure measure : parsedKpi.measures) {
					for (String attribute : measure.attributes) {
						Integer count = attributesOccurrences.get(attribute);
						attributesOccurrences.put(attribute, count == null ? 1 : count + 1);
					}
				}
				Map<Integer, Set<String>> attributesByOccurrency = new TreeMap<Integer, Set<String>>(Collections.reverseOrder());
				for (String attribute : attributesOccurrences.keySet()) {
					Integer count = attributesOccurrences.get(attribute);
					Set<String> attributes = attributesByOccurrency.get(count);
					if (attributes == null) {
						attributes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
						attributesByOccurrency.put(count, attributes);
					}
					attributes.add(attribute);
				}
				List<String> orderByAttributes = new ArrayList<String>();
				for (Set<String> attributes : attributesByOccurrency.values()) {
					orderByAttributes.addAll(attributes);
				}

				// Iterate over non-temporal attibutes combinations
				List<String> ntAttributesAll = new ArrayList<String>();
				long ntComb = 0;
				do {
					Set<String> ntAttributesToIgnore = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
					for (int a = 0; a < ntAttributesAll.size(); a++) {
						if ((ntComb & (1 << a)) > 0) {
							ntAttributesToIgnore.add(ntAttributesAll.get(a));
						}
					}
					// Iterate over temporal types
					int minTemporalTypePriority = 5;
					while (minTemporalTypePriority >= 1) {
						// Create a query for each measure and find the main
						// measure
						// (the highest cardinality, i.e. the one with most
						// attributes in the group-by section)
						List<AggregateMeasureQuery> queries = new ArrayList<AggregateMeasureQuery>();
						List<Map<String, String>> queriesAttributesTemporalTypes = new ArrayList<Map<String, String>>();
						List<Set<String>> queriesIgnoredAttributes = new ArrayList<Set<String>>();
						int mainMeasure = 0;
						int realMinTemporalTypePriority = 0;
						for (int m = 0; m < parsedKpi.measures.size(); m++) {
							// Get measure rule
							ParsedMeasure measure = parsedKpi.measures.get(m);
							Rule rule = kpiDao.loadRule(measure.ruleId, measure.ruleVersion);

							Set<String> groupByAttributes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
							groupByAttributes.addAll(measure.attributes);
							groupByAttributes.removeAll(ntAttributesToIgnore);
							Set<String> ignoredAttributes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
							// ignoredAttributes.addAll(measure.ignoredAttributes);
							ignoredAttributes.addAll(ntAttributesToIgnore);

							// Find temporal attributes (if any)
							Map<String, String> attributesTemporalTypes = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
							for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
								if ("TEMPORAL_ATTRIBUTE".equals(ruleOutput.getType())) {
									String attributeName = ruleOutput.getAlias();

									// YEAR, QUARTER, MONTH, WEEK, DAY
									String attributeTemporalType = ruleOutput.getHierarchy().getValueCd();

									Integer priority = temporalTypesPriorities.get(attributeTemporalType);
									if (priority != null && priority <= minTemporalTypePriority) {
										attributesTemporalTypes.put(attributeName, attributeTemporalType);
										realMinTemporalTypePriority = Math.max(realMinTemporalTypePriority, minTemporalTypePriority);
									} else {
										groupByAttributes.remove(attributeName);
										ignoredAttributes.add(attributeName);
									}
								}
							}
							queriesAttributesTemporalTypes.add(attributesTemporalTypes);
							queriesIgnoredAttributes.add(ignoredAttributes);

							// Build the measure query
							String ruleSql = rule.getDefinition();
							String aggregateMeasureName = parsedKpi.measuresNames.get(m);
							String aggregateMeasureFunction = parsedKpi.measuresFunctions.get(m);
							List<String> queryOrderByAttributes = new ArrayList<String>();
							for (String attribute : orderByAttributes) {
								if (groupByAttributes.contains(attribute))
									queryOrderByAttributes.add(attribute);
							}
							AggregateMeasureQuery query = new AggregateMeasureQuery(rule.getDataSourceId(), ruleSql, aggregateMeasureName,
									aggregateMeasureFunction, groupByAttributes, queryOrderByAttributes, placeholdersMap);
							if (queriesCache.containsKey(query)) {
								// The query will be used more than once:
								// reuse the previous instance of the query,
								// discarding the new one
								// and preload all the tuples

								// The previous instance cannot be retrieved efficiently via a Set, so we are forced to use a Map
								query = queriesCache.get(query);

								// This will have no effect after the first call
								query.preload();
							} else {
								// Add the query instance to the cache
								queriesCache.put(query, query);
							}
							queries.add(query);

							// Update the current main measure
							if (queries.size() == 0 || query.attributesNames.size() > queries.get(mainMeasure).attributesNames.size()) {
								mainMeasure = m;
								if (ntComb == 0) {
									ntAttributesAll.clear();
									ntAttributesAll.addAll(groupByAttributes);
								}
							}
						}
						kpiComputationUnits.add(
								new KpiComputationUnit(parsedKpi, queries, queriesAttributesTemporalTypes, queriesIgnoredAttributes, mainMeasure, replaceMode));
						Integer nextPriority = 0;
						for (String temporalType : queriesAttributesTemporalTypes.get(mainMeasure).values()) {
							Integer priority = temporalTypesPriorities.get(temporalType);
							if (priority != null && priority < realMinTemporalTypePriority && priority >= nextPriority)
								nextPriority = priority;
						}
						minTemporalTypePriority = nextPriority;
					}
					ntComb++;
				} while (ntComb < Math.pow(2, ntAttributesAll.size()));
			}

			// For each kpi, compute values and save them
			for (KpiComputationUnit kpiComputationUnit : kpiComputationUnits) {
				String result = computeKpi(kpiComputationUnit.parsedKpi, kpiComputationUnit.queries, kpiComputationUnit.mainMeasure,
						kpiComputationUnit.replaceMode, kpiComputationUnit.queriesAttributesTemporalTypes, kpiComputationUnit.queriesIgnoredAttributes);
				sb.append(result).append("@@@");
			}
		} catch (Exception e) {
			throw new JobExecutionException(e);
		}
		return sb.toString();
	}

	private static String computeKpi(ParsedKpi parsedKpi, List<AggregateMeasureQuery> queries, Integer mainMeasure, boolean replaceMode,
			List<Map<String, String>> queriesAttributesTemporalTypes, List<Set<String>> queriesIgnoredAttributes) throws JobExecutionException {
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

			Date now = new Date();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			String isoNow = df.format(now);
			// long tsNow = now.getTime();

			Session session = HibernateSessionManager.getCurrentSession();
			for (int r = 0; r < rowsFormulae.size(); r++) {
				String value = rowsFormulae.get(r);
				StringBuffer logicalKey = new StringBuffer();
				List<Comparable> rowAttributesValues = rowsAttributesValues.get(r);
				Map<String, Comparable> temporalValues = new HashMap<String, Comparable>();
				Map<String, Comparable> logicalKeyPairs = new TreeMap<String, Comparable>();
				if (INCLUDE_IGNORED_NON_TEMPORAL_ATTRIBUTES_INTO_KPI_VALUE_LOGICAL_KEY) {
					for (String attributeName : queriesIgnoredAttributes.get(mainMeasure)) {
						logicalKeyPairs.put(attributeName.toUpperCase(), "ALL");
					}
				}
				for (int a = 0; a < attributesNames.size(); a++) {
					String attributeName = attributesNames.get(a);
					String temporalType = queriesAttributesTemporalTypes.get(mainMeasure).get(attributeName);
					if (temporalType != null) {
						temporalValues.put(attributeName, rowAttributesValues.get(a).toString().replaceAll("'", "''"));
						if (EXCLUDE_TEMPORAL_ATTRIBUTES_FROM_KPI_VALUE_LOGICAL_KEY) {
							// Any "ALL" value is removed if present
							logicalKeyPairs.remove(attributesNames.get(a).toUpperCase());
							continue;
						}
					}
					logicalKeyPairs.put(attributesNames.get(a).toUpperCase(), rowAttributesValues.get(a));
				}
				for (String attributeName : logicalKeyPairs.keySet()) {
					if (logicalKey.length() > 0)
						logicalKey.append(",");
					logicalKey.append(attributeName).append("=").append(logicalKeyPairs.get(attributeName));
				}
				String insertSql = "INSERT INTO SBI_KPI_VALUE (kpi_id, kpi_version, logical_key, time_run, computed_value,"
						+ " the_day, the_week, the_month, the_quarter, the_year, state) VALUES (" + parsedKpi.id + "," + parsedKpi.version + ",'"
						+ logicalKey.toString().replaceAll("'", "''") + "','" + isoNow + "'," + (value.toLowerCase().contains("null") ? "0" : value)
						+ ",'ALL','ALL','ALL','ALL','ALL','" + (value.toLowerCase().contains("null") ? '1' : '0') + "')";
				String whereCondition = "kpi_id = " + parsedKpi.id + " AND kpi_version = " + parsedKpi.version + " AND logical_key = '"
						+ logicalKey.toString().replaceAll("'", "''") + "'" + " AND the_day = '" + ifNull(temporalValues.get("DAY"), "ALL")
						+ "' AND the_week = '" + ifNull(temporalValues.get("WEEK"), "ALL") + "'" + " AND the_month = '"
						+ ifNull(temporalValues.get("MONTH"), "ALL") + "' AND the_quarter = '" + ifNull(temporalValues.get("QUARTER"), "ALL")
						+ "' AND the_year = '" + ifNull(temporalValues.get("YEAR"), "ALL") + "'";
				String deleteSql = "DELETE SBI_KPI_VALUE WHERE " + whereCondition;
				String updateSql = "UPDATE SBI_KPI_VALUE SET computed_value = " + (value.toLowerCase().contains("null") ? "0" : value) + ", time_run = '"
						+ isoNow + "', state='" + (value.toLowerCase().contains("null") ? '1' : '0') + "' WHERE " + whereCondition; // Currently unused
				sb.append(insertSql + "|" + deleteSql + "|" + updateSql);

				session.beginTransaction();
				if (replaceMode)
					session.createSQLQuery(deleteSql).executeUpdate();
				session.createSQLQuery(insertSql).executeUpdate();
				session.getTransaction().commit();
				sb.append(" ### INSERT EXECUTED!");
				// break; // TODO remove after debug
			}
			session.close();
			System.out.println(DateFormat.getInstance().format(new Date()) + "...KPI Job PROCESSED");
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

	private static Object ifNull(Object a, Object b) {
		return a == null ? b : a;
	}
}
