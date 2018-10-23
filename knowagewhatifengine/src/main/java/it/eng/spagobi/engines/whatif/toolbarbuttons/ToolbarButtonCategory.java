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
package it.eng.spagobi.engines.whatif.toolbarbuttons;

public enum ToolbarButtonCategory {
	DRILL_ON_DIMENSION("DRILL_ON_DIMENSION"), DRILL_ON_DATA("DRILL_ON_DATA"), OLAP_FUNCTIONS("OLAP_FUNCTIONS"), WHAT_IF("WHAT_IF"), OLAP_DESIGNER(
			"OLAP_DESIGNER"), TABLE_FUNCTIONS("TABLE_FUNCTIONS");

	private final String category;

	ToolbarButtonCategory(String category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return this.category;
	}
}
