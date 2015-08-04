/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.filter.QbeTreeFilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FilteredModelEntity implements IModelEntity{

	private QbeTreeFilter qbeTreeFilter;
	private IDataSource dataSource;
	private IModelEntity wrappedModelEntity;

	public FilteredModelEntity(IModelEntity wrappedModelEntity, IDataSource dataSource, QbeTreeFilter qbeTreeFilter ) {
		this.qbeTreeFilter=qbeTreeFilter;
		this.dataSource=dataSource;
		if(wrappedModelEntity instanceof FilteredModelEntity){
			this.wrappedModelEntity = ((FilteredModelEntity)wrappedModelEntity).getWrappedModelEntity();
		}else{
			this.wrappedModelEntity = wrappedModelEntity;
		}

	}

	public List<IModelField> getKeyFields() {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getKeyFields());
	}

	public List<IModelField> getNormalFields() {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getNormalFields());
	}

	public List<ModelCalculatedField>  getCalculatedFields() {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getCalculatedFields());
	}

	public List<IModelEntity> getSubEntities() {
		List<IModelEntity> modelEntities = qbeTreeFilter.filterEntities(getDataSource(), wrappedModelEntity.getSubEntities());
		List<IModelEntity> filteredModelEntities = new ArrayList<IModelEntity>();
		for(int i=0; i<modelEntities.size(); i++){
			FilteredModelEntity filteredModelEntity;
			if(modelEntities.get(i) instanceof FilteredModelEntity){
				filteredModelEntity = (FilteredModelEntity)modelEntities.get(i);
				filteredModelEntity.setDataSource(dataSource);
				filteredModelEntity.setQbeTreeFilter(qbeTreeFilter);
			}else{
				filteredModelEntity = new FilteredModelEntity(modelEntities.get(i), dataSource, qbeTreeFilter);
			}
			filteredModelEntities.add(filteredModelEntity);
		}
		return filteredModelEntities;
	}


	public QbeTreeFilter getQbeTreeFilter() {
		return qbeTreeFilter;
	}

	public void setQbeTreeFilter(QbeTreeFilter qbeTreeFilter) {
		this.qbeTreeFilter = qbeTreeFilter;
	}

	public IDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public long getId() {
		return wrappedModelEntity.getId();
	}

	public String getName() {
		return wrappedModelEntity.getName();
	}

	public void setName(String name) {
		wrappedModelEntity.setName(name);

	}

	public Map<String, Object> getProperties() {
		return wrappedModelEntity.getProperties();
	}

	public void setProperties(Map<String, Object> properties) {
		wrappedModelEntity.setProperties(properties);
	}

	public String getUniqueName() {
		return wrappedModelEntity.getUniqueName();
	}

	public String getUniqueType() {
		return wrappedModelEntity.getUniqueType();
	}

	public IModelField addNormalField(String fieldName) {
		return wrappedModelEntity.addNormalField(fieldName);
	}

	public IModelField addKeyField(String fieldName) {
		return wrappedModelEntity.addKeyField(fieldName);
	}

	public IModelField getField(String fieldUniqueName) {
		List<IModelField> list = new ArrayList<IModelField>();
		List<IModelField> filteredList;
		IModelField field = wrappedModelEntity.getField(fieldUniqueName);
		if(field==null){
			return null;
		}
		list.add(field);
		filteredList = qbeTreeFilter.filterFields(dataSource,list);
		if(filteredList==null || filteredList.size()==0){
			return null;
		}
		return filteredList.get(0);
	}

	public IModelField getFieldByName(String fieldName) {
		List<IModelField> list = new ArrayList<IModelField>();
		List<IModelField> filteredList;
		IModelField field = wrappedModelEntity.getFieldByName(fieldName);
		if(field==null){
			return null;
		}
		list.add(field);
		filteredList = qbeTreeFilter.filterFields(dataSource,list);
		if(filteredList==null || filteredList.size()==0){
			return null;
		}
		return filteredList.get(0);
	}

	public void addCalculatedField(ModelCalculatedField calculatedField) {
		wrappedModelEntity.addCalculatedField(calculatedField);
	}

	public void deleteCalculatedField(String fieldName) {
		wrappedModelEntity.deleteCalculatedField(fieldName);
	}

	public List<HierarchicalDimensionField> getHierarchicalDimensionFields() {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getHierarchicalDimensionFields());
	}

	public void addHierarchicalDimension(HierarchicalDimensionField hierarchicalDimensionField){
		wrappedModelEntity.addHierarchicalDimension(hierarchicalDimensionField);
	}

	public List<IModelField> getAllFields() {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getAllFields());
	}

	public Iterator<IModelField> getKeyFieldIterator() {
		return getKeyFields().iterator();
	}

	public Iterator<IModelField> getNormalFieldIterator() {
		return getNormalFields().iterator();
	}

	public IModelEntity addSubEntity(String subEntityName,
			String subEntityRole, String subEntityType) {
		return wrappedModelEntity.addSubEntity(subEntityName, subEntityRole, subEntityType);
	}

	public void addSubEntity(IModelEntity entity) {
		wrappedModelEntity.addSubEntity(entity);

	}

	public IModelEntity getSubEntity(String entityUniqueName) {
		List<IModelEntity> list = new ArrayList<IModelEntity>();
		List<IModelEntity> filteredList;
		IModelEntity entityn =  wrappedModelEntity.getSubEntity(entityUniqueName);
		if(entityn==null){
			return null;
		}
		list.add(entityn);
		filteredList = qbeTreeFilter.filterEntities(dataSource,list);
		if(filteredList==null || filteredList.size()==0){
			return null;
		}
		return filteredList.get(0);
	}

	public List<IModelEntity> getAllSubEntities() {
		return qbeTreeFilter.filterEntities(getDataSource(), wrappedModelEntity.getAllSubEntities());
	}

	public List<IModelEntity> getAllSubEntities(String entityName) {
		return qbeTreeFilter.filterEntities(getDataSource(),wrappedModelEntity.getAllSubEntities(entityName));
	}

	public List<IModelField> getAllFieldOccurencesOnSubEntity(String entityName,
			String fieldName) {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getAllFieldOccurencesOnSubEntity(entityName, fieldName));
	}

	public String getPath() {
		return wrappedModelEntity.getPath();
	}

	public void setPath(String path) {
		wrappedModelEntity.setPath(path);

	}

	public String getRole() {
		return wrappedModelEntity.getRole();
	}

	public void setRole(String role) {
		wrappedModelEntity.setRole(role);

	}

	public String getType() {
		return wrappedModelEntity.getType();
	}

	public void setType(String type) {
		wrappedModelEntity.setType(type);
	}

	public IModelEntity getRoot() {
		List<IModelEntity> list = new ArrayList<IModelEntity>();
		List<IModelEntity> filteredList;
		IModelEntity entityn =  wrappedModelEntity.getRoot();
		if(entityn==null){
			return null;
		}
		list.add(entityn);
		filteredList = qbeTreeFilter.filterEntities(dataSource,list);
		if(filteredList==null || filteredList.size()==0){
			return null;
		}
		return filteredList.get(0);
	}

	public IModelStructure getStructure() {
		return wrappedModelEntity.getStructure();
	}

	public IModelEntity getParent() {
		List<IModelEntity> list = new ArrayList<IModelEntity>();
		List<IModelEntity> filteredList;
		IModelEntity entityn =  wrappedModelEntity.getParent();
		if(entityn==null){
			return null;
		}
		list.add(entityn);
		filteredList = qbeTreeFilter.filterEntities(dataSource,list);
		if(filteredList==null || filteredList.size()==0){
			return null;
		}
		return filteredList.get(0);
	}

	public void setParent(IModelEntity parent) {
		wrappedModelEntity.setParent(parent);
	}

	public IModelEntity getWrappedModelEntity() {
		return wrappedModelEntity;
	}

	public void setWrappedModelEntity(IModelEntity wrappedModelEntity) {
		this.wrappedModelEntity = wrappedModelEntity;
	}

	public Object getProperty(String name) {
		return wrappedModelEntity.getProperty(name);
	}

	public String getPropertyAsString(String name) {
		return wrappedModelEntity.getPropertyAsString(name);
	}

	public boolean getPropertyAsBoolean(String name) {
		return wrappedModelEntity.getPropertyAsBoolean(name);
	}

	public int getPropertyAsInt(String name) {
		return wrappedModelEntity.getPropertyAsInt(name);
	}

	public List<IModelField> getFieldsByType(boolean isKey) {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getFieldsByType(isKey));
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelNode#getPathParent()
	 */
	public IModelEntity getLogicalParent() {
		return wrappedModelEntity.getLogicalParent();
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelEntity#addField(it.eng.qbe.model.structure.IModelField)
	 */
	public void addField(IModelField field) {
		wrappedModelEntity.addField(field);

	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelEntity#setRoot(it.eng.qbe.model.structure.IModelEntity)
	 */
	public void setRoot(IModelEntity root) {
		wrappedModelEntity.setRoot(root);

	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelEntity#clone(it.eng.qbe.model.structure.IModelEntity)
	 */
	public IModelEntity clone(IModelEntity newParent, String parentView) {
		IModelEntity clonedWrapp = wrappedModelEntity.clone(newParent, parentView);
		return new FilteredModelEntity(clonedWrapp, dataSource, qbeTreeFilter);
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelNode#getParentViews()
	 */
	public List<ModelViewEntity> getParentViews() {
		return wrappedModelEntity.getParentViews();
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelNode#getModelName()
	 */
	public String getModelName() {
		return wrappedModelEntity.getModelName();
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelNode#setModelName(java.lang.String)
	 */
	public void setModelName(String modelName) {
		wrappedModelEntity.setModelName(modelName);
	}

	public int getDepth() {
		if (this.getParent() != null) {
			return 1 + this.getParent().getDepth();
		} else {
			return 0;
		}
	}

}
