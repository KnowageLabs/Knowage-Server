/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2015 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.listener;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * @author fabrizio
 *
 */
public class DataStoreListenerOperator {

	private static final int ID_NOT_DEFINED = -1;

	private final Map<String, IDataSetListener> listeners = new HashMap<String, IDataSetListener>();

	/**
	 * It's used to check the differences from current data set. It's null at beginning.
	 */
	private IDataStore store;

	private IDataSet dataSet;

	private final DataStoreCloner dataStoreCloner = new DataStoreCloner();

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

	public synchronized List<ListenerResult> changedDataSet(IDataSet currDataSet) {
		Helper.checkNotNull(currDataSet, "currDataSet");

		IDataStore prev = this.store;
		IDataStore curr = currDataSet.getDataStore();
		Assert.assertNotNull(curr, "curr!=null");
		
		this.store = curr;
		this.dataSet = currDataSet;
		
		List<ListenerResult> res = fireListeners(createEvent(prev, curr, currDataSet));
		return res;
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

	protected DataStoreChangedEvent createEvent(IDataStore prev, final IDataStore curr, IDataSet set) {
		if (prev == null) {
			return new DataStoreChangedEvent(set, curr, curr, getAllRecords(curr), new ArrayList<IRecord>(0), new ArrayList<IRecord>(0));
		}
		// prev!=null
		Helper.checkNotNull(curr, "curr");

		IMetaData prevMeta = prev.getMetaData();
		IMetaData currMeta = curr.getMetaData();
		Assert.assertNotNull(prevMeta, "prevMeta");
		Assert.assertNotNull(currMeta, "currMeta");

		int prevFieldIndex = prevMeta.getIdFieldIndex();
		int currFieldIndex = currMeta.getIdFieldIndex();
		if (prevFieldIndex == ID_NOT_DEFINED || currFieldIndex == ID_NOT_DEFINED) {
			return new DataStoreChangedEvent(set, prev, curr, getAllRecords(curr), new ArrayList<IRecord>(0), getAllRecords(prev));
		}

		List<IRecord> updated = getUpdated(prev, new RecordIdRetriever() {

			public IRecord getRecordById(Object id) {
				return curr.getRecordByID(id);
			}
		}, prevFieldIndex);
		List<IRecord> deleted = getDeleted(prev, curr, prevFieldIndex);
		List<IRecord> added = getAdded(prev, new RecordRetriever() {

			public IRecord getRecord(int index) {
				return curr.getRecordAt(index);
			}

			public int countRecords() {
				return (int) curr.getRecordsCount();
			}
		}, prevFieldIndex);
		DataStoreChangedEvent res = new DataStoreChangedEvent(set, prev, curr, added, updated, deleted);
		return res;
	}

	private static interface RecordIdRetriever {
		public IRecord getRecordById(Object id);
	}

	private static List<IRecord> getUpdated(IDataStore prev, RecordIdRetriever currRetr, int prevFieldIndex) {
		List<IRecord> updated = new ArrayList<IRecord>();
		for (int i = 0; i < prev.getRecordsCount(); i++) {
			IRecord prevRec = prev.getRecordAt(i);
			IField prevIdField = prevRec.getFieldAt(prevFieldIndex);
			Assert.assertNotNull(prevIdField, "prevIdField");
			Object prevId = prevIdField.getValue();
			Assert.assertNotNull(prevId, "prevId");
			IRecord currRec = currRetr.getRecordById(prevId);
			if (currRec == null) {
				// deleted
				continue;
			}

			// check updated by position of field
			// a impossible improvement: check the changes by field meta's name
			List<IField> prevFields = prevRec.getFields();
			List<IField> currFields = currRec.getFields();
			if (prevFields.size() != currFields.size()) {
				updated.add(currRec);
				continue;
			}

			// check updated by checking fields values
			for (int j = 0; j < prevFields.size(); j++) {
				IField prevField = prevFields.get(j);
				IField currField = currFields.get(j);

				Assert.assertNotNull(prevField, "prevField");
				Assert.assertNotNull(currField, "currField");

				Object prevValue = prevField.getValue();
				Object currValue = currField.getValue();

				boolean equals = areEquals(prevValue, currValue);
				if (!equals) {
					updated.add(currRec);
					break;
				}
			}
		}
		return updated;
	}

	private static List<IRecord> getDeleted(IDataStore prev, IDataStore curr, int prevFieldIndex) {
		List<IRecord> deleted = new ArrayList<IRecord>();
		for (int i = 0; i < prev.getRecordsCount(); i++) {
			IRecord prevRec = prev.getRecordAt(i);
			IField prevIdField = prevRec.getFieldAt(prevFieldIndex);
			Assert.assertNotNull(prevIdField, "prevIdField");
			Object prevId = prevIdField.getValue();
			Assert.assertNotNull(prevId, "prevId");
			IRecord currRec = curr.getRecordByID(prevId);
			if (currRec == null) {
				deleted.add(prevRec);
				continue;
			}
		}
		return deleted;
	}

	private static interface RecordRetriever {
		IRecord getRecord(int index);

		int countRecords();
	}

	private static List<IRecord> getAdded(IDataStore prev, RecordRetriever currRetr, int prevFieldIndex) {
		List<IRecord> added = new ArrayList<IRecord>();
		for (int i = 0; i < currRetr.countRecords(); i++) {
			IRecord currRec = currRetr.getRecord(i);
			IField currIdField = currRec.getFieldAt(prevFieldIndex);
			Assert.assertNotNull(currIdField, "currId");
			Object id = currIdField.getValue();
			Assert.assertNotNull(id, "id");
			if (prev.getRecordByID(id) == null) {
				added.add(currRec);
			}
		}
		return added;
	}

	private static List<IRecord> getAllRecords(IDataStore ds) {
		List<IRecord> res = new ArrayList<IRecord>((int) Math.min(Integer.MAX_VALUE, ds.getRecordsCount()));
		for (int i = 0; i < ds.getRecordsCount(); i++) {
			res.add(ds.getRecordAt(i));
		}
		return res;
	}

	private static boolean areEquals(Object prev, Object curr) {
		if (prev == null) {
			boolean res = curr == null;
			return res;
		}
		// prev!=null
		boolean res = prev.equals(curr);
		return res;
	}

	public synchronized List<ListenerResult> changedDataSet(List<IRecord> updated) {
		Helper.checkNotNull(updated, "updated");
		checkStateNotNull();
		
		IDataStore prev = this.store;
		IDataStore curr = dataStoreCloner.clone(prev, new ArrayList<IRecord>(0), new ArrayList<IRecord>(0), updated);
		this.store = curr;

		// use the current data set
		DataStoreChangedEvent event = createEvent(prev, curr, this.dataSet);
		List<ListenerResult> res = fireListeners(event);
		return res;
	}

	private void checkStateNotNull() {
		if (this.store==null || this.dataSet==null) {
			throw new IllegalStateException("Previous store and dataset can't be null. changedDataSet must be called at least once.");
		}
	}

	public synchronized List<ListenerResult> changedDataSetUpdatedOrAdded(final List<IRecord> updatedOrAdded, int idFieldIndex) {
		Helper.checkNotNull(updatedOrAdded, "updatedOrAdded");
		Helper.checkNotNegative(idFieldIndex, "idFieldIndex");
		checkStateNotNull();

		IDataStore prev = this.store;
		// find added records
		List<IRecord> added = getAdded(updatedOrAdded, idFieldIndex, prev);
		
		// find updated records
		List<IRecord> updated = getUpdated(updatedOrAdded, idFieldIndex, prev);
		
		IDataStore curr= dataStoreCloner.clone(prev, added, new ArrayList<IRecord>(0), updated);
		this.store = curr;

		// use the current data set
		DataStoreChangedEvent event = createEvent(prev, curr, this.dataSet);
		List<ListenerResult> res = fireListeners(event);
		return res;

	}

	private static List<IRecord> getUpdated(final List<IRecord> updatedOrAdded, int idFieldIndex, IDataStore prev) {
		final Map<Object, IRecord> recordsById = getRecordsById(updatedOrAdded, idFieldIndex);
		List<IRecord> updated = getUpdated(prev, new RecordIdRetriever() {

			public IRecord getRecordById(Object id) {
				return recordsById.get(id);
			}
		}, idFieldIndex);
		return updated;
	}

	private static List<IRecord> getAdded(final List<IRecord> updatedOrAdded, int idFieldIndex, IDataStore prev) {
		List<IRecord> added = getAdded(prev, new RecordRetriever() {

			public IRecord getRecord(int index) {
				return updatedOrAdded.get(index);
			}

			public int countRecords() {
				return updatedOrAdded.size();
			}
		}, idFieldIndex);
		return added;
	}

	private static Map<Object, IRecord> getRecordsById(List<IRecord> updatedOrAdded, int idFieldIndex) {
		Map<Object, IRecord> res = new HashMap<Object, IRecord>(updatedOrAdded.size());
		for (IRecord iRecord : updatedOrAdded) {
			IField fieldId = iRecord.getFieldAt(idFieldIndex);
			Assert.assertNotNull(fieldId, "fieldId");
			Object value = fieldId.getValue();
			Assert.assertNotNull(value, "value");
			res.put(value, iRecord);
		}
		return res;
	}

}
