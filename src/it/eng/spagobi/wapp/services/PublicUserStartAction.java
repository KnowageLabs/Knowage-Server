/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.wapp.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.AbstractBaseHttpAction;
import it.eng.spagobi.utilities.themes.ThemesManager;
import it.eng.spagobi.wapp.util.MenuUtilities;

import org.apache.log4j.Logger;

public class PublicUserStartAction extends AbstractBaseHttpAction{
	public static final String SERVICE_NAME = "START_ACTION_PUBLIC_USER";
	
	static private Logger logger = Logger.getLogger(PublicUserStartAction.class);

	/**
	 *  Creates a publicUserProfile and puts it into the session.
	 */
	public void service(SourceBean serviceRequest, SourceBean serviceResponse)
			throws Exception {
		logger.debug("IN on service");
		try {
			RequestContainer reqCont = RequestContainer.getRequestContainer();
			ResponseContainer respCont= ResponseContainer.getResponseContainer();
			SessionContainer sessCont = reqCont.getSessionContainer();
			SessionContainer permSess = sessCont.getPermanentContainer();
			HttpServletRequest httpRequest =getHttpRequest();
			HttpServletResponse httpResponse = getHttpResponse();
			
			IEngUserProfile userProfile = GeneralUtilities.createNewUserProfile(SpagoBIConstants.PUBLIC_USER_ID);
			permSess.setAttribute(IEngUserProfile.ENG_USER_PROFILE, userProfile);
			
			List lstMenu = MenuUtilities.getMenuItems(userProfile);		
			httpRequest.getSession().setAttribute(MenuUtilities.LIST_MENU, lstMenu);
			
			//defining url for user home with theme
			ConfigSingleton config = ConfigSingleton.getInstance();
			String currTheme=ThemesManager.getCurrentTheme(reqCont);
	    	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
			logger.debug("theme: "+currTheme);
			String url = "/themes/" + currTheme	+ "/jsp/publicUserHome.jsp";
			
			httpRequest.getRequestDispatcher(url).forward(httpRequest, httpResponse);
			

		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Exception occurred while retrieving metadata", e);
		} finally {
			logger.debug("OUT");
		}	
	}
}
