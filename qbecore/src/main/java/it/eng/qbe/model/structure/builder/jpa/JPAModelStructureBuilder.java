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
package it.eng.qbe.model.structure.builder.jpa;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.log4j.Logger;

import it.eng.qbe.datasource.jpa.JPADataSource;
import it.eng.qbe.model.properties.initializer.IModelStructurePropertiesInitializer;
import it.eng.qbe.model.properties.initializer.ModelStructurePropertiesInitializerFactory;
import it.eng.qbe.model.structure.HierarchicalDimensionField;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor.IModelViewRelationshipDescriptor;
import it.eng.qbe.model.structure.ModelCalculatedField;
import it.eng.qbe.model.structure.ModelEntity;
import it.eng.qbe.model.structure.ModelStructure;
import it.eng.qbe.model.structure.ModelViewEntity;
import it.eng.qbe.model.structure.ModelViewEntity.Join;
import it.eng.qbe.model.structure.ModelViewEntity.ViewRelationship;
import it.eng.qbe.model.structure.builder.AbstractModelStructureBuilder;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JPAModelStructureBuilder extends AbstractModelStructureBuilder {

	private static final Logger LOGGER = Logger.getLogger(JPAModelStructureBuilder.class);
	private static final int DEFAULT_MAX_RECURSION_LEVEL = 0;

	public static final String VIEWS_INNER_JOINS_RELATION_NAME = "VIEWS_INNER_JOINS";

	private EntityManager entityManager;
	private int maxRecursionLevel;
	IModelStructurePropertiesInitializer propertiesInitializer;

	/**
	 * Constructor
	 *
	 * @param dataSource the JPA DataSource
	 */
	public JPAModelStructureBuilder(JPADataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("DataSource parameter cannot be null");
		}

		String maxRecursionLevelProperty = (String) dataSource.getConfiguration().loadDataSourceProperties().get("maxRecursionLevel");
		if (maxRecursionLevelProperty == null) {
			this.maxRecursionLevel = DEFAULT_MAX_RECURSION_LEVEL;
		} else {
			this.maxRecursionLevel = Integer.parseInt(maxRecursionLevelProperty);
		}

		setDataSource(dataSource);
		propertiesInitializer = ModelStructurePropertiesInitializerFactory.getDataMartStructurePropertiesInitializer(dataSource);
	}

	/**
	 * This method builds a JPA model structure.
	 *
	 * @return DataMartModelStructure
	 */
	@Override
	public IModelStructure build() {
		ModelStructure modelStructure;
		String modelName;

		LOGGER.debug("IN");

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

			Map<String, HierarchicalDimensionField> hierarchicalDimensions = getDataSource().getConfiguration().loadHierarchicalDimension();
			modelStructure.setHierarchicalDimension(hierarchicalDimensions);

			addRootEntities(modelStructure);
			addViews(modelStructure);
			addRelationshipsBetweenRootEntities(modelStructure);
			addRelationshipsBetweenViews(modelStructure);

			LOGGER.info("Model structure for model [" + modelName + "] succesfully built");

			return modelStructure;

		} catch (Throwable t) {
			throw new RuntimeException("Impossible to build model structure", t);
		} finally {
			LOGGER.debug("OUT");
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
					modelStructure.addRootEntityRelationship(modelName, sourceEntity, sourceFields, destinationEntity, destinationFields, "many-to-one",
							VIEWS_INNER_JOINS_RELATION_NAME, "dragan", null);
					LOGGER.debug("Succesfully added relationship between [" + sourceEntity.getName() + "] and [" + destinationEntity.getName() + "]");
				} catch (Throwable t) {
					LOGGER.error("Impossible to add relationship between [" + sourceEntity.getName() + "] and [" + destinationEntity.getName() + "]", t);
				}

			}

			for (ViewRelationship relationship : viewRelationships) {

				IModelEntity sourceEntity = relationship.getSourceEntity();
				List<IModelField> sourceFields = relationship.getSourceFileds();

				IModelEntity destinationEntity = relationship.getDestinationEntity();
				List<IModelField> destinationFields = relationship.getDestinationFileds();

				try {
					modelStructure.addRootEntityRelationship(modelName, sourceEntity, sourceFields, destinationEntity, destinationFields, "many-to-one",
							VIEWS_INNER_JOINS_RELATION_NAME, "dragan2", null);
					LOGGER.debug("Succesfully added relationship between [" + sourceEntity.getName() + "] and [" + destinationEntity.getName() + "]");
				} catch (Throwable t) {
					LOGGER.error("Impossible to add relationship between [" + sourceEntity.getName() + "] and [" + destinationEntity.getName() + "]", t);
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
		LOGGER.debug("Jpa metamodel contains [" + jpaEntities.size() + "] entity types");

		for (EntityType<?> entityType : jpaEntities) {
			LOGGER.debug("Adding entity type [" + entityType + "] to model structure");
			String entityTypeName = entityType.getJavaType().getName();
			addEntity(modelStructure, modelName, entityTypeName);
			LOGGER.info("Entity type [" + entityType + "] succesfully added to model structure");
		}
	}

	private void addViews(ModelStructure modelStructure) {

		try {
			String modelName = getDataSource().getConfiguration().getModelName();

			/*
			 * 1) Load Views definitions and adds to the model structure (with outbound relations from Business Views)
			 */
			List<ModelViewEntity> addedViewsEntities = new ArrayList<>();
			List<IModelViewEntityDescriptor> list = getDataSource().getConfiguration().loadViews();
			if (!list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					IModelViewEntityDescriptor viewDescriptor = list.get(i);

					ModelViewEntity viewEntity = new ModelViewEntity(viewDescriptor, modelName, modelStructure, null);
					addedViewsEntities.add(viewEntity);
					propertiesInitializer.addProperties(viewEntity);
					addCalculatedFieldsForViews(viewEntity);
					// addHierarchiesForViews(viewEntity);
					modelStructure.addRootEntity(modelName, viewEntity);
				}
			}

			/*
			 * 2) Re-scan model structure to add nodes referencing view (inbound relations to Business Views)
			 */

			if (this.maxRecursionLevel >= 1) {
				// visit all entities
				List<IModelEntity> allEntities = visitModelStructure(modelStructure, modelName);

				for (int i = 0; i < list.size(); i++) {
					IModelViewEntityDescriptor viewDescriptor = list.get(i);
					List<IModelViewRelationshipDescriptor> viewRelationshipsDescriptors = viewDescriptor.getRelationshipDescriptors();
					for (IModelViewRelationshipDescriptor viewRelationshipDescriptor : viewRelationshipsDescriptors) {
						if (!viewRelationshipDescriptor.isOutbound()) {
							String sourceEntityUniqueName = viewRelationshipDescriptor.getSourceEntityUniqueName();
							IModelEntity entity = modelStructure.getEntity(sourceEntityUniqueName);
							LOGGER.debug("Source Entity Unique name: " + entity.getUniqueName());

							// Add node for first level entities (using UniqueName)
							ModelViewEntity viewEntity = new ModelViewEntity(viewDescriptor, modelName, modelStructure, entity);
							addCalculatedFieldsForViews(viewEntity);
							// addHierarchiesForViews(viewEntity);
							propertiesInitializer.addProperties(viewEntity);
							this.addSubEntity(entity, viewEntity, 1);
							// entity.addSubEntity(viewEntity);

							// Add node for subentities (using Entity Type matching)
							for (IModelEntity modelEntity : allEntities) {
								LOGGER.debug("Searched Entity type: " + entity.getType());
								LOGGER.debug("Current Entity type: " + modelEntity.getType());
								if (modelEntity.getType().equals(entity.getType())) {
									ModelViewEntity viewEntitySub = new ModelViewEntity(viewDescriptor, modelName, modelStructure, modelEntity);
									addCalculatedFieldsForViews(viewEntitySub);
									// addHierarchiesForViews(viewEntitySub);
									propertiesInitializer.addProperties(viewEntitySub);
									LOGGER.debug(" ** Found matching for: " + modelEntity.getType() + " with " + entity.getType());
									this.addSubEntity(modelEntity, viewEntitySub, modelEntity.getDepth());
									// modelEntity.addSubEntity(viewEntitySub);
									addedViewsEntities.add(viewEntitySub);
								}
							}
						}
					}
				}
			}

			/*
			 * 3) Now add nodes corresponding to relations between Business Views (BV-to-BV) Analyzing only outbound relationships because we always have an
			 * inbound relationships that's specular
			 */
			for (ModelViewEntity viewEntity : addedViewsEntities) {
				// Outbound relationships
				viewEntity.addOutboundRelationshipsToViewEntities();
			}
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to add views to model structure", t);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	private List<IModelEntity> visitModelStructure(ModelStructure modelStructure, String modelName) {
		List<IModelEntity> rootEntities = modelStructure.getRootEntities(modelName);
		List<IModelEntity> subEntities = new ArrayList<>();
		List<IModelEntity> allSubEntities = new ArrayList<>();
		for (IModelEntity entity : rootEntities) {
			subEntities.addAll(entity.getAllSubEntities());
			visitLevel(entity.getAllSubEntities(), allSubEntities, 1);
		}

		allSubEntities.addAll(subEntities);
		allSubEntities.addAll(rootEntities);
		return allSubEntities;
	}

	private void visitLevel(List<IModelEntity> entities, List<IModelEntity> allEntities, int iterationLevel) {
		// logger.debug("visitLevel "+iterationLevel);
		if (iterationLevel < 8) {
			for (IModelEntity entity : entities) {
				allEntities.addAll(entity.getAllSubEntities());
				visitLevel(entity.getAllSubEntities(), allEntities, iterationLevel + 1);
			}
		}
	}

	private void addEntity(IModelStructure modelStructure, String modelName, String entityType) {

		String entityName = getEntityNameFromEntityType(entityType);
		IModelEntity entity = modelStructure.addRootEntity(modelName, entityName, null, null, entityType);
		propertiesInitializer.addProperties(entity);

		// addKeyFields(dataMartEntity);
		List<IModelEntity> subEntities = addNormalFields(entity);
		addCalculatedFields(entity);
		addHierarchies(entity);
		addSubEntities(entity, subEntities, 0);
	}

	private String getEntityNameFromEntityType(String entityType) {
		String entityName = entityType;
		entityName = (entityName.lastIndexOf('.') > 0 ? entityName.substring(entityName.lastIndexOf('.') + 1, entityName.length()) : entityName);

		return entityName;
	}

	/**
	 * This method adds the normal fields to the model entry structure
	 *
	 * @param modelEntity: the model entity to complete adding normal fields
	 *
	 * @return a list of entities in ONE_TO_MANY relationship with the entity passed in as parameter (i.e. entities whose input entity is related to by means of
	 *         e foreign key - MANY_TO_ONE relatioship)
	 */
	public List<IModelEntity> addNormalFields(IModelEntity modelEntity) {

		LOGGER.debug("Adding the field " + modelEntity.getName());
		List<IModelEntity> subEntities = new ArrayList<>();
		EntityType thisEntityType = null;

		Metamodel classMetadata = getEntityManager().getMetamodel();

		for (Iterator it = classMetadata.getEntities().iterator(); it.hasNext();) {
			EntityType et = (EntityType) it.next();
			if (et.getJavaType().getName().equals(modelEntity.getType())) {
				thisEntityType = et;
				break;
			}
		}

		if (thisEntityType == null) {
			return new ArrayList();
		}

		Set<Attribute> attributes = thisEntityType.getAttributes();
		Iterator<Attribute> attributesIt = attributes.iterator();

		while (attributesIt.hasNext()) {
			Attribute a = attributesIt.next();
			// normal attribute
			if (a.getPersistentAttributeType().equals(PersistentAttributeType.BASIC)) {
				addField(a, modelEntity, "");
			} else if (a.getPersistentAttributeType().equals(PersistentAttributeType.MANY_TO_ONE)) { // relation
				Class c = a.getJavaType();
				javax.persistence.JoinColumn joinColumn = null;
				String entityType = c.getName();
				String columnName = a.getName();
				String joinColumnnName = a.getName();
				String entityName = a.getName(); // getEntityNameFromEntityType(entityType);

				try {
					joinColumn = (((java.lang.reflect.Field) a.getJavaMember()).getAnnotation(javax.persistence.JoinColumn.class));
				} catch (Exception e) {
					LOGGER.error("Error loading the join column annotation for entity " + entityName, e);
				}

				if (joinColumn != null) {
					joinColumnnName = joinColumn.name();
					// add in the entity a property that maps the column name with the join column
					modelEntity.getProperties().put(columnName, joinColumnnName);
				}

				IModelEntity subentity = new ModelEntity(entityName, null, columnName, entityType, modelEntity.getStructure());
				subEntities.add(subentity);
			} else if (a.getPersistentAttributeType().equals(PersistentAttributeType.EMBEDDED)) { // key
				Set<Attribute> keyAttre = ((EmbeddableType) ((SingularAttribute) a).getType()).getAttributes();
				Iterator<Attribute> keyIter = keyAttre.iterator();
				while (keyIter.hasNext()) {
					addField(keyIter.next(), modelEntity, a.getName() + ".");
				}
			}
		}

		LOGGER.debug("Field " + modelEntity.getName() + " added");
		return subEntities;
	}

	/**
	 * Add an attribute to the model
	 *
	 * @param attr           the attribute
	 * @param dataMartEntity the parent entity
	 */
	private void addField(Attribute attr, IModelEntity dataMartEntity, String keyPrefix) {
		Class c = attr.getJavaType();
		String type = c.getName();
		boolean isKey = ((Field) attr.getJavaMember()).isAnnotationPresent(Id.class);

		// TODO: SCALE E PREC
		int scale = 0;
		int precision = 0;

		IModelField modelField = null;
		if (isKey) {
			modelField = dataMartEntity.addKeyField(keyPrefix + attr.getName());
		} else {
			modelField = dataMartEntity.addNormalField(keyPrefix + attr.getName());
		}
		modelField.setModelName(dataMartEntity.getModelName());
		modelField.setType(type);
		modelField.setPrecision(precision);
		modelField.setLength(scale);
		modelField.setJavaClass(c);
		propertiesInitializer.addProperties(modelField);
	}

	private void addCalculatedFields(IModelEntity dataMartEntity) {
		LOGGER.debug("Adding the calculated field " + dataMartEntity.getName());
		List calculatedFileds;
		ModelCalculatedField calculatedField;

		calculatedFileds = dataMartEntity.getStructure().getCalculatedFieldsByEntity(dataMartEntity.getUniqueName());
		if (calculatedFileds != null) {
			for (int i = 0; i < calculatedFileds.size(); i++) {
				calculatedField = (ModelCalculatedField) calculatedFileds.get(i);
				dataMartEntity.addCalculatedField(calculatedField);
				propertiesInitializer.addProperties(calculatedField);
			}
		}
		LOGGER.debug("Added the calculated field " + dataMartEntity.getName());
	}

	private void addCalculatedFieldsForViews(IModelEntity dataMartEntity) {
		addCalculatedFields(dataMartEntity);

		for (int i = 0; i < dataMartEntity.getSubEntities().size(); i++) {
			if (!(dataMartEntity.getSubEntities().get(i) instanceof ModelViewEntity)) {
				addCalculatedFieldsForViews(dataMartEntity.getSubEntities().get(i));
			}
		}
	}

	private void addHierarchies(IModelEntity dataMartEntity) {
		LOGGER.debug("Adding hierarchies " + dataMartEntity.getName());

		HierarchicalDimensionField dimension = dataMartEntity.getStructure().getHierarchicalDimensions().get(dataMartEntity.getUniqueType());
		if (dimension != null) {
			dimension.getProperties().put("visible", "true");
			dataMartEntity.addHierarchicalDimension(dimension);
		}
	}

	private void addHierarchiesForViews(IModelEntity dataMartEntity) {
		addHierarchies(dataMartEntity);

		for (int i = 0; i < dataMartEntity.getSubEntities().size(); i++) {
			if (!(dataMartEntity.getSubEntities().get(i) instanceof ModelViewEntity)) {
				addHierarchiesForViews(dataMartEntity.getSubEntities().get(i));
			}
		}
	}

	private void addSubEntities(IModelEntity modelEntity, List<IModelEntity> subEntities, int recursionLevel) {

		Iterator<IModelEntity> it = subEntities.iterator();
		while (it.hasNext()) {
			IModelEntity subentity = it.next();
			if (subentity.getType().equalsIgnoreCase(modelEntity.getType())) {
				// ciclo di periodo 0!
			} else if (recursionLevel >= maxRecursionLevel) {
				// prune recursion tree
			} else {
				addSubEntity(modelEntity, subentity, recursionLevel + 1);
			}
		}
	}

	private void addSubEntity(IModelEntity parentEntity, IModelEntity subEntity, int recursionLevel) {
		LOGGER.debug("Adding the sub entity field " + subEntity.getName() + " child of " + parentEntity.getName());
		IModelEntity dataMartEntity;

		// String entityName = getEntityNameFromEntityType(entityType);
		dataMartEntity = parentEntity.addSubEntity(subEntity.getName(), subEntity.getRole(), subEntity.getType());
		propertiesInitializer.addProperties(dataMartEntity);

		// addKeyFields(dataMartEntity);
		List<IModelEntity> subEntities = addNormalFields(dataMartEntity);
		addCalculatedFields(dataMartEntity);
		addHierarchies(dataMartEntity);
		addSubEntities(dataMartEntity, subEntities, recursionLevel);
		LOGGER.debug("Added the sub entity field " + subEntity.getName() + " child of " + parentEntity.getName());
	}

	/**
	 * @return the JPADataSource
	 */
	public JPADataSource getDataSource() {
		return (JPADataSource) dataSource;
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
