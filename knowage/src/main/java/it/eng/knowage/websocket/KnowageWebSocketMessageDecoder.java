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

import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.knowage.websocket.bo.WebSocketBO;

public class KnowageWebSocketMessageDecoder implements Decoder.Text<WebSocketBO> {
	private static final Logger logger = Logger.getLogger(KnowageWebSocketMessageDecoder.class);

	@Override
	public WebSocketBO decode(String s) throws DecodeException {

		ObjectMapper mapper = new ObjectMapper();
		WebSocketBO json = null;
		try {
			json = mapper.readValue(s, WebSocketBO.class);
		} catch (JsonParseException e) {
			String message = "Error during decoding KnowageWebSocketMessage";
			logger.error(message);
		} catch (JsonMappingException e) {
			String message = "Error during decoding KnowageWebSocketMessage";
			logger.error(message);
		} catch (IOException e) {
			String message = "Error during decoding KnowageWebSocketMessage";
			logger.error(message);
		}

		return json;
	}

	@Override
	public boolean willDecode(String s) {
		return (s != null);
	}

	@Override
	public void init(EndpointConfig endpointConfig) {
		// Custom initialization logic
	}

	@Override
	public void destroy() {
		// Close resources
	}
}