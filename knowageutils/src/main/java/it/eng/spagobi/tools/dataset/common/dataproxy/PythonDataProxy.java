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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;
import it.eng.spagobi.utilities.rest.RestUtilities.Response;

public class PythonDataProxy extends AbstractDataProxy {

	private static final int OFFSET_NOT_DEFINED = -1;
	private static final int FETCH_SIZE_NOT_DEFINED = -1;
	private static final int MAX_RESULT_NOT_DEFINED = -1;

	protected String requestBody;
	protected String address;
	protected final Map<String, String> requestHeaders;
	protected final HttpMethod method;

	private final String offsetParam;
	private final String fetchSizeParam;
	private final String maxResultsParam;

	public PythonDataProxy(String address, String pythonScript, String dataframeName, String parameters, Map<String, String> requestHeaders, String offsetParam,
			String fetchSizeParam, String maxResultsParam) {
//		Helper.checkNotNull(address, "address");
//		Helper.checkNotEmpty(address, "address");
		// cab be empty
		Helper.checkNotNull(requestHeaders, "requestHeaders");
		// can be null, can't empty
		if (pythonScript != null) {
			Helper.checkNotEmpty(pythonScript, "pythonScript");
		}
		if (dataframeName != null) {
			Helper.checkNotEmpty(dataframeName, "dataframeName");
		}

		// offset and fetch size must exist together
		if (offsetParam != null) {
			Helper.checkNotEmpty(offsetParam, "offsetParam");
			Helper.checkNotNull(fetchSizeParam, "fetchSizeParam");
		}
		if (fetchSizeParam != null) {
			Helper.checkNotEmpty(fetchSizeParam, "fetchSizeParam");
			Helper.checkNotNull(offsetParam, "offsetParam");
		}
		if (maxResultsParam != null) {
			Helper.checkNotEmpty(maxResultsParam, "maxResultsParam");
		}

		this.address = address;
		this.method = HttpMethod.valueOf("Post");
		this.requestHeaders = new HashMap<String, String>(requestHeaders);
		this.requestHeaders.put("Content-Type", "application/json");
		this.requestBody = buildBodyAsJson(pythonScript, dataframeName, parameters);
		this.offsetParam = offsetParam;
		this.fetchSizeParam = fetchSizeParam;
		this.maxResultsParam = maxResultsParam;
	}

	private String buildBodyAsJson(String pythonScript, String dataframeName, String parameters) {
		JSONObject json = new JSONObject();
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, 5);
			Date expiresAt = calendar.getTime();
			String jwtToken = JWTSsoService.pythonScript2jwtToken(pythonScript, expiresAt);
			json.put("script", jwtToken);
			json.put("df_name", dataframeName);
			if (parameters != null) {
				ArrayList<JSONObject> parametersList = new ArrayList<JSONObject>();
				int start = 1, end = parameters.indexOf('}'), i = 1;
				while (end != -1) {
					String param = parameters.substring(start, end + 1);
					JSONObject jsonParameter = new JSONObject(param);
					parametersList.add(jsonParameter);
					start = end + 2;
					end = parameters.indexOf('}', start);
				}
				json.put("parameters", parametersList);
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Cannot build request body as Json", t);
		}

		return json.toString();
	}

	@Override
	public IDataStore load(IDataReader dataReader) {
		try {
			Helper.checkNotNull(dataReader, "dataReader");

			List<NameValuePair> query = getQuery();
			putParameterValuesInRequestBody();
			Response response = RestUtilities.makeRequest(this.method, this.address, this.requestHeaders, this.requestBody, query);
			String responseBody = response.getResponseBody();
			if (response.getStatusCode() != HttpStatus.SC_OK) {
				throw new RESTDataProxyException(
						String.format("The response status is not ok: status=%d, response=%s", response.getStatusCode(), responseBody));
			}
			Assert.assertNotNull(responseBody, "responseBody is null");
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

	private void putParameterValuesInRequestBody() {
		try {
			if (this.parameters != null && this.parameters.size() > 0) {
				JSONObject jsonBody = new JSONObject(this.requestBody);
				Iterator keys = this.parameters.keySet().iterator();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					String val = this.parameters.get(key);
					if (val.startsWith("'") || val.startsWith("\"")) {
						val = val.substring(1, val.length() - 1); // remove ''
					}
					JSONArray parameters = jsonBody.getJSONArray("parameters");
					for (int i = 0; i < parameters.length(); i++) {
						JSONObject param = parameters.getJSONObject(i);
						if (param.getString("name").equals(key)) {
							param.put("value", val);
						}
					}
				}
				this.requestBody = jsonBody.toString();
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Cannot insert runtime parameter value in request body", t);
		}
	}

	protected List<NameValuePair> getQuery() {
		List<NameValuePair> res = new ArrayList<NameValuePair>(3);
		if (offsetParam != null) {
			if (offset != OFFSET_NOT_DEFINED) {
				res.add(new NameValuePair(offsetParam, Integer.toString(offset)));
			}
			Assert.assertTrue(fetchSizeParam != null, "fetchSizeParam!=null");
			if (fetchSize != FETCH_SIZE_NOT_DEFINED) {
				res.add(new NameValuePair(fetchSizeParam, Integer.toString(fetchSize)));
			}
		}

		if (maxResultsParam != null && maxResults != MAX_RESULT_NOT_DEFINED) {
			res.add(new NameValuePair(maxResultsParam, Integer.toString(maxResults)));
		}

		return res;
	}

	protected String setPaginationParameters(String address, IDataReader dataReader) {
		return address;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public String getAddress() {
		return address;
	}

	public Map<String, String> getRequestHeaders() {
		return requestHeaders;
	}

	public HttpMethod getRequestMethod() {
		return method;
	}

	@Override
	public boolean isOffsetSupported() {
		return offsetParam != null;
	}

	@Override
	public boolean isFetchSizeSupported() {
		return fetchSizeParam != null;
	}

	@Override
	public boolean isMaxResultsSupported() {
		return maxResultsParam != null;
	}

	@Override
	public boolean isPaginationSupported() {
		boolean res = offsetParam != null;
		Assert.assertTrue(res == (fetchSizeParam != null), "res==(fetchSizeParam!=null)");
		return res;
	}

	public String getOffsetParam() {
		return offsetParam;
	}

	public String getFetchSizeParam() {
		return fetchSizeParam;
	}

	public String getMaxResultsParam() {
		return maxResultsParam;
	}

}
