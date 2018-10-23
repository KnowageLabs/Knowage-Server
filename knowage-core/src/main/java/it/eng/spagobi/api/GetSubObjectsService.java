package it.eng.spagobi.api;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.dao.ISubObjectDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;

@Path("/1.0/olapsubobjects")
@ManageAuthorization
public class GetSubObjectsService extends AbstractSpagoBIResource {
	public static final String SERVICE_NAME = "GET_SUBOBJECTS_SERVICE_ACTION";

	@GET
	@Path("/getSubObjects")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getSubObjects(@QueryParam("idObj") Integer biobjectId) {
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		List subObjectsList = null;
		IEngUserProfile userProfile = this.getUserProfile();
		try {
			if (userProfile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
				subObjectsList = DAOFactory.getSubObjectDAO().getSubObjects(biobjectId);
			} else {
				subObjectsList = DAOFactory.getSubObjectDAO().getAccessibleSubObjects(biobjectId, userProfile);
			}
		} catch (EMFUserError e) {
			logger.error("Error while recovering subobjects list for document with id = " + biobjectId, e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Cannot load customized views", e);
		} catch (EMFInternalError e) {
			logger.error("Error while recovering information about user", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Error while recovering information about user", e);
		}

		resultAsMap.put("results", subObjectsList);
		return Response.ok(resultAsMap).build();
	}

	@DELETE
	@Path("/removeOlapSubObject")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response removeOlapSubObject(@QueryParam("idObj") Integer biobjectId) {
		UserProfile userProfile = this.getUserProfile();
		ISubObjectDAO dao = DAOFactory.getSubObjectDAO();

		SubObject subObject = null;
		try {
			subObject = dao.getSubObject(biobjectId);
		} catch (EMFUserError e) {
			logger.error("SubObject with id = " + biobjectId + " not found", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Customized view not found", e);
		}
		boolean canDeleteSubObject = false;
		try {
			if (userProfile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)
					|| subObject.getOwner().equals(userProfile.getUserId().toString())) {
				canDeleteSubObject = true;
			}
		} catch (EMFInternalError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (canDeleteSubObject) {
			logger.info("User [id: " + userProfile.getUserUniqueIdentifier() + ", userId: " + userProfile.getUserId() + ", name: " + userProfile.getUserName()
					+ "] " + "is deleting customized view [id: " + subObject.getId() + ", name: " + subObject.getName() + "] ...");
			try {
				dao.deleteSubObject(biobjectId);
			} catch (EMFUserError e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Error while deleting customized view", e);
			}
			logger.debug("Customized view [id: " + subObject.getId() + ", name: " + subObject.getName() + "] deleted.");
		} else {
			logger.error("User [id: " + userProfile.getUserUniqueIdentifier() + ", userId: " + userProfile.getUserId() + ", name: " + userProfile.getUserName()
					+ "] cannot delete customized view");
			throw new SpagoBIServiceException(SERVICE_NAME, "User cannot delete customized view");
		}

		return Response.ok().build();
	}

}
