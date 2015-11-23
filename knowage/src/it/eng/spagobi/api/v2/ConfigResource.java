package it.eng.spagobi.api.v2;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

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
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

@Path("/2.0/configs")
@ManageAuthorization
public class ConfigResource extends AbstractSpagoBIResource {

	// logger component-
	private static Logger logger = Logger.getLogger(ConfigResource.class);

	@GET
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public List<Config> getConfigs() {
		logger.debug("IN");
		IConfigDAO configsDao = null;
		List<Config> allObjects = null;

		try {
			configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			allObjects = configsDao.loadAllConfigParameters();

			if (allObjects != null && !allObjects.isEmpty()) {
				return allObjects;
			}
		} catch (Exception e) {
			logger.error("Error while getting the list of configs", e);
			throw new SpagoBIRuntimeException("Error while getting the list of configs", e);
		} finally {
			logger.debug("OUT");
		}

		return new ArrayList<Config>();
	}

	@GET
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Config getSingleConfig(@PathParam("id") Integer id) {
		logger.debug("IN");
		IConfigDAO configsDao = null;
		List<Config> allObjects = null;

		try {
			configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			allObjects = configsDao.loadAllConfigParameters();

			if (allObjects != null && !allObjects.isEmpty()) {
				for (Config dm : allObjects) {
					if (dm.getId() == id) {
						return dm;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while getting config " + id, e);
			throw new SpagoBIRuntimeException("Error while getting config " + id, e);
		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Consumes("application/json")
	public Response insertConfig(@Valid Config body) {

		IConfigDAO configsDao = null;
		Config config = body;
		if (config == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (config.getId() != null) {
			return Response.status(Status.BAD_REQUEST).entity("Error paramters. New config should not have ID value").build();
		}

		try {
			configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			List<Config> configsList = configsDao.loadAllConfigParameters();
			configsDao.saveConfig(config);
			String encodedConfig = URLEncoder.encode("" + config.getId(), "UTF-8");
			return Response.created(new URI("1.0/configs/" + encodedConfig)).build();
		} catch (Exception e) {
			Response.notModified().build();
			logger.error("Error while creating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while creating url of the new resource", e);
		}
	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateConfig(@PathParam("id") Integer id, @Valid Config body) {

		IConfigDAO configsDao = null;
		Config config = body;

		if (config == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (config.getId() == null) {
			return Response.status(Status.NOT_FOUND).entity("The config with ID " + id + " doesn't exist").build();
		}

		try {
			configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			List<Config> configsList = configsDao.loadAllConfigParameters();
			configsDao.saveConfig(config);
			String encodedConfig = URLEncoder.encode("" + config.getId(), "UTF-8");
			return Response.created(new URI("1.0/configs/" + encodedConfig)).entity(encodedConfig).build();
		} catch (Exception e) {
			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while updating url of the new resource", e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	public Response deleteConfig(@PathParam("id") Integer id) {

		IConfigDAO configsDao = null;

		try {
			configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			configsDao.delete(id);
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);
		}
	}
}
