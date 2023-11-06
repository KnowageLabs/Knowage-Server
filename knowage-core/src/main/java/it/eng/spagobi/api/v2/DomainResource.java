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

import static java.nio.charset.StandardCharsets.UTF_8;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/domains")
@ManageAuthorization
public class DomainResource extends AbstractSpagoBIResource {

	// logger component-
	private static Logger logger = Logger.getLogger(DomainResource.class);

	@GET
	@Path("/")
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

		return new ArrayList<>();
	}

	@GET
	@Path("/byid/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Domain getSingleDomain(@PathParam("id") Integer id) {
		logger.debug("IN");
		IDomainDAO domainsDao = null;
		Domain dom;

		try {
			domainsDao = DAOFactory.getDomainDAO();
			domainsDao.setUserProfile(getUserProfile());
			dom = domainsDao.loadDomainById(id);
		} catch (Exception e) {
			logger.error("Error while getting domain " + id, e);
			throw new SpagoBIRuntimeException("Error while getting domain " + id, e);
		} finally {
			logger.debug("OUT");
		}
		return dom;
	}

	@POST
	@Path("/")
	@Consumes("application/json")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.DOMAIN_WRITE })
	public Response insertDomain(@Valid Domain body) {

		IDomainDAO domainsDao = null;
		Domain domain = body;
		if (domain == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (domain.getValueId() != null) {
			return Response.status(Status.BAD_REQUEST).entity("Error paramters. New domain should not have ID value")
					.build();
		}

		try {
			domainsDao = DAOFactory.getDomainDAO();
			domainsDao.setUserProfile(getUserProfile());
			List<Domain> domainsList = domainsDao.loadListDomains();
			domainsDao.saveDomain(domain);
			String encodedDomain = URLEncoder.encode("" + domain.getValueId(), UTF_8.name());
			return Response.created(new URI("1.0/domains/" + encodedDomain)).entity(encodedDomain).build();
		} catch (Exception e) {
			Response.notModified().build();
			logger.error("Error while creating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while creating url of the new resource", e);
		}
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.DOMAIN_WRITE })
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
			String encodedDomain = URLEncoder.encode("" + domain.getValueId(), UTF_8.name());
			return Response.created(new URI("1.0/domains/" + encodedDomain)).entity(encodedDomain).build();
		} catch (Exception e) {
			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while updating url of the new resource", e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.DOMAIN_WRITE })
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

	@GET
	@Path("/listByCode/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Domain> getDomainsByCode(@PathParam("code") String code) {
		logger.debug("IN");
		IDomainDAO domainsDao = null;
		List<Domain> dom;

		try {
			domainsDao = DAOFactory.getDomainDAO();
			domainsDao.setUserProfile(getUserProfile());
			dom = domainsDao.loadListDomainsByType(code);
		} catch (Exception e) {
			logger.error("Error while getting domain " + code, e);
			throw new SpagoBIRuntimeException("Error while getting domain " + code, e);
		} finally {
			logger.debug("OUT");
		}
		return dom;
	}

	@GET
	@Path("/rolesCategories")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<Domain> getCategoriesOfRoles(@QueryParam("id") List<Integer> ids) {
		logger.debug("IN");
		List<Domain> rolesMetaModelCategories = new ArrayList<>();
		try {
			List<Integer> allRolesCategories = new ArrayList<>();

			IDomainDAO domainsDao = DAOFactory.getDomainDAO();
			ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();

			domainsDao.setUserProfile(getUserProfile());
			for (int i = 0; i < ids.size(); i++) {
				List<Integer> tempL = domainsDao.loadListMetaModelDomainsByRole(ids.get(i));
				for (int j = 0; j < tempL.size(); j++) {
					if (!allRolesCategories.contains(tempL.get(j))) {
						allRolesCategories.add(tempL.get(j));
					}
				}
			}

			List<SbiCategory> metaModelCategories = categoryDao.getCategoriesForBusinessModel();
			for (int i = 0; i < allRolesCategories.size(); i++) {
				for (int j = 0; j < metaModelCategories.size(); j++) {
					SbiCategory sbiCategory = metaModelCategories.get(j);
					if (allRolesCategories.contains(sbiCategory.getId())) {
						Domain domain = Domain.fromCategory(sbiCategory);
						rolesMetaModelCategories.add(domain);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while getting domain " + ids, e);
			throw new SpagoBIRuntimeException("Error while getting domain " + ids, e);
		} finally {
			logger.debug("OUT");
		}
		return rolesMetaModelCategories;
	}
}
