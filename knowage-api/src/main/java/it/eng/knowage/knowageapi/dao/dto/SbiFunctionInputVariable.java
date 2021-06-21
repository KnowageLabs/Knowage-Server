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
			return "Pk [functionId=" + functionId + ", colName=" + varName + "]";
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
		int result = 1;
		result = prime * result + ((function == null) ? 0 : function.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((organization == null) ? 0 : organization.hashCode());
		result = prime * result + ((sbiVersionDe == null) ? 0 : sbiVersionDe.hashCode());
		result = prime * result + ((sbiVersionIn == null) ? 0 : sbiVersionIn.hashCode());
		result = prime * result + ((sbiVersionUp == null) ? 0 : sbiVersionUp.hashCode());
		result = prime * result + ((timeDe == null) ? 0 : timeDe.hashCode());
		result = prime * result + ((timeIn == null) ? 0 : timeIn.hashCode());
		result = prime * result + ((timeUp == null) ? 0 : timeUp.hashCode());
		result = prime * result + ((userDe == null) ? 0 : userDe.hashCode());
		result = prime * result + ((userIn == null) ? 0 : userIn.hashCode());
		result = prime * result + ((userUp == null) ? 0 : userUp.hashCode());
		result = prime * result + ((varType == null) ? 0 : varType.hashCode());
		result = prime * result + ((varValue == null) ? 0 : varValue.hashCode());
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
		SbiFunctionInputVariable other = (SbiFunctionInputVariable) obj;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (organization == null) {
			if (other.organization != null)
				return false;
		} else if (!organization.equals(other.organization))
			return false;
		if (sbiVersionDe == null) {
			if (other.sbiVersionDe != null)
				return false;
		} else if (!sbiVersionDe.equals(other.sbiVersionDe))
			return false;
		if (sbiVersionIn == null) {
			if (other.sbiVersionIn != null)
				return false;
		} else if (!sbiVersionIn.equals(other.sbiVersionIn))
			return false;
		if (sbiVersionUp == null) {
			if (other.sbiVersionUp != null)
				return false;
		} else if (!sbiVersionUp.equals(other.sbiVersionUp))
			return false;
		if (timeDe == null) {
			if (other.timeDe != null)
				return false;
		} else if (!timeDe.equals(other.timeDe))
			return false;
		if (timeIn == null) {
			if (other.timeIn != null)
				return false;
		} else if (!timeIn.equals(other.timeIn))
			return false;
		if (timeUp == null) {
			if (other.timeUp != null)
				return false;
		} else if (!timeUp.equals(other.timeUp))
			return false;
		if (userDe == null) {
			if (other.userDe != null)
				return false;
		} else if (!userDe.equals(other.userDe))
			return false;
		if (userIn == null) {
			if (other.userIn != null)
				return false;
		} else if (!userIn.equals(other.userIn))
			return false;
		if (userUp == null) {
			if (other.userUp != null)
				return false;
		} else if (!userUp.equals(other.userUp))
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
		return "SbiFunctionInputVariable [id=" + id + ", function=" + function + ", varValue=" + varValue + ", varType=" + varType + ", sbiVersionDe="
				+ sbiVersionDe + ", sbiVersionIn=" + sbiVersionIn + ", sbiVersionUp=" + sbiVersionUp + ", timeDe=" + timeDe + ", timeIn=" + timeIn + ", timeUp="
				+ timeUp + ", userDe=" + userDe + ", userIn=" + userIn + ", userUp=" + userUp + ", organization=" + organization + "]";
	}

}
