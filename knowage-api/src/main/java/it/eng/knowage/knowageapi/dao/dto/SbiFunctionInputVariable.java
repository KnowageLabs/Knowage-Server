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

import javax.annotation.Nullable;
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
@Table(name = "SBI_FUNCTION_INPUT_VARIABLE")
@EntityListeners(TenantListener.class)
@FilterDef(name = "organization", parameters = {
		@ParamDef(name = "organization", type = "string")
})
@Filter(name = "organization", condition = "organization like :organization")
@NamedQueries({
	@NamedQuery(name = "SbiFunctionInputVariable.delete", query = "DELETE FROM SbiFunctionInputVariable q WHERE q.id.varName = :varName AND q.id.functionId = :functionId")
})
public class SbiFunctionInputVariable extends AbstractEntity {

	@Embeddable
	public static class Pk implements Serializable {

		private static final long serialVersionUID = 7914853138663962169L;

		@Column(name = "FUNCTION_UUID", insertable = false, updatable = false)
		private String functionId;

		@Column(name = "VAR_NAME", nullable = false, updatable = false)
		@Size(max = 100)
		private String varName;

		public String getFunctionId() {
			return functionId;
		}

		public void setFunctionId(String functionId) {
			this.functionId = functionId;
		}

		public String getVarName() {
			return varName;
		}

		public void setVarName(String colName) {
			this.varName = colName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((varName == null) ? 0 : varName.hashCode());
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
			if (varName == null) {
				if (other.varName != null)
					return false;
			} else if (!varName.equals(other.varName))
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
			return "Pk [functionId=" + functionId + ", varName=" + varName + "]";
		}

	}

	@EmbeddedId
	private Pk id = new Pk();

	@ManyToOne
	@JoinColumn(name = "FUNCTION_UUID", referencedColumnName = "FUNCTION_UUID", insertable = false, updatable = false)
	@MapsId("functionId")
	private SbiCatalogFunction function;

	@Column(name = "VAR_VALUE")
	@NotNull
	@Size(max = 100)
	private String varValue;

	@Column(name = "VAR_TYPE")
	@Nullable
	@Size(max = 100)
	private String varType;

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

	public String getVarValue() {
		return varValue;
	}

	public void setVarValue(String varValue) {
		this.varValue = varValue;
	}

	public String getVarType() {
		return varType;
	}

	public void setVarType(String varType) {
		this.varType = varType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((varType == null) ? 0 : varType.hashCode());
		result = prime * result + ((varValue == null) ? 0 : varValue.hashCode());
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
		SbiFunctionInputVariable other = (SbiFunctionInputVariable) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (varType == null) {
			if (other.varType != null)
				return false;
		} else if (!varType.equals(other.varType))
			return false;
		if (varValue == null) {
			if (other.varValue != null)
				return false;
		} else if (!varValue.equals(other.varValue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SbiFunctionInputVariable [id=" + id + ", varValue=" + varValue + ", varType=" + varType + "]";
	}

}
