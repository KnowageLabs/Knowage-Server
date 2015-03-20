/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core;
       
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetAttributesAction extends AbstractQbeEngineAction {

	public static final String SERVICE_NAME = "GET_ATTRIBUTES_ACTION";
	public String getActionName(){return SERVICE_NAME;}
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(GetAttributesAction.class);
    
    
	public void service(SourceBean request, SourceBean response) {
		
		Iterator it;
		String attributesName;
		JSONArray nodes;
		JSONObject node;
		JSONObject nodeAttributes;
		UserProfile userProfile;
		logger.debug("IN");
		
		try {
			super.service(request, response);	
			
			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			Assert.assertNotNull(getEngineInstance().getEnv(), "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of Env");
			
			userProfile = (UserProfile)getEnv().get(EngineConstants.ENV_USER_PROFILE);
			
			
			nodes = new JSONArray();
			it = userProfile.getUserAttributeNames().iterator();
			while(it.hasNext()) {
				attributesName = (String)it.next();
				node = new JSONObject();
				node.put("id", attributesName);
				node.put("text", attributesName);
				node.put("leaf", true);
				nodeAttributes = new JSONObject();
				nodeAttributes.put("type", "attribute");
				nodeAttributes.put("value", "attributes['" + attributesName+ "']");
				node.put("attributes", nodeAttributes);
				nodes.put(node);
			}
			
			try {
				writeBackToClient( new JSONSuccess(nodes) );
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
}
