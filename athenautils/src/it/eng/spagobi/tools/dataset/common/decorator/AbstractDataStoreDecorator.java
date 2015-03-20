/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.decorator;


import java.util.Iterator;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractDataStoreDecorator implements IDataStoreDecorator {
	
	IDataStoreDecorator nextDecoratr;
	

	public void decorate(IDataStore dataStore) {
		Iterator<IRecord> it = dataStore.iterator();
		while(it.hasNext()) {
			IRecord record = it.next();
			updateDecoration(dataStore, record);
		}
	}
	
	public void updateDecoration(IDataStore dataStore, IRecord record) {
		if( getNextDecoratr() != null) {
			getNextDecoratr().updateDecoration(dataStore, record);
		}
		this.doUpdateDecoration(dataStore, record);
	}
	 
	abstract void doUpdateDecoration(IDataStore dataStore, IRecord record);
	
	public IDataStoreDecorator getNextDecoratr() {
		return nextDecoratr;
	}

	public void setNextDecoratr(IDataStoreDecorator nextDecoratr) {
		this.nextDecoratr = nextDecoratr;
	}
}
