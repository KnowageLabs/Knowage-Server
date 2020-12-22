package it.eng.spagobi.tools.dataset.metasql.query.item;

import org.json.JSONObject;

import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;

public class DataStoreCatalogFunctionField extends AbstractCatalogFunctionField {

	private final String alias;
	private final IAggregationFunction aggregationFunction;
	private final int catalogFunctionId;
	private final JSONObject catalogFunctionConfig;

	public DataStoreCatalogFunctionField(IAggregationFunction aggregationFunction, String columnName, String alias, int catalogFunctionId, JSONObject config) {
		this.aggregationFunction = aggregationFunction;
		this.name = columnName;
		this.alias = alias;
		this.catalogFunctionId = catalogFunctionId;
		this.catalogFunctionConfig = config;
	}

	public String getAlias() {
		return alias;
	}

	public IAggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	public int getCatalogFunctionId() {
		return catalogFunctionId;
	}

	public JSONObject getCatalogFunctionConfig() {
		return catalogFunctionConfig;
	}

}
