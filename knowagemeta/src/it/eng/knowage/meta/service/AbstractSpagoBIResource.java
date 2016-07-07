package it.eng.knowage.meta.service;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.rest.AbstractRestService;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

public class AbstractSpagoBIResource extends AbstractRestService {

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;

	public static transient Logger logger = Logger.getLogger(AbstractSpagoBIResource.class);

	public EngineStartServletIOManager getIOManager() {
		EngineStartServletIOManager ioManager = null;

		try {
			ioManager = new EngineStartServletIOManager(request, response);
			UserProfile userProfile = getUserProfile();
			if (userProfile == null) {
				String userId = request.getHeader("user");
				userProfile = (UserProfile) UserUtilities.getUserProfile(userId);
				ioManager.setUserProfile(userProfile);
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while inizializing ioManager", t);
		}

		return ioManager;
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return request;
	}

	public HttpServletResponse getServletResponse() {
		return response;
	}

	public UserProfile getUserProfile() {
		return UserProfileManager.getProfile();
	}

}
