/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.qbe.federation;

import it.eng.qbe.datasource.sql.DataSetPersister;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class FederationClient extends SimpleRestClient{

	private String serviceUrl = "/restful-services/federateddataset/federation";
	
	public FederationClient(){
		
	}
	
	static protected Logger logger = Logger.getLogger(DataSetPersister.class);

	public FederationDefinition getFederation(String federationID, DataSetServiceProxy proxy) throws Exception {

		logger.debug("IN");

		FederationDefinition toReturn = new FederationDefinition();
		
		Map<String, Object> parameters = new java.util.HashMap<String, Object> ();

		parameters.put("federationId", federationID);
		
		logger.debug("Call persist service in post");
		ClientResponse<String> resp = executePostService(parameters, serviceUrl);
		
		
		String respString = resp.getEntity(String.class);
		
		JSONObject jo = new JSONObject(respString);
		
		toReturn.setFederation_id(jo.getInt("id"));
		toReturn.setLabel(jo.getString("label"));
		toReturn.setName(jo.getString("name"));
		toReturn.setDescription(jo.getString("description"));
		toReturn.setRelationships(jo.getString("relationships"));
		
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

}