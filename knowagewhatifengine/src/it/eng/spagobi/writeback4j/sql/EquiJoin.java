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

package it.eng.spagobi.writeback4j.sql;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */

public class EquiJoin {
	TableEntry leftField;
	TableEntry rightField;

	public EquiJoin(TableEntry leftField, TableEntry rightField) {
		super();
		this.leftField = leftField;
		this.rightField = rightField;
	}

	@Override
	public String toString() {
		if (leftField == null || rightField == null) {
			return "";
		}
		return leftField.toString() + " = " + rightField.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((leftField == null) ? 0 : leftField.hashCode());
		result = prime * result
				+ ((rightField == null) ? 0 : rightField.hashCode());
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		EquiJoin other = (EquiJoin) obj;
		if (leftField == null) {
			if (other.leftField != null) {
				return false;
			}
		} else if (!leftField.equals(other.leftField)) {
			return false;
		}
		if (rightField == null) {
			if (other.rightField != null) {
				return false;
			}
		} else if (!rightField.equals(other.rightField)) {
			return false;
		}
		return true;
	}

}