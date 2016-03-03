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
package it.eng.spagobi.engine.cockpit.api.crosstable;

import it.eng.qbe.query.CriteriaConstants;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.WhereField.Operand;
import it.eng.qbe.statement.AbstractStatement;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engine.cockpit.api.AbstractCockpitEngineResource;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCHiveDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.FilteringBehaviour;
import it.eng.spagobi.tools.dataset.common.behaviour.SelectableFieldsBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.persist.DataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTable;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableRecorder;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.safehaus.uuid.UUIDGenerator;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author Alberto Alagna
 *
 */
@Path("/1.0/crosstab")
public class CrosstabResource extends AbstractCockpitEngineResource {

	static private Logger logger = Logger.getLogger(CrosstabResource.class);

	// INPUT PARAMETERS
	private static final NodeComparator ASC = new NodeComparator(1);
	private static final NodeComparator DESC = new NodeComparator(-1);

	public static final String OUTPUT_TYPE = "OUTPUT_TYPE";

	public enum OutputType {
		JSON, HTML
	};

	private String temporaryTableName;

	@Context
	private HttpServletRequest servletRequest;

	private UserProfile getUserProfile() {
		UserProfile profile = this.getIOManager().getUserProfile();
		return profile;
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getSortedCrosstab(@QueryParam("crosstabDefinition") String crosstabDefinition, @QueryParam("datasetLabel") String datasetLabel) {
		logger.debug("IN");

		// the sort options
		Map<String, Object> columnsSortKeys;
		Map<String, Object> rowsSortKeys;
		// the id of the crosstab in the client configuration array
		Integer myGlobalId;

		try {
			JSONObject object = RestUtilities.readBodyAsJSONObject(servletRequest);
			JSONObject columnsSortKeysJo = object.optJSONObject("columnsSortKeys");
			JSONObject rowsSortKeysJo = object.optJSONObject("rowsSortKeys");
			myGlobalId = object.optInt("myGlobalId");
			columnsSortKeys = JSONUtils.toMap(columnsSortKeysJo);
			rowsSortKeys = JSONUtils.toMap(rowsSortKeysJo);
		} catch (Exception e) {
			logger.error("Error getting the sort info from the request");
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);
		}

		Map<Integer, NodeComparator> columnsSortKeysMap = toComparatorMap(columnsSortKeys);
		Map<Integer, NodeComparator> rowsSortKeysMap = toComparatorMap(rowsSortKeys);

		try {
			return createCrossTable(crosstabDefinition, datasetLabel, columnsSortKeysMap, rowsSortKeysMap, myGlobalId);
		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getSortedCrosstabUpdated(@QueryParam("crosstabDefinition") String crosstabDefinition) {
		logger.debug("IN");

		// the sort options
		Map<String, Object> columnsSortKeys;
		Map<String, Object> rowsSortKeys;
		// the id of the crosstab in the client configuration array
		Integer myGlobalId;

		try {

			JSONObject request = RestUtilities.readBodyAsJSONObject(servletRequest);

			JSONObject sortOptions = request.getJSONObject("sortOptions");

			JSONObject columnsSortKeysJo = sortOptions.optJSONObject("columnsSortKeys");
			JSONObject rowsSortKeysJo = sortOptions.optJSONObject("rowsSortKeys");
			myGlobalId = sortOptions.optInt("myGlobalId");
			columnsSortKeys = JSONUtils.toMap(columnsSortKeysJo);
			rowsSortKeys = JSONUtils.toMap(rowsSortKeysJo);

			Map<Integer, NodeComparator> columnsSortKeysMap = toComparatorMap(columnsSortKeys);
			Map<Integer, NodeComparator> rowsSortKeysMap = toComparatorMap(rowsSortKeys);

			CrosstabBuilder builder = new CrosstabBuilder(getLocale(), crosstabDefinition, request.getJSONArray("jsonData"), request.getJSONObject("metadata"));
			return builder.getSortedCrosstab(columnsSortKeysMap, rowsSortKeysMap, myGlobalId);

		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private Map<Integer, NodeComparator> toComparatorMap(Map<String, Object> sortKeyMap) {
		Map<Integer, NodeComparator> sortKeys = new HashMap<Integer, NodeComparator>();

		Iterator<String> mapIter = sortKeyMap.keySet().iterator();

		while (mapIter.hasNext()) {
			String field = mapIter.next();
			Object order = sortKeyMap.get(field);
			if (order.toString().equals("-1")) {
				sortKeys.put(new Integer(field), DESC);
			} else {
				sortKeys.put(new Integer(field), ASC);
			}
		}

		return sortKeys;
	}

	private String createCrossTable(String jsonData, String datasetLabel, Map<Integer, NodeComparator> columnsSortKeysMap,
			Map<Integer, NodeComparator> rowsSortKeysMap, Integer myGlobalId) {

		CrossTab crossTab;
		IDataStore valuesDataStore = null;
		CrosstabDefinition crosstabDefinition = null;

		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		logger.debug("IN");

		String htmlCode = "";

		try {

			totalTimeMonitor = MonitorFactory.start("WorksheetEngine.loadCrosstabAction.totalTime");
			// jsonData =
			// "{\"config\":{\"measureson\":\"columns\",\"type\":\"pivot\",\"maxcellnumber\":2000},\"rows\":[{\"id\":\"Comune\",\"alias\":\"Comune\",\"iconCls\":\"attribute\",\"nature\":\"attribute\",\"values\":\"[]\"}],\"columns\":[{\"id\":\"Maschi Totale\",\"alias\":\"Maschi Totale\",\"iconCls\":\"attribute\",\"nature\":\"attribute\",\"values\":\"[]\"}],\"measures\":[{\"id\":\"Femmine corsi a tempo pieno\",\"alias\":\"Femmine corsi a tempo pieno\",\"iconCls\":\"measure\",\"nature\":\"measure\",\"funct\":\"SUM\"}]}";
			JSONObject crosstabDefinitionJSON = new JSONObject(jsonData);

			logger.debug("Parameter [" + crosstabDefinitionJSON + "] is equals to [" + crosstabDefinitionJSON.toString() + "]");
			logger.debug("Parameter [datasetLabel] is equals to [" + datasetLabel + "]");

			IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
			dataSetDao.setUserProfile(this.getUserProfile());
			IDataSet dataset = dataSetDao.loadDataSetByLabel(datasetLabel);
			// checkQbeDataset(dataset);

			IDataSource dataSource = dataset.getDataSource();

			// persist dataset into temporary table
			IDataSetTableDescriptor descriptor = this.persistDataSet(dataset);

			// build SQL query against temporary table
			List<WhereField> whereFields = new ArrayList<WhereField>();

			// List<WhereField> temp = getOptionalFilters(optionalFilters);
			// whereFields.addAll(temp);

			// deserialize crosstab definition
			CrosstabJSONDeserializer crosstabJSONDeserializer = (CrosstabJSONDeserializer) CrosstabDeserializerFactory.getInstance().getDeserializer(
					"application/json");

			crosstabDefinition = crosstabJSONDeserializer.deserialize(crosstabDefinitionJSON);

			String worksheetQuery = null;
			IDataSource dsForTheTemporaryTable = descriptor.getDataSource();

			worksheetQuery = this.buildSqlStatement(crosstabDefinition, descriptor, whereFields, dsForTheTemporaryTable);

			// execute SQL query against temporary table
			logger.debug("Executing query on temporary table : " + worksheetQuery);
			valuesDataStore = this.executeWorksheetQuery(worksheetQuery, null, null, dataset);

			LogMF.debug(logger, "Query on temporary table executed successfully; datastore obtained: {0}", valuesDataStore);
			Assert.assertNotNull(valuesDataStore, "Datastore obatined is null!!");

			/*
			 * since the datastore, at this point, is a JDBC datastore, it does not contain information about measures/attributes, fields' name and alias...
			 * therefore we adjust its metadata
			 */
			this.adjustMetadata((DataStore) valuesDataStore, dataset, descriptor);
			LogMF.debug(logger, "Adjusted metadata: {0}", valuesDataStore.getMetaData());

			logger.debug("Decoding dataset ...");
			this.applyOptions(valuesDataStore);
			dataset.decode(valuesDataStore);

			LogMF.debug(logger, "Dataset decoded: {0}", valuesDataStore);

			// serialize crosstab
			if (crosstabDefinition.isPivotTable()) {
				// load the crosstab for a crosstab widget (with headers, sum,
				// ...)
				if (crosstabDefinition.isStatic()) {
					crossTab = new CrossTab(valuesDataStore, crosstabDefinition, null, columnsSortKeysMap, rowsSortKeysMap, myGlobalId);
				} else {
					crossTab = new CrossTab(valuesDataStore, crosstabDefinition, null, columnsSortKeysMap, rowsSortKeysMap, myGlobalId);
				}
			} else {
				// load the crosstab data structure for all other widgets
				crossTab = new CrossTab(valuesDataStore, crosstabDefinition, null, columnsSortKeysMap, rowsSortKeysMap, myGlobalId);
			}

			htmlCode = crossTab.getHTMLCrossTab(this.getLocale());//

		} catch (Exception e) {
			errorHitsMonitor = MonitorFactory.start("WorksheetEngine.errorHits");
			errorHitsMonitor.stop();
			throw new SpagoBIRuntimeException("An unexpecte error occured while genereting cross tab html", e);
		} finally {
			if (totalTimeMonitor != null) {
				totalTimeMonitor.stop();
			}
			logger.debug("OUT");
		}

		return htmlCode;
	}

	public static List<WhereField> transformIntoWhereClauses(Map<String, List<String>> filters) throws JSONException {

		List<WhereField> whereFields = new ArrayList<WhereField>();

		Set<String> keys = filters.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String aFilterName = it.next();
			List<String> values = filters.get(aFilterName);
			if (values != null && values.size() > 0) {
				String operator = values.size() > 1 ? CriteriaConstants.IN : CriteriaConstants.EQUALS_TO;
				Operand leftOperand = new Operand(new String[] { aFilterName }, null, AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD, null, null);
				String[] valuesArray = values.toArray(new String[0]);
				Operand rightOperand = new Operand(valuesArray, null, AbstractStatement.OPERAND_TYPE_STATIC, null, null);
				WhereField whereField = new WhereField(UUIDGenerator.getInstance().generateRandomBasedUUID().toString(), aFilterName, false, leftOperand,
						operator, rightOperand, "AND");

				whereFields.add(whereField);
			}
		}

		return whereFields;
	}

	public static List<WhereField> transformIntoWhereClauses(JSONObject optionalUserFilters) throws JSONException {
		String[] fields = JSONObject.getNames(optionalUserFilters);
		List<WhereField> whereFields = new ArrayList<WhereField>();
		for (int i = 0; i < fields.length; i++) {
			String fieldName = fields[i];
			Object valuesObject = optionalUserFilters.get(fieldName);
			if (valuesObject instanceof JSONArray) {
				JSONArray valuesArray = optionalUserFilters.getJSONArray(fieldName);

				// if the filter has some value
				if (valuesArray.length() > 0) {
					String[] values = new String[1];
					values[0] = fieldName;

					Operand leftOperand = new Operand(values, fieldName, AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD, values, values);

					values = new String[valuesArray.length()];
					for (int j = 0; j < valuesArray.length(); j++) {
						values[j] = valuesArray.getString(j);
					}

					Operand rightOperand = new Operand(values, fieldName, AbstractStatement.OPERAND_TYPE_STATIC, values, values);

					String operator = "EQUALS TO";
					if (valuesArray.length() > 1) {
						operator = "IN";
					}

					whereFields.add(new WhereField("OptionalFilter" + i, "OptionalFilter" + i, false, leftOperand, operator, rightOperand, "AND"));
				}
			} else {
				logger.debug("The values of the filter " + fieldName + " are not a JSONArray but " + valuesObject);
			}

		}
		return whereFields;
	}

	public IDataSetTableDescriptor persistDataSet(IDataSet dataset) {

		if (dataset.isPersisted() || dataset.isFlatDataset()) {
			return getDescriptorFromDatasetMeta(dataset);
		} else {
			// String tableName = engineInstance.getTemporaryTableName();
			return persistDataSetWithTemporaryTable(dataset, getTemporaryTableName());
		}

	}

	public List<WhereField> getOptionalFilters(JSONObject optionalUserFilters) throws JSONException {
		if (optionalUserFilters != null) {
			return transformIntoWhereClauses(optionalUserFilters);
		} else {
			return new ArrayList<WhereField>();
		}
	}

	/**
	 * Build the sql statement to query the temporary table
	 *
	 * @param crosstabDefinition
	 *            definition of the crosstab
	 * @param descriptor
	 *            the temporary table descriptor
	 * @param dataSource
	 *            the datasource
	 * @param tableName
	 *            the temporary table name
	 * @return the sql statement to query the temporary table
	 */
	protected String buildSqlStatement(CrosstabDefinition crosstabDefinition, IDataSetTableDescriptor descriptor, List<WhereField> filters,
			IDataSource dataSource) {
		return CrosstabQueryCreator.getCrosstabQuery(crosstabDefinition, descriptor, filters, dataSource);
	}

	public IDataStore executeWorksheetQuery(String worksheetQuery, Integer start, Integer limit, IDataSet dataset) {

		IDataStore dataStore = null;

		if (dataset.isFlatDataset() || dataset.isPersisted()) {
			dataStore = useDataSetStrategy(worksheetQuery, dataset, start, limit);
		} else {
			logger.debug("Using temporary table strategy....");
			dataStore = useTemporaryTableStrategy(worksheetQuery, start, limit);
		}

		Assert.assertNotNull(dataStore, "The dataStore cannot be null");
		logger.debug("Query executed succesfully");

		Integer resultNumber = (Integer) dataStore.getMetaData().getProperty("resultNumber");
		Assert.assertNotNull(resultNumber, "property [resultNumber] of the dataStore returned by queryTemporaryTable method of the class ["
				+ TemporaryTableManager.class.getName() + "] cannot be null");
		logger.debug("Total records: " + resultNumber);

		UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
		Integer maxSize = null; // QbeEngineConfig.getInstance().getResultLimit();
		boolean overflow = maxSize != null && resultNumber >= maxSize;
		if (overflow) {
			logger.warn("Query results number [" + resultNumber + "] exceeds max result limit that is [" + maxSize + "]");
		}

		return dataStore;
	}

	protected void adjustMetadata(DataStore dataStore, IDataSet dataset, IDataSetTableDescriptor descriptor) {
		adjustMetadata(dataStore, dataset, descriptor, null);
	}

	public void applyOptions(IDataStore dataStore) {

		// IMetaData metadata = dataStore.getMetaData();
		// int fieldsCount = metadata.getFieldCount();
		// for (int i = 0 ; i < fieldsCount ; i++ ) {
		// IFieldMetaData fieldMetadata = metadata.getFieldMeta(i);
		// FieldOptions fieldOptions =
		// options.getOptionsForFieldByFieldId(fieldMetadata.getName());
		// if (fieldOptions != null) {
		// // there are options for the field
		// logger.debug("Field [name : " + fieldMetadata.getName() +
		// " ; alias : " + fieldMetadata.getAlias() + "] has options set");
		// Map properties = fieldMetadata.getProperties();
		// List<FieldOption> list = fieldOptions.getOptions();
		// Iterator<FieldOption> it = list.iterator();
		// while (it.hasNext()) {
		// FieldOption option = it.next();
		// String name = option.getName();
		// Object value = option.getValue();
		// logger.debug("Putting option [name : " + name + " ; value : " + value
		// +
		// "] into field [name : " + fieldMetadata.getName() + " ; alias : " +
		// fieldMetadata.getAlias() + "]");
		// properties.put(name, value);
		// }
		// } else {
		// logger.debug("Field [name : " + fieldMetadata.getName() +
		// " ; alias : " + fieldMetadata.getAlias() + "] has no options set");
		// }
		// }

	}

	private IDataSetTableDescriptor getDescriptorFromDatasetMeta(IDataSet dataset) {
		logger.debug("Getting the TableDescriptor for the dataset with label [" + dataset.getLabel() + "]");
		IDataSetTableDescriptor td = new DataSetTableDescriptor(dataset);
		logger.debug("Table descriptor successully created : " + td);
		return td;
	}

	private String getTemporaryTableName() {
		logger.debug("IN");
		String temporaryTableNameRoot = (String) this.getEnv().get(SpagoBIConstants.TEMPORARY_TABLE_ROOT_NAME);
		logger.debug("Temporary table name root specified on the environment : [" + temporaryTableNameRoot + "]");

		// if temporaryTableNameRadix is not specified on the environment,
		// create a new name using the user profile
		if (temporaryTableNameRoot == null) {
			logger.debug("Temporary table name root not specified on the environment, creating a new one using user identifier ...");
			UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
			temporaryTableNameRoot = userProfile.getUserId().toString();
		}

		logger.debug("Temporary table root name : [" + temporaryTableNameRoot + "]");

		String temporaryTableNameComplete = TemporaryTableManager.getTableName(temporaryTableNameRoot);
		logger.debug("Temporary table name : [" + temporaryTableNameComplete + "]. Putting it into the environment");

		this.getEnv().put(SpagoBIConstants.TEMPORARY_TABLE_NAME, temporaryTableNameComplete);
		logger.debug("OUT : temporaryTableName = [" + temporaryTableNameComplete + "]");

		this.temporaryTableName = temporaryTableNameComplete;

		return this.temporaryTableName;
	}

	private IDataSetTableDescriptor persistDataSetWithTemporaryTable(IDataSet dataset, String tableName) {
		// get temporary table name

		logger.debug("Temporary table name is [" + tableName + "]");

		// set all filters into dataset, because dataset's getSignature() and
		// persist() methods may depend on them

		Assert.assertNotNull(dataset, "The engine instance is missing the dataset!!");
		Map<String, List<String>> filters = getFiltersOnDomainValues();
		if (dataset.hasBehaviour(FilteringBehaviour.ID)) {
			logger.debug("Dataset has FilteringBehaviour.");
			FilteringBehaviour filteringBehaviour = (FilteringBehaviour) dataset.getBehaviour(FilteringBehaviour.ID);
			logger.debug("Setting filters on domain values : " + filters);
			filteringBehaviour.setFilters(filters);
		}

		if (dataset.hasBehaviour(SelectableFieldsBehaviour.ID)) {
			logger.debug("Dataset has SelectableFieldsBehaviour.");
			List<String> fields = getAllFields();
			SelectableFieldsBehaviour selectableFieldsBehaviour = (SelectableFieldsBehaviour) dataset.getBehaviour(SelectableFieldsBehaviour.ID);
			logger.debug("Setting list of fields : " + fields);
			selectableFieldsBehaviour.setSelectedFields(fields);
		}

		String signature = dataset.getSignature();
		logger.debug("Dataset signature : " + signature);
		if (signature.equals(TemporaryTableManager.getLastDataSetSignature(tableName))) {
			// signature matches: no need to create a TemporaryTable
			logger.debug("Signature matches: no need to create a TemporaryTable");
			return TemporaryTableManager.getLastDataSetTableDescriptor(tableName);
		}

		IDataSource dataSource = null;

		try {
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(this.getUserProfile());
			dataSource = dataSourceDAO.loadDataSourceWriteDefault();
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException(e);
		}

		// drop the temporary table if one exists
		try {
			logger.debug("Signature does not match: dropping TemporaryTable " + tableName + " if it exists...");
			TemporaryTableManager.dropTableIfExists(tableName, dataSource);
		} catch (Exception e) {
			logger.error("Impossible to drop the temporary table with name " + tableName, e);
			throw new SpagoBIEngineRuntimeException("Impossible to drop the temporary table with name " + tableName, e);
		}

		IDataSetTableDescriptor td = null;

		try {
			logger.debug("Persisting dataset ...");

			td = dataset.persist(tableName, dataSource);
			this.recordTemporaryTable(tableName, dataSource);

			/**
			 * Do not remove comments from the following line: we cannot change the datatset state, since we are only temporarily persisting the dataset, but
			 * the dataset itself could change during next user interaction (example: the user is using Qbe and he will change the dataset itself). We will use
			 * TemporaryTableManager to store this kind of information.
			 *
			 * dataset.setDataSourceForReading(getEngineInstance(). getDataSourceForWriting()); dataset.setPersisted(true);
			 * dataset.setPersistTableName(td.getTableName());
			 */

			logger.debug("Dataset persisted");
		} catch (Exception e) {
			logger.error("Error while persisting dataset", e);
			throw new SpagoBIRuntimeException("Error while persisting dataset", e);
		}

		logger.debug("Dataset persisted successfully. Table descriptor : " + td);
		TemporaryTableManager.setLastDataSetSignature(tableName, signature);
		TemporaryTableManager.setLastDataSetTableDescriptor(tableName, td);
		return td;
	}

	private IDataStore useDataSetStrategy(String worksheetQuery, IDataSet dataset, Integer start, Integer limit) {
		IDataStore dataStore = null;

		UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);

		logger.debug("Querying dataset's flat/persistence table: user [" + userProfile.getUserId() + "] (SQL): [" + worksheetQuery + "]");

		try {

			logger.debug("SQL statement is [" + worksheetQuery + "]");
			IDataSet newdataset;
			if (dataset instanceof JDBCHiveDataSet) {
				newdataset = new JDBCHiveDataSet();
				((JDBCHiveDataSet) newdataset).setQuery(worksheetQuery);
			} else {
				newdataset = new JDBCDataSet();
				((JDBCDataSet) newdataset).setQuery(worksheetQuery);
			}

			newdataset.setDataSource(dataset.getDataSourceForReading());
			if (start == null && limit == null) {
				newdataset.loadData();
			} else {
				newdataset.loadData(start, limit, -1);
			}
			dataStore = newdataset.getDataStore();
			logger.debug("Data store retrieved successfully");
			logger.debug("OUT");
			return dataStore;
		} catch (Exception e) {
			logger.debug("Query execution aborted because of an internal exception");

			throw new SpagoBIEngineRuntimeException(e);
		}
	}

	private IDataStore useTemporaryTableStrategy(String worksheetQuery, Integer start, Integer limit) {

		IDataSource dataSource = null;

		try {
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(this.getUserProfile());
			dataSource = dataSourceDAO.loadDataSourceWriteDefault();
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException(e);
		}

		IDataStore dataStore = null;

		UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);

		logger.debug("Querying temporary table: user [" + userProfile.getUserId() + "] (SQL): [" + worksheetQuery + "]");

		try {
			dataStore = TemporaryTableManager.queryTemporaryTable(worksheetQuery, dataSource, start, limit);
		} catch (Exception e) {
			logger.debug("Query execution aborted because of an internal exception");

			throw new SpagoBIEngineRuntimeException(e);
		}
		return dataStore;
	}

	protected void adjustMetadata(DataStore dataStore, IDataSet dataset, IDataSetTableDescriptor descriptor, JSONArray fieldOptions) {

		IMetaData dataStoreMetadata = dataStore.getMetaData();
		IMetaData dataSetMetadata = dataset.getMetadata();
		MetaData newdataStoreMetadata = new MetaData();
		int fieldCount = dataStoreMetadata.getFieldCount();
		for (int i = 0; i < fieldCount; i++) {
			IFieldMetaData dataStoreFieldMetadata = dataStoreMetadata.getFieldMeta(i);
			String columnName = dataStoreFieldMetadata.getName();
			logger.debug("Column name : " + columnName);
			String fieldName = descriptor.getFieldName(columnName);
			logger.debug("Field name : " + fieldName);
			int index = dataSetMetadata.getFieldIndex(fieldName);
			logger.debug("Field index : " + index);
			IFieldMetaData dataSetFieldMetadata = dataSetMetadata.getFieldMeta(index);
			logger.debug("Field metadata : " + dataSetFieldMetadata);
			FieldMetadata newFieldMetadata = new FieldMetadata();
			String decimalPrecision = (String) dataSetFieldMetadata.getProperty(IFieldMetaData.DECIMALPRECISION);
			if (decimalPrecision != null) {
				newFieldMetadata.setProperty(IFieldMetaData.DECIMALPRECISION, decimalPrecision);
			}
			if (fieldOptions != null) {
				addMeasuresScaleFactor(fieldOptions, dataSetFieldMetadata.getName(), newFieldMetadata);
			}
			newFieldMetadata.setAlias(dataSetFieldMetadata.getAlias());
			newFieldMetadata.setFieldType(dataSetFieldMetadata.getFieldType());
			newFieldMetadata.setName(dataSetFieldMetadata.getName());
			newFieldMetadata.setType(dataStoreFieldMetadata.getType());
			newdataStoreMetadata.addFiedMeta(newFieldMetadata);
		}
		newdataStoreMetadata.setProperties(dataStoreMetadata.getProperties());
		dataStore.setMetaData(newdataStoreMetadata);
	}

	public Map<String, List<String>> getFiltersOnDomainValues() {
		// WorksheetEngineInstance engineInstance = this.getEngineInstance();
		// WorkSheetDefinition workSheetDefinition = (WorkSheetDefinition)
		// engineInstance.getAnalysisState();
		// Map<String, List<String>> toReturn = null;
		// try {
		// toReturn = workSheetDefinition.getFiltersOnDomainValues();
		// } catch (WrongConfigurationForFiltersOnDomainValuesException e) {
		// throw new SpagoBIEngineServiceException(this.getActionName(),
		// e.getMessage(), e);
		// }

		Map<String, List<String>> toReturn = new HashMap();

		return toReturn;
	}

	public List<String> getAllFields() {
		// WorksheetEngineInstance engineInstance = this.getEngineInstance();
		// WorkSheetDefinition workSheetDefinition = (WorkSheetDefinition)
		// engineInstance.getAnalysisState();
		// List<Field> fields = workSheetDefinition.getAllFields();
		// Iterator<Field> it = fields.iterator();
		// List<String> toReturn = new ArrayList<String>();
		// while (it.hasNext()) {
		// Field field = it.next();
		// toReturn.add(field.getEntityId());
		// }
		List<String> toReturn = new ArrayList<String>();
		return toReturn;
	}

	private void recordTemporaryTable(String tableName, IDataSource dataSource) {
		String attributeName = TemporaryTableRecorder.class.getName();
		TemporaryTableRecorder recorder = (TemporaryTableRecorder) this.getHttpSession().getAttribute(attributeName);
		if (recorder == null) {
			recorder = new TemporaryTableRecorder();
		}
		recorder.addTemporaryTable(new TemporaryTable(tableName, dataSource));
		this.getHttpSession().setAttribute(attributeName, recorder);
	}

	public static final String WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_OPTIONS = "options";
	public static final String WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR = "measureScaleFactor";

	private void addMeasuresScaleFactor(JSONArray fieldOptions, String fieldId, FieldMetadata newFieldMetadata) {
		if (fieldOptions != null) {
			for (int i = 0; i < fieldOptions.length(); i++) {
				try {
					JSONObject afield = fieldOptions.getJSONObject(i);
					JSONObject aFieldOptions = afield.getJSONObject(WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_OPTIONS);
					String afieldId = afield.getString("id");
					String scaleFactor = aFieldOptions.optString(WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR);
					if (afieldId.equals(fieldId) && scaleFactor != null) {
						newFieldMetadata.setProperty(WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR, scaleFactor);
						return;
					}
				} catch (Exception e) {
					throw new RuntimeException("An unpredicted error occurred while adding measures scale factor", e);
				}
			}
		}
	}

	// private void checkQbeDataset(IDataSet dataSet) {
	//
	// IDataSet ds = null;
	// if (dataSet instanceof VersionedDataSet) {
	// VersionedDataSet versionedDataSet = (VersionedDataSet) dataSet;
	// ds = versionedDataSet.getWrappedDataset();
	// } else {
	// ds = dataSet;
	// }
	//
	// if (ds instanceof QbeDataSet) {
	// UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
	// String userId = userProfile.getUserId().toString();
	// MetamodelServiceProxy proxy = new MetamodelServiceProxy(userId, servletRequest.getSession());
	// DefaultEngineDatamartRetriever retriever = new DefaultEngineDatamartRetriever(proxy);
	// Map parameters = ds.getParamsMap();
	// if (parameters == null) {
	// parameters = new HashMap();
	// ds.setParamsMap(parameters);
	// }
	// ds.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
	// }
	// }
}
