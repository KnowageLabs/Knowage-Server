package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

import java.io.Serializable;

public class MetaModelParuse implements Serializable {

	private Integer paruseId;
	private Integer metamodelParId;
	private Integer useId;
	private Integer metaModelParFatherId;
	private Integer prog;
	private String filterColumn;
	private String filterOperation;
	private String preCondition;
	private String postCondition;
	private String logicOperator;
	private String metaModelParFatherUrlName;

	public Integer getParuseId() {
		return paruseId;
	}

	public void setParuseId(Integer paruseId) {
		this.paruseId = paruseId;
	}

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

	public void setMetaModelParFatherId(Integer objParFatherId) {
		this.metaModelParFatherId = objParFatherId;
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

	public String getMetaModelParFatherUrlName() {
		return metaModelParFatherUrlName;
	}

	public void setMetaModelParFatherUrlName(String objParFatherUrlName) {
		this.metaModelParFatherUrlName = objParFatherUrlName;
	}
}
