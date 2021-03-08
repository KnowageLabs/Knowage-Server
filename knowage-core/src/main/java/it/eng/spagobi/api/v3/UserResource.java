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

import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.node.ObjectNode;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.api.v2.ProfileAttributeResourceRoleProcessor;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bo.UserBO;
import it.eng.spagobi.profiling.bo.UserInformationBO;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.profiling.dao.SbiUserDAOHibImpl;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;

@Path("/3.0/users")
@ManageAuthorization
public class UserResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	@GET
	@Path("/current")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getCurrentUserInformation(@Context HttpServletRequest httpRequest) {
		SbiUserDAOHibImpl hib = new SbiUserDAOHibImpl();
		try {

			SbiUser sbiUser = new SbiUser();
			UserBO user = new UserBO();
			ISbiUserDAO usersDao = DAOFactory.getSbiUserDAO();
			UserProfile userProfile = getUserProfile();
			usersDao.setUserProfile(userProfile);
			sbiUser = usersDao.loadSbiUserByUserId(String.valueOf(userProfile.getUserId()));

			if (sbiUser == null) {
				String message = "No current user existing.";
				logger.error(message);
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			user = hib.toUserBO(sbiUser);

			ISbiAttributeDAO objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(userProfile);

			if (!UserUtilities.isTechnicalUser(getUserProfile())) {
				ProfileAttributeResourceRoleProcessor roleFilter = new ProfileAttributeResourceRoleProcessor();
				ArrayList<Integer> hiddenAttributesIds = roleFilter.getHiddenAttributesIds();
				roleFilter.removeHiddenAttributes(hiddenAttributesIds, user);
			}

			UserInformationBO userInformationBO = new UserInformationBO(user);

			JSONObject documentsJSON = null;
			MessageBuilder m = new MessageBuilder();
			Locale locale = m.getLocale(httpRequest);
			ObjectNode documentsObjectNode = null;
			try {
				documentsJSON = (JSONObject) SerializerFactory.getSerializer(MediaType.APPLICATION_JSON).serialize(userInformationBO, locale);
				if (documentsJSON != null)
					documentsObjectNode = documentsJSON.getWrappedObject();
			} catch (SerializationException e) {
				String message = "Error serializing menus for user";
				logger.error(message, e);
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
			}

			return Response.ok(documentsObjectNode).build();

		} catch (EMFUserError e) {
			String message = "No current user existing.";
			logger.error(message, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
		}

	}

}