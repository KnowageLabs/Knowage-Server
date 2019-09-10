/**
 *
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
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;

/**
 * @author Dragan Pirkovic
 *
 */
public class DataStoreConfigurationConverter implements IDataStoreConfiguration, IConverter<IDataStoreConfiguration, ICockpitWidget> {

	IConverter<IJsonConfiguration, ICockpitWidget> converter;
	IJsonConfiguration jsonConfiguration;

	/**
	 * @return the jsonConfiguration
	 */
	public IJsonConfiguration getJsonConfiguration() {
		return jsonConfiguration;
	}

	/**
	 * @param jsonConfiguration
	 *            the jsonConfiguration to set
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
	public IDataStoreConfiguration convert(ICockpitWidget widget) {
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
			return DAOFactory.getDataSetDAO().loadDataSetByLabel(jsonConfiguration.getDatasetLabel());
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getGroups()
	 */
	@Override
	public List<Projection> getGroups() {
		if (jsonConfiguration != null) {
			return ConverterFactory.getGroupConverter(getDataset()).convert(jsonConfiguration.getAggregations());
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
	public List<Projection> getProjections() {
		if (jsonConfiguration != null) {
			return ConverterFactory.getProjectionConverter(getDataset()).convert(jsonConfiguration.getAggregations());
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
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getSummaryRowProjections()
	 */
	@Override
	public List<Projection> getSummaryRowProjections() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
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
