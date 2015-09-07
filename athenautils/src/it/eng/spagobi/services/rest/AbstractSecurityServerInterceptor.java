package it.eng.spagobi.services.rest;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.security.ExternalServiceController;
import it.eng.spagobi.services.common.SsoServiceFactory;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.services.rest.annotations.CheckFunctionalitiesParser;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;

/**
 * The org.jboss.resteasy.spi.interception.PreProcessInterceptor runs after a JAX-RS resource method is found to invoke on, but before the actual invocation
 * happens
 *
 * Similar to SpagoBIAccessFilter but designed for REST services
 *
 */
public abstract class AbstractSecurityServerInterceptor {

	static private Logger logger = Logger.getLogger(AbstractSecurityServerInterceptor.class);

	@Context
	private HttpServletRequest servletRequest;

	/**
	 * Preprocess all the REST requests.
	 *
	 * Get the UserProfile from the session and checks if has the grants to execute the service
	 */
	public ServerResponse preProcess(HttpRequest request, ResourceMethod resourceMethod) throws Failure, WebApplicationException {

		ServerResponse response;

		logger.trace("IN");

		response = null;
		try {
			String serviceUrl = InterceptorUtilities.getServiceUrl(request);
			serviceUrl = serviceUrl.replaceAll("/1.0/", "/");
			serviceUrl = serviceUrl.replaceAll("/2.0/", "/");

			String methodName = resourceMethod.getMethod().getName();

			logger.info("Receiving request from: " + servletRequest.getRemoteAddr());
			logger.info("Attempt to invoke method [" + methodName + "] on class [" + resourceMethod.getResourceClass().getName() + "]");

			// Check for Services that can be invoked externally without user login in SpagoBI
			ExternalServiceController externalServiceController = ExternalServiceController.getInstance();
			boolean isExternalService = externalServiceController.isExternalService(serviceUrl);
			if (isExternalService == true) {
				// TODO check if here we need to create and put in session a profile for the guest user
				return null; // we return null to continue with the service execution
			}

			UserProfile profile = null;

			// Other checks are required
			boolean authenticated = isUserAuthenticatedInSpagoBI();
			if (!authenticated) {
				// try to authenticate the user on the fly using simple-authentication schema
				profile = authenticateUser();
			} else {
				// get the user profile from session
				profile = (UserProfile) getUserProfileFromSession();
			}

			if (profile == null) {
				return notAuthenticated();
			}
			// Profile is not null
			UserProfileManager.setProfile(profile);

			boolean authorized = false;

			// If the resource class is annotated with @ManageAuthorization authorizations are specified using the @UserConstraint annotation
			if (resourceMethod.getMethod().getDeclaringClass().isAnnotationPresent(ManageAuthorization.class)) {
				// Functionalities annotation parser
				CheckFunctionalitiesParser checkFunctionalitiesParser = new CheckFunctionalitiesParser();

				// If the security annotation is not present on the method, this method is public
				boolean isPublicService = checkFunctionalitiesParser.isPublicService(resourceMethod.getMethod());

				if (isPublicService)
					authorized = true;
				else {
					authorized = checkFunctionalitiesParser.checkFunctionalitiesByAnnotation(resourceMethod.getMethod(), profile);
				}
			} else { // Old method for authorization (without annotation)
				try {
					authorized = profile.isAbleToExecuteService(serviceUrl);
				} catch (Throwable e) {
					logger.debug("Error checking if the user [" + profile.getUserName() + "] has the rights to call the service [" + serviceUrl + "]", e);
					throw new SpagoBIRuntimeException("Error checking if the user [" + profile.getUserName() + "] has the rights to call the service ["
							+ serviceUrl + "]", e);
				}
			}

			if (!authorized) {
				try {
					return new ServerResponse(ExceptionUtilities.serializeException("not-enabled-to-call-service", null), 400, new Headers<Object>());
				} catch (Exception e) {
					throw new SpagoBIRuntimeException("Error checking if the user [" + profile.getUserName() + "] has the rights to call the service ["
							+ serviceUrl + "]", e);
				}
			} else {
				logger.debug("The user [" + profile.getUserName() + "] is enabled to execute the service [" + serviceUrl + "]");
			}

			return response;
		} catch (SpagoBIRuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An unexpected error occured while preprocessing service request", e);
		}
	}

	/**
	 * It defines the behaviour of the interceptor when the user is not authenticated. It has to be returned a response with status code 401. If Basic
	 * Authentication is used it should be send back also a the "WWW-Authenticate" header as required by Basic Authentication standard.
	 * */
	protected abstract ServerResponse notAuthenticated();

	public void postProcess(ServerResponse response) {
		logger.debug("IN");
		UserProfileManager.unset();
		logger.debug("OUT");
	}

	protected abstract UserProfile authenticateUser();

	protected abstract boolean isUserAuthenticatedInSpagoBI();

	protected IEngUserProfile getUserProfileFromUserId() {
		IEngUserProfile engProfile = null;
		String userId = null;
		try {
			userId = getUserIdentifier();
		} catch (Exception e) {
			logger.debug("User identifier not found");
			throw new SpagoBIRuntimeException("User identifier not found", e);
		}

		logger.debug("User id = " + userId);
		if (StringUtilities.isNotEmpty(userId)) {
			try {
				engProfile = createProfile(userId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			setUserProfileInSession(engProfile);
		}

		return engProfile;
	}

	protected abstract IEngUserProfile createProfile(String userId);

	/**
	 * Finds the user identifier from http request or from SSO system (by the http request in input). Use the SsoServiceInterface for read the userId in all
	 * cases, if SSO is disabled use FakeSsoService. Check spagobi_sso.xml
	 *
	 * @param httpRequest
	 *            The http request
	 *
	 * @return the current user unique identified
	 *
	 * @throws Exception
	 *             in case the SSO is enabled and the user identifier specified on http request is different from the SSO detected one.
	 */
	protected String getUserIdentifier() throws Exception {
		logger.debug("IN");
		String userId = null;
		try {
			SsoServiceInterface userProxy = SsoServiceFactory.createProxyService();
			userId = userProxy.readUserIdentifier(servletRequest);
		} finally {
			logger.debug("OUT");
		}
		return userId;
	}

	protected abstract IEngUserProfile getUserProfileFromSession();

	protected void setUserProfileInSession(IEngUserProfile engProfile) {
		servletRequest.getSession().setAttribute(IEngUserProfile.ENG_USER_PROFILE, engProfile);
	}
}
