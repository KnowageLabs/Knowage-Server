/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.dataset.common.transformer;

import java.util.List;

import it.eng.spagobi.tools.dataset.common.datastore.DataStoreStats;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;

/**
 * @author Marco Libanori
 *
 */
public class DataStoreStatsTransformer extends AbstractDataStoreTransformer {

	public DataStoreStatsTransformer() {
	}

	@Override
	public void transformDataSetRecords(IDataStore dataStore) {
		DataStoreStats stats = dataStore.getStats();

		List<IRecord> records = dataStore.getRecords();
		for (IRecord record : records) {
			stats.addRecord(record);
		}
	}

	@Override
	public void transformDataSetMetaData(IDataStore dataStore) {
	}

}
