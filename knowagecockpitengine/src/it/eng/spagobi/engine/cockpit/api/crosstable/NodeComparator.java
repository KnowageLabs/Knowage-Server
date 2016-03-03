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
package it.eng.spagobi.engine.cockpit.api.crosstable;

import java.util.Comparator;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */

public class NodeComparator implements Comparator<Node> {

	private final int direction;

	public NodeComparator(int direction) {
		this.direction = direction;
	}

	@Override
	public int compare(Node arg0, Node arg1) {
		try {
			Float arg0Value = new Float(arg0.getValue());
			Float arg1Value = new Float(arg1.getValue());
			return direction * arg0Value.compareTo(arg1Value);
		} catch (Exception e) {
			// if its not possible to convert the values in float, consider them
			// as strings
			return direction * arg0.getValue().compareTo(arg1.getValue());
		}
	}

	public int getDirection() {
		return direction;
	}

}
