package it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiMetaModelParview extends SbiHibernateModel {

	private Integer parviewId;
	private SbiMetaModelParameter sbiMetaModelPar;
	private SbiMetaModelParameter sbiMetaModelFather;
	private String operation;
	private String compareValue;
	private Integer prog;
	private String viewLabel;

	public SbiMetaModelParview() {
	}

	public SbiMetaModelParview(Integer id) {
		this.parviewId = id;
	}

	public Integer getParviewId() {
		return parviewId;
	}

	public void setParviewId(Integer parviewId) {
		this.parviewId = parviewId;
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

	public Integer getProg() {
		return prog;
	}

	public void setProg(Integer prog) {
		this.prog = prog;
	}

	public String getViewLabel() {
		return viewLabel;
	}

	public void setViewLabel(String viewLabel) {
		this.viewLabel = viewLabel;
	}

	public SbiMetaModelParameter getSbiMetaModelPar() {
		return sbiMetaModelPar;
	}

	public void setSbiMetaModelPar(SbiMetaModelParameter sbiMetaModelPar) {
		this.sbiMetaModelPar = sbiMetaModelPar;
	}

	public SbiMetaModelParameter getSbiMetaModelFather() {
		return sbiMetaModelFather;
	}

	public void setSbiMetaModelFather(SbiMetaModelParameter sbiMetaModelFather) {
		this.sbiMetaModelFather = sbiMetaModelFather;
	}

}