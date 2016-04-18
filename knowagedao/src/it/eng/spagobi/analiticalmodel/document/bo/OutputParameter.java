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
package it.eng.spagobi.analiticalmodel.document.bo;

import it.eng.spagobi.commons.bo.Domain;

import java.io.Serializable;

public class OutputParameter implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private Integer id;
	private String name;
	/**
	 * domainCd = PAR_TYPE
	 */
	private Domain type;
	private Integer biObjectId;
	private String formatCode;
	private String formatValue;

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
	 * @return the biObjectId
	 */
	public Integer getBiObjectId() {
		return biObjectId;
	}

	/**
	 * @param biObjectId
	 *            the biObjectId to set
	 */
	public void setBiObjectId(Integer biObjectId) {
		this.biObjectId = biObjectId;
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

	/**
	 * @return the type
	 */
	public Domain getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Domain type) {
		this.type = type;
	}

}
