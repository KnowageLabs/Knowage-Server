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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.websocket.bo.WSDownloadBO;
import it.eng.knowage.websocket.bo.WSDownloadCountBO;
import it.eng.knowage.websocket.bo.WSNewsBO;
import it.eng.knowage.websocket.bo.WSNewsCountBO;
import it.eng.knowage.websocket.bo.WebSocketBO;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.v2.export.Entry;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.resource.export.Utilities;
import it.eng.spagobi.tools.news.bo.AdvancedNews;
import it.eng.spagobi.tools.news.bo.BasicNews;
import it.eng.spagobi.tools.news.dao.ISbiNewsDAO;
import it.eng.spagobi.tools.news.dao.ISbiNewsReadDAO;
import it.eng.spagobi.tools.news.manager.INewsManager;
import it.eng.spagobi.tools.news.manager.NewsManagerImpl;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@ServerEndpoint(value = "/webSocket", encoders = KnowageWebSocketMessageEncoder.class, decoders = KnowageWebSocketMessageDecoder.class, configurator = HttpSessionConfigurator.class)
public class KnowageWebSocket {

	private static final Logger LOGGER = Logger.getLogger(KnowageWebSocket.class);

	private static CopyOnWriteArraySet<Session> webSocketSet = new CopyOnWriteArraySet<>();
	private static final HashMap<Session, HashMap<String, Object>> sessionToHttpMap = new HashMap<Session, HashMap<String, Object>>();
	final ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) throws IOException, EncodeException {

		webSocketSet.add(session);

		HttpSession httpSession = (HttpSession) config.getUserProperties().get("HTTP_SESSION");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("httpSession", httpSession);

		Runnable downloadPollingTask = () -> {
			WebSocketBO masterWebSocketBO = getMasterMessageObject(session, false, true);
			try {
				broadcastDownload(session, masterWebSocketBO);
			} catch (IOException | EncodeException e) {
				UserProfile userProfile = (UserProfile) getProfileFromSession(session);
				LOGGER.debug(
						"Task for download handling: user_id:[" + userProfile.getUserUniqueIdentifier() + "], tenant:[" + userProfile.getOrganization() + "]");
			}
		};
		ScheduledFuture<?> scheduledFuture = ses.scheduleAtFixedRate(downloadPollingTask, 0, 1, TimeUnit.MINUTES);
		map.put("downloadPollingThread", scheduledFuture);

		sessionToHttpMap.put(session, map);

		WebSocketBO masterWebSocketBO = getMasterMessageObject(session, true, true);
		broadcastNews(session, masterWebSocketBO);
		broadcastDownload(session, masterWebSocketBO);

	}

	@OnMessage
	public void onMessage(Session session, String message) throws IOException, EncodeException, JSONException, EMFInternalError {
		JSONObject obj = new JSONObject(message);

		broadcastNews(session, obj);

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
			webSocketSet.remove(session);
			((ScheduledFuture<?>) sessionToHttpMap.get(session).get("downloadPollingThread")).cancel(true);
			sessionToHttpMap.remove(session);
		}
	}

	private WebSocketBO getMasterMessageObject(Session session, boolean news, boolean downloads) {
		WebSocketBO masterWebSocketBO = new WebSocketBO();

		initializeUserProfileAndTenant(session);

		if (news)
			handleNews(session, masterWebSocketBO);

		if (downloads)
			handleDownloads(masterWebSocketBO);

		return masterWebSocketBO;
	}

	private void initializeUserProfileAndTenant(Session session) {

		IEngUserProfile userProfile = getProfileFromSession(session);

		UserProfileManager.setProfile((UserProfile) userProfile);

		String tenantId = ((UserProfile) userProfile).getOrganization();
		LogMF.debug(LOGGER, "Tenant identifier is [{0}]", tenantId);
		Tenant tenant = new Tenant(tenantId);
		TenantManager.setTenant(tenant);

	}

	private WebSocketBO handleDownloads(WebSocketBO masterWebSocketBO) {
		Utilities exportResourceUtilities = new Utilities();

		int total = 0;
		long alreadyDownloaded = 0;
		try {
			List<Entry> exportedFilesList = exportResourceUtilities.getAllExportedFiles(true);
			total = exportedFilesList.size();
			alreadyDownloaded = exportedFilesList.stream().filter(Entry::isAlreadyDownloaded).count();

			WSDownloadBO downloads = masterWebSocketBO.getDownloads();
			if (downloads == null)
				downloads = new WSDownloadBO(new WSDownloadCountBO(0, 0));

			WSDownloadCountBO wsDownloadCountBO = new WSDownloadCountBO(total, alreadyDownloaded);

			downloads.setCount(wsDownloadCountBO);

			masterWebSocketBO.setDownloads(downloads);

		} catch (IOException e1) {
			String message = "Error while searching exported datasets";
			LOGGER.error(message);
		}

		return masterWebSocketBO;
	}

	private WebSocketBO handleNews(Session session, WebSocketBO masterWebSocketBO) {
		UserProfile userProfile = (UserProfile) getProfileFromSession(session);

		INewsManager newsManager = new NewsManagerImpl();
		ISbiNewsDAO sbiNewsDAO = DAOFactory.getSbiNewsDAO();
		List<BasicNews> allNewsList = newsManager.getAllNews(userProfile);
		int total = 0;
		for (BasicNews basicNews : allNewsList) {
			AdvancedNews news = sbiNewsDAO.getNewsById(basicNews.getId(), userProfile);

			if (news.getActive() && news.getExpirationDate().after(new Date()))
				total++;
		}

		WSNewsBO news = masterWebSocketBO.getNews();
		if (news == null)
			news = new WSNewsBO(new WSNewsCountBO(0, 0));

		if (total > 0) {
			ISbiNewsReadDAO newsReadDao = DAOFactory.getSbiNewsReadDAO();
			newsReadDao.setUserProfile(userProfile);
			List<Integer> listOfReads = newsReadDao.getReadNews(userProfile);
			int unread = total - listOfReads.size();

			WSNewsCountBO wsNewsCountBO = new WSNewsCountBO(total, unread);

			news.setCount(wsNewsCountBO);

		}
		masterWebSocketBO.setNews(news);

		return masterWebSocketBO;
	}

	private IEngUserProfile getProfileFromSession(Session session) {
		HttpSession httpSession = (HttpSession) sessionToHttpMap.get(session).get("httpSession");
		IEngUserProfile userProfile = (IEngUserProfile) httpSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		return userProfile;
	}

	private void broadcastDownload(Session session, WebSocketBO webSocketBO) throws IOException, EncodeException {
		session.getBasicRemote().sendObject(webSocketBO);
	}

	private void broadcastNews(Session session, WebSocketBO webSocketBO) throws IOException, EncodeException {
		session.getBasicRemote().sendObject(webSocketBO);
	}

	private void broadcastNews(Session session, JSONObject obj) {

		webSocketSet.forEach(x -> {
			try {
//				IEngUserProfile userProfile = getProfileFromSession(x);
//				Collection userRoles = userProfile.getRoles();
//
//				JSONArray roles = obj.getJSONArray("roles");
//				Set<String> rolesSet = new HashSet<String>();
//				for (int i = 0; i < roles.length(); i++) {
//					JSONObject role = (JSONObject) roles.get(i);
//					rolesSet.add((String) role.get("name"));
//				}
//
//				for (Object object : userRoles) {
//					String role = (String) object;
//
//					if (rolesSet.contains(role)) {
				WebSocketBO webSocketBO = getMasterMessageObject(x, true, false);
				x.getBasicRemote().sendObject(webSocketBO);
//						break;
//					}
//				}
//
//			} catch (EMFInternalError | JSONException | IOException | EncodeException e) {
//				throw new SpagoBIRuntimeException(e);
//			}
			} catch (IOException | EncodeException e) {
				throw new SpagoBIRuntimeException(e);
			}
		});

	}

}