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

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.utilities.themes.ThemesManager;
import it.eng.spagobi.wapp.util.MenuUtilities;

/**
 *
 * SpagoBI - The Business Intelligence Free Platform
 *
 * Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 *
 **/

public class ChangeLanguage extends AbstractHttpAction {

	static private Logger logger = Logger.getLogger(ChangeLanguage.class);

	public static final String LIST_MENU = "LIST_MENU";

	UserProfile userProfile = null;

	@Override
	public void service(SourceBean serviceRequest, SourceBean serviceResponse) throws Exception {
		logger.debug("IN");
		RequestContainer reqCont = RequestContainer.getRequestContainer();
		SessionContainer sessCont = reqCont.getSessionContainer();
		SessionContainer permSess = sessCont.getPermanentContainer();

		HttpServletRequest servletRequest = getHttpRequest();

		Locale locale = MessageBuilder.getBrowserLocaleFromSpago();

		String srLanguage = (String) serviceRequest.getAttribute("language_id");
		String srCountry = (String) serviceRequest.getAttribute("country_id");
		String srScript = (String) serviceRequest.getAttribute("script_id");
		String isPublicUser = (String) serviceRequest.getAttribute("IS_PUBLIC_USER");
		logger.debug("language selected: " + srLanguage);
		String currTheme = (String) serviceRequest.getAttribute(ChangeTheme.THEME_NAME);
		if (currTheme == null)
			ThemesManager.getDefaultTheme();
		logger.debug("theme used: " + currTheme);

		IEngUserProfile profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		userProfile = null;
		String lang = "";
		if (profile instanceof UserProfile) {
			userProfile = (UserProfile) profile;
		}

		List<Locale> supportedLocales = GeneralUtilities.getSupportedLocales();

		if (srLanguage == null) {
			logger.error("language not specified");
		} else {

			Locale currLocale = null;
			for (Locale supportedLocale : supportedLocales) {
				boolean found = false;
				String language = supportedLocale.getLanguage();
				String country = supportedLocale.getCountry();
				String script = supportedLocale.getScript();

				found = language.equals(srLanguage) && country.equals(srCountry);

				if (StringUtils.isNotBlank(srScript)) {
					found &= script.equals(srScript);
				}

				if (found) {
					currLocale = supportedLocale;
					break;
				}
			}

			if (currLocale == null) {

			} else {
				String scriptForSession = StringUtils.isNotBlank(currLocale.getScript()) ? currLocale.getScript() : "";
				permSess.setAttribute("AF_LANGUAGE", currLocale.getLanguage());
				permSess.setAttribute("AF_COUNTRY", currLocale.getCountry());
				permSess.setAttribute("AF_SCRIPT", scriptForSession);

				if (userProfile != null) {
					userProfile.setAttributeValue(SpagoBIConstants.LANGUAGE, currLocale.getLanguage());
					userProfile.setAttributeValue(SpagoBIConstants.COUNTRY, currLocale.getCountry());
					userProfile.setAttributeValue(SpagoBIConstants.SCRIPT, scriptForSession);
					logger.debug("modified profile attribute to " + lang);
				} else {
					logger.error("profile attribute not modified to " + lang);
				}
			}

//			boolean found = false;
//			while (iter.hasNext() && found == false) {
//				Locale localeTmp = iter.next();
//				String lang_supported = localeTmp.getLanguage();
//				String country_supported = localeTmp.getCountry();
//
//				if (language.equalsIgnoreCase(lang_supported) && (country == null || country.equalsIgnoreCase(country_supported))) {
//
//					locale = new Locale(language, country, "");
//					permSess.setAttribute("AF_LANGUAGE", locale.getLanguage());
//					permSess.setAttribute("AF_COUNTRY", locale.getCountry());
//
//					if (userProfile != null) {
//						userProfile.setAttributeValue(SpagoBIConstants.LANGUAGE, language);
//						userProfile.setAttributeValue(SpagoBIConstants.COUNTRY, country);
//						logger.debug("modified profile attribute to " + lang);
//					} else {
//						logger.error("profile attribute not modified to " + lang);
//					}
//					found = true;
//				}
//			}
		}

//		MenuUtilities.getMenuItems(serviceRequest, serviceResponse, profile);
		List lstMenu = MenuUtilities.getMenuItems(profile);

		serviceResponse.setAttribute("MENU_MODE", "ALL_TOP");
		Collection functionalities = profile.getFunctionalities();
		boolean docAdmin = false;
		boolean docDev = false;
		boolean docTest = false;
		if (functionalities != null && !functionalities.isEmpty()) {
			docAdmin = functionalities.contains("DocumentAdministration") || functionalities.contains("DocumentAdminManagement");
			docDev = functionalities.contains("DocumentDevManagement");
			docTest = functionalities.contains("DocumentTestManagement");
		}

		String url = "/themes/" + currTheme + "/jsp/";
		if (UserUtilities.isTechnicalUser(profile)) {
			url += "adminHome.jsp";
		} else if (isPublicUser != null && isPublicUser.equalsIgnoreCase("TRUE")) {
			url += "publicUserHome.jsp";
		} else {
			url += "userHome.jsp";
		}
		servletRequest.getSession().setAttribute(LIST_MENU, lstMenu);
		getHttpRequest().getRequestDispatcher(url).forward(getHttpRequest(), getHttpResponse());
//		if (isPublicUser != null && isPublicUser.equalsIgnoreCase("TRUE")){
//			serviceResponse.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "userhomePublicUser");
//		}else{
//			serviceResponse.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "userhome");
//		}
		logger.debug("OUT");
	}

}
