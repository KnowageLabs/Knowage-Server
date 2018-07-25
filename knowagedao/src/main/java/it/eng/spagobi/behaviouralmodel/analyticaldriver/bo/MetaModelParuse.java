package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

import java.io.Serializable;

public class MetaModelParuse extends AbstractParuse implements Serializable {

	private Integer metamodelParId;
	private Integer useId;
	private Integer metaModelParFatherId;
	private String metaModelParFatherUrlName;

	public Integer getMetamodelParId() {
		return metamodelParId;
	}

	public void setMetamodelParId(Integer metamodelParId) {
		this.metamodelParId = metamodelParId;
	}

	public Integer getUseId() {
		return useId;
	}

	public void setUseId(Integer useId) {
		this.useId = useId;
	}

	public Integer getMetaModelParFatherId() {
		return metaModelParFatherId;
	}

	public void setMetaModelParFatherId(Integer metaModelParFatherId) {
		this.metaModelParFatherId = metaModelParFatherId;
	}

	public String getMetaModelParFatherUrlName() {
		return metaModelParFatherUrlName;
	}

	public void setMetaModelParFatherUrlName(String metaModelParFatherUrlName) {
		this.metaModelParFatherUrlName = metaModelParFatherUrlName;
	}

}
