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
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
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

	public VersionedDataSet(){		
	}
	
	public VersionedDataSet(IDataSet wrappedDataset, Integer version, boolean isActive){
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
	 * @param wrappedDataset the wrappedDataset to set
	 */
	public void setWrappedDataset(IDataSet wrappedDataset) {
		this.wrappedDataset = wrappedDataset;
	}
	
	
	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return wrappedDataset.getOrganization();
	}

	/**
	 * @param wrappedDataset the organization to set
	 */
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
	 * @param version the version to set
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
	 * @param isActive the isActive to set
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getDsMetadata(){
		return wrappedDataset.getDsMetadata();
	}
	
	public void setDsMetadata(String dsMetadata){
		wrappedDataset.setDsMetadata(dsMetadata);
	}
	
	public IMetaData getMetadata(){
		return wrappedDataset.getMetadata();
	}
	
	public void setMetadata(IMetaData metadata){
		wrappedDataset.setMetadata(metadata);
	}
	
	// general properties ....
	public int getId(){
		return wrappedDataset.getId();
	}
	
	public void setId(int id){
		wrappedDataset.setId(id);
	}
	
	public String getName(){
		return wrappedDataset.getName();
	}
	
	public void setName(String name){
		wrappedDataset.setName(name);
	}
	
	public String getDescription(){
		return wrappedDataset.getDescription();
	}
	
	public void setDescription(String description){
		wrappedDataset.setDescription(description);
	}
	
	public String getLabel(){
		return wrappedDataset.getLabel();
	}
	
	public void setLabel(String label){
		wrappedDataset.setLabel(label);
	}
	
	public Integer getCategoryId(){
		return wrappedDataset.getCategoryId();
	}
	public void setCategoryId(Integer categoryId){
		wrappedDataset.setCategoryId(categoryId);
	}
	
	public String getCategoryCd(){
		return wrappedDataset.getCategoryCd();
	}
	
	public void setCategoryCd(String categoryCd){
		wrappedDataset.setCategoryCd(categoryCd);
	}
	
	public String getDsType(){
		return wrappedDataset.getDsType();
	}
	
	public void setDsType(String dsType){
		wrappedDataset.setDsType(dsType);
	}


	public String getConfiguration(){
		return wrappedDataset.getConfiguration();
	}
	public void setConfiguration(String configuration){
		wrappedDataset.setConfiguration(configuration);
	}

	public Map getProperties(){
		return wrappedDataset.getProperties();
	}
	
	public void setProperties(Map map){
		wrappedDataset.setProperties(map);
	}
	
	
	// parametrization ....
	// --------------------------------------------------------------------------------------------------
	// INVESTIGATE: why this 2 similar methods ??? FIND OUT & REFACTOR !
	public String getParameters(){
		return wrappedDataset.getParameters();
	}
	public void setParameters(String parameters){
		wrappedDataset.setParameters(parameters);
	}

	public Map getParamsMap(){
		return wrappedDataset.getParamsMap();
	}
	public void setParamsMap(Map params){
		wrappedDataset.setParamsMap(params);
	}
	// --------------------------------------------------------------------------------------------------
	
	// profilation ...
	public Map getUserProfileAttributes(){
		return wrappedDataset.getUserProfileAttributes();
	}
	public void setUserProfileAttributes(Map<String, Object> attributes){
		wrappedDataset.setUserProfileAttributes(attributes);
	}
	
	// execution ...
	// --------------------------------------------------------------------------------------------------
	public void loadData(){
		wrappedDataset.loadData();
	}
	public void loadData(int offset, int fetchSize, int maxResults){
		wrappedDataset.loadData(offset, fetchSize, maxResults);
	}
	// --------------------------------------------------------------------------------------------------
 	
	public IDataStore getDataStore(){
		return wrappedDataset.getDataStore();
	}
	
	
	// extension points ...
	public boolean hasBehaviour(String behaviourId){
		return wrappedDataset.hasBehaviour(behaviourId);
	}
	public Object getBehaviour(String behaviourId){
		return wrappedDataset.getBehaviour(behaviourId);
	}
	public void addBehaviour(IDataSetBehaviour behaviour){
		wrappedDataset.addBehaviour(behaviour);
	}
	
	// =================================================================================================
	// TO BE DEPRECATED ( do not cross this line ;-) )
	// =================================================================================================
	public Integer getTransformerId(){
		return wrappedDataset.getTransformerId();
	}
	public void setTransformerId(Integer transformerId){
		wrappedDataset.setTransformerId(transformerId);
	}
	
	public String getTransformerCd(){
		return wrappedDataset.getTransformerCd();
	}
	public void setTransformerCd(String transfomerCd){
		wrappedDataset.setTransformerCd(transfomerCd);
	}

	public String getPivotColumnName(){
		return wrappedDataset.getPivotColumnName();
	}
	public void setPivotColumnName(String pivotColumnName){
		wrappedDataset.setPivotColumnName(pivotColumnName);
	}

	public String getPivotRowName(){
		return wrappedDataset.getPivotRowName();
	}
	public void setPivotRowName(String pivotRowName){
		wrappedDataset.setPivotRowName(pivotRowName);
	}
	
	public boolean isNumRows(){
		return wrappedDataset.isNumRows();
	}
	public void setNumRows(boolean numRows){
		wrappedDataset.setNumRows(numRows);
	}

	public String getPivotColumnValue(){
		return wrappedDataset.getPivotColumnValue();
	}
	public void setPivotColumnValue(String pivotColumnValue){
		wrappedDataset.setPivotColumnValue(pivotColumnValue);
	}
	
	public boolean hasDataStoreTransformer() {
		return wrappedDataset.hasDataStoreTransformer();
	}
	public void removeDataStoreTransformer() {
		wrappedDataset.removeDataStoreTransformer();
	}
	
	public void setAbortOnOverflow(boolean abortOnOverflow){
		wrappedDataset.setAbortOnOverflow(abortOnOverflow);
	}
	public void addBinding(String bindingName, Object bindingValue){
		wrappedDataset.addBinding(bindingName, bindingValue);
	}
	
	public void setDataStoreTransformer(IDataStoreTransformer transformer){
		wrappedDataset.setDataStoreTransformer(transformer);
	}
	public IDataStoreTransformer getDataStoreTransformer(){
		return wrappedDataset.getDataStoreTransformer();
	}
	
	public boolean isPersisted(){
		return wrappedDataset.isPersisted();
	}
	public void setPersisted(boolean persisted){
		wrappedDataset.setPersisted(persisted);
	}

	public boolean isScheduled() {
		return wrappedDataset.isScheduled();
	}

	public void setScheduled(boolean scheduled) {
		wrappedDataset.setScheduled(scheduled);
	}

	public boolean isFlatDataset() {
		return wrappedDataset.isFlatDataset();
	}
	
	public String getFlatTableName(){
		return wrappedDataset.getFlatTableName();
	}
	
	public boolean isPublic(){
		return wrappedDataset.isPublic();
	}
	public void setPublic(boolean publicDS){
		wrappedDataset.setPublic(publicDS);
	}
	
	public String getOwner(){
		return wrappedDataset.getOwner();
	}
	public void setOwner(String owner){
		wrappedDataset.setOwner(owner);
	}

	public String getUserIn() {
		return wrappedDataset.getUserIn();
	}

	public void setUserIn(String userIn) {
		wrappedDataset.setUserIn(userIn);
	}
	
	public Date getDateIn() {
		return wrappedDataset.getDateIn();
	}

	public void setDateIn(Date dateIn) {
		wrappedDataset.setDateIn(dateIn);
	}
	
	public List getNoActiveVersions(){
		return wrappedDataset.getNoActiveVersions();
	}
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
	public SpagoBiDataSet toSpagoBiDataSet(){
		return wrappedDataset.toSpagoBiDataSet();
	}
	
	// --------------------------------------------------------------------------------------------------

	public IDataStore test(){
		return wrappedDataset.test();
	}
	public IDataStore test(int offset, int fetchSize, int maxResults){
		return wrappedDataset.test(offset, fetchSize, maxResults);
	}
	
	public String getSignature(){
		return wrappedDataset.getSignature();
	}
	
	public IDataSetTableDescriptor persist(String tableName, IDataSource dataSource){
		return wrappedDataset.persist(tableName, dataSource);
	}
	
	public IDataStore getDomainValues(String fieldName, 
            Integer start, Integer limit, IDataStoreFilter filter){
		return wrappedDataset.getDomainValues(fieldName, start, limit, filter);
	}
	
	public IDataStore decode(IDataStore datastore){
		return wrappedDataset.decode(datastore);
	}
	
	public boolean isCalculateResultNumberOnLoadEnabled(){
		return wrappedDataset.isCalculateResultNumberOnLoadEnabled();
	}
	
	public void setCalculateResultNumberOnLoad(boolean enabled){
		wrappedDataset.setCalculateResultNumberOnLoad(enabled);
	}

	public void setDataSource(IDataSource dataSource) {
		wrappedDataset.setDataSource(dataSource);
	}

	public IDataSource getDataSource() {
		return wrappedDataset.getDataSource();
	}

	public String getTableNameForReading() {
		return wrappedDataset.getTableNameForReading();
	}

	public IDataSource getDataSourceForReading() {
		return wrappedDataset.getDataSourceForReading();
	}
	
	public void setDataSourceForReading(IDataSource dataSource) {
		wrappedDataset.setDataSourceForReading(dataSource);
	}

	public String getPersistTableName() {
		return wrappedDataset.getPersistTableName();
	}

	public void setPersistTableName(String persistTableName) {
		wrappedDataset.setPersistTableName(persistTableName);
	}

	public IDataSource getDataSourceForWriting() {
		return wrappedDataset.getDataSourceForWriting();
	}

	public void setDataSourceForWriting(IDataSource dataSource) {
		wrappedDataset.setDataSourceForWriting(dataSource);
	}

	public Integer getScopeId() {
		return wrappedDataset.getScopeId();
	}

	public void setScopeId(Integer scopeId) {
		wrappedDataset.setScopeId(scopeId);
		
	}

	public String getScopeCd() {
		return wrappedDataset.getScopeCd();
	}

	public void setScopeCd(String scopeCd) {
		wrappedDataset.setScopeCd(scopeCd);
		
	}


	
	
}
