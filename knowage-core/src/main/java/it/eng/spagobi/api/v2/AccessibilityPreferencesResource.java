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
package it.eng.spagobi.api.v2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.AccessibilityPreferences;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.dao.ISbiAccessibilityPreferencesDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/2.0/preferences")
@ManageAuthorization
public class AccessibilityPreferencesResource extends AbstractSpagoBIResource {
	private static Logger logger = Logger.getLogger(AccessibilityPreferencesResource.class);

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveAccessibilityControls(AccessibilityPreferences ap) {
		ISbiAccessibilityPreferencesDAO objDao = null;
		try {
			objDao = DAOFactory.getSiAccessibilityPreferencesDAO();
			objDao.setUserProfile(getUserProfile());

			objDao.saveOrUpdatePreferencesControls((String) getUserProfile().getUserId(), ap.isEnableUio(), ap.isEnableRobobraille(), ap.isEnableVoice(),
					ap.isEnableGraphSonification());

			return Response.ok().build();

		} catch (EMFUserError e) {
			logger.error("Error while saving accessibility preferences", e);
			throw new SpagoBIRestServiceException(getLocale(), e);

		}

	}

	@POST
	@Path("/uio")
	public Response updatePreferences(@Context HttpServletRequest req) {
		ISbiAccessibilityPreferencesDAO objDao = null;

		try {
			JSONObject jo = RestUtilities.readBodyAsJSONObject(req);

			objDao = DAOFactory.getSiAccessibilityPreferencesDAO();
			objDao.setUserProfile(getUserProfile());

			objDao.saveOrUpdateUserPreferences((String) getUserProfile().getUserId(), jo.toString());

			return Response.ok().build();

		} catch (EMFUserError e) {
			logger.error("Error while updating user preferences", e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		} catch (IOException e) {
			logger.error("Error while updating user preferences", e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		} catch (JSONException e) {
			logger.error("Error while updating user preferences", e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}

	}

}
