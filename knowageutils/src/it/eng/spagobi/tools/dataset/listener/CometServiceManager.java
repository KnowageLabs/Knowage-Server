/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2015 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.listener;

import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.cometd.bayeux.server.BayeuxServer;

public class CometServiceManager {

	private final static Logger log = LogManager.getLogger(CometServiceManager.class);

	private final Map<String, Map<String, Map<String, CometService>>> servicesByIdByLabelByUUId = new HashMap<String, Map<String, Map<String, CometService>>>();

	public void dataStoreChanged(String uuid, String dataSetLabel, DataStoreChangedEvent event,String serviceId) {
		Helper.checkNotNull(event, "event");
		Helper.checkNotNullNotTrimNotEmpty(uuid, "uuid");
		Helper.checkNotNullNotTrimNotEmpty(dataSetLabel, "dataSetLabel");
		Helper.checkNotNullNotTrimNotEmpty(serviceId, "serviceId");
		
		addServiceIfAbsent(uuid, dataSetLabel,serviceId);

		// can be null
		Map<String, CometService> services = getService(uuid, dataSetLabel);
		if (services == null) {
			return;
		}

		for (CometService service : services.values()) {
			try {
				service.dataStoreChanged(event);
			} catch (Exception e) {
				log.error("Error while sending the data store event", e);
			}
		}
	}

	private synchronized Map<String, CometService> getService(String uuid, String dataSetLabel) {
		Map<String, Map<String, CometService>> m = servicesByIdByLabelByUUId.get(uuid);
		if (m == null) {
			return null;
		}

		return m.get(dataSetLabel);
	}

	private synchronized void addServiceIfAbsent(String uuid, String dataSetLabel, String id) {
		Map<String, Map<String, CometService>> a = servicesByIdByLabelByUUId.get(uuid);
		if (a == null) {
			a = new HashMap<String, Map<String, CometService>>();
			servicesByIdByLabelByUUId.put(uuid, a);
		}

		Map<String, CometService> b = a.get(dataSetLabel);
		if (b == null) {
			b = new HashMap<String, CometService>();
			a.put(dataSetLabel, b);
		}

		CometService c = b.get(id);
		if (c == null) {
			c = new CometService(getBayeuxServer(), "dataStoreCometservice", getDataSetChannelName(uuid, dataSetLabel, id));
			b.put(id, c);
		}

	}

	private BayeuxServer getBayeuxServer() {
		return CometDInitializer.getServer();
	}

	/**
	 * from channel name: /{user id}/dataset/{dataset label}/{id} return {user id}
	 * 
	 * @param channel
	 * @return
	 */
	public static String getUserChannel(String channel) {
		if (channel.length() < 3) {
			throwChannelNotValid(channel);
		}
		if (channel.charAt(0) != '/') {
			throwChannelNotValid(channel);
		}
		int indexSecond = channel.indexOf('/', 1);
		if (indexSecond == -1) {
			throwChannelNotValid(channel);
		}
		return channel.substring(1, indexSecond);
	}

	private static void throwChannelNotValid(String channel) {
		throw new SpagoBIRuntimeException("Channel not valid: " + channel);
	}

	private static String getDataSetChannelName(String uuid, String dataSetLabel, String id) {
		return "/" + uuid + "/dataset/" + dataSetLabel + "/" + id;
	}
}
