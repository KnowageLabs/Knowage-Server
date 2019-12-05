package it.eng.spagobi.services.security.service;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spagobi.api.DataSetResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;

@Path("/security")
public class RESTSecurityService {

	static protected Logger logger = Logger.getLogger(DataSetResource.class);

	@POST
	@Path("/pythonEdit/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String checkPythonEditAuthorization(@PathParam("userId") String userId) {
		logger.debug("IN");
		try {
			JSONObject json = new JSONObject();
			UserProfile userProfile = (UserProfile) UserUtilities.getUserProfile(userId);
			Boolean permission = userProfile.isAbleToExecuteAction("EditPythonScripts");
			if (permission) {
				saveScriptOnDatabase("", userId);
			}
			json.put("Authorization", permission);
			return json.toString();
		} catch (Throwable t) {
			logger.error("Error while checking python authorization for user " + userId, t);
			return null;
		} finally {
			logger.debug("OUT");
		}
	}

	private void saveScriptOnDatabase(String userId, String script) {

	}
}
