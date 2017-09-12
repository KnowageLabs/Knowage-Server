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

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.utilities.Helper;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class DataStoreChangedEvent extends DataSetEvent {

	private final IDataStore previousStore;
	private final IDataStore currentStore;

	private final List<IRecord> added;
	private final List<IRecord> updated;
	private final List<IRecord> deleted;

	private final boolean isChanged;

	public DataStoreChangedEvent(IDataSet dataSet, IDataStore previousStore, IDataStore currentStore, List<IRecord> added, List<IRecord> updated,
			List<IRecord> deleted) {
		super(dataSet);

		Helper.checkNotNull(added, "added");
		Helper.checkNotNull(updated, "updated");
		Helper.checkNotNull(deleted, "deleted");

		this.previousStore = previousStore;
		this.currentStore = currentStore;

		this.added = Collections.unmodifiableList(added);
		this.updated = Collections.unmodifiableList(updated);
		this.deleted = Collections.unmodifiableList(deleted);

		this.isChanged = added.size() != 0 || updated.size() != 0 || deleted.size() != 0;

	}

	public IDataStore getPreviousStore() {
		return previousStore;
	}

	public IDataStore getCurrentStore() {
		return currentStore;
	}

	public List<IRecord> getAdded() {
		return added;
	}

	public List<IRecord> getUpdated() {
		return updated;
	}

	public List<IRecord> getDeleted() {
		return deleted;
	}

	public boolean isChanged() {
		return isChanged;
	}

}
