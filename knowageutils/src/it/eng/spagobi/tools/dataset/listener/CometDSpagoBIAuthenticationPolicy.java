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
package it.eng.spagobi.tools.dataset.listener;

import java.util.Map;

import org.apache.log4j.Logger;
import org.cometd.bayeux.server.BayeuxContext;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.DefaultSecurityPolicy;

/**
 * Add to web.xml
 *
 * <pre>
 * <filter-mapping>
 *     		<filter-name>SpagoBIAccessFilter</filter-name>
 *     		<url-pattern>/cometd/*</url-pattern>
 * 	</filter-mapping>
 * </pre>
 *
 * @author fabrizio
 *
 */
public class CometDSpagoBIAuthenticationPolicy extends DefaultSecurityPolicy {

	private static final String USER_CHANNEL_PROP_NAME = "userChannel";
	private final static Logger log = Logger.getLogger(CometDSpagoBIAuthenticationPolicy.class);

	@Override
	public boolean canHandshake(BayeuxServer server, ServerSession session, ServerMessage message) {
		if (session.isLocalSession()) {
			return true;
		}

		Map<String, Object> ext = message.getExt();
		if (ext == null) {
			log.warn("Ext authentication object not present");
			return false;
		}
		String channel = (String) ext.get(USER_CHANNEL_PROP_NAME);
		log.warn("User channel not present");
		if (channel == null) {
			return false;
		}

		String userChannel = CometServiceManager.getUserChannel(channel);
		BayeuxContext context = server.getContext();
		String userId = (String) context.getHttpSessionAttribute("userId");
		return userChannel.equals(userId);
	}

}
