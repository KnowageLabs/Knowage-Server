package it.eng.spagobi.profiling.bo;

import it.eng.spagobi.profiling.bean.SbiAttribute;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

public class ProfileAttribute implements Serializable {

	private Integer attributeId;
	@NotNull
	private String attributeName = "";
	private String attributeDescription = "";

	public ProfileAttribute() {
	}

	public ProfileAttribute(SbiAttribute sa) {
		this.attributeId = sa.getAttributeId();
		this.attributeName = sa.getAttributeName();
		this.attributeDescription = sa.getDescription();
	}

	public Integer getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(Integer attributeId) {
		this.attributeId = attributeId;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeDescription() {
		return attributeDescription;
	}

	public void setAttributeDescription(String attributeDescription) {
		this.attributeDescription = attributeDescription;
	}

}
