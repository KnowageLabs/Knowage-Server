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

import java.util.Collections;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.log4j.Logger;

public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {

	private static transient Logger logger = Logger.getLogger(HttpSessionConfigurator.class);

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		logger.debug("modifyHandshake() Current thread " + Thread.currentThread().getName());

		Object httpSession = request.getHttpSession();

		if (httpSession != null) {
			sec.getUserProperties().remove("HTTP_SESSION");
			sec.getUserProperties().put("HTTP_SESSION", httpSession);
		} else {
			// Reject
			response.getHeaders().put(HandshakeResponse.SEC_WEBSOCKET_ACCEPT, Collections.emptyList());
		}
	}

}