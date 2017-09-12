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
package it.eng.spagobi.tools.crossnavigation.bo;

import java.util.ArrayList;
import java.util.List;

public class NavigationDetail {

	private SimpleNavigation simpleNavigation;
	private List<SimpleParameter> fromPars = new ArrayList<>();
	private List<SimpleParameter> toPars = new ArrayList<>();
	private boolean newRecord;

	/**
	 * @return the fromPars
	 */
	public List<SimpleParameter> getFromPars() {
		return fromPars;
	}

	/**
	 * @param fromPars
	 *            the fromPars to set
	 */
	public void setFromPars(List<SimpleParameter> fromPars) {
		this.fromPars = fromPars;
	}

	/**
	 * @return the toPars
	 */
	public List<SimpleParameter> getToPars() {
		return toPars;
	}

	/**
	 * @param toPars
	 *            the toPars to set
	 */
	public void setToPars(List<SimpleParameter> toPars) {
		this.toPars = toPars;
	}

	/**
	 * @return the isNewRecord
	 */
	public boolean isNewRecord() {
		return newRecord;
	}

	/**
	 * @param isNewRecord
	 *            the isNewRecord to set
	 */
	public void setNewRecord(boolean newRecord) {
		this.newRecord = newRecord;
	}

	/**
	 * @return the simpleNavigation
	 */
	public SimpleNavigation getSimpleNavigation() {
		return simpleNavigation;
	}

	/**
	 * @param simpleNavigation
	 *            the simpleNavigation to set
	 */
	public void setSimpleNavigation(SimpleNavigation simpleNavigation) {
		this.simpleNavigation = simpleNavigation;
	}

}
