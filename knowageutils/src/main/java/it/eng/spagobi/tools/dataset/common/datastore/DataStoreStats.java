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

package it.eng.spagobi.tools.dataset.common.datastore;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DataStoreStats {

	private final Map<Integer, FieldStats> fields = new TreeMap<>();

	DataStoreStats() {
	}

	public void addRecord(IRecord record) {
		List<IField> recordFields = record.getFields();

		int i = 0;
		for (IField field : recordFields) {
			fields.putIfAbsent(i, new FieldStats());
			FieldStats fieldStats = fields.get(i);
			Object value = field.getValue();
			fieldStats.add(value);
			i++;
		}
	}

	/**
	 * @return the fields
	 */
	public Map<Integer, FieldStats> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	public FieldStats getField(int fieldIndex) {
		return fields.get(fieldIndex);
	}

}
