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

	@Override
	public List<IModelField> getKeyFields() {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getKeyFields());
	}

	@Override
	public List<IModelField> getNormalFields() {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getNormalFields());
	}

	@Override
	public List<ModelCalculatedField>  getCalculatedFields() {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getCalculatedFields());
	}

	@Override
	public List<IModelEntity> getSubEntities() {
		List<IModelEntity> modelEntities = qbeTreeFilter.filterEntities(getDataSource(), wrappedModelEntity.getSubEntities());
		List<IModelEntity> filteredModelEntities = new ArrayList<>();
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

	@Override
	public long getId() {
		return wrappedModelEntity.getId();
	}

	@Override
	public String getName() {
		return wrappedModelEntity.getName();
	}

	@Override
	public void setName(String name) {
		wrappedModelEntity.setName(name);

	}

	@Override
	public Map<String, Object> getProperties() {
		return wrappedModelEntity.getProperties();
	}

	@Override
	public void setProperties(Map<String, Object> properties) {
		wrappedModelEntity.setProperties(properties);
	}

	@Override
	public String getUniqueName() {
		return wrappedModelEntity.getUniqueName();
	}

	@Override
	public String getUniqueType() {
		return wrappedModelEntity.getUniqueType();
	}

	@Override
	public IModelField addNormalField(String fieldName) {
		return wrappedModelEntity.addNormalField(fieldName);
	}

	@Override
	public IModelField addKeyField(String fieldName) {
		return wrappedModelEntity.addKeyField(fieldName);
	}

	@Override
	public IModelField getField(String fieldUniqueName) {
		List<IModelField> list = new ArrayList<>();
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

	@Override
	public IModelField getFieldByName(String fieldName) {
		List<IModelField> list = new ArrayList<>();
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

	@Override
	public void addCalculatedField(ModelCalculatedField calculatedField) {
		wrappedModelEntity.addCalculatedField(calculatedField);
	}

	@Override
	public void deleteCalculatedField(String fieldName) {
		wrappedModelEntity.deleteCalculatedField(fieldName);
	}

	@Override
	public HierarchicalDimensionField getHierarchicalDimensionByEntity(String entity) {
		ArrayList<HierarchicalDimensionField> list = new ArrayList<>();
		HierarchicalDimensionField dimension = wrappedModelEntity.getHierarchicalDimensionByEntity(entity);
		if(dimension != null){
			list.add(dimension);
			return (HierarchicalDimensionField) qbeTreeFilter.filterFields(dataSource, list).get(0);
		}else{
			return null;
		}
	}

	@Override
	public void addHierarchicalDimension(HierarchicalDimensionField hierarchicalDimensionField){
		wrappedModelEntity.addHierarchicalDimension(hierarchicalDimensionField);
	}

	@Override
	public List<IModelField> getAllFields() {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getAllFields());
	}

	@Override
	public Iterator<IModelField> getKeyFieldIterator() {
		return getKeyFields().iterator();
	}

	@Override
	public Iterator<IModelField> getNormalFieldIterator() {
		return getNormalFields().iterator();
	}

	@Override
	public IModelEntity addSubEntity(String subEntityName,
			String subEntityRole, String subEntityType) {
		return wrappedModelEntity.addSubEntity(subEntityName, subEntityRole, subEntityType);
	}

	@Override
	public void addSubEntity(IModelEntity entity) {
		wrappedModelEntity.addSubEntity(entity);

	}

	@Override
	public IModelEntity getSubEntity(String entityUniqueName) {
		List<IModelEntity> list = new ArrayList<>();
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

	@Override
	public List<IModelEntity> getAllSubEntities() {
		return qbeTreeFilter.filterEntities(getDataSource(), wrappedModelEntity.getAllSubEntities());
	}

	@Override
	public List<IModelEntity> getAllSubEntities(String entityName) {
		return qbeTreeFilter.filterEntities(getDataSource(),wrappedModelEntity.getAllSubEntities(entityName));
	}

	@Override
	public List<IModelField> getAllFieldOccurencesOnSubEntity(String entityName,
			String fieldName) {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getAllFieldOccurencesOnSubEntity(entityName, fieldName));
	}

	@Override
	public String getPath() {
		return wrappedModelEntity.getPath();
	}

	@Override
	public void setPath(String path) {
		wrappedModelEntity.setPath(path);

	}

	@Override
	public String getRole() {
		return wrappedModelEntity.getRole();
	}

	@Override
	public void setRole(String role) {
		wrappedModelEntity.setRole(role);

	}

	@Override
	public String getType() {
		return wrappedModelEntity.getType();
	}

	@Override
	public void setType(String type) {
		wrappedModelEntity.setType(type);
	}

	@Override
	public IModelEntity getRoot() {
		List<IModelEntity> list = new ArrayList<>();
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

	@Override
	public IModelStructure getStructure() {
		return wrappedModelEntity.getStructure();
	}

	@Override
	public IModelEntity getParent() {
		List<IModelEntity> list = new ArrayList<>();
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

	@Override
	public void setParent(IModelEntity parent) {
		wrappedModelEntity.setParent(parent);
	}

	public IModelEntity getWrappedModelEntity() {
		return wrappedModelEntity;
	}

	public void setWrappedModelEntity(IModelEntity wrappedModelEntity) {
		this.wrappedModelEntity = wrappedModelEntity;
	}

	@Override
	public Object getProperty(String name) {
		return wrappedModelEntity.getProperty(name);
	}

	@Override
	public String getPropertyAsString(String name) {
		return wrappedModelEntity.getPropertyAsString(name);
	}

	@Override
	public boolean getPropertyAsBoolean(String name) {
		return wrappedModelEntity.getPropertyAsBoolean(name);
	}

	@Override
	public int getPropertyAsInt(String name) {
		return wrappedModelEntity.getPropertyAsInt(name);
	}

	@Override
	public List<IModelField> getFieldsByType(boolean isKey) {
		return qbeTreeFilter.filterFields(dataSource, wrappedModelEntity.getFieldsByType(isKey));
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelNode#getPathParent()
	 */
	@Override
	public IModelEntity getLogicalParent() {
		return wrappedModelEntity.getLogicalParent();
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelEntity#addField(it.eng.qbe.model.structure.IModelField)
	 */
	@Override
	public void addField(IModelField field) {
		wrappedModelEntity.addField(field);

	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelEntity#setRoot(it.eng.qbe.model.structure.IModelEntity)
	 */
	@Override
	public void setRoot(IModelEntity root) {
		wrappedModelEntity.setRoot(root);

	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelEntity#clone(it.eng.qbe.model.structure.IModelEntity)
	 */
	@Override
	public IModelEntity clone(IModelEntity newParent, String parentView) {
		IModelEntity clonedWrapp = wrappedModelEntity.clone(newParent, parentView);
		return new FilteredModelEntity(clonedWrapp, dataSource, qbeTreeFilter);
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelNode#getParentViews()
	 */
	@Override
	public List<ModelViewEntity> getParentViews() {
		return wrappedModelEntity.getParentViews();
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelNode#getModelName()
	 */
	@Override
	public String getModelName() {
		return wrappedModelEntity.getModelName();
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelNode#setModelName(java.lang.String)
	 */
	@Override
	public void setModelName(String modelName) {
		wrappedModelEntity.setModelName(modelName);
	}

	@Override
	public int getDepth() {
		if (this.getParent() != null) {
			return 1 + this.getParent().getDepth();
		} else {
			return 0;
		}
	}

}
