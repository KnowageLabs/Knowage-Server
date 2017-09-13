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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.knowage.engine.dossier.activity.bo.DossierActivity;
import it.eng.knowage.engines.dossier.rest.client.DossierActivityCreatorClient;
import it.eng.knowage.engines.dossier.rest.client.DossierExecutionClient;
import it.eng.knowage.engines.dossier.template.DossierTemplate;
import it.eng.knowage.engines.dossier.template.parameter.Parameter;
import it.eng.knowage.engines.dossier.template.placeholder.PlaceHolder;
import it.eng.knowage.engines.dossier.template.ppt.PptTemplate;
import it.eng.knowage.engines.dossier.template.report.Report;
import it.eng.spagobi.utilities.engines.rest.AbstractEngineRestService;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/dossier")
public class DossierExecutionResource extends AbstractEngineRestService{

	
	@Context
	 protected HttpServletRequest request;
	static private Logger logger = Logger.getLogger(DossierExecutionResource.class);
	
	@POST
	@Path("/run")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response executeDocuments(DossierTemplate dossierTemplate,@QueryParam ("activityName")String activityName,@QueryParam ("documentId")Integer documentId){
		
		
		String userId;
		Map<String,Object> executionParams;
		Map<String,Object> activityParams;
		DossierActivity dossierActivity;
		DossierExecutionClient dossierExecutionClient;
		DossierActivityCreatorClient activityCreatorClient;
		Integer threadId;
		Integer activityId;
		logger.debug("IN");
		
		userId = getUserId();
		activityParams= null;
		executionParams = null;
		
		
		
		
		try {
			logger.debug("clients creation");
			dossierExecutionClient = new DossierExecutionClient();
			activityCreatorClient = new DossierActivityCreatorClient();
			
			logger.debug("dossierExecutionClient calling a service to execute documets");
			threadId = dossierExecutionClient.executeDocuments(dossierTemplate, userId, executionParams);
			
			logger.debug("DossierActivityCreatorClient calling a service to create activity");
			dossierActivity = new  DossierActivity();
			dossierActivity.setActivity(activityName);
			dossierActivity.setDocumentId(documentId);
			dossierActivity.setProgressId(threadId);
			//dossierActivity.setBinId(1);;
			
			
			activityId = activityCreatorClient.createActivity(dossierActivity, userId, activityParams);
			logger.debug("OUT");
			
			if(activityId!=null){
				return Response.ok().build();
			}
			
			
			return Response.serverError().build();
			
		} catch (Exception e) {
			logger.error("error creating an activity",e);
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
