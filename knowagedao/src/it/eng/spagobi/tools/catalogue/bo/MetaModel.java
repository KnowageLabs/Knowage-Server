/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.catalogue.bo;

public class MetaModel {

	private Integer id;

	private String name;

	private String description;

	private Integer category;

	private String dataSourceLabel;

	private Boolean modelLocked;

	private String modelLocker;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the category
	 */
	public Integer getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(Integer category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return "MetaModel [id=" + id + ", name=" + name + ", description=" + description + ", category = " + category + "]";
	}

	public String getDataSourceLabel() {
		return dataSourceLabel;
	}

	public void setDataSourceLabel(String dataSourceLabel) {
		this.dataSourceLabel = dataSourceLabel;
	}

	/**
	 * @return the modelLocked
	 */
	public Boolean getModelLocked() {
		return modelLocked;
	}

	/**
	 * @param modelLocked
	 *            the modelLocked to set
	 */
	public void setModelLocked(Boolean modelLocked) {
		this.modelLocked = modelLocked;
	}

	/**
	 * @return the modelLocker
	 */
	public String getModelLocker() {
		return modelLocker;
	}

	/**
	 * @param modelLocker
	 *            the modelLocker to set
	 */
	public void setModelLocker(String modelLocker) {
		this.modelLocker = modelLocker;
	}

}
