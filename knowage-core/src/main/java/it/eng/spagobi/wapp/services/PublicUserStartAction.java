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
