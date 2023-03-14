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

package it.eng.knowage.websocket;

import static it.eng.knowage.websocket.KnowageWebSocket.USER_PROPERTIES_USER_PROFILE;
import static java.util.Objects.nonNull;

import java.util.Collections;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;

public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {

	private static Logger logger = Logger.getLogger(HttpSessionConfigurator.class);

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {

		HttpSession httpSession = (HttpSession) request.getHttpSession();
		UserProfile userProfile = null;

		if (nonNull(httpSession)) {
			userProfile = (UserProfile) httpSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		}

		if (nonNull(userProfile)) {
			sec.getUserProperties().remove(USER_PROPERTIES_USER_PROFILE);
			sec.getUserProperties().put(USER_PROPERTIES_USER_PROFILE, userProfile);
		} else {
			// Reject
			response.getHeaders().put(HandshakeResponse.SEC_WEBSOCKET_ACCEPT, Collections.emptyList());
		}
	}



}