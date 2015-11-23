package it.eng.spagobi.api.v2;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
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

@Path("/2.0/domains")
@ManageAuthorization
public class DomainResource extends AbstractSpagoBIResource {

	// logger component-
	private static Logger logger = Logger.getLogger(DomainResource.class);

	@GET
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public List<Domain> getDomains() {
		logger.debug("IN");
		IDomainDAO domainsDao = null;
		List<Domain> allObjects = null;

		try {
			domainsDao = DAOFactory.getDomainDAO();
			domainsDao.setUserProfile(getUserProfile());
			allObjects = domainsDao.loadListDomains();

			if (allObjects != null && !allObjects.isEmpty()) {
				return allObjects;
			}
		} catch (Exception e) {
			logger.error("Error while getting the list of domains", e);
			throw new SpagoBIRuntimeException("Error while getting the list of domains", e);
		} finally {
			logger.debug("OUT");
		}

		return new ArrayList<Domain>();
	}

	@GET
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Domain getSingleDomain(@PathParam("id") Integer id) {
		logger.debug("IN");
		IDomainDAO domainsDao = null;
		List<Domain> allObjects = null;

		try {
			domainsDao = DAOFactory.getDomainDAO();
			domainsDao.setUserProfile(getUserProfile());
			allObjects = domainsDao.loadListDomains();

			if (allObjects != null && !allObjects.isEmpty()) {
				for (Domain dm : allObjects) {
					if (dm.getValueId() == id) {
						return dm;
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
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Consumes("application/json")
	public Response insertDomain(@Valid Domain body) {

		IDomainDAO domainsDao = null;
		Domain domain = body;
		if (domain == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (domain.getValueId() != null) {
			return Response.status(Status.BAD_REQUEST).entity("Error paramters. New domain should not have ID value").build();
		}

		try {
			domainsDao = DAOFactory.getDomainDAO();
			domainsDao.setUserProfile(getUserProfile());
			List<Domain> domainsList = domainsDao.loadListDomains();
			domainsDao.saveDomain(domain);
			String encodedDomain = URLEncoder.encode("" + domain.getValueId(), "UTF-8");
			return Response.created(new URI("1.0/domains/" + encodedDomain)).build();
		} catch (Exception e) {
			Response.notModified().build();
			logger.error("Error while creating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while creating url of the new resource", e);
		}
	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDomain(@PathParam("id") Integer id, @Valid Domain body) {

		IDomainDAO domainsDao = null;
		Domain domain = body;

		if (domain == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (domain.getValueId() == null) {
			return Response.status(Status.NOT_FOUND).entity("The domain with ID " + id + " doesn't exist").build();
		}

		try {
			domainsDao = DAOFactory.getDomainDAO();
			domainsDao.setUserProfile(getUserProfile());
			List<Domain> domainsList = domainsDao.loadListDomains();
			domainsDao.saveDomain(domain);
			String encodedDomain = URLEncoder.encode("" + domain.getValueId(), "UTF-8");
			return Response.created(new URI("1.0/domains/" + encodedDomain)).entity(encodedDomain).build();
		} catch (Exception e) {
			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while updating url of the new resource", e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public Response deleteDomain(@PathParam("id") Integer id) {

		IDomainDAO domainsDao = null;

		try {
			domainsDao = DAOFactory.getDomainDAO();
			domainsDao.setUserProfile(getUserProfile());
			domainsDao.delete(id);
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);
		}
	}
}
