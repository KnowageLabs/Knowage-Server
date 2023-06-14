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
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * SSO system based on request header, suitable when integrating with Shibboleth or any other external system that is providing information on headers
 */
public class FullRequestScanSsoService extends JWTSsoService {

	private static final String USER_IDENTIFIER_DEFAULT_REQUEST_HEADER_NAME = "REMOTE_USER";

	private static final String USER_IDENTIFIER_PARAMETER_NAME_SYSTEM_PROPERTY = "knowage.sso.request.parameter.name";

	private static int USER_JWT_TOKEN_EXPIRE_HOURS = 10; // JWT token for regular users will expire in 10 HOURS

	private static Logger logger = Logger.getLogger(FullRequestScanSsoService.class);

	@FunctionalInterface
	interface OptionalFunction {
		Optional<String> getUserId();
	}

	@Override
	public String readUserIdentifier(HttpServletRequest request) {
		logger.debug("IN");

		Assert.assertNotNull(request, "Input parameter [request] cannot be null");

		String jwtToken = null;

		try {
			String headerName = getHeaderName();
			String attributeName = headerName;

			OptionalFunction getUserIdFromHeader = () -> {
				String userId = request.getHeader(headerName);
				LogMF.debug(logger, "Request header {0} is equal to [{1}]", headerName, userId);
				return Optional.ofNullable(userId);
			};
			OptionalFunction getUserIdFromAttribute = () -> {
				String userId = (String) request.getAttribute(attributeName);
				LogMF.debug(logger, "Request attribute {0} is equal to [{1}]", headerName, userId);
				return Optional.ofNullable(userId);
			};
			OptionalFunction getUserIdFromRemoteUser = () -> {
				String userId = request.getRemoteUser();
				LogMF.debug(logger, "Request remote user is equal to [{0}]", userId);
				return Optional.ofNullable(userId);
			};

			Stream<OptionalFunction> stream = Stream.of(getUserIdFromHeader, getUserIdFromAttribute, getUserIdFromRemoteUser);

			Optional<String> userIdOpt = stream.flatMap(opt -> opt.getUserId().map(Stream::of).orElseGet(Stream::empty)).findFirst();

			if (userIdOpt.isPresent()) {
				String userId = userIdOpt.get();
				jwtToken = getJWTToken(userId);
			} else {
				// in case header / attribute / remote user are not present, defaults to regular JWT SSO system
				jwtToken = super.readUserIdentifier(request);
			}

			LogMF.debug(logger, "OUT: returning [{0}]", jwtToken);
			return jwtToken;
		} catch (Exception t) {
			logger.error("An unpredicted error occurred while reading user identifier", t);
			throw new SpagoBIRuntimeException("An unpredicted error occurred while reading user identifier", t);
		}
	}

	protected String getJWTToken(String userId) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, USER_JWT_TOKEN_EXPIRE_HOURS);
		Date expiresAt = calendar.getTime();
		return JWTSsoService.userId2jwtToken(userId, expiresAt);
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
