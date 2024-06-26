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

import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStoreCloner {

	public IDataStore clone(IDataStore store, List<IRecord> addedRecords, List<IRecord> deletedRecords, List<IRecord> updatedRecords) {
		Helper.checkNotNull(store, "store");

		int idIndex = getIdField(store, deletedRecords, updatedRecords);
		Map<Object, IRecord> deleted = getIds(deletedRecords, idIndex);
		Map<Object, IRecord> updated = getIds(updatedRecords, idIndex);

		DataStore res = new DataStore();
		res.setMetaData(store.getMetaData()); // same pointer
		for (int i = 0; i < store.getRecordsCount(); i++) {
			IRecord rec = store.getRecordAt(i);

			// check deleted
			if (!deleted.isEmpty()) {
				Object id = rec.getFieldAt(idIndex);
				if (id == null) {
					throw new DataStoreClonerException("Id is null");
				}
				if (deleted.containsKey(id)) {
					continue;
				}
			}

			// check updated
			if (!updated.isEmpty()) {
				Object id = rec.getFieldAt(idIndex);
				if (id == null) {
					throw new DataStoreClonerException("Id is null");
				}
				if (updated.containsKey(id)) {
					Record newRec = cloneRecord(res, updated.get(id));
					checkRec(rec, newRec);
					res.appendRecord(newRec);
					continue;
				}
			}

			// copy record
			Record newRec = cloneRecord(res, rec);

			checkRec(rec, newRec);
			res.appendRecord(newRec);
		}

		// add added

		for (IRecord add : addedRecords) {
			Record newRec = cloneRecord(res, add);
			res.appendRecord(newRec);
		}
		return res;
	}

	private static void checkRec(IRecord rec, Record newRec) {
		Assert.assertTrue(rec.getFields().size() == newRec.getFields().size(), "rec.getFields().size()==newRec.getFields().size()");
	}

	private static Record cloneRecord(DataStore res, IRecord rec) {
		Record newRec = new Record(res);
		for (IField f : rec.getFields()) {
			Field newField = new Field();
			newField.setDescription(f.getDescription());
			newField.setValue(f.getValue());
			newRec.appendField(newField);
		}
		return newRec;
	}

	private static int getIdField(IDataStore store, List<IRecord> deleted, List<IRecord> updated) {
		int idField = -1;
		if (!deleted.isEmpty() || !updated.isEmpty()) {
			idField = store.getMetaData().getIdFieldIndex();
			if (idField == -1) {
				throw new DataStoreClonerException("Records can't be deleted, id field is not defined");
			}
		}
		return idField;
	}

	private static Map<Object, IRecord> getIds(List<IRecord> list, int idField) {
		Map<Object, IRecord> res = new HashMap<Object, IRecord>();
		for (IRecord rec : list) {
			Assert.assertTrue(idField != -1, "idField!=-1");

			IField id = rec.getFieldAt(idField);
			Assert.assertNotNull(id, "id");
			res.put(id, rec);
		}
		return res;
	}

}
