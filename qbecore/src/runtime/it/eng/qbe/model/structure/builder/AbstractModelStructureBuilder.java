package it.eng.qbe.model.structure.builder;

import it.eng.qbe.datasource.AbstractDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelRelationshipDescriptor;
import it.eng.qbe.model.structure.ModelStructure;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class AbstractModelStructureBuilder implements IModelStructureBuilder {

	public static transient Logger logger = Logger.getLogger(AbstractModelStructureBuilder.class);

	protected AbstractDataSource dataSource;

	protected void addRelationshipsBetweenRootEntities(ModelStructure modelStructure) {

		String modelName = dataSource.getConfiguration().getModelName();

		// add relationship between rootEntities
		RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(modelName, false);
		List<IModelRelationshipDescriptor> relationships = dataSource.getConfiguration().loadRelationships();

		for (IModelRelationshipDescriptor relationship : relationships) {
			if (relationship.getType().equals("many-to-one") == false && relationship.getType().equals("optional-many-to-one") == false)
				continue;

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
			try {
				modelStructure.addRootEntityRelationship(modelName, sourceEntity, sourceFields, destinationEntity, destinationFields, relationship.getType(),
						relationship.getLabel());
				logger.debug("Succesfully added relationship between [" + sourceEntity.getName() + "] and [" + destinationEntity.getName() + "]");
			} catch (Throwable t) {
				logger.error("Impossible to add relationship between [" + sourceEntity.getName() + "] and [" + destinationEntity.getName() + "]", t);
			}
		}
	}

}
