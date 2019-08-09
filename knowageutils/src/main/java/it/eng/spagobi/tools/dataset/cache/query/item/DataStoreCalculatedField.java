package it.eng.spagobi.tools.dataset.cache.query.item;

import java.util.List;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

public class DataStoreCalculatedField extends AbstractCalculatedField {

	private IDataSet dataSet;
	private String name;
	private String alias;
	private List<AbstractSelectionField> projectionsList;
	private String formula;

	public DataStoreCalculatedField(IDataSet dataSet, String name, String alias, List<AbstractSelectionField> projectionsList) {
		super();
		this.dataSet = dataSet;
		this.name = name;
		this.alias = alias;
		this.projectionsList = projectionsList;
	}

	public IDataSet getDataSet() {
		return dataSet;
	}
	public void setDataSet(IDataSet dataSet) {
		this.dataSet = dataSet;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public List<AbstractSelectionField> getProjectionsList() {
		return projectionsList;
	}
	public void setProjectionsList(List<AbstractSelectionField> projectionsList) {
		this.projectionsList = projectionsList;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}


}
