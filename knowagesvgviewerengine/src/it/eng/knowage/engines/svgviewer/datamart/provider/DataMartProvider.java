package it.eng.knowage.engines.svgviewer.datamart.provider;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.datamart.provider.configurator.DataMartProviderConfigurator;
import it.eng.knowage.engines.svgviewer.dataset.DataMart;
import it.eng.knowage.engines.svgviewer.dataset.provider.Hierarchy;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.transformer.IDataStoreTransformer;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.EngineConstants;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

// TODO: Auto-generated Javadoc
/**
 * This class wrap an object of type IDataSet
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class DataMartProvider extends AbstractDataMartProvider {

	/** The data source. */
	private IDataSource dataSource;

	/** The query. */
	private String query;

	private IDataSet ds;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(DataMartProvider.class);

	public static final String QUERY = "query";

	public DataMartProvider() {
		super();
	}

	@Override
	public void init(Object conf) throws SvgViewerEngineException {
		super.init(conf);
		DataMartProviderConfigurator.configure(this, getConf());
	}

	@Override
	public DataMart getDataMart() throws SvgViewerEngineException {

		DataMart dataMart = null;
		IDataSet dataSet;

		dataSet = (IDataSet) getEnv().get(EngineConstants.ENV_DATASET);

		if (dataSet == null) {
			JDBCDataSet jdbcDataSet = new JDBCDataSet();
			jdbcDataSet.setQuery(query);
			jdbcDataSet.setDataSource(dataSource);
			dataSet = jdbcDataSet;
			dataSet.setParamsMap(getEnv());
		}

		if (dataSet.hasBehaviour(QuerableBehaviour.class.getName())) {
			QuerableBehaviour querableBehaviour = (QuerableBehaviour) dataSet.getBehaviour(QuerableBehaviour.class.getName());
			// querableBehaviour.setQueryTransformer( getDrillQueryTransformer() );
			querableBehaviour.setQueryTransformer(new DrillThroughQueryTransformer(this));

			try {
				logger.warn("trying to load data...");
				dataSet.loadData();
				logger.warn("success!!");
			} catch (Throwable e) {
				logger.error("failure!!", e);
				throw new SvgViewerEngineException("Impossible to load data from dataset", e);
			}

			IDataStore dataStore = dataSet.getDataStore();
			IMetaData dataStoreMeta = dataStore.getMetaData();
			dataStoreMeta.setIdField(dataStoreMeta.getFieldIndex(getSelectedLevel().getColumnId()));

			dataMart = new DataMart();
			dataMart.setDataStore(dataStore);
			try {

				dataMart.setTargetFeatureName(getSelectedLevel().getFeatureName());
				String columnid = getSelectedLevel().getColumnId();
				String[] measureColumnNames = (String[]) getMetaData().getMeasureColumnNames().toArray(new String[0]);

				Iterator it = dataStore.iterator();
				while (it.hasNext()) {
					IRecord record = (IRecord) it.next();

					IField field;

					field = record.getFieldAt(dataStoreMeta.getFieldIndex(columnid));
					String id = "" + field.getValue();
					if ((id == null) || (id.trim().equals(""))) {
						continue;
					}
					dataStoreMeta.getFieldMeta(dataStoreMeta.getFieldIndex(columnid)).setProperty("ROLE", "GEOID");

					for (int i = 0; i < measureColumnNames.length; i++) {
						field = record.getFieldAt(dataStoreMeta.getFieldIndex(measureColumnNames[i]));
						String value = "" + field.getValue();
						if ((value == null) || (value.trim().equals(""))) {
							continue;
						}
						dataStoreMeta.getFieldMeta(dataStoreMeta.getFieldIndex(measureColumnNames[i])).setProperty("ROLE", "MEASURE");

					}
				}

				IDataStoreTransformer dddLinkFieldTransformer = new AddLinkFieldsTransformer(measureColumnNames, getSelectedLevel(), this.getEnv());
				dddLinkFieldTransformer.transform(dataStore);
			} catch (Exception e) {
				logger.error(e.getMessage());
				throw new SvgViewerEngineException("Impossible to get DataMart");
			}
		}

		return dataMart;
	}

	/**
	 * Gets the dim geo query.
	 *
	 * @return the dim geo query
	 */
	private String getDimGeoQuery() {
		String query = "";

		Hierarchy hierarchy = getSelectedHierarchy();
		String baseLevelName = getMetaData().getLevelName(hierarchy.getName());
		Hierarchy.Level baseLevel = hierarchy.getLevel(baseLevelName);

		List levels = hierarchy.getSublevels(baseLevel.getName());

		query += "SELECT " + baseLevel.getColumnId();
		for (int i = 0; i < levels.size(); i++) {
			Hierarchy.Level subLevel;
			subLevel = (Hierarchy.Level) levels.get(i);
			query += ", " + subLevel.getColumnId();
		}
		query += " FROM " + hierarchy.getTable();
		query += " GROUP BY " + baseLevel.getColumnId();
		for (int i = 0; i < levels.size(); i++) {
			Hierarchy.Level subLevel;
			subLevel = (Hierarchy.Level) levels.get(i);
			query += ", " + subLevel.getColumnId();
		}

		return query;

	}

	/**
	 * Gets the executable query.
	 *
	 * @return the executable query
	 */
	public String getExecutableQuery() {
		String executableQuery = null;
		IDataSet dataSet;

		dataSet = (IDataSet) getEnv().get(EngineConstants.ENV_DATASET);

		if (dataSet != null) {
			try {
				JSONObject jsonConf = ObjectUtils.toJSONObject(ds.getConfiguration());
				// executableQuery = (String)dataSet.getQuery();
				executableQuery = jsonConf.getString(QUERY);
			} catch (Exception e) {
				logger.error("Error while getting query configuration.  Error: " + e.getMessage());
			}
		} else {
			executableQuery = query;
		}

		if (executableQuery.indexOf("${") == -1)
			return executableQuery;
		while (executableQuery.indexOf("${") != -1) {
			int startInd = executableQuery.indexOf("${") + 2;
			int endInd = executableQuery.indexOf("}", startInd);
			String paramName = executableQuery.substring(startInd, endInd);
			String paramValue = null;
			if (getEnv().containsKey(paramName)) {
				paramValue = getEnv().get(paramName).toString();
			}
			if (paramValue == null) {
				logger.error("Cannot find in service request a valid value for parameter: parameter name " + paramName);

				paramValue = "";
			}
			executableQuery = executableQuery.substring(0, startInd - 2) + paramValue + executableQuery.substring(endInd + 1);
		}
		return executableQuery;
	}

	/**
	 * Gets the filtered query.
	 *
	 * @param filterValue
	 *            the filter value
	 *
	 * @return the filtered query
	 */
	private String getFilteredQuery(String filterValue) {
		String aggragateQuery = null;
		String query = getExecutableQuery();

		String subQueryAlias = "t" + System.currentTimeMillis();
		String normalizedSubQueryAlias = "n" + System.currentTimeMillis();
		String dimGeoAlias = "g" + System.currentTimeMillis();

		Hierarchy hierarchy = getSelectedHierarchy();
		Hierarchy.Level level = getSelectedLevel();
		String baseLevelName = getMetaData().getLevelName(hierarchy.getName());
		Hierarchy.Level baseLevel = hierarchy.getLevel(baseLevelName);

		if (hierarchy.getType().equalsIgnoreCase("custom")) {
			System.out.println("\nCUSTOM HIERARCHY...\n");
			String aggregationColumnName = level.getColumnId();
			aggragateQuery = "SELECT * ";
			aggragateQuery += " \nFROM ( " + query + ") " + subQueryAlias;
			aggragateQuery += " \nWHERE " + subQueryAlias + "." + level.getColumnId();
			aggragateQuery += " = '" + filterValue + "'";
		} else {
			System.out.println("\nDEFAULT HIERARCHY...\n");
			String aggregationColumnName = level.getColumnId();
			aggragateQuery = "SELECT * ";
			String[] kpiColumnNames = (String[]) getMetaData().getMeasureColumnNames().toArray(new String[0]);

			String normalizedSubQuery = query;

			normalizedSubQuery = "SELECT " + normalizedSubQueryAlias + "." + getMetaData().getGeoIdColumnName(hierarchy.getName()) + " AS "
					+ getMetaData().getGeoIdColumnName(hierarchy.getName());
			for (int i = 0; i < kpiColumnNames.length; i++) {
				normalizedSubQuery += ", SUM(" + normalizedSubQueryAlias + "." + kpiColumnNames[i] + ") AS " + kpiColumnNames[i];
			}
			normalizedSubQuery += " \nFROM ( " + query + ") " + normalizedSubQueryAlias;
			normalizedSubQuery += " \nGROUP BY " + normalizedSubQueryAlias + "." + getMetaData().getGeoIdColumnName(hierarchy.getName());
			System.out.println("\nNormalized query:\n" + normalizedSubQuery);

			aggragateQuery += " \nFROM ( \n" + normalizedSubQuery + "\n ) " + subQueryAlias;
			String dimGeoQuery = getDimGeoQuery();
			System.out.println("\nDimGeo query:\n" + dimGeoQuery);
			aggragateQuery += ", (" + dimGeoQuery + ") " + dimGeoAlias;
			aggragateQuery += " \nWHERE " + subQueryAlias + "." + getMetaData().getGeoIdColumnName(hierarchy.getName());
			aggragateQuery += " = " + dimGeoAlias + "." + baseLevel.getColumnId();
			aggragateQuery += " \nAND  " + dimGeoAlias + "." + level.getColumnId() + " = '" + filterValue + "'";
		}

		System.out.println("\nExecutable query:\n" + aggragateQuery);

		return aggragateQuery;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.dataset.provider.AbstractDatasetProvider#getDataDetails(java.lang.String)
	 */
	@Override
	public SourceBean getDataDetails(String featureValue) {
		SourceBean results = null;

		Hierarchy hierarchy = getSelectedHierarchy();
		String baseLevelName = getMetaData().getLevelName(hierarchy.getName());
		Hierarchy.Level baseLevel = hierarchy.getLevel(baseLevelName);
		String columnid = baseLevel.getColumnId();

		String targetLevelName = getSelectedLevelName();
		String filterValue = featureValue;
		if (filterValue.trim().startsWith(targetLevelName + "_")) {
			filterValue = filterValue.substring(targetLevelName.length() + 1);
		}

		String filteredQuery = "";
		filteredQuery = getFilteredQuery(filterValue);
		int max_rows = 1000;

		Connection connection = null;
		try {
			JDBCDataSet dataSet = (JDBCDataSet) getEnv().get(EngineConstants.ENV_DATASET);
			if (dataSet != null) {
				connection = dataSet.getDataSource().getConnection();
			} else {
				connection = getDataSource().getConnection();
			}
			Statement statement = connection.createStatement();
			statement.execute(filteredQuery);
			ResultSet resultSet = statement.getResultSet();

			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int columnCount = resultSetMetaData.getColumnCount();

			results = new SourceBean("ROWS");
			SourceBean row;
			// resultSet.beforeFirst();
			int rowno = 0;
			while (resultSet.next()) {
				if (++rowno > 1000)
					break;

				String id = resultSet.getString(resultSet.findColumn(columnid));
				if ((id == null) || (id.trim().equals(""))) {
					continue;
				}

				row = new SourceBean("ROW");

				for (int i = 1; i <= columnCount; i++) {
					row.setAttribute(resultSetMetaData.getColumnLabel(i), (resultSet.getString(i) == null) ? "" : resultSet.getString(i));
				}
				results.setAttribute(row);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			// throw new EMFUserError(EMFErrorSeverity.ERROR, "error.mapfile.notloaded");
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					// throw new EMFUserError(EMFErrorSeverity.ERROR, "Impossible to close connection");
				}
			}
		}

		return results;
	}

	/**
	 * Gets the data source.
	 *
	 * @return the data source
	 */
	protected IDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the data source.
	 *
	 * @param dataSource
	 *            the new data source
	 */
	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Gets the query.
	 *
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Sets the query.
	 *
	 * @param query
	 *            the new query
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	public IDataSet getDs() {
		return ds;
	}

	public void setDs(IDataSet ds) {
		this.ds = ds;
	}
}
