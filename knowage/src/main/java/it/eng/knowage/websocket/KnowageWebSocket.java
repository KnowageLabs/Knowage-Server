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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpSession;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.v2.export.Entry;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.resource.export.Utilities;
import it.eng.spagobi.tools.news.dao.ISbiNewsReadDAO;
import it.eng.spagobi.tools.news.manager.INewsManager;
import it.eng.spagobi.tools.news.manager.NewsManagerImpl;
import it.eng.spagobi.user.UserProfileManager;

@ServerEndpoint(value = "/webSocket/{login}", encoders = KnowageWebSocketMessageEncoder.class, decoders = KnowageWebSocketMessageDecoder.class, configurator = HttpSessionConfigurator.class)
public class KnowageWebSocket {

	private static final Logger LOGGER = Logger.getLogger(KnowageWebSocket.class);

	private Session session;
	private static Map<Object, Session> userSession = new HashMap<>();
	private HttpSession httpSession;

	private static Map<Object, KnowageWebSocket> userWebSockets = new HashMap<>();
	private static CopyOnWriteArraySet<KnowageWebSocket> webSocketSet = new CopyOnWriteArraySet<>();

	@OnOpen
	public void onOpen(@PathParam("login") boolean login, Session session, EndpointConfig config) throws IOException, EncodeException {

		try {
			webSocketSet.add(this);
			this.session = session;

			this.httpSession = (HttpSession) config.getUserProperties().get("HTTP_SESSION");

			if (login) {
				JSONObject masterJsonObject = getMasterMessageObject();
				broadcast(masterJsonObject);
			}
		} catch (Exception e) {
			LOGGER.error("Error opening the web socket for notifications", e);
		}
	}

	private JSONObject getMasterMessageObject() {
		return getMasterMessageObject(true, true);
	}

	private JSONObject getMasterMessageObject(boolean news, boolean downloads) {
		JSONObject masterJsonObject = new JSONObject();

		initializeUserProfileAndTenant();
		try {
			if (news)
				masterJsonObject.put("news", handleNews());

			if (downloads)
				masterJsonObject.put("downloads", handleDownloads());
		} catch (JSONException e) {
			String message = "Error while creating JSON message";
			LOGGER.error(message);
		}
		return masterJsonObject;
	}

	private void initializeUserProfileAndTenant() {
		IEngUserProfile userProfile = UserProfileManager.getProfile();

		if (userProfile == null) {
			userProfile = (IEngUserProfile) this.httpSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

			UserProfileManager.setProfile((UserProfile) userProfile);

			String tenantId = ((UserProfile) userProfile).getOrganization();
			LogMF.debug(LOGGER, "Tenant identifier is [{0}]", tenantId);
			// putting tenant id on thread local
			Tenant tenant = new Tenant(tenantId);
			TenantManager.setTenant(tenant);

			Object userUniqueIdentifier = userProfile.getUserUniqueIdentifier();
			userSession.computeIfAbsent(userUniqueIdentifier, k -> session);
			userWebSockets.computeIfAbsent(userUniqueIdentifier, k -> this);
		}
	}

	private JSONObject handleDownloads() {
		Utilities exportResourceUtilities = new Utilities();

		int total = 0;
		long alreadyDownloaded = 0;
		JSONObject downloads = null;
		try {
			List<Entry> exportedFilesList = exportResourceUtilities.getAllExportedFiles(true);
			total = exportedFilesList.size();
			alreadyDownloaded = exportedFilesList.stream().filter(Entry::isAlreadyDownloaded).count();
			downloads = new JSONObject();

			JSONObject countJSONObject = new JSONObject();

			countJSONObject.put("total", total);
			countJSONObject.put("alreadyDownloaded", alreadyDownloaded);

			downloads.put("count", countJSONObject);

		} catch (JSONException e) {
			String message = "Error while creating download JSON message";
			LOGGER.error(message);
		} catch (IOException e1) {
			String message = "Error while searching exported datasets";
			LOGGER.error(message);
		}

		return downloads;
	}

	private JSONObject handleNews() {
		UserProfile userProfile = UserProfileManager.getProfile();

		INewsManager newsManager = new NewsManagerImpl();
		int total = newsManager.getAllNews(userProfile).size();

		ISbiNewsReadDAO newsReadDao = DAOFactory.getSbiNewsReadDAO();
		newsReadDao.setUserProfile(userProfile);
		List<Integer> listOfReads = newsReadDao.getReadNews(userProfile);
		int unread = total - listOfReads.size();

		JSONObject news = new JSONObject();
		try {
			JSONObject countJSONObject = new JSONObject();

			countJSONObject.put("total", total);
			countJSONObject.put("unread", unread);

			news.put("count", countJSONObject);
		} catch (JSONException e) {
			String message = "Error while creating news JSON message";
			LOGGER.error(message);
		}

		return news;
	}

	@OnMessage
	public void onMessage(Session session, String message) throws IOException, EncodeException, JSONException {

		JSONObject messageJSON = new JSONObject(message);

		broadcast(getMasterMessageObject(messageJSON.has("news"), messageJSON.has("downloads")));
	}

	private void broadcast(JSONObject message) throws IOException, EncodeException {

		webSocketSet.forEach(x -> {

			try {
				x.session.getBasicRemote().sendObject(message);
			} catch (IOException | EncodeException e) {
				LOGGER.error("Error during broadcasting", e);
			}
		});

	}

	@OnClose
	public void onClose(Session session) {

		webSocketSet.remove(this);

	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		LOGGER.error("Web socket handler get following error", throwable);
	}

}