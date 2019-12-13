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

import java.util.Set;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiCrossNavigation extends SbiHibernateModel {

	private static final long serialVersionUID = -5674358775970036877L;
	/**
	 *
	 */
	private Integer id;

	private String name;
	private String description;
	private String breadcrumb;
	private Integer type;
	private String popupOptions;
	private Set<SbiCrossNavigationPar> sbiCrossNavigationPars;

	private boolean newRecord;
	private Integer fromDocId;
	private Integer toDocId;

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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the breadcrumb
	 */
	public String getBreadcrumb() {
		return breadcrumb;
	}

	/**
	 * @param breadcrumb
	 *            the breadcrumb to set
	 */
	public void setBreadcrumb(String breadcrumb) {
		this.breadcrumb = breadcrumb;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
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

	/**
	 *
	 * @return the fromDocId
	 */
	public Integer getFromDocId() {
		return fromDocId;
	}

	/**
	 * @param id of the document from which the cross navigation starts
	 */
	public void setFromDocId(Integer fromDocId) {
		this.fromDocId = fromDocId;
	}

	/**
	 *
	 * @return the toDocId
	 */
	public Integer getToDocId() {
		return toDocId;
	}

	/**
	 * @param id of the document in which the cross navigation ends
	 */
	public void setToDocId(Integer toDocId) {
		this.toDocId = toDocId;
	}

	public String getPopupOptions() {
		return popupOptions;
	}

	public void setPopupOptions(String popupOptions) {
		this.popupOptions = popupOptions;
	}
}
