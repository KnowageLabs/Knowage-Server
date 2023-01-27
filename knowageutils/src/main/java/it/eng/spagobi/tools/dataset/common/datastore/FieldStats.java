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

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Marco Libanori
 */
public class FieldStats {

	private Object min;
	private Object max;
	private final TreeSet<Object> distinct = new TreeSet<>();

	FieldStats() {
	}

	public void add(Object value) {
		try {

			distinct.add(value);
			min = distinct.first();
			max = distinct.last();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * @return the min
	 */
	public Object getMin() {
		return min;
	}

	/**
	 * @param min the min to set
	 */
	void setMin(Object min) {
		this.min = min;
	}

	/**
	 * @return the max
	 */
	public Object getMax() {
		return max;
	}

	/**
	 * @param max the max to set
	 */
	void setMax(Object max) {
		this.max = max;
	}

	/**
	 * @return the distinct
	 */
	public Set<Object> getDistinct() {
		return distinct;
	}

	public int getCardinality() {
		return distinct.size();
	}

}
