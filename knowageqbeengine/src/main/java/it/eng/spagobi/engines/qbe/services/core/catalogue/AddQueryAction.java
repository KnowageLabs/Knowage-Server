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
package it.eng.spagobi.engines.qbe.services.core.catalogue;

import it.eng.qbe.query.Query;
import it.eng.qbe.query.QueryMeta;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * The Class ExecuteQueryAction.
 */
public class AddQueryAction extends AbstractQbeEngineAction {	
	
	public static final String SERVICE_NAME = "ADD_QUERY_ACTION";
	public String getActionName(){return SERVICE_NAME;}
	
	
	// INPUT PARAMETERS
	public static final String QUERY_NAME = "name";
	
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(AddQueryAction.class);
   
    
	
	public void service(SourceBean request, SourceBean response)  {				
				
		String name;
		Query query;
		JSONObject queryJSON;	
		JSONObject nodeJSON;
					
		logger.debug("IN");
		
		try {
		
			super.service(request, response);		
			
			name = getAttributeAsString( QUERY_NAME );
			logger.debug("Parameter [" + QUERY_NAME + "] is equals to [" + name + "]");
			
			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			
			query = new Query();
			getEngineInstance().getQueryCatalogue().addQuery(query);
			
			queryJSON = serializeQuery(query);
			nodeJSON = createNode(queryJSON);
			
			try {
				writeBackToClient( new JSONSuccess(nodeJSON) );
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
	
	// @TODO create a general purpose serializer not dependant on the datamartModel
	private JSONObject serializeQuery(Query query) throws SerializationException {
		return (JSONObject)SerializerFactory.getSerializer("application/json").serialize(query, getEngineInstance().getDataSource(), null);
	}

	private JSONObject serializeMeta(QueryMeta meta) throws JSONException {
		JSONObject metaJSON;
		
		metaJSON = new JSONObject();
		metaJSON.put("id", meta.getId());
		metaJSON.put("name", meta.getName());
		return metaJSON;
	}
	
	private JSONObject createNode(JSONObject query) throws JSONException {
		JSONObject nodeJSON;
		JSONObject nodeAttributes;
		
		nodeJSON = new JSONObject();
		nodeJSON.put("id", query.getString("id"));
		nodeJSON.put("text", query.getString("name"));
		nodeJSON.put("leaf", true);	
		
		nodeAttributes = new JSONObject();
		nodeAttributes.put("iconCls", "icon-query");
		nodeAttributes.put("query", query);
		
		nodeJSON.put("attributes", nodeAttributes);
		
		return nodeJSON;
	}

}
