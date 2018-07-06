package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

import java.io.Serializable;

public class MetaModelParview implements Serializable {

	private Integer parviewId;
	private Integer metaModelParId;
	private Integer metaModelParFatherId;
	private Integer prog;
	private String operation;
	private String compareValue;
	private String viewLabel;
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

	public Integer getProg() {
		return prog;
	}

	public void setProg(Integer prog) {
		this.prog = prog;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getCompareValue() {
		return compareValue;
	}

	public void setCompareValue(String compareValue) {
		this.compareValue = compareValue;
	}

	public String getViewLabel() {
		return viewLabel;
	}

	public void setViewLabel(String viewLabel) {
		this.viewLabel = viewLabel;
	}

	public String getMetaModelParFatherUrlName() {
		return metaModelParFatherUrlName;
	}

	public void setMetaModelParFatherUrlName(String metaModelParFatherUrlName) {
		this.metaModelParFatherUrlName = metaModelParFatherUrlName;
	}

}
