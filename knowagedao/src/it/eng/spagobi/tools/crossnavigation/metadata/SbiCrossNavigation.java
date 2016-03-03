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
package it.eng.spagobi.tools.crossnavigation.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.Set;

public class SbiCrossNavigation extends SbiHibernateModel {

	private static final long serialVersionUID = -5674358775970036877L;
	/**
	 * 
	 */
	private Integer id;

	private String name;
	private Set<SbiCrossNavigationPar> sbiCrossNavigationPars;

	private boolean newRecord;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the newRecord
	 */
	public boolean isNewRecord() {
		return newRecord;
	}

	/**
	 * @param newRecord
	 *            the newRecord to set
	 */
	public void setNewRecord(boolean newRecord) {
		this.newRecord = newRecord;
	}

	/**
	 * @return the parameters
	 */
	public Set<SbiCrossNavigationPar> getSbiCrossNavigationPars() {
		return sbiCrossNavigationPars;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setSbiCrossNavigationPars(Set<SbiCrossNavigationPar> sbiCrossNavigationPars) {
		this.sbiCrossNavigationPars = sbiCrossNavigationPars;
	}

}
