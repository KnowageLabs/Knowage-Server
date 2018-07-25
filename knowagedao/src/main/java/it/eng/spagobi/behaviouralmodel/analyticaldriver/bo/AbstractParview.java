package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

public abstract class AbstractParview {

	private Integer prog;
	private String operation;
	private String compareValue;
	private String viewLabel;

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
