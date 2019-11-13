/**
 *
 */
package it.eng.spagobi.tools.dataset.metasql.query.item;

import java.util.List;
import java.util.Map;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

/**
 * @author Dragan Pirkovic
 *
 */
public class DataStoreConfigurationImpl implements IDataStoreConfiguration {

	private IDataSet dataset;
	private boolean isRealTime = true;
	private Map<String, String> parameters;
	private List<Projection> projections;
	private Filter filters;
	private List<Projection> groups;
	private List<Sorting> sortings;
	private List<List<Projection>> summaryRowProjections;
	private final Integer offset = -1;
	private final Integer fetchSize = -1;
	private final Integer maxRowCount = -1;

	/**
	 *
	 */

	/**
	 * @param dataset
	 * @param parameters
	 * @param projections
	 * @param filters
	 * @param groups
	 * @param sortings
	 * @param summaryRowProjections
	 */
	public DataStoreConfigurationImpl(IDataSet dataset, Map<String, String> parameters, List<Projection> projections, Filter filters, List<Projection> groups,
			List<Sorting> sortings, List<List<Projection>> summaryRowProjections) {
		super();
		this.dataset = dataset;
		this.parameters = parameters;
		this.projections = projections;
		this.filters = filters;
		this.groups = groups;
		this.sortings = sortings;
		this.summaryRowProjections = summaryRowProjections;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getDataset()
	 */
	@Override
	public IDataSet getDataset() {
		return this.dataset;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getFetchSize()
	 */
	@Override
	public Integer getFetchSize() {
		return this.fetchSize;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getFilter()
	 */
	@Override
	public Filter getFilter() {
		return this.filters;
	}

	/**
	 * @return the filters
	 */
	public Filter getFilters() {
		return filters;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getGroups()
	 */
	@Override
	public List<Projection> getGroups() {
		return this.groups;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getMaxRowCount()
	 */
	@Override
	public Integer getMaxRowCount() {
		return this.maxRowCount;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getOffset()
	 */
	@Override
	public Integer getOffset() {
		return this.offset;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getParameters()
	 */
	@Override
	public Map<String, String> getParameters() {
		return this.parameters;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getProjections()
	 */
	@Override
	public List<Projection> getProjections() {
		return this.projections;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getSortings()
	 */
	@Override
	public List<Sorting> getSortings() {
		return this.sortings;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#getSummaryRowProjections()
	 */
	@Override
	public List<List<Projection>> getSummaryRowProjections() {
		return this.summaryRowProjections;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration#isRealtime()
	 */
	@Override
	public boolean isRealtime() {
		return this.isRealTime;
	}

	/**
	 * @param dataset
	 *            the dataset to set
	 */
	public void setDataset(IDataSet dataset) {
		this.dataset = dataset;
	}

	/**
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(Filter filters) {
		this.filters = filters;
	}

	/**
	 * @param groups
	 *            the groups to set
	 */
	public void setGroups(List<Projection> groups) {
		this.groups = groups;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @param projections
	 *            the projections to set
	 */
	public void setProjections(List<Projection> projections) {
		this.projections = projections;
	}

	/**
	 * @param isRealTime
	 *            the isRealTime to set
	 */
	public void setRealTime(boolean isRealTime) {
		this.isRealTime = isRealTime;
	}

	/**
	 * @param sortings
	 *            the sortings to set
	 */
	public void setSortings(List<Sorting> sortings) {
		this.sortings = sortings;
	}

	/**
	 * @param summaryRowProjections
	 *            the summaryRowProjections to set
	 */
	public void setSummaryRowProjections(List<List<Projection>> summaryRowProjections) {
		this.summaryRowProjections = summaryRowProjections;
	}

}
