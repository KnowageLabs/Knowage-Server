/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.api.v3;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.v3.MenuListJSONSerializerForREST;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.themes.ThemesManager;
import it.eng.spagobi.wapp.util.MenuUtilities;

/**
 * @author albnale
 */
@Path("/3.0/menu")
@ManageAuthorization
public class MenuResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	@GET
	@Path("/enduser")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getEndUserMenu(@Context HttpServletRequest req) {

		logger.debug("IN");

		IEngUserProfile userProfile = getUserProfile();

		if (userProfile == null) {
			return ExceptionUtilities.serializeException("Profile not found when executing service", null);
		}

		UserProfile profile = (UserProfile) userProfile;

		Locale currentLocale = Locale.forLanguageTag(req.getParameter("locale"));
		List lstMenu = null;
		try {
			lstMenu = MenuUtilities.getMenuItems(profile);
		} catch (EMFUserError e1) {
			String message = "An error occured while retrieving menu";
			throw new SpagoBIRuntimeException(message, e1);
		}

		RequestContainer recCont = ChannelUtilities.getRequestContainer(req);
		String currentTheme = ThemesManager.getCurrentTheme(recCont);

		HttpSession session = req.getSession();
		// Locale locale = MessageBuilder.getBrowserLocaleFromSpago();
		List filteredMenuList = MenuUtilities.filterListForUser(lstMenu, userProfile);
		MenuListJSONSerializerForREST serializer = new MenuListJSONSerializerForREST(userProfile, session, currentTheme);
		JSONObject jsonMenuList = new JSONObject();
		try {
			jsonMenuList = (JSONObject) serializer.serialize(filteredMenuList, currentLocale);
		} catch (SerializationException e) {
			String message = "An error occured while serialiazing menu";
			throw new SpagoBIRuntimeException(message, e);
		}

		logger.debug("OUT");
		return jsonMenuList.toString();

	}

}
