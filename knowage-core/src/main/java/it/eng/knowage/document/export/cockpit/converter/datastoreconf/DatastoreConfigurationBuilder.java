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
package it.eng.knowage.document.export.cockpit.converter.datastoreconf;

import java.util.List;
import java.util.Map;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.DataStoreConfigurationImpl;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;

/**
 * @author Dragan Pirkovic
 *
 */
public class DatastoreConfigurationBuilder {

	private IDataSet dataset;
	private Map<String, String> parameters;
	private List<List<AbstractSelectionField>> summaryRowProjections;
	private List<Sorting> sortings;
	private List<AbstractSelectionField> groups;
	private Filter filter;
	private List<AbstractSelectionField> projections;

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
	public DatastoreConfigurationBuilder setProjections(List<AbstractSelectionField> projections) {
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
	public DatastoreConfigurationBuilder setGroups(List<AbstractSelectionField> groups) {
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
	public DatastoreConfigurationBuilder setSummaryRow(List<List<AbstractSelectionField>> summaryRowProjections) {
		this.summaryRowProjections = summaryRowProjections;
		return this;
	}

}
