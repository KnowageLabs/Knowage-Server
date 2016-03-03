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

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.IDataSetBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStoreFilter;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.transformer.IDataStoreTransformer;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */
public class VersionedDataSet implements IDataSet {

	private IDataSet wrappedDataset;
	private Integer version;
	private boolean isActive;

	private static transient Logger logger = Logger.getLogger(VersionedDataSet.class);

	public VersionedDataSet() {
	}

	public VersionedDataSet(IDataSet wrappedDataset, Integer version, boolean isActive) {
		this.wrappedDataset = wrappedDataset;
		this.version = version;
		this.isActive = isActive;
	}

	/**
	 * @return the wrappedDataset
	 */
	public IDataSet getWrappedDataset() {
		return wrappedDataset;
	}

	/**
	 * @param wrappedDataset
	 *            the wrappedDataset to set
	 */
	public void setWrappedDataset(IDataSet wrappedDataset) {
		this.wrappedDataset = wrappedDataset;
	}

	/**
	 * @return the organization
	 */
	@Override
	public String getOrganization() {
		return wrappedDataset.getOrganization();
	}

	/**
	 * @param wrappedDataset
	 *            the organization to set
	 */
	@Override
	public void setOrganization(String organization) {
		this.wrappedDataset.setOrganization(organization);
	}

	/**
	 * @return the version
	 */
	public Integer getVersionNum() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersionNum(Integer version) {
		this.version = version;
	}

	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * @param isActive
	 *            the isActive to set
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public String getDsMetadata() {
		return wrappedDataset.getDsMetadata();
	}

	@Override
	public void setDsMetadata(String dsMetadata) {
		wrappedDataset.setDsMetadata(dsMetadata);
	}

	@Override
	public IMetaData getMetadata() {
		return wrappedDataset.getMetadata();
	}

	@Override
	public void setMetadata(IMetaData metadata) {
		wrappedDataset.setMetadata(metadata);
	}

	// general properties ....
	@Override
	public int getId() {
		return wrappedDataset.getId();
	}

	@Override
	public void setId(int id) {
		wrappedDataset.setId(id);
	}

	@Override
	public String getName() {
		return wrappedDataset.getName();
	}

	@Override
	public void setName(String name) {
		wrappedDataset.setName(name);
	}

	@Override
	public String getDescription() {
		return wrappedDataset.getDescription();
	}

	@Override
	public void setDescription(String description) {
		wrappedDataset.setDescription(description);
	}

	@Override
	public String getLabel() {
		return wrappedDataset.getLabel();
	}

	@Override
	public void setLabel(String label) {
		wrappedDataset.setLabel(label);
	}

	@Override
	public Integer getCategoryId() {
		return wrappedDataset.getCategoryId();
	}

	@Override
	public void setCategoryId(Integer categoryId) {
		wrappedDataset.setCategoryId(categoryId);
	}

	@Override
	public String getCategoryCd() {
		return wrappedDataset.getCategoryCd();
	}

	@Override
	public void setCategoryCd(String categoryCd) {
		wrappedDataset.setCategoryCd(categoryCd);
	}

	@Override
	public String getDsType() {
		return wrappedDataset.getDsType();
	}

	@Override
	public void setDsType(String dsType) {
		wrappedDataset.setDsType(dsType);
	}

	@Override
	public String getConfiguration() {
		return wrappedDataset.getConfiguration();
	}

	@Override
	public void setConfiguration(String configuration) {
		wrappedDataset.setConfiguration(configuration);
	}

	@Override
	public Map getProperties() {
		return wrappedDataset.getProperties();
	}

	@Override
	public void setProperties(Map map) {
		wrappedDataset.setProperties(map);
	}

	// parametrization ....
	// --------------------------------------------------------------------------------------------------
	// INVESTIGATE: why this 2 similar methods ??? FIND OUT & REFACTOR !
	@Override
	public String getParameters() {
		return wrappedDataset.getParameters();
	}

	@Override
	public void setParameters(String parameters) {
		wrappedDataset.setParameters(parameters);
	}

	@Override
	public Map getParamsMap() {
		return wrappedDataset.getParamsMap();
	}

	@Override
	public void setParamsMap(Map params) {
		wrappedDataset.setParamsMap(params);
	}

	// --------------------------------------------------------------------------------------------------

	// profilation ...
	@Override
	public Map getUserProfileAttributes() {
		return wrappedDataset.getUserProfileAttributes();
	}

