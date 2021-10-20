/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import it.eng.knowage.websocket.bo.WSDownloadBO;
import it.eng.knowage.websocket.bo.WSDownloadCountBO;
import it.eng.knowage.websocket.bo.WSNewsBO;
import it.eng.knowage.websocket.bo.WSNewsCountBO;
import it.eng.knowage.websocket.bo.WebSocketBO;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;

/**
 * @author Marco Libanori
 */
class SessionData implements NewsListener, AsyncDownloadsListener {
	private static final Logger LOGGER = Logger.getLogger(SessionData.class);

	private final Session session;
	private final HttpSession httpSession;
	private final UserProfile userProfile;
	private final WebSocketBO webSocketBO = new WebSocketBO();
	private final String username;
	private final Set<String> roles = new TreeSet<>();
	private final String tenantId;

	public SessionData(Session session, EndpointConfig config) {
		this.session = session;
		this.httpSession = (HttpSession) config.getUserProperties().get("HTTP_SESSION");
		this.userProfile = (UserProfile) httpSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		this.tenantId = userProfile.getOrganization();
		this.username = userProfile.getUserId().toString();
		try {
			this.roles.addAll(userProfile.getRoles());
		} catch (EMFInternalError e) {
			LOGGER.error("Error getting roles", e);
		}
	}

	@Override
	public Session getSession() {
		return session;
	}

	@Override
	public UserProfile getUserProfile() {
		return userProfile;
	}

	@Override
	public void listenForNews(int total, int unread) {
		WSNewsBO news = webSocketBO.getNews();
		WSNewsCountBO count = news.getCount();

		count.setTotal(total);
		count.setUnread(unread);

		sendStatus();
	}

	@Override
	public void listenForDownload(int total, int alreadyDownloaded) {
		WSDownloadBO downloads = webSocketBO.getDownloads();
		WSDownloadCountBO count = downloads.getCount();

		count.setTotal(total);
		count.setAlreadyDownloaded(alreadyDownloaded);

		sendStatus();
	}

	@Override
	public String subscribeForUser() {
		return username;
	}

	@Override
	public Set<String> subscribeForRoles() {
		return roles;
	}

	@Override
	public String subscribeForOrganization() {
		return tenantId;
	}

	private void sendStatus() {
		if (getSession().isOpen()) {
			try {
				getSession().getBasicRemote().sendObject(webSocketBO);
			} catch (IOException | EncodeException e) {
				LOGGER.error("Error sending message to web socket", e);
			}
		}
	}

}
