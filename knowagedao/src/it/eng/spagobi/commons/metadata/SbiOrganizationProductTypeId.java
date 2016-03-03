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


public class SbiOrganizationProductTypeId implements java.io.Serializable {

	private int productTypeId;
	private int organizationId;

	public SbiOrganizationProductTypeId() {
	}

	public SbiOrganizationProductTypeId(int productTypeId, int organizationId) {
		this.productTypeId = productTypeId;
		this.organizationId = organizationId;
	}

	public int getProductTypeId() {
		return productTypeId;
	}

	public void setProductTypeId(int productTypeId) {
		this.productTypeId = productTypeId;
	}

	public int getOrganizationId() {
		return this.organizationId;
	}

	public void setOrganizationId(int organizationId) {
		this.organizationId = organizationId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SbiOrganizationProductTypeId))
			return false;
		SbiOrganizationProductTypeId castOther = (SbiOrganizationProductTypeId) other;

		return (this.getProductTypeId() == castOther.getProductTypeId())
				&& (this.getOrganizationId() == castOther.getOrganizationId());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getProductTypeId();
		result = 37 * result + this.getOrganizationId();
		return result;
	}

}
