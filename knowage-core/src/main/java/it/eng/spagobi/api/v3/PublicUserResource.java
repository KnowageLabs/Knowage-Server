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

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.profiling.PublicProfile;
import it.eng.spagobi.profiling.bo.UserInformationDTO;
import it.eng.spagobi.services.rest.annotations.PublicService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("3.0/public-user")

public class PublicUserResource {

	private static final Logger LOGGER = LogManager.getLogger(PublicUserResource.class);

	@Context
	private HttpServletRequest httpRequest;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@PublicService
	public Response getPublicUserProfile(@QueryParam("organization") String organization) {
		LOGGER.debug("IN; organization = [{}]", organization);

		UserInformationDTO toReturn = null;

		try {
			// if public user is disabled globally, return BAD_REQUEST (400)
			if (publicUserIsDisabled()) {
				LOGGER.error("Cannot create public user: public user functionality is not enabled");
				return Response.status(Response.Status.BAD_REQUEST).build();
			}

			// if organization is not specified, return BAD_REQUEST (400)
			if (StringUtils.isEmpty(organization)) {
				LOGGER.error("Cannot create public user: missing 'organization' parameter");
				return Response.status(Response.Status.BAD_REQUEST).build();
			}

			// if organization is not valid, return BAD_REQUEST (400)
			Optional<SbiTenant> tenant = getTenant(organization);
			if (!tenant.isPresent()) {
				LOGGER.error("Cannot create public user: tenant [{}] not found", organization);
				return Response.status(Response.Status.BAD_REQUEST).build();
			}

			// if organization has no public role, return BAD_REQUEST (400)
			Optional<Role> publicRole = getPublicRoleIfExistsInTenant(organization);
			if (!publicRole.isPresent()) {
				LOGGER.error("Cannot create public user: tenant [{}] has no public role", organization);
				return Response.status(Response.Status.BAD_REQUEST).build();
			}

			Role role = publicRole.get();
			// public role MUST be final user type; other role types are not permitted
			if (!role.getRoleTypeCD().equalsIgnoreCase(SpagoBIConstants.ROLE_TYPE_USER)) {
				LOGGER.error("Cannot create public user: public role [{}] in tenant [{}] is not regular user type!!! This is not allowed!!", role.getName(),
						role.getOrganization());
				return Response.status(Response.Status.BAD_REQUEST).build();
			}

			String publicUserId = PublicProfile.createPublicUserId(organization);
			LOGGER.debug("Public user id is [{}]", publicUserId);

			HttpSession session = httpRequest.getSession(false);
			if (session != null) {
				UserProfile existingUserProfile = (UserProfile) session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
				if (existingUserProfile != null) {
					if (existingUserProfile.getUserId().equals(publicUserId)) {
						// if public user profile object already exists, return it
						return Response.ok(new UserInformationDTO(existingUserProfile)).build();
					} else {
						// if a different user profile object already exists, return BAD_REQUEST (400)
						LOGGER.error(
								"Cannot create public user [{}]: an user profile object already exists in session with user id = [{}] beloning to tenant [{}].",
								publicUserId, existingUserProfile.getUserId(), existingUserProfile.getOrganization());
						return Response.status(Response.Status.BAD_REQUEST).build();
					}
				}
			}

			// create public user profile object and store it in session
			SpagoBIUserProfile spagoBIProfile = PublicProfile.createPublicUserProfile(publicUserId);
			UserProfile userProfile = new UserProfile(spagoBIProfile);
			storeProfileInSession(userProfile);

			toReturn = new UserInformationDTO(userProfile);
			LOGGER.info("Created public user profile [{}]", toReturn);
			return Response.ok(toReturn).build();

		} finally {
			LOGGER.debug("OUT: returning public user profile [{}]", toReturn);
		}

	}

	protected Optional<Role> getPublicRoleIfExistsInTenant(String organization) {
		try {
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			roleDAO.setTenant(organization);
			return Optional.ofNullable(roleDAO.loadPublicRole());
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occurred while loading public role", e);
		}
	}

	protected Optional<SbiTenant> getTenant(String organization) {
		try {
			return Optional.ofNullable(DAOFactory.getTenantsDAO().loadTenantByName(organization));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occurred while loading tenant", e);
		}
	}

	private boolean publicUserIsDisabled() {
		try {
			IConfigDAO configDAO = DAOFactory.getSbiConfigDAO();
			Optional<Config> usePublicUserConfig = configDAO.loadConfigParametersByLabelIfExist(SpagoBIConstants.USE_PUBLIC_USER);
			return !usePublicUserConfig.isPresent() || !Boolean.parseBoolean(usePublicUserConfig.get().getValueCheck());
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occurred while getting configuration about public user functionality", e);
		}
	}

	private void storeProfileInSession(UserProfile userProfile) {
		try {
			HttpSession session = httpRequest.getSession(true);

			RequestContainer requestContainer = (RequestContainer) session.getAttribute(Constants.REQUEST_CONTAINER);
			if (requestContainer == null) {
				requestContainer = new RequestContainer();
				SessionContainer sessionContainer = new SessionContainer(true);
				requestContainer.setSessionContainer(sessionContainer);
				session.setAttribute(Constants.REQUEST_CONTAINER, requestContainer);
			}
			ResponseContainer responseContainer = (ResponseContainer) session.getAttribute(Constants.RESPONSE_CONTAINER);
			if (responseContainer == null) {
				responseContainer = new ResponseContainer();
				SourceBean serviceResponse = new SourceBean(Constants.SERVICE_RESPONSE);
				responseContainer.setServiceResponse(serviceResponse);
				session.setAttribute(Constants.RESPONSE_CONTAINER, responseContainer);
			}
			SessionContainer sessionContainer = requestContainer.getSessionContainer();
			SessionContainer permanentSession = sessionContainer.getPermanentContainer();

			permanentSession.setAttribute(IEngUserProfile.ENG_USER_PROFILE, userProfile);
			session.setAttribute(IEngUserProfile.ENG_USER_PROFILE, userProfile);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot set user profile object in session", e);
		}
	}

}