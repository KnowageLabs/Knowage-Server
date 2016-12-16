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

package it.eng.spagobi.tools.dataset.common.similarity;

public class Field implements Comparable<Field> {

	public final String datasetLabel;
	public final String datasetColumn;

	public Field(String datasetLabel, String datasetColumn) {
		this.datasetLabel = datasetLabel;
		this.datasetColumn = datasetColumn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datasetColumn == null) ? 0 : datasetColumn.hashCode());
		result = prime * result + ((datasetLabel == null) ? 0 : datasetLabel.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Field)) {
			return false;
		}
		Field other = (Field) obj;
		if (datasetColumn == null) {
			if (other.datasetColumn != null) {
				return false;
			}
		} else if (!datasetColumn.equals(other.datasetColumn)) {
			return false;
		}
		if (datasetLabel == null) {
			if (other.datasetLabel != null) {
				return false;
			}
		} else if (!datasetLabel.equals(other.datasetLabel)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Field other) {
		int result = datasetLabel.compareTo(other.datasetLabel);
		if (result == 0) {
			result += datasetColumn.compareTo(other.datasetColumn);
		}
		return result;
	}
}
