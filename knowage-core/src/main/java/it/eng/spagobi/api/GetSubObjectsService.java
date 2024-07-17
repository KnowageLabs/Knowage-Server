package it.eng.spagobi.api;

import java.util.HashMap;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.dao.ISubObjectDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/1.0/olapsubobjects")
@ManageAuthorization
public class GetSubObjectsService extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(GetSubObjectsService.class);

	public static final String SERVICE_NAME = "GET_SUBOBJECTS_SERVICE_ACTION";

	@GET
	@Path("/getSubObjects")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getSubObjects(@QueryParam("idObj") Integer biobjectId) {
		HashMap<String, Object> resultAsMap = new HashMap<>();
		List<SubObject> subObjectsList = null;
		IEngUserProfile userProfile = this.getUserProfile();
		try {
			if (userProfile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
				subObjectsList = DAOFactory.getSubObjectDAO().getSubObjects(biobjectId);
			} else {
				subObjectsList = DAOFactory.getSubObjectDAO().getAccessibleSubObjects(biobjectId, userProfile);
			}
		} catch (EMFUserError e) {
			LOGGER.error("Error while recovering subobjects list for document with id = {}", biobjectId, e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Cannot load customized views", e);
		} catch (EMFInternalError e) {
			LOGGER.error("Error while recovering information about user", e);
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
			LOGGER.error("SubObject with id = " + biobjectId + " not found", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Customized view not found", e);
		}
		boolean canDeleteSubObject = false;
		try {
			if (userProfile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN)
					|| subObject.getOwner().equals(userProfile.getUserId().toString())) {
				canDeleteSubObject = true;
			}
		} catch (EMFInternalError e1) {
			LOGGER.error("Error while recovering information about user",e1);
		}
		if (canDeleteSubObject) {
			LOGGER.info("User [id: {}, userId: {}, name: {}] is deleting customized view [id: {}, name: {}] ...",
					userProfile.getUserUniqueIdentifier(), userProfile.getUserId(), userProfile.getUserName(),
					subObject.getId(), subObject.getName());
			try {
				dao.deleteSubObject(biobjectId);
			} catch (EMFUserError e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Error while deleting customized view", e);
			}
			LOGGER.debug("Customized view [id: {}, name: {}] deleted.", subObject.getId(), subObject.getName());
		} else {
			LOGGER.error("User [id: {}, userId: {}, name: {}] cannot delete customized view",
					userProfile.getUserUniqueIdentifier(), userProfile.getUserId(), userProfile.getUserName());
			throw new SpagoBIServiceException(SERVICE_NAME, "User cannot delete customized view");
		}

		return Response.ok().build();
	}

}
