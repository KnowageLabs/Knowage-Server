/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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