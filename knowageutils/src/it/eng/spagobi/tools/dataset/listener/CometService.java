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

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;

import java.util.List;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ConfigurableServerChannel;
import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.server.AbstractService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CometService extends AbstractService {

	private final static JSONDataWriter writer = new JSONDataWriter();
	
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
			channel.publish(session, json);
		} catch (JSONException e) {
			throw new CometServiceException(e);
		} catch (Exception e) {
			throw new CometServiceException(e);
		}
	}

	private ServerChannel getChannel() {
		// Initialize the channel, making it persistent and lazy
		BayeuxServer server = getBayeux();
		server.createChannelIfAbsent(channelName, new ConfigurableServerChannel.Initializer() {
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
		res.put("deleted", getJSONRecords(event.getDeleted(), event.getPreviousStore()));
		res.put("added", getJSONRecords(event.getAdded(), event.getCurrentStore()));
		res.put("updated", getJSONRecords(event.getUpdated(), event.getCurrentStore()));
		res.put("isChanged", event.isChanged());
		return res.toString();
	}

	private static JSONArray getJSONRecords(List<IRecord> recs, IDataStore dataStore) throws JSONException {
		JSONArray res = new JSONArray();
		for (IRecord rec : recs) {
			res.put(writer.writeRecord(dataStore, rec));
		}
		return res;
	}

}
