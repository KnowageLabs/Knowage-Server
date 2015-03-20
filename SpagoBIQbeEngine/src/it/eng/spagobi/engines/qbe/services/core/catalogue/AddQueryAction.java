/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
