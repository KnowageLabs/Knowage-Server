package it.eng.spagobi.commons.metadata;

import java.util.HashSet;
import java.util.Set;

public class SbiProductType extends SbiHibernateModel {
	// Fields

	private Integer productTypeId;
	private String label;
	private Set sbiOrganizationProductType = new HashSet(0);
	private Set sbiUserFunctionality = new HashSet(0);
	private Set sbiAuthorizations = new HashSet(0);

	public SbiProductType() {
	}

	public SbiProductType(Integer productTypeId) {
		this.productTypeId = productTypeId;
	}

	public Integer getProductTypeId() {
		return productTypeId;
	}

	public void setProductTypeId(Integer productTypeId) {
		this.productTypeId = productTypeId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Set getSbiOrganizationProductType() {
		return sbiOrganizationProductType;
	}

	public void setSbiOrganizationProductType(Set sbiOrganizationProductType) {
		this.sbiOrganizationProductType = sbiOrganizationProductType;
	}

	public Set getSbiUserFunctionality() {
		return sbiUserFunctionality;
	}

	public void setSbiUserFunctionality(Set sbiUserFunctionality) {
		this.sbiUserFunctionality = sbiUserFunctionality;
	}

	public Set getSbiAuthorizations() {
		return sbiAuthorizations;
	}

	public void setSbiAuthorizations(Set sbiAuthorizations) {
		this.sbiAuthorizations = sbiAuthorizations;
	}

}
