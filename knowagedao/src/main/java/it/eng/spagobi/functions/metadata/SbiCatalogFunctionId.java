package it.eng.spagobi.functions.metadata;

import java.util.Objects;

public class SbiCatalogFunctionId implements java.io.Serializable {

	private String functionUuid;
	private String organization;

	public SbiCatalogFunctionId() {
	}

	public SbiCatalogFunctionId(String functionUuid, String organization) {
		this.functionUuid = functionUuid;
		this.organization = organization;
	}

	public String getFunctionUuid() {
		return functionUuid;
	}

	public void setFunctionUuid(String functionUuid) {
		this.functionUuid = functionUuid;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	@Override
	public int hashCode() {
		return Objects.hash(functionUuid, organization);
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
		SbiCatalogFunctionId other = (SbiCatalogFunctionId) obj;
		return Objects.equals(functionUuid, other.functionUuid) && Objects.equals(organization, other.organization);
	}

}
