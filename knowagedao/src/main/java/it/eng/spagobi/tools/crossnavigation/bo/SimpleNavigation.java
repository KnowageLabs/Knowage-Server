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

public class SimpleNavigation {

	private Integer id;
	private String name;
	private String description;
	private String breadcrumb;
	private Integer type;
	private String fromDoc;
	private Integer fromDocId;
	private String toDoc;
	private Integer toDocId;
	private String fixedValue;
	private String popupOptions;

	public SimpleNavigation() {
	}

	public SimpleNavigation(Integer id, String name, String description, String breadcrumb, Integer type, String fromDoc, Integer fromDocId, String toDoc,
			Integer toDocId,String popupOptions) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.breadcrumb = breadcrumb;
		this.type = type;
		this.fromDoc = fromDoc;
		this.toDoc = toDoc;
		this.fromDocId = fromDocId;
		this.toDocId = toDocId;
		this.popupOptions = popupOptions;
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
	 * @return the fromDoc
	 */
	public String getFromDoc() {
		return fromDoc;
	}

	/**
	 * @param fromDoc
	 *            the fromDoc to set
	 */
	public void setFromDoc(String fromDoc) {
		this.fromDoc = fromDoc;
	}

	/**
	 * @return the toDoc
	 */
	public String getToDoc() {
		return toDoc;
	}

	/**
	 * @param toDoc
	 *            the toDoc to set
	 */
	public void setToDoc(String toDoc) {
		this.toDoc = toDoc;
	}

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
	 * @return the fixedValue
	 */
	public String getFixedValue() {
		return fixedValue;
	}

	/**
	 * @param fixedValue
	 *            the fixedValue to set
	 */
	public void setFixedValue(String fixedValue) {
		this.fixedValue = fixedValue;
	}

	/**
	 * @return the fromDocId
	 */
	public Integer getFromDocId() {
		return fromDocId;
	}

	/**
	 * @param fromDocId
	 *            the fromDocId to set
	 */
	public void setFromDocId(Integer fromDocId) {
		this.fromDocId = fromDocId;
	}

	/**
	 * @param fromDocId the toDocId to set
	 */
	public Integer getToDocId() {
		return toDocId;
	}

	/**
	 * @param toDocId the toDocId to set
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
