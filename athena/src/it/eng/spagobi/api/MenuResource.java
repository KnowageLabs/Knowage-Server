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
