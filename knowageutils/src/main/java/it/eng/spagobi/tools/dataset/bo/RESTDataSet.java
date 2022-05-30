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
import java.util.Vector;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.RESTDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader.JSONPathAttribute;
import it.eng.spagobi.tools.dataset.constants.RESTDataSetConstants;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.listener.DataSetListenerManager;
import it.eng.spagobi.tools.dataset.listener.DataSetListenerManagerFactory;
import it.eng.spagobi.tools.dataset.notifier.NotifierServlet;
import it.eng.spagobi.tools.dataset.notifier.fiware.OAuth2Utils;
import it.eng.spagobi.tools.dataset.notifier.fiware.OrionContextSubscriber;
import it.eng.spagobi.tools.dataset.utils.ParametersResolver;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.ConfigurationException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.objects.Couple;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;

public class RESTDataSet extends ConfigurableDataSet {

	private static final Logger logger = Logger.getLogger(RESTDataSet.class);

	public static final String DATASET_TYPE = "SbiRESTDataSet";

	private static final String NAME_JSON_PATH_ATTRIBUTE_PROP_NAME = "name";
	private static final String JSON_PATH_VALUE_JSON_PATH_ATTRIBUTE_PROP_NAME = "jsonPathValue";
	private static final String JSON_PATH_TYPE_JSON_PATH_ATTRIBUTE_PROP_NAME = "jsonPathType";

	private final ParametersResolver parametersResolver = new ParametersResolver();

	private boolean ngsi;

	private boolean realtimeNgsiConsumer = true;

	private boolean notifiable;

	public RESTDataSet() {
	}

	public RESTDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

