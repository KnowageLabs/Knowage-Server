/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure.builder.jpa;

import it.eng.qbe.datasource.jpa.JPADataSource;
import it.eng.qbe.model.properties.initializer.IModelStructurePropertiesInitializer;
import it.eng.qbe.model.properties.initializer.ModelStructurePropertiesInitializerFactory;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelRelationshipDescriptor;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor.IModelViewRelationshipDescriptor;
import it.eng.qbe.model.structure.ModelCalculatedField;
import it.eng.qbe.model.structure.ModelEntity;
import it.eng.qbe.model.structure.ModelStructure;
import it.eng.qbe.model.structure.ModelViewEntity;
import it.eng.qbe.model.structure.ModelViewEntity.Join;
import it.eng.qbe.model.structure.ModelViewEntity.ViewRelationship;
import it.eng.qbe.model.structure.builder.IModelStructureBuilder;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;
import it.eng.spagobi.utilities.assertion.Assert;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.log4j.Logger;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JPAModelStructureBuilder implements IModelStructureBuilder {
	
	private JPADataSource dataSource;	
	private EntityManager entityManager;
	private int maxRecursionLevel;
	IModelStructurePropertiesInitializer propertiesInitializer;
	public static final String VIEWS_INNER_JOINS_RELATION_NAME = "VIEWS_INNER_JOINS";

	private static int DEFAULT_MAX_RECURSION_LEVEL = 0;
	
	private static transient Logger logger = Logger.getLogger(JPAModelStructureBuilder.class);
	
	
	/**
	 * Constructor
	 * @param dataSource the JPA DataSource
	 */
	public JPAModelStructureBuilder(JPADataSource dataSource) {
		
		String maxRecursionLevelProperty = (String)dataSource.getConfiguration().loadDataSourceProperties().get("maxRecursionLevel");
		if(maxRecursionLevelProperty == null) {
			this.maxRecursionLevel = DEFAULT_MAX_RECURSION_LEVEL;
		} else {
			this.maxRecursionLevel = Integer.parseInt(maxRecursionLevelProperty);
		}
		
		
		if(dataSource == null) {
			throw new IllegalArgumentException("DataSource parameter cannot be null");
		}
		setDataSource( dataSource );
		propertiesInitializer = ModelStructurePropertiesInitializerFactory.getDataMartStructurePropertiesInitializer(dataSource);
	}
	
	/**
	 * This method builds a JPA model structure.
	 * @return DataMartModelStructure
	 */
	public IModelStructure build() {
		ModelStructure modelStructure;
		String modelName;
		
		logger.debug("IN");
		
		try {
			modelStructure = new ModelStructure();
			modelStructure.setMaxRecursionLevel(this.maxRecursionLevel);
	
			modelName = getDataSource().getConfiguration().getModelName();
			Assert.assertNotNull(getDataSource(), "datasource cannot be null");	
			setEntityManager(getDataSource().getEntityManager());
			Assert.assertNotNull(getEntityManager(), "Impossible to find the jar file associated to datamart named: [" + modelName + "]");
			
			propertiesInitializer.addProperties(modelStructure);	
			Map calculatedFields = getDataSource().getConfiguration().loadCalculatedFields();
			modelStructure.setCalculatedFields(calculatedFields);
				
			addRootEntities(modelStructure);
			addViews(modelStructure);
			addRelationshipsBetweenRootEntities(modelStructure);
			addRelationshipsBetweenViews(modelStructure);

			logger.info("Model structure for model [" + modelName + "] succesfully built");
			
			return modelStructure;
			
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to build model structure", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	
	private void addRelationshipsBetweenViews(ModelStructure modelStructure) {
		
		String modelName = getDataSource().getConfiguration().getModelName();
		List<ModelViewEntity> entities = modelStructure.getViewsEntities(modelName);
		
		for (ModelViewEntity view : entities) {
			List<ViewRelationship> viewRelationships = view.getRelationships();
			
			List<Join> joins = view.getJoins();
			
			for (Join join : joins) {
				
				IModelEntity sourceEntity = join.getSourceEntity();
				List<IModelField> sourceFields = join.getSourceFileds();
				
				IModelEntity destinationEntity = join.getDestinationEntity();
				List<IModelField> destinationFields = join.getDestinationFileds();

				try {
					modelStructure.addRootEntityRelationship(modelName, sourceEntity, sourceFields, destinationEntity, destinationFields, "many-to-one", VIEWS_INNER_JOINS_RELATION_NAME);
					logger.debug("Succesfully added relationship between [" + sourceEntity.getName() + "] and [" + destinationEntity.getName() + "]");
				} catch (Throwable t) {
					logger.error("Impossible to add relationship between [" + sourceEntity.getName() + "] and [" + destinationEntity.getName() + "]", t);
				}
				
			}
			
			
			for (ViewRelationship relationship : viewRelationships) {
				
				IModelEntity sourceEntity = relationship.getSourceEntity();
				List<IModelField> sourceFields = relationship.getSourceFileds();
				
				IModelEntity destinationEntity = relationship.getDestinationEntity();
				List<IModelField> destinationFields = relationship.getDestinationFileds();

				try {
					modelStructure.addRootEntityRelationship(modelName, sourceEntity, sourceFields, destinationEntity, destinationFields, "many-to-one", VIEWS_INNER_JOINS_RELATION_NAME);
					logger.debug("Succesfully added relationship between [" + sourceEntity.getName() + "] and [" + destinationEntity.getName() + "]");
				} catch (Throwable t) {
					logger.error("Impossible to add relationship between [" + sourceEntity.getName() + "] and [" + destinationEntity.getName() + "]", t);
				}
				
			}
			
		}
		
	}

	private void addRootEntities(ModelStructure modelStructure) {
		Metamodel jpaMetamodel;
		Set<EntityType<?>> jpaEntities;
		
		String modelName = getDataSource().getConfiguration().getModelName();
		
		jpaMetamodel = getEntityManager().getMetamodel();		
		jpaEntities = jpaMetamodel.getEntities();
		logger.debug("Jpa metamodel contains ["+ jpaEntities.size() + "] entity types");
		
		for(EntityType<?> entityType: jpaEntities) {
			logger.debug("Adding entity type [" + entityType + "] to model structure");
			String entityTypeName =  entityType.getJavaType().getName();
			addEntity(modelStructure, modelName, entityTypeName);	
			logger.info("Entity type [" + entityType + "] succesfully added to model structure");
		}
	}
	
	private void addRelationshipsBetweenRootEntities(ModelStructure modelStructure) {
		
		String modelName = getDataSource().getConfiguration().getModelName();
		
		// add relationship between rootEntities
		RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(modelName, false);
		List<IModelRelationshipDescriptor> relationships = getDataSource().getConfiguration().loadRelationships();
		
		for(IModelRelationshipDescriptor relationship: relationships) {
			if(relationship.getType().equals("many-to-one") == false 
					&& relationship.getType().equals("optional-many-to-one") == false) continue;
			
			IModelEntity sourceEntity = rootEntitiesGraph.getRootEntityByName(relationship.getSourceEntityUniqueName());
			if(sourceEntity == null) throw new RuntimeException("Impossibe to find source entity whose name is equal to [" + relationship.getSourceEntityUniqueName() + "]");
			List<IModelField> sourceFields = new ArrayList<IModelField>();
			List<String> sourceFieldNames = relationship.getSourceFieldUniqueNames();
			
			IModelEntity destinationEntity = rootEntitiesGraph.getRootEntityByName(relationship.getDestinationEntityUniqueName());
			if(destinationEntity == null) throw new RuntimeException("Impossibe to find destination entity whose name is equal to [" + relationship.getDestinationEntityUniqueName() + "]");
			List<IModelField> destinationFields = new ArrayList<IModelField>();
			List<String> destinationFieldNames = relationship.getDestinationFieldUniqueNames();
			
			for(int i =0; i<sourceFieldNames.size(); i++) {
				String sourceFieldName =  sourceFieldNames.get(i);
				IModelField field = sourceEntity.getField(sourceFieldName);
				if(field == null) {	// if the field is not part of the key it is not yet added to the entity. we have to create it now.				
					String generatedFieldName = relationship.getName() + "."  + destinationFieldNames.get(i).split(":")[1];
					field = sourceEntity.addNormalField(generatedFieldName);
					field.getProperties().put("visible", "true");
					field.getProperties().put("position", "" + Integer.MAX_VALUE);
					field.getProperties().put("type", "attribute");
					String columnName = sourceEntity.getPropertyAsString(relationship.getName());
					if(columnName==null){
						columnName = generatedFieldName;
					}
					field.getProperties().put("joinColumnName", columnName);	
				}
				sourceFields.add(field);
			}
			

			for(int i =0; i<destinationFieldNames.size(); i++) {
				String destinationFieldName =  destinationFieldNames.get(i);
				IModelField field = destinationEntity.getField(destinationFieldName);
				if(field == null) {
					String generatedFieldName = relationship.getName() + "."  + sourceFieldNames.get(i).split(":")[1];
					field = destinationEntity.addNormalField(generatedFieldName);
					field.getProperties().put("visible", "true");
					field.getProperties().put("position", "" + Integer.MAX_VALUE);
					field.getProperties().put("type", "attribute");
					String columnName = sourceEntity.getPropertyAsString(relationship.getName());
					if(columnName==null){
						columnName = generatedFieldName;
					}
					field.getProperties().put("joinColumnName", columnName);	
					//throw new RuntimeException("Impossibe to find in destination entity [" + destinationEntity.getName() + "] a field whose name is equal to [" + destinationFieldName + "]");
				}
				destinationFields.add(field);
			}
			try {
				modelStructure.addRootEntityRelationship(modelName, sourceEntity, sourceFields, destinationEntity, destinationFields, relationship.getType(), relationship.getLabel());
				logger.debug("Succesfully added relationship between [" + sourceEntity.getName() + "] and [" + destinationEntity.getName() + "]");
			} catch (Throwable t) {
				logger.error("Impossible to add relationship between [" + sourceEntity.getName() + "] and [" + destinationEntity.getName() + "]", t);
			}
		}
	}

	private void addViews(ModelStructure modelStructure) {
		
		try {
			String modelName = getDataSource().getConfiguration().getModelName();
			
			/*
			 * 1) Load Views definitions and adds to the model structure (with outbound relations from Business Views)
			 */
			List<ModelViewEntity> addedViewsEntities = new ArrayList<ModelViewEntity>();
			List<IModelViewEntityDescriptor> list = getDataSource().getConfiguration().loadViews();
			if(list.size() > 0) {
				for (int i=0; i<list.size(); i++){
					IModelViewEntityDescriptor viewDescriptor = list.get(i);
	
					ModelViewEntity viewEntity = new ModelViewEntity(viewDescriptor, modelName, modelStructure, null);
					addedViewsEntities.add(viewEntity);
					propertiesInitializer.addProperties(viewEntity);
					addCalculatedFieldsForViews(viewEntity);
					modelStructure.addRootEntity(modelName, viewEntity);
				}
			}
			
			/*
			 * 2) Re-scan model structure to add nodes referencing view (inbound relations to Business Views)
			 */
			
			if (this.maxRecursionLevel >= 1) {
				//visit all entities
				List<IModelEntity> allEntities = visitModelStructure(modelStructure,modelName);
				
				for (int i=0; i<list.size(); i++){
					IModelViewEntityDescriptor viewDescriptor = list.get(i);
					List<IModelViewRelationshipDescriptor> viewRelationshipsDescriptors = viewDescriptor.getRelationshipDescriptors();
					for (IModelViewRelationshipDescriptor  viewRelationshipDescriptor : viewRelationshipsDescriptors){
						if (!viewRelationshipDescriptor.isOutbound()){
							String sourceEntityUniqueName = viewRelationshipDescriptor.getSourceEntityUniqueName();
							IModelEntity entity = modelStructure.getEntity(sourceEntityUniqueName);	
							logger.debug("Source Entity Unique name: "+entity.getUniqueName());
							
							//Add node for first level entities (using UniqueName)
							ModelViewEntity viewEntity = new ModelViewEntity(viewDescriptor, modelName, modelStructure, entity);
							addCalculatedFieldsForViews(viewEntity);
							propertiesInitializer.addProperties(viewEntity);
							this.addSubEntity(entity, viewEntity, 1);
							//entity.addSubEntity(viewEntity);
							
							//Add node for subentities (using Entity Type matching)
							for(IModelEntity modelEntity : allEntities){
								logger.debug("Searched Entity type: "+entity.getType());
								logger.debug("Current Entity type: "+modelEntity.getType());
								if (modelEntity.getType().equals(entity.getType())){
									ModelViewEntity viewEntitySub = new ModelViewEntity(viewDescriptor, modelName, modelStructure, modelEntity);
									addCalculatedFieldsForViews(viewEntitySub);
									propertiesInitializer.addProperties(viewEntitySub);
									logger.debug(" ** Found matching for: "+modelEntity.getType()+" with "+entity.getType());
									this.addSubEntity(modelEntity, viewEntitySub, modelEntity.getDepth());
									//modelEntity.addSubEntity(viewEntitySub);
									addedViewsEntities.add(viewEntitySub);
								}
							}	
						}
					}
				}
			}
			
			/*
			 * 3) Now add nodes corresponding to relations between Business Views (BV-to-BV)
			 * 	  Analyzing only outbound relationships because 
			 *    we always have an inbound relationships that's specular
			 */
			for (ModelViewEntity viewEntity : addedViewsEntities){
				//Outbound relationships
				viewEntity.addOutboundRelationshipsToViewEntities();
			}
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to add views to model structure", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	
	private List<IModelEntity> visitModelStructure(ModelStructure modelStructure, String modelName){
		List<IModelEntity> rootEntities = modelStructure.getRootEntities(modelName);
		List<IModelEntity> subEntities = new ArrayList<IModelEntity>();
		List<IModelEntity> allSubEntities = new ArrayList<IModelEntity>();
		for (IModelEntity entity : rootEntities){
			subEntities.addAll(entity.getAllSubEntities());
			visitLevel(entity.getAllSubEntities(),allSubEntities,1);
		}
		
		allSubEntities.addAll(subEntities);
		allSubEntities.addAll(rootEntities);
		return allSubEntities;
	}
	
	private void visitLevel(List<IModelEntity> entities, List<IModelEntity>allEntities, int iterationLevel){
		//logger.debug("visitLevel "+iterationLevel);
		if (iterationLevel < 8){
			for (IModelEntity entity:entities){
				allEntities.addAll(entity.getAllSubEntities());
				visitLevel(entity.getAllSubEntities(),allEntities,iterationLevel+1);
			}
		}
	}
	
	
	private void addEntity (IModelStructure modelStructure, String modelName, String entityType){

		String entityName = getEntityNameFromEntityType(entityType);		
		IModelEntity entity = modelStructure.addRootEntity(modelName, entityName, null, null, entityType);
		propertiesInitializer.addProperties(entity);
		
		//addKeyFields(dataMartEntity);		
		List<IModelEntity> subEntities = addNormalFields(entity);
		addCalculatedFields(entity);
		addSubEntities(entity, subEntities, 0);
	}
	
	private String getEntityNameFromEntityType(String entityType) {
		String entityName = entityType;
		entityName = (entityName.lastIndexOf('.') > 0 ?
				  entityName.substring(entityName.lastIndexOf('.') + 1 , entityName.length()) :
				  entityName);
				  
		return entityName;
	}

	/**
	 * This method adds the normal fields to the model entry structure
	 
	 * @param modelEntity:  the model entity to complete adding normal fields
	 * 
	 * @return a list of entities in ONE_TO_MANY relationship with the entity passed in as parameter (i.e. entities whose
	 * input entity is related to by means of e foreign key - MANY_TO_ONE relatioship)
	 */
	public List<IModelEntity> addNormalFields(IModelEntity modelEntity) {		
		
		logger.debug("Adding the field "+modelEntity.getName());
		List<IModelEntity> subEntities = new ArrayList<IModelEntity>();			
		EntityType thisEntityType = null;
		
		Metamodel classMetadata = getEntityManager().getMetamodel();
		
		for(Iterator it = classMetadata.getEntities().iterator(); it.hasNext(); ) {
			EntityType et = (EntityType)it.next();
			if(et.getJavaType().getName().equals(modelEntity.getType())){
				thisEntityType = et;
				break;
			}
		}	
		
		if(thisEntityType==null){
			return new ArrayList();
		}
		
		Set<Attribute> attributes = thisEntityType.getAttributes();
		Iterator<Attribute> attributesIt = attributes.iterator();
		
		
		while(attributesIt.hasNext()){
			Attribute a = attributesIt.next();
			//normal attribute
			if(a.getPersistentAttributeType().equals(PersistentAttributeType.BASIC)){		
				addField(a, modelEntity,"");
			} else if(a.getPersistentAttributeType().equals(PersistentAttributeType.MANY_TO_ONE)){ // relation
				Class c = a.getJavaType();
				javax.persistence.JoinColumn joinColumn = null;
				String entityType = c.getName();
				String columnName = a.getName();
				String joinColumnnName = a.getName();
				String entityName =  a.getName(); //getEntityNameFromEntityType(entityType);
				
				try {
					joinColumn = (javax.persistence.JoinColumn)(((java.lang.reflect.Field)a.getJavaMember()).getAnnotation(javax.persistence.JoinColumn.class));
				} catch (Exception e) {
					logger.error("Error loading the join column annotation for entity "+entityName, e);
				}

				if(joinColumn!=null){
					joinColumnnName = joinColumn.name();
					//add in the entity a property that maps the column name with the join column 
					modelEntity.getProperties().put(columnName, joinColumnnName);
				}
			
				
				IModelEntity subentity = new ModelEntity(entityName, null, columnName, entityType, modelEntity.getStructure());		
				subEntities.add(subentity);		
			} else if(a.getPersistentAttributeType().equals(PersistentAttributeType.EMBEDDED)){ // key
				Set<Attribute> keyAttre = ((EmbeddableType)((SingularAttribute)a).getType()).getAttributes();
				Iterator<Attribute> keyIter = keyAttre.iterator();
				while(keyIter.hasNext()){
					addField(keyIter.next(), modelEntity, a.getName()+".");	
				}
			}
		}
		
		logger.debug("Field "+modelEntity.getName()+" added");
		return subEntities;
	}
	
	/**
	 * Add an attribute to the model
	 * @param attr the attribute
	 * @param dataMartEntity the parent entity
	 */
	private void addField(Attribute attr, IModelEntity dataMartEntity, String keyPrefix){
		String n = attr.getName();
		Member m = attr.getJavaMember();
		Class c = attr.getJavaType();
		String type = c.getName();

		// TODO: SCALE E PREC
		int scale = 0;
		int precision = 0;

		IModelField modelField = dataMartEntity.addNormalField(keyPrefix+ attr.getName());
		modelField.setType(type);
		modelField.setPrecision(precision);
		modelField.setLength(scale);
		propertiesInitializer.addProperties(modelField);
	}
	
	private void addCalculatedFields(IModelEntity dataMartEntity) {
		logger.debug("Adding the calculated field "+dataMartEntity.getName());
		List calculatedFileds;
		ModelCalculatedField calculatedField;
		
		calculatedFileds = dataMartEntity.getStructure().getCalculatedFieldsByEntity(dataMartEntity.getUniqueName()); 
		if(calculatedFileds != null) {
			for(int i = 0; i < calculatedFileds.size(); i++) {
				calculatedField = (ModelCalculatedField)calculatedFileds.get(i);
				dataMartEntity.addCalculatedField(calculatedField);
				propertiesInitializer.addProperties(calculatedField);
			}
		}
		logger.debug("Added the calculated field "+dataMartEntity.getName());
	}

	private void addCalculatedFieldsForViews(IModelEntity dataMartEntity) {
		addCalculatedFields(dataMartEntity);
		
		for(int i = 0; i < dataMartEntity.getSubEntities().size(); i++) {
			if(!(dataMartEntity.getSubEntities().get(i) instanceof ModelViewEntity)){
				addCalculatedFieldsForViews(dataMartEntity.getSubEntities().get(i));
			}
		}
	}
	
	
	private void addSubEntities(IModelEntity modelEntity, List<IModelEntity> subEntities, int recursionLevel) {
		
		Iterator<IModelEntity> it = subEntities.iterator();
		while (it.hasNext()) {
			IModelEntity subentity = it.next();
			if (subentity.getType().equalsIgnoreCase(modelEntity.getType())){
				// ciclo di periodo 0!
			} else if(recursionLevel >= maxRecursionLevel) {
				// prune recursion tree 
			} else {
				addSubEntity(modelEntity, subentity, recursionLevel+1);
			}
		}
	}

	private void addSubEntity (IModelEntity parentEntity, IModelEntity subEntity, int recursionLevel){
		logger.debug("Adding the sub entity field "+subEntity.getName()+" child of "+parentEntity.getName());
		IModelEntity dataMartEntity;				
		
		//String entityName = getEntityNameFromEntityType(entityType);		
		dataMartEntity = parentEntity.addSubEntity(subEntity.getName(), subEntity.getRole(), subEntity.getType());
		propertiesInitializer.addProperties(dataMartEntity);
		
		//addKeyFields(dataMartEntity);			
		List<IModelEntity> subEntities = addNormalFields(dataMartEntity);		
		addCalculatedFields(dataMartEntity);
		addSubEntities(dataMartEntity, subEntities, recursionLevel);
		logger.debug("Added the sub entity field "+subEntity.getName()+" child of "+parentEntity.getName());
	}
	
	/**
	 * @return the JPADataSource
	 */
	public JPADataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @param JPADataSource the datasource to set
	 */
	public void setDataSource(JPADataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @return the entityManager
	 */
	public EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * @param entityManager the entityManager to set
	 */
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
}
