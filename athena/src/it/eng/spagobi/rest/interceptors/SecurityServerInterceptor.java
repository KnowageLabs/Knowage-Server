package it.eng.spagobi.rest.interceptors;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.proxy.SecurityServiceProxy;
import it.eng.spagobi.services.rest.AbstractSecurityServerInterceptor;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.services.security.service.SecurityServiceSupplierFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

/**
 * The org.jboss.resteasy.spi.interception.PreProcessInterceptor runs after a JAX-RS resource method is found to invoke on, but before the actual invocation
 * happens
 *
 * Similar to SpagoBIAccessFilter but designed for REST services
 *
 */
@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class SecurityServerInterceptor extends AbstractSecurityServerInterceptor implements PreProcessInterceptor, AcceptedByMethod {

	static private Logger logger = Logger.getLogger(SecurityServerInterceptor.class);

	@Context
	private HttpServletRequest servletRequest;

	@Override
	protected ServerResponse notAuthenticated() {
		/*
		 * This response is standard in Basic authentication. If the header with credentials is missing the server send the response asking for the header. The
		 * browser will show a popup that requires the user credential.
		 */
		Headers<Object> header = new Headers<Object>();
		header.add("WWW-Authenticate", "Basic realm='spagobi'");
		return new ServerResponse("", 401, header);
	}

	@Override
	protected UserProfile authenticateUser() {
		UserProfile profile = null;

		logger.trace("IN");

		try {
			String auto = servletRequest.getHeader("Authorization");
			int position = auto.indexOf("Direct");
			if(position>-1 && position<5){//Direct stay at the beginning of the header
				String encodedUser=  auto.replaceFirst("Direct ", "");
				byte[] decodedBytes = Base64.decode(encodedUser);
				String user = new String(decodedBytes, "UTF-8");
				profile = (UserProfile) UserUtilities.getUserProfile(user);
			}else{
				String encodedUserPassword =  auto.replaceFirst("Basic ", "");
				String credentials = null;
				byte[] decodedBytes = Base64.decode(encodedUserPassword);
				credentials = new String(decodedBytes, "UTF-8");

				StringTokenizer tokenizer = new StringTokenizer(credentials, ":");
				String user = tokenizer.nextToken();
				String password = tokenizer.nextToken();

				ISecurityServiceSupplier supplier = SecurityServiceSupplierFactory.createISecurityServiceSupplier();

				SpagoBIUserProfile spagoBIUserProfile = supplier.checkAuthentication(user, password);
				if (spagoBIUserProfile != null) {
					profile = (UserProfile) UserUtilities.getUserProfile(user);
				}

			}
		} catch (Throwable t) {
			logger.trace("Problem during authentication, returning null", t);
		} finally {
			logger.trace("OUT");
		}

		return profile;
	}

	@Override
	protected boolean isUserAuthenticatedInSpagoBI() {
		boolean authenticated = true;
		IEngUserProfile engProfile = getUserProfileFromSession();

		if (engProfile == null) {
			authenticated = false;
			logger.debug("User profile not in session.");
		}
		return authenticated;
	}

	@Override
	protected IEngUserProfile createProfile(String userId) {
		SecurityServiceProxy proxy = new SecurityServiceProxy(userId, servletRequest.getSession());

		try {
			return GeneralUtilities.createNewUserProfile(userId);
		} catch (Exception e) {
			logger.error("Error while creating user profile with user id = [" + userId + "]", e);
			throw new SpagoBIRuntimeException("Error while creating user profile with user id = [" + userId + "]", e);
		}
	}

	@Override
	protected IEngUserProfile getUserProfileFromSession() {
		return (IEngUserProfile) servletRequest.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	}

	public boolean accept(Class declaring, Method method) {
		// return !method.isAnnotationPresent(POST.class);
		return true;
	}
}
