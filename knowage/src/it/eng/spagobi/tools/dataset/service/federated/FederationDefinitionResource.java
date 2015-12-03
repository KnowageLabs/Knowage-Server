package it.eng.spagobi.tools.dataset.service.federated;

import it.eng.qbe.dataset.FederatedDataSet;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.federateddataset.dao.ISbiFederationDefinitionDAO;
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

	ISbiFederationDefinitionDAO objDao;
	List<FederationDefinition> listOfFederations;

	@GET
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.FEDERATED_DATASET_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public List<FederationDefinition> get() {
		try {
			objDao = DAOFactory.getFedetatedDatasetDAO();
			listOfFederations = objDao.loadAllFederatedDataSets();
			// needs serialization
			return listOfFederations;
		} catch (EMFUserError e) {
			logger.error("Error while loading federations", e);
			throw new SpagoBIRuntimeException("Error while loading federations", e);
		}
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FEDERATED_DATASET_MANAGEMENT })
	public Response update(FederatedDataSet fds) {
		logger.debug("IN");
		try {
			objDao = DAOFactory.getFedetatedDatasetDAO();
			// objDao.modifyFederation(fds); needs new modifyFederation method
			return Response.ok().build();
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
			objDao = DAOFactory.getFedetatedDatasetDAO();
			objDao.deleteFederatedDatasetById(id);
			return Response.ok().build();
		} catch (EMFUserError e) {
			logger.error("Error while deleting resource", e);
			throw new SpagoBIRuntimeException("Error deleting federation", e);
		}
	}

}
