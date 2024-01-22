/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.dataset.bo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.dataproxy.IDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.JDBCBigQueryDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.JDBCDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.JDBCRedShiftDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.JDBCSpannerDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.JDBCSynapseDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.AbstractDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JDBCStandardDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.iterator.ResultSetIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.metasql.query.SelectQuery;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.AbstractDataBase;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

public abstract class AbstractJDBCDataset extends ConfigurableDataSet {

	private static final Logger LOGGER = LogManager.getLogger(AbstractJDBCDataset.class);
	private static final String DS_TYPE = "SbiQueryDataSet";

	protected SelectQuery selectQuery;

	private static final String QUERY = "query";
	private static final String QUERY_SCRIPT = "queryScript";
	private static final String QUERY_SCRIPT_LANGUAGE = "queryScriptLanguage";

	/**
	 * Instantiates a new empty JDBC data set.
	 */
	protected AbstractJDBCDataset() {
		setDataProxy(new JDBCDataProxy());
		setDataReader(new JDBCStandardDataReader());
		addBehaviour(new QuerableBehaviour(this));
	}

	// cannibalization :D
	protected AbstractJDBCDataset(JDBCDataSet jdbcDataset) {
		this(jdbcDataset.toSpagoBiDataSet());
	}

	protected AbstractJDBCDataset(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

		setDataProxy(new JDBCDataProxy());
		setDataReader(new JDBCStandardDataReader());

		try {
			setDataSource(DataSourceFactory.getDataSource(dataSetConfig.getDataSource()));
		} catch (Exception e) {
			throw new RuntimeException("Missing right exstension", e);
		}
		try {
			String config = JSONUtils.escapeJsonString(dataSetConfig.getConfiguration());
			JSONObject jsonConf = ObjectUtils.toJSONObject(config);
			setQuery((jsonConf.get(QUERY) != null) ? jsonConf.get(QUERY).toString() : "");
			setQueryScript((jsonConf.get(QUERY_SCRIPT) != null) ? jsonConf.get(QUERY_SCRIPT).toString() : "");
			setQueryScriptLanguage(
					(jsonConf.get(QUERY_SCRIPT_LANGUAGE) != null) ? jsonConf.get(QUERY_SCRIPT_LANGUAGE).toString()
							: "");
		} catch (Exception e) {
			LOGGER.error("Error while defining dataset configuration.", e);
			throw new SpagoBIRuntimeException("Error while defining dataset configuration", e);
		}

		addBehaviour(new QuerableBehaviour(this));
	}

	/**
	 * Redefined for set schema name
	 *
	 */
	@Override
	public void setUserProfileAttributes(Map userProfile) {
		this.userProfileParameters = userProfile;
		IDataSource dataSource = getDataSource();
		if (dataSource != null && dataSource.checkIsMultiSchema()) {
			String schema = null;
			try {
				schema = (String) userProfile.get(dataSource.getSchemaAttribute());
				((JDBCDataProxy) dataProxy).setSchema(schema);
				LOGGER.debug("Set UP Schema={}", schema);
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An error occurred while reading schema name from user profile", t);
			}
		}
	}

	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet toReturn;
		JDBCDataProxy dataProxy;

		toReturn = super.toSpagoBiDataSet();

		toReturn.setType(DS_TYPE);

		dataProxy = this.getDataProxy();
		toReturn.setDataSource(dataProxy.getDataSource().toSpagoBiDataSource());

		try {
			JSONObject jsonConf = new JSONObject();
			jsonConf.put(QUERY, (getQuery() == null) ? "" : getQuery());
			jsonConf.put(QUERY_SCRIPT, (getQueryScript() == null) ? "" : getQueryScript());
			jsonConf.put(QUERY_SCRIPT_LANGUAGE, (getQueryScriptLanguage() == null) ? "" : getQueryScriptLanguage());
			toReturn.setConfiguration(jsonConf.toString());
		} catch (Exception e) {
			LOGGER.error("Error while defining dataset configuration. Error:", e);
			throw new SpagoBIRuntimeException("Error while defining dataset configuration. Error:", e);
		}

