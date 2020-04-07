package it.eng.spagobi.tools.dataset.metasql.query.item;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.database.AbstractDataBase;

public class Projection extends AbstractSelectionField {
	private IAggregationFunction aggregationFunction;
	private IDataSet dataSet;
	private String alias;
	@SuppressWarnings("rawtypes")
	private Class type;

	public Projection(IDataSet dataSet, String columnName) {
		this(null, dataSet, columnName, null);
	}

	public Projection(IDataSet dataSet, String columnName, String alias) {
		this(null, dataSet, columnName, alias);
	}

	public Projection(IAggregationFunction aggregationFunction, IDataSet dataSet, String columnName) {
		this(aggregationFunction, dataSet, columnName, null);
	}

	public Projection(IAggregationFunction aggregationFunction, IDataSet dataSet, String columnName, String alias) {
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

	public IAggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	public String getName() {
		return name;
	}

	public String getAlias() {
		return alias;
	}

	@SuppressWarnings("rawtypes")
	public Class getType() {
		return type;
	}

	public IDataSet getDataset() {
		return dataSet;
	}

	public boolean hasAlias() {
		return alias != null;
	}

	public String getAliasOrName() {
		return hasAlias() ? getAlias() : getName();
	}
}
