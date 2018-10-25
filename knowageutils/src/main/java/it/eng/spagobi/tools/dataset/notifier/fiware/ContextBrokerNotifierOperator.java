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
package it.eng.spagobi.tools.dataset.notifier.fiware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.eng.spagobi.commons.bo.UserProfile;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.listener.DataSetListenerManager;
import it.eng.spagobi.tools.dataset.listener.DataSetListenerManagerFactory;
import it.eng.spagobi.tools.dataset.notifier.INotifierOperator;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class ContextBrokerNotifierOperator implements INotifierOperator {

	private static final Logger log = Logger.getLogger(ContextBrokerNotifierOperator.class);

	private static final String SUBSCRIPTION_ID = "subscriptionId";
	private final String subscriptionId;
	private final UserProfile profile;
	private final String dataSetLabel;
	private final String dataSetSignature;
	private boolean realtimeNgsiConsumer;
	private final DataSetListenerManager manager;
	private final JSONPathDataReader reader;

	public ContextBrokerNotifierOperator(String subscriptionId, UserProfile profile, String dataSetLabel, String dataSetSignature, boolean realtimeNgsiConsumer,
			DataSetListenerManager manager, JSONPathDataReader reader) {
		Helper.checkNotNullNotTrimNotEmpty(subscriptionId, "subscriptionId");
		Helper.checkNotNullNotTrimNotEmpty(dataSetLabel, "dataSetLabel");
		Helper.checkNotNullNotTrimNotEmpty(dataSetSignature, "dataSetSignature");
		Helper.checkNotNull(manager, "manager");
		Helper.checkNotNull(reader, "reader");

		this.subscriptionId = subscriptionId;
		this.profile = profile;
		this.dataSetLabel = dataSetLabel;
		this.dataSetSignature = dataSetSignature;
		this.realtimeNgsiConsumer = realtimeNgsiConsumer;
		this.manager = manager;
		this.reader = reader;

	}

	public ContextBrokerNotifierOperator(String subscriptionId, UserProfile profile, String dataSetLabel, String dataSetSignature, boolean realtimeNgsiConsumer,
			JSONPathDataReader reader) {
		this(subscriptionId, profile, dataSetLabel, dataSetSignature, realtimeNgsiConsumer, DataSetListenerManagerFactory.getManager(), reader);
	}

	private IDataStore getUpdatedOrAddedRecords(HttpServletRequest req, String body) {
		try {
			Helper.checkNotNull(req, "action");

			if (body.isEmpty()) {
				throw new ContextBrokerNotifierException("Request body is empty.");
			}

			JSONObject bodyJSON = getJSON(body);

			if (bodyJSON == null) {
				throw new ContextBrokerNotifierException("Request body is not a valid JSON: " + body);
			}

			if (!bodyJSON.has(SUBSCRIPTION_ID)) {
				throw new ContextBrokerNotifierException("Request body has not " + SUBSCRIPTION_ID + ": " + body);
			}

			String subId = bodyJSON.getString(SUBSCRIPTION_ID);
			if (!subscriptionId.equals(subId)) {
				log.info("Subscription id not equals: " + subId + "!=" + subscriptionId + ", notifier skipped.");
				return null;
			}

			String data = bodyJSON.getJSONArray("data").toString();
			// updated
			return reader.read(data);
		} catch (ContextBrokerNotifierException e) {
			throw e;
		} catch (Exception e) {
			throw new ContextBrokerNotifierException("Error in context broker notification", e);
		}
	}

	private static JSONObject getJSON(String body) throws JSONException {
		try {
			return new JSONObject(body);
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isRealtimeNgsiConsumer() {
		return realtimeNgsiConsumer;
	}

	public void setRealtimeNgsiConsumer(boolean realtimeNgsiConsumer) {
		this.realtimeNgsiConsumer = realtimeNgsiConsumer;
	}

	@Override
	public void notify(HttpServletRequest req, HttpServletResponse resp, String reqBody) {
		// updated and added (there is no disctinction in the notification)
		IDataStore updatedOrAdded = getUpdatedOrAddedRecords(req, reqBody);
		if (updatedOrAdded == null) {
			// different subscription id
			return;
		}
		int idFieldIndex = reader.getIdFieldIndex();
		Assert.assertTrue(idFieldIndex != -1, "idFieldIndex!=-1");
		try {
			manager.changedDataSet(profile, realtimeNgsiConsumer, dataSetLabel, dataSetSignature, updatedOrAdded, idFieldIndex);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

}
