package it.eng.spagobi.tools.dataset.service.federated;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.federateddataset.dao.ISbiFederationDefinitionDAO;
import it.eng.spagobi.federateddataset.metadata.SbiFederationDefinition;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

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

@Path("/2.0/federateddataset")
@ManageAuthorization
public class FederationDefinitionResource {

	static protected Logger logger = Logger.getLogger(FederationDefinitionResource.class);

	ISbiFederationDefinitionDAO fdsDAO;
	List<FederationDefinition> listOfFederations;

	@GET
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.FEDERATED_DATASET_MANAGEMENT })
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

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FEDERATED_DATASET_MANAGEMENT })
	public Integer update(@PathParam("id") Integer id, FederationDefinition fds) {
		logger.debug("IN");
		try {
			fdsDAO = DAOFactory.getFedetatedDatasetDAO();
			SbiFederationDefinition sfds = new SbiFederationDefinition(fds.getFederation_id(), fds.getLabel(), fds.getName(), fds.getDescription(),
					fds.getRelationships());
			fdsDAO.modifyFederation(sfds);
			return sfds.getFederation_id();
		} catch (Exception e) {
			logger.error("Error while updating federation", e);
			throw new SpagoBIRuntimeException("Error while updating federation", e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.FEDERATED_DATASET_MANAGEMENT })
	public Response remove(@PathParam("id") Integer id) {
		try {
			fdsDAO = DAOFactory.getFedetatedDatasetDAO();
			fdsDAO.deleteFederatedDatasetById(id);
			return Response.ok().build();
		} catch (EMFUserError e) {
			logger.error("Error while deleting resource", e);
			throw new SpagoBIRuntimeException("Error deleting federation", e);
		}
	}

}
