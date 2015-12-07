/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2015 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
