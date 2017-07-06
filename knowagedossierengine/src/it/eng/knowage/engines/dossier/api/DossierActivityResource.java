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

package it.eng.knowage.engines.dossier.api;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.GenericType;

import it.eng.knowage.engine.dossier.activity.bo.DossierActivity;
import it.eng.knowage.engines.dossier.rest.client.DossierActivityCreatorClient;
import it.eng.knowage.engines.dossier.rest.client.PPTContentClient;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.utilities.engines.rest.AbstractEngineRestService;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/dossierActivity")
public class DossierActivityResource extends AbstractEngineRestService{

	
	@Context
	 protected HttpServletRequest request;
	static private Logger logger = Logger.getLogger(DossierActivityResource.class);
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Integer createActivity(DossierActivity dossierActivity){
		
		//Object dossierActivity;
		String userId;
		Map<String,Object> params;
		DossierActivityCreatorClient dossierActivityCreatorClient;
		Integer activityId;
		
		logger.debug("IN");
		
		userId = getUserId();
		params = null;
	
		try {
			dossierActivityCreatorClient = new DossierActivityCreatorClient();
			logger.debug("calling a service using rest client");
			activityId = dossierActivityCreatorClient.createActivity(dossierActivity, userId, params);
			logger.debug("OUT");
			
			return activityId;
			
		} catch (Exception e) {
			logger.error("error creating an activity",e);
			throw new SpagoBIRestServiceException(getLocale(),e);
		}
	}
	
	@POST
	@Path("/{id}/ppt")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public void storePPT(@MultipartForm MultipartFormDataInput multipartFormDataInput,@PathParam("id") Integer activityId){
		
		byte[] ppt;
		String userId;
		Map<String,Object> params;
		PPTContentClient pptContentClient;	
		
		logger.debug("IN");

		try {
			userId = getUserId();
			params = null;
			ppt =  multipartFormDataInput.getFormDataPart("file", new GenericType<byte[]>(){});	
			
			pptContentClient = new PPTContentClient();
			logger.debug("calling a service using rest client");
			 pptContentClient.storePPT(activityId, ppt, userId, params);
			logger.debug("OUT");
			
		} catch (Exception e) {
			logger.error("error storing a ppt",e);
			throw new SpagoBIRestServiceException(getLocale(),e);
		}
	}
	
	
	@Override
	public String getEngineName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return request;
	}
	
	

}
