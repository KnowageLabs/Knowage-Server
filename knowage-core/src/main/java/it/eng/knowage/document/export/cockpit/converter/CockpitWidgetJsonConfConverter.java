/**
 *
 */
package it.eng.knowage.document.export.cockpit.converter;

import java.util.Map;

import org.json.JSONObject;

import it.eng.knowage.document.cockpit.CockpitDocument;
import it.eng.knowage.document.cockpit.template.widget.ICockpitWidget;
import it.eng.knowage.document.export.cockpit.IConverter;

/**
 * @author Dragan Pirkovic
 *
 */
public class CockpitWidgetJsonConfConverter implements IConverter<IJsonConfiguration, ICockpitWidget>, IJsonConfiguration {

	private CockpitDocument cockpitDocument;
	private ICockpitWidget cockpitWidget;

	/**
	 * @param cockpitDocument
	 * @param userProfile
	 */
	public CockpitWidgetJsonConfConverter(CockpitDocument cockpitDocument) {
		this.cockpitDocument = cockpitDocument;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.IConverter#convert(java.lang.Object)
	 */
	@Override
	public IJsonConfiguration convert(ICockpitWidget cockpitWidget) {
		this.cockpitWidget = cockpitWidget;

		return new JsonConfigurationBuilder().setDataset(getDatasetLabel()).setParameter(getParameters()).setSelections(getSelections())
				.setAggregations(getAggregations()).setSummaryRow(getSummaryRow()).setOptions(getOptions()).build();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.common.datastore.IDataStoreConfiguration#getDataset()
	 */
	@Override
	public String getDatasetLabel() {

		return cockpitDocument.getDataSetLabelById(cockpitWidget.getDsId());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.common.datastore.IDataStoreConfiguration#getParameters()
	 */

	/**
	 * @return the cockpitDocument
	 */
	public CockpitDocument getCockpitDocument() {
		return cockpitDocument;
	}

	/**
	 * @param cockpitDocument
	 *            the cockpitDocument to set
	 */
	public void setCockpitDocument(CockpitDocument cockpitDocument) {
		this.cockpitDocument = cockpitDocument;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.converter.IDataStoreConfiguration#getParameters()
	 */
	@Override
	public JSONObject getParameters() {
		return ConverterFactory.getParametersConverter(getDocumentParams()).convert(getDataSetParams());
	}

	/**
	 * @return
	 */
	private Map<String, String> getDocumentParams() {
		return this.cockpitDocument.getParameters();
	}

	/**
	 * @return
	 */
	private JSONObject getDataSetParams() {
		return this.cockpitDocument.getParamsByDataSetId(this.cockpitWidget.getDsId());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.converter.IDataStoreConfiguration#getSelections()
	 */
	@Override
	public JSONObject getSelections() {
		return ConverterFactory.getSelectionsConverter(cockpitWidget.getDsLabel(), this.cockpitDocument.getFilters()).convert(this.cockpitWidget.getFilters());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.converter.IDataStoreConfiguration#getLikeSelections()
	 */
	@Override
	public JSONObject getLikeSelections() {
		return new JSONObject();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.converter.IDataStoreConfiguration#getAggregations()
	 */
	@Override
	public JSONObject getAggregations() {
		return ConverterFactory.getAggregationConverter(cockpitWidget.getDsLabel()).convert(cockpitWidget.getJsonWidget());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.converter.IDataStoreConfiguration#SummaryRow()
	 */
	@Override
	public JSONObject getSummaryRow() {
		return new JSONObject();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.converter.IDataStoreConfiguration#getOptions()
	 */
	@Override
	public JSONObject getOptions() {
		return new JSONObject();
	}

}
