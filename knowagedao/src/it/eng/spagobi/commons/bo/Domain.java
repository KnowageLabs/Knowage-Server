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

import it.eng.spagobi.commons.utilities.messages.MessageBuilder;

import java.io.Serializable;
import java.util.Locale;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Defines a Domain object.
 */
@JsonInclude(Include.NON_NULL)
public class Domain implements Serializable {

	private Integer valueId;
	@NotNull
	private String valueCd = "";
	@NotNull
	private String valueName = "";
	private String valueDescription = "";
	private String domainCode = "";
	private String domainName = "";

	/**
	 * Gets the value cd.
	 *
	 * @return Returns the valueCd.
	 */

	public Domain() {
	}

	public String getValueCd() {
		return valueCd;
	}

	/**
	 * Sets the value cd.
	 *
	 * @param valueCd
	 *            The valueCd to set.
	 */
	public void setValueCd(String valueCd) {
		this.valueCd = valueCd;
	}

	/**
	 * Gets the value id.
	 *
	 * @return Returns the valueId.
	 */
	public Integer getValueId() {
		return valueId;
	}

	/**
	 * Sets the value id.
	 *
	 * @param valueId
	 *            The valueId to set.
	 */
	public void setValueId(Integer valueId) {
		this.valueId = valueId;
	}

	/**
	 * Gets the value name.
	 *
	 * @return Returns the valueName.
	 */
	public String getValueName() {
		return valueName;
	}

	/**
	 * Sets the value name.
	 *
	 * @param valueName
	 *            The valueName to set.
	 */
	public void setValueName(String valueName) {
		this.valueName = valueName;
	}

	/**
	 * Gets the domain code.
	 *
	 * @return the domain code
	 */
	public String getDomainCode() {
		return domainCode;
	}

	/**
	 * Sets the domain code.
	 *
	 * @param domainCode
	 *            the new domain code
	 */
	public void setDomainCode(String domainCode) {
		this.domainCode = domainCode;
	}

	/**
	 * Gets the domain name.
	 *
	 * @return the domain name
	 */
	public String getDomainName() {
		return domainName;
	}

	/**
	 * Sets the domain name.
	 *
	 * @param domainName
	 *            the new domain name
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	/**
	 * Gets the value description.
	 *
	 * @return the value description
	 */
	public String getValueDescription() {
		return valueDescription;
	}

	/**
	 * Sets the value description.
	 *
	 * @param valueDescription
	 *            the new value description
	 */
	public void setValueDescription(String valueDescription) {
		this.valueDescription = valueDescription;
	}

	public String getTranslatedValueName(Locale locale) {
		String toReturn = new String(valueName);
		MessageBuilder msgBuild = new MessageBuilder();
		toReturn = msgBuild.getMessage(toReturn, locale);
		return toReturn;

	}

	public String getTranslatedValueDescription(Locale locale) {
		String toReturn = new String(valueDescription);
		MessageBuilder msgBuild = new MessageBuilder();
		toReturn = msgBuild.getMessage(toReturn, locale);
		return toReturn;
	}

	public void setTranslatedValueName(String vn) {
		// Do nothing
	}

	public void setTranslatedValueDescription(String vn) {
		// Do nothing
	}

	public String getTranslatedValueName() {
		String toReturn = new String(valueName);
		MessageBuilder msgBuild = new MessageBuilder();
		toReturn = msgBuild.getMessage(toReturn);
		return toReturn;

	}

	public String getTranslatedValueDescription() {
		String toReturn = new String(valueDescription);
		MessageBuilder msgBuild = new MessageBuilder();
		toReturn = msgBuild.getMessage(toReturn);
		return toReturn;
	}
}
