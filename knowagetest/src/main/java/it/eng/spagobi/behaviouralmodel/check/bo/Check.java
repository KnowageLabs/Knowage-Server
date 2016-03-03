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
package it.eng.spagobi.behaviouralmodel.check.bo;

import java.io.Serializable;

public class Check implements Serializable {

	private Integer checkId;

	private Integer valueTypeId;

	private String Name;

	private String label;

	private String Description;

	private String valueTypeCd;

	private String firstValue;

	private String secondValue;

	/**
	 * Gets the description.
	 *
	 * @return Returns the description.
	 */
	public String getDescription() {
		return Description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		Description = description;
	}

	/**
	 * Gets the first value.
	 *
	 * @return Returns the firstValue.
	 */
	public String getFirstValue() {
		return firstValue;
	}

	/**
	 * Sets the first value.
	 *
	 * @param firstValue
	 *            The firstValue to set.
	 */
	public void setFirstValue(String firstValue) {
		this.firstValue = firstValue;
	}

	/**
	 * Gets the name.
	 *
	 * @return Returns the name.
	 */
	public String getName() {
		return Name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		Name = name;
	}

	/**
	 * Gets the second value.
	 *
	 * @return Returns the secondValue.
	 */
	public String getSecondValue() {
		return secondValue;
	}

	/**
	 * Sets the second value.
	 *
	 * @param secondValue
	 *            The secondValue to set.
	 */
	public void setSecondValue(String secondValue) {
		this.secondValue = secondValue;
	}

	/**
	 * Gets the value type cd.
	 *
	 * @return Returns the valueTypeCd.
	 */
	public String getValueTypeCd() {
		return valueTypeCd;
	}

	/**
	 * Sets the value type cd.
	 *
	 * @param valueTypeCd
	 *            The valueTypeCd to set.
	 */
	public void setValueTypeCd(String valueTypeCd) {
		this.valueTypeCd = valueTypeCd;
	}

	/*
	 * public static Check load(SourceBean sb) throws EMFUserError {
	 * CheckDAOImpl repChecksDAO = new CheckDAOImpl(); return
	 * (Check)repChecksDAO.load(sb);
	 *
	 * }
	 *
	 * public void modify() throws EMFUserError { CheckDAOImpl repChecksDAO =
	 * new CheckDAOImpl(); repChecksDAO.modify(this); }
	 *
	 * public void erase() throws EMFUserError { CheckDAOImpl repChecksDAO = new
	 * CheckDAOImpl(); repChecksDAO.erase(this); }
	 *
	 * public void insert() throws EMFUserError { CheckDAOImpl repChecksDAO =
	 * new CheckDAOImpl(); repChecksDAO.insert(this); }
	 */
	/**
	 * Gets the check id.
	 *
	 * @return Returns the CheckId.
	 */
	public Integer getCheckId() {
		return checkId;
	}

	/**
	 * Sets the check id.
	 *
	 * @param checkId
	 *            The checkId to set.
	 */
	public void setCheckId(Integer checkId) {
		this.checkId = checkId;
	}

	/**
	 * Gets the value type id.
	 *
	 * @return Returns the ValueTypeId.
	 */

	public Integer getValueTypeId() {
		return valueTypeId;
	}

	/**
	 * Sets the value type id.
	 *
	 * @param valueTypeId
	 *            The valueTypeId to set.
	 */
	public void setValueTypeId(Integer valueTypeId) {
		this.valueTypeId = valueTypeId;
	}

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label
	 *            the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
}
