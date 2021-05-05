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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FieldStatsInfo;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.DataStoreCalculatedField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.tools.dataset.solr.ExtendedSolrQuery;
import it.eng.spagobi.tools.dataset.solr.SolrDataStore;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

class SolrEvaluationStrategy extends AbstractSolrStrategy {

	private static final Logger logger = Logger.getLogger(SolrEvaluationStrategy.class);

	public SolrEvaluationStrategy(IDataSet dataSet) {
		super(dataSet);
	}

	@Override
	protected IDataStore execute(List<AbstractSelectionField> projections, Filter filter, List<AbstractSelectionField> groups, List<Sorting> sortings,
			List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize, int maxRowCount, Set<String> indexes) {
		SolrDataSet solrDataSet = dataSet.getImplementation(SolrDataSet.class);
		solrDataSet.setSolrQueryParameters(solrDataSet.getSolrQuery(), solrDataSet.getParamsMap());
		SolrQuery solrQuery;
		try {
			solrQuery = new ExtendedSolrQuery(solrDataSet.getSolrQuery()).fields(projections).sorts(sortings).filter(filter, solrDataSet.getTextFields())
					.jsonFacets(groups, solrDataSet.getFacetsLimitOption());
		} catch (JsonProcessingException e) {
			throw new SpagoBIRuntimeException(e);
		}
		solrDataSet.setSolrQuery(solrQuery, getFacetsWithAggregation(groups));
		dataSet.loadData(offset, fetchSize, maxRowCount);
		IDataStore dataStore = dataSet.getDataStore();

		dataStore = checkIfItHasLikeID(dataStore, projections); // Coherence control since LikeSelection uses to add a new "ID" field
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

	private Map<String, String> getFacetsWithAggregation(List<AbstractSelectionField> groups) {
		Map<String, String> facets = new HashMap<>(groups.size());
		for (AbstractSelectionField facet : groups) {

			if (facet instanceof Projection) {
				Projection proj = (Projection) facet;
				facets.put(proj.getName(), proj.getAggregationFunction().getName().toLowerCase());
			} else {
				DataStoreCalculatedField proj = (DataStoreCalculatedField) facet;
				facets.put(proj.getName(), proj.getAggregationFunction().getName().toLowerCase());
			}
		}
		return facets;
	}

	private IDataStore checkIfItHasLikeID(IDataStore pagedDataStore, List<AbstractSelectionField> projections) {

		if (pagedDataStore instanceof SolrDataStore) {

			SolrDataStore originalDTS = (SolrDataStore) pagedDataStore;
			SolrDataStore datastoresToAdd = new SolrDataStore(originalDTS.getFacets());
			IMetaData pagedMetaData = pagedDataStore.getMetaData();
			Integer idIndex = null;
			ArrayList<FieldMetadata> metas = (ArrayList<FieldMetadata>) pagedMetaData.getFieldsMeta();
			boolean hasIdOnMeta = false;
			for (int i = 0; i < metas.size(); i++) {

				if (metas.get(i).getName().equalsIgnoreCase("id")) {
					idIndex = new Integer(i);
					hasIdOnMeta = true;
				}
			}

			boolean hasId = false;

			for (AbstractSelectionField abstractSelectionField : projections) {
				if (abstractSelectionField.getName().equalsIgnoreCase("ID")) {
					hasId = true;
				}

			}
			if (!hasId && hasIdOnMeta) {

				pagedMetaData.deleteFieldMetaDataAt(idIndex);

				datastoresToAdd.setMetaData(pagedMetaData);

				for (int projectionIndex = 0; projectionIndex < pagedDataStore.getRecordsCount(); projectionIndex++) {
					Record newRecord = new Record();
					newRecord = (Record) pagedDataStore.getRecordAt(projectionIndex);
					newRecord.removeFieldAt(idIndex);

					datastoresToAdd.appendRecord(newRecord);
				}

			} else {
				datastoresToAdd = originalDTS;
			}
			return datastoresToAdd;
		}
		return pagedDataStore;

	}
}
