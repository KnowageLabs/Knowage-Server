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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
@Table(name = "SBI_FUNCTION_INPUT_COLUMN")
@EntityListeners(TenantListener.class)
@FilterDef(name = "organization", parameters = {
		@ParamDef(name = "organization", type = "string")
})
@Filter(name = "organization", condition = "organization like :organization")
@NamedQueries({
	@NamedQuery(name = "SbiFunctionInputColumn.delete", query = "DELETE FROM SbiFunctionInputColumn q WHERE q.id.colName = :colName AND q.id.functionId = :functionId")
})
public class SbiFunctionInputColumn extends AbstractEntity {

	@Embeddable
	public static class Pk implements Serializable {

		private static final long serialVersionUID = -7475483116085747667L;

		@Column(name = "FUNCTION_UUID", insertable = false, updatable = false)
		private String functionId;

		@Column(name = "COL_NAME", nullable = false, updatable = false)
		@Size(max = 100)
		private String colName;

		public String getFunctionId() {
			return functionId;
		}

		public void setFunctionId(String functionId) {
			this.functionId = functionId;
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
			result = prime * result + ((functionId == null) ? 0 : functionId.hashCode());
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
			if (functionId == null) {
				if (other.functionId != null)
					return false;
			} else if (!functionId.equals(other.functionId))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Pk [functionId=" + functionId + ", colName=" + colName + "]";
		}

	}

	@EmbeddedId
	private Pk id = new Pk();

	@ManyToOne
	@JoinColumn(name = "FUNCTION_UUID", referencedColumnName = "FUNCTION_UUID", insertable = false, updatable = false)
	@MapsId("functionId")
	private SbiCatalogFunction function;

	@Column(name = "COL_TYPE")
	@NotNull
	@Size(max = 100)
	private String colType;

	public Pk getId() {
		return id;
	}

	public void setId(Pk id) {
		this.id = id;
	}

	public SbiCatalogFunction getFunction() {
		return function;
	}

	public void setFunction(SbiCatalogFunction function) {
		this.function = function;
	}

	public String getColType() {
		return colType;
	}

	public void setColType(String colType) {
		this.colType = colType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		SbiFunctionInputColumn other = (SbiFunctionInputColumn) obj;
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
		return "SbiFunctionInputColumn [id=" + id + ", colType=" + colType + "]";
	}

}
