/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure;


import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * represent the structure of the data exposed by a <code>IDataSource</code>. Because a data source can be
 * built also upon different data models (i.e. CompositeQbe) an <code>IModelStructure</code> can contains
 * multiple structure, one for each model contained in the composite data source. Each structure is
 * represented trough a collection of tree hierarchies identified by the name of the model it maps.
 *
 *
 * @authors
 *  Andrea Gioia (andrea.gioia@eng.it)
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public interface IModelStructure extends IModelObject {

		public long getNextId() ;

		/**
		 *
		 * @return The names of all the models managed by this structure. To each model is associated
		 * an independent collection of tree hierarchies of <code>IModelObjects</code> identified by
		 * the name of the model it maps.
		 */
		public Set<String> getModelNames() ;


		// ----------------------------------
		// Root entities
		// ----------------------------------

		/**
		 * Add a new root entity to the collection of tree hierarchies associated to the specified model name
		 *
		 * @param modelName the name of the model to which the new entity will be added
		 * @param name the name of the  new entity
		 * @param path the path of the new entity
		 * @param role the role of the new entity
		 * @param type the type of the new entity
		 *
		 * @return the new root entity added to the collection of tree hierarchies associated to the specified model name
		 */
		public IModelEntity addRootEntity(String modelName, String name, String path, String role, String type);
		public void addRootEntity(String modelName, IModelEntity entity) ;

		/**
		 * @param modelName the target model's name
		 * @param entityName the target entity's name
		 *
		 * @return The root entity named entityName contained in the collection of tree associated to
		 * the model named modelName
		 */
		public IModelEntity getRootEntity(String modelName, String entityName) ;

		/**
		 * Gets the root entity relevant to the input entity
		 *
		 * @param entity
		 *
		 * @return the root entity relevant to the input entity
		 */
		public IModelEntity getRootEntity(IModelEntity entity) ;

		/**
		 * Gets the root entity relevant to the input entity in the specified in input
		 *
		 * @param the target entity
		 * @param the name of the target model
		 *
		 * @return the root entity relevant to the input entity in the model
		 * specified in input
		 */
		public IModelEntity getRootEntity(IModelEntity entity, String modelName) ;

		/**
		 * Gets the root entity iterator for the target model.
		 *
		 * @param modelName the name of the target model
		 *
		 * @return the root entities iterator
		 */
		public Iterator<IModelEntity> getRootEntityIterator(String modelName) ;

		/**
		 * Gets the root entities.
		 *
		 * @param modelName the name of the target model
		 *
		 * @return the root entities
		 */
		public List<IModelEntity> getRootEntities(String modelName) ;

		public boolean areRootEntitiesConnected(Set<IModelEntity> entities);

		public RootEntitiesGraph getRootEntitiesGraph(String modelName, boolean createIfNotExist);

	//	public Set<Relationship> getRootEntitiesConnections(Set<IModelEntity> entities);

		public Set<Relationship> getDirectConnections(IModelEntity source, IModelEntity target);

		public  Set<Relationship> getRootEntityDirectConnections(IModelEntity entity);

		// ----------------------------------
		// RootEntityRelationship
		// ----------------------------------
		/**
		 * Add a relationship between the two input entities
		 *
		 * @param modelName the name of the target model
		 * @param fromEntity the relationship source entity
		 * @param fromFields the relationship source fields
		 * @param toEntity the relationship destination entity
		 * @param fromFields the relationship destination fields
		 * @param type relationship type
		 */
		void addRootEntityRelationship(String modelName,
				IModelEntity fromEntity, List<IModelField> fromFields,
				IModelEntity toEntity, List<IModelField> toFields,
				String type, String relationName);


		// ----------------------------------
		// Entities
		// ----------------------------------

		/**
		 * Adds the entity.
		 *
		 * @param entity the entity
		 */
		public void addEntity(IModelEntity entity) ;

		/**
		 * Gets the entity.
		 *
		 * @param entityUniqueName the entity unique name
		 *
		 * @return the entity
		 */
		public IModelEntity getEntity(String entityUniqueName) ;


		// ----------------------------------
		// Fields
		// ----------------------------------
		/**
		 * Adds the field.
		 *
		 * @param field the field
		 */
		public void addField(IModelField field) ;


		/**
		 * Gets the field.
		 *
		 * @param fieldUniqueName the field unique name
		 *
		 * @return the field
		 */
		public IModelField getField(String fieldUniqueName) ;

		public Map<String, List<ModelCalculatedField>> getCalculatedFields() ;

		public List<ModelCalculatedField> getCalculatedFieldsByEntity(String entityName) ;

		public void setCalculatedFields(Map<String, List<ModelCalculatedField>> calculatedFields) ;

		public void addCalculatedField(String entityName, ModelCalculatedField calculatedFiled) ;

		public void removeCalculatedField(String entityName, ModelCalculatedField calculatedFiled) ;

		public Map<String, HierarchicalDimensionField> getHiearchicalDimensions() ;

		public void addHierarchicalDimensionField(String entityName, HierarchicalDimensionField hierarchicalDimensionField) ;

		public void setMaxRecursionLevel(int maxRecursionLevel);

		public int getMaxRecursionLevel();
}
