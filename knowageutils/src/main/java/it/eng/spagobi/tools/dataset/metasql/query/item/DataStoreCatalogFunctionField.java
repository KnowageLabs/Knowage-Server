package it.eng.spagobi.tools.dataset.metasql.query.item;

import java.util.UUID;

import org.json.JSONObject;

import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;

public class DataStoreCatalogFunctionField extends AbstractCatalogFunctionField {

	private final String alias;
	private final IAggregationFunction aggregationFunction;
	private final UUID catalogFunctionUuid;
	private final JSONObject catalogFunctionConfig;

	public DataStoreCatalogFunctionField(IAggregationFunction aggregationFunction, String columnName, String alias, UUID catalogFunctionUuid,
			JSONObject config) {
		this.aggregationFunction = aggregationFunction;
		this.name = columnName;
		this.alias = alias;
		this.catalogFunctionUuid = catalogFunctionUuid;
		this.catalogFunctionConfig = config;
	}

	public String getAlias() {
		return alias;
	}

	public IAggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	public UUID getCatalogFunctionUuid() {
		return catalogFunctionUuid;
	}

	public JSONObject getCatalogFunctionConfig() {
		return catalogFunctionConfig;
	}

}
