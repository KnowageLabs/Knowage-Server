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
import java.util.Locale;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.services.validation.ExtendedAlphanumeric;

/**
 * Defines a Domain object.
 */
@JsonInclude(Include.NON_NULL)
public class Domain implements Serializable {

	private static final long serialVersionUID = 3795397248242537831L;

	/**
	 *
	 * @param category
	 * @return
	 * @deprecated Introduced for compatibility between {@link SbiCategory} and {@link Domain}.
	 *   All the code referencing this should be changed to use {@link SbiCategory}.
	 */
	@Deprecated
	public static Domain fromCategory(SbiCategory category) {
		Domain ret = new Domain();

		ret.setValueCd(category.getName());
		ret.setValueName(category.getName());
		ret.setDomainCode(category.getType());
		ret.setDomainName(category.getType());
		ret.setValueDescription(category.getName());
		ret.setValueId(category.getId());

		return ret;
	}

	private Integer valueId;

	@NotEmpty
	@ExtendedAlphanumeric
	@Size(max = 100)
	private String valueCd = "";

	@NotEmpty
	@ExtendedAlphanumeric
	@Size(max = 40)
	private String valueName = "";

	@NotEmpty
	@ExtendedAlphanumeric
	@Size(max = 160)
	private String valueDescription = "";

	@NotEmpty
	@ExtendedAlphanumeric
	@Size(max = 20)
	private String domainCode = "";

	@NotEmpty
	@ExtendedAlphanumeric
	@Size(max = 40)
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
		if (valueName != null) {
			return new MessageBuilder().getMessage(valueName, locale);
		} else {
			return "";
		}
	}

	public String getTranslatedValueDescription(Locale locale) {
		if (valueDescription != null) {
			return new MessageBuilder().getMessage(valueDescription, locale);
		} else {
			return "";
		}
	}

	public void setTranslatedValueName(String vn) {
		// Do nothing
	}

	public void setTranslatedValueDescription(String vn) {
		// Do nothing
	}

	public String getTranslatedValueName() {
		return getTranslatedValueName(null);
	}

	public String getTranslatedValueDescription() {
		return getTranslatedValueDescription(null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domainCode == null) ? 0 : domainCode.hashCode());
		result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
		result = prime * result + ((valueCd == null) ? 0 : valueCd.hashCode());
		result = prime * result + ((valueDescription == null) ? 0 : valueDescription.hashCode());
		result = prime * result + ((valueId == null) ? 0 : valueId.hashCode());
		result = prime * result + ((valueName == null) ? 0 : valueName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Domain))
			return false;
		Domain other = (Domain) obj;
		if (domainCode == null) {
			if (other.domainCode != null)
				return false;
		} else if (!domainCode.equals(other.domainCode))
			return false;
		if (domainName == null) {
			if (other.domainName != null)
				return false;
		} else if (!domainName.equals(other.domainName))
			return false;
		if (valueCd == null) {
			if (other.valueCd != null)
				return false;
		} else if (!valueCd.equals(other.valueCd))
			return false;
		if (valueDescription == null) {
			if (other.valueDescription != null)
				return false;
		} else if (!valueDescription.equals(other.valueDescription))
			return false;
		if (valueId == null) {
			if (other.valueId != null)
				return false;
		} else if (!valueId.equals(other.valueId))
			return false;
		if (valueName == null) {
			if (other.valueName != null)
				return false;
		} else if (!valueName.equals(other.valueName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Domain [valueId=");
		builder.append(valueId);
		builder.append(", valueCd=");
		builder.append(valueCd);
		builder.append(", valueName=");
		builder.append(valueName);
		builder.append(", valueDescription=");
		builder.append(valueDescription);
		builder.append(", domainCode=");
		builder.append(domainCode);
		builder.append(", domainName=");
		builder.append(domainName);
		builder.append("]");
		return builder.toString();
	}
}
