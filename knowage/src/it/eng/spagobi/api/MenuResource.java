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
package it.eng.spagobi.api;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.serializer.MenuListJSONSerializerForREST;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.wapp.util.MenuUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 * @class MenuResource
 *
 *        Provides services to retrieve menu composition for user
 *
 */

@Path("/1.0/menu")
@ManageAuthorization
public class MenuResource extends AbstractSpagoBIResource {

	public static transient Logger logger = Logger.getLogger(MenuResource.class);

	/**
	 * Service to return the content menu of the end user
	 *
	 * @param req
	 * @return
	 */
	@GET
	@Path("/enduser")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getEndUserMenu(@Context HttpServletRequest req) {

		logger.debug("IN");

		IEngUserProfile userProfile = getUserProfile();

		if (userProfile == null) {
			return ExceptionUtilities.serializeException("Profile not found when executing service", null);
		}

		try {
			UserProfile profile = (UserProfile) userProfile;
			String userName = (String) profile.getUserName();

			String country = req.getParameter("curr_country");
			String language = req.getParameter("curr_language");

			Locale currentLocale = new Locale(language, country, "");
			List lstMenu = new ArrayList();
			// search for custom menu defined for this user
			if (getAttributeFromHttpSession(MenuUtilities.LIST_MENU) != null) {
				lstMenu = (List) getAttributeFromHttpSession(MenuUtilities.LIST_MENU);
			}

			HttpSession session = req.getSession();
			// Locale locale = MessageBuilder.getBrowserLocaleFromSpago();
			List filteredMenuList = MenuUtilities.filterListForUser(lstMenu, userProfile);
			MenuListJSONSerializerForREST serializer = new MenuListJSONSerializerForREST(userProfile, session);
			JSONObject jsonMenuList = (JSONObject) serializer.serialize(filteredMenuList, currentLocale);

			jsonMenuList.put("userName", userName);

			return jsonMenuList.toString();

		} catch (Throwable t) {
			return ExceptionUtilities.serializeException("An unexpected error occured while executing service", null);
		} finally {
			logger.debug("OUT");
		}

	}

}
