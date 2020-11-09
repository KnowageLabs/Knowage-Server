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
package it.eng.spagobi.tools.catalogue.bo;

import java.util.List;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.services.validation.ExtendedAlphanumeric;
import it.eng.spagobi.tools.catalogue.metadata.IDrivableBIResource;

public class MetaModel implements IDrivableBIResource<BIMetaModelParameter> {

	private Integer id;

	@NotEmpty
	@ExtendedAlphanumeric
	@Size(max = 100)
	private String name;

	@ExtendedAlphanumeric
	@Size(max = 500)
	private String description;

	private Integer category;

	@NotEmpty
	@ExtendedAlphanumeric
	private String dataSourceLabel;

	private Integer dataSourceId;

	private Boolean modelLocked;

	@Size(max = 100)
	private String modelLocker;

	private Boolean smartView;

	private List<BIMetaModelParameter> biMetaModelParameters = null;

	@ExtendedAlphanumeric
	@Size(max = 500)
	private String tablePrefixLike;

	@ExtendedAlphanumeric
	@Size(max = 500)
	private String tablePrefixNotLike;

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

	public Integer getDataSourceId() {
		return dataSourceId;
	}

	public void setDataSourceId(Integer dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	/**
	 * @return the modelLocked
	 */
	public Boolean getModelLocked() {
		if (modelLocked == null)
			return false;
		else
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

	// public List<BIMetaModelParameter> getBiMetaModelParameters() {
	// return biMetaModelParameters;
	// }
	//
	// public void setBiMetaModelParameters(List<BIMetaModelParameter> biMetaModelParameters) {
	// this.biMetaModelParameters = biMetaModelParameters;
	// }

	/**
	 * @return the smartView
	 */
	public Boolean getSmartView() {
		if (smartView == null) {
			return false;
		}
		return smartView;
	}

	/**
	 * @param smartView
	 *            the smartView to set
	 */
	public void setSmartView(Boolean smartView) {
		this.smartView = smartView;
	}

	@Override
	public List<BIMetaModelParameter> getDrivers() {
		return biMetaModelParameters;
	}

	@Override
	public void setDrivers(List<BIMetaModelParameter> drivers) {
		this.biMetaModelParameters = drivers;
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

	@Override
	public List<? extends AbstractDriver> getMetamodelDrivers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMetamodelDrivers(List<BIMetaModelParameter> drivers) {
		// TODO Auto-generated method stub
		
	}

}
