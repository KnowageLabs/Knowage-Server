package it.eng.spagobi.commons.metadata;


public class SbiOrganizationProductTypeId implements java.io.Serializable {

	private int productTypeId;
	private int organizationId;

	public SbiOrganizationProductTypeId() {
	}

	public SbiOrganizationProductTypeId(int productTypeId, int organizationId) {
		this.productTypeId = productTypeId;
		this.organizationId = organizationId;
	}

	public int getProductTypeId() {
		return productTypeId;
	}

	public void setProductTypeId(int productTypeId) {
		this.productTypeId = productTypeId;
	}

	public int getOrganizationId() {
		return this.organizationId;
	}

	public void setOrganizationId(int organizationId) {
		this.organizationId = organizationId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SbiOrganizationProductTypeId))
			return false;
		SbiOrganizationProductTypeId castOther = (SbiOrganizationProductTypeId) other;

		return (this.getProductTypeId() == castOther.getProductTypeId())
				&& (this.getOrganizationId() == castOther.getOrganizationId());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getProductTypeId();
		result = 37 * result + this.getOrganizationId();
		return result;
	}

}
