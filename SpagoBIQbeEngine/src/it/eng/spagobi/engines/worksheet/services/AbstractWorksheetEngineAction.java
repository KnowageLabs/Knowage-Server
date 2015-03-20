/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.services;

import it.eng.qbe.query.AbstractSelectField;
import it.eng.qbe.query.CriteriaConstants;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.WhereField.Operand;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.qbe.statement.AbstractStatement;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.worksheet.WorksheetEngineInstance;
import it.eng.spagobi.engines.worksheet.bo.Attribute;
import it.eng.spagobi.engines.worksheet.bo.Field;
import it.eng.spagobi.engines.worksheet.bo.FieldOption;
import it.eng.spagobi.engines.worksheet.bo.FieldOptions;
import it.eng.spagobi.engines.worksheet.bo.Sheet;
import it.eng.spagobi.engines.worksheet.bo.WorkSheetDefinition;
import it.eng.spagobi.engines.worksheet.bo.WorksheetFieldsOptions;
import it.eng.spagobi.engines.worksheet.exceptions.WrongConfigurationForFiltersOnDomainValuesException;
import it.eng.spagobi.engines.worksheet.serializer.json.WorkSheetSerializationUtils;
import it.eng.spagobi.engines.worksheet.utils.crosstab.CrosstabQueryCreator;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCHiveDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.FilteringBehaviour;
import it.eng.spagobi.tools.dataset.common.behaviour.SelectableFieldsBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.persist.DataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTable;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableRecorder;
import it.eng.spagobi.utilities.engines.AbstractEngineAction;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.safehaus.uuid.UUIDGenerator;

