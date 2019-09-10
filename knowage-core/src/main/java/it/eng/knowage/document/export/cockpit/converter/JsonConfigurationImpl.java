/**
 *
 */
package it.eng.knowage.document.export.cockpit.converter;

import org.json.JSONObject;

/**
 * @author Dragan Pirkovic
 *
 */
public class JsonConfigurationImpl implements IJsonConfiguration {

	private String datasetName;
	private JSONObject parameters;
	private JSONObject selections;
	private JSONObject likeSelections;

	private JSONObject aggregations;

	private JSONObject summaryRow;

	private JSONObject options;

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.converter.IDataStoreConfiguration#getAggregations()
	 */
	@Override
	public JSONObject getAggregations() {
		return this.aggregations;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.common.datastore.IDataStoreConfiguration#getDataset()
	 */
	@Override
	public String getDatasetLabel() {

		return this.datasetName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.converter.IDataStoreConfiguration#getLikeSelections()
	 */
	@Override
	public JSONObject getLikeSelections() {
		return this.likeSelections;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.converter.IDataStoreConfiguration#getOptions()
	 */
	@Override
	public JSONObject getOptions() {
		return this.options;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.converter.IDataStoreConfiguration#getParameters()
	 */
	@Override
	public JSONObject getParameters() {

		return this.parameters;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.converter.IDataStoreConfiguration#getSelections()
	 */
	@Override
	public JSONObject getSelections() {
		return this.selections;
	}

	/**
	 * @param aggregations
	 *            the aggregations to set
	 */
	public void setAggregations(JSONObject aggregations) {
		this.aggregations = aggregations;
	}

	/**
	 * @param datasetLabel
	 *            the dataset to set
	 */
	public void setDataset(String datasetLabel) {
		this.datasetName = datasetLabel;
	}

	/**
	 * @param likeSelections
	 *            the likeSelections to set
	 */
	public void setLikeSelections(JSONObject likeSelections) {
		this.likeSelections = likeSelections;
	}

	/**
	 * @param options
	 *            the options to set
	 */
	public void setOptions(JSONObject options) {
		this.options = options;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(JSONObject parameters) {
		this.parameters = parameters;
	}

	/**
	 * @param selections
	 *            the selections to set
	 */
	public void setSelections(JSONObject selections) {
		this.selections = selections;
	}

	/**
	 * @param summaryRow
	 *            the summaryRow to set
	 */
	public void setSummaryRow(JSONObject summaryRow) {
		this.summaryRow = summaryRow;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.converter.IDataStoreConfiguration#SummaryRow()
	 */
	@Override
	public JSONObject getSummaryRow() {
		return this.summaryRow;
	}

}
