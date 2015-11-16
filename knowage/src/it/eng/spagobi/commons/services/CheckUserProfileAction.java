/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.services;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.security.IEngUserProfile;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * This action checks if user profile object is in session;
 * if the user profile is found, it returns "userProfileFound", elsewhere "userProfileNotFound"
 * See also home.jsp.
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class CheckUserProfileAction extends AbstractHttpAction {

	static private Logger logger = Logger.getLogger(CheckUserProfileAction.class);
	
	public void service(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		try {
			this.freezeHttpResponse();
			HttpServletResponse httpResponse = getHttpResponse();
			RequestContainer requestContainer = this.getRequestContainer();
			SessionContainer permanentSession = requestContainer.getSessionContainer().getPermanentContainer();
		    IEngUserProfile profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		    String toReturn = null;
		    if (profile == null) {
		    	toReturn = "userProfileNotFound";
		    } else {
		    	toReturn = "userProfileFound";
		    }
		    httpResponse.setContentLength(toReturn.length());
		    httpResponse.getOutputStream().write(toReturn.getBytes());
		    httpResponse.getOutputStream().flush();
    	} catch (Exception e) {
    	    logger.error("Error while checking user profile existence", e);
    	} finally {
    	    logger.debug("OUT");
    	}
	}

}