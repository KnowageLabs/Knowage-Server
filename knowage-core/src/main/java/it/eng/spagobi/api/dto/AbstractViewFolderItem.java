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

package it.eng.spagobi.api.dto;

import java.time.Instant;

import org.apache.commons.lang3.builder.CompareToBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author Marco Libanori
 * @since KNOWAGE_TM-513
 */
public abstract class AbstractViewFolderItem implements Comparable<AbstractViewFolderItem> {

	private String id;

	private Integer biObjectId;

	private String biObjectTypeCode;

	private String parentId;

	@JsonInclude(Include.NON_NULL)
	private Instant created;

	@JsonInclude(Include.NON_NULL)
	private Instant updated;

	@Override
	public int compareTo(AbstractViewFolderItem o) {
		return new CompareToBuilder()
				.append(this.getName(), o.getName())
				.append(this.getType(), o.getType())
				.append(this.getId(), o.getId())
				.toComparison();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractViewFolderItem other = (AbstractViewFolderItem) obj;
		if (getType() == null) {
			if (other.getType() != null)
				return false;
		} else if (!getType().equals(other.getType()))
			return false;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;

		return true;
	}

	/**
	 * @return the biObjectId
	 */
	public Integer getBiObjectId() {
		return biObjectId;
	}

	/**
	 * @return the biObjectTypeCode
	 */
	public String getBiObjectTypeCode() {
		return biObjectTypeCode;
	}

	/**
	 * @return the created
	 */
	public Instant getCreated() {
		return created;
	}

	/**
	 * @return the description
	 */
	public abstract String getDescription();

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the label
	 */
	public abstract String getLabel();

	/**
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * @return the parentId
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * @return the type
	 */
	public abstract String getType();

	/**
	 * @return the updated
	 */
	public Instant getUpdated() {
		return updated;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	/**
	 * @param biObjectId the biObjectId to set
	 */
	public void setBiObjectId(Integer biObjectId) {
		this.biObjectId = biObjectId;
	}

	/**
	 * @param biObjectTypeCode the biObjectTypeCode to set
	 */
	public void setBiObjectTypeCode(String biObjectTypeCode) {
		this.biObjectTypeCode = biObjectTypeCode;
	}

	/**
	 * @param created the created to set
	 */
	public void setCreated(Instant created) {
		this.created = created;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * @param updated the updated to set
	 */
	public void setUpdated(Instant updated) {
		this.updated = updated;
	}

}
