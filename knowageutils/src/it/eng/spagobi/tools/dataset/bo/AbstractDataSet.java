/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;
import it.eng.spagobi.tools.dataset.common.behaviour.IDataSetBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStoreFilter;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.transformer.IDataStoreTransformer;
import it.eng.spagobi.tools.dataset.common.transformer.PivotDataSetTransformer;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.persist.DataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.sql.SQLStatementConditionalOperators;
import it.eng.spagobi.utilities.sql.SQLStatementConditionalOperators.IConditionalOperator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @authors Angelo Bernabei (angelo.bernabei@eng.it) Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class AbstractDataSet implements IDataSet {

	private int id;
	private String name;
	private String description;
	private String label;
	private Integer categoryId;
	private String categoryCd;

	// duplication ahead!
	private String parameters;
	private Map paramsMap;
	Map<String, Object> properties;

	// Transformer attributes (better to remove them.
	// They should be stored only into dataSetTransformer (see above)
	protected Integer transformerId;
	protected String transformerCd;
	protected String pivotColumnName;
	protected String pivotRowName;
	protected String pivotColumnValue;
	protected boolean numRows;
	protected String organization;

	protected String startDateField;
	protected String endDateField;
	protected String schedulingCronLine;

	protected IDataSource datasourceForWriting;
	protected IDataSource datasourceForReading;

	protected IDataStoreTransformer dataSetTransformer;

	// hook for extension points
	private final Map behaviours;

	private String dsMetadata;
	private String userIn;
	private Date dateIn;

	private String dsType;

	// Attribute related to the particular dataset implementation
	// TODO the do not belong here. just store at this level a generic
	// configuration object that it s then handled properly by the
	// specific subclasses
	protected String resPath;
	protected Object query;
	protected String queryScript;
	protected String queryScriptLanguage;

	protected boolean persisted;
	protected String persistTableName;
	protected boolean scheduled;
	protected String configuration;
	protected List noActiveVersions;

	protected String owner;
	protected boolean isPublic;

	protected Integer scopeId;
	protected String scopeCd;

	private FederationDefinition datasetFederation;

	private static transient Logger logger = Logger.getLogger(AbstractDataSet.class);

	public AbstractDataSet() {
		super();
		behaviours = new HashMap();
	}

	@Override
	public Integer getScopeId() {
		return scopeId;
	}

	@Override
	public void setScopeId(Integer scopeId) {
		this.scopeId = scopeId;
	}

	@Override
	public String getScopeCd() {
		return scopeCd;
	}

	@Override
	public void setScopeCd(String scopeCd) {
		this.scopeCd = scopeCd;
	}

	public AbstractDataSet(SpagoBiDataSet dataSet) {
		super();
		setId(dataSet.getDsId());
		setName(dataSet.getName());
		setLabel(dataSet.getLabel());
		setDescription(dataSet.getDescription());
		setLabel(dataSet.getLabel());
		setConfiguration(dataSet.getConfiguration());
		setCategoryId(dataSet.getCategoryId());
		setParameters(dataSet.getParameters());

		setTransformerId(dataSet.getTransformerId());
		setPivotColumnName(dataSet.getPivotColumnName());
		setPivotRowName(dataSet.getPivotRowName());
		setPivotColumnValue(dataSet.getPivotColumnValue());
		setNumRows(dataSet.isNumRows());
		setDsMetadata(dataSet.getDsMetadata());
		setPersisted(dataSet.isPersisted());
		setPersistTableName(dataSet.getPersistTableName());
		setScheduled(dataSet.isScheduled());
		SpagoBiDataSource dsDataSourceForReading = dataSet.getDataSourceForReading();
		setDataSourceForReading(dsDataSourceForReading != null ? DataSourceFactory.getDataSource(dsDataSourceForReading) : null);
		setPublic(dataSet.is_public());
		setScopeId(dataSet.getScopeId());
		setScopeCd(dataSet.getScopeCd());
		setOwner(dataSet.getOwner());

		if (this.getPivotColumnName() != null && this.getPivotColumnValue() != null && this.getPivotRowName() != null) {
			setDataStoreTransformer(new PivotDataSetTransformer(getPivotColumnName(), getPivotColumnValue(), getPivotRowName(), isNumRows()));
		}

		behaviours = new HashMap();
	}

	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd = new SpagoBiDataSet();

		sbd.setDsId(getId());
		sbd.setLabel(getLabel());
		sbd.setName(getName());
		sbd.setParameters(getParameters());
		sbd.setDescription(getDescription());
		sbd.setCategoryId(getCategoryId());
		sbd.setDsMetadata(getDsMetadata());
		sbd.setConfiguration(getConfiguration());

		sbd.setTransformerId(getTransformerId());
		sbd.setPivotColumnName(getPivotColumnName());
		sbd.setPivotRowName(getPivotRowName());
		sbd.setPivotColumnValue(getPivotColumnValue());
		sbd.setNumRows(isNumRows());
		sbd.setPersisted(isPersisted());
		sbd.setPersistTableName(getPersistTableName());
		sbd.setScheduled(isScheduled());
		IDataSource dataSourceForReading = getDataSourceForReading();
		sbd.setDataSourceForReading(dataSourceForReading != null ? dataSourceForReading.toSpagoBiDataSource() : null);
		sbd.set_public(isPublic());

		sbd.setOrganization(getOrganization());
		sbd.setScopeId(getScopeId());
		sbd.setScopeCd(getScopeCd());
		sbd.setOwner(getOwner());

		sbd.setStartDateField(getStartDateField());
		sbd.setEndDateField(getEndDateField());
		sbd.setSchedulingCronLine(getSchedulingCronLine());

		return sbd;
	}

	// ===============================================
	// Generic dataset's attributes accessor methods
	// ===============================================
	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Integer getCategoryId() {
		return categoryId;
	}

	@Override
	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	@Override
	public void setCategoryCd(String categoryCd) {
		this.categoryCd = categoryCd;
	}

	@Override
	public String getCategoryCd() {
		return categoryCd;
	}

	public boolean hasMetadata() {
		return (getDsMetadata() != null && getDsMetadata().trim().equals("") == false);
	}

	@Override
	public String getDsMetadata() {
		return dsMetadata;
	}

	@Override
	public void setDsMetadata(String dsMetadata) {
		this.dsMetadata = dsMetadata;
	}

	@Override
	public IMetaData getMetadata() {
		IMetaData toReturn = null;
		String xmlMetadata = this.getDsMetadata();
		if (xmlMetadata == null || xmlMetadata.trim().equals("")) {
			logger.error("This dataset has no metadata");
			throw new SpagoBIRuntimeException("This dataset has no metadata");
		}
		DatasetMetadataParser parser = new DatasetMetadataParser();
		try {
			toReturn = parser.xmlToMetadata(xmlMetadata);
		} catch (Exception e) {
			logger.error("Error parsing dataset's metadata", e);
			throw new SpagoBIRuntimeException("Error parsing dataset's metadata", e);
		}
		return toReturn;
	}

	@Override
	public void setMetadata(IMetaData metadata) {
		// do nothings
	}

	@Override
	public String getDsType() {
		return dsType;
	}

	@Override
	public void setDsType(String dsType) {
		this.dsType = dsType;
	}

	// -----------------------------------------------
	// Parameters management
	// -----------------------------------------------

	@Override
	public String getParameters() {
		return parameters;
	}

	@Override
	public Map getParamsMap() {
		DataSetUtilities.fillDefaultValues(this, this.paramsMap);
		return paramsMap;
	}

	@Override
	public void setParamsMap(Map paramsMap) {
		this.paramsMap = paramsMap;
		DataSetUtilities.fillDefaultValues(this, this.paramsMap);
	}

	// these has to be implemented by the user creating a custom DataSet
	@Override
	public Map getProperties() {
		// TODO Auto-generated method stub
		return this.properties;
	}

	@Override
	public void setProperties(Map map) {
		this.properties = map;
	}

	public String getTemporaryTableName() {
		if (this.getParamsMap() == null) {
			return null;
		}
		String toReturn = (String) this.getParamsMap().get(SpagoBIConstants.TEMPORARY_TABLE_NAME);
		return toReturn;
	}

	// -----------------------------------------------
	// Transformer management
	// -----------------------------------------------
	@Override
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	@Override
	public Integer getTransformerId() {
		return transformerId;
	}

	@Override
	public void setTransformerId(Integer transformerId) {
		this.transformerId = transformerId;
	}

	@Override
	public String getTransformerCd() {
		return transformerCd;
	}

	@Override
	public void setTransformerCd(String transformerCd) {
		this.transformerCd = transformerCd;
	}

	@Override
	public String getPivotColumnName() {
		return pivotColumnName;
	}

	@Override
	public void setPivotColumnName(String pivotColumnName) {
		this.pivotColumnName = pivotColumnName;
	}

	@Override
	public String getPivotRowName() {
		return pivotRowName;
	}

	@Override
	public void setPivotRowName(String pivotRowName) {
		this.pivotRowName = pivotRowName;
	}

	@Override
	public String getPivotColumnValue() {
		return pivotColumnValue;
	}

	@Override
	public void setPivotColumnValue(String pivotColumnValue) {
		this.pivotColumnValue = pivotColumnValue;
	}

	@Override
	public boolean isNumRows() {
		return numRows;
	}

	@Override
	public void setNumRows(boolean numRows) {
		this.numRows = numRows;
	}

	@Override
	public boolean hasDataStoreTransformer() {
		return getDataStoreTransformer() != null;
	}

	@Override
	public void removeDataStoreTransformer() {
		setDataStoreTransformer(null);
	}

	@Override
	public void setDataStoreTransformer(IDataStoreTransformer dataSetTransformer) {
		this.dataSetTransformer = dataSetTransformer;
	}

	@Override
	public IDataStoreTransformer getDataStoreTransformer() {
		return this.dataSetTransformer;
	}

	// -----------------------------------------------
	// Extension point hook
	// -----------------------------------------------

	@Override
	public boolean hasBehaviour(String behaviourId) {
		return behaviours.containsKey(behaviourId);
	}

	@Override
	public Object getBehaviour(String behaviourId) {
		return behaviours.get(behaviourId);
	}

	@Override
	public void addBehaviour(IDataSetBehaviour behaviour) {
		behaviours.put(behaviour.getId(), behaviour);
	}

	// ===============================================
	// Custom dataset's attributes accessor methods
	// ===============================================

	public String getResourcePath() {
		return resPath;
	}

	public void setResourcePath(String resPath) {
		this.resPath = resPath;
	}

	public Object getQuery() {
		return query;
	}

	public void setQuery(Object query) {
		this.query = query;
	}

	public String getQueryScript() {
		return queryScript;
	}

	public void setQueryScript(String script) {
		this.queryScript = script;
	}

	public String getQueryScriptLanguage() {
		return queryScriptLanguage;
	}

	public void setQueryScriptLanguage(String queryScriptLanguage) {
		this.queryScriptLanguage = queryScriptLanguage;
	}

	/**
	 * @return the persisted
	 */
	@Override
	public boolean isPersisted() {
		return persisted;
	}

	/**
	 * @param persisted
	 *            the persisted to set
	 */
	@Override
	public void setPersisted(boolean persisted) {
		this.persisted = persisted;
	}

	/**
	 * @return the scheduled
	 */
	@Override
	public boolean isScheduled() {
		return scheduled;
	}

	/**
	 * @param scheduled
	 *            the scheduled to set
	 */
	@Override
	public void setScheduled(boolean scheduled) {
		this.scheduled = scheduled;
	}

	/**
	 * @return the flatDataset
	 */
	@Override
	public boolean isFlatDataset() {
		return this instanceof FlatDataSet;
	}

	/**
	 * @return the flatTableName
	 */
	@Override
	public String getFlatTableName() {
		if (!this.isFlatDataset()) {
			throw new SpagoBIRuntimeException("This dataset is not a flat dataset!!!");
		}
		FlatDataSet thisDataSet = (FlatDataSet) this;
		return thisDataSet.getTableName();
	}

	/**
	 * @return the persistTableName
	 */
	@Override
	public String getPersistTableName() {
		return persistTableName;
	}

	/**
	 * @param persistTableName
	 *            the persistTableName to set
	 */
	@Override
	public void setPersistTableName(String persistTableName) {
		this.persistTableName = persistTableName;
	}

	/**
	 * @return the configuration
	 */
	@Override
	public String getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration
	 *            the configuration to set
	 */
	@Override
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	/**
	 * @return the userIn
	 */
	@Override
	public String getUserIn() {
		return userIn;
	}

	/**
	 * @param userIn
	 *            the userIn to set
	 */
	@Override
	public void setUserIn(String userIn) {
		this.userIn = userIn;
	}

	/**
	 * @return the dateIn
	 */
	@Override
	public Date getDateIn() {
		return dateIn;
	}

	/**
	 * @param dateIn
	 *            the dateIn to set
	 */
	@Override
	public void setDateIn(Date dateIn) {
		this.dateIn = dateIn;
	}

	/**
	 * @return the oldVersions
	 */
	@Override
	public List getNoActiveVersions() {
		return noActiveVersions;
	}

	/**
	 * @param oldVersions
	 *            the oldVersions to set
	 */
	@Override
	public void setNoActiveVersions(List noActiveVersions) {
		this.noActiveVersions = noActiveVersions;
	}

	/**
	 * @return the owner
	 */
	@Override
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	@Override
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the isPublic
	 */
	@Override
	public boolean isPublic() {
		return isPublic;
	}

	/**
	 * @param isPublic
	 *            the isPublic to set
	 */
	@Override
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	// ===============================================
	// Core methods
	// ===============================================
	@Override
	public void loadData() {
		loadData(0, -1, -1);
	}

	@Override
	public void loadData(int offset, int fetchSize, int maxResults) {
		throw new RuntimeException("Unsupported method");
	}

	@Override
	public String getTableNameForReading() {
		if (isPersisted()) {
			return getPersistTableName();
		} else if (isFlatDataset()) {
			return getFlatTableName();
		} else {
			return null;
			// throw new RuntimeException("Dataset is not persisted");
		}
	}

	@Override
	public IDataSetTableDescriptor persist(String tableName, IDataSource dataSource) {
		PersistedTableManager persister = new PersistedTableManager();
		try {
			persister.persistDataSet(this, dataSource, tableName);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while persisting dataset", e);
		}
		List<String> fields = this.getFieldsList();
		IDataSetTableDescriptor descriptor = null;
		try {
			descriptor = TemporaryTableManager.getTableDescriptor(fields, tableName, dataSource);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting table information", e);
		}
		return descriptor;
	}

	public List<String> getFieldsList() {
		List<String> toReturn = new ArrayList<String>();
		IMetaData metadata = this.getMetadata();
		int count = metadata.getFieldCount();
		for (int i = 0; i < count; i++) {
			toReturn.add(metadata.getFieldName(i));
		}
		return toReturn;
	}

	/**
	 * Get the values for a certain dataset's field, considering a optional filter. In case the dataset is persisted or flat, the values are retrieved by the
	 * persistence table. In case the dataset is neither persisted nor flat, it will look for a temporary table with the same signature using
	 * TemporaryTableManager; in case there is no temporary table, the dataset will be persisted, therefore the datasource must be read and write or a
	 * datasource for writing must be provided.
	 *
	 * @param fieldName
	 *            The dataset's field
	 * @param start
	 *            The offset on results
	 * @param limit
	 *            The limit on result
	 * @param filter
	 *            The optional filter
	 * @return The datastore containing the values for the dataset's field
	 */
	@Override
	public IDataStore getDomainValues(String fieldName, Integer start, Integer limit, IDataStoreFilter filter) {
		if (this.isPersisted() || this.isFlatDataset()) {
			return getDomainValuesFromPersistenceTable(fieldName, start, limit, filter);
		} else {
			return getDomainValuesFromTemporaryTable(fieldName, start, limit, filter);
		}
	}

	protected IDataStore getDomainValuesFromPersistenceTable(String fieldName, Integer start, Integer limit, IDataStoreFilter filter) {
		IDataStore toReturn = null;
		try {
			String tableName = this.getTableNameForReading();
			IDataSource dataSource = this.getDataSourceForReading();
			StringBuffer buffer = new StringBuffer("Select DISTINCT " + AbstractJDBCDataset.encapsulateColumnName(fieldName, dataSource) + " FROM " + tableName);
			IDataSetTableDescriptor tableDescriptor = new DataSetTableDescriptor(this);
			manageFilterOnDomainValues(buffer, fieldName, tableDescriptor, filter);
			String sqlStatement = buffer.toString();
			toReturn = dataSource.executeStatement(sqlStatement, start, limit);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting domain values from persistence table", e);
		}
		return toReturn;
	}

	protected IDataStore getDomainValuesFromTemporaryTable(String fieldName, Integer start, Integer limit, IDataStoreFilter filter) {

		IDataStore toReturn = null;
		try {
			String tableName = this.getTemporaryTableName();
			logger.debug("Temporary table name : [" + tableName + "]");
			if (tableName == null) {
				logger.error("Temporary table name not set, cannot proceed!!");
				throw new SpagoBIEngineRuntimeException("Temporary table name not set");
			}
			String signature = this.getSignature();
			IDataSetTableDescriptor tableDescriptor = null;
			if (signature.equals(TemporaryTableManager.getLastDataSetSignature(tableName))) {
				// signature matches: no need to create a TemporaryTable
				tableDescriptor = TemporaryTableManager.getLastDataSetTableDescriptor(tableName);
			} else {
				IDataSource dataSource = this.getDataSource();
				if (dataSource == null || dataSource.checkIsReadOnly()) {
					logger.debug(dataSource == null ? "Datasource not set" : "Datasource is read only");
					logger.debug("Getting datasource for writing...");
					dataSource = this.getDataSourceForWriting();
				}
				if (dataSource == null) {
					logger.error("Datasource for persistence not set, cannot proceed!!");
					throw new SpagoBIEngineRuntimeException("Datasource for persistence not set");
				}

				tableDescriptor = this.persist(tableName, dataSource);
				TemporaryTableManager.setLastDataSetTableDescriptor(tableName, tableDescriptor);
				TemporaryTableManager.setLastDataSetSignature(tableName, signature);
			}
			IDataSource dataSource = tableDescriptor.getDataSource();
			String filterColumnName = tableDescriptor.getColumnName(fieldName);
			StringBuffer buffer = new StringBuffer("Select DISTINCT " + AbstractJDBCDataset.encapsulateColumnName(filterColumnName, dataSource) + " FROM "
					+ tableName);
			manageFilterOnDomainValues(buffer, fieldName, tableDescriptor, filter);
			String sqlStatement = buffer.toString();
			toReturn = TemporaryTableManager.queryTemporaryTable(sqlStatement, dataSource, start, limit);
		} catch (Exception e) {
			logger.error("Error loading the domain values for the field " + fieldName, e);
			throw new SpagoBIEngineRuntimeException("Error loading the domain values for the field " + fieldName, e);

		}
		return toReturn;
	}

	protected void manageFilterOnDomainValues(StringBuffer buffer, String fieldName, IDataSetTableDescriptor tableDescriptor, IDataStoreFilter filter) {
		if (filter != null) {
			String filterColumnName = tableDescriptor.getColumnName(fieldName);
			if (filterColumnName == null) {
				throw new SpagoBIRuntimeException("Field name [" + fieldName + "] not found");
			}
			String columnName = tableDescriptor.getColumnName(fieldName);
			Class clazz = tableDescriptor.getColumnType(fieldName);
			String value = getFilterValue(filter.getValue(), clazz);
			IConditionalOperator conditionalOperator = SQLStatementConditionalOperators.getOperator(filter.getOperator());
			String temp = conditionalOperator.apply(AbstractJDBCDataset.encapsulateColumnName(columnName, tableDescriptor.getDataSource()),
					new String[] { value });
			buffer.append(" WHERE " + temp);
		}
	}

	private String getFilterValue(String value, Class clazz) {
		String toReturn = null;
		if (String.class.isAssignableFrom(clazz)) {
			value = StringUtils.escapeQuotes(value);
			toReturn = StringUtils.bound(value, "'");
		} else if (Number.class.isAssignableFrom(clazz)) {
			toReturn = value;
		} else if (Boolean.class.isAssignableFrom(clazz)) {
			toReturn = value;
		} else {
			// TODO manage other types, such as date and timestamp
			throw new SpagoBIRuntimeException("Unsupported operation: cannot filter on a fild type " + clazz.getName());
		}
		return toReturn;
	}

	// public IDataSource getDataSourceForReading() {
	// return getDataSource();
	//
	// // if (isPersisted()) {
	// // return getDataSourcePersist();
	// // } else if (isFlatDataset()) {
	// // return getDataSource();
	// // } else {
	// // return null;
	// // }
	// }

	@Override
	public String getOrganization() {
		return organization;
	}

	@Override
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	@Override
	public IDataSource getDataSourceForWriting() {
		return this.datasourceForWriting;
	}

	@Override
	public void setDataSourceForWriting(IDataSource dataSource) {
		this.datasourceForWriting = dataSource;
	}

	@Override
	public IDataSource getDataSourceForReading() {
		return datasourceForReading;
	}

	@Override
	public void setDataSourceForReading(IDataSource datasourceForReading) {
		this.datasourceForReading = datasourceForReading;
	}

	@Override
	public FederationDefinition getDatasetFederation() {
		return datasetFederation;
	}

	@Override
	public void setDatasetFederation(FederationDefinition datasetFederation) {
		this.datasetFederation = datasetFederation;
	}

	@Override
	public String getStartDateField() {
		return startDateField;
	}

	@Override
	public void setStartDateField(String startDateField) {
		this.startDateField = startDateField;
	}

	@Override
	public String getEndDateField() {
		return endDateField;
	}

	@Override
	public void setEndDateField(String endDateField) {
		this.endDateField = endDateField;
	}

	@Override
	public String getSchedulingCronLine() {
		return schedulingCronLine;
	}

	@Override
	public void setSchedulingCronLine(String schedulingCronLine) {
		this.schedulingCronLine = schedulingCronLine;
	}

	@Override
	public Map<String, ?> getDefaultValues() {
		return DataSetUtilities.getParamsDefaultValues(this);
	}

}
