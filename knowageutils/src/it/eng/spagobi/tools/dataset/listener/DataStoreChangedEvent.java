/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2015 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
