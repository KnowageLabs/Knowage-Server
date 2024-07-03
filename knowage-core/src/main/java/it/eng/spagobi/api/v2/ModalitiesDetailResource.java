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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.owasp.esapi.reference.DefaultEncoder; 


@Path("/2.0/customChecks")
@ManageAuthorization
public class ModalitiesDetailResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(ModalitiesDetailResource.class);
	private static final String CHARSET = "; charset=UTF-8";
	private static org.owasp.esapi.Encoder esapiEncoder = DefaultEncoder.getInstance();
	@GET
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CONTSTRAINT_MANAGEMENT })
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + CHARSET)
	public Response getCustom() {
		ICheckDAO checksDao = null;
		List<Check> fullList = null;

		try {

			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			fullList = checksDao.loadCustomChecks();
			return Response.ok(fullList).build();
		} catch (Exception e) {
			LOGGER.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	@GET
	@Path("/{id}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CONTSTRAINT_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + CHARSET)
	public Response getSingleCheck(@PathParam("id") Integer id) {
		ICheckDAO checksDao = null;

		try {
			Check check = new Check();
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			check = checksDao.loadCheckByID(id);
			return Response.ok(check).build();

		} catch (Exception e) {
			LOGGER.error("Check with selected id: " + id + " doesn't exists", e);
			throw new SpagoBIRestServiceException("Item with selected id: " + id + " doesn't exists", buildLocaleFromSession(), e);
		}

	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CONTSTRAINT_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertCheck(@Valid Check body) {

		ICheckDAO checksDao = null;
		Check check = body;
		if (check.getCheckId() != null) {
			LOGGER.error("Error paramters. New check should not have ID value");
			throw new SpagoBIRuntimeException("Error paramters. New check should not have ID value");
		}

		try {
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			Integer id = checksDao.insertCheck(check);
			String encodedCheck = esapiEncoder.encodeForURL("" + id);
			return Response.created(new URI("2.0/customChecks/" + encodedCheck)).entity(encodedCheck).build();
		} catch (Exception e) {
			LOGGER.error("Error while inserting resource", e);
			throw new SpagoBIRestServiceException("Error while inserting resource", buildLocaleFromSession(), e);
		}
	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CONTSTRAINT_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateCheck(@PathParam("id") Integer id, @Valid Check body) {

		ICheckDAO checksDao = null;
		Check check = body;
		if (check.getCheckId() == null) {
			LOGGER.error("The check with ID " + id + " doesn't exist");
			throw new SpagoBIRuntimeException("The check with ID " + id + " doesn't exist");
		}

		try {
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			checksDao.modifyCheck(check);
			String encodedCheck = esapiEncoder.encodeForURL("" + check.getCheckId());
			return Response.created(new URI("2.0/customChecks/" + encodedCheck)).entity(encodedCheck).build();
		} catch (Exception e) {
			LOGGER.error("Error while modifying resource with id: " + id, e);
			throw new SpagoBIRestServiceException("Error while modifying resource with id: " + id, buildLocaleFromSession(), e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CONTSTRAINT_MANAGEMENT })
	public Response deleteCheck(@PathParam("id") Integer id) {

		ICheckDAO checksDao = null;

		try {
			Check check = new Check();
			check.setCheckId(id);
			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			checksDao.eraseCheck(check);
			String encodedCheck = esapiEncoder.encodeForURL("" + check.getCheckId());
			return Response.ok().entity(encodedCheck).build();
		} catch (Exception e) {
			LOGGER.error("Error with deleting resource with id: " + id, e);
			throw new SpagoBIRestServiceException("Error with deleting resource with id: " + id, buildLocaleFromSession(), e);
		}
	}
}
