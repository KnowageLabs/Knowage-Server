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
package it.eng.qbe.utility;

import it.eng.spagobi.tools.dataset.common.datastore.IRecord;

public class TemporalRecord {

	private Object id;
	private Object period;
	private Object[] parentPeriods;

	public TemporalRecord(IRecord r, int numerOfParentPeriods) {
		super();
		Object id = r.getFieldAt(0).getValue();
		Object period = r.getFieldAt(1).getValue();

		this.id = id;
		this.period = period;

		if (numerOfParentPeriods > 0) {
			this.parentPeriods = new Object[numerOfParentPeriods];
			for (int i = 0; i < this.parentPeriods.length; i++) {
				this.parentPeriods[i] = r.getFieldAt(i + 2).getValue();
			}
		} else {
			this.parentPeriods = new Object[] {};
		}

	}

	public Object getId() {
		return id;
	}

	public Object getPeriod() {
		return period;
	}

	public Object[] getParentPeriods() {
		return parentPeriods;
	}

	@Override
	public String toString() {
		return "|" + id + "|" + parentPeriods + "|" + period + "|";
	}

	public String getPeriodIdentifier() {
		return getParentPeriods() + "" + getPeriod();
	}

	@Override
	public int hashCode() {
		return getPeriodIdentifier().hashCode() * 23;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TemporalRecord)) {
			return false;
		}
		TemporalRecord other = (TemporalRecord) obj;
		return this.getPeriodIdentifier().equals(other.getPeriodIdentifier());
	}
}
