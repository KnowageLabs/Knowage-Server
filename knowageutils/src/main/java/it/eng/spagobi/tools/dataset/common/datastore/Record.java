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
package it.eng.spagobi.tools.dataset.common.datastore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Angelo Bernabei angelo.bernabei@eng.it
 */
public class Record implements IRecord, Serializable, Cloneable {

	IDataStore dataStore;
	List<IField> fields = new ArrayList<>();

	public Record() {
		this.fields = new ArrayList<>();
	}

	public Record(IDataStore dataStore) {
		this.fields = new ArrayList<>();
		this.setDataStore(dataStore);
	}

	@Override
	public IField getFieldAt(int position) {
		return fields.get(position);
	}

	@Override
	public void appendField(IField field) {
		fields.add(field);
	}

	@Override
	public void insertField(int fieldIndex, IField field) {
		fields.add(fieldIndex, field);
	}

	@Override
	public IField removeFieldAt(int fieldIndex) {
		return fields.remove(fieldIndex);
	}

	@Override
	public List<IField> getFields() {
		return this.fields;
	}

	@Override
	public void setFields(List<IField> fields) {
		this.fields = fields;
	}

	@Override
	public IDataStore getDataStore() {
		return dataStore;
	}

	public void setDataStore(IDataStore dataStore) {
		this.dataStore = dataStore;
	}

	@Override
	public String toString() {
		return "" + getFields().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Record other = (Record) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		return true;
	}

	@Override
	protected final Object clone() throws CloneNotSupportedException {
		Record clone = new Record();
		clone.setDataStore(dataStore);
		for (IField field : fields) {
			clone.appendField(((Field) field).clone());
		}
		return clone;
	}

}
