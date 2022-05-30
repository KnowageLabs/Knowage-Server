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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.RESTDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.SolrDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.CompositeSolrDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.FacetSolrDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.SolrDataReader;
import it.eng.spagobi.tools.dataset.constants.RESTDataSetConstants;
import it.eng.spagobi.tools.dataset.constants.SolrDataSetConstants;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.notifier.fiware.OAuth2Utils;
import it.eng.spagobi.tools.dataset.solr.SolrConfiguration;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.ConfigurationException;
import it.eng.spagobi.utilities.objects.Couple;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;

public class SolrDataSet extends RESTDataSet {

	private static final String SOLR_DEFAULT_QUERY = "*:*";
	public static final String DATASET_TYPE = "SbiSolrDataSet";
	private static final Logger logger = Logger.getLogger(SolrDataSet.class);
	private int facetsLimitOption = 10;

	protected SolrConfiguration solrConfiguration;
	private DatasetEvaluationStrategyType evaluationStrategy;

	public SolrDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);
		evaluationStrategy = DatasetEvaluationStrategyType.SOLR;
	}

	public SolrDataSet(JSONObject jsonConf) {
		setConfiguration(jsonConf.toString());
		initConf(jsonConf, false);
		evaluationStrategy = DatasetEvaluationStrategyType.SOLR;
	}

	public SolrDataSet(JSONObject jsonConf, HashMap<String, String> parametersMap) {
		this.setParamsMap(parametersMap);
		setConfiguration(jsonConf.toString());
		initConf(jsonConf, true);
		evaluationStrategy = DatasetEvaluationStrategyType.SOLR;
	}

	public SolrDataSet(JSONObject jsonConf, HashMap<String, String> parametersMap, UserProfile userProfile) {
		this.setParamsMap(parametersMap);
		setConfiguration(jsonConf.toString());
		initConf(jsonConf, true, userProfile);
		evaluationStrategy = DatasetEvaluationStrategyType.SOLR;
	}

	@Override
	public void initConf(JSONObject jsonConf, boolean resolveParams) {

		initSolrConfiguration(jsonConf, resolveParams);
		initDataProxy(jsonConf, resolveParams);
		initDataReader(jsonConf, resolveParams);

	}

	public void initConf(JSONObject jsonConf, boolean resolveParams, UserProfile userProfile) {

		initSolrConfiguration(jsonConf, resolveParams, userProfile);
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
				query = SOLR_DEFAULT_QUERY;
			}
			solrQuery.setQuery(query);
			String fieldList = getProp(SolrDataSetConstants.SOLR_FIELD_LIST, jsonConf, true, resolveParams);
			if (fieldList != null && !fieldList.trim().isEmpty()) {
				solrQuery.setFields(fieldList.split(","));
			}
			String solrFields = getProp(SolrDataSetConstants.SOLR_FIELDS, jsonConf, true, resolveParams);
			if (solrFields != null && !solrFields.trim().isEmpty()) {
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

	protected void initSolrConfiguration(JSONObject jsonConf, boolean resolveParams, UserProfile userProfile) {
		try {
			solrConfiguration = new SolrConfiguration();
			String address = getProp(SolrDataSetConstants.SOLR_BASE_ADDRESS, jsonConf, false, resolveParams);
			solrConfiguration.setUrl(address);
			String collection = getProp(SolrDataSetConstants.SOLR_COLLECTION, jsonConf, false, resolveParams);
			solrConfiguration.setCollection(collection);
			SolrQuery solrQuery = new SolrQuery();
			String query = getProp(SolrDataSetConstants.SOLR_QUERY, jsonConf, true, resolveParams);
			if (query == null || query.isEmpty()) {
				query = SOLR_DEFAULT_QUERY;
			}
			solrQuery.setQuery(query);
			String fieldList = getProp(SolrDataSetConstants.SOLR_FIELD_LIST, jsonConf, true, resolveParams);
			if (fieldList != null && !fieldList.trim().isEmpty()) {
				solrQuery.setFields(fieldList.split(","));
			}
			String solrFields = getProp(SolrDataSetConstants.SOLR_FIELDS, jsonConf, true, resolveParams);
			if (solrFields != null && !solrFields.trim().isEmpty()) {
				solrConfiguration.setSolrFields(solrFields);
			}

			List<Couple<String, String>> filterQueries = getListProp(SolrDataSetConstants.SOLR_FILTER_QUERY, jsonConf, true, userProfile);
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

	public void forceSchemaRead(JSONObject jsonConf) throws JSONException {

		logger.debug("Force schema read from Solr");
		setDataReader(new SolrDataReader("$.response.docs.[*]", getJsonPathAttributes(true)));

		// Fundamental!
		jsonConf.put(SolrDataSetConstants.SOLR_FIELDS, getSolrFields().toString());
	}

	private List<JSONPathDataReader.JSONPathAttribute> getJsonPathAttributes() {
		return getJsonPathAttributes(false);
	}

	private List<JSONPathDataReader.JSONPathAttribute> getJsonPathAttributes(boolean force) {

		JSONArray solrFields = getSolrFields(force);

		String config = getConfiguration();
		try {
			JSONObject jsonConfig = new JSONObject(config);
			jsonConfig.put("solrFields", solrFields.toString());
			setConfiguration(jsonConfig.toString());
		} catch (JSONException e) {
			throw new ConfigurationException("Unable to update Solr fields", e);
		}
		solrConfiguration.setSolrFields(solrFields.toString());

		return getJsonPathAttributes(solrFields);

	}

	private List<JSONPathDataReader.JSONPathAttribute> getJsonPathAttributes(JSONArray solrFields) {
		List<String> fields = Arrays.asList(solrConfiguration.getSolrQuery().getFields().split(","));
		List<JSONPathDataReader.JSONPathAttribute> jsonPathAttributes = new ArrayList<>(fields.size());
		for (int i = 0; i < solrFields.length(); i++) {

			JSONObject solrField = null;
			try {
				solrField = solrFields.getJSONObject(i);

				String name = solrField.optString("name");
				String type = solrField.optString("type", "string");
				boolean multiValued = solrField.optBoolean("multiValued", false);

				if (!fields.contains(name)) {
					continue;
				}

				String jsonPathType = JSONPathDataReader.JSONPathAttribute.getJsonPathTypeFromSolrFieldType(type);
				jsonPathAttributes.add(new JSONPathDataReader.JSONPathAttribute(name, "$." + name, jsonPathType, multiValued));
			} catch (JSONException e) {
				throw new RuntimeException("Cannot parse Solr schema: " + String.valueOf(solrField), e);
			}
		}
		return jsonPathAttributes;
	}

	public JSONArray getSolrFields() {
		return getSolrFields(false);
	}

	public JSONArray getSolrFields(boolean force) {
		RESTDataProxy dataProxy = getDataProxy();
		JSONArray solrFields = new JSONArray();

		try {
			String configurationSolrFields = null;

			configurationSolrFields = solrConfiguration.getSolrFields();
			if (configurationSolrFields != null) {
				solrFields = new JSONArray(configurationSolrFields);
			}

			if (force || solrFields.length() == 0) {
				RestUtilities.Response response = RestUtilities.makeRequest(HttpMethod.Get,
						solrConfiguration.getUrl() + solrConfiguration.getCollection() + "/schema/fields?wt=json", dataProxy.getRequestHeaders(), null, null);
				logger.debug(response.getStatusCode());
				Assert.assertTrue(response.getStatusCode() == HttpStatus.SC_OK, "Response status is not ok");

				String responseBody = response.getResponseBody();
				logger.debug(responseBody);
				Assert.assertNotNull(responseBody, "Response body is null");

				solrFields = new JSONObject(responseBody).getJSONArray("fields");
			}
		} catch (Exception e) {
			logger.error("Unable to read Solr fields", e);
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

		String body = null;
		String address = null;
		/*
		 * In case in the future we will let the user to choose between GET and POST...
		 */
		HttpMethod method = HttpMethod.Post;
		if (method == HttpMethod.Get) {
			address = solrConfiguration.toString();
		} else if (method == HttpMethod.Post) {
			address = solrConfiguration.toString(false);
			body = solrConfiguration.getQueryParameters();
			requestHeaders.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		}
		setDataProxy(new SolrDataProxy(address, method, body, facetField, requestHeaders, offset, fetchSize, maxResults, isFacet()));
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

	public String getSolrCollection() {
		return solrConfiguration.getCollection();
	}

	public String getSolrUrlWithCollection() {
		StringBuilder sb = new StringBuilder();
		sb.append(solrConfiguration.getUrl());
		if (!solrConfiguration.getUrl().endsWith("/"))
			sb.append("/");
		sb.append(solrConfiguration.getCollection());
		return sb.toString();
	}

	public void setSolrQuery(SolrQuery solrQuery) {
		setSolrQuery(solrQuery, Collections.EMPTY_MAP);
	}

	public void setSolrQuery(SolrQuery solrQuery, Map<String, String> facets) {
		solrConfiguration.setSolrQuery(solrQuery);
		try {
			JSONObject jsonConfiguration = new JSONObject(configuration);
			initDataProxy(jsonConfiguration, true);
			initDataReader(jsonConfiguration, true);
			if (facets != null && !facets.isEmpty()) {
				CompositeSolrDataReader compositeSolrDataReader = new CompositeSolrDataReader((SolrDataReader) dataReader);

				for (String facet : facets.keySet()) {
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

	public void setSolrQueryParameters(SolrQuery solrQuery, Map parametersMap) {
		try {
			JSONObject jsonConfiguration = new JSONObject(configuration);
			List<Couple<String, String>> filterQueries = getListProp(SolrDataSetConstants.SOLR_FILTER_QUERY, jsonConfiguration, true);
			if (filterQueries != null && !filterQueries.isEmpty()) {
				String[] array = new String[filterQueries.size()];
				for (int i = 0; i < array.length; i++) {
					if (filterQueries != null && filterQueries.get(i) != null && filterQueries.get(i).getSecond() != null
							&& filterQueries.get(i).getSecond().contains(",")) {
						String multivalue = filterQueries.get(i).getSecond().replace(",", " OR ");
						multivalue = "(" + multivalue + ")";
						array[i] = filterQueries.get(i).getFirst() + ":" + multivalue;
					} else
						array[i] = filterQueries.get(i).getFirst() + ":" + filterQueries.get(i).getSecond();
				}
				solrQuery.setFilterQueries(array);
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
		return evaluationStrategy;
	}

	public void setEvaluationStrategy(DatasetEvaluationStrategyType evaluationStrategy) {
		this.evaluationStrategy = evaluationStrategy;
	}

	public List<String> getTextFields() {
		List<JSONPathDataReader.JSONPathAttribute> attributes = ((SolrDataReader) dataReader).getJsonPathAttributes("text");
		List<String> textFields = new ArrayList<>(attributes.size());
		for (JSONPathDataReader.JSONPathAttribute attribute : attributes) {
			textFields.add(attribute.getName());
		}
		return textFields;
	}

	public int getFacetsLimitOption() {
		return facetsLimitOption;
	}

	public void setFacetsLimitOption(int facetsLimitOption) {
		this.facetsLimitOption = facetsLimitOption;
	}

	/*
	 * These snippets of code come from a common utilities logic, now the logic behind parameters handling is inside each type of dataset
	 */
	@Override
	public void setParametersMap(Map<String, String> paramValues) throws JSONException {
		List<JSONObject> parameters = getDataSetParameters();
		if (parameters.size() > paramValues.size()) {
			String parameterNotValorizedStr = getParametersNotValorized(parameters, paramValues);
			throw new ParametersNotValorizedException("The following parameters have no value [" + parameterNotValorizedStr + "]");
		}

		if (paramValues.size() > 0) {
			for (String paramName : paramValues.keySet()) {
				for (int i = 0; i < parameters.size(); i++) {
					JSONObject parameter = parameters.get(i);
					if (paramName.equals(parameter.optString("namePar"))) {
						String[] values = getValuesAsArray(paramValues, paramName, parameter);
						List<String> encapsulatedValues = encapsulateValues(parameter, values);
						paramValues.put(paramName, org.apache.commons.lang3.StringUtils.join(encapsulatedValues, ","));
						break;
					}
				}
			}
			setParamsMap(paramValues);
		}
	}

	private String[] getValuesAsArray(Map<String, String> paramValues, String paramName, JSONObject parameter) {
		boolean isMultiValue = parameter.optBoolean("multiValuePar");
		String paramValue = paramValues.get(paramName);
		String[] values = null;
		if (isMultiValue) {
			List<String> list = new ArrayList<String>();
			boolean paramValueConsumed = false;
			try {
				JSONArray jsonArray = new JSONArray(paramValue);
				for (int j = 0; j < jsonArray.length(); j++) {
					list.add(jsonArray.getString(j));
				}
				paramValueConsumed = true;
			} catch (JSONException e) {
				paramValueConsumed = false;
			}
			if (!paramValueConsumed) {
				list.add(paramValue);
			}
			values = list.toArray(new String[0]);
			if (values != null && values.length == 1 && !values[0].isEmpty()) {
				String valuesString = values[0];
//				if (valuesString.startsWith("'") && valuesString.endsWith("'")) {
//					// patch for KNOWAGE-4600: An error occurs when propagating a driver value with commas through cross navigation.
//					// Do nothing, keep values as it is
//				} else {
//					values = valuesString.split(",");
//				}
			}
		} else {
			values = Arrays.asList(paramValue).toArray(new String[0]);
		}
		return values;
	}

	private static String getParametersNotValorized(List<JSONObject> parameters, Map<String, String> parametersValues) throws JSONException {
		String toReturn = "";

		for (Iterator<JSONObject> iterator = parameters.iterator(); iterator.hasNext();) {
			JSONObject parameter = iterator.next();
			String parameterName = parameter.getString("namePar");
			if (parametersValues.get(parameterName) == null) {
				toReturn += parameterName;
				if (iterator.hasNext()) {
					toReturn += ", ";
				}
			}
		}
		return toReturn;
	}

	/**
	 * Encapsulate values into SQL values.
	 *
	 * For every type of data except string, the method convert the values to strings.
	 *
	 * With strings we can have two case:
	 * <ul>
	 * <li>String that starts and ends with single quote</li>
	 * <li>String that doesn't start and end with single quote</li>
	 * </ul>
	 *
	 * In the first case, FE are sending us SQL values that probably contain JSON escape (e.g., a JSON value like 'this string contains a \' in it').
	 *
	 * In the second case, FE are sending us standard not-SQL-escaded string ( e.g., a string like "this string contains a ' in it"). In this second case this
	 * method escapes single quote and duplicates them as requested by SQL.
	 *
	 * @param parameter Original parameter JSON metadata
	 * @param values    Actual values of parameters
	 * @return List of encapsulated values as strings
	 */
	private List<String> encapsulateValues(JSONObject parameter, String[] values) {
		String typePar = parameter.optString("typePar");
		boolean isString = "string".equalsIgnoreCase(typePar);
		String delim = isString ? "'" : "";

		List<String> newValues = new ArrayList<>();
		for (int j = 0; j < values.length; j++) {
			String value = values[j].trim();
			if (!value.isEmpty()) {
				if (value.startsWith(delim) && value.endsWith(delim)) {
					if (value.contains(",")) {
						value = value.substring(1, value.length() - 1);
						String[] valuesArray = value.split(",");
						String newValuesFromArray = "";
						for (int i = 0; i < valuesArray.length; i++) {
							String temp = valuesArray[i];
							if (!delim.isEmpty() && temp.startsWith(delim) && temp.endsWith(delim))
								temp = temp.substring(1, temp.length() - 1);
							temp = temp.replaceAll("'", "\"");
							if (i == 0) {
								if (!delim.isEmpty() && temp.startsWith(delim) && temp.endsWith(delim)) {
									temp = temp.substring(1, temp.length() - 1);
								}
								temp = "\"" + temp + "\"";
								newValuesFromArray = (temp);
							}

							else {
								if (!delim.isEmpty() && temp.startsWith(delim) && temp.endsWith(delim)) {
									temp = temp.substring(1, temp.length() - 1);
								}
								temp = "\"" + temp + "\"";
								newValuesFromArray = newValuesFromArray + " , " + (temp);
							}

						}
						newValues.add(newValuesFromArray);
					} else {
						if (isString) {
							value = value.substring(1, value.length() - 1);
						}
						if (!delim.isEmpty() && value.startsWith(delim) && value.endsWith(delim)) {
							value = value.substring(1, value.length() - 1);
						}
						newValues.add(value);
					}
				} else {
					if (isString) {
						// Duplicate single quote to transform it into an escaped SQL single quote
						value = value.replaceAll("'", "''");
					}
					newValues.add(value);
				}
			}
		}
		return newValues;
	}
}
