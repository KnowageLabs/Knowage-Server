package it.eng.spagobi.rest.interceptors;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.security.ExternalServiceController;
import it.eng.spagobi.services.common.SsoServiceFactory;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.services.security.service.SecurityServiceSupplierFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.filters.FilterIOManager;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

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

/**
 * The org.jboss.resteasy.spi.interception.PreProcessInterceptor runs after a JAX-RS resource 
 * method is found to invoke on, but before the actual invocation happens
 * 
 * Similar to SpagoBIAccessFilter but designed for REST services
 *
 */
@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class SecurityServerInterceptor implements PreProcessInterceptor, AcceptedByMethod {

	static private Logger logger = Logger.getLogger(SecurityServerInterceptor.class);
	
	@Context
	private HttpServletRequest servletRequest;
	
	/**
	 * Preprocess all the REST requests.
	 * 
	 * Get the UserProfile from the session and checks if has
	 * the grants to execute the service
	 */
	public ServerResponse preProcess(HttpRequest request, ResourceMethod resourceMethod)
	throws Failure, WebApplicationException {
		
		ServerResponse response;
		
		logger.trace("IN");
		
		response = null;
		try {
			String serviceUrl = InterceptorUtilities.getServiceUrl(request);
			serviceUrl = serviceUrl.replaceAll("/1.0/", "/");
						
			String methodName = resourceMethod.getMethod().getName();
					
			logger.info("Receiving request from: " + servletRequest.getRemoteAddr());
			logger.info("Attempt to invoke method [" + methodName + "] on class [" + resourceMethod.getResourceClass().getName() + "]");
			
			//Check for Services that can be invoked externally without user login in SpagoBI
			ExternalServiceController externalServiceController = ExternalServiceController.getInstance();
			boolean isExternalService = externalServiceController.isExternalService(serviceUrl);
			if (isExternalService == true){
				// TODO check if here we need to create and put in session a profile for the guest user
				return null; // we return null to continue with the service execution
			}
			
			UserProfile profile = null;
			
			//Other checks are required
			boolean authenticated = isUserAuthenticatedInSpagoBI();
			if(!authenticated){
				// try to authenticate the user on the fly using simple-authentication schema
				profile = authenticateUser();
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
				
			boolean authorized = false;
			try {
				authorized = profile.isAbleToExecuteService(serviceUrl);
			} catch (Throwable e) {
				logger.debug("Error checking if the user [" + profile.getUserName() + "] has the rights to call the service ["+serviceUrl+"]",e);
				throw new SpagoBIRuntimeException("Error checking if the user [" + profile.getUserName() + "] has the rights to call the service ["+serviceUrl+"]",e);
			}
				
			if(!authorized){
				try {
					return new ServerResponse( ExceptionUtilities.serializeException("not-enabled-to-call-service", null),	400, new Headers<Object>());
				} catch (Exception e) {
					throw new SpagoBIRuntimeException("Error checking if the user ["+profile.getUserName()+"] has the rights to call the service ["+serviceUrl+"]",e);
				}				
			}else{
				logger.debug("The user ["+profile.getUserName()+"] is enabled to execute the service ["+serviceUrl+"]");
			}
		} catch(Throwable t) {
			if(t instanceof SpagoBIRuntimeException) {
				// ok it's a known exception
			} else {
				new SpagoBIRuntimeException("An unexpected error occured while preprocessing service request", t);
			}
			String msg = t.getMessage();
			if(t.getCause() != null && t.getCause().getMessage() != null) msg += ": " + t.getCause().getMessage();
			response = new ServerResponse(msg, 400, new Headers<Object>());
		} finally {
			logger.trace("OUT");
		}
		
		return response;
	}
	
	
	private UserProfile authenticateUser() {
		UserProfile profile = null;
		
		logger.trace("IN");
		
		try {
			String user = servletRequest.getHeader("user");
			String password = servletRequest.getHeader("password");
			
			ISecurityServiceSupplier supplier = SecurityServiceSupplierFactory.createISecurityServiceSupplier();
			
			SpagoBIUserProfile spagoBIUserProfile = supplier.checkAuthentication(user, password);
			if(spagoBIUserProfile != null) {
				profile = (UserProfile) UserUtilities.getUserProfile(user);
			}
		} catch(Throwable t) {
			throw new RuntimeException("An unexpected error occured while authenticating user", t);
		} finally {
			logger.trace("OUT");
		}
		
		return profile;
	}

	private boolean isUserAuthenticatedInSpagoBI(){
		
		boolean authenticated = true;

		IEngUserProfile engProfile = getUserProfileFromSession();
		
		if (engProfile != null) {
			// verify if the profile stored in session is still valid
			String userId = null;
			try {
				userId = getUserIdentifier();
			} catch (Exception e) {
				logger.debug("User identifier not found");
			}
			if(userId != null && userId.equals(engProfile.getUserUniqueIdentifier().toString()) == false) {
				logger.debug("User is authenticated but the profile store in session need to be updated");
				engProfile = this.getUserProfileFromUserId();
			} else {
				logger.debug("User is authenticated and his profile is already stored in session");
			}
			
		} else {
			engProfile = this.getUserProfileFromUserId();
			if (engProfile !=  null) {
				logger.debug("User is authenticated but his profile is not already stored in session");
			} else {
				logger.debug("User is not authenticated");
				authenticated = false;
			}
		}

		return authenticated;
	}
		
	private IEngUserProfile getUserProfileFromUserId() {
		IEngUserProfile engProfile = null;
		String userId = null;
		try {
			userId = getUserIdentifier();
		} catch (Exception e) {
			logger.debug("User identifier not found");
		}
							
		logger.debug("User id = " + userId);
		if (StringUtilities.isNotEmpty(userId)) {	
			try {
				engProfile = GeneralUtilities.createNewUserProfile(userId);
			} catch (Exception e) {
				e.printStackTrace();
			}	
			setUserProfileInSession(engProfile);
		}
		
		return engProfile;
	}
	
	/**
	 * Finds the user identifier from http request or from SSO system (by the
	 * http request in input). Use the SsoServiceInterface for read the userId
	 * in all cases, if SSO is disabled use FakeSsoService. Check
	 * spagobi_sso.xml
	 * 
	 * @param httpRequest
	 *            The http request
	 * 
	 * @return the current user unique identified
	 * 
	 * @throws Exception
	 *             in case the SSO is enabled and the user identifier specified
	 *             on http request is different from the SSO detected one.
	 */

	private String getUserIdentifier() throws Exception {
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

	// these methods should be abstract because are different in SpagoBI and in the engines. In the first case the object is
	// set and get from session in the second the object is set and get from a specific context withing session(in particular there should
	// be one different context for each distinct executions lunched by the same user on the same borwser.
	private IEngUserProfile getUserProfileFromSession() {
		IEngUserProfile engProfile = null;
		FilterIOManager ioManager = new FilterIOManager(servletRequest, null);
		ioManager.initConetxtManager();	
	
		engProfile =  (IEngUserProfile)servletRequest.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		if(engProfile == null) {
			engProfile = (IEngUserProfile)ioManager.getContextManager().get(IEngUserProfile.ENG_USER_PROFILE);	
		} else {
			setUserProfileInSession(engProfile);
		}	
		
		return engProfile;
	}
	
	private void setUserProfileInSession(IEngUserProfile engProfile) {
		FilterIOManager ioManager = new FilterIOManager(servletRequest, null);
		ioManager.initConetxtManager();	
		ioManager.getContextManager().set(IEngUserProfile.ENG_USER_PROFILE, engProfile);
		
//		servletRequest.getSession().setAttribute(IEngUserProfile.ENG_USER_PROFILE, engProfile);
	}
	
	public boolean accept(Class declaring, Method method) {
		return !method.isAnnotationPresent(POST.class);
		//return true;
	}
}
