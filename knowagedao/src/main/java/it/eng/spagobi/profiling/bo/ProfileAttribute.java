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
package it.eng.spagobi.profiling.bo;

import java.io.Serializable;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BooleanJsonSerializer;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BooleanToShortJSONDeserializer;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.services.validation.ExtendedAlphanumeric;

public class ProfileAttribute implements Serializable {

	private Integer attributeId;

	@NotEmpty
	@ExtendedAlphanumeric
	@Size(max = 255)
	private String attributeName = "";

	@ExtendedAlphanumeric
	@Size(max = 500)
	private String attributeDescription = "";
	@JsonDeserialize(using = BooleanToShortJSONDeserializer.class)
	@JsonSerialize(using = BooleanJsonSerializer.class)
	private Short allowUser;
	@JsonDeserialize(using = BooleanToShortJSONDeserializer.class)
	@JsonSerialize(using = BooleanJsonSerializer.class)
	private Short multivalue;
	@JsonDeserialize(using = BooleanToShortJSONDeserializer.class)
	@JsonSerialize(using = BooleanJsonSerializer.class)
	private Short syntax;
	private Integer lovId;

	@Enumerated(EnumType.STRING)
	private ProfileAttributesValueTypes value;

	public ProfileAttribute() {
	}

	public ProfileAttribute(SbiAttribute sa) {
		this.attributeId = sa.getAttributeId();
		this.attributeName = sa.getAttributeName();
		this.attributeDescription = sa.getDescription();
		this.allowUser = sa.getAllowUser();
		this.lovId = sa.getLovId();
		this.syntax = sa.getSyntax();
		this.multivalue = sa.getMultivalue();
		this.value = sa.getValue();
	}

	public Integer getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(Integer attributeId) {
		this.attributeId = attributeId;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeDescription() {
		return attributeDescription;
	}

	public void setAttributeDescription(String attributeDescription) {
		this.attributeDescription = attributeDescription;
	}

	public Short getAllowUser() {
		return allowUser;
	}

	public void setAllowUser(Short allowUser) {
		this.allowUser = allowUser;
	}

	public Short getMultivalue() {
		return multivalue;
	}

	public void setMultivalue(Short multivalue) {
		this.multivalue = multivalue;
	}

	public Short getSyntax() {
		return syntax;
	}

	public void setSyntax(Short syntax) {
		this.syntax = syntax;
	}

	public Integer getLovId() {
		return lovId;
	}

	public void setLovId(Integer lovId) {
		this.lovId = lovId;
	}

	public ProfileAttributesValueTypes getValue() {
		return value;
	}

	public void setValue(ProfileAttributesValueTypes value) {
		this.value = value;
	}

}
