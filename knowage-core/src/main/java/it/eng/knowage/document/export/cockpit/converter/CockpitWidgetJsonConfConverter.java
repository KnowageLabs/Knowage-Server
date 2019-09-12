/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
		try {
			return ConverterFactory.getParametersConverter(getDocumentParams()).convert(getDataSetParams());
		} catch (IConverterException e) {

		}
		return null;
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
		try {
			return ConverterFactory.getSelectionsConverter(cockpitWidget.getDsLabel(), this.cockpitDocument.getFilters())
					.convert(this.cockpitWidget.getFilters());
		} catch (IConverterException e) {

		}
		return null;
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
		try {
			return ConverterFactory.getAggregationConverter(cockpitWidget.getDsLabel()).convert(cockpitWidget.getJsonWidget());
		} catch (IConverterException e) {

		}
		return null;
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
