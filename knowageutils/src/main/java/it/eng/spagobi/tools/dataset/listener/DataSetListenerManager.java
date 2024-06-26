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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.eng.spagobi.commons.bo.UserProfile;
import org.apache.log4j.Logger;

import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.client.CacheClient;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;

public class DataSetListenerManager {

	private static Logger log = Logger.getLogger(DataSetListenerManager.class);

	/**
	 * Operator by DataSet Label by User Unique Identifiers
	 */
	private final Map<String, Map<String, DataStoreListenerOperator>> operatorsyByLabelByUUId = new HashMap<String, Map<String, DataStoreListenerOperator>>();

	public synchronized void addIDataSetListenerIfAbsent(String uuid, String dataSetLabel, IDataSetListener listener, String listenerId) {
		Helper.checkNotNullNotTrimNotEmpty(uuid, "uuid");
		Helper.checkNotNullNotTrimNotEmpty(dataSetLabel, "dataSetLabel");
		Helper.checkNotNull(listener, "listener");
		Helper.checkNotNullNotTrimNotEmpty(listenerId, "listenerId");

		DataStoreListenerOperator op = getOperator(uuid, dataSetLabel);
		op.addIDataSetListenerIfAbsent(listener, listenerId);
	}

	public boolean removeIDataSetListener(String uuid, String dataSetLabel, IDataSetListener listener, String listenerId) {
		Helper.checkNotNullNotTrimNotEmpty(uuid, "uuid");
		Helper.checkNotNullNotTrimNotEmpty(dataSetLabel, "dataSetLabel");
		Helper.checkNotNull(listener, "listener");
		Helper.checkNotNullNotTrimNotEmpty(listenerId, "listenerId");

		DataStoreListenerOperator op = getOperator(uuid, dataSetLabel);
		return op.removeIDataSetListener(listenerId);
	}

	public synchronized boolean removeUser(String uuid) {
		Helper.checkNotNullNotTrimNotEmpty(uuid, "uuid");

		return operatorsyByLabelByUUId.remove(uuid) != null;
	}

	public synchronized boolean removeDataSetLabel(String uuid, String dataSetLabel) {
		Helper.checkNotNullNotTrimNotEmpty(uuid, "uuid");
		Helper.checkNotNullNotTrimNotEmpty(dataSetLabel, "dataSetLabel");

		Map<String, DataStoreListenerOperator> ops = operatorsyByLabelByUUId.get(uuid);
		if (ops == null) {
			return false;
		}

		return ops.remove(dataSetLabel) != null;
	}

	public void changedDataSet(String uuid, String dataSetLabel, IDataSet currDataSet) {
		Helper.checkNotNullNotTrimNotEmpty(uuid, "uuid");
		Helper.checkNotNullNotTrimNotEmpty(dataSetLabel, "dataSetLabel");
		Helper.checkNotNull(currDataSet, "currDataSet");

		DataStoreListenerOperator op = getOperator(uuid, dataSetLabel);
		List<ListenerResult> res = op.changedDataSet(currDataSet);
		manageListenerResult(res);
	}

	private synchronized DataStoreListenerOperator getOperator(String uuid, String dataSetLabel) {
		Map<String, DataStoreListenerOperator> operatorsByLabel = operatorsyByLabelByUUId.get(uuid);
		if (operatorsByLabel == null) {
			operatorsByLabel = new HashMap<String, DataStoreListenerOperator>();
			operatorsyByLabelByUUId.put(uuid, operatorsByLabel);
		}

		DataStoreListenerOperator op = operatorsByLabel.get(dataSetLabel);
		if (op == null) {
			op = new DataStoreListenerOperator();
			operatorsByLabel.put(dataSetLabel, op);
		}
		return op;
	}

	private static void manageListenerResult(List<ListenerResult> res) {
		for (ListenerResult lr : res) {
			if (lr.isException()) {
				log.error("Error while calling dataset listener", lr.getException());
			}
		}
	}

	public void changedDataSet(UserProfile profile, boolean realtimeNgsiConsumer, String dataSetLabel, String dataSetSignature, IDataStore updatedOrAdded,
							   int idFieldIndex) throws Exception {
		Helper.checkNotNullNotTrimNotEmpty(dataSetLabel, "dataSetLabel");
		Helper.checkNotNullNotTrimNotEmpty(dataSetSignature, "dataSetSignature");
		Helper.checkNotNull(updatedOrAdded, "updatedOrAdded");
		Helper.checkNotNegative(idFieldIndex, "idFieldIndex");

		IDataStore dataStore = updateDataSetInCache(profile, dataSetLabel, dataSetSignature, updatedOrAdded, realtimeNgsiConsumer);
		if (realtimeNgsiConsumer) {
			Assert.assertNotNull(dataStore, "Datastore from cache cannot be null");
			changedDataSet(profile.getUserId().toString(), dataSetLabel, dataStore, idFieldIndex);
		}
	}

	private IDataStore updateDataSetInCache(UserProfile profile, String dataSetLabel, String dataSetSignature, IDataStore updatedOrAdded, boolean realtimeNgsiConsumer)
			throws Exception {
		String userId = profile.getUserId().toString();
		DataStoreListenerOperator op = getOperator(userId, dataSetLabel);
		CacheClient client = new CacheClient();
		IDataStore result = client.updateDataSet(dataSetSignature, updatedOrAdded, realtimeNgsiConsumer, profile.getUserUniqueIdentifier().toString());
		if (result == null) {
			log.debug("Impossible to apply updates. Error while executing dataset with label [" + op.getDataSet().getLabel() + "] in cache for user [" + userId
					+ "]");
		}
		return result;
	}

	private void changedDataSet(String uuid, String dataSetLabel, IDataStore updatedOrAdded, int idFieldIndex) {
		DataStoreListenerOperator op = getOperator(uuid, dataSetLabel);

		List<ListenerResult> res = op.changedDataSet(updatedOrAdded);
		manageListenerResult(res);
	}

	/**
	 * If it's not initialized then it does nothing.
	 *
	 * @param uuid
	 * @param dataSetLabel
	 * @param listenerId
	 */
	public void addCometListenerIfInitializedAndAbsent(final String uuid, final String dataSetLabel, final String listenerId) {
		if (!CometDInitializerChecker.isCometdInitialized()) {
			return;
		}

		IDataSetListener listener = new IDataSetListener() {

			@Override
			public void dataStoreChanged(DataStoreChangedEvent event) throws DataSetListenerException {

				// notify the frontend clients about the dataStore changes
				CometServiceManager manager = CometServiceManagerFactory.getManager();
				manager.dataStoreChanged(uuid, dataSetLabel, event, listenerId);
			}

		};

		DataStoreListenerOperator op = getOperator(uuid, dataSetLabel);
		op.addIDataSetListenerIfAbsent(listener, listenerId);
	}
}