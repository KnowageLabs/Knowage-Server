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

package it.eng.spagobi.tools.dataset.solr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.metasql.query.item.CoupledProjection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedSolrQuery extends SolrQuery {

    private static final Logger logger = Logger.getLogger(ExtendedSolrQuery.class);

    public ExtendedSolrQuery(SolrQuery initialQuery) {
        if (initialQuery.getQuery() != null) setQuery(initialQuery.getQuery());
        if (initialQuery.getFilterQueries() != null) setFilterQueries(initialQuery.getFilterQueries());
        if (initialQuery.getFields() != null) setFields(initialQuery.getFields());
        if (initialQuery.getSorts() != null) setSorts(initialQuery.getSorts());
    }

    public ExtendedSolrQuery filter(Filter filter) {
        return filter(filter, null);
    }

    public ExtendedSolrQuery filter(Filter filter, List<String> highlightFields) {
        if(filter != null) {
            SolrFilterVisitor visitor = new SolrFilterVisitor(highlightFields);
            visitor.apply(this, filter);
        }
        return this;
    }

    public ExtendedSolrQuery fields(List<Projection> projections) {
        if(!projections.isEmpty()) {
            setFields(null);
            for (Projection projection : projections) {
                addField(projection.getName());
            }
        }
        return this;
    }

    public ExtendedSolrQuery jsonFacets(List<Projection> groups) throws JsonProcessingException {
        if (!groups.isEmpty()) {
            Map<String, JsonFacet> jsonFacetMap = new HashMap<>(groups.size());
            for(Projection group : groups) {
                JsonFacet jsonFacet;
                if(group instanceof CoupledProjection) {
                    jsonFacet = new AggregationJsonFacet(group.getName(), group.getAggregationFunction(), ((CoupledProjection)group).getAggregatedProjection().getName());
                } else {
                    jsonFacet = new CountJsonFacet(group.getName());
                }
                jsonFacetMap.put(group.getName(), jsonFacet);
            }
            add("json.facet", new ObjectMapper().writeValueAsString(jsonFacetMap));
        }



        return this;
    }

    public ExtendedSolrQuery facets(List<Projection> groups) {
        if (!groups.isEmpty()) {
            String[] facetFields = new String[groups.size()];
            for(int i = 0; i < groups.size(); i++) {
                facetFields[i] = groups.get(i).getName();
            }
            addFacetField(facetFields);
        }
        return this;
    }

    public ExtendedSolrQuery facets(IDataSet dataSet, String... columnNames) {
        List<Projection> facets = new ArrayList<>(columnNames.length);
        for (String columnName : columnNames) {
            facets.add(new Projection(dataSet, columnName));
        }
        return facets(facets);
    }

    public ExtendedSolrQuery stats(List<Projection> projections) {
        if(!projections.isEmpty()) {
            for (Projection projection : projections) {
                setGetFieldStatistics(projection.getName());
                if(AggregationFunctions.COUNT_DISTINCT.equals(projection.getAggregationFunction().getName())) {
                    addStatsFieldCalcDistinct(projection.getName(), true);
                }
            }
        }
        return this;
    }

    public ExtendedSolrQuery sorts(List<Sorting> sortings) {
        if(!sortings.isEmpty()) {
            for (Sorting sorting : sortings) {
                SolrQuery.ORDER order = sorting.isAscending() ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc;
                String item = sorting.getProjection().getName();
                addSort(item, order);
            }
        }
        return this;
    }

}
