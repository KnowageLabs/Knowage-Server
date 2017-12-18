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
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Return items contained into the catalogue. An item is composed by query, meta and all the properties 
 * needed to use it as TreeNode on the client side
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetCatalogueAction extends AbstractQbeEngineAction {	
	
	public static final String SERVICE_NAME = "GET_CATALOGUE_ACTION";
	public String getActionName(){return SERVICE_NAME;}
	
	
	// INPUT PARAMETERS
	// none
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(GetCatalogueAction.class);
   
    
	
	public void service(SourceBean request, SourceBean response)  {				
		
		JSONArray result;
		Iterator it;
		String id;
		Query query;
		//QueryMeta meta;
		
		JSONObject queryJSON;
		JSONObject metaJSON;
		
		JSONObject nodeJSON;
		
		logger.debug("IN");
		
		try {
		
			super.service(request, response);		
			
			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			
			result = new JSONArray();
			it = getEngineInstance().getQueryCatalogue().getAllQueries(false).iterator();
			while(it.hasNext()) {
				query = (Query)it.next();
				
				try {					
					queryJSON = serializeQuery(query);
					nodeJSON = createNode(queryJSON);
				} catch (Throwable e) {
					throw new SpagoBIEngineServiceException(getActionName(), "An error occurred while serializig query wiyh id equals to [" + query.getId() +"]", e);
				}
				
				result.put(nodeJSON);
			}
			
			try {
				writeBackToClient( new JSONSuccess(result) );
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
		return (JSONObject)SerializerFactory.getSerializer("application/json").serialize(query, getEngineInstance().getDataSource(), getLocale());
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
		JSONArray subqueries;
		JSONObject childNodeJSON;
		JSONArray childNodesJSON;
		
		nodeJSON = new JSONObject();
		nodeJSON.put("id", query.getString("id"));
		nodeJSON.put("text", query.getString("name"));
		
		nodeAttributes = new JSONObject();
		nodeAttributes.put("iconCls", "icon-query");
		nodeAttributes.put("query", query);
		
		nodeJSON.put("attributes", nodeAttributes);
		
		subqueries = query.getJSONArray("subqueries");
		if(subqueries.length() > 0) {
			//nodeJSON.put("leaf", false);	
			childNodesJSON = new JSONArray();
			for(int i = 0; i < subqueries.length(); i++) {
				childNodeJSON = createNode( subqueries.getJSONObject(i) );
				childNodesJSON.put(childNodeJSON);
			}
			nodeJSON.put("children", childNodesJSON);
			//query.put("subqueries", new JSONArray());
			//nodeAttributes.put("children", childNodesJSON);
		} else {
			nodeJSON.put("leaf", true);	
		}
	
		return nodeJSON;
	}

}
