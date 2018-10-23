package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

public abstract class AbstractParuse {

	private Integer id;
	private Integer parFatherId;
	private Integer parId;
	private Integer useModeId;
	private String parFatherUrlName;
	private Integer prog;
	private String filterColumn;
	private String filterOperation;
	private String preCondition;
	private String postCondition;
	private String logicOperator;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getParFatherId() {
		return parFatherId;
	}

	public void setParFatherId(Integer parFatherId) {
		this.parFatherId = parFatherId;
	}

	public Integer getParId() {
		return parId;
	}

	public void setParId(Integer parId) {
		this.parId = parId;
	}

	public Integer getUseModeId() {
		return useModeId;
	}

	public void setUseModeId(Integer useModeId) {
		this.useModeId = useModeId;
	}

	public String getParFatherUrlName() {
		return parFatherUrlName;
	}

	public void setParFatherUrlName(String parFatherUrlName) {
		this.parFatherUrlName = parFatherUrlName;
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
