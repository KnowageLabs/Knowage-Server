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

import javax.servlet.http.HttpSession;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.resource.export.Utilities;
import it.eng.spagobi.tools.news.bo.BasicNews;
import it.eng.spagobi.tools.news.dao.ISbiNewsDAO;
import it.eng.spagobi.tools.news.dao.ISbiNewsReadDAO;
import it.eng.spagobi.user.UserProfileManager;

@ServerEndpoint(value = "/webSocket", encoders = KnowageWebSocketMessageEncoder.class, configurator = HttpSessionConfigurator.class)

public class KnowageWebSocket {

	private static final Logger logger = Logger.getLogger(KnowageWebSocket.class);

	private Session session;
	private static Map<String, String> users = new HashMap<>();
	private HttpSession httpSession;

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) throws IOException, EncodeException {

		this.httpSession = (HttpSession) config.getUserProperties().get("HTTP_SESSION");

		initializeUserProfileAndTenant();

		JSONObject masterJsonObject = new JSONObject();

		try {
			masterJsonObject.put("news", handleNews());
			masterJsonObject.put("downloads", handleDownloads());
		} catch (JSONException e) {
			String message = "Error while creating JSON message";
			logger.error(message);
		}

		if (session.isOpen())
			session.getBasicRemote().sendObject(masterJsonObject);
	}

	private void initializeUserProfileAndTenant() {
		IEngUserProfile userProfile = UserProfileManager.getProfile();

		if (userProfile == null) {
			userProfile = (IEngUserProfile) this.httpSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

			UserProfileManager.setProfile((UserProfile) userProfile);

			String tenantId = ((UserProfile) userProfile).getOrganization();
			LogMF.debug(logger, "Tenant identifier is [{0}]", tenantId);
			// putting tenant id on thread local
			Tenant tenant = new Tenant(tenantId);
			TenantManager.setTenant(tenant);
		}

	}

	private JSONObject handleDownloads() {
		Utilities exportResourceUtilities = new Utilities();

		int count = 0;
		JSONObject downloads = null;
		try {
			count = exportResourceUtilities.countAllExportedFiles(true);
			downloads = new JSONObject();
			downloads.put("count", count);

		} catch (JSONException e) {
			String message = "Error while creating download JSON message";
			logger.error(message);
		} catch (IOException e1) {
			String message = "Error while searching exported datasets";
			logger.error(message);
		}

		return downloads;
	}

	private JSONObject handleNews() {
		UserProfile userProfile = UserProfileManager.getProfile();

		ISbiNewsDAO newsDao = DAOFactory.getSbiNewsDAO();
		List<BasicNews> allNews = newsDao.getAllNews(userProfile);

		ISbiNewsReadDAO newsReadDao = DAOFactory.getSbiNewsReadDAO();
		List<Integer> readNews = newsReadDao.getReadNews(userProfile);

		int count = 0;
		for (BasicNews basicNews : allNews) {
			if (!readNews.contains(basicNews.getId()))
				count++;
		}

		JSONObject news = new JSONObject();
		try {
			news.put("count", count);
		} catch (JSONException e) {
			String message = "Error while creating news JSON message";
			logger.error(message);
		}

		return news;
	}

	@OnClose
	public void onClose(Session session) throws IOException, EncodeException {

		JSONObject disconnectedJSON = new JSONObject();
		try {
			disconnectedJSON.put("disconnected", true);
			session.getBasicRemote().sendObject(disconnectedJSON);
		} catch (JSONException e) {
			String message = "Error while creating closing JSON message";
			logger.error(message);
		}
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
	}

}