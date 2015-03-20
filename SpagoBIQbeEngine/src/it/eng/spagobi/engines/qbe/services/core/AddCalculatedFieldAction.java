/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.ModelCalculatedField;
import it.eng.qbe.model.structure.ModelCalculatedField.Slot;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONAcknowledge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * The Class ExecuteQueryAction.
 */
public class AddCalculatedFieldAction extends AbstractQbeEngineAction {	
	
	public static final String SERVICE_NAME = "ADD_CALCULATED_FIELD_ACTION";
	public String getActionName(){return SERVICE_NAME;}
	
	
	// INPUT PARAMETERS
	public static final String EDITING_MODE = "editingMode";
	public static final String FIELD_NAME = "fieldId";
	public static final String PARENT_ENTITY_UNIQUE_NAME = "entityId";
	public static final String FIELD = "field";
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(AddCalculatedFieldAction.class);
   
    
	
	public void service(SourceBean request, SourceBean response)  {				
			
		String editingMode;
		String fieldName;
		String parentEntityUniqueName;
		JSONObject fieldJSON;
				
		logger.debug("IN");
		
		try {
		
			super.service(request, response);
			
			editingMode = this.getAttributeAsString( EDITING_MODE );
			logger.debug("Parameter [" + EDITING_MODE + "] is equals to [" + editingMode + "]");
			Assert.assertNotNull(editingMode, "Parametr [" + EDITING_MODE + "] cannot be null");
			fieldName = null;
			if(editingMode.equalsIgnoreCase("modify")) {
				fieldName = this.getAttributeAsString( FIELD_NAME );
				logger.debug("Parameter [" + FIELD_NAME + "] is equals to [" + fieldName + "]");
				Assert.assertNotNull(fieldName, "Parametr [" + FIELD_NAME + "] cannot be null if parameter [" + EDITING_MODE + "] is equal to [" + editingMode + "]");
			} 
			
			parentEntityUniqueName = this.getAttributeAsString( PARENT_ENTITY_UNIQUE_NAME );
			logger.debug("Parameter [" + PARENT_ENTITY_UNIQUE_NAME + "] is equals to [" + parentEntityUniqueName + "]");
		
			fieldJSON = this.getAttributeAsJSONObject( FIELD );
			logger.debug("Parameter [" + FIELD + "] is equals to [" + fieldJSON + "]");
			
			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			
			
			ModelCalculatedField calculatedField = deserialize(fieldJSON);
		
			IModelEntity parentEntity = getDataSource().getModelStructure().getEntity(parentEntityUniqueName);
			if(editingMode.equalsIgnoreCase("modify")) {
				ModelCalculatedField calculatedFieldToModify = new ModelCalculatedField(fieldName, null, null);
				calculatedFieldToModify.setParent(parentEntity);
				parentEntity.deleteCalculatedField(calculatedFieldToModify.getUniqueName());
				parentEntity.addCalculatedField(calculatedField);
			} else {
				parentEntity.addCalculatedField(calculatedField);
			}
			
			try {
				writeBackToClient( new JSONAcknowledge() );
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}
			
		} catch(Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}			
	}

	private ModelCalculatedField deserialize(JSONObject fieldJSON) {
		ModelCalculatedField field;
		String alias;
		String fieldType;
		
		JSONObject fieldClaculationDescriptor;
		String type;
		String nature;
		String expression;
		String slots;

		
		
		try {
			alias = fieldJSON.getString(QuerySerializationConstants.FIELD_ALIAS);
			fieldType = fieldJSON.getString(QuerySerializationConstants.FIELD_TYPE);
						
			fieldClaculationDescriptor = fieldJSON.getJSONObject("calculationDescriptor");
			type = fieldClaculationDescriptor.getString(QuerySerializationConstants.FIELD_TYPE);
			nature = fieldClaculationDescriptor.getString(QuerySerializationConstants.FIELD_NATURE);
			expression = fieldClaculationDescriptor.getString(QuerySerializationConstants.FIELD_EXPRESSION);
			slots = fieldClaculationDescriptor.optString(QuerySerializationConstants.FIELD_SLOTS);
						
			fieldType = fieldJSON.getString("filedType");
			if(fieldType.equals("calculatedField")){
				field = new ModelCalculatedField(alias, type, expression);
			} else {
				field = new ModelCalculatedField(alias, type, expression, true);
			}
			field.setNature(nature);
			
			///begin patch : default properties --bug SPAGOBI-1292
			setDefaultProperties(field, nature);
			////---end patch
			
			if(slots != null && slots.trim().length() > 0) {
				JSONArray slotsJSON = new JSONArray(slots);
				List<Slot> slotList = new ArrayList<Slot>();
				for(int i = 0; i < slotsJSON.length(); i++) {
					Slot slot = (Slot)SerializationManager.deserialize(slotsJSON.get(i), "application/json", Slot.class);
					if( slot.getMappedValuesDescriptors().isEmpty() ){
						// it's the default slot
						field.setDefaultSlotValue(slot.getName());
					} else {
						slotList.add(slot);
					}
				}
				
				field.addSlots(slotList);
			}
			
			
		} catch (Throwable t) {
			throw new SpagoBIEngineServiceException(getActionName(), "impossible to deserialize calculated field [" + fieldJSON.toString() + "]", t);
		}					
		
		
		return field;
	}
	
	private void setDefaultProperties(ModelCalculatedField field, String nature){
		
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(QuerySerializationConstants.FIELD_VISIBLE, "true");	
		properties.put("position", "0");
		if(nature == null){
			nature = "attribute";
		}
		properties.put("type", nature.toLowerCase());
		properties.put("format", "null");
		field.setProperties(properties);	
		
	}
}