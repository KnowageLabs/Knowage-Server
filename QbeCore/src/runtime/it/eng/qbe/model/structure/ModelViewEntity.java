/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure;

import it.eng.qbe.model.structure.IModelViewEntityDescriptor.IModelViewJoinDescriptor;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor.IModelViewRelationshipDescriptor;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ModelViewEntity extends ModelEntity {
	
	List<IModelEntity> entities;
	List<Join> joins;
	List<ViewRelationship> viewRelationships;
	private static transient Logger logger = Logger.getLogger(ModelViewEntity.class);
	
	IModelViewEntityDescriptor viewDescriptor;
	String modelName;
	
	// =========================================================================
	// INNER CLASSES
	// =========================================================================
	
	public class Join {
		IModelEntity sourceEntity;
		List <IModelField> sourceFields;
		IModelEntity destinationEntity;
		List <IModelField> destinationFields;
		
		public String getFieldUniqueName(IModelEntity parentEntity, String fieldName) {
			if (parentEntity == null){
				logger.debug("parentEntity is null, field name is "+fieldName);
			}
			if(parentEntity.getParent() == null) {
				return parentEntity.getType() + ":" + fieldName;
			}
			
			
			String parentViewName = parentEntity.getPropertyAsString("parentView");
			if(parentViewName!= null) {
				return parentViewName+":"+parentEntity.getType() + ":" + getName();
			}
			return parentEntity.getUniqueName() + ":" + fieldName;
		}
		
		public Join(IModelViewJoinDescriptor joinDescriptor, String modelName, IModelStructure structure) {
			
			sourceEntity = structure.getRootEntity(modelName, joinDescriptor.getSourceEntityUniqueName());
			destinationEntity = structure.getRootEntity(modelName, joinDescriptor.getDestinationEntityUniqueName());
			
			sourceFields = new ArrayList<IModelField>();
			for(String fieldName : joinDescriptor.getSourceColumns()) {
				String fieldUniqueName = getFieldUniqueName(sourceEntity, fieldName);
				IModelField f = sourceEntity.getField(fieldUniqueName);
				if(f == null) {
					List<IModelField> fields = sourceEntity.getAllFields();
					String str = "";
					for(IModelField field : fields) {
						str = field.getUniqueName() + ";  ";
					}
					Assert.assertNotNull(f, "Impossible to find source field [" + fieldUniqueName + "]. Valid filed name are [" + str + "]");
				}
				
				sourceFields.add(f);
			}
			
			destinationFields = new ArrayList<IModelField>();
			for(String fieldName : joinDescriptor.getDestinationColumns()) {
				String fieldUniqueName = getFieldUniqueName(destinationEntity, fieldName);
				IModelField f = destinationEntity.getField(fieldUniqueName);
				Assert.assertNotNull(f, "Impossible to find destination field [" + fieldUniqueName + "]");
				destinationFields.add(f);
			}
			
		}
		
		public IModelEntity getSourceEntity() {
			return sourceEntity;
		}
		
		public IModelEntity getDestinationEntity() {
			return destinationEntity;
		}
		
		public List<IModelField> getSourceFileds() {
			return sourceFields;
		}
		
		public List<IModelField> getDestinationFileds() {
			return destinationFields;
		}
	}
	
	public class ViewRelationship {
		IModelEntity sourceEntity;
		List <IModelField> sourceFields;
		IModelEntity destinationEntity;
		List <IModelField> destinationFields;
		boolean isOutbound;
		
		public String getFieldUniqueName(IModelEntity parentEntity, String fieldName) {
			if (parentEntity == null){
				logger.debug("parentEntity is null, field name is "+fieldName);
				return null;
			} else {
				if(parentEntity.getParent() == null) {
					logger.debug("FieldUniqueName is "+parentEntity.getType() + ":" + fieldName);
					return parentEntity.getType() + ":" + fieldName;
				}
			} 
			String parentViewName = parentEntity.getPropertyAsString("parentView");
			if(parentViewName!= null) {
				return parentViewName+":"+parentEntity.getType() + ":" + getName();
			}
			return parentEntity.getUniqueName() + ":" + fieldName;
		}
		
		public ViewRelationship(IModelViewRelationshipDescriptor relationshipDescriptor, String modelName, IModelStructure structure) {
			isOutbound = relationshipDescriptor.isOutbound();
			
			if (!isOutbound){
				sourceEntity = structure.getRootEntity(modelName, relationshipDescriptor.getSourceEntityUniqueName());
								
				if (relationshipDescriptor.isSourceEntityView()){
					//empty
					sourceFields = new ArrayList<IModelField>();
				} else {
					sourceFields = new ArrayList<IModelField>();
					for(String fieldName : relationshipDescriptor.getSourceColumns()) {
						String fieldUniqueName = getFieldUniqueName(sourceEntity, fieldName);
						IModelField f = sourceEntity.getField(fieldUniqueName);
						if(f == null) {
							List<IModelField> fields = sourceEntity.getAllFields();
							String str = "";
							for(IModelField field : fields) {
								str = field.getUniqueName() + ";  ";
							}
							Assert.assertNotNull(f, "Impossible to find source field [" + fieldUniqueName + "]. Valid filed name are [" + str + "]");
						}
						
						sourceFields.add(f);
					}
				}
				destinationFields = new ArrayList<IModelField>();
				if (!relationshipDescriptor.isSourceEntityView()){
					List<String> detinationColumns = relationshipDescriptor.getDestinationColumns();
					if(detinationColumns!=null ){
						for(String fieldName : detinationColumns) {
							
							for(int x=0; x<entities.size(); x++ ){
								List<IModelField> fields = entities.get(x).getAllFields();
								if(fields!=null){
									for(int y=0; y<fields.size(); y++ ){
										if(fields.get(y).getName().equals("compId."+fieldName)){
											destinationFields.add(fields.get(y));
											destinationEntity = entities.get(x);
											break;			
										}
									}
								}
								if(destinationEntity!=null){
									break;
								}
							}
						}
					}
				}
			} else {
				destinationEntity = structure.getRootEntity(modelName, relationshipDescriptor.getDestinationEntityUniqueName());
				
				
				if (relationshipDescriptor.isDestinationEntityView()){
					//empty
					destinationFields = new ArrayList<IModelField>();
				} else {
					destinationFields = new ArrayList<IModelField>();
					for(String fieldName : relationshipDescriptor.getDestinationColumns()) {
						String fieldUniqueName = getFieldUniqueName(destinationEntity, fieldName);
						IModelField f = destinationEntity.getField(fieldUniqueName);
						if(f == null) {
							List<IModelField> fields = destinationEntity.getAllFields();
							String str = "";
							for(IModelField field : fields) {
								str = field.getUniqueName() + ";  ";
							}
							Assert.assertNotNull(f, "Impossible to find destination field [" + fieldUniqueName + "]. Valid filed name are [" + str + "]");
						}
						destinationFields.add(f);
					}
				}
				//destinationEntity.setParent((ModelViewEntity.this));
				sourceFields = new ArrayList<IModelField>();
				if (!relationshipDescriptor.isDestinationEntityView()){
					List<String> sourceColumns = relationshipDescriptor.getSourceColumns();
					if(sourceColumns!=null ){
						for(String fieldName : sourceColumns) {
							
							for(int x=0; x<entities.size(); x++ ){
								List<IModelField> fields = entities.get(x).getAllFields();
								if(fields!=null){
									for(int y=0; y<fields.size(); y++ ){
										if(fields.get(y).getName().equals("compId."+fieldName)){
											sourceFields.add(fields.get(y));
											sourceEntity = entities.get(x);
											break;			
										}
									}
								}
								if(sourceEntity!=null){
									break;
								}
							}
						}
					}
				}
			}
		}
		
		public IModelEntity getSourceEntity() {
			return sourceEntity;
		}
		
		public IModelEntity getDestinationEntity() {
			return destinationEntity;
		}
		
		public List<IModelField> getSourceFileds() {
			return sourceFields;
		}
		
		public List<IModelField> getDestinationFileds() {
			return destinationFields;
		}
	
		public boolean isOutbound() {
			return isOutbound;
		}
	}
	
	
	// =========================================================================
	// COSTRUCTORS 
	// =========================================================================

	
	
	public ModelViewEntity(IModelViewEntityDescriptor view,  String modelName, IModelStructure structure, IModelEntity parent) throws Exception{
		super(view.getName(), null,  view.getType(), parent , structure);
		
		viewDescriptor = view;
		this.modelName = modelName;
		
		entities = new ArrayList<IModelEntity>();
		subEntities = new HashMap<String,IModelEntity>();
		
		Set<String> innerEntityUniqueNames = view.getInnerEntityUniqueNames();
		for(String innerEntityUniqueName : innerEntityUniqueNames) {
			IModelEntity e = structure.getRootEntity(modelName, innerEntityUniqueName);
			IModelEntity clonedEntity = e.clone(null, getUniqueName());
			entities.add(clonedEntity);
		}
				
		joins = new ArrayList<Join>();
		List<IModelViewJoinDescriptor> joinDescriptors = view.getJoinDescriptors();
		for(IModelViewJoinDescriptor joinDescriptor : joinDescriptors) {
			joins.add( new Join(joinDescriptor, modelName, structure) );
		}

		viewRelationships = new ArrayList<ViewRelationship>();
		List<IModelViewRelationshipDescriptor> relationshipDescriptors = view.getRelationshipDescriptors();
		for(IModelViewRelationshipDescriptor relationshipDescriptor : relationshipDescriptors) {
			viewRelationships.add( new ViewRelationship(relationshipDescriptor, modelName, structure) );
		}

		//only outbound relationship from view are added as subentities
		//String subEntityPath;
		if (this.getStructure().getMaxRecursionLevel() > 0) {
			for(ViewRelationship relationship : viewRelationships){
				if (relationship.isOutbound()){
					IModelEntity me = (relationship.getDestinationEntity()).clone(this, null);
					this.addSubEntity(me);
				}
			}
		}
		
	}

	
	// =========================================================================
	// ACCESORS 
	// =========================================================================
	
	public IModelField getField(String fieldName) {
		IModelField field = null;
		for(IModelEntity entity : entities) {
			field = entity.getField(fieldName);
			if(field != null) break;
		}
		return field;
	}
	
	public List<IModelField> getAllFields() {
		List<IModelField> fields = new ArrayList<IModelField>();
		for(IModelEntity entity : entities) {
			fields.addAll( entity.getAllFields() );
		}
		return fields;
	}	
	
	
//	public List<ModelCalculatedField> getCalculatedFields() {
//		return calculatedFields;
//	}

	
	public List<IModelField> getKeyFields() {
		List<IModelField> fields = new ArrayList<IModelField>();
		for(IModelEntity entity : entities) {
			fields.addAll( entity.getKeyFields() );
		}
		return fields;
	}
	
	public List<IModelField> getFieldsByType(boolean isKey) {
		List<IModelField> fields = new ArrayList<IModelField>();
		for(IModelEntity entity : entities) {
			fields.addAll( entity.getFieldsByType(isKey) );
		}
		return fields;
	}
	
	public IModelEntity getEntityByField(IModelField field) {
		for(int x=0; x<entities.size(); x++ ){
			List<IModelField> fields = entities.get(x).getAllFields();
			if(fields!=null){
				for(int y=0; y<fields.size(); y++ ){
					if(fields.get(y).equals(field)){
						return entities.get(x);
					}
				}
			}
		}
		return null;
	}
	
	public List<IModelField> getNormalFields() {
		List<IModelField> fields = new ArrayList<IModelField>();
		for(IModelEntity entity : entities) {
			fields.addAll( entity.getFieldsByType(false) );
		}
		return fields;
	}
	
	
	public List<IModelField> getAllFieldOccurencesOnSubEntity(String entityName, String fieldName) {
		List<IModelField> fields = new ArrayList<IModelField>();
		for(IModelEntity entity : entities) {
			fields.addAll( entity.getAllFieldOccurencesOnSubEntity(entityName, fieldName) );
		}
		return fields;
	}

	public List<Join> getJoins() {
		 return joins;		
	}
	
	public List<IModelEntity> getInnerEntities() {
		 return entities;		
	}
	
	public List<ViewRelationship> getRelationships() {
		return viewRelationships;
	}
	
	public List<ViewRelationship> getRelationshipsToViews(){
		List<ViewRelationship> relationshipsToViews = new ArrayList<ViewRelationship>();
		List<IModelViewRelationshipDescriptor> relationshipDescriptors = viewDescriptor.getRelationshipToViewsDescriptors();
		for(IModelViewRelationshipDescriptor relationshipDescriptor : relationshipDescriptors) {
			relationshipsToViews.add( new ViewRelationship(relationshipDescriptor, modelName, structure) );
		}
		return relationshipsToViews;
	}
	
	//Only outbound relationship from view to another view are added as subentities
	public void addOutboundRelationshipsToViewEntities() {
		List<ViewRelationship> relationshipsToViews  = getRelationshipsToViews();
		if (!relationshipsToViews.isEmpty()){			
			for(ViewRelationship relationship : relationshipsToViews){
				if (relationship.isOutbound()){
					subEntities.put(relationship.getDestinationEntity().getUniqueName(),relationship.getDestinationEntity());
					logger.debug("["+relationship.getDestinationEntity()+"] was added as subentity of " +
							"["+relationship.getDestinationEntity().getUniqueName()+"]");
				}
			} 
		}
	}	
	
	public IModelEntity clone(IModelEntity newParent, String parentView){
		try {
			ModelViewEntity newModelEntity = new ModelViewEntity(viewDescriptor,  name, structure,  newParent);
			newModelEntity.calculatedFields = this.calculatedFields;
			if(newParent==null || newParent.getRoot()==null){
				newModelEntity.setRoot(newParent);
			}else{
				newModelEntity.setRoot(newParent.getRoot());
			}

			Map<String,Object> properties2 = new HashMap<String, Object>();
			for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext();) {
				String key= (String)iterator.next();
				String o = (String)properties.get(key);
				properties2.put(key.substring(0), o.substring(0));
			}
			properties2.put("parentView", parentView);
			
			newModelEntity.setProperties(properties2);

			return newModelEntity;
		} catch (Exception e) {
			logger.error("Error cloning the view"+name);
			throw new SpagoBIRuntimeException("Error cloning the view"+name, e);
		}

	}
}
