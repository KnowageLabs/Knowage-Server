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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import it.eng.knowage.boot.dao.dto.AbstractEntity;

/**
 * @author Marco Libanori
 */
@Entity
@Table(name = "SBI_FUNCTION_OUTPUT_COLUMN")
public class SbiFunctionOutputColumn extends AbstractEntity implements Comparable<SbiFunctionOutputColumn> {

	@Embeddable
	public static class Pk implements AbstractSbiCatalogFunctionForeignKey {

		private static final long serialVersionUID = -292836728995936381L;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "FUNCTION_UUID", referencedColumnName = "FUNCTION_UUID", insertable = false, updatable = false)
		@JoinColumn(name = "ORGANIZATION", referencedColumnName = "ORGANIZATION", insertable = false, updatable = false)
		private SbiCatalogFunction function;

		@Column(name = "COL_NAME")
		@Size(max = 100)
		private String colName;

		@Override
		public SbiCatalogFunction getFunction() {
			return function;
		}

		@Override
		public void setFunction(SbiCatalogFunction function) {
			this.function = function;
		}

		public String getColName() {
			return colName;
		}

		public void setColName(String colName) {
			this.colName = colName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((colName == null) ? 0 : colName.hashCode());
			result = prime * result + ((function == null) ? 0 : function.hashCode());
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
			if (colName == null) {
				if (other.colName != null)
					return false;
			} else if (!colName.equals(other.colName))
				return false;
			if (function == null) {
				if (other.function != null)
					return false;
			} else if (!function.equals(other.function))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Pk [function=" + function + ", colName=" + colName + "]";
		}

	}

	@EmbeddedId
	private Pk id = new Pk();

	@Column(name = "COL_TYPE")
	@NotNull
	@Size(max = 100)
	private String colType;

	@Column(name = "COL_FIELD_TYPE")
	@NotNull
	@Size(max = 100)
	private String colFieldType;

	public Pk getId() {
		return id;
	}

	public void setId(Pk id) {
		this.id = id;
	}

	public String getColType() {
		return colType;
	}

	public void setColType(String colType) {
		this.colType = colType;
	}

	public String getColFieldType() {
		return colFieldType;
	}

	public void setColFieldType(String colFieldType) {
		this.colFieldType = colFieldType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((colFieldType == null) ? 0 : colFieldType.hashCode());
		result = prime * result + ((colType == null) ? 0 : colType.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		SbiFunctionOutputColumn other = (SbiFunctionOutputColumn) obj;
		if (colFieldType == null) {
			if (other.colFieldType != null)
				return false;
		} else if (!colFieldType.equals(other.colFieldType))
			return false;
		if (colType == null) {
			if (other.colType != null)
				return false;
		} else if (!colType.equals(other.colType))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SbiFunctionOutputColumn [id=" + id + ", colType=" + colType + ", colFieldType=" + colFieldType + "]";
	}

	@Override
	public int compareTo(SbiFunctionOutputColumn o) {
		String thisColName = Optional.ofNullable(this.id).map(e -> e.colName).orElse("");
		String otheColName = Optional.ofNullable(o).map(e -> e.id).map(e -> e.colName).orElse("");

		return thisColName.compareTo(otheColName);
	}

}
