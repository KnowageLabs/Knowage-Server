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
import java.util.HashMap;

import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;
import org.json.JSONException;

@ServerEndpoint(value = "/webSocket", encoders = KnowageWebSocketMessageEncoder.class, decoders = KnowageWebSocketMessageDecoder.class, configurator = HttpSessionConfigurator.class)
public class KnowageWebSocket {

	private static final Logger LOGGER = Logger.getLogger(KnowageWebSocket.class);

	private static NewsFeed newsFeed = new NewsFeed();
	private static AsyncDownloadsFeed asyncDownloadsFeed = new AsyncDownloadsFeed();

	private static final HashMap<Session, SessionData> session2SessionData = new HashMap<>();

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) throws IOException, EncodeException {

		SessionData sessionData = new SessionData(session, config);

		session2SessionData.put(session, sessionData);

		newsFeed.addListener(sessionData);
		asyncDownloadsFeed.addListener(sessionData);

		// Force sync of news
		newsFeed.refresh(sessionData.subscribeForOrganization());
	}

	@OnMessage
	public void onMessage(Session session, String message) throws JSONException {
		SessionData sessionData = session2SessionData.get(session);

		// Force sync of news
		newsFeed.refresh(sessionData.subscribeForOrganization());
	}

	@OnClose
	public void onClose(Session session) {
		handleCloseOrError(session);
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		handleCloseOrError(session);
		LOGGER.error("Web socket handler get following error", throwable);
	}

	private void handleCloseOrError(Session session) {
		if (!session.isOpen()) {
			SessionData sessionData = session2SessionData.get(session);
			newsFeed.removeListener(sessionData);
			asyncDownloadsFeed.removeListener(sessionData);
		}
	}

}