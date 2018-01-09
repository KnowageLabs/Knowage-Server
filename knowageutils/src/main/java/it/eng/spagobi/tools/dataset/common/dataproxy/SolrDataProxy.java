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

	private String facetField = null;
	private boolean facets;
	private static final Logger logger = Logger.getLogger(SolrDataProxy.class);
	private int maxRestConf = 999999;

	public SolrDataProxy(String address, HttpMethod method, String facetField, Map<String, String> requestHeaders, String offsetParam, String fetchSizeParam,
			String maxResultsParam, boolean facets) {
		super(address, method, null, requestHeaders, offsetParam, fetchSizeParam, maxResultsParam, false);
		this.facetField = facetField;
		this.facets = facets;
	}

	@Override
	protected String setPaginationParameters(String address, IDataReader dataReader) {
		if (this.facets) {
			if (dataReader.isOffsetSupported() && dataReader.getOffset() > 0) {
				address = address + "&facet.offset=" + dataReader.getOffset();
			}

			if (dataReader.isFetchSizeSupported() && dataReader.getFetchSize() > 0) {
				address = address + "&facet.limit=" + dataReader.getFetchSize();
			}
		} else {
			if (dataReader.isOffsetSupported() && dataReader.getOffset() > 0) {
				address = address + "&start=" + dataReader.getOffset();
			}

			if (dataReader.isFetchSizeSupported() && dataReader.getFetchSize() > 0) {
				address = address + "&rows=" + dataReader.getFetchSize();
			} else if (dataReader.isMaxResultsSupported() && dataReader.getMaxResults() > 0) {
				address = address + "&rows=" + dataReader.getMaxResults();
			} else {
				address = address + "&rows=" + maxRestConf;
			}
			// if (getOffsetParam() != null) {
			// address = address + "&start=" + getOffsetParam();
			// }
			// if (getFetchSizeParam() != null) {
			// address = address + "&rows=" + getFetchSizeParam();
			// }
			//
			// if (dataReader.isOffsetSupported() && dataReader.isFetchSizeSupported()) {
			// int offsetPag = 0;
			// int offsetDef = 0;
			// int fetchPag = 0;
			// int fetchDef = 0;
			//
			// try {
			// if (getOffsetParam() != null) {
			// offsetDef = new Integer(getOffsetParam());
			// }
			// if (getFetchSizeParam() != null) {
			// fetchDef = new Integer(getFetchSizeParam());
			// }
			// } catch (Exception e) {
			// logger.debug("Error loading the offet and fetchParam");
			// offsetDef = 0;
			// fetchDef = 0;
			// }
			//
			// fetchPag = dataReader.getFetchSize();
			// offsetPag = dataReader.getOffset();
			//
			// if (offsetDef + offsetPag > 0) {
			// address = address + "&start=" + (offsetDef + offsetPag);
			// }
			//
			// if (fetchDef > 0 && fetchPag <= 0) {
			// address = address + "&rows=" + fetchDef;
			// } else if (fetchDef <= 0 && fetchPag > 0) {
			// address = address + "&rows=" + fetchPag;
			// } else if (fetchDef > 0 && fetchPag > 0) {
			// if (fetchDef + offsetDef <= offsetDef + offsetPag + fetchPag) {
			// address = address + "&rows=" + fetchPag;
			// } else {
			// address = address + "&rows=" + (fetchDef - offsetPag);
			// }
			// }
			// } else {
			//
			// if (getOffsetParam() != null) {
			// address = address + "&start=" + getOffsetParam();
			// }
			// if (getFetchSizeParam() != null) {
			// address = address + "&rows=" + getFetchSizeParam();
			// }
			//
			// }

		}
		return address;

	}

	@Override
	public IDataStore load(IDataReader dataReader) {
		if (!facets) {
			return super.load(dataReader);
		} else {
			try {
				Helper.checkNotNull(dataReader, "dataReader");

				List<NameValuePair> query = getQuery();

				String tempAddress = this.address.replaceAll(" ", "%20");

				Response response = RestUtilities.makeRequest(this.method, setPaginationParameters(tempAddress, dataReader), this.requestHeaders,
						this.requestBody, query);
				String responseBody = response.getResponseBody();
				if (response.getStatusCode() != HttpStatus.SC_OK) {
					throw new RESTDataProxyException(
							String.format("The response status is not ok: status=%d, response=%s", response.getStatusCode(), responseBody));
				}
				Assert.assertNotNull(responseBody, "responseBody is null");

				if (calculateResultNumberOnLoad && facetField != null) {

					int resultNumber = 0;
					// add stuff to get the number of facets
					response = RestUtilities.makeRequest(this.method, tempAddress + ("&stats=true&stats.field=" + facetField + "&stats.calcdistinct=true"),
							this.requestHeaders, this.requestBody, query);
					String responseBodyNumber = response.getResponseBody();
					if (response.getStatusCode() != HttpStatus.SC_OK) {
						logger.debug("Can not resolve the resolutNumber of the query" + responseBodyNumber);
						response = RestUtilities.makeRequest(this.method, address, this.requestHeaders, this.requestBody, query);
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
