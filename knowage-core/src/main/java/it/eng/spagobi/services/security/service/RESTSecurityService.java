package it.eng.spagobi.services.security.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.user.UserProfileManager;

@Path("/userprofile")
public class RESTSecurityService {

	static protected Logger logger = Logger.getLogger(RESTSecurityService.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public UserProfile getUserProfile() {
		logger.debug("IN");
		UserProfile userProfile = UserProfileManager.getProfile();
		logger.debug("OUT");
		return userProfile;
	}
}
