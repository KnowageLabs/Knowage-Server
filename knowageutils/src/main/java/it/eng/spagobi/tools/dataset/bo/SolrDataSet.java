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
package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.security.hmacfilter.HMACSecurityException;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.RESTDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.SolrDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.CompositeSolrDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.FacetSolrDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.SolrDataReader;
import it.eng.spagobi.tools.dataset.constants.RESTDataSetConstants;
import it.eng.spagobi.tools.dataset.constants.SolrDataSetConstants;
import it.eng.spagobi.tools.dataset.notifier.fiware.OAuth2Utils;
import it.eng.spagobi.tools.dataset.solr.SolrConfiguration;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.ConfigurationException;
import it.eng.spagobi.utilities.objects.Couple;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolrDataSet extends RESTDataSet {

    public static final String DATASET_TYPE = "SbiSolrDataSet";
    private static final Logger logger = Logger.getLogger(SolrDataSet.class);

    protected SolrConfiguration solrConfiguration;

    public SolrDataSet(SpagoBiDataSet dataSetConfig) {
        super(dataSetConfig);

    }

    public SolrDataSet(JSONObject jsonConf) {
        setConfiguration(jsonConf.toString());
        initConf(jsonConf, false);

    }

    public SolrDataSet(JSONObject jsonConf, HashMap<String, String> parametersMap) {
        this.setParamsMap(parametersMap);
        setConfiguration(jsonConf.toString());
        initConf(jsonConf, true);

    }

    @Override
    public void initConf(JSONObject jsonConf, boolean resolveParams) {
        initSolrConfiguration(jsonConf, resolveParams);
        initDataProxy(jsonConf, resolveParams);
        initDataReader(jsonConf, resolveParams);
    }

    protected void initSolrConfiguration(JSONObject jsonConf, boolean resolveParams) {
        try {
            solrConfiguration = new SolrConfiguration();
            String address = getProp(SolrDataSetConstants.SOLR_BASE_ADDRESS, jsonConf, false, resolveParams);
            solrConfiguration.setUrl(address);
            String collection = getProp(SolrDataSetConstants.SOLR_COLLECTION, jsonConf, false, resolveParams);
            solrConfiguration.setCollection(collection);
            SolrQuery solrQuery = new SolrQuery();
            String query = getProp(SolrDataSetConstants.SOLR_QUERY, jsonConf, true, resolveParams);
            if (query == null || query.isEmpty()) {
                query = "*.*";
            }
            solrQuery.setQuery(query);
            String fieldList = getProp(SolrDataSetConstants.SOLR_FIELD_LIST, jsonConf, true, resolveParams);
            if(fieldList != null && !fieldList.trim().isEmpty()) {
                solrQuery.setFields(fieldList.split(","));
            }
            String solrFields = getProp(SolrDataSetConstants.SOLR_FIELDS, jsonConf, true, resolveParams);
            if(solrFields != null && !solrFields.trim().isEmpty()) {
                solrConfiguration.setSolrFields(solrFields);
            }

            List<Couple<String, String>> filterQueries = getListProp(SolrDataSetConstants.SOLR_FILTER_QUERY, jsonConf, true);
            if (filterQueries != null && !filterQueries.isEmpty()) {
                String[] array = new String[filterQueries.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = filterQueries.get(i).getFirst() + ":" + filterQueries.get(i).getSecond();
                }
                solrQuery.setFilterQueries(array);
            }
            solrQuery.setFacet(isFacet());
            solrConfiguration.setSolrQuery(solrQuery);
        } catch (JSONException e) {
            throw new ConfigurationException("Problems in configuration of solr query", e);
        }
    }

    protected void initDataReader(JSONObject jsonConf, boolean resolveParams) {
        logger.debug("Reading Solr dataset documents");
        setDataReader(new SolrDataReader("$.response.docs.[*]", getJsonPathAttributes()));
    }

    private List<JSONPathDataReader.JSONPathAttribute> getJsonPathAttributes() {
        JSONArray solrFields = getSolrFields();
        Map<String, String> solrFieldTypes = getSolrFieldTypes(solrFields);
        return getJsonPathAttributes(solrFieldTypes);
    }

    private Map<String, String> getSolrFieldTypes(JSONArray solrFields) {
        Map<String, String> solrFieldTypes = new HashMap<>(solrFields.length());
        for (int i = 0; i < solrFields.length(); i++) {
            JSONObject solrField = solrFields.optJSONObject(i);
            String name = solrField.optString("name");
            String type = solrField.optString("type");
            solrFieldTypes.put(name, type);
        }
        return solrFieldTypes;
    }

    private List<JSONPathDataReader.JSONPathAttribute> getJsonPathAttributes(Map<String, String> solrFieldTypes) {
		String[] fields = solrConfiguration.getSolrQuery().getFields().split(",");
		List<JSONPathDataReader.JSONPathAttribute> jsonPathAttributes = new ArrayList<>(fields.length);
		for (String field : fields) {
			String solrFieldType = solrFieldTypes.containsKey(field) ? solrFieldTypes.get(field) : "string";
			String jsonPathType = JSONPathDataReader.JSONPathAttribute.getJsonPathTypeFromSolrFieldType(solrFieldType);
			jsonPathAttributes.add(new JSONPathDataReader.JSONPathAttribute(field, "$." + field, jsonPathType));
		}
		return jsonPathAttributes;
	}

	public JSONArray getSolrFields() {
        RESTDataProxy dataProxy = getDataProxy();
        JSONArray solrFields;
        String configurationSolrFields = solrConfiguration.getSolrFields();
        try {
            if(configurationSolrFields != null){
                solrFields = new JSONArray(configurationSolrFields);
            }else {
                RestUtilities.Response response = RestUtilities.makeRequest(dataProxy.getRequestMethod(),
                        solrConfiguration.getUrl() + solrConfiguration.getCollection() + "/schema/fields?wt=json",
                        dataProxy.getRequestHeaders(), null,
                        null);
                Assert.assertTrue(response.getStatusCode() == HttpStatus.SC_OK, "Response status is not ok");

                String responseBody = response.getResponseBody();
                Assert.assertNotNull(responseBody, "Response body is null");

                solrFields = new JSONObject(responseBody).getJSONArray("fields");
            }
		} catch (Exception e) {
			logger.error("Unable to read Solr fields", e);
            solrFields = new JSONArray();
		}

		return solrFields;
	}

    private void initDataProxy(JSONObject jsonConf, boolean resolveParams) {
        Map<String, String> requestHeaders = getRequestHeaders(jsonConf, resolveParams);

        // Pagination parameters
        String offset = getProp(RESTDataSetConstants.REST_OFFSET, jsonConf, true, resolveParams);
        String fetchSize = getProp(RESTDataSetConstants.REST_FETCH_SIZE, jsonConf, true, resolveParams);
        String maxResults = getProp(RESTDataSetConstants.REST_MAX_RESULTS, jsonConf, true, resolveParams);

        String facetField = getSolrFacetField(jsonConf, resolveParams);
        setDataProxy(new SolrDataProxy(solrConfiguration.toString(), HttpMethod.Get, facetField, requestHeaders, offset, fetchSize, maxResults, isFacet()));
    }

    protected String getSolrFacetField(JSONObject jsonConf, boolean resolveParams) {
        return null;
    }

    private Map<String, String> getRequestHeaders(JSONObject jsonConf, boolean resolveParams) {
        Map<String, String> requestHeaders;
        try {
            requestHeaders = getRequestHeadersPropMap(RESTDataSetConstants.REST_REQUEST_HEADERS, jsonConf, resolveParams);

            // add bearer token for OAuth Fiware
            if (resolveParams && OAuth2Utils.isOAuth2() && !OAuth2Utils.containsOAuth2(requestHeaders)) {
                String oAuth2Token = getOAuth2Token();
                if (oAuth2Token != null) {
                    requestHeaders.putAll(OAuth2Utils.getOAuth2Headers(oAuth2Token));
                }
            }
            return requestHeaders;
        } catch (Exception e) {
            throw new ConfigurationException("Problems in configuration of data proxy", e);
        }
    }

    protected boolean isFacet() {
        return false;
    }

    public SolrQuery getSolrQuery() {
        return solrConfiguration.getSolrQuery();
    }

    public String getSolrUrl() {
        return solrConfiguration.getUrl();
    }

    public String getSolrCollection() { return solrConfiguration.getCollection(); }

    public String getSolrUrlWithCollection() {
        StringBuilder sb = new StringBuilder();
        sb.append(solrConfiguration.getUrl());
        if(!solrConfiguration.getUrl().endsWith("/")) sb.append("/");
        sb.append(solrConfiguration.getCollection());
        return sb.toString();
    }

    public void setSolrQuery(SolrQuery solrQuery, Map<String, String> facets) {
        Assert.assertNotNull(facets, "Facets cannot be null, at least empty");
        solrConfiguration.setSolrQuery(solrQuery);
        try {
            JSONObject jsonConfiguration = new JSONObject(configuration);
            initDataProxy(jsonConfiguration, true);
            initDataReader(jsonConfiguration, true);
            if(!facets.isEmpty()) {
                CompositeSolrDataReader compositeSolrDataReader = new CompositeSolrDataReader((SolrDataReader)dataReader);

                for(String facet : facets.keySet()) {
                    String aggregation = facets.get(facet);
                    FacetSolrDataReader facetSolrDataReader = new FacetSolrDataReader("$.facets." + facet + ".buckets.[*].['val','" + aggregation + "']", true);
                    facetSolrDataReader.setFacetField(facet);
                    facetSolrDataReader.setCalculateResultNumberEnabled(true);
                    compositeSolrDataReader.addFacetSolrDataReader(facetSolrDataReader);
                }
                setDataReader(compositeSolrDataReader);
            }
        } catch (JSONException e) {
            throw new ConfigurationException("Problems in configuration of data proxy", e);
        }
    }

    @Override
    public boolean isCachingSupported() {
        return false;
    }

    @Override
    public DatasetEvaluationStrategyType getEvaluationStrategy(boolean isNearRealtime) {
        return DatasetEvaluationStrategyType.SOLR;
    }

    public List<String> getTextFields(){
        List<JSONPathDataReader.JSONPathAttribute> attributes = ((SolrDataReader) dataReader).getJsonPathAttributes("text");
        List<String> textFields = new ArrayList<>(attributes.size());
        for (int i = 0; i < attributes.size(); i++) {
            textFields.add(attributes.get(i).getName());
        }
        return textFields;
    }
}
