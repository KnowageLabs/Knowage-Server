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
