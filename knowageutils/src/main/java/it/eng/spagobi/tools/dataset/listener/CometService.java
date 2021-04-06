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

import org.cometd.bayeux.Promise;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ConfigurableServerChannel;
import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.server.AbstractService;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;

public class CometService extends AbstractService {

	private final static CockpitJSONDataWriter writer = new CockpitJSONDataWriter();

	private final String channelName;

	public CometService(BayeuxServer bayeux, String name, String channelName) {
		super(bayeux, name);
		this.channelName = channelName;
	}

	public void dataStoreChanged(DataStoreChangedEvent event) {
		try {
			ServerChannel channel = getChannel();

			LocalSession session = getLocalSession();
			String json = getJSON(event);
			channel.publish(session, json, Promise.noop());
		} catch (Exception e) {
			throw new CometServiceException(e);
		}
	}

	private ServerChannel getChannel() {
		// Initialize the channel, making it persistent and lazy
		BayeuxServer server = getBayeux();
		server.createChannelIfAbsent(channelName, new ConfigurableServerChannel.Initializer() {
			@Override
			public void configureChannel(ConfigurableServerChannel channel) {
				channel.setPersistent(true);
			}
		});

		// Publish to all subscribers on that channel
		ServerChannel channel = server.getChannel(channelName);
		return channel;
	}

	protected String getJSON(DataStoreChangedEvent event) throws JSONException {
		JSONObject res = new JSONObject();
		res.put("dataStore", writer.write(event.getDataStore()));
		res.put("isFoundInCache", event.isFoundInCache());
		return res.toString();
	}
}
