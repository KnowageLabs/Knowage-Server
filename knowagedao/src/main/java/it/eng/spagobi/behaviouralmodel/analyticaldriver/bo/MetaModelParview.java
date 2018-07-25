package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

import java.io.Serializable;

public class MetaModelParview extends AbstractParview implements Serializable {

	private Integer parviewId;
	private Integer metaModelParId;
	private Integer metaModelParFatherId;
	private String metaModelParFatherUrlName;

	public Integer getParviewId() {
		return parviewId;
	}

	public void setParviewId(Integer parviewId) {
		this.parviewId = parviewId;
	}

	public Integer getMetaModelParId() {
		return metaModelParId;
	}

	public void setMetaModelParId(Integer metaModelParId) {
		this.metaModelParId = metaModelParId;
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
