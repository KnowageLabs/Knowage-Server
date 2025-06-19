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
package it.eng.spagobi.tools.dataset.service.federated;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.dataset.federation.FederationDefinitionAccessController;
import it.eng.spagobi.dataset.federation.exceptions.FederationDefinitionAccessException;
import it.eng.spagobi.federateddataset.dao.ISbiFederationDefinitionDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/federateddataset")
@ManageAuthorization
public class FederationDefinitionResource {

	protected static Logger logger = Logger.getLogger(FederationDefinitionResource.class);

	ISbiFederationDefinitionDAO fdsDAO;
	List<FederationDefinition> listOfFederations;

	@GET
	@Path("/")
	@UserConstraint(functionalities = {  })
	@Produces(MediaType.APPLICATION_JSON)
	public List<FederationDefinition> get() {
		try {
			fdsDAO = DAOFactory.getFedetatedDatasetDAO();
			listOfFederations = fdsDAO.loadAllFederatedDataSets();
			// needs serialization
			return listOfFederations;
		} catch (EMFUserError e) {
			logger.error("Error while loading federations", e);
			throw new SpagoBIRuntimeException("Error while loading federations", e);
		}
	}

	@GET
	@Path("/{id}")
	@UserConstraint(functionalities = {  })
	@Produces(MediaType.APPLICATION_JSON)
	public FederationDefinition getFederationByID(@PathParam("id") Integer id) {
		try {
			FederationDefinition federationDefinition;
			fdsDAO = DAOFactory.getFedetatedDatasetDAO();
			federationDefinition = fdsDAO.loadFederationDefinition(id);
			return federationDefinition;
		} catch (EMFUserError e) {
			logger.error("Error while getting federation by id", e);
			throw new SpagoBIRuntimeException("Error getting federation by id", e);
		}
	}

	@GET
	@Path("/dataset/{dsId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadFederationsByDataset(@PathParam("dsId") Integer dsId) {
		logger.debug("IN");
		List<FederationDefinition> toReturn = new ArrayList<>();
		try {
			fdsDAO = DAOFactory.getFedetatedDatasetDAO();
			toReturn = fdsDAO.loadFederationsUsingDataset(dsId);
			return Response.ok(toReturn).build();
		} catch (Exception e) {
			logger.error("Error while loading federation models by dataset", e);
			throw new SpagoBIRuntimeException("Error while loading federation models by dataset", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { })
	public Integer update(@PathParam("id") Integer id, FederationDefinition fds) {
		logger.debug("IN");
		try {
			fdsDAO = DAOFactory.getFedetatedDatasetDAO();
			fdsDAO.modifyFederation(fds);
			return fds.getFederation_id();
		} catch (Exception e) {
			logger.error("Error while updating federation", e);
			throw new SpagoBIRuntimeException("Error while updating federation", e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = {  })
	public Response remove(@PathParam("id") Integer id) {

		try {
			UserProfile userProfile = UserProfileManager.getProfile();
			FederationDefinitionAccessController fdaController = new FederationDefinitionAccessController(userProfile);
			fdaController.delete(id);

			return Response.ok().build();
		} catch (FederationDefinitionAccessException e) {
			logger.error("User doesn't have permission to delete resource", e);
			throw new SpagoBIRuntimeException("User doesn't have permission to delete resource", e);
		} catch (Exception e) {
			logger.error("Error while deleting resource", e);
			throw new SpagoBIRuntimeException("Error deleting federation", e);
		}
	}

}