/**
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public abstract class AbstractWorksheetEngineAction extends AbstractEngineAction {

	private static final long serialVersionUID = 6446776217192515816L;

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(AbstractWorksheetEngineAction.class);
	public static transient Logger auditlogger = Logger.getLogger("audit.query");

	public IDataStore executeWorksheetQuery(String worksheetQuery, Integer start, Integer limit) {

		IDataStore dataStore = null;
		IDataSet dataset = getDataSet();

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
		Integer maxSize = QbeEngineConfig.getInstance().getResultLimit();
		boolean overflow = maxSize != null && resultNumber >= maxSize;
		if (overflow) {
			logger.warn("Query results number [" + resultNumber + "] exceeds max result limit that is [" + maxSize + "]");
			auditlogger.info("[" + userProfile.getUserId() + "]:: max result limit [" + maxSize + "] exceeded with SQL: " + worksheetQuery);
		}

		return dataStore;
	}

	private IDataStore useDataSetStrategy(String worksheetQuery, IDataSet dataset, Integer start, Integer limit) {
		IDataStore dataStore = null;

		UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);

		logger.debug("Querying dataset's flat/persistence table: user [" + userProfile.getUserId() + "] (SQL): [" + worksheetQuery + "]");
		auditlogger.info("Querying dataset's flat/persistence table: user [" + userProfile.getUserId() + "] (SQL): [" + worksheetQuery + "]");

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
			String message = "An error occurred in " + getActionName() + " service while querying temporary table";
			SpagoBIEngineServiceException exception = new SpagoBIEngineServiceException(getActionName(), message, e);
			exception.addHint("Check if the crosstab's query is properly formed: [" + worksheetQuery + "]");
			exception.addHint("Check connection configuration: connection's user must have DROP and CREATE privileges");
			throw exception;
		}
	}

	private IDataStore useTemporaryTableStrategy(String worksheetQuery, Integer start, Integer limit) {

		IDataStore dataStore = null;

		UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
		IDataSource dataSource = this.getEngineInstance().getDataSourceForWriting();

		logger.debug("Querying temporary table: user [" + userProfile.getUserId() + "] (SQL): [" + worksheetQuery + "]");
		auditlogger.info("Querying temporary table: user [" + userProfile.getUserId() + "] (SQL): [" + worksheetQuery + "]");

		try {
			dataStore = TemporaryTableManager.queryTemporaryTable(worksheetQuery, dataSource, start, limit);
		} catch (Exception e) {
			logger.debug("Query execution aborted because of an internal exception");
			String message = "An error occurred in " + getActionName() + " service while querying temporary table";
			SpagoBIEngineServiceException exception = new SpagoBIEngineServiceException(getActionName(), message, e);
			// exception.addHint("Check if the base query is properly formed: [" + baseQuery + "]");
			exception.addHint("Check if the crosstab's query is properly formed: [" + worksheetQuery + "]");
			exception.addHint("Check connection configuration: connection's user must have DROP and CREATE privileges");
			throw exception;
		}
		return dataStore;
	}

	// private IDataStore useInLineViewStrategy(String worksheetQuery,
	// String baseQuery, Integer start, Integer limit) {
	//
	// IDataStore dataStore = null;
	//
	// UserProfile userProfile = (UserProfile)getEnv().get(EngineConstants.ENV_USER_PROFILE);
	// ConnectionDescriptor connection = (ConnectionDescriptor)getDataSource().getConfiguration().loadDataSourceProperties().get("connection");
	// DataSource dataSource = getDataSource(connection);
	//
	// int beginIndex = worksheetQuery.toUpperCase().indexOf(" FROM ") + " FROM ".length();
	// int endIndex = worksheetQuery.indexOf(" ", beginIndex);
	// String inlineSQLQuery = worksheetQuery.substring(0, beginIndex) + " ( " + baseQuery + " ) TEMP " + worksheetQuery.substring(endIndex);
	// logger.debug("Executable query for user [" + userProfile.getUserId() + "] (SQL): [" + inlineSQLQuery + "]");
	// auditlogger.info("[" + userProfile.getUserId() + "]:: SQL: " + inlineSQLQuery);
	// JDBCDataSet dataSet = new JDBCDataSet();
	// dataSet.setDataSource(dataSource);
	// dataSet.setQuery(inlineSQLQuery);
	// if (start != null && limit != null) {
	// dataSet.loadData(start, limit, -1);
	// } else {
	// dataSet.loadData();
	// }
	// dataStore = (DataStore) dataSet.getDataStore();
	// return dataStore;
	// }

	@Override
	public WorksheetEngineInstance getEngineInstance() {
		return (WorksheetEngineInstance) getAttributeFromSession(WorksheetEngineInstance.class.getName());
	}

	public void setEngineInstance(WorksheetEngineInstance engineInstance) {
		setAttributeInSession(WorksheetEngineInstance.class.getName(), engineInstance);
	}

	public IDataSource getDataSource() {
		WorksheetEngineInstance engineInstance = getEngineInstance();
		if (engineInstance == null) {
			return null;
		}
		return engineInstance.getDataSource();
	}

	public IDataSet getDataSet() {
		WorksheetEngineInstance engineInstance = getEngineInstance();
		if (engineInstance == null) {
			return null;
		}
		return engineInstance.getDataSet();
	}

	public void setDataSource(IDataSource dataSource) {
		WorksheetEngineInstance engineInstance = getEngineInstance();
		if (engineInstance == null) {
			return;
		}
		engineInstance.setDataSource(dataSource);
	}

	public IDataSetTableDescriptor persistDataSet() {

		WorksheetEngineInstance engineInstance = getEngineInstance();
		IDataSet dataset = engineInstance.getDataSet();

		if (dataset.isPersisted() || dataset.isFlatDataset()) {
			return getDescriptorFromDatasetMeta(dataset);
		} else {
			String tableName = engineInstance.getTemporaryTableName();
			return persistDataSetWithTemporaryTable(dataset, tableName);
		}

	}

	/**
	 * Persist the data set in the db and returns the descriptor of the created table
	 * 
	 * @param dataset
	 * @param tableName
	 * @return
	 */
	private IDataSetTableDescriptor persistDataSetWithTemporaryTable(IDataSet dataset, String tableName) {
		// get temporary table name

		logger.debug("Temporary table name is [" + tableName + "]");

		HttpSession session = this.getHttpSession();
		synchronized (session) { // we synchronize this block in order to avoid concurrent requests

			// set all filters into dataset, because dataset's getSignature() and persist() methods may depend on them

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

			// drop the temporary table if one exists
			try {
				logger.debug("Signature does not match: dropping TemporaryTable " + tableName + " if it exists...");
				TemporaryTableManager.dropTableIfExists(tableName, getEngineInstance().getDataSourceForWriting());
			} catch (Exception e) {
				logger.error("Impossible to drop the temporary table with name " + tableName, e);
				throw new SpagoBIEngineRuntimeException("Impossible to drop the temporary table with name " + tableName, e);
			}

			IDataSetTableDescriptor td = null;

			try {
				logger.debug("Persisting dataset ...");

				td = dataset.persist(tableName, getEngineInstance().getDataSourceForWriting());
				this.recordTemporaryTable(tableName, getEngineInstance().getDataSourceForWriting());

				/**
				 * Do not remove comments from the following line: we cannot change the datatset state, since we are only temporarily persisting the dataset,
				 * but the dataset itself could change during next user interaction (example: the user is using Qbe and he will change the dataset itself). We
				 * will use TemporaryTableManager to store this kind of information.
				 * 
				 * dataset.setDataSourceForReading(getEngineInstance(). getDataSourceForWriting()); dataset.setPersisted(true);
				 * dataset.setPersistTableName(td.getTableName());
				 */

				logger.debug("Dataset persisted");
			} catch (Throwable t) {
				logger.error("Error while persisting dataset", t);
				throw new SpagoBIRuntimeException("Error while persisting dataset", t);
			}

			logger.debug("Dataset persisted successfully. Table descriptor : " + td);
			TemporaryTableManager.setLastDataSetSignature(tableName, signature);
			TemporaryTableManager.setLastDataSetTableDescriptor(tableName, td);
			return td;
		}
	}

	/**
	 * The table is already present in the db because the dataset is flat or persisted. So we take the descriptor of the table from the metadata of the dataset
	 * 
	 * @param dataset
	 * @return
	 */
	private IDataSetTableDescriptor getDescriptorFromDatasetMeta(IDataSet dataset) {
		logger.debug("Getting the TableDescriptor for the dataset with label [" + dataset.getLabel() + "]");
		IDataSetTableDescriptor td = new DataSetTableDescriptor(dataset);
		logger.debug("Table descriptor successully created : " + td);
		return td;
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

	public Map<String, List<String>> getFiltersOnDomainValues() {
		WorksheetEngineInstance engineInstance = this.getEngineInstance();
		WorkSheetDefinition workSheetDefinition = (WorkSheetDefinition) engineInstance.getAnalysisState();
		Map<String, List<String>> toReturn = null;
		try {
			toReturn = workSheetDefinition.getFiltersOnDomainValues();
		} catch (WrongConfigurationForFiltersOnDomainValuesException e) {
			throw new SpagoBIEngineServiceException(this.getActionName(), e.getMessage(), e);
		}
		return toReturn;
	}

	public Map<String, List<String>> getSheetFiltersOnDomainValues(String sheetName) {
		WorksheetEngineInstance engineInstance = this.getEngineInstance();
		WorkSheetDefinition workSheetDefinition = (WorkSheetDefinition) engineInstance.getAnalysisState();
		Sheet sheet = workSheetDefinition.getSheet(sheetName);
		List<Attribute> sheetFilters = sheet.getFiltersOnDomainValues();
		Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
		Iterator<Attribute> it = sheetFilters.iterator();
		while (it.hasNext()) {
			Attribute attribute = it.next();
			toReturn.put(attribute.getEntityId(), attribute.getValuesAsList());
		}
		return toReturn;
	}

	public Map<String, List<String>> getGlobalFiltersOnDomainValues() {
		WorksheetEngineInstance engineInstance = this.getEngineInstance();
		WorkSheetDefinition workSheetDefinition = (WorkSheetDefinition) engineInstance.getAnalysisState();
		Map<String, List<String>> toReturn = workSheetDefinition.getGlobalFiltersAsMap();
		return toReturn;
	}

	public List<String> getAllFields() {
		WorksheetEngineInstance engineInstance = this.getEngineInstance();
		WorkSheetDefinition workSheetDefinition = (WorkSheetDefinition) engineInstance.getAnalysisState();
		List<Field> fields = workSheetDefinition.getAllFields();
		Iterator<Field> it = fields.iterator();
		List<String> toReturn = new ArrayList<String>();
		while (it.hasNext()) {
			Field field = it.next();
			toReturn.add(field.getEntityId());
		}
		return toReturn;
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

	public List<WhereField> getOptionalFilters(JSONObject optionalUserFilters) throws JSONException {
		if (optionalUserFilters != null) {
			return transformIntoWhereClauses(optionalUserFilters);
		} else {
			return new ArrayList<WhereField>();
		}
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

	/**
	 * Sets the worksheet definition into the worksheet engine instance
	 * 
	 * @param worksheetDefinitionJSON
	 *            The worksheet definition in JSON format
	 * @throws Exception
	 */
	public void updateWorksheetDefinition(JSONObject worksheetDefinitionJSON) throws Exception {

		WorkSheetDefinition workSheetDefinition = (WorkSheetDefinition) SerializationManager.deserialize(worksheetDefinitionJSON, "application/json",
				WorkSheetDefinition.class);

		WorksheetEngineInstance worksheetEngineInstance = getEngineInstance();
		worksheetEngineInstance.setAnalysisState(workSheetDefinition);
	}

	protected void adjustMetadata(DataStore dataStore, IDataSet dataset, IDataSetTableDescriptor descriptor) {
		adjustMetadata(dataStore, dataset, descriptor, null);
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

	private void addMeasuresScaleFactor(JSONArray fieldOptions, String fieldId, FieldMetadata newFieldMetadata) {
		if (fieldOptions != null) {
			for (int i = 0; i < fieldOptions.length(); i++) {
				try {
					JSONObject afield = fieldOptions.getJSONObject(i);
					JSONObject aFieldOptions = afield.getJSONObject(WorkSheetSerializationUtils.WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_OPTIONS);
					String afieldId = afield.getString("id");
					String scaleFactor = aFieldOptions.optString(WorkSheetSerializationUtils.WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR);
					if (afieldId.equals(fieldId) && scaleFactor != null) {
						newFieldMetadata.setProperty(WorkSheetSerializationUtils.WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR, scaleFactor);
						return;
					}
				} catch (Exception e) {
					throw new RuntimeException("An unpredicted error occurred while adding measures scale factor", e);
				}
			}
		}
	}

	public it.eng.spagobi.tools.dataset.common.datastore.IDataStore getUserSheetFilterValues(String sheetName, String fieldName) throws JSONException {

		it.eng.spagobi.tools.dataset.common.datastore.IDataStore dataStore = null;
		JSONObject gridDataFeed = null;

		WorksheetEngineInstance engineInstance = getEngineInstance();
		Assert.assertNotNull(engineInstance, "It's not possible to execute " + this.getActionName()
				+ " service before having properly created an instance of EngineInstance class");

		// persist dataset into temporary table
		IDataSetTableDescriptor descriptor = this.persistDataSet();
		IDataSet dataset = engineInstance.getDataSet();

		// Get the order type of the field values in the field metadata
		int fieldIndex = dataset.getMetadata().getFieldIndex(fieldName);
		IFieldMetaData dataSetFieldMetadata = dataset.getMetadata().getFieldMeta(fieldIndex);
		String orderType = AbstractSelectField.ORDER_ASC;// default ascendant
		String orderTypeMeta = (String) dataSetFieldMetadata.getProperty(IFieldMetaData.ORDERTYPE);
		if (orderTypeMeta != null && (orderTypeMeta.equals(AbstractSelectField.ORDER_ASC) || orderTypeMeta.equals(AbstractSelectField.ORDER_DESC))) {
			orderType = orderTypeMeta;
		}

		// build SQL query against temporary table
		List<WhereField> whereFields = new ArrayList<WhereField>();
		if (!dataset.hasBehaviour(FilteringBehaviour.ID)) {
			Map<String, List<String>> globalFilters = getGlobalFiltersOnDomainValues();
			List<WhereField> temp = transformIntoWhereClauses(globalFilters);
			whereFields.addAll(temp);
		}
		Map<String, List<String>> sheetFilters = getSheetFiltersOnDomainValues(sheetName);
		List<WhereField> temp = transformIntoWhereClauses(sheetFilters);
		whereFields.addAll(temp);

		String worksheetQuery = this.buildSqlStatement(fieldName, descriptor, whereFields, orderType);
		// execute SQL query against temporary table
		logger.debug("Executing query on temporary table : " + worksheetQuery);
		dataStore = this.executeWorksheetQuery(worksheetQuery, null, null);
		LogMF.debug(logger, "Query on temporary table executed successfully; datastore obtained: {0}", dataStore);
		Assert.assertNotNull(dataStore, "Datastore obatined is null!!");
		/*
		 * since the datastore, at this point, is a JDBC datastore, it does not contain information about measures/attributes, fields' name... therefore we
		 * adjust its metadata
		 */
		this.adjustMetadata((it.eng.spagobi.tools.dataset.common.datastore.DataStore) dataStore, dataset, descriptor);
		LogMF.debug(logger, "Adjusted metadata: {0}", dataStore.getMetaData());
		it.eng.spagobi.tools.dataset.common.datastore.DataStore clone = this.clone(dataStore);
		logger.debug("Decoding dataset ...");
		this.applyOptions(dataStore);
		dataStore = dataset.decode(dataStore);
		LogMF.debug(logger, "Dataset decoded: {0}", dataStore);

		IMetaData metadata = dataStore.getMetaData();
		IFieldMetaData fieldMetadata = metadata.getFieldMeta(0);
		IMetaData newMetadata = new MetaData();
		newMetadata.addFiedMeta(fieldMetadata);
		newMetadata.addFiedMeta(new FieldMetadata(fieldMetadata.getName() + "_description", String.class));
		clone.setMetaData(newMetadata);
		long count = clone.getRecordsCount();
		for (long i = 0; i < count; i++) {
			it.eng.spagobi.tools.dataset.common.datastore.IRecord record = clone.getRecordAt((int) i);
			IField field = dataStore.getRecordAt((int) i).getFieldAt(0);
			String description = field.getDescription() != null ? field.getDescription().toString() : field.getValue().toString();
			record.appendField(new it.eng.spagobi.tools.dataset.common.datastore.Field(description));
		}

		return clone;

	}

	public void applyOptions(IDataStore dataStore) {
		WorksheetEngineInstance engineInstance = this.getEngineInstance();
		WorkSheetDefinition definition = engineInstance.getTemplate().getWorkSheetDefinition();
		WorksheetFieldsOptions options = definition.getFieldsOptions();
		IMetaData metadata = dataStore.getMetaData();
		int fieldsCount = metadata.getFieldCount();
		for (int i = 0; i < fieldsCount; i++) {
			IFieldMetaData fieldMetadata = metadata.getFieldMeta(i);
			FieldOptions fieldOptions = options.getOptionsForFieldByFieldId(fieldMetadata.getName());
			if (fieldOptions != null) {
				// there are options for the field
				logger.debug("Field [name : " + fieldMetadata.getName() + " ; alias : " + fieldMetadata.getAlias() + "] has options set");
				Map properties = fieldMetadata.getProperties();
				List<FieldOption> list = fieldOptions.getOptions();
				Iterator<FieldOption> it = list.iterator();
				while (it.hasNext()) {
					FieldOption option = it.next();
					String name = option.getName();
					Object value = option.getValue();
					logger.debug("Putting option [name : " + name + " ; value : " + value + "] into field [name : " + fieldMetadata.getName() + " ; alias : "
							+ fieldMetadata.getAlias() + "]");
					properties.put(name, value);
				}
			} else {
				logger.debug("Field [name : " + fieldMetadata.getName() + " ; alias : " + fieldMetadata.getAlias() + "] has no options set");
			}
		}

	}

	private it.eng.spagobi.tools.dataset.common.datastore.DataStore clone(it.eng.spagobi.tools.dataset.common.datastore.IDataStore dataStore) {
		it.eng.spagobi.tools.dataset.common.datastore.DataStore toReturn = new it.eng.spagobi.tools.dataset.common.datastore.DataStore();
		IMetaData metadata = dataStore.getMetaData();
		toReturn.setMetaData(metadata);
		long count = dataStore.getRecordsCount();
		for (long i = 0; i < count; i++) {
			it.eng.spagobi.tools.dataset.common.datastore.IRecord record = dataStore.getRecordAt((int) i);
			it.eng.spagobi.tools.dataset.common.datastore.IField field = record.getFieldAt(0);
			Object value = field.getValue();
			it.eng.spagobi.tools.dataset.common.datastore.IRecord newRecord = new it.eng.spagobi.tools.dataset.common.datastore.Record();
			newRecord.appendField(new it.eng.spagobi.tools.dataset.common.datastore.Field(value));
			toReturn.appendRecord(newRecord);
		}
		return toReturn;
	}

	protected String buildSqlStatement(String fieldName, IDataSetTableDescriptor descriptor, List<WhereField> filters, String ordeType) {
		List<String> fieldNames = new ArrayList<String>();
		fieldNames.add(fieldName);
		return CrosstabQueryCreator.getTableQuery(fieldNames, true, descriptor, filters, ordeType, fieldNames);
	}

}