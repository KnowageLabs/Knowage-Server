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