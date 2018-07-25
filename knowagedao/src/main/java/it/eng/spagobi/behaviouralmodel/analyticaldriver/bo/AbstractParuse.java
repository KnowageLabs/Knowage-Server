package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

public abstract class AbstractParuse {

	private Integer paruseId;
	private Integer prog;
	private String filterColumn;
	private String filterOperation;
	private String preCondition;
	private String postCondition;
	private String logicOperator;

	public Integer getParuseId() {
		return paruseId;
	}

	public void setParuseId(Integer paruseId) {
		this.paruseId = paruseId;
	}

	public Integer getProg() {
		return prog;
	}

	public void setProg(Integer prog) {
		this.prog = prog;
	}

	public String getFilterColumn() {
		return filterColumn;
	}

	public void setFilterColumn(String filterColumn) {
		this.filterColumn = filterColumn;
	}

	public String getFilterOperation() {
		return filterOperation;
	}

	public void setFilterOperation(String filterOperation) {
		this.filterOperation = filterOperation;
	}

	public String getPreCondition() {
		return preCondition;
	}

	public void setPreCondition(String preCondition) {
		this.preCondition = preCondition;
	}

	public String getPostCondition() {
		return postCondition;
	}

	public void setPostCondition(String postCondition) {
		this.postCondition = postCondition;
	}

	public String getLogicOperator() {
		return logicOperator;
	}

	public void setLogicOperator(String logicOperator) {
		this.logicOperator = logicOperator;
	}

}
