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
package it.eng.spagobi.services.oauth2;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.services.common.JWTSsoService;

/**
 * This SSO class retrieves the OAuth2 access token plus some other extra request parameters into an unique JWT token. This is useful in case, when access token
 * is being used to query an external profile manager, some other parameters are needed to retrieve the user information from the profile manager.
 *
 * @author Davide Zerbetto
 *
 */
public class Oauth2TokenPlusExtraParametersSsoService extends Oauth2SsoService {

	private static Logger logger = Logger.getLogger(Oauth2TokenPlusExtraParametersSsoService.class);

	public static final String EXTRA_PARAMETERS_NAMES_CONFIG_KEY = "oauth2_extra_parameters_names";

	@Override
	public String readUserIdentifier(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String accessToken = (String) session.getAttribute(Oauth2SsoService.ACCESS_TOKEN);
		if (accessToken == null) {
			logger.debug("Access token not found.");
			return super.readUserIdentifier(request);
		}
		LogMF.debug(logger, "Access token found: [{0}]", accessToken);

		List<String> extraParametersNames = getExtraParametersNames();

		Map<String, String> extraParametersValues = getExtraParametersValue(request, extraParametersNames);

		extraParametersValues.put(Oauth2SsoService.ACCESS_TOKEN, accessToken);

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, 1); // token will last 1 hour
		Date expiresAt = calendar.getTime();

		String json = JWTSsoService.map2jwtToken(extraParametersValues, expiresAt);

		LogMF.debug(logger, "OUT: returning [{0}]", json);
		return json;
	}

	public static List<String> getExtraParametersNames() {
		logger.debug("IN");
		String extraParametersName = Optional
				.ofNullable(System.getProperty(EXTRA_PARAMETERS_NAMES_CONFIG_KEY, System.getenv(EXTRA_PARAMETERS_NAMES_CONFIG_KEY.toUpperCase())))
				.orElseThrow(() -> new RuntimeException("Missing " + EXTRA_PARAMETERS_NAMES_CONFIG_KEY + " configuration!!"));
		List<String> toReturn = Arrays.asList(extraParametersName.split(" "));
		LogMF.debug(logger, "OUT: extra parameters' names list is {0}", toReturn);
		return toReturn;
	}

	protected Map<String, String> getExtraParametersValue(HttpServletRequest request, List<String> extraParametersNames) {
		LogMF.debug(logger, "IN: input parameters' names list is {0}", extraParametersNames);

		// @formatter:off
		Map<String, String> toReturn = extraParametersNames.stream()
				.filter(parameter -> request.getParameter(parameter) != null)
				.collect(
					Collectors.toMap(
						Function.identity(),
						request::getParameter
		));
		// @formatter:on

		LogMF.debug(logger, "OUT: extra parameters name/value map is {0}", toReturn);
		return toReturn;
	}

}
