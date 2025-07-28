package it.eng.spagobi.functions.metadata;

import java.util.Objects;

public class SbiFunctionInputColumnId implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7037043321436197370L;
	private String functionUuid;
	private String colName;
	private String organization;

	public SbiFunctionInputColumnId() {
	}

	public SbiFunctionInputColumnId(String functionUuid, String colName, String organization) {
		this.functionUuid = functionUuid;
		this.colName = colName;
		this.organization = organization;
	}

	public String getFunctionUuid() {
		return this.functionUuid;
	}

	private void setFunctionUuid(String functionUuid) {
		this.functionUuid = functionUuid;
	}

	public String getColName() {
		return this.colName;
	}

	private void setColName(String colName) {
		this.colName = colName;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	@Override
	public int hashCode() {
		return Objects.hash(colName, functionUuid, organization);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SbiFunctionInputColumnId other = (SbiFunctionInputColumnId) obj;
		return Objects.equals(colName, other.colName) && Objects.equals(functionUuid, other.functionUuid) && Objects.equals(organization, other.organization);
	}
}
