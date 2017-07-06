/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.engines.dossier.rest.client;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.knowage.engine.dossier.activity.bo.DossierActivity;
import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;

public class PPTContentClient extends SimpleRestClient{
	
	private static final String SERVICE_URL = "/restful-services/dossier/activity/";
	static private Logger logger = Logger.getLogger(PPTContentClient.class);
	
	public void storePPT(Integer activityId, byte[] ppt,String userId,Map<String,Object> queryParams){
		
		logger.debug("IN");
		ClientResponse<Integer> clientResponse; 
		String url = SERVICE_URL+activityId+"/ppt";
		Map<String,Object> formParams;
		
		try {
			formParams = new HashMap<>();
			formParams.put("file", ppt);
			logger.debug("executing post service to create an activity");
			clientResponse = executePostService(queryParams, formParams,url, userId, MediaType.MULTIPART_FORM_DATA_TYPE, null);
			logger.debug("response");
			
			
			
		} catch (Exception e) {
			logger.error("Error while creating dossier activity",e);
		}
		logger.debug("OUT");
	}

}
