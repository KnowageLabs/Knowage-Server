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

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.dao.QueryFilters;
import it.eng.spagobi.profiling.PublicProfile;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.profiling.bo.UserBO;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.profiling.dao.SbiUserDAOHibImpl;
import it.eng.spagobi.profiling.dao.filters.FinalUsersFilter;
import it.eng.spagobi.security.Password;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/2.0/users")
@ManageAuthorization
public class UserResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT, SpagoBIConstants.FINAL_USERS_MANAGEMENT })
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getUserList(@QueryParam("dateFilter") String dateFilter) {
		ISbiUserDAO usersDao = null;
		List<UserBO> fullList = null;
		List<SbiAttribute> attrList = null;
		ISbiAttributeDAO objDao = null;
		ArrayList<Integer> hiddenAttributesIds = new ArrayList<>();
		ProfileAttributeResourceRoleProcessor roleFilter = new ProfileAttributeResourceRoleProcessor();
		try {
			IEngUserProfile profile = getUserProfile();
			QueryFilters qp = new QueryFilters();
			objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(getUserProfile());
			attrList = objDao.loadSbiAttributes();
			if (profile.isAbleToExecuteAction(SpagoBIConstants.PROFILE_MANAGEMENT)) {
				// administrator: he can see every user
			} else {
				// user with FINAL_USERS_MANAGEMENT (users with neither
				// FINAL_USERS_MANAGEMENT nor PROFILE_MANAGEMENT are blocked by the
				// business_map.xml therefore they cannot execute this action)
				qp.add(new FinalUsersFilter());
			}

			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());

			if (dateFilter != null) {
				fullList = usersDao.loadUsers(qp, dateFilter);
			} else {
				fullList = usersDao.loadUsers(qp);
			}

			for (UserBO user : fullList) {
				if (!UserUtilities.isTechnicalUser(getUserProfile())) {
					hiddenAttributesIds = roleFilter.getHiddenAttributesIds();
					roleFilter.removeHiddenAttributes(hiddenAttributesIds, user);
				}

			}

			return Response.ok(fullList).build();
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}
	}

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT, SpagoBIConstants.FINAL_USERS_MANAGEMENT })
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getUserById(@PathParam("id") Integer id) {
		ISbiUserDAO usersDao = null;
		SbiUserDAOHibImpl hib = new SbiUserDAOHibImpl();
		List<SbiAttribute> attrList = null;
		ISbiAttributeDAO objDao = null;
		ArrayList<Integer> hiddenAttributesIds = new ArrayList<>();
		ProfileAttributeResourceRoleProcessor roleFilter = new ProfileAttributeResourceRoleProcessor();
		try {

			SbiUser sbiUser = new SbiUser();
			UserBO user = new UserBO();
			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			sbiUser = usersDao.loadSbiUserById(id);
			user = hib.toUserBO(sbiUser);

			objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(getUserProfile());
			attrList = objDao.loadSbiAttributes();

			if (!UserUtilities.isTechnicalUser(getUserProfile())) {
				hiddenAttributesIds = roleFilter.getHiddenAttributesIds();
				roleFilter.removeHiddenAttributes(hiddenAttributesIds, user);
			}
			return Response.ok(user).build();
		} catch (Exception e) {
			logger.error("User with selected id: " + id + " doesn't exists", e);
			throw new SpagoBIRestServiceException("Item with selected id: " + id + " doesn't exists", buildLocaleFromSession(), e);
		}
	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT, SpagoBIConstants.FINAL_USERS_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertUser(@Valid UserBO requestDTO) {
		ISbiUserDAO usersDao = null;

		String userId = requestDTO.getUserId();
		if (userId.startsWith(PublicProfile.PUBLIC_USER_PREFIX)) {
			logger.error("public is reserved prefix for user id");
			throw new SpagoBIServiceException("SPAGOBI_SERVICE", "public_ is a reserved prefix for user name", null);
		}

		try {
			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			SbiUser existingUser = usersDao.loadSbiUserByUserId(userId);
			if (existingUser != null && userId.equals(existingUser.getUserId())) {
				logger.error("User already exists. User_ID is unique");
				throw new SpagoBIRestServiceException("User with provided ID already exists.", buildLocaleFromSession(), new Throwable());
			}
		} catch (SpagoBIRestServiceException ex) {
			throw ex;
		}

		SbiUser sbiUser = new SbiUser();
		sbiUser.setUserId(requestDTO.getUserId());
		sbiUser.setFullName(requestDTO.getFullName());
		sbiUser.setPassword(requestDTO.getPassword());
		sbiUser.setDefaultRoleId(requestDTO.getDefaultRoleId());

		List<Integer> list = requestDTO.getSbiExtUserRoleses();
		Set<SbiExtRoles> roles = new HashSet<SbiExtRoles>(0);
		for (Integer id : list) {
			SbiExtRoles role = new SbiExtRoles();
			role.setExtRoleId(id);
			roles.add(role);
		}
		sbiUser.setSbiExtUserRoleses(roles);

		HashMap<Integer, HashMap<String, String>> map = requestDTO.getSbiUserAttributeses();
		Set<SbiUserAttributes> attributes = new HashSet<SbiUserAttributes>(0);

		for (Entry<Integer, HashMap<String, String>> entry : map.entrySet()) {
			SbiUserAttributes attribute = new SbiUserAttributes();
			SbiUserAttributesId attid = new SbiUserAttributesId();
			attid.setAttributeId(entry.getKey());
			attribute.setId(attid);
			for (Entry<String, String> value : entry.getValue().entrySet()) {

				attribute.setAttributeValue(value.getValue());

			}
			attributes.add(attribute);
		}
		sbiUser.setSbiUserAttributeses(attributes);

		String password = sbiUser.getPassword();
		if (password != null && password.length() > 0) {
			try {
				sbiUser.setPassword(Password.encriptPassword(password));
			} catch (Exception e) {
				logger.error("Impossible to encrypt Password", e);
				throw new SpagoBIServiceException("SPAGOBI_SERVICE", "Impossible to encrypt Password", e);
			}
		}

		try {
			Integer id = usersDao.fullSaveOrUpdateSbiUser(sbiUser);
			String encodedUser = URLEncoder.encode("" + id, "UTF-8");
			return Response.created(new URI("2.0/users/" + encodedUser)).entity(encodedUser).build();
		} catch (Exception e) {
			logger.error("Error while inserting resource", e);
			throw new SpagoBIRestServiceException("Error while inserting resource", buildLocaleFromSession(), e);
		}
	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT, SpagoBIConstants.FINAL_USERS_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(@PathParam("id") Integer id, @Valid UserBO requestDTO) {

		SbiUser sbiUserOriginal = new SbiUser();
		ISbiUserDAO usersDao = null;
		ProfileAttributeResourceRoleProcessor roleFilter = new ProfileAttributeResourceRoleProcessor();

		String userId = requestDTO.getUserId();
		if (userId.startsWith(PublicProfile.PUBLIC_USER_PREFIX)) {
			logger.error("public is reserved prefix for user id");
			throw new SpagoBIServiceException("SPAGOBI_SERVICE", "public_ is a reserved prefix for user name", null);
		}

		SbiUser sbiUser = new SbiUser();
		sbiUser.setId(id);
		sbiUser.setUserId(requestDTO.getUserId());
		sbiUser.setFullName(requestDTO.getFullName());
		sbiUser.setPassword(requestDTO.getPassword());
		sbiUser.setDefaultRoleId(requestDTO.getDefaultRoleId());
		sbiUser.setFailedLoginAttempts(requestDTO.getFailedLoginAttempts());
		// This reset the account lock enabled in case of too much failed login attempts
//		sbiUser.setFailedLoginAttempts(0);

		List<Integer> list = requestDTO.getSbiExtUserRoleses();
		Set<SbiExtRoles> roles = new HashSet<SbiExtRoles>(0);
		for (Integer i : list) {
			SbiExtRoles role = new SbiExtRoles();
			role.setExtRoleId(i);
			roles.add(role);
		}
		sbiUser.setSbiExtUserRoleses(roles);

		HashMap<Integer, HashMap<String, String>> map = requestDTO.getSbiUserAttributeses();
		Set<SbiUserAttributes> attributes = new HashSet<SbiUserAttributes>(0);
		List<SbiAttribute> attrList = null;
		ISbiAttributeDAO objDao = null;

		try {
			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			sbiUserOriginal = usersDao.loadSbiUserById(sbiUser.getId());
			objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(getUserProfile());
			attrList = objDao.loadSbiAttributes();
		} catch (EMFUserError e1) {
			logger.error("Impossible get attributes", e1);
		}

		for (Entry<Integer, HashMap<String, String>> entry : map.entrySet()) {
			SbiUserAttributes attribute = new SbiUserAttributes();
			SbiUserAttributesId attid = new SbiUserAttributesId();
			attid.setAttributeId(entry.getKey());
			attribute.setId(attid);
			for (Entry<String, String> value : entry.getValue().entrySet()) {

				attribute.setAttributeValue(value.getValue());

			}
			attributes.add(attribute);
		}

		// This method get hidden attributes from user and sets their value to last known in DB
		// By this we are avoiding changing that value if user change some other attributes
		try {
			if (objDao.getUserProfile().getRoles().size() == 1 && objDao.getUserProfile().getRoles().toArray()[0].equals("user"))
				roleFilter.setAttributeHiddenFromUser(sbiUserOriginal, attributes, attrList);
		} catch (EMFInternalError e1) {
			logger.error(e1.getMessage(), e1);
		}

		sbiUser.setSbiUserAttributeses(attributes);

		String password = sbiUser.getPassword();
		if (password != null && password.length() > 0) {
			try {
				sbiUser.setPassword(Password.encriptPassword(password));
			} catch (Exception e) {
				logger.error("Impossible to encrypt Password", e);
				throw new SpagoBIServiceException("SPAGOBI_SERVICE", "Impossible to encrypt Password", e);
			}
		} else {
			sbiUser.setPassword(null);
		}

		try {
			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			Integer idToReturn = usersDao.fullSaveOrUpdateSbiUser(sbiUser);
			String encodedUser = URLEncoder.encode("" + idToReturn, "UTF-8");
			return Response.created(new URI("2.0/users/" + encodedUser)).entity(encodedUser).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRestServiceException(e.getMessage(), buildLocaleFromSession(), e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT, SpagoBIConstants.FINAL_USERS_MANAGEMENT })
	public Response deleteCheck(@PathParam("id") Integer id) {

		ISbiUserDAO usersDao = null;

		try {
			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			usersDao.deleteSbiUserById(id);
			String encodedUser = URLEncoder.encode("" + id, "UTF-8");
			return Response.ok().entity(encodedUser).build();
		} catch (Exception e) {
			logger.error("Error with deleting resource with id: " + id, e);
			throw new SpagoBIRestServiceException("Error with deleting resource with id: " + id, buildLocaleFromSession(), e);
		}
	}

}
