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

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.RESTDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader.JSONPathAttribute;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.listener.DataSetListenerManager;
import it.eng.spagobi.tools.dataset.listener.DataSetListenerManagerFactory;
import it.eng.spagobi.tools.dataset.notifier.NotifierServlet;
import it.eng.spagobi.tools.dataset.notifier.fiware.OAuth2Utils;
import it.eng.spagobi.tools.dataset.notifier.fiware.OrionContextSubscriber;
import it.eng.spagobi.tools.dataset.utils.ParametersResolver;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.ConfigurationException;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RESTDataSet extends ConfigurableDataSet {

	private static final Logger log = Logger.getLogger(RESTDataSet.class);

	public static final String DATASET_TYPE = "SbiRESTDataSet";

	private static final String NAME_JSON_PATH_ATTRIBUTE_PROP_NAME = "name";

	private static final String JSON_PATH_VALUE_JSON_PATH_ATTRIBUTE_PROP_NAME = "jsonPathValue";

	private static final String JSON_PATH_TYPE_JSON_PATH_ATTRIBUTE_PROP_NAME = "jsonPathType";

	private final ParametersResolver parametersResolver = new ParametersResolver();

	private boolean ngsi;

	/**
	 * for testing purpose
	 */
	private boolean ignoreConfigurationOnLoad;

	private boolean notifiable;

	public RESTDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

		initConf(false);
	}

	/**
	 * protected for testing purpose
	 * 
	 * @param resolveParams
	 */
	protected void initConf(boolean resolveParams) {
		// config alread set
		JSONObject jsonConf = getJSONConfig();
		Assert.assertNotNull(jsonConf, "configuration is null");

		initConf(jsonConf, resolveParams);
	}

	public RESTDataSet(JSONObject jsonConf) {
		Helper.checkNotNull(jsonConf, "jsonConf");

		setConfiguration(jsonConf.toString());
		initConf(jsonConf, false);
	}

	@Override
	public void loadData(int offset, int fetchSize, int maxResults) {
		if (!ignoreConfigurationOnLoad) {
			// reload the configuration for possible changes due to
			// customizable properties through parameters and/or profile attributes
			initConf(true);
		}

		super.loadData(offset, fetchSize, maxResults);

		// notify for all listeners (also for not NGSI datasets)
		notifyListeners();

		// after the first datastore initialization
		if (isNgsi() && NotifierServlet.isNotifiable()) {
			log.info(String.format("Subscribe NGSI dataset with label %s to orion notifications.", getLabel()));
			subscribeNGSI();
		}
	}

	private void notifyListeners() {
		DataSetListenerManager manager = DataSetListenerManagerFactory.getManager();
		String uuid=getUserId();
		if (uuid==null) {
			// temporary dataset
			return;
		}
		
		String label = getLabel();
		if (label == null) {
			// temporary dataset
			return;
		}
		manager.addCometListenerIfInitializedAndAbsent(uuid, label, "1");
		manager.changedDataSet(uuid, label, this);
	}

	private void subscribeNGSI() {
		try {
			OrionContextSubscriber subscriber = new OrionContextSubscriber(this);
			subscriber.subscribeNGSI();
			notifiable = true;
		} catch (Exception e) {
			log.error("Errror in Orion subscription", e);
			notifiable = false;
		}
	}

	public boolean isNotifiable() {
		return notifiable;
	}

	@Override
	public RESTDataProxy getDataProxy() {
		return (RESTDataProxy) super.getDataProxy();
	}

	@Override
	public JSONPathDataReader getDataReader() {
		return (JSONPathDataReader) super.getDataReader();
	}

	/**
	 * for testing
	 * 
	 * @param ignoreConfigurationOnLoad
	 */
	public void setIgnoreConfigurationOnLoad(boolean ignoreConfigurationOnLoad) {
		this.ignoreConfigurationOnLoad = ignoreConfigurationOnLoad;
	}

	private void initConf(JSONObject jsonConf, boolean resolveParams) {
		initNGSI(jsonConf, resolveParams);
		initDataProxy(jsonConf, resolveParams);
		initDataReader(jsonConf, resolveParams);
	}

	private void initNGSI(JSONObject jsonConf, boolean resolveParams) {
		String ngsiProp = getProp(DataSetConstants.REST_NGSI, jsonConf, true, false);
		this.ngsi = Boolean.parseBoolean(ngsiProp);
	}

	private void initDataReader(JSONObject jsonConf, boolean resolveParams) {
		// json data reader attributes
		String jsonPathItems = getProp(DataSetConstants.REST_JSON_PATH_ITEMS, jsonConf, true, resolveParams);
		List<JSONPathAttribute> jsonPathAttributes;
		try {
			jsonPathAttributes = getJsonPathAttributes(DataSetConstants.REST_JSON_PATH_ATTRIBUTES, jsonConf, resolveParams);
		} catch (JSONException e) {
			throw new ConfigurationException("Problems in configuration of data reader", e);
		}

		String directlyAttributes = getProp(DataSetConstants.REST_JSON_DIRECTLY_ATTRIBUTES, jsonConf, true, false);
		setDataReader(new JSONPathDataReader(jsonPathItems, jsonPathAttributes, Boolean.parseBoolean(directlyAttributes),this.ngsi));
	}

	private void initDataProxy(JSONObject jsonConf, boolean resolveParams) {
		// data proxy attributes
		String address = getProp(DataSetConstants.REST_ADDRESS, jsonConf, false, resolveParams);

		// can be null not empty
		String requestBody = getProp(DataSetConstants.REST_REQUEST_BODY, jsonConf, true, resolveParams);

		String method = getProp(DataSetConstants.REST_HTTP_METHOD, jsonConf, false, resolveParams);
		method = method.toLowerCase();
		HttpMethod methodEnum;
		try {
			methodEnum = HttpMethod.valueOf(method.substring(0, 1).toUpperCase() + method.substring(1));
		} catch (Exception e) {
			throw new ConfigurationException(String.format("HTTP Method is not valid in configuration: %s", jsonConf.toString()), e);
		}

		// no request body with get
		if (HttpMethod.Get.equals(methodEnum) && requestBody != null) {
			throw new ConfigurationException(String.format("A get request can't have request body: %s", jsonConf.toString()));
		}

		Map<String, String> requestHeaders;
		try {
			requestHeaders = getRequestHeadersPropMap(DataSetConstants.REST_REQUEST_HEADERS, jsonConf, resolveParams);
			
			//add bearer token for OAuth Fiware
			if ( resolveParams && OAuth2Utils.isOAuth2() && !OAuth2Utils.containsOAuth2(requestHeaders)) {
				String oAuth2Token = getOAuth2Token();
				if (oAuth2Token!=null) {
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

		setDataProxy(new RESTDataProxy(address, methodEnum, requestBody, requestHeaders, offset, fetchSize, maxResults,isNgsi()));
	}

	public String getOAuth2Token() {
		UserProfile up = getUserProfile();
		if (up == null) {
			return null;
		}
		
		String uuid = (String) up.getUserUniqueIdentifier();
		return uuid;
	}

	private JSONObject getJSONConfig() {
		// String config = JSONUtils.escapeJsonString(getConfiguration());
		JSONObject jsonConf = ObjectUtils.toJSONObject(getConfiguration());
		return jsonConf;
	}

	private List<JSONPathAttribute> getJsonPathAttributes(String propName, JSONObject conf, boolean resolveParams) throws JSONException {
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
			String name = getProp(NAME_JSON_PATH_ATTRIBUTE_PROP_NAME, ojson, false, resolveParams);
			String jsonPathValue = getProp(JSON_PATH_VALUE_JSON_PATH_ATTRIBUTE_PROP_NAME, ojson, false, resolveParams);
			String jsonPathType = getProp(JSON_PATH_TYPE_JSON_PATH_ATTRIBUTE_PROP_NAME, ojson, false, resolveParams);
			res.add(new JSONPathAttribute(name, jsonPathValue, jsonPathType));
		}
		return res;
	}

	private Map<String, String> getRequestHeadersPropMap(String propName, JSONObject conf, boolean resolveParams) throws JSONException {
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
			if (resolveParams) {
				key = parametersResolver.resolveAll(key, this);
				value = parametersResolver.resolveAll(value, this);
			}
			res.put(key, value);
		}
		return res;
	}

	/**
	 * Case: Return null if it's empty and optional
	 * 
	 * @param propName
	 * @param conf
	 * @param optional
	 * @param resolveParams
	 * @return
	 */
	private String getProp(String propName, JSONObject conf, boolean optional, boolean resolveParams) {
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
			// resolve parameters and profile attributes
			if (resolveParams) {
				r = parametersResolver.resolveAll(r, this);
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

	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd = super.toSpagoBiDataSet();
		sbd.setType(DATASET_TYPE);
		return sbd;
	}

	@Override
	public String getSignature() {
		JSONObject config = getJSONConfig();
		// it's supposed that it doesn't change with same config derived from same json
		String res = config.toString();
		res += toStringUserParameters();
		res = Helper.md5(res);
		return res;
	}

	private String toStringUserParameters() {
		StringBuilder res = new StringBuilder();
		for (Entry<String, Object> entry : new TreeMap<String, Object>(userProfileParameters).entrySet()) {
			res.append(",");
			res.append(entry.getKey());
			res.append(":");
			res.append(entry.getValue());
		}
		return res.toString();
	}

	public boolean isNgsi() {
		return ngsi;
	}
	
	@Override
	public IDataSource getDataSource() {
		return null;
	}
	
	@Override
	public void setDataSource(IDataSource dataSource) {
		throw new IllegalStateException(RESTDataSet.class.getSimpleName()+" doesn't need the dataSource");
	}

	public String getUserId() {
		UserProfile up = getUserProfile();
		if (up == null) {
			return null;
		}
		
		String uuid = (String) up.getUserId();
		return uuid;
	}

}
