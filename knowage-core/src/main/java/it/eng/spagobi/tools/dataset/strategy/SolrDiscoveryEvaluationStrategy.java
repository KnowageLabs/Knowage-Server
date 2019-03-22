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

import com.fasterxml.jackson.core.JsonProcessingException;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.*;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.tools.dataset.solr.ExtendedSolrQuery;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FieldStatsInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SolrEvaluationStrategy extends AbstractEvaluationStrategy {

    private static final Logger logger = Logger.getLogger(SolrEvaluationStrategy.class);

    public SolrEvaluationStrategy(IDataSet dataSet) {
        super(dataSet);
    }

    @Override
    protected IDataStore execute(List<Projection> projections, Filter filter, List<Projection> groups, List<Sorting> sortings, List<Projection> summaryRowProjections, int offset, int fetchSize, int maxRowCount) {
        SolrDataSet solrDataSet = dataSet.getImplementation(SolrDataSet.class);
        SolrQuery solrQuery;
        try {
            solrQuery = new ExtendedSolrQuery(solrDataSet.getSolrQuery()).fields(projections).sorts(sortings).filter(filter, solrDataSet.getTextFields()).jsonFacets(groups);
        } catch (JsonProcessingException e) {
            throw new SpagoBIRuntimeException(e);
        }
        solrDataSet.setSolrQuery(solrQuery, getFacetsWithAggregation(groups));
        dataSet.loadData(offset, fetchSize, maxRowCount);
        IDataStore dataStore = dataSet.getDataStore();
        dataStore.setCacheDate(getDate());
        return dataStore;
    }

    @Override
    protected IDataStore executeSummaryRow(List<Projection> summaryRowProjections, IMetaData metaData, Filter filter, int maxRowCount) {
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
        for(int i = 0; i< dataStore.getMetaData().getFieldCount(); i++) {
            String fieldName = dataStore.getMetaData().getFieldName(i);
            for(Projection projection : summaryRowProjections) {
                if(projection.getName().equals(fieldName)) {
                    Object value = getValue(fieldStatsInfo.get(fieldName), projection.getAggregationFunction());
                    IField field = new Field(value);
                    dataStore.getMetaData().getFieldMeta(i).setType(value.getClass());
                    summaryRow.appendField(field);
                    break;
                }
            }
        }
        dataStore.appendRecord(summaryRow);
        dataStore.getMetaData().setProperty("resultNumber", 1);

        return dataStore;
    }

    private Object getValue(FieldStatsInfo fieldStats, IAggregationFunction aggregationFunction) {
        if(AggregationFunctions.COUNT.equals(aggregationFunction.getName())) { return fieldStats.getCount(); }
        if(AggregationFunctions.COUNT_DISTINCT.equals(aggregationFunction.getName())) { return fieldStats.getCountDistinct(); }
        if(AggregationFunctions.MIN.equals(aggregationFunction.getName())) { return fieldStats.getMin(); }
        if(AggregationFunctions.MAX.equals(aggregationFunction.getName())) { return fieldStats.getMax(); }
        if(AggregationFunctions.SUM.equals(aggregationFunction.getName())) { return fieldStats.getSum(); }
        if(AggregationFunctions.AVG.equals(aggregationFunction.getName())) { return fieldStats.getMean(); }
        throw new IllegalArgumentException("The function " + aggregationFunction.getName() + " is not valid here");
    }

    private Map<String, String> getFacetsWithAggregation(List<Projection> groups) {
        Map<String, String> facets = new HashMap<>(groups.size());
        for(Projection facet : groups) {
            facets.put(facet.getName(), facet.getAggregationFunction().getName().toLowerCase());
        }
        return facets;
    }
}