	@Override
	public void setUserProfileAttributes(Map<String, Object> attributes) {
		wrappedDataset.setUserProfileAttributes(attributes);
	}

	// execution ...
	// --------------------------------------------------------------------------------------------------
	@Override
	public void loadData() {
		wrappedDataset.loadData();
	}

	@Override
	public void loadData(int offset, int fetchSize, int maxResults) {
		wrappedDataset.loadData(offset, fetchSize, maxResults);
	}

	// --------------------------------------------------------------------------------------------------

	@Override
	public IDataStore getDataStore() {
		return wrappedDataset.getDataStore();
	}

	// extension points ...
	@Override
	public boolean hasBehaviour(String behaviourId) {
		return wrappedDataset.hasBehaviour(behaviourId);
	}

	@Override
	public Object getBehaviour(String behaviourId) {
		return wrappedDataset.getBehaviour(behaviourId);
	}

	@Override
	public void addBehaviour(IDataSetBehaviour behaviour) {
		wrappedDataset.addBehaviour(behaviour);
	}

	// =================================================================================================
	// TO BE DEPRECATED ( do not cross this line ;-) )
	// =================================================================================================
	@Override
	public Integer getTransformerId() {
		return wrappedDataset.getTransformerId();
	}

	@Override
	public void setTransformerId(Integer transformerId) {
		wrappedDataset.setTransformerId(transformerId);
	}

	@Override
	public String getTransformerCd() {
		return wrappedDataset.getTransformerCd();
	}

	@Override
	public void setTransformerCd(String transfomerCd) {
		wrappedDataset.setTransformerCd(transfomerCd);
	}

	@Override
	public String getPivotColumnName() {
		return wrappedDataset.getPivotColumnName();
	}

	@Override
	public void setPivotColumnName(String pivotColumnName) {
		wrappedDataset.setPivotColumnName(pivotColumnName);
	}

	@Override
	public String getPivotRowName() {
		return wrappedDataset.getPivotRowName();
	}

	@Override
	public void setPivotRowName(String pivotRowName) {
		wrappedDataset.setPivotRowName(pivotRowName);
	}

	@Override
	public boolean isNumRows() {
		return wrappedDataset.isNumRows();
	}

	@Override
	public void setNumRows(boolean numRows) {
		wrappedDataset.setNumRows(numRows);
	}

	@Override
	public String getPivotColumnValue() {
		return wrappedDataset.getPivotColumnValue();
	}

	@Override
	public void setPivotColumnValue(String pivotColumnValue) {
		wrappedDataset.setPivotColumnValue(pivotColumnValue);
	}

	@Override
	public boolean hasDataStoreTransformer() {
		return wrappedDataset.hasDataStoreTransformer();
	}

	@Override
	public void removeDataStoreTransformer() {
		wrappedDataset.removeDataStoreTransformer();
	}

	@Override
	public void setAbortOnOverflow(boolean abortOnOverflow) {
		wrappedDataset.setAbortOnOverflow(abortOnOverflow);
	}

	@Override
	public void addBinding(String bindingName, Object bindingValue) {
		wrappedDataset.addBinding(bindingName, bindingValue);
	}

	@Override
	public void setDataStoreTransformer(IDataStoreTransformer transformer) {
		wrappedDataset.setDataStoreTransformer(transformer);
	}

	@Override
	public IDataStoreTransformer getDataStoreTransformer() {
		return wrappedDataset.getDataStoreTransformer();
	}

	@Override
	public boolean isPersisted() {
		return wrappedDataset.isPersisted();
	}

	@Override
	public void setPersisted(boolean persisted) {
		wrappedDataset.setPersisted(persisted);
	}

	@Override
	public boolean isScheduled() {
		return wrappedDataset.isScheduled();
	}

	@Override
	public void setScheduled(boolean scheduled) {
		wrappedDataset.setScheduled(scheduled);
	}

	@Override
	public boolean isFlatDataset() {
		return wrappedDataset.isFlatDataset();
	}

	@Override
	public String getFlatTableName() {
		return wrappedDataset.getFlatTableName();
	}

	@Override
	public boolean isPublic() {
		return wrappedDataset.isPublic();
	}

	@Override
	public void setPublic(boolean publicDS) {
		wrappedDataset.setPublic(publicDS);
	}

	@Override
	public String getOwner() {
		return wrappedDataset.getOwner();
	}

	@Override
	public void setOwner(String owner) {
		wrappedDataset.setOwner(owner);
	}

	@Override
	public String getUserIn() {
		return wrappedDataset.getUserIn();
	}

