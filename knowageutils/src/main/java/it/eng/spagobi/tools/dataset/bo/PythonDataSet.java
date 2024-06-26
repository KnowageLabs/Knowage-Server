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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.backendservices.rest.widgets.PythonUtils;
import it.eng.knowage.backendservices.rest.widgets.RUtils;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.PythonDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader.JSONPathAttribute;
import it.eng.spagobi.tools.dataset.constants.PythonDataSetConstants;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.ConfigurationException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class PythonDataSet extends ConfigurableDataSet {

	private static final Logger logger = Logger.getLogger(PythonDataSet.class);

	public static final String DATASET_TYPE = "SbiPythonDataSet";

	private static final String NAME_JSON_PATH_ATTRIBUTE_PROP_NAME = "name";
	private static final String JSON_PATH_VALUE_JSON_PATH_ATTRIBUTE_PROP_NAME = "jsonPathValue";
	private static final String JSON_PATH_TYPE_JSON_PATH_ATTRIBUTE_PROP_NAME = "jsonPathType";

	public PythonDataSet() {
	}

	public PythonDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

		initConf();
	}

	/**
	 * protected for testing purpose
	 *
	 */
	protected void initConf() {
		// config alread set
		JSONObject jsonConf = getJSONConfig();
		Assert.assertNotNull(jsonConf, "configuration is null");

		initConf(jsonConf);
	}

	public PythonDataSet(JSONObject jsonConf) {
		Helper.checkNotNull(jsonConf, "jsonConf");

		setConfiguration(jsonConf.toString());
		initConf(jsonConf);
	}

	@Override
	public void loadData(int offset, int fetchSize, int maxResults) {

		super.loadData(offset, fetchSize, maxResults);
	}

	@Override
	public PythonDataProxy getDataProxy() {
		return (PythonDataProxy) super.getDataProxy();
	}

	@Override
	public JSONPathDataReader getDataReader() {
		return (JSONPathDataReader) super.getDataReader();
	}

	public void initConf(JSONObject jsonConf) {
		initDataProxy(jsonConf);
		setDataReader(new JSONPathDataReader("$[*]", new ArrayList<JSONPathAttribute>(), true, false));
	}

	private void initDataProxy(JSONObject jsonConf) {
		String restAddress = "";
		Map<String, String> requestHeaders = null;
		try {
			requestHeaders = getRequestHeadersPropMap(PythonDataSetConstants.REST_REQUEST_HEADERS, jsonConf);
			JSONObject pythonEnv = new JSONObject(getProp(PythonDataSetConstants.PYTHON_ENVIRONMENT, jsonConf, false));
			restAddress = getRestAddressFromConf(pythonEnv, jsonConf);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Problems in configuration of data proxy", e);
		}
		String pythonScript = getProp(PythonDataSetConstants.PYTHON_SCRIPT, jsonConf, true);
		String parameters = getProp(PythonDataSetConstants.PYTHON_SCRIPT_PARAMETERS, jsonConf, true);
		String dataframeName = getProp(PythonDataSetConstants.PYTHON_DATAFRAME_NAME, jsonConf, true);
		// Pagination parameters
		String offset = getProp(PythonDataSetConstants.REST_OFFSET, jsonConf, true);
		String fetchSize = getProp(PythonDataSetConstants.REST_FETCH_SIZE, jsonConf, true);
		String maxResults = getProp(PythonDataSetConstants.REST_MAX_RESULTS, jsonConf, true);

		setDataProxy(new PythonDataProxy(restAddress, pythonScript, dataframeName, parameters, requestHeaders, offset, fetchSize, maxResults));
	}

	private String getRestAddressFromConf(JSONObject pythonEnv, JSONObject jsonConf) {
		String toReturn = "";
		try {
			String pythonAddress = null;
			String pythonDatasetType = getProp(PythonDataSetConstants.PYTHON_DATASET_TYPE, jsonConf, true);
			String label = pythonEnv.get("label").toString();
			if ("python".equals(pythonDatasetType)) {
				pythonAddress = PythonUtils.getPythonAddress(label);
			} else if ("r".equals(pythonDatasetType)) {
				pythonAddress = RUtils.getRAddress(label);
			} else {
				throw new IllegalStateException("Invalid Python Dataset Type: " + pythonDatasetType);
			}
			toReturn = pythonAddress.replaceAll("/+$", "") + "/dataset";
		} catch (Exception e) {
			logger.error("Cannot retrieve python address from configuration.", e);
			return "";
		}
		return toReturn;
	}

	private JSONObject getJSONConfig() {
		JSONObject jsonConf = ObjectUtils.toJSONObject(getConfiguration());
		return jsonConf;
	}

	protected List<JSONPathAttribute> getJsonPathAttributes(String propName, JSONObject conf) throws JSONException {
		checkPropExists(propName, conf);

		Object sub = conf.get(propName);
		if (!(sub instanceof JSONArray)) {
			throw new ConfigurationException(String.format("%s is not a json array in configuration: %s", propName, conf.toString()));
		}
		Assert.assertNotNull(sub, "property is null");
		JSONArray subs = (JSONArray) sub;
		List<JSONPathAttribute> res = new ArrayList<JSONPathDataReader.JSONPathAttribute>(subs.length());
		for (int i = 0; i < subs.length(); i++) {
			Object o = subs.get(i);
			if (!(o instanceof JSONObject)) {
				throw new ConfigurationException(String.format("The configuration for %s is not correct: %s", propName, conf.toString()));
			}
			JSONObject ojson = (JSONObject) o;
			String name = getProp(NAME_JSON_PATH_ATTRIBUTE_PROP_NAME, ojson, false);
			String jsonPathValue = getProp(JSON_PATH_VALUE_JSON_PATH_ATTRIBUTE_PROP_NAME, ojson, false);
			String jsonPathType = getProp(JSON_PATH_TYPE_JSON_PATH_ATTRIBUTE_PROP_NAME, ojson, false);
			res.add(new JSONPathAttribute(name, jsonPathValue, jsonPathType));
		}
		return res;
	}

	protected Map<String, String> getRequestHeadersPropMap(String propName, JSONObject conf) throws JSONException {
		if (!conf.has(propName) || conf.getString(propName).isEmpty()) {
			// optional property
			return Collections.emptyMap();
		}

		Object c = conf.get(propName);
		if (!(c instanceof JSONObject)) {
			throw new ConfigurationException(String.format("%s is not another json object in configuration: %s", propName, conf.toString()));
		}
		Assert.assertNotNull(c, "property is null");
		JSONObject r = (JSONObject) c;
		Map<String, String> res = new HashMap<String, String>(r.length());
		Iterator<String> it = r.keys();
		while (it.hasNext()) {
			String key = it.next();
			String value = r.getString(key);
			res.put(key, value);
		}
		return res;
	}

	protected String getProp(String propName, JSONObject conf, boolean optional) {
		if (!optional) {
			checkPropExists(propName, conf);
		} else {
			if (!conf.has(propName)) {
				return null;
			}
		}
		try {
			Object res = conf.get(propName);
			if (!(res instanceof String)) {
				throw new ConfigurationException(String.format("%s is not a string in configuration: %s", propName, conf.toString()));
			}
			Assert.assertNotNull(res, "property is null");
			String r = (String) res;
			r = r.trim();
			if (r.isEmpty()) {
				if (optional) {
					return null;
				}
				throw new ConfigurationException(String.format("%s is empty in configuration: %s", propName, conf.toString()));
			}
			return r;
		} catch (Exception e) {
			throw new ConfigurationException(String.format("Error during configuration: %s", conf.toString()), e);
		}
	}

	private static void checkPropExists(String propName, JSONObject conf) {
		if (!conf.has(propName)) {
			throw new ConfigurationException(String.format("%s is not present in configuration: %s", propName, conf.toString()));
		}
	}

	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd = super.toSpagoBiDataSet();
		sbd.setType(DATASET_TYPE);
		return sbd;
	}

	@Override
	public IDataSource getDataSource() {
		return null;
	}

	@Override
	public void setDataSource(IDataSource dataSource) {
		throw new IllegalStateException(RESTDataSet.class.getSimpleName() + " doesn't need the dataSource");
	}

	public String getUserId() {
		UserProfile up = getCurrentUserProfile();
		if (up == null) {
			return null;
		}

		String uuid = (String) up.getUserId();
		return uuid;
	}

	private UserProfile getCurrentUserProfile() {
		return getUserProfile() != null ? getUserProfile() : UserProfileManager.getProfile();
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
		} else {
			values = Arrays.asList(paramValue).toArray(new String[0]);
		}
		return values;
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
					if (value.contains("','")) {
						value = value.substring(1, value.length() - 1);
						String[] valuesArray = value.split("','");
						String newValuesFromArray = "";
						for (int i = 0; i < valuesArray.length; i++) {
							String temp = valuesArray[i];
							if (!delim.isEmpty() && temp.startsWith(delim) && temp.endsWith(delim))
								temp = temp.substring(1, temp.length() - 1);
							temp = temp.replaceAll("'", "''");
							if (i == 0) {
								if (!delim.isEmpty() && temp.startsWith(delim) && temp.endsWith(delim)) {
									temp = temp.substring(1, temp.length() - 1);
								}
								newValuesFromArray = (temp);
							}

							else {
								if (!delim.isEmpty() && temp.startsWith(delim) && temp.endsWith(delim)) {
									temp = temp.substring(1, temp.length() - 1);
								}
								newValuesFromArray = newValuesFromArray + "," + (temp);
							}

						}
						newValues.add(newValuesFromArray);
					} else {
						if (isString) {
							value = value.substring(1, value.length() - 1);
							value = value.replaceAll("'", "''");
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
