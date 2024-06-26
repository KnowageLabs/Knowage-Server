/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.qbe.model.structure.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import it.eng.qbe.datasource.AbstractDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelRelationshipDescriptor;
import it.eng.qbe.model.structure.ModelStructure;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;

public abstract class AbstractModelStructureBuilder implements IModelStructureBuilder {

	public static transient Logger logger = Logger.getLogger(AbstractModelStructureBuilder.class);

	protected AbstractDataSource dataSource;

	protected void addRelationshipsBetweenRootEntities(ModelStructure modelStructure) {

		String modelName = dataSource.getConfiguration().getModelName();
		// add relationship between rootEntities
		RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(modelName, false);
		List<IModelRelationshipDescriptor> relationships = dataSource.getConfiguration().loadRelationships();
		Map<String, DeserializedRelation> mergedRelations = new HashMap<String, DeserializedRelation>();

		// take all the relations that have same source and target and merge in the same relation
		// so if we have 2 relation like this:
		// --Entity A (field a)--> Entity B (field b)
		// --Entity A (field a1)--> Entity B (field b1)
		// we merge these 2 relations in Entity A (field a, field a1)--> Entity B (field b, field b1)
		for (IModelRelationshipDescriptor relationship : relationships) {
			IModelEntity sourceEntity = rootEntitiesGraph.getRootEntityByName(relationship.getSourceEntityUniqueName());
			List<IModelField> sourceFields = new ArrayList<IModelField>();
			List<String> sourceFieldNames = relationship.getSourceFieldUniqueNames();
			IModelEntity destinationEntity = rootEntitiesGraph.getRootEntityByName(relationship.getDestinationEntityUniqueName());
			List<IModelField> destinationFields = new ArrayList<IModelField>();
			List<String> destinationFieldNames = relationship.getDestinationFieldUniqueNames();
			if (sourceEntity == null)
				throw new RuntimeException("Impossibe to find source entity whose name is equal to [" + relationship.getSourceEntityUniqueName() + "]");

			if (destinationEntity == null)
				throw new RuntimeException(
						"Impossibe to find destination entity whose name is equal to [" + relationship.getDestinationEntityUniqueName() + "]");

			for (int i = 0; i < sourceFieldNames.size(); i++) {
				String sourceFieldName = sourceFieldNames.get(i);
				IModelField field = sourceEntity.getField(sourceFieldName);
				if (field == null) { // if the field is not part of the key it is not yet added to the entity. we have to create it now.
					String generatedFieldName = relationship.getName() + "." + destinationFieldNames.get(i).split(":")[1];
					field = sourceEntity.addNormalField(generatedFieldName);
					field.getProperties().put("visible", "true");
					field.getProperties().put("position", "" + Integer.MAX_VALUE);
					field.getProperties().put("type", "attribute");
					field.getProperties().put("relation", true);
					String columnName = sourceEntity.getPropertyAsString(relationship.getName());
					if (columnName == null) {
						columnName = generatedFieldName;
					}
					field.getProperties().put("joinColumnName", columnName);
				}
				sourceFields.add(field);
			}

			for (int i = 0; i < destinationFieldNames.size(); i++) {
				String destinationFieldName = destinationFieldNames.get(i);
				IModelField field = destinationEntity.getField(destinationFieldName);
				if (field == null) {
					String generatedFieldName = relationship.getName() + "." + sourceFieldNames.get(i).split(":")[1];
					field = destinationEntity.addNormalField(generatedFieldName);
					field.getProperties().put("visible", "true");
					field.getProperties().put("position", "" + Integer.MAX_VALUE);
					field.getProperties().put("type", "attribute");
					field.getProperties().put("relation", true);
					String columnName = sourceEntity.getPropertyAsString(relationship.getName());
					if (columnName == null) {
						columnName = generatedFieldName;
					}
					field.getProperties().put("joinColumnName", columnName);
					// throw new RuntimeException("Impossibe to find in destination entity [" + destinationEntity.getName() +
					// "] a field whose name is equal to [" + destinationFieldName + "]");
				}
				destinationFields.add(field);
			}

			// search if the exist already a relation between the 2 entities. The name of relation has to be the same
			DeserializedRelation aDeserializedRelation = new DeserializedRelation(sourceEntity, sourceFields, destinationEntity, destinationFields,
					relationship.getType(), relationship.getLabel(), relationship.getSourceJoinPath(), relationship.getTargetJoinPath());

			String key = sourceEntity.getUniqueName() + destinationEntity.getUniqueName() + relationship.getLabel() + relationship.getType();
			DeserializedRelation aDeserializedRelationInMap = mergedRelations.get(key);

			if (aDeserializedRelationInMap == null) {
				mergedRelations.put(key, aDeserializedRelation);
			} else {
				aDeserializedRelationInMap.addSources(sourceFields);
				aDeserializedRelationInMap.addTo(destinationFields);
			}
		}

		List<String> relationNamesList = sortRelations(mergedRelations.keySet());

		for (String relationshipName : relationNamesList) {
			DeserializedRelation aDeserializedRelation = mergedRelations.get(relationshipName);
			try {
				modelStructure.addRootEntityRelationship(modelName, aDeserializedRelation.getFromEntity(), aDeserializedRelation.getFromFields(),
						aDeserializedRelation.getToEntity(), aDeserializedRelation.getToFields(), aDeserializedRelation.getType(),
						aDeserializedRelation.getRelationName(), aDeserializedRelation.getSourceJoinPath(), aDeserializedRelation.getTargetJoinPath());
				logger.debug("Succesfully added relationship between [" + aDeserializedRelation.getFromEntity().getName() + "] and ["
						+ aDeserializedRelation.getToEntity().getName() + "]");
			} catch (Throwable t) {
				logger.error("Impossible to add relationship between [" + aDeserializedRelation.getFromEntity().getName() + "] and ["
						+ aDeserializedRelation.getToEntity().getName() + "]", t);
			}
		}
	}

