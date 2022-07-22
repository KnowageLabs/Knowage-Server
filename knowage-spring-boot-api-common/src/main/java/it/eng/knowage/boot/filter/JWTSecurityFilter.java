/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.boot.filter;

import java.io.IOException;

import javax.naming.Context;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.error.KnowageRuntimeException;
import it.eng.knowage.boot.filter.bean.KnowageHttpServletRequestWrapper;
import it.eng.knowage.boot.utils.ConfigSingleton;
import it.eng.spagobi.services.security.SecurityServiceService;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

public class JWTSecurityFilter implements Filter {

	private static final Logger LOGGER = LogManager.getLogger(JWTSecurityFilter.class);

	@Lazy
	@Autowired
	private SecurityServiceService securityServiceService;

	@Autowired
	private BusinessRequestContext businessRequestContext;

	@Autowired
	private Context ctx;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			String userToken = "";
			String authorizationHeaderName = ConfigSingleton.getInstance().getAuthorizationHeaderName();

			LOGGER.info("Looking for HTTP header with name " + authorizationHeaderName);
			userToken = httpRequest.getHeader(authorizationHeaderName);

			// Fallback to a query parameter with the same name of the HTTP header
			if (userToken == null) {
				LOGGER.warn("Looking for user to token on query parameter");
				LOGGER.info("Looking for query parameter with name " + authorizationHeaderName);
				userToken = httpRequest.getParameter(authorizationHeaderName);
			}

			if (userToken == null) {
				httpResponse.setStatus(401);
			} else {
				LOGGER.info("header: " + userToken);
				String noBearerUserToken = userToken.replace("Bearer ", "");
				String technicalToken = getTechnicalToken();
				try {
					SpagoBIUserProfile profile = securityServiceService.getUserProfile(technicalToken, noBearerUserToken);

					if (profile != null) {

						businessRequestContext.setUsername(profile.getUserId());
						businessRequestContext.setOrganization(profile.getOrganization());
						businessRequestContext.setUserProfile(profile);
						businessRequestContext.setUserToken(userToken);

						RequestContextHolder.currentRequestAttributes().setAttribute("userProfile", profile, RequestAttributes.SCOPE_REQUEST);
						RequestContextHolder.currentRequestAttributes().setAttribute("userToken", userToken, RequestAttributes.SCOPE_REQUEST);

						KnowageHttpServletRequestWrapper newRequest = new KnowageHttpServletRequestWrapper(httpRequest, profile);

						chain.doFilter(newRequest, response);
					} else {
						httpResponse.setStatus(401);
					}

				} catch (Exception e) {
					LOGGER.error("Impossible to get UserProfile from SOAP security service", e);
					httpResponse.setStatus(500);
				}
			}
		} else {
			if (response instanceof HttpServletResponse) {
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.setStatus(400);
			}
		}
	}

	private String getTechnicalToken() {
		String technicalToken = null;
		try {
			String key = (String) ctx.lookup("java:comp/env/hmacKey");
			Algorithm algorithm = Algorithm.HMAC256(key);
			technicalToken = JWT.create().withIssuer("knowage").sign(algorithm);
		} catch (Exception e) {
			throw new KnowageRuntimeException("Cannot get HMAC key", e);
		}
		return technicalToken;
	}

}
