/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.services.common;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * SSO system based on request header, suitable when integrating with Shibboleth or any other external system that is providing information on headers
 */
public class FullRequestScanSsoService extends JWTSsoService {

	static private final String USER_IDENTIFIER_DEFAULT_REQUEST_HEADER_NAME = "REMOTE_USER";

	static private final String USER_IDENTIFIER_PARAMETER_NAME_SYSTEM_PROPERTY = "knowage.sso.request.parameter.name";

	private static int USER_JWT_TOKEN_EXPIRE_HOURS = 10; // JWT token for regular users will expire in 10 HOURS

	static private Logger logger = Logger.getLogger(FullRequestScanSsoService.class);

	@Override
	public String readUserIdentifier(HttpServletRequest request) {
		logger.debug("IN");

		Assert.assertNotNull(request, "Input parameter [request] cannot be null");

		try {

			String headerName = getHeaderName();
			String userId = request.getHeader(headerName);
			LogMF.debug(logger, "Request header {0} is equal to [{1}]", headerName, userId);
			if (userId == null) {

				LogMF.debug(logger, "Request header {0} not found. Using default JWT SSO system...", headerName);
				// in case header is not present, defaults to regular JWT SSO system
				for (Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();) {
					String nextHeaderName = (String) e.nextElement();
					LogMF.debug(logger, "Header nextHeaderName...", nextHeaderName);
					String headerValue = request.getHeader(nextHeaderName);
					LogMF.debug(logger, "Header values...", headerValue);
				}

				userId = (String) request.getAttribute(headerName);
				logger.debug("Request attribute [" + headerName + "] is equal to [" + userId + "]");

				if (userId == null) {
					userId = request.getParameter(USER_IDENTIFIER_PARAMETER_NAME_SYSTEM_PROPERTY);
					logger.debug("Request parameter [" + USER_IDENTIFIER_PARAMETER_NAME_SYSTEM_PROPERTY + "] is equal to [" + userId + "]");

					if (userId == null) {
						userId = request.getRemoteUser();
						logger.debug("Remote user is equal to [" + userId + "]");
					}
				}

			}
			String jwtToken = getJWTToken(userId);
			LogMF.debug(logger, "OUT: returning [{0}]", jwtToken);

			return jwtToken;
		} catch (Exception t) {
			// fail fast
			logger.error("An unpredicted error occurred while reading user identifier", t);
			throw new RuntimeException("An unpredicted error occurred while reading user identifier", t);
		}
	}

	protected String getJWTToken(String userId) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, USER_JWT_TOKEN_EXPIRE_HOURS);
		Date expiresAt = calendar.getTime();
		String jwtToken = JWTSsoService.userId2jwtToken(userId, expiresAt);
		return jwtToken;
	}

	private String getHeaderName() {
		String toReturn = System.getProperty(USER_IDENTIFIER_PARAMETER_NAME_SYSTEM_PROPERTY);
		LogMF.debug(logger, "Request header name found from system properties: [{0}]", toReturn);
		if (StringUtilities.isEmpty(toReturn)) {
			LogMF.debug(logger, "System property [{0}] was not found or it was empty. Using default request header name [{1}] ...",
					USER_IDENTIFIER_PARAMETER_NAME_SYSTEM_PROPERTY, USER_IDENTIFIER_DEFAULT_REQUEST_HEADER_NAME);
			toReturn = USER_IDENTIFIER_DEFAULT_REQUEST_HEADER_NAME;
		}
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

}
