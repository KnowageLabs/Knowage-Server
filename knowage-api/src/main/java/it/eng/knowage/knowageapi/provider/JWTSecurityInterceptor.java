/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.knowageapi.provider;

import java.io.IOException;

import javax.annotation.Priority;
import javax.naming.Context;
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

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.error.KnowageRuntimeException;
import it.eng.knowage.boot.utils.ConfigSingleton;
import it.eng.spagobi.services.security.SecurityServiceService;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Provider
@Priority(Priorities.AUTHENTICATION)
@Component
public class JWTSecurityInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

	private static final Logger LOGGER = Logger.getLogger(JWTSecurityInterceptor.class);

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		LOGGER.info("FILTER OUT");
	}

	@Autowired
	@Lazy
	SecurityServiceService securityServiceService;

	@Autowired
	private BusinessRequestContext businessRequestContext;

	@Autowired
	private Context ctx;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		LOGGER.info("FILTER IN");
		String userToken = "";
		try {
			userToken = requestContext.getHeaderString(ConfigSingleton.getInstance().getAuthorizationHeaderName());
		} catch (Exception e) {
			LOGGER.error("Impossible to get Header Authentication X-Kn-Authorization");
			throw new KnowageRuntimeException("Impossible to get Header Authentication X-Kn-Authorization", e);
		}
		LOGGER.info("header: " + userToken);
		SpagoBIUserProfile profile = null;
		String noBearerUserToken = userToken.replace("Bearer ", "");
		String technicalToken = getTechnicalToken();
		try {
			profile = securityServiceService.getUserProfile(technicalToken, noBearerUserToken);

			businessRequestContext.setUsername(profile.getUserId());
			businessRequestContext.setOrganization(profile.getOrganization());
			businessRequestContext.setUserProfile(profile);
			businessRequestContext.setUserToken(userToken);

			RequestContextHolder.currentRequestAttributes().setAttribute("userProfile", profile, RequestAttributes.SCOPE_REQUEST);
			RequestContextHolder.currentRequestAttributes().setAttribute("userToken", userToken, RequestAttributes.SCOPE_REQUEST);
		} catch (Exception e) {
			throw new KnowageRuntimeException("Impossible to get UserProfile from SOAP security service", e);
		}
	}

	public String getTechnicalToken() {
		String technicalToken = null;
		try {
			String key = (String) ctx.lookup("java:comp/env/hmacKey");
			Algorithm algorithm = Algorithm.HMAC256(key);
			technicalToken = JWT.create().withIssuer("knowage").sign(algorithm);
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}
		return technicalToken;
	}
}
