package it.eng.spagobi.tools.dataset.metasql.query.item;

import java.util.List;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.database.AbstractDataBase;

public class DataStoreCalculatedField extends AbstractCalculatedField {

	private IDataSet dataSet;
	private String name;
	private String alias;
	private List<AbstractSelectionField> projectionsList;
	private String formula;
	private IAggregationFunction aggregationFunction;
	@SuppressWarnings("rawtypes")
	private Class type;

	public DataStoreCalculatedField(IDataSet dataSet, String name, String alias, List<AbstractSelectionField> projectionsList) {
		super();
		this.dataSet = dataSet;
		this.name = name;
		this.alias = alias;
		this.projectionsList = projectionsList;
	}

	public DataStoreCalculatedField(IDataSet dataSet, String columnName) {
		this(null, dataSet, columnName, null);
	}

	public DataStoreCalculatedField(IDataSet dataSet, String columnName, String alias) {
		this(null, dataSet, columnName, alias);
	}

	public DataStoreCalculatedField(IAggregationFunction aggregationFunction, IDataSet dataSet, String columnName) {
		this(aggregationFunction, dataSet, columnName, null);
	}

	public DataStoreCalculatedField(IAggregationFunction aggregationFunction, IDataSet dataSet, String columnName, String alias) {
		this.aggregationFunction = aggregationFunction;

		this.dataSet = dataSet;

		IFieldMetaData fieldMetaData = DataSetUtilities.getFieldMetaData(dataSet, columnName);

		if (columnName.contains(AbstractDataBase.STANDARD_ALIAS_DELIMITER)) {
			this.name = columnName;
		} else {
			String columnNameWithoutQbePrefix = DataSetUtilities.getColumnNameWithoutQbePrefix(fieldMetaData.getName());
			if (!columnName.equals(columnNameWithoutQbePrefix)) {
				this.name = fieldMetaData.getAlias();
			} else {
				this.name = columnName;
			}
		}

		this.alias = alias;

		this.type = fieldMetaData.getType();
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
	public IAggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	@SuppressWarnings("rawtypes")
	public Class getType() {
		return type;
	}

	public IDataSet getDataset() {
		return dataSet;
	}

	public boolean hasAlias(){
		return alias != null;
	}

	public String getAliasOrName() {
		return hasAlias() ? getAlias() : getName();
	}

}
