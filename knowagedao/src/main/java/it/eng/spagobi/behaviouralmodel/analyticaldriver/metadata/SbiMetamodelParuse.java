package it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiMetamodelParuse extends SbiHibernateModel {

	private Integer id;
	private SbiParuse sbiParuse;
	private SbiMetaModelParameter sbiMetaModelPar;
	private SbiMetaModelParameter sbiMetaModelParFather;
	private String filterOperation;
	private Integer prog;
	private String filterColumn;
	private String preCondition;
	private String postCondition;
	private String logicOperator;

	public SbiMetamodelParuse() {
	}

	/**
	 * constructor with id.
	 *
	 * @param id
	 *            the id
	 */
	public SbiMetamodelParuse(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public SbiParuse getSbiParuse() {
		return sbiParuse;
	}

	public void setSbiParuse(SbiParuse sbiParuse) {
		this.sbiParuse = sbiParuse;
	}

	public SbiMetaModelParameter getSbiMetaModelPar() {
		return sbiMetaModelPar;
	}

	public void setSbiMetaModelPar(SbiMetaModelParameter sbiMetaModelPar) {
		this.sbiMetaModelPar = sbiMetaModelPar;
	}

	public SbiMetaModelParameter getSbiMetaModelParFather() {
		return sbiMetaModelParFather;
	}

	public void setSbiMetaModelParFather(SbiMetaModelParameter sbiMetaModelParFather) {
		this.sbiMetaModelParFather = sbiMetaModelParFather;
	}

	public String getFilterOperation() {
		return filterOperation;
	}

	public void setFilterOperation(String filterOperation) {
		this.filterOperation = filterOperation;
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

	public String getPreCondition() {
		return preCondition;
	}

	public void setPreCondition(String preCondition) {
		this.preCondition = preCondition;
	}

	public String getLogicOperator() {
		return logicOperator;
	}

	public void setLogicOperator(String logicOperator) {
		this.logicOperator = logicOperator;
	}

	public String getPostCondition() {
		return postCondition;
	}

	public void setPostCondition(String postCondition) {
		this.postCondition = postCondition;
	}

}
