package it.eng.knowage.knowageapi.provider;

import java.io.IOException;

import javax.annotation.Priority;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import it.eng.knowage.knowageapi.error.KnowageRuntimeException;
import it.eng.knowage.knowageapi.utils.ConfigSingleton;
import it.eng.spagobi.services.security.SecurityServiceService;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Provider
@Priority(Priorities.AUTHENTICATION)
@Component
public class JWTSecurityInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

	static private Logger logger = Logger.getLogger(JWTSecurityInterceptor.class);

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		logger.info("FILTER OUT");
	}

	@Autowired
	@Lazy
	SecurityServiceService securityServiceService;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		logger.info("FILTER IN");
		String userToken = requestContext.getHeaderString(ConfigSingleton.getInstance().getAuthorizationHeaderName());
		logger.info("header: " + userToken);
		SpagoBIUserProfile profile = null;
		String noBearerUserToken = userToken.replace("Bearer ", "");
		String technicalToken = getTechnicalToken();
		try {
			profile = securityServiceService.getUserProfile(technicalToken, noBearerUserToken);
			RequestContextHolder.currentRequestAttributes().setAttribute("userProfile", profile, RequestAttributes.SCOPE_REQUEST);
			RequestContextHolder.currentRequestAttributes().setAttribute("userToken", userToken, RequestAttributes.SCOPE_REQUEST);
		} catch (Exception e) {
			throw new KnowageRuntimeException("Impossible to get UserProfile from SOAP security service", e);
		}
	}

	public static String getTechnicalToken() {
		String technicalToken = null;
		Context ctx;
		try {
			ctx = new InitialContext();
			// Calendar calendar = Calendar.getInstance();
			// calendar.add(Calendar.MINUTE, 5); // token for services will expire in 5 minutes
			// Date expiresAt = calendar.getTime();
			String key = (String) ctx.lookup("java:/comp/env/hmacKey");
			Algorithm algorithm = Algorithm.HMAC256(key);
			technicalToken = JWT.create().withIssuer("knowage")
					// .withExpiresAt(expiresAt)
					.sign(algorithm);
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}
		return technicalToken;
	}
}
