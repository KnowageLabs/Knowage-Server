/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 21-apr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
