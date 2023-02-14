package it.eng.knowage.boot.interceptor;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.error.ExceptionUtilities;
import it.eng.knowage.boot.error.KnowageRuntimeException;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Component
@Provider
public class SecurityAuthorizationInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

	private static final Logger LOGGER = LogManager.getLogger(SecurityAuthorizationInterceptor.class);

	@Context
	private ResourceInfo resourceInfo;

	@Context
	protected HttpServletRequest servletRequest;

	@Autowired
	private BusinessRequestContext businessRequestContext;

	public SecurityAuthorizationInterceptor(BusinessRequestContext businessRequestContext) {
		super();
		this.businessRequestContext = businessRequestContext;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		Method method = resourceInfo.getResourceMethod();
		LOGGER.info("Receiving request from: " + servletRequest.getRemoteAddr());
		LOGGER.info("Attempt to invoke method [" + method.getName() + "] on class [" + resourceInfo.getResourceClass() + "]");

		if (method.isAnnotationPresent(PublicService.class)) {
			LOGGER.debug("Invoked service is public");
			return;
		}

		SpagoBIUserProfile profile = businessRequestContext.getUserProfile();

		// look for @UserConstraint annotation
		CheckFunctionalitiesParser checkFunctionalitiesParser = new CheckFunctionalitiesParser();

		// the user is authorized for the service if it does not have a user constraint or in case the user satisfies the constraints
		boolean authorized;
		try {
			authorized = !checkFunctionalitiesParser.hasUserConstraints(method) || checkFunctionalitiesParser.checkFunctionalitiesByAnnotation(method, profile);

			if (!authorized) {
				try {
					requestContext.abortWith(Response.status(400).entity(ExceptionUtilities.serializeException("not-enabled-to-call-service", null)).build());
				} catch (Exception e) {
					throw new KnowageRuntimeException("Error checking if the user [" + profile.getUserName() + "] has the rights to invoke method ["
							+ method.getName() + "] on class [" + resourceInfo.getResourceClass() + "]", e);
				}
			} else {
				LOGGER.debug("The user [" + profile.getUserName() + "] is enabled to invoke method [" + method.getName() + "] on class ["
						+ resourceInfo.getResourceClass() + "]");
			}

		} catch (Exception e1) {
			throw new KnowageRuntimeException("Error in user authorization", e1);
		}

	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		// TODO Auto-generated method stub

	}
}
