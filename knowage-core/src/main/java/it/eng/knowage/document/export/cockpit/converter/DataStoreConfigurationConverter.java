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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.knowage.document.cockpit.CockpitDocument;
import it.eng.knowage.document.cockpit.template.widget.ICockpitWidget;
import it.eng.knowage.document.export.cockpit.IConverter;
import it.eng.knowage.document.export.cockpit.converter.datastoreconf.DatastoreConfigurationBuilder;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;

/**
 * @author Dragan Pirkovic
 *
 */
public class DataStoreConfigurationConverter implements IDataStoreConfiguration, IConverter<IDataStoreConfiguration, ICockpitWidget> {

	IConverter<IJsonConfiguration, ICockpitWidget> converter;
	IJsonConfiguration jsonConfiguration;
	private IDataSet dataSet;

	/**
	 * @return the jsonConfiguration
	 */
	public IJsonConfiguration getJsonConfiguration() {
		return jsonConfiguration;
	}

	/**
	 * @param jsonConfiguration the jsonConfiguration to set
	 */
	public void setJsonConfiguration(IJsonConfiguration jsonConfiguration) {
		this.jsonConfiguration = jsonConfiguration;
	}

	/**
	 * @param cockpitDocument
	 */
	public DataStoreConfigurationConverter(CockpitDocument cockpitDocument) {
		converter = new CockpitWidgetJsonConfConverter(cockpitDocument);

	}

	@Override
	public IDataStoreConfiguration convert(ICockpitWidget widget) throws IConverterException {
		this.jsonConfiguration = converter.convert(widget);
		return new DatastoreConfigurationBuilder().setDataSet(getDataset()).setParameters(getParameters()).setProjections(getProjections())
				.setFilter(getFilter()).setGroups(getGroups()).setSortings(getSortings()).setSummaryRow(getSummaryRowProjections()).build();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getDataset()
	 */
	@Override
	public IDataSet getDataset() {
		if (jsonConfiguration != null)
			if (this.dataSet == null)
				return DAOFactory.getDataSetDAO().loadDataSetByLabel(jsonConfiguration.getDatasetLabel());
		return this.dataSet;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getFetchSize()
	 */
	@Override
	public Integer getFetchSize() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getFilter()
	 */
	@Override
	public Filter getFilter() {
		if (jsonConfiguration != null) {
			try {
				return ConverterFactory.getFilterConverter(getDataset(), jsonConfiguration.getAggregations()).convert(jsonConfiguration.getSelections());
			} catch (IConverterException e) {

			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getGroups()
	 */
	@Override
	public List<AbstractSelectionField> getGroups() {
		if (jsonConfiguration != null) {
			try {
				return ConverterFactory.getGroupConverter(getDataset()).convert(jsonConfiguration.getAggregations());
			} catch (IConverterException e) {

			}
		}
		return null;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getMaxRowCount()
	 */
	@Override
	public Integer getMaxRowCount() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getOffset()
	 */
	@Override
	public Integer getOffset() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getParameters()
	 */
	@Override
	public Map<String, String> getParameters() {
		try {
			return new ObjectMapper().readValue(jsonConfiguration.getParameters().toString(), new TypeReference<Map<String, String>>() {
			});
		} catch (IOException e1) {

		}
		return null;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getProjections()
	 */
	@Override
	public List<AbstractSelectionField> getProjections() {
		if (jsonConfiguration != null) {
			try {
				return ConverterFactory.getProjectionConverter(getDataset()).convert(jsonConfiguration.getAggregations());
			} catch (IConverterException e) {

			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getSortings()
	 */
	@Override
	public List<Sorting> getSortings() {
		if (jsonConfiguration != null) {
			try {
				return ConverterFactory.getSortingsConverter(getDataset()).convert(jsonConfiguration.getAggregations());
			} catch (IConverterException e) {

			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getSummaryRowProjections()
	 */
	@Override
	public List<List<AbstractSelectionField>> getSummaryRowProjections() {
		// TODO Auto-generated method stub
		List<List<AbstractSelectionField>> newL = new ArrayList<List<AbstractSelectionField>>();
		return newL;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#isRealtime()
	 */
	@Override
	public boolean isRealtime() {
		// TODO Auto-generated method stub
		return false;
	}

}
