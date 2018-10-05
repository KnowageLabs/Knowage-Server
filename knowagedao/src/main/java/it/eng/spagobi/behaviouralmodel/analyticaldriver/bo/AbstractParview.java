package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

public abstract class AbstractParview {


	private Integer id;
	private Integer parId;
	private Integer parFatherId;
	private String parFatherUrlName;
	private Integer prog;
	private String operation;
	private String compareValue;
	private String viewLabel;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getParFatherUrlName() {
		return parFatherUrlName;
	}

	public void setParFatherUrlName(String parFatherUrlName) {
		this.parFatherUrlName = parFatherUrlName;
	}

	public Integer getParId() {
		return parId;
	}

	public void setParId(Integer parId) {
		this.parId = parId;
	}

	public Integer getParFatherId() {
		return parFatherId;
	}

	public void setParFatherId(Integer parFatherId) {
		this.parFatherId = parFatherId;
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

}
