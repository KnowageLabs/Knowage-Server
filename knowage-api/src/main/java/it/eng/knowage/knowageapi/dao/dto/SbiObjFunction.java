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

import java.util.Optional;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import it.eng.knowage.boot.dao.dto.AbstractEntity;

/**
 * @author Marco Libanori
 */
@Entity
@Table(name = "SBI_OBJ_FUNCTION")
public class SbiObjFunction extends AbstractEntity implements Comparable<SbiObjFunction> {

	@Embeddable
	public static class Pk implements AbstractSbiCatalogFunctionForeignKey {

		private static final long serialVersionUID = -8082082252956174041L;

		@Column(name = "BIOBJ_FUNCTION_ID")
		private int id;

		@Column(name = "BIOBJ_ID")
		private int biObjId;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "FUNCTION_UUID", referencedColumnName = "FUNCTION_UUID", insertable = false, updatable = false)
		@JoinColumn(name = "ORGANIZATION", referencedColumnName = "ORGANIZATION", insertable = false, updatable = false)
		private SbiCatalogFunction function;

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

		@Override
		public SbiCatalogFunction getFunction() {
			return function;
		}

		@Override
		public void setFunction(SbiCatalogFunction function) {
			this.function = function;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + biObjId;
			result = prime * result + ((function == null) ? 0 : function.hashCode());
			result = prime * result + id;
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
			Pk other = (Pk) obj;
			if (biObjId != other.biObjId)
				return false;
			if (function == null) {
				if (other.function != null)
					return false;
			} else if (!function.equals(other.function))
				return false;
			if (id != other.id)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Pk [id=" + id + ", biObjId=" + biObjId + ", function=" + function + "]";
		}
	}

	@EmbeddedId
	private Pk id = new Pk();

	@Column(name = "META_VERSION")
	@Nullable
	@Size(max = 100)
	private String metaVersion;

	public String getMetaVersion() {
		return metaVersion;
	}

	public void setMetaVersion(String metaVersion) {
		this.metaVersion = metaVersion;
	}

	public Pk getId() {
		return id;
	}

	public void setId(Pk id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((metaVersion == null) ? 0 : metaVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SbiObjFunction other = (SbiObjFunction) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
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
		return "SbiObjFunction [id=" + id + ", metaVersion=" + metaVersion + "]";
	}

	@Override
	public int compareTo(SbiObjFunction o) {
		Integer thisId = Optional.ofNullable(this.id).map(e -> e.id).orElse(0);
		Integer otherId = Optional.ofNullable(o).map(e -> e.id).map(e -> e.id).orElse(0);

		return thisId.compareTo(otherId);
	}

}