		initConf(false);
	}

	/**
	 * protected for testing purpose
	 *
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

		super.loadData(offset, fetchSize, maxResults);

		if (isNgsi()) {
			// notify for all listeners
			notifyListeners();

			// after the first datastore initialization
			if (NotifierServlet.isNotifiable()) {
				logger.info(String.format("Subscribe NGSI dataset with label %s to orion notifications.", getLabel()));
				subscribeNGSI();
			}
		}
	}

	private void notifyListeners() {
		logger.debug("IN");
		DataSetListenerManager manager = DataSetListenerManagerFactory.getManager();
		String uuid = getUserId();
		if (uuid == null) {
			// temporary dataset
			return;
		}

		String label = getLabel();
		if (label == null) {
			// temporary dataset
			logger.debug("Label is null, returning null");
			return;
		}
		manager.addCometListenerIfInitializedAndAbsent(uuid, label, "1");
		manager.changedDataSet(uuid, label, this);
		logger.debug("OUT");
	}

	public void subscribeNGSI() {
		try {
			OrionContextSubscriber subscriber = new OrionContextSubscriber(this, getCurrentUserProfile());
			subscriber.subscribeNGSI();
			notifiable = true;
		} catch (Exception e) {
			logger.error("Errror in Orion subscription", e);
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

	public void initConf(JSONObject jsonConf, boolean resolveParams) {
		initNGSI(jsonConf, resolveParams);
		initDataProxy(jsonConf, resolveParams);
		initDataReader(jsonConf, resolveParams);
	}

	private void initNGSI(JSONObject jsonConf, boolean resolveParams) {
		String ngsiProp = getProp(RESTDataSetConstants.REST_NGSI, jsonConf, true, false);
		this.ngsi = Boolean.parseBoolean(ngsiProp);
	}

	private void initDataReader(JSONObject jsonConf, boolean resolveParams) {
		// json data reader attributes
		String jsonPathItems = getProp(RESTDataSetConstants.REST_JSON_PATH_ITEMS, jsonConf, true, resolveParams);
		List<JSONPathAttribute> jsonPathAttributes;
		try {
			jsonPathAttributes = getJsonPathAttributes(RESTDataSetConstants.REST_JSON_PATH_ATTRIBUTES, jsonConf, resolveParams);
		} catch (JSONException e) {
			throw new ConfigurationException("Problems in configuration of data reader", e);
		}

		String directlyAttributes = getProp(RESTDataSetConstants.REST_JSON_DIRECTLY_ATTRIBUTES, jsonConf, true, false);
		setDataReader(new JSONPathDataReader(jsonPathItems, jsonPathAttributes, Boolean.parseBoolean(directlyAttributes), this.ngsi));
	}

	private void initDataProxy(JSONObject jsonConf, boolean resolveParams) {
		// data proxy attributes
		String address = getProp(RESTDataSetConstants.REST_ADDRESS, jsonConf, false, resolveParams);

		// can be null not empty
		String requestBody = getProp(RESTDataSetConstants.REST_REQUEST_BODY, jsonConf, true, resolveParams);

		String method = getProp(RESTDataSetConstants.REST_HTTP_METHOD, jsonConf, false, resolveParams);
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
			requestHeaders = getRequestHeadersPropMap(RESTDataSetConstants.REST_REQUEST_HEADERS, jsonConf, resolveParams);

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
		String offset = getProp(RESTDataSetConstants.REST_OFFSET, jsonConf, true, resolveParams);

		String fetchSize = getProp(RESTDataSetConstants.REST_FETCH_SIZE, jsonConf, true, resolveParams);

		String maxResults = getProp(RESTDataSetConstants.REST_MAX_RESULTS, jsonConf, true, resolveParams);

		setDataProxy(new RESTDataProxy(address, methodEnum, requestBody, requestHeaders, offset, fetchSize, maxResults, isNgsi()));
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

	protected List<JSONPathAttribute> getJsonPathAttributes(String propName, JSONObject conf, boolean resolveParams) throws JSONException {
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

	protected Map<String, String> getRequestHeadersPropMap(String propName, JSONObject conf, boolean resolveParams) throws JSONException {
		if (!conf.has(propName) || conf.getString(propName).isEmpty()) {
			// optional property
			return new HashMap<String, String>();
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
				key = resolveAll(key, this);
				value = resolveAll(value, this);
			}
			res.put(key, value);
		}
		return res;
	}

	protected List<Couple<String, String>> getListProp(String propName, JSONObject conf, boolean resolveParams) throws JSONException {
		if (!conf.has(propName) || conf.getString(propName).isEmpty()) {
			// optional property
			return Collections.emptyList();
		}

		Object c = conf.get(propName);
		if (!(c instanceof JSONArray)) {
			throw new ConfigurationException(String.format("%s is not another json object in configuration: %s", propName, conf.toString()));
		}
		Assert.assertNotNull(c, "property is null");
		JSONArray r = (JSONArray) c;
		List<Couple<String, String>> res = new ArrayList<Couple<String, String>>(r.length());

		for (int i = 0; i < r.length(); i++) {
			JSONObject jo = r.getJSONObject(i);
			String key = jo.getString("name");
			String value = jo.getString("value");
			if (resolveParams) {
				key = resolveAll(key, this);
				value = resolveAll(value, this);
				res.add(new Couple<String, String>(key, value));
			}
		}
		return res;
	}

	protected List<Couple<String, String>> getListProp(String propName, JSONObject conf, boolean resolveParams, UserProfile userProfile) throws JSONException {
		this.setUserProfile(userProfile);
		this.setUserProfileAttributes(userProfile.getUserAttributes());
		if (!conf.has(propName) || conf.getString(propName).isEmpty()) {
			// optional property
			return Collections.emptyList();
		}

		Object c = conf.get(propName);
		if (!(c instanceof JSONArray)) {
			throw new ConfigurationException(String.format("%s is not another json object in configuration: %s", propName, conf.toString()));
		}
		Assert.assertNotNull(c, "property is null");
		JSONArray r = (JSONArray) c;
		List<Couple<String, String>> res = new ArrayList<Couple<String, String>>(r.length());

		for (int i = 0; i < r.length(); i++) {
			JSONObject jo = r.getJSONObject(i);
			String key = jo.getString("name");
			String value = jo.getString("value");
			if (resolveParams) {
				key = resolveAll(key, this);
				value = resolveAll(value, this);
				res.add(new Couple<String, String>(key, value));
			}
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
	protected String getProp(String propName, JSONObject conf, boolean optional, boolean resolveParams) {
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

	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd = super.toSpagoBiDataSet();
		sbd.setType(DATASET_TYPE);
		return sbd;
	}

	public boolean isNgsi() {
		return ngsi;
	}

	public boolean isRealtimeNgsiConsumer() {
		return realtimeNgsiConsumer;
	}

	public void setRealtimeNgsiConsumer(boolean realtimeNgsiConsumer) {
		this.realtimeNgsiConsumer = realtimeNgsiConsumer;
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

	@Override
	public boolean isRealtime() {
		return isNgsi();
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String resolveParameters(String statement, IDataSet targetDataSet) {

		String newStatement = statement;

		logger.debug("Dataset paramMap [" + targetDataSet.getParamsMap() + "]");

		if (targetDataSet.getParamsMap() != null) {
			logger.debug("Dataset paramMap contains [" + targetDataSet.getParamsMap().size() + "] parameters");

			// if a parameter has value '' put null!
			Map parameterValues = targetDataSet.getParamsMap();
			Vector<String> parsToChange = new Vector<String>();

			for (Iterator iterator = parameterValues.keySet().iterator(); iterator.hasNext();) {
				String parName = (String) iterator.next();
				Object val = parameterValues.get(parName);
				if (val != null && val.equals("")) {
					val = null;
					parsToChange.add(parName);
				}
			}
			for (Iterator iterator = parsToChange.iterator(); iterator.hasNext();) {
				String parName = (String) iterator.next();
				parameterValues.remove(parName);
				parameterValues.put(parName, null);
			}

			try {
				Map parTypeMap = getParTypeMap(targetDataSet);
				newStatement = substituteDatasetParametersInString(newStatement, targetDataSet, parTypeMap, false);
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("An error occurred while settin up parameters", e);
			}
		}

		// after having substituted all parameters check there are not other
		// parameters unfilled otherwise throw an exception;
		List<String> parsUnfilled = checkParametersUnfilled(newStatement);
		if (parsUnfilled != null) {
			// means there are parameters not valorized, throw exception
			logger.error("there are parameters without values");
			String pars = "";
			for (Iterator iterator = parsUnfilled.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				pars += string;
				if (iterator.hasNext()) {
					pars += ", ";
				}
			}
			pars += " have no value specified";
			throw new ParametersNotValorizedException("The folowing parameters have no value [" + pars + "]");

		}

		return newStatement;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List checkParametersUnfilled(String statement) {
		List toReturn = null;
		int index = statement.indexOf("$P{");
		while (index != -1) {
			int endIndex = statement.indexOf('}', index);
			if (endIndex != -1) {
				String nameAttr = statement.substring(index, endIndex + 1);
				if (toReturn == null) {
					toReturn = new ArrayList<String>();
				}
				toReturn.add(nameAttr);
				index = statement.indexOf("$P{", endIndex);
			}
		}
		return toReturn;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map getParTypeMap(IDataSet dataSet) throws SourceBeanException {

		Map parTypeMap;
		String parametersXML;
		List parameters;

		logger.debug("IN");

		try {
			parTypeMap = new HashMap();
			parametersXML = dataSet.getParameters();

			logger.debug("Dataset parameters string is equals to [" + parametersXML + "]");

			if (!StringUtilities.isEmpty(parametersXML)) {
				parameters = DataSetParametersList.fromXML(parametersXML).getItems();
				logger.debug("Dataset have  [" + parameters.size() + "] parameters");

				for (int i = 0; i < parameters.size(); i++) {
					DataSetParameterItem dsDet = (DataSetParameterItem) parameters.get(i);
					String name = dsDet.getName();
					String type = dsDet.getType();
					logger.debug("Paremeter [" + (i + 1) + "] name is equals to  [" + name + "]");
					logger.debug("Paremeter [" + (i + 1) + "] type is equals to  [" + type + "]");
					parTypeMap.put(name, type);
				}
			}

		} finally {
			logger.debug("OUT");
		}

		return parTypeMap;
	}

	/**
	 * Substitutes parameters with sintax "$P{parameter_name}" whose value is set in the map. This is only for dataset, had to duplicate to handle null values,
	 * in case ogf null does not throw an exception but substitute null!
	 *
	 * @param statement          The string to be modified (tipically a query)
	 * @param valuesMap          Map name-value
	 * @param surroundWithQuotes flag: if true, the replacement will be surrounded by quotes if they are missing
	 *
	 * @return The statement with profile attributes replaced by their values.
	 *
	 * @throws Exception the exception
	 */
	public static String substituteDatasetParametersInString(String statement, IDataSet dataset, Map parType, boolean surroundWithQuotes) throws Exception {
		logger.debug("IN");

		boolean changePars = true;
		while (changePars) {
			// int profileAttributeStartIndex = statement.indexOf("$P{");
			int profileAttributeStartIndex = statement.indexOf("$P{");
			if (profileAttributeStartIndex != -1)
				statement = substituteDatasetParametersInString(statement, dataset, parType, profileAttributeStartIndex, surroundWithQuotes);
			else
				changePars = false;

		}
		logger.debug("OUT");
		return statement;
	}

	/**
	 * Substitutes the parameters with sintax "$P{attribute_name}" with the correspondent value in the string passed at input. Only for dataset parameters, had
	 * to duplicate to handle null values, not throw an exception but put null!
	 *
	 * @param statement                  The string to be modified (tipically a query)
	 * @param userProfile                The IEngUserProfile object
	 * @param profileAttributeStartIndex The start index for query parsing (useful for recursive calling)
	 * @param surroundWithQuotes         Flag: if true, the replacement will be surrounded by quotes if they are missing
	 *
	 * @return The statement with parameters replaced by their values.
	 * @throws Exception
	 */
	private static String substituteDatasetParametersInString(String statement, IDataSet dataset, Map parTypeMap, int profileAttributeStartIndex,
			boolean surroundWithQuotes) throws Exception {
		logger.debug("IN");
		Map valuesMap = dataset.getParamsMap();
		int profileAttributeEndIndex = statement.indexOf("}", profileAttributeStartIndex);
		if (profileAttributeEndIndex == -1)
			throw new Exception("Not closed profile attribute: '}' expected.");
		if (profileAttributeEndIndex < profileAttributeEndIndex)
			throw new Exception("Not opened profile attribute: '$P{' expected.");
		String attribute = statement.substring(profileAttributeStartIndex + 3, profileAttributeEndIndex).trim();

		String dequotePrefix = "_dequoted";
		if (attribute.endsWith(dequotePrefix)) {
			surroundWithQuotes = false;
		}

		int startConfigIndex = attribute.indexOf("(");
		String attributeName = "";
		String prefix = "";
		String split = "";
		String suffix = "";
		boolean attributeExcpetedToBeMultiValue = false;

		if (startConfigIndex != -1) {
			// the parameter is expected to be multivalue
			attributeExcpetedToBeMultiValue = true;
			int endConfigIndex = attribute.length() - 1;
			if (attribute.charAt(endConfigIndex) != ')')
				throw new Exception("Sintax error: \")\" missing. The expected sintax for " + "parameter is  $P{parameters} for singlevalue parameters. ");
			String configuration = attribute.substring(startConfigIndex + 1, endConfigIndex);
			// check the configuration content and add empty prefix/suffix as default if they are null
			if (configuration.equals(";,;"))
				configuration = " ;,; ";
			String[] configSplitted = configuration.split(";");
			if (configSplitted == null || configSplitted.length != 3)
				throw new Exception("Sintax error. The expected sintax for parameters"
						+ "or $P{parameter} for singlevalue parameter. 'parameterName' must not contain '(' characters. "
						+ "The (prefix;split;suffix) is not properly configured");
			prefix = configSplitted[0];
			split = configSplitted[1];
			suffix = configSplitted[2];
			logger.debug("Multi-value parameter configuration found: prefix: '" + prefix + "'; split: '" + split + "'; suffix: '" + suffix + "'.");
			attributeName = attribute.substring(0, startConfigIndex);
			logger.debug("Expected multi-value parameter name: '" + attributeName + "'");
		} else {
			attributeName = attribute;
			logger.debug("Expected single-value parameter name: '" + attributeName + "'");
		}

		String value = (String) valuesMap.get(attributeName);
		boolean isNullValue = false;
		if (value == null) {
			isNullValue = true;
			value = "null";
		}

		if (value.startsWith("' {"))
			value = value.substring(1);
		if (value.endsWith("}'"))
			value = value.substring(0, value.indexOf("}'") + 1);
		value = value.trim();
		logger.debug("Parameter value found: " + value);
		String replacement = null;
		String newListOfValues = null;

		// if is specified a particular type for the parameter can add '' in case of String or Date
		String parType = null;
		if (parTypeMap != null) {
			parType = (String) parTypeMap.get(attributeName);
		}
		if (parType == null)
			parType = new String("");

		if (attributeExcpetedToBeMultiValue) {
			if (value.startsWith("{")) {
				// the parameter is multi-value
				String[] values = findAttributeValues(value);
				logger.debug("N. " + values.length + " parameter values found: '" + values + "'");
				// newListOfValues = values[0];
				newListOfValues = ((values[0].startsWith(prefix))) ? "" : prefix + values[0] + ((values[0].endsWith(suffix)) ? "" : suffix);
				for (int i = 1; i < values.length; i++) {
					// newListOfValues = newListOfValues + split + values[i];
					String singleValue = ((values[i].startsWith(prefix))) ? "" : prefix + values[i] + ((values[i].endsWith(suffix)) ? "" : suffix);
					singleValue = checkParType(singleValue, parType, attribute);
					newListOfValues = newListOfValues + split + singleValue;
				}
			} else {
				logger.warn("The attribute value has not the sintax of a multi value parameter; considering it as a single value.");
				newListOfValues = value;
			}

		} else {
			if (value.startsWith("{")) {
				// the profile attribute is multi-value
				logger.warn(
						"The attribute value seems to be a multi value parameter; trying considering it as a multi value using its own splitter and no prefix and suffix.");
				try {
					// checks the sintax
					String[] values = findAttributeValues(value);
					newListOfValues = values[0];
					for (int i = 1; i < values.length; i++) {
						newListOfValues = newListOfValues + value.charAt(1) + values[i];
					}
				} catch (Exception e) {
					logger.error("The attribute value does not respect the sintax of a multi value attribute; considering it as a single value.", e);
					newListOfValues = value;
				}
			} else {
				newListOfValues = value;
			}
		}
		String nullValueString = null;
		if (newListOfValues.equals("") || newListOfValues.equals("''") || newListOfValues.equals("null")) {
			try {
				nullValueString = SingletonConfig.getInstance().getConfigValue("DATA_SET_NULL_VALUE");
				if (nullValueString != null) {
					newListOfValues = "'" + nullValueString + "'";
				}
			} catch (Throwable e) {
				// try to read engine_config settings
				if ((SourceBean) EnginConf.getInstance().getConfig().getAttribute("DATA_SET_NULL_VALUE") != null) {
					nullValueString = ((SourceBean) EnginConf.getInstance().getConfig().getAttribute("DATA_SET_NULL_VALUE")).getCharacters();
				}
				if (nullValueString != null) {
					newListOfValues = "'" + nullValueString + "'";

				}
			}

		}
		replacement = ((newListOfValues.startsWith(prefix)) ? "" : prefix) + newListOfValues + ((newListOfValues.endsWith(suffix)) ? "" : suffix);

		if (!attributeExcpetedToBeMultiValue)
			replacement = checkParType(replacement, parType, attribute);

		if (surroundWithQuotes || parType.equalsIgnoreCase("DATE")) {
			if (!isNullValue) {
				if (!replacement.startsWith("'"))
					replacement = "'" + replacement;
				if (!replacement.endsWith("'"))
					replacement = replacement + "'";
			}
		}

		attribute = quote(attribute);
		statement = statement.replaceAll("\\$P\\{" + attribute + "\\}", replaceSpecials(replacement));

		/*
		 * Workaround in case of multivalue parameters with spaces TODO: See if there is the possibility to add a solution before this place
		 */
		if (statement != null && statement.startsWith("\"") && dataset instanceof SolrDataSet) {

			if (statement.contains(" OR ")) {
				statement = statement.replaceAll("\"", "");
				statement = statement.replaceAll("'", "\"");
			} else if (statement.equals("\"*\"")) {
				statement = statement.replaceAll("\"", "");
			} else if (statement.equals("\"'%'\"")) {
				statement = statement.replaceAll("\"", "");
				statement = statement.replaceAll("'", "");
				statement = statement.replace("%", "*");
			} else if (statement.contains(" , ")) {
				if (!statement.isEmpty() && statement.startsWith("\"") && statement.endsWith("\"")) {
					statement = statement.substring(1, statement.length() - 1);
				}
			}
		}

		/*
		 * profileAttributeStartIndex = statement.indexOf("$P{", profileAttributeEndIndex-1); if (profileAttributeStartIndex != -1) statement =
		 * substituteParametersInString(statement, valuesMap, profileAttributeStartIndex);
		 */
		logger.debug("OUT");

		return statement;

	}

	/**
	 * Find the attribute values in case of multi value attribute. The sintax is: {splitter character{list of values separated by the splitter}}. Examples:
	 * {;{value1;value2;value3....}} {|{value1|value2|value3....}}
	 *
	 * @param attributeValue The String representing the list of attribute values
	 * @return The array of attribute values
	 * @throws Exception in case of sintax error
	 */
	public static String[] findAttributeValues(String attributeValue) throws Exception {
		logger.debug("IN");
		String sintaxErrorMsg = "Multi value attribute sintax error.";
		// Clean specification of type (STRING, NUM..) from values (if exists!!)
		int lastBrace = attributeValue.lastIndexOf("}");
		int previousLastBrace = attributeValue.indexOf("}");
		String type = attributeValue.substring(previousLastBrace + 1, lastBrace);
		if (type.length() > 0) {
			attributeValue = attributeValue.substring(0, previousLastBrace + 1) + "}";
		}
		if (attributeValue.length() < 6)
			throw new Exception(sintaxErrorMsg);
		if (!attributeValue.endsWith("}}"))
			throw new Exception(sintaxErrorMsg);
		if (attributeValue.charAt(2) != '{')
			throw new Exception(sintaxErrorMsg);
		char splitter = attributeValue.charAt(1);
		String valuesList = attributeValue.substring(3, attributeValue.length() - 2);
		String[] values = valuesList.split(String.valueOf(splitter));
		logger.debug("OUT");
		return values;
	}

	/*
	 * This method exists since jdk 1.5 (java.util.regexp.Patter.quote())
	 */
	/**
	 * Quote.
	 *
	 * @param s the s
	 *
	 * @return the string
	 */
	public static String quote(String s) {
		logger.debug("IN");
		int slashEIndex = s.indexOf("\\E");
		if (slashEIndex == -1)
			return "\\Q" + s + "\\E";

		StringBuffer sb = new StringBuffer(s.length() * 2);
		sb.append("\\Q");
		slashEIndex = 0;
		int current = 0;
		while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
			sb.append(s.substring(current, slashEIndex));
			current = slashEIndex + 2;
			sb.append("\\E\\\\E\\Q");
		}
		sb.append(s.substring(current, s.length()));
		sb.append("\\E");
		logger.debug("OUT");
		return sb.toString();
	}

	/**
	 * Check the correct validity of the parameter value
	 *
	 * @param replacement : the parameter
	 * @param parType     : the parameter type
	 * @param attribute   : the attribute
	 * @return
	 */
	private static String checkParType(String replacement, String parType, String attribute) throws NumberFormatException {
		logger.debug("IN");
		String toReturn = replacement;
		// check if numbers are number otherwise throw exception
		try {
			if (parType.equalsIgnoreCase("NUMBER")) {
				toReturn = toReturn.replaceAll("'", "").replaceAll(";", ",");
				if (toReturn.indexOf(",") >= 0) {
					// multivalues management
					String[] values = toReturn.split(",");
					for (int i = 0; i < values.length; i++) {
						Double double1 = Double.valueOf(values[i]);
					}
				} else {
					Double double1 = Double.valueOf(toReturn);
				}
			}
		} catch (NumberFormatException e) {
			String me = e.getMessage();
			me += " - attribute " + attribute + " should be of number type";
			NumberFormatException numberFormatException = new NumberFormatException(attribute);
			numberFormatException.setStackTrace(e.getStackTrace());
			throw numberFormatException;
		}

		// check when type is RAW that there are not '' surrounding values (in case remove them)
		// remotion done here in order to not modify SpagoBI Analytical driver of type string handling
		try {
			if (parType.equalsIgnoreCase("RAW")) {
				logger.debug("Parmaeter is Raw type, check if there are '' and remove them");
				if (toReturn.length() > 2) {
					if (toReturn.startsWith("'")) {
						logger.debug("first character is ', remove");
						toReturn = toReturn.substring(1);
					}
					if (toReturn.endsWith("'")) {
						logger.debug("last character is ', remove");
						toReturn = toReturn.substring(0, replacement.length() - 1);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in removing the '' in value " + toReturn + " do not substitute them");
		}

		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Parse special characters with String replacement
	 */
	public static String replaceSpecials(String replacement) {

		return Matcher.quoteReplacement(replacement);
	}

	/**
	 * Resolve profile attributes and parameters
	 *
	 * @param statement
	 * @param dataSet
	 * @return
	 */
	public String resolveAll(String statement, IDataSet dataSet) {
		String res = parametersResolver.resolveProfileAttributes(statement, dataSet);
		res = resolveParameters(res, dataSet);
		return res;
	}
}
