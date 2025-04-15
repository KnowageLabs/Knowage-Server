/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2024 Engineering Ingegneria Informatica S.p.A.

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

import java.util.Optional;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;

/**
 *
 */
public class SyncMetaDataDataStoreTransformer implements IDataStoreTransformer {

	private final IDataSet dataSet;
	private final IMetaData dataSetMetaData;

	public SyncMetaDataDataStoreTransformer(IDataSet dataSet) {
		this.dataSet = dataSet;
		this.dataSetMetaData = this.dataSet != null && this.dataSet.getMetadata() != null ? this.dataSet.getMetadata()
				: new MetaData();
	}

	@Override
	public void transform(IDataStore dataStore) {
		IMetaData metaData = dataStore.getMetaData();

		// @formatter:off
		metaData.getFieldsMeta()
			.stream()
			.forEach(e -> {
				Optional<IFieldMetaData> first = dataSetMetaData.getFieldsMeta()
					.stream()
					.filter(f -> e.getName().equals(f.getName()))
					.findFirst();

				if (first.isPresent()) {
					IFieldMetaData currField = first.get();

					e.setDecrypt(currField.isDecrypt());
					e.setPersonal(currField.isPersonal());
					e.setSubjectId(currField.isSubjectId());
					e.setDescription(currField.getDescription());
				}
			});
		// @formatter:on
	}

}
