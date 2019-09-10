/**
 *
 */
package it.eng.knowage.document.export.cockpit.converter.datastoreconf;

import java.util.List;
import java.util.Map;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metasql.query.item.DataStoreConfigurationImpl;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;

/**
 * @author Dragan Pirkovic
 *
 */
public class DatastoreConfigurationBuilder {

	private IDataSet dataset;
	private Map<String, String> parameters;
	private List<Projection> summaryRowProjections;
	private List<Sorting> sortings;
	private List<Projection> groups;
	private Filter filter;
	private List<Projection> projections;

	/**
	 * @param dataset
	 * @return
	 */
	public DatastoreConfigurationBuilder setDataSet(IDataSet dataset) {
		this.dataset = dataset;
		return this;
	}

	/**
	 * @param parameters
	 * @return
	 */
	public DatastoreConfigurationBuilder setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
		return this;
	}

	/**
	 * @param projections
	 * @return
	 */
	public DatastoreConfigurationBuilder setProjections(List<Projection> projections) {
		this.projections = projections;
		return this;
	}

	/**
	 * @param filter
	 * @return
	 */
	public DatastoreConfigurationBuilder setFilter(Filter filter) {
		this.filter = filter;
		return this;
	}

	/**
	 * @param groups
	 * @return
	 */
	public DatastoreConfigurationBuilder setGroups(List<Projection> groups) {
		this.groups = groups;
		return this;
	}

	/**
	 * @param sortings
	 * @return
	 */
	public DatastoreConfigurationBuilder setSortings(List<Sorting> sortings) {
		this.sortings = sortings;
		return this;
	}

	/**
	 * @return
	 */
	public IDataStoreConfiguration build() {
		// TODO Auto-generated method stub
		return new DataStoreConfigurationImpl(dataset, parameters, projections, filter, groups, sortings, summaryRowProjections);
	}

	/**
	 * @param summaryRowProjections
	 * @return
	 */
	public DatastoreConfigurationBuilder setSummaryRow(List<Projection> summaryRowProjections) {
		this.summaryRowProjections = summaryRowProjections;
		return this;
	}

}
