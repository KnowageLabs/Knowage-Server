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
package it.eng.knowage.engine.api.excel.export.oldcockpit.crosstable;

import java.util.Comparator;

public class ValueComparator implements Comparator<Double> {

	private int direction;

	public ValueComparator() {
		this.direction = 1; // default
	}

	public ValueComparator(int direction) {
		this.direction = direction;
	}

	@Override
	public int compare(Double arg0, Double arg1) {
		try {
			// compares only on values
			return direction * arg0.compareTo(arg1);
		} catch (Exception e) {
			// if its not possible to convert the values in float, consider them
			// as strings
			return direction * arg0.compareTo(arg1);
		}
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int dir) {
		this.direction = dir;
	}

}
