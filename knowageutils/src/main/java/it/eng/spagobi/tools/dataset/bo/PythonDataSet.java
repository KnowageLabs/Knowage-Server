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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.PythonDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader.JSONPathAttribute;
import it.eng.spagobi.tools.dataset.constants.PythonDataSetConstants;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.ConfigurationException;

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
		initDataReader(jsonConf);
	}

	private void initDataReader(JSONObject jsonConf) {
		// json data reader attributes
		String jsonPathItems = getProp(PythonDataSetConstants.REST_JSON_PATH_ITEMS, jsonConf, true);
		List<JSONPathAttribute> jsonPathAttributes;
		try {
			jsonPathAttributes = getJsonPathAttributes(PythonDataSetConstants.REST_JSON_PATH_ATTRIBUTES, jsonConf);
		} catch (JSONException e) {
			throw new ConfigurationException("Problems in configuration of data reader", e);
		}

		String directlyAttributes = getProp(PythonDataSetConstants.REST_JSON_DIRECTLY_ATTRIBUTES, jsonConf, true);
		setDataReader(new JSONPathDataReader(jsonPathItems, jsonPathAttributes, Boolean.parseBoolean(directlyAttributes), false));
	}

	private void initDataProxy(JSONObject jsonConf) {
		String address = getProp(PythonDataSetConstants.REST_ADDRESS, jsonConf, false);

		Map<String, String> requestHeaders;
		try {
			requestHeaders = getRequestHeadersPropMap(PythonDataSetConstants.REST_REQUEST_HEADERS, jsonConf);
		} catch (Exception e) {
			throw new ConfigurationException("Problems in configuration of data proxy", e);
		}
		String pythonScript = getProp(PythonDataSetConstants.REST_PYTHON_SCRIPT, jsonConf, true);
		String dataframeName = getProp(PythonDataSetConstants.REST_DATAFRAME_NAME, jsonConf, true);

		// Pagination parameters
		String offset = getProp(PythonDataSetConstants.REST_OFFSET, jsonConf, true);
		String fetchSize = getProp(PythonDataSetConstants.REST_FETCH_SIZE, jsonConf, true);
		String maxResults = getProp(PythonDataSetConstants.REST_MAX_RESULTS, jsonConf, true);

		String parameters = getProp(PythonDataSetConstants.REST_SCRIPT_PARAMETERS, jsonConf, true);

		setDataProxy(new PythonDataProxy(address, pythonScript, dataframeName, parameters, requestHeaders, offset, fetchSize, maxResults));
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

}
