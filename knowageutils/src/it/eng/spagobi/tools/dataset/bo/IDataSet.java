/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.IDataSetBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStoreFilter;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.transformer.IDataStoreTransformer;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IDataSet {

	String getDsMetadata();

	void setDsMetadata(String dsMetadata);

	IMetaData getMetadata();

	void setMetadata(IMetaData metadata);

	// general properties ....
	int getId();

	void setId(int id);

	String getName();

	void setName(String name);

	String getDescription();

	void setDescription(String description);

	String getLabel();

	void setLabel(String label);

	Integer getCategoryId();

	void setCategoryId(Integer categoryId);

	String getCategoryCd();

	void setCategoryCd(String categoryCd);

	String getDsType();

	void setDsType(String dsType);

	String getConfiguration();

	void setConfiguration(String configuration);

	Map getProperties();

	void setProperties(Map map);

	String getOwner();

	void setOwner(String owner);

	boolean isPublic();

	void setPublic(boolean isPublic);

	String getUserIn();

	void setUserIn(String userIn);

	Date getDateIn();

	void setDateIn(Date dateIn);

	Integer getScopeId();

	void setScopeId(Integer scopeId);

	String getScopeCd();

	void setScopeCd(String scopeCd);

	// parametrization ....
	// --------------------------------------------------------------------------------------------------
	// INVESTIGATE: why this 2 similar methods ??? FIND OUT & REFACTOR !
	String getParameters();

	void setParameters(String parameters);

	Map getParamsMap();

	void setParamsMap(Map params);

	// --------------------------------------------------------------------------------------------------

	// profilation ...
	public Map getUserProfileAttributes();

	public void setUserProfileAttributes(Map<String, Object> attributes);

	// execution ...
	// --------------------------------------------------------------------------------------------------
	void loadData();

	void loadData(int offset, int fetchSize, int maxResults);

	// --------------------------------------------------------------------------------------------------

	IDataStore getDataStore();

	// extension points ...
	boolean hasBehaviour(String behaviourId);

	Object getBehaviour(String behaviourId);

	void addBehaviour(IDataSetBehaviour behaviour);

	// =================================================================================================
	// TO BE DEPRECATED ( do not cross this line ;-) )
	// =================================================================================================
	Integer getTransformerId();

	void setTransformerId(Integer transformerId);

	String getTransformerCd();

	void setTransformerCd(String transfomerCd);

	String getPivotColumnName();

	void setPivotColumnName(String pivotColumnName);

	String getPivotRowName();

	void setPivotRowName(String pivotRowName);

	boolean isNumRows();

	void setNumRows(boolean numRows);

	String getPivotColumnValue();

	void setPivotColumnValue(String pivotColumnValue);

	boolean hasDataStoreTransformer();

	void removeDataStoreTransformer();

	void setAbortOnOverflow(boolean abortOnOverflow);

	void addBinding(String bindingName, Object bindingValue);

	void setDataStoreTransformer(IDataStoreTransformer transformer);

	IDataStoreTransformer getDataStoreTransformer();

	boolean isPersisted();

	void setPersisted(boolean persisted);

	boolean isScheduled();

	void setScheduled(boolean scheduled);

	boolean isFlatDataset();

	String getFlatTableName();

	List getNoActiveVersions();

	void setNoActiveVersions(List noActiveVersions);

	String getPersistTableName();

	void setPersistTableName(String persistTableName);

	// TODO these methods do NOT belong to the dataset interface. remove them and refactor the code.
	// --------------------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------------------
	// TODO these methods must be moved into a proper factory that convert SpagoBI BO into data bean passed
	// to the DAO. For the conversion from data bean and SpagoBI BO such a factory alredy exist
	// NOTE: SpagoBiDataSet: change when possible the name SpagoBiDataSet following a convention common to
	// all data bean
	SpagoBiDataSet toSpagoBiDataSet();

	// --------------------------------------------------------------------------------------------------

	IDataStore test();

	IDataStore test(int offset, int fetchSize, int maxResults);

	String getSignature();

	IDataSetTableDescriptor persist(String tableName, IDataSource dataSource);

	/**
	 * Get the values for a certain dataset's field, considering a optional filter.
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
	public IDataStore getDomainValues(String fieldName, Integer start, Integer limit, IDataStoreFilter filter);

	public IDataStore decode(IDataStore datastore);

	boolean isCalculateResultNumberOnLoadEnabled();

	void setCalculateResultNumberOnLoad(boolean enabled);

	public void setDataSource(IDataSource dataSource);

	public IDataSource getDataSource();

	public String getTableNameForReading();

	public IDataSource getDataSourceForReading();

	public void setDataSourceForReading(IDataSource dataSource);

	public String getOrganization();

	public void setOrganization(String organization);

	public IDataSource getDataSourceForWriting();

	public void setDataSourceForWriting(IDataSource dataSource);

	public FederationDefinition getDatasetFederation();

	public void setDatasetFederation(FederationDefinition datasetFederation);

	public String getStartDateField();

	public void setStartDateField(String startDateField);

	public String getEndDateField();

	public void setEndDateField(String endDateField);

	public String getSchedulingCronLine();

	public void setSchedulingCronLine(String schedulingCronLine);

	public Map<String, ?> getDefaultValues();

}