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
package it.eng.spagobi.tools.hierarchiesmanagement.utils;

import java.util.HashMap;
import java.util.Map;

public class FillConfiguration {

	private final HashMap hierarchyConfig;
	private boolean fillEmpty;
	private String fillValue;

	public FillConfiguration(HashMap hierarchyConfig) {
		this.hierarchyConfig = hierarchyConfig;

		this.setFillConfiguration();

	}

	// getter and setter methods

	public HashMap getHierarchyConfig() {
		return hierarchyConfig;
	}

	public boolean isFillEmpty() {
		return fillEmpty;
	}

	public String getFillValue() {
		return fillValue;
	}

	// public methods

	public Object fillHandler(Map<Integer, Object[]> levelsMap, int valueIndex) {

		Object result = null;

		// the level index points at the last level inserted
		int lvlIndex = levelsMap.size();

		if (this.fillEmpty && lvlIndex == 0) {
			result = this.fillValue;
		} else if (fillEmpty && lvlIndex > 0) {
			result = (this.fillValue == null) ? levelsMap.get(lvlIndex)[valueIndex] : this.fillValue;
		}

		return result;

	}

	// private methods

	private void setFillConfiguration() {

		String tmpFillEmpty = (String) this.hierarchyConfig.get(HierarchyConstants.FILL_EMPTY);
		if (tmpFillEmpty != null) {
			this.fillEmpty = (tmpFillEmpty.equalsIgnoreCase(HierarchyConstants.FILL_EMPTY_YES)) ? true : false;
		} else {
			this.fillEmpty = false;
		}

		String tmpFillValue = (String) this.hierarchyConfig.get(HierarchyConstants.FILL_VALUE);
		this.fillValue = (tmpFillValue != null && tmpFillValue.equals("")) ? null : tmpFillValue;

	}

}
