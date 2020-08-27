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
package it.eng.spagobi.commons.bo;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import it.eng.spagobi.commons.IConfiguration;
import it.eng.spagobi.services.validation.ExtendedAlphanumeric;
import it.eng.spagobi.services.validation.Xss;

/**
 * Defines a Config object.
 */
public class Config implements IConfiguration, Serializable {

	private Integer id;

	@NotEmpty
	@ExtendedAlphanumeric
	@Size(max = 100)
	private String label;

	@NotEmpty
	@ExtendedAlphanumeric
	@Size(max = 100)
	private String name;

	@ExtendedAlphanumeric
	@Size(max = 500)
	private String description;

	@NotNull
	private boolean isActive;

	@Xss
	@Size(max = 1000)
	private String valueCheck;

	private Integer valueTypeId;

	@NotEmpty
	@ExtendedAlphanumeric
	@Size(max = 100)
	private String category;

	public Config() {
	}

	/**
	 * @return the id
	 */
	@Override
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
	 * @return the label
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the name
	 */
	@Override
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
	@Override
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
	 * @return the isActive
	 */
	@Override
	public boolean isActive() {
		return isActive;
	}

	/**
	 * @param isActive
	 *            the isActive to set
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * @return the valueCheck
	 */
	@Override
	public String getValueCheck() {
		return valueCheck;
	}

	/**
	 * @param valueCheck
	 *            the valueCheck to set
	 */
	public void setValueCheck(String valueCheck) {
		this.valueCheck = valueCheck;
	}

	/**
	 * @return the valueTypeId
	 */
	@Override
	public Integer getValueTypeId() {
		return valueTypeId;
	}

	/**
	 * @param valueTypeId
	 *            the valueTypeId to set
	 */
	public void setValueTypeId(Integer valueTypeId) {
		this.valueTypeId = valueTypeId;
	}

	/**
	 * @return the category to get
	 */
	@Override
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            . The category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

}
