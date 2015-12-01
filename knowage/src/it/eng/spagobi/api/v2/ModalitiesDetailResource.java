package it.eng.spagobi.api.v2;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/detailmodalities")
@ManageAuthorization
public class ModalitiesDetailResource extends AbstractSpagoBIResource {

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.CONTSTRAINT_MANAGEMENT })
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
				return fullList;
			}
		} catch (Exception e) {
			logger.error("Error while getting constraints ", e);
		}

		return fullList;
	}

	@GET
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONTSTRAINT_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Check getSingleCheck(@PathParam("id") Integer id) {
		ICheckDAO checksDao = null;
		List<Check> fullList = null;

		try {
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			fullList = checksDao.loadCustomChecks();
			if (fullList != null && !fullList.isEmpty()) {
				for (int i = 0; i < fullList.size(); i++) {
					if (fullList.get(i).getCheckId() == id.intValue()) {
						return fullList.get(i);
					}

				}
			}
		} catch (Exception e) {
			logger.error("Error while getting constraints " + id, e);
			throw new SpagoBIRuntimeException("Error while getting constraints " + id, e);
		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.CONTSTRAINT_MANAGEMENT })
	@Consumes("application/json")
	public List<Check> insertCheck(@Valid Check body) {

		ICheckDAO checksDao = null;
		Check check = body;
		if (check == null) {
			logger.error("Error JSON parsing");
		}

		if (check.getCheckId() != null) {
			logger.error("Error paramters. New check should not have ID value");
		}

		try {
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			checksDao.insertCheck(check);
			String encodedCheck = URLEncoder.encode("" + check.getCheckId(), "UTF-8");
			return checksDao.loadCustomChecks();
		} catch (Exception e) {
			Response.notModified().build();
			logger.error("Error while creating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while creating url of the new resource", e);
		}
	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONTSTRAINT_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public List<Check> updateCheck(@PathParam("id") Integer id, @Valid Check body) {

		ICheckDAO checksDao = null;
		Check check = body;

		if (check == null) {
			logger.error("Error JSON parsing");
		}

		if (check.getCheckId() == null) {
			logger.error("The check with ID " + id + " doesn't exist");
		}

		try {
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			checksDao.modifyCheck(check);
			String encodedCheck = URLEncoder.encode("" + check.getCheckId(), "UTF-8");
			return checksDao.loadCustomChecks();
		} catch (Exception e) {
			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while updating url of the new resource", e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONTSTRAINT_MANAGEMENT })
	public List<Check> deleteCheck(@PathParam("id") Integer id) {

		ICheckDAO checksDao = null;

		try {
			Check check = new Check();
			check.setCheckId(id);
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			checksDao.eraseCheck(check);
			return checksDao.loadCustomChecks();
		} catch (Exception e) {
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);
		}
	}

	@DELETE
	@Path("/")
	public List<Check> deleteMultiple(@QueryParam("id") int[] ids) {

		ICheckDAO checksDao = null;
		try {

			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			for (int i = 0; i < ids.length; i++) {
				Check check = new Check();
				check.setCheckId(ids[i]);
				checksDao.eraseCheck(check);
			}
			return checksDao.loadCustomChecks();
		} catch (Exception e) {
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);
		}
	}
}
