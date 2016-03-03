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

package it.eng.spagobi.engines.qbe.federation;

import it.eng.qbe.datasource.sql.DataSetPersister;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class FederationClient extends SimpleRestClient{

	private String getServiceUrl = "/restful-services/federateddataset/federation";
	private String addServiceUrl = "/restful-services/federateddataset/insertNoDup";
	public static final String DATASET_ID = "id";
	public static final String VERSION_NUM = "versionNum";
	
	public FederationClient(){
		
	}
	
	static protected Logger logger = Logger.getLogger(DataSetPersister.class);

	public FederationDefinition getFederation(String federationID, String userId, DataSetServiceProxy proxy) throws Exception {

		logger.debug("IN");

		FederationDefinition toReturn = new FederationDefinition();
		
		Map<String, Object> parameters = new java.util.HashMap<String, Object> ();

		parameters.put("federationId", federationID);
		
		logger.debug("Call persist service in post");
		ClientResponse<String> resp = executePostService(parameters, getServiceUrl, userId, null, null);
		
		
		String respString = resp.getEntity(String.class);
		
		JSONObject jo = new JSONObject(respString);
		
		toReturn.setFederation_id(jo.optInt("id"));
		toReturn.setLabel(jo.getString("label"));
		toReturn.setName(jo.getString("name"));
		toReturn.setDescription(jo.optString("description"));
		toReturn.setRelationships(jo.optString("relationships"));
		
		JSONArray datasetsArray = jo.optJSONArray("sourceDataset");
		Set<IDataSet> datasest = new java.util.HashSet<IDataSet>();
		
		if(datasetsArray!=null){
			for (int i = 0; i < datasetsArray.length(); i++) {
				String label = datasetsArray.getJSONObject(i).getString("label");
				IDataSet dataset = proxy.getDataSetByLabel(label);
				datasest.add(dataset);
			}
		}
		toReturn.setSourceDatasets(datasest);
		
		
		
		logger.debug("OUT");
		
		return toReturn;
	}

	
	
	public FederationDefinition addFederation(FederationDefinition federation, String userId) throws Exception {
		logger.debug("IN");

		Map<String, Object> parameters = new java.util.HashMap<String, Object> ();
	
		logger.debug("Call persist service in post");
		ClientResponse<String> resp = executePostService(parameters, addServiceUrl, userId, MediaType.TEXT_HTML_TYPE, serialize(federation).toString());
		//ClientResponse<String> resp = executePostService(parameters, addServiceUrl, null,null);
				
		String respString = resp.getEntity(String.class);
		
		
		federation.setFederation_id(new Integer(respString));
		
		
		return federation;
	}
	
	
	public static JSONObject serialize(FederationDefinition fd) {
		JSONObject result = null;

		try {
			result = new JSONObject();
			result.put("id", fd.getFederation_id());
			result.put("label", fd.getLabel());
			result.put("name", fd.getName());
			result.put("description", fd.getDescription());
			result.put("degenerated", fd.isDegenerated());
			
			JSONArray ja = new JSONArray();
			Set<IDataSet> sourceDatasets = fd.getSourceDatasets();
			
			for (Iterator iterator = sourceDatasets.iterator(); iterator.hasNext();) {
				IDataSet iDataSet = (IDataSet) iterator.next();
				JSONObject jo = new JSONObject();
				jo.put(DATASET_ID, iDataSet.getId());
				
				ja.put(jo);
			}
			
			
			result.put("sourcesDataset", ja);
			
		}catch (Exception e){
			logger.error("Error creating a new federation linked to datase. Serialization error", e);
			throw new SpagoBIEngineRuntimeException("Error creating a new federation linked to datase. Serialization error", e);
		}

		return result;
	}
	
	
}