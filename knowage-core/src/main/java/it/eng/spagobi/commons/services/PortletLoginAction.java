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

import java.security.Principal;
import java.util.Locale;

import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.PortletUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.security.exceptions.SecurityException;

/**
 * This class reads user from portal and traces information for this connected
 * user; after it gets the profile for the principal user and puts it into the
 * permanent container.
 * 
 */
public class PortletLoginAction extends AbstractHttpAction {

    static Logger logger = Logger.getLogger(PortletLoginAction.class);

    /**
     * Service.
     * 
     * @param request
     *                the request
     * @param response
     *                the response
     * 
     * @throws Exception
     *                 the exception
     * 
     * @see it.eng.spago.dispatching.action.AbstractHttpAction#service(it.eng.spago.base.SourceBean,
     *      it.eng.spago.base.SourceBean)
     */
    public void service(SourceBean request, SourceBean response) throws Exception {
	logger.debug("IN");

	try {

	    RequestContainer reqCont = getRequestContainer();
	    SessionContainer sessionCont = reqCont.getSessionContainer();
	    SessionContainer permSession = sessionCont.getPermanentContainer();

	    IEngUserProfile profile = (IEngUserProfile) permSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

	    if (profile == null) {

		String userId = null;
		PortletRequest portletRequest = PortletUtilities.getPortletRequest();
		Principal principal = portletRequest.getUserPrincipal();
		if(principal != null) {
			userId = principal.getName();
		} else {
			logger.debug("Principal not found on request. Looking for a default user configuration.... ");
			String defatulUserSB = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.DEFAULT_USER");
			if(defatulUserSB != null) {
				userId = defatulUserSB;
				logger.debug("Default user configuration found = [" + userId + "]");
			} else 	{
				logger.error("No default user configuration found");
				throw new Exception("Cannot identify user");
			}
		}		
		
		logger.debug("got userId from Principal=" + userId);
		
		profile=UserUtilities.getUserProfile(userId);
		logger.debug("userProfile created.UserUniqueIDentifier= " + (String) profile.getUserUniqueIdentifier());
		logger.debug("Attributes name of the user profile: " + profile.getUserAttributeNames());
		logger.debug("Functionalities of the user profile: " + profile.getFunctionalities());
		logger.debug("Roles of the user profile: " + profile.getRoles());

		permSession.setAttribute(IEngUserProfile.ENG_USER_PROFILE, profile);
		// updates locale information on permanent container for Spago messages mechanism
		Locale locale = PortletUtilities.getLocaleForMessage();
		if (locale != null) {
			permSession.setAttribute(Constants.USER_LANGUAGE, locale.getLanguage());
			permSession.setAttribute(Constants.USER_COUNTRY, locale.getCountry());
		}
		
		//String username = (String) profile.getUserUniqueIdentifier();
		String username = (String)((UserProfile)profile).getUserId();
		if (!UserUtilities.userFunctionalityRootExists(username)) {
		    UserUtilities.createUserFunctionalityRoot(profile);
		}

	    }

	} catch (Exception e) {
	    logger.error("Exception");
	    throw new SecurityException("Exception", e);
	} finally {
	    logger.debug("OUT");
	}

    }

}
