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
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilteredModelStructure extends AbstractModelObject implements IModelStructure {


	private QbeTreeFilter qbeTreeFilter;
	private IDataSource dataSource;
	private IModelStructure wrappedModelStructure;

	private int maxRecursionLevel;

	public FilteredModelStructure(IModelStructure wrappedModelStructure, IDataSource dataSource, QbeTreeFilter qbeTreeFilter){
		this.qbeTreeFilter=qbeTreeFilter;
		this.dataSource=dataSource;
		if(wrappedModelStructure instanceof FilteredModelStructure){
			this.wrappedModelStructure = ((FilteredModelStructure)wrappedModelStructure).getWrappedModelStructure();
		}else{
			this.wrappedModelStructure = wrappedModelStructure;
		}
	}

	public List<IModelEntity> getRootEntities(String modelName){
		List<IModelEntity> iModelEntities = qbeTreeFilter.filterEntities(dataSource,wrappedModelStructure.getRootEntities(modelName));
		List<IModelEntity> filteredModelEntities = new ArrayList<IModelEntity>();
		for(int i=0; i<iModelEntities.size(); i++){
			filteredModelEntities.add(toFilteredModelEntity(iModelEntities.get(i)));
		}
		return filteredModelEntities;
	}

	public QbeTreeFilter getQbeTreeFilter() {
		return qbeTreeFilter;
	}

	public void setQbeTreeFilter(QbeTreeFilter qbeTreeFilter) {
		this.qbeTreeFilter = qbeTreeFilter;
	}

	@Override
	public long getId() {
		return wrappedModelStructure.getId();
	}

	@Override
	public String getName() {
		return wrappedModelStructure.getName();
	}

	@Override
	public void setName(String name) {
		wrappedModelStructure.setName(name);
	}

	@Override
	public Map<String, Object> getProperties() {
		return wrappedModelStructure.getProperties();
	}

	@Override
	public void setProperties(Map<String, Object> properties) {
		wrappedModelStructure.setProperties(properties);

	}

	public long getNextId() {
		return wrappedModelStructure.getNextId();
	}

	public Set<String> getModelNames() {
		return wrappedModelStructure.getModelNames();
	}

	public IModelEntity addRootEntity(String modelName, String name, String path, String role, String type) {
		List<IModelEntity> list = new ArrayList<IModelEntity>();
		List<IModelEntity> filteredList;
		IModelEntity entityn =  wrappedModelStructure.addRootEntity(modelName, name, path, role, type);
		if(entityn==null){
			return null;
		}
		list.add(toFilteredModelEntity(entityn));
		filteredList = qbeTreeFilter.filterEntities(dataSource,list);
		if(filteredList==null || filteredList.size()==0){
			return null;
		}
		return filteredList.get(0);
	}

	public void addRootEntity(String modelName, IModelEntity entity) {
		wrappedModelStructure.addRootEntity(modelName, entity);
	}

	public IModelEntity getRootEntity(String modelName, String entityName) {
		List<IModelEntity> list = new ArrayList<IModelEntity>();
		List<IModelEntity> filteredList;
		IModelEntity entityn = wrappedModelStructure.getRootEntity(modelName, entityName);
		if(entityn==null){
			return null;
		}
		list.add(toFilteredModelEntity(entityn));
		filteredList = qbeTreeFilter.filterEntities(dataSource,list);
		if(filteredList==null || filteredList.size()==0){
			return null;
		}
		return filteredList.get(0);
	}

	public IModelEntity getRootEntity(IModelEntity entity) {
		List<IModelEntity> list = new ArrayList<IModelEntity>();
		List<IModelEntity> filteredList;
		IModelEntity entityn = wrappedModelStructure.getRootEntity(entity);
		if(entity==null){
			return null;
		}
		list.add(toFilteredModelEntity(entityn));
		filteredList = qbeTreeFilter.filterEntities(dataSource,list);
		if(filteredList==null || filteredList.size()==0){
			return null;
		}
		return filteredList.get(0);
	}

	public IModelEntity getRootEntity(IModelEntity entity, String modelName) {
		List<IModelEntity> list = new ArrayList<IModelEntity>();
		List<IModelEntity> filteredList;
		IModelEntity entityn = wrappedModelStructure.getRootEntity(entity, modelName);
		if(entity==null){
			return null;
		}
		list.add(toFilteredModelEntity(entityn));
		filteredList = qbeTreeFilter.filterEntities(dataSource,list);
		if(filteredList==null || filteredList.size()==0){
			return null;
		}
		return filteredList.get(0);
	}

	public Iterator<IModelEntity> getRootEntityIterator(String modelName) {
		return getRootEntities(modelName).iterator();
	}

	public boolean areRootEntitiesConnected(Set<IModelEntity> entities){
		return wrappedModelStructure.areRootEntitiesConnected(entities);
	}


	public void addEntity(IModelEntity entity) {
		wrappedModelStructure.addEntity(entity);

	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#addRootEntityRelationship(java.lang.String, it.eng.qbe.model.structure.IModelEntity, it.eng.qbe.model.structure.IModelEntity, java.lang.String)
	 */
	public void addRootEntityRelationship(String modelName,
			IModelEntity fromEntity, List<IModelField> fromFields,
			IModelEntity toEntity, List<IModelField> toFields,
			String type, String relationName) {
		wrappedModelStructure.addRootEntityRelationship(modelName, fromEntity, fromFields, toEntity, toFields, type, relationName);
	}

//	public Set<Relationship> getRootEntitiesConnections(Set<IModelEntity> entities) {
//		return wrappedModelStructure.getRootEntitiesConnections(entities);
//	}

	public IModelEntity getEntity(String entityUniqueName) {
		List<IModelEntity> list = new ArrayList<IModelEntity>();
		List<IModelEntity> filteredList;
		IModelEntity entity = wrappedModelStructure.getEntity(entityUniqueName);
		if(entity==null){
			return null;
		}
		list.add(toFilteredModelEntity(entity));
		filteredList = qbeTreeFilter.filterEntities(dataSource,list);
		if(filteredList==null || filteredList.size()==0){
			return null;
		}
		return filteredList.get(0);
	}

	public void addField(IModelField field) {
		wrappedModelStructure.addField(field);

	}

	public IModelField getField(String fieldUniqueName) {
		List<IModelField> list = new ArrayList<IModelField>();
		List<IModelField> filteredList;
		IModelField field = wrappedModelStructure.getField(fieldUniqueName);
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

	public Map<String, List<ModelCalculatedField>> getCalculatedFields() {
		Map<String, List<ModelCalculatedField>> calculatedFields =  wrappedModelStructure.getCalculatedFields();
		Map<String, List<ModelCalculatedField>> filteredCalculatedFields =  new HashMap<String, List<ModelCalculatedField>>();
		Iterator<String> iter = calculatedFields.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			filteredCalculatedFields.put(key, qbeTreeFilter.filterFields(dataSource,calculatedFields.get(key)));
		}
		return filteredCalculatedFields;
	}
	public List<ModelCalculatedField> getCalculatedFieldsByEntity(
			String entityName) {
		return qbeTreeFilter.filterFields(dataSource,wrappedModelStructure.getCalculatedFieldsByEntity(entityName));
	}

	public void setCalculatedFields(
			Map<String, List<ModelCalculatedField>> calculatedFields) {
		wrappedModelStructure.setCalculatedFields(calculatedFields);
	}

	public void addCalculatedField(String entityName,
			ModelCalculatedField calculatedFiled) {
		wrappedModelStructure.addCalculatedField(entityName, calculatedFiled);

	}

	public Map<String, HierarchicalDimensionField> getHierarchicalDimensions() {
		return null;
	}

	public void addHierarchicalDimensionField(String entityName,
			HierarchicalDimensionField hierarchicalDimensionFiled) {
		wrappedModelStructure.addHierarchicalDimensionField(entityName, hierarchicalDimensionFiled);

	}

	public void removeCalculatedField(String entityName,
			ModelCalculatedField calculatedFiled) {
		wrappedModelStructure.removeCalculatedField(entityName, calculatedFiled);

	}

	public IDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public IModelStructure getWrappedModelStructure() {
		return wrappedModelStructure;
	}

	public void setWrappedModelStructure(IModelStructure wrappedModelStructure) {
		this.wrappedModelStructure = wrappedModelStructure;
	}

	private FilteredModelEntity toFilteredModelEntity(IModelEntity modelEntity){
		FilteredModelEntity filteredModelEntity;
		if(modelEntity instanceof FilteredModelEntity){
			filteredModelEntity = (FilteredModelEntity)modelEntity;
			filteredModelEntity.setDataSource(dataSource);
			filteredModelEntity.setQbeTreeFilter(qbeTreeFilter);
		} else {
			filteredModelEntity = new FilteredModelEntity(modelEntity, dataSource, qbeTreeFilter);
		}
		return filteredModelEntity;
	}


	public void setMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
	}


	public int getMaxRecursionLevel() {
		return this.maxRecursionLevel;
	}

	public RootEntitiesGraph getRootEntitiesGraph(String modelName,
			boolean createIfNotExist) {
		return wrappedModelStructure.getRootEntitiesGraph(modelName, createIfNotExist);
	}

	public Set<Relationship> getDirectConnections(IModelEntity source,
			IModelEntity target) {
		return wrappedModelStructure.getDirectConnections(source, target);
	}

	public Set<Relationship> getRootEntityDirectConnections(IModelEntity entity) {
		return wrappedModelStructure.getRootEntityDirectConnections(entity);
	}


}
