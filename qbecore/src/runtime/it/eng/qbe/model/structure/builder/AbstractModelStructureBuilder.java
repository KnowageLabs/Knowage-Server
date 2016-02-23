package it.eng.qbe.model.structure.builder;

import it.eng.qbe.datasource.AbstractDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelRelationshipDescriptor;
import it.eng.qbe.model.structure.ModelStructure;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public abstract class AbstractModelStructureBuilder implements IModelStructureBuilder {

	public static transient Logger logger = Logger.getLogger(AbstractModelStructureBuilder.class);

	protected AbstractDataSource dataSource;

	
	protected void addRelationshipsBetweenRootEntities(ModelStructure modelStructure) {

		String modelName = dataSource.getConfiguration().getModelName();

		// add relationship between rootEntities
		RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(modelName, false);
		List<IModelRelationshipDescriptor> relationships = dataSource.getConfiguration().loadRelationships();

		
		Map<String,DeserializedRelation> mergedRelations = new HashMap<String, DeserializedRelation>();
		
		//take all the relations that have same source and target and merge in the same relation
		//so if we have 2 relation like this:
		//--Entity A (field a)--> Entity B (field b)
		//--Entity A (field a1)--> Entity B (field b1)
		// we merge these 2 relations in Entity A (field a, field a1)--> Entity B (field b, field b1)
		for (IModelRelationshipDescriptor relationship : relationships) {
			IModelEntity sourceEntity = rootEntitiesGraph.getRootEntityByName(relationship.getSourceEntityUniqueName());
			if (sourceEntity == null)
				throw new RuntimeException("Impossibe to find source entity whose name is equal to [" + relationship.getSourceEntityUniqueName() + "]");
			List<IModelField> sourceFields = new ArrayList<IModelField>();
			List<String> sourceFieldNames = relationship.getSourceFieldUniqueNames();

			IModelEntity destinationEntity = rootEntitiesGraph.getRootEntityByName(relationship.getDestinationEntityUniqueName());
			if (destinationEntity == null)
				throw new RuntimeException("Impossibe to find destination entity whose name is equal to [" + relationship.getDestinationEntityUniqueName()
						+ "]");
			List<IModelField> destinationFields = new ArrayList<IModelField>();
			List<String> destinationFieldNames = relationship.getDestinationFieldUniqueNames();

			for (int i = 0; i < sourceFieldNames.size(); i++) {
				String sourceFieldName = sourceFieldNames.get(i);
				IModelField field = sourceEntity.getField(sourceFieldName);
				if (field == null) { // if the field is not part of the key it is not yet added to the entity. we have to create it now.
					String generatedFieldName = relationship.getName() + "." + destinationFieldNames.get(i).split(":")[1];
					field = sourceEntity.addNormalField(generatedFieldName);
					field.getProperties().put("visible", "true");
					field.getProperties().put("position", "" + Integer.MAX_VALUE);
					field.getProperties().put("type", "attribute");
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
			
			//search if the exist already a relation between the 2 entities. The name of relation has to be the same
			DeserializedRelation aDeserializedRelation = new DeserializedRelation(sourceEntity, sourceFields, destinationEntity, destinationFields, relationship.getType(),
						relationship.getLabel());
			
			DeserializedRelation aDeserializedRelationInMap = mergedRelations.get(sourceEntity.getUniqueName()+destinationEntity.getUniqueName()+relationship.getLabel());
			
			if(aDeserializedRelationInMap == null){
				mergedRelations.put(sourceEntity.getUniqueName()+destinationEntity.getUniqueName()+relationship.getLabel(),aDeserializedRelation);
			}else{
				aDeserializedRelationInMap.addSources(sourceFields);
				aDeserializedRelationInMap.addTo(destinationFields);
			}
		}
		
		
		for (String relationshipName : mergedRelations.keySet()) {
			DeserializedRelation aDeserializedRelation = mergedRelations.get(relationshipName);
			try {
				modelStructure.addRootEntityRelationship(modelName, aDeserializedRelation.getFromEntity(), aDeserializedRelation.getFromFields(), aDeserializedRelation.getToEntity(), aDeserializedRelation.getToFields(), aDeserializedRelation.getType(),
						aDeserializedRelation.getRelationName());
				logger.debug("Succesfully added relationship between [" + aDeserializedRelation.getFromEntity().getName() + "] and [" + aDeserializedRelation.getToEntity().getName() + "]");
			} catch (Throwable t) {
				logger.error("Impossible to add relationship between [" + aDeserializedRelation.getFromEntity().getName() + "] and [" + aDeserializedRelation.getToEntity().getName() + "]", t);
			}
		}
	}
	
	private class DeserializedRelation{

		IModelEntity fromEntity;
		List<IModelField> fromFields;
		IModelEntity toEntity;
		List<IModelField> toFields;
		String type; 
		String relationName;
		
		public DeserializedRelation(IModelEntity fromEntity,
				List<IModelField> fromFields, IModelEntity toEntity,
				List<IModelField> toFields, String type, String relationName) {
			super();
			this.fromEntity = fromEntity;
			this.fromFields = fromFields;
			this.toEntity = toEntity;
			this.toFields = toFields;
			this.type = type;
			this.relationName = relationName;
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

		public String getRelationName() {
			return relationName;
		}
		
		public void addSources(List<IModelField> first ){
			merge(fromFields, first);
		}
		
		public void addTo(List<IModelField> first ){
			merge(toFields, first);
		}
		
		private void merge(List<IModelField> first , List<IModelField> second){
			for(int i=0; i<second.size(); i++){
				if(!first.contains(second.get(i))){
					first.add(second.get(i));
				}
			}
		}
		
	}

}
