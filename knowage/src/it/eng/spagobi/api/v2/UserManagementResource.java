package it.eng.spagobi.api.v2;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bo.UserBO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/2.0/users")
@ManageAuthorization
public class UserManagementResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public List<UserBO> getUserList() {
		ISbiUserDAO usersDao = null;
		List<UserBO> fullList = null;

		try {

			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			fullList = usersDao.loadUsers();
			return fullList;
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("sbi.modalities.check.rest.error", buildLocaleFromSession(), e);
		}

	}

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public UserBO getUserById(@PathParam("id") Integer id) {
		ISbiUserDAO usersDao = null;
		List<UserBO> fullList = null;
		try {

			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			fullList = usersDao.loadUsers();
			for (int i = 0; i < fullList.size(); i++) {
				if (fullList.get(i).getId() == id.intValue()) {
					return fullList.get(i);
				}
			}
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("sbi.modalities.check.rest.error", buildLocaleFromSession(), e);
		}
		return new UserBO();

	}

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Path("/roles")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public List<Role> getUserRoles() {
		IRoleDAO rolesDao = null;
		List<Role> fullList = null;

		try {

			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(getUserProfile());
			fullList = rolesDao.loadAllRoles();
			return fullList;
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("sbi.modalities.check.rest.error", buildLocaleFromSession(), e);
		}

	}

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Path("/{id}/roles")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public List<SbiExtRoles> getRolesByUserId(@PathParam("id") Integer id) {
		try {

			DAOFactory.getSbiUserDAO().setUserProfile(getUserProfile());
			SbiUser user = DAOFactory.getSbiUserDAO().loadSbiUserById(id);
			ArrayList<SbiExtRoles> roles = DAOFactory.getSbiUserDAO().loadSbiUserRolesById(user.getId());
			return roles;
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("sbi.modalities.check.rest.error", buildLocaleFromSession(), e);
		}
	}

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Path("/{id}/attributes")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public List<SbiUserAttributes> getAttributesByUserId(@PathParam("id") Integer id) {

		try {
			DAOFactory.getSbiUserDAO().setUserProfile(getUserProfile());
			SbiUser user = DAOFactory.getSbiUserDAO().loadSbiUserById(id);
			ArrayList<SbiUserAttributes> attributes = DAOFactory.getSbiUserDAO().loadSbiUserAttributesById(user.getId());
			return attributes;
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("sbi.modalities.check.rest.error", buildLocaleFromSession(), e);
		}
	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertUser(@Valid SbiUser body) {

		ISbiUserDAO usersDao = null;
		SbiUser user = body;
		try {
			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			Integer id = usersDao.fullSaveOrUpdateSbiUser(user);
			String encodedUser = URLEncoder.encode("" + id, "UTF-8");
			return Response.created(new URI("2.0/users/" + encodedUser)).entity(encodedUser).build();
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("sbi.modalities.check.rest.error", buildLocaleFromSession(), e);
		}
	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(@PathParam("id") Integer id, @Valid SbiUser body) {

		ISbiUserDAO usersDao = null;
		SbiUser user = body;

		try {
			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			usersDao.fullSaveOrUpdateSbiUser(user);
			String encodedUser = URLEncoder.encode("" + user.getId(), "UTF-8");
			return Response.created(new URI("2.0/customChecks/" + encodedUser)).entity(encodedUser).build();
		} catch (Exception e) {
			logger.error("Error with loading resource" + id, e);
			throw new SpagoBIRestServiceException("sbi.modalities.check.rest.error" + "with id: " + id, buildLocaleFromSession(), e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	public Response deleteCheck(@PathParam("id") Integer id) {

		ISbiUserDAO usersDao = null;

		try {
			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			usersDao.deleteSbiUserById(id);
			String encodedUser = URLEncoder.encode("" + id, "UTF-8");
			return Response.ok().entity(encodedUser).build();
		} catch (Exception e) {
			logger.error("Error with loading resource" + id, e);
			throw new SpagoBIRestServiceException("sbi.modalities.check.rest.error" + "with id: " + id, buildLocaleFromSession(), e);
		}
	}

}
