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

package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.datareader.FacetSolrDataReader;
import it.eng.spagobi.tools.dataset.constants.SolrDataSetConstants;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.HashMap;

public class FacetSolrDataSet extends SolrDataSet {

    private static final Logger logger = Logger.getLogger(FacetSolrDataSet.class);

    public FacetSolrDataSet(SpagoBiDataSet dataSetConfig) {
        super(dataSetConfig);
    }

    public FacetSolrDataSet(JSONObject jsonConf) {
        super(jsonConf);
    }

    public FacetSolrDataSet(JSONObject jsonConf, HashMap<String, String> parametersMap) {
        super(jsonConf, parametersMap);
    }

    @Override
    protected void initDataReader(JSONObject jsonConf, boolean resolveParams) {
        logger.debug("Reading Solr dataset facets");
        if (getProp(SolrDataSetConstants.SOLR_FACET_QUERY, jsonConf, true, resolveParams) != null) {
            setDataReader(new FacetSolrDataReader("$.facet_counts.facet_queries", true));
        } else {
            setDataReader(new FacetSolrDataReader("$.facet_counts.facet_fields.*.[*]", false));
        }
    }

    protected void initSolrConfiguration(JSONObject jsonConf, boolean resolveParams) {
        super.initSolrConfiguration(jsonConf, resolveParams);
        setFacetParams(jsonConf, resolveParams);
    }

    @Override
    protected String getSolrFacetField(JSONObject jsonConf, boolean resolveParams) {
        return getProp(SolrDataSetConstants.SOLR_FACET_FIELD, jsonConf, true, resolveParams);
    }

    private void setFacetParams(JSONObject jsonConf, boolean resolveParams) {
        String facetField = getProp(SolrDataSetConstants.SOLR_FACET_FIELD, jsonConf, true, resolveParams);
        if(facetField != null) {
            solrConfiguration.getSolrQuery().addFacetField(facetField.split(","));
        }

        String facetQuery = getProp(SolrDataSetConstants.SOLR_FACET_QUERY, jsonConf, true, resolveParams);
        if (facetQuery != null) {
            solrConfiguration.getSolrQuery().addFacetQuery(facetQuery);
        }

        String facetPrefix = getProp(SolrDataSetConstants.SOLR_FACET_PREFIX, jsonConf, true, resolveParams);
        if (facetPrefix != null) {
            solrConfiguration.getSolrQuery().setFacetPrefix(facetPrefix);
        }
    }

    @Override
    protected boolean isFacet() {
        return true;
    }

    @Override
    public boolean isCachingSupported() {
        return true;
    }

    @Override
    public DatasetEvaluationStrategyType getEvaluationStrategy(boolean isNearRealtime) {
        return DatasetEvaluationStrategyType.CACHED;
    }
}
