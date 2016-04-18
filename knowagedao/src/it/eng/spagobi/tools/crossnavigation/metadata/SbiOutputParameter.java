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

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiOutputParameter extends SbiHibernateModel {

	private static final long serialVersionUID = -5492568111739841068L;
	/**
	 * 
	 */
	private Integer id;
	private Integer biobjId;
	private Integer parameterTypeId;
	private String label;
	private String formatCode;
	private String formatValue;
	private SbiObjects sbiObject;
	private SbiDomains parameterType;

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
	 * @return the label
	 */
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
	 * @return the biobjId
	 */
	public Integer getBiobjId() {
		return biobjId;
	}

	/**
	 * @param biobjId
	 *            the biobjId to set
	 */
	public void setBiobjId(Integer biobjId) {
		this.biobjId = biobjId;
	}

	/**
	 * @return the biobj
	 */
	public SbiObjects getSbiObject() {
		return sbiObject;
	}

	/**
	 * @param biobj
	 *            the biobj to set
	 */
	public void setSbiObject(SbiObjects sbiObject) {
		this.sbiObject = sbiObject;
	}

	/**
	 * @return the parameterTypeId
	 */
	public Integer getParameterTypeId() {
		return parameterTypeId;
	}

	/**
	 * @param parameterTypeId
	 *            the parameterTypeId to set
	 */
	public void setParameterTypeId(Integer parameterTypeId) {
		this.parameterTypeId = parameterTypeId;
	}

	/**
	 * @return the parameterType
	 */
	public SbiDomains getParameterType() {
		return parameterType;
	}

	/**
	 * @param parameterType
	 *            the parameterType to set
	 */
	public void setParameterType(SbiDomains parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * @return the formatCode
	 */
	public String getFormatCode() {
		return formatCode;
	}

	/**
	 * @param formatCode
	 *            the formatCode to set
	 */
	public void setFormatCode(String formatCode) {
		this.formatCode = formatCode;
	}

	/**
	 * @return the formatValue
	 */
	public String getFormatValue() {
		return formatValue;
	}

	/**
	 * @param formatValue
	 *            the formatValue to set
	 */
	public void setFormatValue(String formatValue) {
		this.formatValue = formatValue;
	}

}