	/**
	 * Sort the relations in order to get the many to one before.. This because t
	 *
	 * @param relationNames
	 * @return
	 */
	private List<String> sortRelations(Set<String> relationNames) {
		List<String> relationNamesList = new ArrayList<String>();
		for (String relationshipName : relationNames) {
			if (relationshipName.endsWith("many-to-one")) {
				relationNamesList.add(relationshipName);
			}
		}

		for (String relationshipName : relationNames) {
			if (!relationshipName.endsWith("many-to-one")) {
				relationNamesList.add(relationshipName);
			}
		}
		return relationNamesList;
	}

	private class DeserializedRelation {

		IModelEntity fromEntity;
		List<IModelField> fromFields;
		IModelEntity toEntity;
		List<IModelField> toFields;
		String type;
		String relationName;
		String sourceJoinPath;
		String targetJoinPath;

		public DeserializedRelation(IModelEntity fromEntity, List<IModelField> fromFields, IModelEntity toEntity, List<IModelField> toFields, String type,
				String relationName, String sourceJoinPath, String targetJoinPath) {
			super();
			this.fromEntity = fromEntity;
			this.fromFields = fromFields;
			this.toEntity = toEntity;
			this.toFields = toFields;
			this.type = type;
			this.relationName = relationName;
			this.sourceJoinPath = sourceJoinPath;
			this.targetJoinPath = targetJoinPath;
		}

		public IModelEntity getFromEntity() {
			return fromEntity;
		}

		public List<IModelField> getFromFields() {
			return fromFields;
		}

		public IModelEntity getToEntity() {
			return toEntity;
		}

		public List<IModelField> getToFields() {
			return toFields;
		}

		public String getType() {
			return type;
		}

		public String getSourceJoinPath() {
			return sourceJoinPath;
		}

		public String getTargetJoinPath() {
			return targetJoinPath;
		}

		public String getRelationName() {
			return relationName;
		}

		public void addSources(List<IModelField> first) {
			merge(fromFields, first);
		}

		public void addTo(List<IModelField> first) {
			merge(toFields, first);
		}

		private void merge(List<IModelField> first, List<IModelField> second) {
			for (int i = 0; i < second.size(); i++) {
				if (!first.contains(second.get(i))) {
					first.add(second.get(i));
				}
			}
		}

	}

}
