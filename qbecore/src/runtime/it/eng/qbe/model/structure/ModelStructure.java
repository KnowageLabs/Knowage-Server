/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure;

import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class ModelStructure extends AbstractModelObject implements IModelStructure {



	public static class ModelRootEntitiesMap {
		protected Map<String, RootEntitiesGraph> modelRootEntitiesMap;

		public ModelRootEntitiesMap() {
			modelRootEntitiesMap = new HashMap<String, RootEntitiesGraph>();
		}

		public Set<String> getModelNames() {
			return modelRootEntitiesMap.keySet();
		}

		public RootEntitiesGraph getRootEntities(String modelName) {
			return modelRootEntitiesMap.get(modelName);
		}

		public void setRootEntities(String modelName, RootEntitiesGraph modelRootEntities) {
			modelRootEntitiesMap.put(modelName, modelRootEntities);
		}
	}

	protected long nextId;

	protected int maxRecursionLevel;

	protected ModelRootEntitiesMap modelRootEntitiesMap;
	//protected Map<String, Map<String,IModelEntity>> rootEntities;	// modelName->(entityUniqueName->entity)

	protected Map<String, IModelEntity> entities; //entityUniqueName->entity
	protected Map<String, IModelField> fields; // uniqueName -> field
	protected Map<String, List<ModelCalculatedField>> calculatedFields; // entity uniqueName -> fields' list
	protected Map<String, HierarchicalDimensionField> hierarchicalDimensions;

	// =========================================================================
	// COSTRUCTORS
	// =========================================================================

	/**
	 * Instantiate a new empty ModelStructure object
	 */
	public ModelStructure() {
		nextId = 0;
		id = getNextId();
		name = "Generic Model";

		//rootEntities = new HashMap<String, Map<String,IModelEntity>>();
		modelRootEntitiesMap = new ModelRootEntitiesMap();

		entities = new HashMap<String, IModelEntity>();
		fields = new HashMap<String, IModelField>();
		calculatedFields = new  HashMap<String, List<ModelCalculatedField>>();
		initProperties();

	}


	// =========================================================================
	// ACCESORS
	// =========================================================================

	/**
	 * Gets the next id.
	 *
	 * @return the next id
	 */
	public long getNextId() {
		return nextId++;
	}

	public Set<String> getModelNames() {
		return modelRootEntitiesMap.getModelNames();
	}

	public RootEntitiesGraph getRootEntitiesGraph(String modelName, boolean createIfNotExist) {
		RootEntitiesGraph rootEntitiesGraph;

		rootEntitiesGraph = modelRootEntitiesMap.getRootEntities(modelName);
		if (rootEntitiesGraph == null && createIfNotExist == true) {
			rootEntitiesGraph = new RootEntitiesGraph();
			modelRootEntitiesMap.setRootEntities(modelName, rootEntitiesGraph);
		}

		return rootEntitiesGraph;
	}

	// Root Entities -----------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#addRootEntity(String modelName, String name, String path, String role, String type)
	 */
	public IModelEntity addRootEntity(String modelName, String name, String path, String role, String type) {
		IModelEntity entity = new ModelEntity(name, path, role, type, this);
		addRootEntity(modelName, entity);
		return entity;
	}

	public void addRootEntity(String modelName, IModelEntity entity) {
		RootEntitiesGraph rootEntitiesGraph;
		rootEntitiesGraph = getRootEntitiesGraph(modelName, true);
		rootEntitiesGraph.addRootEntity(entity);
		addEntity(entity);
		entity.setModelName(modelName);
	}

	/**
	 * NOTE: At the moment is not possible to have connected entites that belong to different models
	 */
	public boolean areRootEntitiesConnected(Set<IModelEntity> entities) {
		RootEntitiesGraph rootEntitiesGraph;

		// check if all entities belong to the same model
		String modelName = null;
		for(IModelEntity entity : entities) {
			Assert.assertTrue(entity.getParent() == null, "Entity [" + entity.getUniqueName() + "] is not a root entity");
			Assert.assertTrue(entity.getModelName() != null, "Entity [" + entity.getUniqueName() + "] does not belong to any model");
			if(modelName == null) {
				modelName = entity.getModelName();
			} else {
				if(modelName.equals( entity.getModelName() ) == false) return false;
			}
		}

		rootEntitiesGraph = getRootEntitiesGraph(modelName, false);

		return rootEntitiesGraph.areRootEntitiesConnected(entities);
	}

	public Set<Relationship> getRootEntitiesConnections(Set<IModelEntity> entities) {
		RootEntitiesGraph rootEntitiesGraph;
		Iterator<IModelEntity> it = entities.iterator();
		IModelEntity entity = it.next();
		rootEntitiesGraph = getRootEntitiesGraph(entity.getModelName(), true);
		return rootEntitiesGraph.getConnectingRelatiosnhips(entities);
	}

	public Set<Relationship> getRootEntityDirectConnections(IModelEntity entity) {
		RootEntitiesGraph rootEntitiesGraph = getRootEntitiesGraph(entity.getModelName(), true);
		return rootEntitiesGraph.getRootEntityDirectConnections(entity);
	}

	public Set<Relationship> getDirectConnections(IModelEntity source, IModelEntity target ) {
		RootEntitiesGraph rootEntitiesGraph = getRootEntitiesGraph(source.getModelName(), true);
		return rootEntitiesGraph.getDirectConnections(source, target);
	}
	// Root Entities Relationship -------------------------------------------------


	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#addRootEntityRelationship(it.eng.qbe.model.structure.IModelEntity, it.eng.qbe.model.structure.IModelEntity, java.lang.String)
	 */
	public void addRootEntityRelationship(String modelName,
			IModelEntity fromEntity, List<IModelField> fromFields,
			IModelEntity toEntity, List<IModelField> toFields,
			String type, String relationName) {
		RootEntitiesGraph rootEntitiesGraph;

		rootEntitiesGraph = modelRootEntitiesMap.getRootEntities(modelName);
		if (rootEntitiesGraph == null) {
			rootEntitiesGraph = new RootEntitiesGraph();
			modelRootEntitiesMap.setRootEntities(modelName, rootEntitiesGraph);
		}

		rootEntitiesGraph.addRelationship(fromEntity, fromFields, toEntity, toFields, type, relationName);
	}



	// Entities ---------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#getRootEntity(String modelName, String entityName)
	 */
	public IModelEntity getRootEntity(String modelName, String entityName) {
		//Map<String, IModelEntity> modelRootEntities = rootEntities.get(modelName);
		RootEntitiesGraph rootEntitiesGraph = modelRootEntitiesMap.getRootEntities(modelName);
		return rootEntitiesGraph == null ? null : (IModelEntity)rootEntitiesGraph.getRootEntityByName(entityName);
	}

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#getRootEntity(IModelEntity entity)
	 */
	public IModelEntity getRootEntity(IModelEntity entity) {
		if (entity == null) {
			return null;
		}
		IModelEntity toReturn = null;
		Iterator<String> keysIt = getModelNames().iterator();
		while (keysIt.hasNext()) {
			String modelName = keysIt.next();
			IModelEntity rootEntity = getRootEntity(entity, modelName);
			if (rootEntity != null) {
				toReturn = rootEntity;
				break;
			}
		}
		return toReturn;
	}

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#getRootEntity(IModelEntity entity, String modelName)
	 */
	public IModelEntity getRootEntity(IModelEntity entity, String modelName) {
		if (entity == null) {
			return null;
		}
		IModelEntity toReturn = null;
		List<IModelEntity> rootEntities = getRootEntities(modelName);
		Iterator<IModelEntity> rootEntitiesIt = rootEntities.iterator();
		while (rootEntitiesIt.hasNext()) {
			IModelEntity rootEntity = rootEntitiesIt.next();
			if (entity.getType().equals(rootEntity.getType())) {
				toReturn = rootEntity;
				break;
			}
		}
		return toReturn;
	}

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#getRootEntityIterator(String modelName)
	 */
	public Iterator<IModelEntity> getRootEntityIterator(String modelName) {
		return getRootEntities(modelName).iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#getRootEntities(String modelName)
	 */
	public List<IModelEntity> getRootEntities(String modelName) {
		List<IModelEntity> list;
		RootEntitiesGraph rootEntitiesGraph;
		rootEntitiesGraph = modelRootEntitiesMap.getRootEntities(modelName);
		list = rootEntitiesGraph.getAllRootEntities();
		return list;
	}


	// Entities -----------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#ddEntity(IModelEntity entity)
	 */
	public void addEntity(IModelEntity entity) {
		entities.put(entity.getUniqueName(), entity);
	}

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#getEntity(String entityUniqueName)
	 */
	public IModelEntity getEntity(String entityUniqueName) {
		IModelEntity entity = entities.get(entityUniqueName);
		return entity;
	}

	// Fields -----------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#addField(IModelField field)
	 */
	public void addField(IModelField field) {
		fields.put(field.getUniqueName(), field);
	}

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure# getField(String fieldUniqueName)
	 */
	public IModelField getField(String fieldUniqueName) {
		IModelField field = fields.get(fieldUniqueName);
		return field;
	}

	// Calculated Fields ----------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#getCalculatedFields()
	 */
	public Map<String, List<ModelCalculatedField>> getCalculatedFields() {
		return calculatedFields;
	}

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#getCalculatedFieldsByEntity(String entityName)
	 */
	public List<ModelCalculatedField> getCalculatedFieldsByEntity(String entityName) {
		List<ModelCalculatedField> result;

		result = new ArrayList<ModelCalculatedField>();
		if(calculatedFields.containsKey(entityName)) {
			result.addAll( calculatedFields.get(entityName) );
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#setCalculatedFields(Map<String, List<ModelCalculatedField>> calculatedFields)
	 */
	public void setCalculatedFields(Map<String, List<ModelCalculatedField>> calculatedFields) {
		this.calculatedFields = calculatedFields;
	}

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#addCalculatedField(String entityName, ModelCalculatedField calculatedFiled)
	 */
	public void addCalculatedField(String entityName, ModelCalculatedField calculatedFiled) {
		List<ModelCalculatedField> calculatedFiledsOnTargetEntity;
		if(!calculatedFields.containsKey(entityName)) {
			calculatedFields.put(entityName, new ArrayList<ModelCalculatedField>());
		}
		calculatedFiledsOnTargetEntity = calculatedFields.get(entityName);
		List<ModelCalculatedField> toRemove = new ArrayList<ModelCalculatedField>();
		for(int i = 0; i < calculatedFiledsOnTargetEntity.size(); i++) {
			ModelCalculatedField f = calculatedFiledsOnTargetEntity.get(i);
			if(f.getName().equals(calculatedFiled.getName())) {
				toRemove.add(f);
			}
		}
		for(int i = 0; i < toRemove.size(); i++) {
			calculatedFiledsOnTargetEntity.remove(toRemove.get(i));
		}
		calculatedFiledsOnTargetEntity.add(calculatedFiled);
	}

	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelStructure#removeCalculatedField(String entityName, ModelCalculatedField calculatedFiled)
	 */
	public void removeCalculatedField(String entityName, ModelCalculatedField calculatedFiled) {
		List<ModelCalculatedField> calculatedFieldsOnTargetEntity;

		calculatedFieldsOnTargetEntity = calculatedFields.get(entityName);
		if(calculatedFieldsOnTargetEntity != null) {
			calculatedFieldsOnTargetEntity.remove(calculatedFiled);
		}
	}

	public Map<String, HierarchicalDimensionField> getHiearchicalDimensions() {
		return this.hierarchicalDimensions;
	}

	public void setHierarchicalDimension(Map<String, HierarchicalDimensionField> hierarchicalDimensions) {
		this.hierarchicalDimensions = hierarchicalDimensions;
	}

	public void setMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
	}


	public int getMaxRecursionLevel() {
		return this.maxRecursionLevel;
	}

	public List<ModelViewEntity> getViewsEntities(String modelName) {
		List<ModelViewEntity> toReturn = new ArrayList<ModelViewEntity>();
		List<IModelEntity> entities = this.getRootEntities(modelName);
		for (IModelEntity entity : entities) {
			if (entity instanceof ModelViewEntity) {
				toReturn.add((ModelViewEntity) entity);
			}
		}
		return toReturn;
	}





}
