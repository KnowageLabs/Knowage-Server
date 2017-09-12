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
package it.eng.spagobi.engines.qbe.services.core;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.ModelCalculatedField;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONAcknowledge;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONObject;


/**
 * The Class ExecuteQueryAction.
 */
public class DeleteCalculatedFieldAction extends AbstractQbeEngineAction {	
	
	public static final String SERVICE_NAME = "DELETE_CALCULATED_FIELD_ACTION";
	public String getActionName(){return SERVICE_NAME;}
	
	
	// INPUT PARAMETERS
	public static final String PARENT_ENTITY_UNIQUE_NAME = "entityId";
	public static final String FIELD = "field";
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(DeleteCalculatedFieldAction.class);
   
    
	
	public void service(SourceBean request, SourceBean response)  {				
			
		String parentEntityUniqueName;
		JSONObject fieldJSON;
				
		logger.debug("IN");
		
		try {
		
			super.service(request, response);		
			
			parentEntityUniqueName = this.getAttributeAsString( PARENT_ENTITY_UNIQUE_NAME );
			logger.debug("Parameter [" + PARENT_ENTITY_UNIQUE_NAME + "] is equals to [" + parentEntityUniqueName + "]");
			
			fieldJSON = this.getAttributeAsJSONObject( FIELD );
			logger.debug("Parameter [" + FIELD + "] is equals to [" + fieldJSON + "]");
			
			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			
			ModelCalculatedField calculatedField = deserialize(fieldJSON);
			
			IModelEntity parentEntity = getDataSource().getModelStructure().getEntity(parentEntityUniqueName);
			calculatedField.setParent(parentEntity);
			parentEntity.deleteCalculatedField(calculatedField.getUniqueName());
			
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
		
		String fieldUniqueName;		
		String group;
		String order;
		String funct;
		
		JSONObject fieldClaculationDescriptor;
		String type;
		String expression;
		
		boolean visible;
		boolean included;
		
		
		try {
			alias = fieldJSON.getString(QuerySerializationConstants.FIELD_ALIAS);
			fieldType = fieldJSON.getString(QuerySerializationConstants.FIELD_TYPE);
						
			fieldClaculationDescriptor = fieldJSON.getJSONObject("calculationDescriptor");
			type = fieldClaculationDescriptor.getString(QuerySerializationConstants.FIELD_TYPE);
			expression = fieldClaculationDescriptor.getString(QuerySerializationConstants.FIELD_EXPRESSION);
			
			field = new ModelCalculatedField(alias, type, expression);
		} catch (Throwable t) {
			throw new SpagoBIEngineServiceException(getActionName(), "impossible to deserialize calculated field [" + fieldJSON.toString() + "]", t);
		}					
		
		
		return field;
	}
	
}