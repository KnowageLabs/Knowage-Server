/**
 *
 */
package it.eng.knowage.document.export.cockpit.converter;

import org.json.JSONObject;

/**
 * @author Dragan Pirkovic
 *
 */
public class JsonConfigurationBuilder {

	private final JsonConfigurationImpl dataStoreConfigurationImpl;

	/**
	 *
	 */
	public JsonConfigurationBuilder() {
		dataStoreConfigurationImpl = new JsonConfigurationImpl();
	}

	/**
	 * @param dataset
	 *            the dataset to set
	 */
	public JsonConfigurationBuilder setDataset(String datasetLabel) {
		dataStoreConfigurationImpl.setDataset(datasetLabel);
		return this;
	}

	public JsonConfigurationBuilder setParameter(JSONObject parameters) {
		dataStoreConfigurationImpl.setParameters(parameters);
		return this;
	}

	public JsonConfigurationBuilder setSelections(JSONObject selections) {
		dataStoreConfigurationImpl.setSelections(selections);
		return this;
	}

	public JsonConfigurationBuilder setLikeSelections(JSONObject likeSelections) {
		dataStoreConfigurationImpl.setLikeSelections(likeSelections);
		return this;
	}

	public JsonConfigurationBuilder setAggregations(JSONObject aggregations) {
		dataStoreConfigurationImpl.setAggregations(aggregations);
		return this;
	}

	public JsonConfigurationBuilder setSummaryRow(JSONObject summaryRow) {
		dataStoreConfigurationImpl.setSummaryRow(summaryRow);
		return this;
	}

	public JsonConfigurationBuilder setOptions(JSONObject options) {
		dataStoreConfigurationImpl.setOptions(options);
		return this;
	}

	public JsonConfigurationImpl build() {
		return dataStoreConfigurationImpl;

	}

}
