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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.Helper;

/**
 *
 *
 * @author fabrizio
 *
 */
public class DataStoreListenerOperator {

	private final Map<String, IDataSetListener> listeners = new HashMap<String, IDataSetListener>();

	private IDataSet dataSet;

	public IDataSet getDataSet() {
		return dataSet;
	}

	public synchronized void addIDataSetListenerIfAbsent(IDataSetListener listener, String listenerId) {
		Helper.checkNotNull(listener, "listener");
		Helper.checkNotNullNotTrimNotEmpty(listenerId, "listenerId");

		if (!listeners.containsKey(listenerId)) {
			listeners.put(listenerId, listener);
		}
	}

	public synchronized boolean removeIDataSetListener(String listenerId) {
		Helper.checkNotNullNotTrimNotEmpty(listenerId, "listenerId");

		IDataSetListener res = listeners.remove(listenerId);
		return res != null;
	}

	private List<ListenerResult> fireListeners(DataStoreChangedEvent event) {
		Helper.checkNotNull(event, "event");

		List<ListenerResult> res = new ArrayList<ListenerResult>();
		for (IDataSetListener l : listeners.values()) {
			try {
				l.dataStoreChanged(event);
				res.add(new ListenerResult());
			} catch (Exception e) {
				res.add(new ListenerResult(e));
			}
		}
		return res;
	}

	public synchronized List<ListenerResult> changedDataSet(IDataSet dataSet) {
		Helper.checkNotNull(dataSet, "dataSet");
		this.dataSet = dataSet;

		// use the current data set
		DataStoreChangedEvent event = new DataStoreChangedEvent(dataSet, dataSet.getDataStore());
		List<ListenerResult> res = fireListeners(event);
		return res;
	}

	public synchronized List<ListenerResult> changedDataSet(IDataStore dataStore) {
		checkStateNotNull();

		// use the current data set
		DataStoreChangedEvent event = new DataStoreChangedEvent(dataSet, dataStore);
		List<ListenerResult> res = fireListeners(event);
		return res;
	}

	private void checkStateNotNull() {
		if (this.dataSet == null) {
			throw new IllegalStateException("Previous store and dataset can't be null. changedDataSet must be called at least once.");
		}
	}
}
