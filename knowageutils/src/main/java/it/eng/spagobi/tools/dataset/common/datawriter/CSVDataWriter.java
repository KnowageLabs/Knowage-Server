/**
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
package it.eng.spagobi.tools.dataset.common.datawriter;

import java.util.Iterator;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 *
 * @author raselako
 * @email radmila.selakovic@eng.it
 *
 */
public class CSVDataWriter implements IDataWriter {

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.common.datawriter.IDataWriter#write(it.eng.spagobi.tools.dataset.common.datastore.IDataStore)
	 */
	@Override
	public Object write(IDataStore dataStore) {
		StringBuilder sb = new StringBuilder();
		try {
			for (int i = 0; i < dataStore.getMetaData().getFieldCount(); i++) {
				if (i != 0) {
					sb.append(",");
				}
				IFieldMetaData fieldMeta = dataStore.getMetaData().getFieldMeta(i);
				String columnName = fieldMeta.getAlias() != null ? fieldMeta.getAlias() : fieldMeta.getName();
				sb.append("\"" + columnName + "\"");
			}

			Iterator records = dataStore.iterator();
			while (records.hasNext()) {
				sb.append("\n");
				IRecord record = (IRecord) records.next();

				for (int j = 0; j < dataStore.getMetaData().getFieldCount(); j++) {
					if (j != 0) {
						sb.append(",");
					}
					Object fieldValue = record.getFieldAt(j).getValue();
					sb.append(fieldValue != null ? fieldValue : "");
				}

			}
		} catch (Exception t) {
			throw new SpagoBIRuntimeException("An unpredicted error occurred while serializing dataStore", t);
		}
		return sb.toString();

	}

}
