/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.view.metadata;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.CompareToBuilder;

import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

/**
 * @author Marco Libanori
 * @since KNOWAGE_TM-513
 */
public class SbiViewForDoc extends SbiHibernateModel implements Comparable<SbiViewForDoc>, Serializable {

	private static final long serialVersionUID = 5241995015625283956L;

	@NotNull
	private String id;

	@NotNull
	private Integer biObjId;

	@NotNull
	private SbiViewHierarchy parent;

	@Override
	public int compareTo(SbiViewForDoc o) {
		// @formatter:off
		return new CompareToBuilder()
				.append(this.id, o.id)
				.toComparison();
		// @formatter:on
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SbiViewForDoc other = (SbiViewForDoc) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;

		SbiCommonInfo commonInfo = getCommonInfo();
		String organization = commonInfo.getOrganization();

		SbiCommonInfo otherCommonInfo = other.getCommonInfo();
		String otherOrganization = otherCommonInfo.getOrganization();

		if (organization == null) {
			if (otherOrganization != null)
				return false;
		} else if (!organization.equals(otherOrganization))
			return false;

		return true;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the biObjId
	 */
	public Integer getBiObjId() {
		return biObjId;
	}

	/**
	 * @return the parent
	 */
	public SbiViewHierarchy getParent() {
		return parent;
	}

	@Override
	public int hashCode() {
		SbiCommonInfo commonInfo = getCommonInfo();
		String organization = commonInfo.getOrganization();

		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((organization == null) ? 0 : organization.hashCode());
		return result;
	}

	/**
	 * @param id the id to set
	 */
	private void setId(String id) {
		this.id = id;
	}

	/**
	 * @param biObjId the biObjId to set
	 */
	public void setBiObjId(Integer biObjId) {
		this.biObjId = biObjId;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(SbiViewHierarchy parent) {
		this.parent = parent;
	}

}
