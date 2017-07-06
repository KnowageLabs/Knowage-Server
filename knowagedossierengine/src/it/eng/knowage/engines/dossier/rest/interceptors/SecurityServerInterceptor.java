package it.eng.knowage.engines.dossier.rest.interceptors;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.LoggableFailure;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.common.SsoServiceFactory;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.proxy.SecurityServiceProxy;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class SecurityServerInterceptor implements PreProcessInterceptor, AcceptedByMethod{

	
	static private Logger logger = Logger.getLogger(SecurityServerInterceptor.class);
	
	@Context
	private HttpServletRequest servletRequest;
	
	@Override
	public boolean accept(Class arg0, Method arg1) {
		return true;
	}

	@Override
	public ServerResponse preProcess(HttpRequest request, ResourceMethod arg1) throws Failure, WebApplicationException {
		
		ServerResponse response = null;
		UserProfile profile = null;
		logger.debug("IN");
		try{
			
			//Other checks are required
			boolean authenticated = isUserAuthenticatedInSpagoBI();
			if(!authenticated){
				// try to authenticate the user on the fly using simple-authentication schema
				//profile = authenticateUser();
			} else {
				// get the user profile from session 
				profile = (UserProfile) getUserProfileFromSession();
			}
			
			if(profile == null) {
				// TODO check if the error can be processed by the client
				//throws unlogged user exception that will be managed by RestExcepionMapper
			    logger.info("User not logged");
			    throw new LoggableFailure( request.getUri().getRequestUri().getPath() );
			}
			
		}catch(Throwable t) {
			if(t instanceof SpagoBIRuntimeException) {
				// ok it's a known exception
			} else {
				new SpagoBIRuntimeException("An unexpected error occured while preprocessing service request", t);
			}
			String msg = t.getMessage();
			if(t.getCause() != null && t.getCause().getMessage() != null) msg += ": " + t.getCause().getMessage();
			response = new ServerResponse(msg, 400, new Headers<Object>());
		} finally {
			logger.debug("OUT");
		}
		
		 return response;
	}

//	private UserProfile authenticateUser() {
//		UserProfile profile = null;
//
//		logger.trace("IN");
//
//		try {
//			String auto = servletRequest.getHeader("Authorization");
//			if (auto != null) {
//				int position = auto.indexOf("Direct");
//				if (position > -1 && position < 5) {// Direct stay at the beginning of the header
//					String encodedUser = auto.replaceFirst("Direct ", "");
//					byte[] decodedBytes = Base64.decode(encodedUser);
//					String userId = new String(decodedBytes, "UTF-8");
//					SecurityServiceProxy proxy = new SecurityServiceProxy(userId, servletRequest.getSession());
//					profile = (UserProfile) proxy.getUserProfile();
//					
//					setUserProfileInSession(profile);
//				}
//			}
//		} catch (Throwable t) {
//			logger.trace("Problem during authentication, returning null", t);
//		} finally {
//			logger.trace("OUT");
//		}
//
//		return profile;
//	}

	private boolean isUserAuthenticatedInSpagoBI() {
		logger.debug("IN");
		boolean authenticated = true;

		try {
			IEngUserProfile engProfile = getUserProfileFromSession();

			if (engProfile != null) {
				logger.debug("User is authenticated and his profile is already stored in session");
			} else {
				// TODO THIS NEED TO BE FIXED -> THIS METHOD SHOULD ONLY VERIFY IF THE USER IS IN SESSION, NOT TRYING TO AUTHENTICATE IT
				engProfile = this.getUserProfileFromUserId();
			}

			if (engProfile != null) {
				logger.debug("User is authenticated but his profile is not already stored in session");
			} else {
				logger.debug("User is not authenticated");
				authenticated = false;
			}
		} catch (Exception e) {
			logger.debug("Error while attempt to find user profile in session or authenticate user. Returning [false]", e);
			authenticated = false;
		}

		logger.debug("OUT");
		return authenticated;
	}

	private IEngUserProfile getUserProfileFromUserId() {
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
				logger.debug("Error creating user profile");
				throw new SpagoBIRuntimeException("Error creating user profile", e);
			}
			setUserProfileInSession(engProfile);
		}

		return engProfile;
	}

	private void setUserProfileInSession(IEngUserProfile engProfile) {
		servletRequest.getSession().setAttribute(IEngUserProfile.ENG_USER_PROFILE, engProfile);
		
	}

	private IEngUserProfile createProfile(String userId) {
		SecurityServiceProxy proxy = new SecurityServiceProxy(userId, servletRequest.getSession());
		try {
			return proxy.getUserProfile();
		} catch (it.eng.spagobi.services.security.exceptions.SecurityException e) {
			logger.error("Error while creating user profile with user id = [" + userId + "]", e);
			throw new SpagoBIRuntimeException("Error while creating user profile with user id = [" + userId + "]", e);
		}
	}

	private String getUserIdentifier() {
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

	private IEngUserProfile getUserProfileFromSession() {
		return (IEngUserProfile) servletRequest.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	}

	

}
