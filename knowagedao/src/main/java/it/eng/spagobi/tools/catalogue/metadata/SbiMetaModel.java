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
package it.eng.spagobi.tools.catalogue.metadata;

import java.util.Set;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiMetaModelParameter;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;

public class SbiMetaModel extends SbiHibernateModel {

	// Fields

	private int id;

	private String name;

	private String description;

	private Integer category;

	private SbiDataSource dataSource;

	private Boolean modelLocked;

	private String modelLocker;

	private Boolean smartView;

	private Set<SbiMetaModelParameter> biMetaModelParameters = null;

	private String tablePrefixLike;

	private String tablePrefixNotLike;

	// Constructors

	public SbiMetaModel() {
	}

	public SbiMetaModel(int id) {
		this.id = id;
	}

	public SbiMetaModel(int id, String name, String description, int category) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
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

	public SbiDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(SbiDataSource dataSource) {
		this.dataSource = dataSource;
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

	/**
	 * @return the smartView
	 */
	public Boolean getSmartView() {
		return smartView;
	}

	/**
	 * @param smartView
	 *            the smartView to set
	 */
	public void setSmartView(Boolean smartView) {
		this.smartView = smartView;
	}

	public Set<SbiMetaModelParameter> getSbiMetaModelParameters() {
		return this.biMetaModelParameters;
	}

	public void setSbiMetaModelParameters(Set<SbiMetaModelParameter> sbiMetaModelParameters) {
		this.biMetaModelParameters = sbiMetaModelParameters;
	}

	public String getTablePrefixLike() {
		return tablePrefixLike;
	}

	public void setTablePrefixLike(String tablePrefixLike) {
		this.tablePrefixLike = tablePrefixLike;
	}

	public String getTablePrefixNotLike() {
		return tablePrefixNotLike;
	}

	public void setTablePrefixNotLike(String tablePrefixNotLike) {
		this.tablePrefixNotLike = tablePrefixNotLike;
	}

}
