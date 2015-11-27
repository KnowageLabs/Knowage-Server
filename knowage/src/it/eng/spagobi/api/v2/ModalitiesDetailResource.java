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
import javax.ws.rs.core.Response.Status;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/detailmodalities")
@ManageAuthorization
public class ModalitiesDetailResource extends AbstractSpagoBIResource {

	@GET
	// @UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<Check> getPredefined() {
		ICheckDAO checksDao = null;
		List<Check> fullList = null;

		try {

			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			fullList = checksDao.loadCustomChecks();
			if (fullList != null && !fullList.isEmpty()) {
				System.out.println(fullList);
				return fullList;
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		return new ArrayList<Check>();
	}

	@GET
	@Path("/{id}")
	// @UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Check getSingleCheck(@PathParam("id") Integer id) {
		ICheckDAO checksDao = null;
		List<Check> fullList = null;

		try {
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			fullList = checksDao.loadAllChecks();

			if (fullList != null && !fullList.isEmpty()) {
				for (Check c : fullList) {
					if (c.getCheckId() == id) {
						return c;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while getting domain " + id, e);
			throw new SpagoBIRuntimeException("Error while getting domain " + id, e);
		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	@POST
	@Path("/")
	// @UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Consumes("application/json")
	public Response insertCheck(@Valid Check body) {

		ICheckDAO checksDao = null;
		Check check = body;
		System.out.println(check);
		if (check == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (check.getCheckId() != null) {
			return Response.status(Status.BAD_REQUEST).entity("Error paramters. New check should not have ID value").build();
		}

		try {
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			checksDao.insertCheck(check);
			String encodedCheck = URLEncoder.encode("" + check.getCheckId(), "UTF-8");
			return Response.created(new URI("2.0/detailmodalities/" + encodedCheck)).build();
		} catch (Exception e) {
			Response.notModified().build();
			logger.error("Error while creating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while creating url of the new resource", e);
		}
	}

	@PUT
	@Path("/{id}")
	// @UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDomain(@PathParam("id") Integer id, @Valid Check body) {

		ICheckDAO checksDao = null;
		Check check = body;

		if (check == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (check.getCheckId() == null) {
			return Response.status(Status.NOT_FOUND).entity("The check with ID " + id + " doesn't exist").build();
		}

		try {
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			List<Check> ChecksList = checksDao.loadAllChecks();
			checksDao.modifyCheck(check);
			String encodedCheck = URLEncoder.encode("" + check.getCheckId(), "UTF-8");
			return Response.created(new URI("2.0/detailmodalities/" + encodedCheck)).entity(encodedCheck).build();
		} catch (Exception e) {
			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while updating url of the new resource", e);
		}
	}

	@DELETE
	@Path("/{id}")
	// @UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public Response deleteCheck(@PathParam("id") Integer id) {

		ICheckDAO checksDao = null;

		try {
			Check check = new Check();
			check.setCheckId(id);
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			checksDao.eraseCheck(check);
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);
		}
	}
}
