/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.wapp.services;

/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.wapp.bo.Menu;
import it.eng.spagobi.wapp.util.MenuUtilities;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class ChangeTheme extends AbstractHttpAction{

	static private Logger logger = Logger.getLogger(ChangeTheme.class);

	public static final String THEME_NAME="THEME_NAME";
	
    UserProfile userProfile = null;
	
	
	public void service(SourceBean serviceRequest, SourceBean serviceResponse)
	throws Exception {
		logger.debug("IN");
		RequestContainer reqCont = RequestContainer.getRequestContainer();
		SessionContainer sessCont = reqCont.getSessionContainer();
		SessionContainer permSess = sessCont.getPermanentContainer();

		String theme_name=(String)serviceRequest.getAttribute(THEME_NAME);
		logger.debug("theme selected: "+theme_name);

		
		
		permSess.setAttribute(SpagoBIConstants.THEME, theme_name);
		

		IEngUserProfile profile = (IEngUserProfile)permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		userProfile=null;
		if (profile  instanceof UserProfile) {
			userProfile = (UserProfile) profile;
		}
		
		MenuUtilities.getMenuItems(serviceRequest, serviceResponse,profile);
		
		serviceResponse.setAttribute("MENU_MODE", "ALL_TOP");
		serviceResponse.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "userhome");
		logger.debug("OUT");
	}

	
	
	
	/**
	 * Check if the menu element in input is already presents into the list
	 * @param lst the list to check
	 * @param menu the element to check
	 * @return true if the element is already presents, false otherwise
	 */
	private boolean containsMenu(List lst, Menu menu){
		if (lst == null)
			return false;
		
		for (int i=0; i<lst.size(); i++){
			Menu tmpMenu = (Menu)lst.get(i);
			if (tmpMenu.getMenuId().intValue() == menu.getMenuId().intValue())
				return true;	
		}
		return false;
	}
    /**
     * Finds the user identifier from http request or from SSO system (by the http request in input).
     * Use the SsoServiceInterface for read the userId in all cases, if SSO is disabled use FakeSsoService.
     * Check spagobi_sso.xml
     * 
     * @param httpRequest The http request
     * @param serviceRequest the service request
     * 
     * @return the current user unique identified
     * 
     * @throws Exception in case the SSO is enabled and the user identifier specified on service request is different from the SSO detected one.
     */
    private static String findUserId(SourceBean serviceRequest, HttpServletRequest httpRequest) throws Exception {
    	logger.debug("IN");
    	String userId = null;
    	try {
    		// Get userid from request
	    	Object requestUserIdObj = serviceRequest.getAttribute("userid");
	    	if (requestUserIdObj != null) userId = requestUserIdObj.toString();    	
    	} finally {
    		logger.debug("OUT: userId = [" + userId + "]");
    	}
    	return userId;
    }	
	
	
	
	
}



