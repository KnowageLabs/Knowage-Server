/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

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
package it.eng.knowage.knowageapi.dao.dto;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import it.eng.knowage.knowageapi.dao.listener.TenantListener;

/**
 * @author Marco Libanori
 */
@Entity
@Table(name = "SBI_OBJ_FUNCTION")
@EntityListeners(TenantListener.class)
@FilterDef(name = "organization", parameters = {
		@ParamDef(name = "organization", type = "string")
})
@Filter(name = "organization", condition = "organization like :organization")
public class SbiObjFunction extends AbstractEntity {

	@Id
	@Column(name = "BIOBJ_FUNCTION_ID")
	private int id;

	@Column(name = "BIOBJ_ID")
	@NotNull
	private int biObjId;

	@ManyToOne
	@JoinColumn(name = "FUNCTION_UUID", referencedColumnName = "FUNCTION_UUID", insertable = false, updatable = false)
	@MapsId("functionId")
	private SbiCatalogFunction function;

	@Column(name = "FUNCTION_UUID")
	@NotNull
	private String functionId;

	@Column(name = "META_VERSION")
	@Nullable
	@Size(max = 100)
	private String metaVersion;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBiObjId() {
		return biObjId;
	}

	public void setBiObjId(int biObjId) {
		this.biObjId = biObjId;
	}

	public String getFunctionId() {
		return functionId;
	}

	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	public String getMetaVersion() {
		return metaVersion;
	}

	public void setMetaVersion(String metaVersion) {
		this.metaVersion = metaVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + biObjId;
		result = prime * result + ((functionId == null) ? 0 : functionId.hashCode());
		result = prime * result + id;
		result = prime * result + ((metaVersion == null) ? 0 : metaVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SbiObjFunction other = (SbiObjFunction) obj;
		if (biObjId != other.biObjId)
			return false;
		if (functionId == null) {
			if (other.functionId != null)
				return false;
		} else if (!functionId.equals(other.functionId))
			return false;
		if (id != other.id)
			return false;
		if (metaVersion == null) {
			if (other.metaVersion != null)
				return false;
		} else if (!metaVersion.equals(other.metaVersion))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SbiObjFunction [id=" + id + ", biObjId=" + biObjId + ", functionId=" + functionId + ", metaVersion=" + metaVersion + "]";
	}

}
