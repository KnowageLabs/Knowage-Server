/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.rest.interceptors;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

/**
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 * Updates the audit log for the successful requests. For the services that throw exceptions look at RestExceptionMapper
 *
 */
@Provider
@ServerInterceptor
public class AuditRestPostInterceptor implements PostProcessInterceptor{


	static private Logger logger = Logger.getLogger(AuditRestPostInterceptor.class);
	
	
	@Context
	private HttpServletRequest servletRequest;
	@Context
	private HttpRequest request;
	

	
	/**
	 * Postprocess all the REST requests..
	 * Add an entry into the audit log for every rest service
	 */
	public void postProcess(ServerResponse response){
		logger.debug("AuditRestInterceptor:postProcess IN");
		
		
		
		try {	
			String serviceUrl = InterceptorUtilities.getServiceUrl(request);
			HashMap<String,String> parameters = InterceptorUtilities.getRequestParameters(request, servletRequest);
//			Object responseBody = response.getEntity();
//			if(responseBody!=null){
//				String responseBodyString = response.getEntity().toString();
//				parameters.put("Response Body", responseBodyString);
//			}

			
			UserProfile profile = (UserProfile) servletRequest.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
				
			String actionCode = "[Service:"+ serviceUrl +" ; Class:"+response.getResourceClass()+" ; Method:"+response.getResourceMethod()+"]";
			String result ="";
			if(response.getStatus()==200){
				result = "OK";
			}else{
				result="ERR ("+response.getStatus()+")";
			}
			AuditLogUtilities.updateAudit(servletRequest, profile, actionCode,	parameters, result);
		} catch (Exception e) {
			logger.error("Error updating audit", e);
		}finally{
			logger.debug("AuditRestInterceptor:postProcess OUT");
		}
	}
	
	

	


}