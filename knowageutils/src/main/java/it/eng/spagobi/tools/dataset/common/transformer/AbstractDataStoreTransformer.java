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
package it.eng.spagobi.tools.dataset.common.transformer;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractDataStoreTransformer implements IDataStoreTransformer {

	AbstractDataStoreTransformer nextTransformer;

	@Override
	public void transform(IDataStore dataStore) {
		transformDataSetRecords(dataStore);
		transformDataSetMetaData(dataStore);
		if (getNextTransformer() != null) {
			getNextTransformer().transform(dataStore);
		}
	}

	// TODO : change to protected
	public abstract void transformDataSetRecords(IDataStore dataStore);

	// TODO : change to protected
	public abstract void transformDataSetMetaData(IDataStore dataStore);

	public AbstractDataStoreTransformer getNextTransformer() {
		return nextTransformer;
	}

	public void setNextTransformer(AbstractDataStoreTransformer nextTransformer) {
		this.nextTransformer = nextTransformer;
	}

}
