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

package it.eng.spagobi.engines.whatif.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Dragan Pirkovic
 *
 */
public class SeachParameters {
	@JsonProperty(value = "hierarchy")
	private String hierarchyUniqueName;
	private int axis;
	private String name;
	@JsonProperty(value = "showS")
	private boolean showSiblings;

	/**
	 * @return the axis
	 */
	public int getAxis() {
		return axis;
	}

	/**
	 * @return the hierarchyUniqueName
	 */
	public String getHierarchyUniqueName() {
		return hierarchyUniqueName;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the showS
	 */
	public boolean isShowSiblings() {
		return showSiblings;
	}

	/**
	 * @param axis
	 *            the axis to set
	 */
	public void setAxis(int axis) {
		this.axis = axis;
	}

	/**
	 * @param hierarchyUniqueName
	 *            the hierarchyUniqueName to set
	 */
	public void setHierarchyUniqueName(String hierarchyUniqueName) {
		this.hierarchyUniqueName = hierarchyUniqueName;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param showS
	 *            the showS to set
	 */
	public void setShowSiblins(boolean showS) {
		this.showSiblings = showS;
	}

}
