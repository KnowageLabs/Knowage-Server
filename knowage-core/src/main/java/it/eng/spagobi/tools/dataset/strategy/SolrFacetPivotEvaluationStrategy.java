/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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
 *
 */

package it.eng.spagobi.tools.dataset.strategy;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONException;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.SolrFacetPivotDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.tools.dataset.solr.ExtendedSolrQuery;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

class SolrFacetPivotEvaluationStrategy extends SolrEvaluationStrategy {

	private static final Logger logger = Logger.getLogger(SolrFacetPivotEvaluationStrategy.class);

	public SolrFacetPivotEvaluationStrategy(IDataSet dataSet) {
		super(dataSet);
	}

	@Override
	protected IDataStore execute(List<AbstractSelectionField> projections, Filter filter, List<AbstractSelectionField> groups, List<Sorting> sortings,
			List<AbstractSelectionField> summaryRowProjections, int offset, int fetchSize, int maxRowCount, Set<String> columns) {
		SolrDataSet solrDataSet = dataSet.getImplementation(SolrDataSet.class);
		solrDataSet.setSolrQueryParameters(solrDataSet.getSolrQuery(), solrDataSet.getParamsMap());
		SolrQuery solrQuery;
		try {
			solrQuery = new ExtendedSolrQuery(solrDataSet.getSolrQuery()).filter(filter).jsonFacets(projections, groups, sortings);
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException(e);
		}
		solrQuery.setRows(0);

		solrDataSet.setSolrQuery(solrQuery, null);

		JSONPathDataReader dataReader = solrDataSet.getDataReader();
		SolrFacetPivotDataReader solrFacetPivotDataReader = new SolrFacetPivotDataReader(dataReader.getJsonPathItems(), dataReader.getJsonPathAttributes());
		solrDataSet.setDataReader(solrFacetPivotDataReader);

		dataSet.loadData(offset, fetchSize, maxRowCount);
		IDataStore dataStore = dataSet.getDataStore();
		dataStore.setCacheDate(getDate());
		return dataStore;
	}
}
