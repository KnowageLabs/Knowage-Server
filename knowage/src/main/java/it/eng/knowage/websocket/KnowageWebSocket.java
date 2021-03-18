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
import java.util.Map.Entry;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.tools.news.bo.BasicNews;
import it.eng.spagobi.tools.news.dao.ISbiNewsDAO;
import it.eng.spagobi.tools.news.dao.ISbiNewsReadDAO;
import it.eng.spagobi.user.UserProfileManager;

@ServerEndpoint(value = "/webSocket/{userId}", encoders = { KnowageWebSocketMessageEncoder.class }, decoders = { KnowageWebSocketMessageDecoder.class })

public class KnowageWebSocket {

	private Session session;
	private static Map<String, String> users = new HashMap<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("userId") String userId) throws IOException, EncodeException {

		if (!users.containsKey(userId)) {
			this.session = session;
			users.put(userId, session.getId());
		}

		KnowageWebSocketMessage message = new KnowageWebSocketMessage();
		message.setFrom(userId);

		newsUpdate(userId, message);

		session.getBasicRemote().sendObject(message);
	}

	private void newsUpdate(String userId, KnowageWebSocketMessage message) {
		SbiUser user = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);

		UserProfile userProfile = UserProfileManager.getProfile();
		if (userProfile == null) {
			userProfile = new UserProfile(userId, user.getCommonInfo().getOrganization());
			UserProfileManager.setProfile(userProfile);
		}
		ISbiNewsDAO newsDao = DAOFactory.getSbiNewsDAO();
		List<BasicNews> allNews = newsDao.getAllNews(userProfile);

		ISbiNewsReadDAO newsReadDao = DAOFactory.getSbiNewsReadDAO();
		List<Integer> readNews = newsReadDao.getReadNews(userProfile);

		int count = 0;
		for (BasicNews basicNews : allNews) {
			if (!readNews.contains(basicNews.getId()))
				count++;
		}
		Map<String, Integer> newsCountMap = new HashMap<String, Integer>();
		newsCountMap.put("newsCount", count);
		message.setContent(newsCountMap);
	}

	public <K, V> K getKey(Map<K, V> map, V value) {
		for (Entry<K, V> entry : map.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		return null;
	}

	@OnClose
	public void onClose(Session session) throws IOException, EncodeException {

		KnowageWebSocketMessage message = new KnowageWebSocketMessage();
		message.setFrom(users.get(session.getId()));
		message.setContent("Disconnected!");
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
	}

}