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

import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.services.validation.Xss;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

public class ProfileAttribute implements Serializable {
	@NotNull
	private Integer attributeId;
	@NotNull
	@Xss
	private String attributeName = "";
	@Xss
	private String attributeDescription = "";

	public ProfileAttribute() {
	}

	public ProfileAttribute(SbiAttribute sa) {
		this.attributeId = sa.getAttributeId();
		this.attributeName = sa.getAttributeName();
		this.attributeDescription = sa.getDescription();
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

}
