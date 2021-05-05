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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FieldStatsInfo;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.DataStoreCalculatedField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.tools.dataset.solr.ExtendedSolrQuery;

/**
 * Simple query strategy for Solr dataset.
 *
 * @author Marco Libanori
 */
public class SolrSimpleEvaluationStrategy extends AbstractSolrStrategy {

	public SolrSimpleEvaluationStrategy(IDataSet dataSet) {
		super(dataSet);
	}

	@Override
	protected IDataStore execute(List<AbstractSelectionField> projections, Filter filter, List<AbstractSelectionField> groups, List<Sorting> sortings,
			List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize, int maxRowCount, Set<String> indexes) {
		SolrDataSet solrDataSet = dataSet.getImplementation(SolrDataSet.class);
		solrDataSet.setSolrQueryParameters(solrDataSet.getSolrQuery(), solrDataSet.getParamsMap());
		SolrQuery solrQuery = solrDataSet.getSolrQuery();
		solrDataSet.setSolrQuery(solrQuery);
		dataSet.loadData(offset, fetchSize, maxRowCount);
		IDataStore dataStore = dataSet.getDataStore();
		dataStore.setCacheDate(getDate());
		return dataStore;
	}

	@Override
	protected IDataStore executeSummaryRow(List<AbstractSelectionField> summaryRowProjections, IMetaData metaData, Filter filter, int maxRowCount) {
		IDataStore dataStore = new DataStore(metaData);

		SolrDataSet solrDataSet = dataSet.getImplementation(SolrDataSet.class);
		SolrQuery solrQuery = new ExtendedSolrQuery(solrDataSet.getSolrQuery()).fields(summaryRowProjections).filter(filter).stats(summaryRowProjections);
		SolrClient solrClient = new HttpSolrClient.Builder(solrDataSet.getSolrUrlWithCollection()).build();
		Map<String, FieldStatsInfo> fieldStatsInfo;
		try {
			fieldStatsInfo = solrClient.query(solrQuery).getFieldStatsInfo();
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
		IRecord summaryRow = new Record(dataStore);
		for (int i = 0; i < dataStore.getMetaData().getFieldCount(); i++) {
			IFieldMetaData fieldMeta = dataStore.getMetaData().getFieldMeta(i);
			String fieldName = dataStore.getMetaData().getFieldName(i);
			for (AbstractSelectionField proj : summaryRowProjections) {
				if (proj instanceof Projection) {
					Projection projection = (Projection) proj;
					if (projection.getName().equals(fieldName)) {
						Object value = getValue(fieldStatsInfo.get(fieldName), projection.getAggregationFunction());
						IField field = new Field(value);
						dataStore.getMetaData().getFieldMeta(i).setType(value.getClass());
						summaryRow.appendField(field);
						break;
					}
				}

				else {
					DataStoreCalculatedField projection = (DataStoreCalculatedField) proj;
					if (projection.getName().equals(fieldName)) {
						Object value = getValue(fieldStatsInfo.get(fieldName), projection.getAggregationFunction());
						IField field = new Field(value);
						dataStore.getMetaData().getFieldMeta(i).setType(value.getClass());
						summaryRow.appendField(field);
						break;
					}
				}
			}
		}
		dataStore.appendRecord(summaryRow);
		dataStore.getMetaData().setProperty("resultNumber", 1);

		return dataStore;
	}

}
