package it.eng.spagobi.wapp.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.SessionUserProfile;
import it.eng.spagobi.commons.bo.SessionUserProfileBuilder;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/setsessionrole")
public class SetSessionRoleResource extends AbstractSpagoBIResource {

	private static Logger logger = Logger.getLogger(SetSessionRoleResource.class);
	public static final String SERVICE_NAME = "SET_SESSION_ROLE";
	public static final String SELECTED_ROLE = "SELECTED_ROLE";

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setSessionRole(@Context HttpServletRequest request) {

		logger.debug("IN on service");
		try {

			IEngUserProfile profile = getUserProfile();
			String sessionRole = request.getParameter(SELECTED_ROLE);
			logger.debug("Selected role " + sessionRole);

			// check if selected role is part of the user ones
			ArrayList<String> roles = (ArrayList<String>) profile.getRoles();

			for (int i = 0; i < roles.size(); i++) {
				logger.debug("user roles " + roles.get(i));
			}

			if (sessionRole.equals("")) {
				sessionRole = null;
			}

			if (sessionRole != null && !roles.contains(sessionRole)) {
				UserProfile userProfile = (UserProfile) profile;
				logger.error("Security alert. Role not among the user ones");
				throw new SpagoBIServiceException(SERVICE_NAME, "Role selected is not permitted for user " + userProfile.getUserId());
			}

			// set this role as session one, or clear session role if not present
			if (!(profile instanceof SessionUserProfile)) {
				// in case the user profile is not a SessionUserProfile, we create it and replace the previous profile in session
				logger.debug("Creating an instance of SessionUserProfile...");
				profile = SessionUserProfileBuilder.getDefaultUserProfile((UserProfile) profile);
				logger.debug("Storing the SessionUserProfile in session in place of the previous profile object");
				storeProfileInSession(profile, request);
			}
			// at this moment, the profile is and instance of SessionUserProfile
			String previousSessionRole = ((SessionUserProfile) profile).getSessionRole();
			logger.debug("previous session role " + previousSessionRole);
			logger.debug("new session role " + sessionRole);
			((SessionUserProfile) profile).setSessionRole(sessionRole);
			logger.debug("session role set! ");

			// now I must refresh userProfile functionalities

			// if new defaultRole is null refresh all the functionalities!
			if (sessionRole == null) {
				logger.debug("Selected role is null, refresh all functionalities");
				IEngUserProfile newProfile = UserUtilities.getUserProfile(profile.getUserUniqueIdentifier().toString());
				Collection functionalities = newProfile.getFunctionalities();
				LogMF.debug(logger, "User functionalities: {0}", new String[] { functionalities.toString() });
				((UserProfile) profile).setFunctionalities(functionalities);
			} else {
				// there is a session role selected so filter only its functionalities
				logger.debug("Selected role is not null, put right functionality");
				Collection functionalities = this.getFunctionalitiesForSessionRole(profile, sessionRole);
				LogMF.debug(logger, "User functionalities considering default role [{0}]: {1}", new String[] { sessionRole, functionalities.toString() });
				((UserProfile) profile).setFunctionalities(functionalities);
				logger.debug("set functionalities for default role");
			}
			logger.debug("Filtered functionalities for selected role " + sessionRole);

			return Response.ok().build();

		} catch (SpagoBIServiceException sbe) {
			// Errori di validazione/servizio -> 400
			logger.error("Service error in " + SERVICE_NAME, sbe);
			return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("error", sbe.getMessage())).build();
		} catch (Exception e) {
			// Errori generici -> 500
			logger.error("Exception occurred while retrieving metadata", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Collections.singletonMap("error", e.getMessage())).build();
		} finally {
			logger.debug("OUT");
		}
	}

	private void storeProfileInSession(IEngUserProfile userProfile, HttpServletRequest request) {
		logger.debug("IN");
		request.getSession().setAttribute(IEngUserProfile.ENG_USER_PROFILE, userProfile);
		logger.debug("OUT");
	}

	private Collection getFunctionalitiesForSessionRole(IEngUserProfile engUserProfile, String sessionRole) {
		logger.debug("IN: sessionRole is [" + sessionRole + "]");
		Collection toReturn = null;
		try {
			String[] roles = new String[] { sessionRole };
			SpagoBIUserProfile profile = ((UserProfile) engUserProfile).getSpagoBIUserProfile();
			SpagoBIUserProfile clone = UserUtilities.clone(profile);
			// we limit the roles to the clone object and recalculate functionalities
			clone.setRoles(roles);
			String[] functionalitiesArray = UserUtilities.readFunctionality(clone);
			toReturn = StringUtilities.convertArrayInCollection(functionalitiesArray);
		} catch (Exception ex) {
			logger.error("Error while getting functionalities from the session role", ex);
			throw ex;
		} finally {
			if (toReturn != null) {
				LogMF.debug(logger, "Returning: {0}", new String[] { toReturn.toString() });
			}
		}
		return toReturn;
	}

}
