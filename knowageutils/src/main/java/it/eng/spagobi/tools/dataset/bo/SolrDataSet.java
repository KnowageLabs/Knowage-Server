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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.SolrDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader.JSONPathAttribute;
import it.eng.spagobi.tools.dataset.common.datareader.SolrDataReader;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.notifier.fiware.OAuth2Utils;
import it.eng.spagobi.utilities.exceptions.ConfigurationException;
import it.eng.spagobi.utilities.objects.Couple;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;

public class SolrDataSet extends RESTDataSet {

	public static final String DATASET_TYPE = "SbiSolrDataSet";
	private static final Logger logger = Logger.getLogger(SolrDataSet.class);
	private boolean isFacet = true;

	public SolrDataSet() {

	};

	public SolrDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

	}

	public SolrDataSet(JSONObject jsonConf) {
		this();
		setConfiguration(jsonConf.toString());
		initConf(jsonConf, false);

	}

	public SolrDataSet(JSONObject jsonConf, HashMap<String, String> parametersMap) {
		this();
		this.setParamsMap(parametersMap);
		setConfiguration(jsonConf.toString());
		initConf(jsonConf, true);

	}

	@Override
	public void initConf(JSONObject jsonConf, boolean resolveParams) {
		String solrType = getProp(DataSetConstants.SOLR_TYPE, jsonConf, false, resolveParams);
		isFacet = solrType == null || !solrType.equals(DataSetConstants.SOLR_TYPE_DOCUMENT);
		initDataProxy(jsonConf, resolveParams);
		initDataReader(jsonConf, resolveParams);
	}

	private void initDataReader(JSONObject jsonConf, boolean resolveParams) {
		// json data reader attributes

		if (!isFacet) {
			logger.debug("Reading Solr dataste documents");
			List<JSONPathAttribute> jsonPathAttributes;
			try {
				jsonPathAttributes = getJsonPathAttributes(DataSetConstants.REST_JSON_PATH_ATTRIBUTES, jsonConf, resolveParams);
			} catch (JSONException e) {
				throw new ConfigurationException("Problems in configuration of data reader", e);
			}

			String directlyAttributes = getProp(DataSetConstants.REST_JSON_DIRECTLY_ATTRIBUTES, jsonConf, true, resolveParams);

			setDataReader(new SolrDataReader("$.response.docs.[*]", jsonPathAttributes, Boolean.parseBoolean(directlyAttributes)));

		} else {
			logger.debug("Reading Solr dataset facets");
			if (getProp(DataSetConstants.SOLR_FACET_QUERY, jsonConf, true, resolveParams) != null) {
				setDataReader(new SolrDataReader("$.facet_counts.facet_queries", true, true));
			} else {
				setDataReader(new SolrDataReader("$.facet_counts.facet_fields.*.[*]", true, false));
			}

		}

	}

	private void initDataProxy(JSONObject jsonConf, boolean resolveParams) {
		// data proxy attributes
		String address = getProp(DataSetConstants.REST_ADDRESS, jsonConf, false, resolveParams);

		HttpMethod methodEnum = HttpMethod.Get;

		Map<String, String> requestHeaders;
		try {
			requestHeaders = getRequestHeadersPropMap(DataSetConstants.REST_REQUEST_HEADERS, jsonConf, resolveParams);

			// add bearer token for OAuth Fiware
			if (resolveParams && OAuth2Utils.isOAuth2() && !OAuth2Utils.containsOAuth2(requestHeaders)) {
				String oAuth2Token = getOAuth2Token();
				if (oAuth2Token != null) {
					requestHeaders.putAll(OAuth2Utils.getOAuth2Headers(oAuth2Token));
				}
			}
		} catch (Exception e) {
			throw new ConfigurationException("Problems in configuration of data proxy", e);
		}

		// Pagination parameters
		String offset = getProp(DataSetConstants.REST_OFFSET, jsonConf, true, resolveParams);

		String fetchSize = getProp(DataSetConstants.REST_FETCH_SIZE, jsonConf, true, resolveParams);

		String maxResults = getProp(DataSetConstants.REST_MAX_RESULTS, jsonConf, true, resolveParams);

		StringBuilder addressBuilder = new StringBuilder(address);

		addQueryParam(addressBuilder, jsonConf, resolveParams);

		addAdditionalParams(addressBuilder, jsonConf);

		// add json writer
		addressBuilder.append("&wt=json");

		if (isFacet) {
			addFacetParams(addressBuilder, jsonConf, resolveParams);
			setDataProxy(new SolrDataProxy(addressBuilder.toString(), methodEnum, getProp(DataSetConstants.SOLR_FACET_FIELD, jsonConf, true, resolveParams),
					requestHeaders, offset, fetchSize, maxResults, true));
		} else {
			setDataProxy(new SolrDataProxy(addressBuilder.toString(), methodEnum, null, requestHeaders, offset, maxResults, maxResults, false));
		}

	}

	private void addFacetParams(StringBuilder address, JSONObject jsonConf, boolean resolveParams) {
		logger.debug("Address without facet params [" + address + "]");
		if (address.lastIndexOf("?") < 0) {
			address.append("?");
		} else {
			address.append("&");
		}
		address.append("facet=on");
		String facet = getProp(DataSetConstants.SOLR_FACET_FIELD, jsonConf, true, resolveParams);
		if (facet == null) {
			facet = getProp(DataSetConstants.SOLR_FACET_QUERY, jsonConf, true, resolveParams);
			if (facet != null) {
				if (!facet.contains("facet.query")) {
					address.append("&facet.query=" + facet);
				} else {
					address.append("&");
					address.append(facet);
				}
			}
		} else {
			try {
				facet = URLEncoder.encode(facet, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("Error creating facet", e);
				facet = URLEncoder.encode(facet);
			}
			address.append("&facet.field=" + facet);
		}

		String facetPrefix = getProp(DataSetConstants.SOLR_FACET_PREFIX, jsonConf, true, resolveParams);
		if (facetPrefix != null) {
			address.append("&facet.prefix=" + facetPrefix);
		}

		logger.debug("Address with facet params [" + address + "]");

	}

	private void addQueryParam(StringBuilder address, JSONObject jsonConf, boolean resolveParams) {
		logger.debug("Address without solr query [" + address + "]");
		if (address.lastIndexOf("?") < 0) {
			address.append("?");
		} else {
			address.append("&");
		}
		address.append("q=");
		String query = getProp(DataSetConstants.REST_REQUEST_BODY, jsonConf, true, resolveParams);
		if (query == null) {
			query = "*.*";
		}
		address.append(query);

		logger.debug("Address with solr query [" + address + "]");
	}

	private void addAdditionalParams(StringBuilder address, JSONObject jsonConf) {
		logger.debug("Address without additional parameters [" + address + "]");

		List<Couple<String, String>> requestHeaders;
		try {
			requestHeaders = getListProp(DataSetConstants.SOLR_ADDITIONAL_PARAMETERS, jsonConf, true);

		} catch (Exception e) {
			throw new ConfigurationException("Problems in configuration of data proxy", e);
		}

		for (Iterator<Couple<String, String>> iterator = requestHeaders.iterator(); iterator.hasNext();) {
			Couple<String, String> key = iterator.next();
			String values = key.getSecond();
			address.append("&");
			address.append(key.getFirst());
			address.append("=");
			if (values.indexOf(",") > 0) {// for multivalue parameters
				int fieldEnd = values.indexOf(":") + 1;
				String fqKey = values.substring(0, fieldEnd);
				StringTokenizer stk = new StringTokenizer(values.substring(fieldEnd), ",");
				while (stk.hasMoreTokens()) {
					String value = stk.nextToken();
					address.append(fqKey);
					address.append(value);
					address.append(" OR ");
				}
				if (address.length() > 3) {
					address = address.delete(address.length() - 3, address.length());
				}

			} else {

				address.append(key.getSecond());
			}
		}

		logger.debug("Address with additional parameters  [" + address + "]");
	}

}
