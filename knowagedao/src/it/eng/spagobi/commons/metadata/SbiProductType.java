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
package it.eng.spagobi.commons.metadata;

import java.util.HashSet;
import java.util.Set;

public class SbiProductType extends SbiHibernateModel {
	// Fields

	private Integer productTypeId;
	private String label;
	private Set sbiOrganizationProductType = new HashSet(0);
	private Set sbiUserFunctionality = new HashSet(0);
	private Set sbiAuthorizations = new HashSet(0);
	private Set sbiProductTypeEngine = new HashSet(0);

	public SbiProductType() {
	}

	public SbiProductType(Integer productTypeId) {
		this.productTypeId = productTypeId;
	}

	public Integer getProductTypeId() {
		return productTypeId;
	}

	public void setProductTypeId(Integer productTypeId) {
		this.productTypeId = productTypeId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Set getSbiOrganizationProductType() {
		return sbiOrganizationProductType;
	}

	public void setSbiOrganizationProductType(Set sbiOrganizationProductType) {
		this.sbiOrganizationProductType = sbiOrganizationProductType;
	}

	public Set getSbiUserFunctionality() {
		return sbiUserFunctionality;
	}

	public void setSbiUserFunctionality(Set sbiUserFunctionality) {
		this.sbiUserFunctionality = sbiUserFunctionality;
	}

	public Set getSbiAuthorizations() {
		return sbiAuthorizations;
	}

	public void setSbiAuthorizations(Set sbiAuthorizations) {
		this.sbiAuthorizations = sbiAuthorizations;
	}

	public Set getSbiProductTypeEngine() {
		return sbiProductTypeEngine;
	}

	public void setSbiProductTypeEngine(Set sbiProductTypeEngine) {
		this.sbiProductTypeEngine = sbiProductTypeEngine;
	}

}
