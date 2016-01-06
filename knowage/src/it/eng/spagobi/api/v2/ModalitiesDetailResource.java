package it.eng.spagobi.api.v2;

import java.net.URI;
import java.net.URLEncoder;
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
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/customChecks")
@ManageAuthorization
public class ModalitiesDetailResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.CONTSTRAINT_MANAGEMENT })
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getCustom() {
		ICheckDAO checksDao = null;
		List<Check> fullList = null;

		try {

			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			fullList = checksDao.loadCustomChecks();
			return Response.ok(fullList).build();
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	@GET
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONTSTRAINT_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getSingleCheck(@PathParam("id") Integer id) {
		ICheckDAO checksDao = null;

		try {
			Check check = new Check();
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			check = checksDao.loadCheckByID(id);
			return Response.ok(check).build();

		} catch (Exception e) {
			logger.error("Check with selected id: " + id + " doesn't exists", e);
			throw new SpagoBIRestServiceException("Item with selected id: " + id + " doesn't exists", buildLocaleFromSession(), e);
		}

	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.CONTSTRAINT_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertCheck(@Valid Check body) {

		ICheckDAO checksDao = null;
		Check check = body;
		if (check.getCheckId() != null) {
			logger.error("Error paramters. New check should not have ID value");
			throw new SpagoBIRuntimeException("Error paramters. New check should not have ID value");
		}

		try {
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			Integer id = checksDao.insertCheck(check);
			String encodedCheck = URLEncoder.encode("" + id, "UTF-8");
			return Response.created(new URI("2.0/customChecks/" + encodedCheck)).entity(encodedCheck).build();
		} catch (Exception e) {
			logger.error("Error while inserting resource", e);
			throw new SpagoBIRestServiceException("Error while inserting resource", buildLocaleFromSession(), e);
		}
	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONTSTRAINT_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateCheck(@PathParam("id") Integer id, @Valid Check body) {

		ICheckDAO checksDao = null;
		Check check = body;
		if (check.getCheckId() == null) {
			logger.error("The check with ID " + id + " doesn't exist");
			throw new SpagoBIRuntimeException("The check with ID " + id + " doesn't exist");
		}

		try {
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			checksDao.modifyCheck(check);
			String encodedCheck = URLEncoder.encode("" + check.getCheckId(), "UTF-8");
			return Response.created(new URI("2.0/customChecks/" + encodedCheck)).entity(encodedCheck).build();
		} catch (Exception e) {
			logger.error("Error while modifying resource with id: " + id, e);
			throw new SpagoBIRestServiceException("Error while modifying resource with id: " + id, buildLocaleFromSession(), e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONTSTRAINT_MANAGEMENT })
	public Response deleteCheck(@PathParam("id") Integer id) {

		ICheckDAO checksDao = null;

		try {
			Check check = new Check();
			check.setCheckId(id);
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			checksDao.eraseCheck(check);
			String encodedCheck = URLEncoder.encode("" + check.getCheckId(), "UTF-8");
			return Response.ok().entity(encodedCheck).build();
		} catch (Exception e) {
			logger.error("Error with deleting resource with id: " + id, e);
			throw new SpagoBIRestServiceException("Error with deleting resource with id: " + id, buildLocaleFromSession(), e);
		}
	}
}