	@Override
	public void setUserIn(String userIn) {
		wrappedDataset.setUserIn(userIn);
	}

	@Override
	public Date getDateIn() {
		return wrappedDataset.getDateIn();
	}

	@Override
	public void setDateIn(Date dateIn) {
		wrappedDataset.setDateIn(dateIn);
	}

	@Override
	public List getNoActiveVersions() {
		return wrappedDataset.getNoActiveVersions();
	}

	@Override
	public void setNoActiveVersions(List noActiveVersions) {
		wrappedDataset.setNoActiveVersions(noActiveVersions);
	}

	// TODO these methods do NOT belong to the dataset interface. remove them and refactor the code.
	// --------------------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------------------
	// TODO these methods must be moved into a proper factory that convert SpagoBI BO into data bean passed
	// to the DAO. For the conversion from data bean and SpagoBI BO such a factory alredy exist
	// NOTE: SpagoBiDataSet: change when possible the name SpagoBiDataSet following a convention common to
	// all data bean
	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		return wrappedDataset.toSpagoBiDataSet();
	}

	// --------------------------------------------------------------------------------------------------

	@Override
	public IDataStore test() {
		return wrappedDataset.test();
	}

	@Override
	public IDataStore test(int offset, int fetchSize, int maxResults) {
		return wrappedDataset.test(offset, fetchSize, maxResults);
	}

	@Override
	public String getSignature() {
		return wrappedDataset.getSignature();
	}

	@Override
	public IDataSetTableDescriptor persist(String tableName, IDataSource dataSource) {
		return wrappedDataset.persist(tableName, dataSource);
	}

	@Override
	public IDataStore getDomainValues(String fieldName, Integer start, Integer limit, IDataStoreFilter filter) {
		return wrappedDataset.getDomainValues(fieldName, start, limit, filter);
	}

	@Override
	public IDataStore decode(IDataStore datastore) {
		return wrappedDataset.decode(datastore);
	}

	@Override
	public boolean isCalculateResultNumberOnLoadEnabled() {
		return wrappedDataset.isCalculateResultNumberOnLoadEnabled();
	}

	@Override
	public void setCalculateResultNumberOnLoad(boolean enabled) {
		wrappedDataset.setCalculateResultNumberOnLoad(enabled);
	}

	@Override
	public void setDataSource(IDataSource dataSource) {
		wrappedDataset.setDataSource(dataSource);
	}

	@Override
	public IDataSource getDataSource() {
		return wrappedDataset.getDataSource();
	}

	@Override
	public String getTableNameForReading() {
		return wrappedDataset.getTableNameForReading();
	}

	@Override
	public IDataSource getDataSourceForReading() {
		return wrappedDataset.getDataSourceForReading();
	}

	@Override
	public void setDataSourceForReading(IDataSource dataSource) {
		wrappedDataset.setDataSourceForReading(dataSource);
	}

	@Override
	public String getPersistTableName() {
		return wrappedDataset.getPersistTableName();
	}

	@Override
	public void setPersistTableName(String persistTableName) {
		wrappedDataset.setPersistTableName(persistTableName);
	}

	@Override
	public IDataSource getDataSourceForWriting() {
		return wrappedDataset.getDataSourceForWriting();
	}

	@Override
	public void setDataSourceForWriting(IDataSource dataSource) {
		wrappedDataset.setDataSourceForWriting(dataSource);
	}

	@Override
	public Integer getScopeId() {
		return wrappedDataset.getScopeId();
	}

	@Override
	public void setScopeId(Integer scopeId) {
		wrappedDataset.setScopeId(scopeId);

	}

	@Override
	public String getScopeCd() {
		return wrappedDataset.getScopeCd();
	}

	@Override
	public void setScopeCd(String scopeCd) {
		wrappedDataset.setScopeCd(scopeCd);

	}

	public void getFe(String scopeCd) {
		wrappedDataset.setScopeCd(scopeCd);

	}

	@Override
	public FederationDefinition getDatasetFederation() {
		return wrappedDataset.getDatasetFederation();
	}

	@Override
	public void setDatasetFederation(FederationDefinition datasetFederation) {
		wrappedDataset.setDatasetFederation(datasetFederation);

	}

	@Override
	public String getStartDateField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStartDateField(String startDateField) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getEndDateField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEndDateField(String endDateField) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSchedulingCronLine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSchedulingCronLine(String schedulingCronLine) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, ?> getDefaultValues() {
		return DataSetUtilities.getParamsDefaultValues(this);
	}

}
