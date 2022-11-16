package it.eng.spagobi.api.v3;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.knowage.security.ProductProfiler;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * 
 * @author albnale
 *
 *         This service was created while porting Knowage Vue. It is used to retrieve the list of roles available for the user to execute the document you are
 *         trying to execute.
 */

@Path("/3.0/documentexecution")
public class DocumentExecutionResource {

	static protected Logger logger = Logger.getLogger(DocumentExecutionResource.class);

	@GET
	@Path("/correctRolesForExecution")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentExecutionFilterList(@QueryParam("id") Integer id, @QueryParam("label") String label) {
		logger.debug("IN");

		List roles = null;
		UserProfile userProfile = UserProfileManager.getProfile();

		ObjectsAccessVerifier oav = new ObjectsAccessVerifier();

		try {
			checkExecRightsByProducts(id, label);
			if (id != null) {
				roles = oav.getCorrectRolesForExecution(id, userProfile);
			} else {
				roles = oav.getCorrectRolesForExecution(label, userProfile);
			}
		} catch (EMFInternalError e) {
			logger.error("Cannot retrieve correct roles for execution", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		} catch (EMFUserError e) {
			logger.error("Cannot retrieve correct roles for execution", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		}

		if (roles == null) {
			roles = new ArrayList<String>();
		}

		logger.debug("OUT");
		return Response.ok().entity(roles).build();
	}

	private void checkExecRightsByProducts(Integer id, String label) throws EMFUserError {
		BIObject biobj = null;
		if (id != null) {
			biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(id);
		} else {
			biobj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(label);
		}
		if (!ProductProfiler.canExecuteDocument(biobj)) {
			throw new SpagoBIRuntimeException("This document cannot be executed within the current product!");
		}
	}

}