		return toReturn;
	}

	@Override
	public JDBCDataProxy getDataProxy() {
		IDataProxy dataProxy;

		dataProxy = super.getDataProxy();

		if (dataProxy == null) {
			setDataProxy(new JDBCDataProxy());
			dataProxy = getDataProxy();
		}

		if (!(dataProxy instanceof JDBCDataProxy) && !(dataProxy instanceof JDBCRedShiftDataProxy)
				&& !(dataProxy instanceof JDBCBigQueryDataProxy) && !(dataProxy instanceof JDBCSynapseDataProxy)
				&& !(dataProxy instanceof JDBCSpannerDataProxy)) {
			throw new RuntimeException(
					"DataProxy cannot be of type [" + dataProxy.getClass().getName() + "] in JDBCDataSet");
		}

		return (JDBCDataProxy) dataProxy;
	}

	@Override
	public void setDataSource(IDataSource dataSource) {
		getDataProxy().setDataSource(dataSource);
	}

	@Override
	public IDataSource getDataSource() {
		return getDataProxy().getDataSource();
	}

	@Override
	public IMetaData getMetadata() {
		IMetaData metadata = null;
		try {
			DatasetMetadataParser dsp = new DatasetMetadataParser();
			metadata = dsp.xmlToMetadata(getDsMetadata());
		} catch (Exception e) {
			LOGGER.error("Error loading the metadata", e);
			throw new SpagoBIEngineRuntimeException("Error loading the metadata", e);
		}
		return metadata;
	}

	@Override
	public IDataStore test() {
		loadData();
		return getDataStore();
	}

	@Override
	public IDataSetTableDescriptor persist(String tableName, IDataSource dataSource) {
		IDataSource datasetDataSource = getDataSource();
		if (datasetDataSource.getLabel().equals(dataSource.getLabel())) {
			LOGGER.debug("Specified datasource is the dataset's datasource; using CREATE TABLE AS SELECT tecnique");
			try {
				List<String> fields = this.getFieldsList();
				return TemporaryTableManager.createTable(fields, (String) query, tableName, getDataSource());
			} catch (Exception e) {
				LOGGER.error("Error peristing the temporary table", e);
				throw new SpagoBIEngineRuntimeException("Error peristing the temporary table", e);
			}
		} else {
			LOGGER.debug("Specified datasource is NOT the dataset's datasource");
			return super.persist(tableName, dataSource);
		}
	}

	public static String encapsulateColumnName(String columnName, IDataSource dataSource) {
		LOGGER.debug("IN");
		try {
			String toReturn = columnName;
			if (columnName != null) {
				String aliasDelimiter = TemporaryTableManager.getAliasDelimiter(dataSource);
				LOGGER.debug("Alias delimiter is [{}]", aliasDelimiter);
				if (!columnName.startsWith(aliasDelimiter) || !columnName.endsWith(aliasDelimiter)) {
					toReturn = aliasDelimiter + columnName + aliasDelimiter;
				}
			}
			LOGGER.debug("OUT: returning {}", toReturn);
			return toReturn;
		} catch (DataBaseException e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	public static String substituteStandardWithDatasourceDelimiter(String columnName, IDataSource dataSource)
			throws DataBaseException {
		LOGGER.debug("IN");
		if (columnName != null) {
			LOGGER.debug("Column name is [{}]", columnName);
			columnName = columnName.replaceAll(AbstractDataBase.STANDARD_ALIAS_DELIMITER,
					TemporaryTableManager.getAliasDelimiter(dataSource));
			LOGGER.debug("Column name after replacement is [{}]", columnName);
		} else {
			throw new IllegalArgumentException(
					"THe arguments columnName [" + columnName + "] and dataSource [" + dataSource + "] cannot be null");
		}
		LOGGER.debug("OUT");
		return columnName;
	}

	@Override
	public DataIterator iterator() {
		LOGGER.debug("IN");
		try {
			IMetaData currMetadata = getMetadata();
			QuerableBehaviour querableBehaviour = (QuerableBehaviour) getBehaviour(QuerableBehaviour.class.getName());
			String statement = querableBehaviour.getStatement();
			LOGGER.debug("Obtained statement [{}]", statement);
			dataProxy.setStatement(statement);
			JDBCDataProxy jdbcDataProxy = (JDBCDataProxy) dataProxy;
			IDataSource dataSource = jdbcDataProxy.getDataSource();
			Assert.assertNotNull(dataSource, "Invalid datasource");
			Connection connection = dataSource.getConnection();
			Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_READ_ONLY);

			connection.setAutoCommit(false); // PostgreSQL requires disabling auto-commit for setFetchSize to work
			stmt.setFetchSize(5000);

			ResultSet rs = (ResultSet) dataProxy.getData(dataReader, stmt);
			return new ResultSetIterator(connection, stmt, rs, currMetadata);
		} catch (ClassNotFoundException | SQLException | NamingException e) {
			throw new SpagoBIRuntimeException(e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@Override
	public boolean isIterable() {
		return true;
	}

	public SelectQuery getSelectQuery() {
		return selectQuery;
	}

	public void setSelectQuery(SelectQuery selectQuery) {
		this.selectQuery = selectQuery;
	}

	protected abstract AbstractDataReader createDataReader();

	/*
	 * These snippets of code come from a common utilities logic, now the logic behind parameters handling is inside each type of dataset
	 */
	@Override
	public void setParametersMap(Map<String, String> paramValues) throws JSONException {
		List<JSONObject> parameters = getDataSetParameters();
		if (parameters.size() > paramValues.size()) {
			String parameterNotValorizedStr = getParametersNotValorized(parameters, paramValues);
			throw new ParametersNotValorizedException(
					"The following parameters have no value [" + parameterNotValorizedStr + "]");
		}

		if (paramValues.size() > 0) {
			for (String paramName : paramValues.keySet()) {
				for (int i = 0; i < parameters.size(); i++) {
					JSONObject parameter = parameters.get(i);
					if (paramName.equals(parameter.optString("namePar"))) {
						String[] values = getValuesAsArray(paramValues, paramName, parameter);
						List<String> encapsulatedValues = encapsulateValues(parameter, values);
						paramValues.put(paramName, org.apache.commons.lang3.StringUtils.join(encapsulatedValues, ","));
						break;
					}
				}
			}
			setParamsMap(paramValues);
		}
	}

	private String[] getValuesAsArray(Map<String, String> paramValues, String paramName, JSONObject parameter) {
		boolean isMultiValue = parameter.optBoolean("multiValuePar");
		String paramValue = paramValues.get(paramName);
		String[] values = null;
		if (isMultiValue) {
			List<String> list = new ArrayList<>();
			boolean paramValueConsumed = false;
			try {
				JSONArray jsonArray = new JSONArray(paramValue);
				for (int j = 0; j < jsonArray.length(); j++) {
					list.add(jsonArray.getString(j));
				}
				paramValueConsumed = true;
			} catch (JSONException e) {
				paramValueConsumed = false;
			}
			if (!paramValueConsumed) {
				list.add(paramValue);
			}
			values = list.toArray(new String[0]);
		} else {
			values = Arrays.asList(paramValue).toArray(new String[0]);
		}
		return values;
	}

	private static String getParametersNotValorized(List<JSONObject> parameters, Map<String, String> parametersValues)
			throws JSONException {
		StringBuilder toReturn = new StringBuilder("");

		for (Iterator<JSONObject> iterator = parameters.iterator(); iterator.hasNext();) {
			JSONObject parameter = iterator.next();
			String parameterName = parameter.getString("namePar");
			if (parametersValues.get(parameterName) == null) {
				toReturn.append(parameterName);
				if (iterator.hasNext()) {
					toReturn.append(", ");
				}
			}
		}
		return toReturn.toString();
	}

	/**
	 * Encapsulate values into SQL values.
	 *
	 * For every type of data except string, the method convert the values to strings.
	 *
	 * With strings we can have two case:
	 * <ul>
	 * <li>String that starts and ends with single quote</li>
	 * <li>String that doesn't start and end with single quote</li>
	 * </ul>
	 *
	 * In the first case, FE are sending us SQL values that probably contain JSON escape (e.g., a JSON value like 'this string contains a \' in it').
	 *
	 * In the second case, FE are sending us standard not-SQL-escaded string ( e.g., a string like "this string contains a ' in it"). In this second case this
	 * method escapes single quote and duplicates them as requested by SQL.
	 *
	 * @param parameter Original parameter JSON metadata
	 * @param values    Actual values of parameters
	 * @return List of encapsulated values as strings
	 */
	private List<String> encapsulateValues(JSONObject parameter, String[] values) {
		String typePar = parameter.optString("typePar");
		boolean isString = "string".equalsIgnoreCase(typePar);
		String delim = isString ? "'" : "";

		List<String> newValues = new ArrayList<>();
		for (int j = 0; j < values.length; j++) {
			String value = values[j].trim();
			if (!value.isEmpty()) {
				if (value.startsWith(delim) && value.endsWith(delim)) {
					if (value.contains("','")) {
						value = value.substring(1, value.length() - 1);
						String[] valuesArray = value.split("','");
						StringBuilder newValuesFromArray = new StringBuilder("");
						for (int i = 0; i < valuesArray.length; i++) {
							String temp = valuesArray[i];
							if (!delim.isEmpty() && temp.startsWith(delim) && temp.endsWith(delim))
								temp = temp.substring(1, temp.length() - 1);
							temp = temp.replace("'", "''");
							if (i == 0)
								newValuesFromArray.append(delim + temp + delim);
							else
								newValuesFromArray.append(",").append(delim + temp + delim);

						}
						newValues.add(newValuesFromArray.toString());
					} else {
						if (isString) {
							value = value.substring(1, value.length() - 1);
							value = value.replace("'", "''");
						}
						newValues.add(delim + value + delim);
					}
				} else {
					if (isString) {
						// Duplicate single quote to transform it into an escaped SQL single quote
						value = value.replace("'", "''");
					}
					newValues.add(delim + value + delim);
				}
			}
		}
		return newValues;
	}
}
