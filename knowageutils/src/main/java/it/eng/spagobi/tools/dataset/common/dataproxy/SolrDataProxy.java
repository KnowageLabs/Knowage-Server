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
package it.eng.spagobi.tools.dataset.common.dataproxy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;

import com.jayway.jsonpath.JsonPath;

import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.SolrDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;
import it.eng.spagobi.utilities.rest.RestUtilities.Response;

public class SolrDataProxy extends RESTDataProxy {

	private String facetField;
	private boolean facets;
	private static final Logger logger = Logger.getLogger(SolrDataProxy.class);
	private int maxRestConf = 999999;

	public SolrDataProxy(String address, HttpMethod method, String body, String facetField, Map<String, String> requestHeaders, String offsetParam, String fetchSizeParam,
			String maxResultsParam, boolean facets) {
		super(address, method, body, requestHeaders, offsetParam, fetchSizeParam, maxResultsParam, false);
		this.facetField = facetField;
		this.facets = facets;
	}

	private String createPaginationParameters(IDataReader dataReader) {
		StringBuilder paginationParam = new StringBuilder();
		if (this.facets) {
			if (dataReader.isOffsetSupported() && dataReader.getOffset() > 0) {
				paginationParam.append("&facet.offset=" + dataReader.getOffset());
			}

			if (dataReader.isFetchSizeSupported() && dataReader.getFetchSize() > 0) {
				paginationParam.append("&facet.limit=" + dataReader.getFetchSize());
			}
		} else {
			if (dataReader.isOffsetSupported() && dataReader.getOffset() > 0) {
				paginationParam.append("&start=" + dataReader.getOffset());
			}

			paginationParam.append("&rows=");
			if (dataReader.isFetchSizeSupported() && dataReader.getFetchSize() > 0) {
				paginationParam.append(dataReader.getFetchSize());
			} else if (dataReader.isMaxResultsSupported() && dataReader.getMaxResults() > 0) {
				paginationParam.append(dataReader.getMaxResults());
			} else {
				paginationParam.append(maxRestConf);
			}

		}

		return paginationParam.toString();

	}

	private void addPaginationParams(IDataReader dataReader) {
		String paginationParam = createPaginationParameters(dataReader);
		if (method == HttpMethod.Get) {
			setAddress(getAddress() + paginationParam);
		} else if (method == HttpMethod.Post) {
			setRequestBody(getRequestBody() + paginationParam);
		}

	}

	@Override
	public IDataStore load(IDataReader dataReader) {
		if (!facets) {
			setAddress(getAddress().replaceAll(" ", "%20"));
			addPaginationParams(dataReader);
			logger.info("Solr query to execute has address [" + getAddress() + "] and body [" + getRequestBody() + "]");
			return super.load(dataReader);
		} else {
			try {
				Helper.checkNotNull(dataReader, "dataReader");

				List<NameValuePair> query = getQuery();

				String tempAddress = getAddress().replaceAll(" ", "%20");
				String tempBody = getRequestBody();
				if (method == HttpMethod.Get) {
					tempAddress += createPaginationParameters(dataReader);
				} else if (method == HttpMethod.Post) {
					tempBody += createPaginationParameters(dataReader);
				}
				logger.info("Solr query to execute has address [" + tempAddress + "] and body [" + tempBody + "]");

				Response response = RestUtilities.makeRequest(this.method, tempAddress, getRequestHeaders(),
						tempBody, query);
				String responseBody = response.getResponseBody();
				if (response.getStatusCode() != HttpStatus.SC_OK) {
					throw new RESTDataProxyException(
							String.format("The response status is not ok: status=%d, response=%s", response.getStatusCode(), responseBody));
				}
				Assert.assertNotNull(responseBody, "responseBody is null");

				if (calculateResultNumberOnLoad && facetField != null) {
					boolean resultNumberNotAvailable = true;
					int resultNumber = 0;
					// add stuff to get the number of facets
					response = RestUtilities.makeRequest(this.method, tempAddress + ("&stats=true&stats.field=" + facetField + "&stats.calcdistinct=true"),
							getRequestHeaders(), getRequestBody(), query);
					String responseBodyNumber = response.getResponseBody();

					try {
						Object parsed = JsonPath.read(responseBodyNumber, "$.stats.stats_fields.*.countDistinct");
						resultNumberNotAvailable = parsed == null;
					} catch (Exception e) {
						resultNumberNotAvailable = true;
					}
					if (response.getStatusCode() != HttpStatus.SC_OK || resultNumberNotAvailable) {
						logger.debug("Can not resolve the resolutNumber of the query" + responseBodyNumber);
						response = RestUtilities.makeRequest(this.method, tempAddress, getRequestHeaders(), getRequestBody(), query);
						responseBodyNumber = response.getResponseBody();
						Object parsed = JsonPath.read(responseBodyNumber, "$.facet_counts.facet_fields.*.[*]");
						List<Object> parsedData;
						if (parsed instanceof List) {
							parsedData = (List<Object>) parsed;
						} else {
							parsedData = Arrays.asList(parsed);
						}
						resultNumber = parsedData.size() / 2;
					} else {
						Object parsed = JsonPath.read(responseBodyNumber, "$.stats.stats_fields.*.countDistinct");
						// can be an array or a single object

						if (parsed instanceof List) {
							List<Integer> parsedDataCount = (List<Integer>) parsed;
							for (int i = 0; i < parsedDataCount.size(); i++) {
								resultNumber = resultNumber + parsedDataCount.get(i);
							}
						} else {
							resultNumber = (Integer) (parsed);
						}
					}

					((SolrDataReader) dataReader).setResultNumber(resultNumber);
				}

				dataReader.setCalculateResultNumberEnabled(calculateResultNumberOnLoad);
				IDataStore res = dataReader.read(responseBody);

				Assert.assertNotNull(res, "datastore is null");
				return res;
			} catch (RESTDataProxyException e) {
				throw e;
			} catch (Exception e) {
				throw new RESTDataProxyException(e);
			}

		}
	}

}
