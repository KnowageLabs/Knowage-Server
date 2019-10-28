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
package it.eng.spagobi.tools.dataset.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONObjectDeserializator;

import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.tools.dataset.bo.DataSetParameterItem;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.AbstractDataBase;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

/**
 * TODO : move it in it.eng.spagobi.tools.dataset and rename to DataSetProfiler (or similar)
 */
public class DataSetUtilities {

	public static final String STRING_TYPE = "string";
	public static final String NUMBER_TYPE = "number";
	public static final String RAW_TYPE = "raw";
	public static final String GENERIC_TYPE = "generic";

	/**
	 * It's possible to disable the default params behavior
	 */
	private static boolean disableDefaultParams;

	private final static Logger logger = Logger.getLogger(DataSetUtilities.class);

	/**
	 * Check if the dataset is executable by the user
	 *
	 * @param dataset
	 * @param owner
	 * @param isAdminUser
	 * @return
	 */
	public static boolean isExecutableByUser(IDataSet dataset, IEngUserProfile profile) {
		if (profile == null) {
			return false;
		}
		boolean isAdminUser = isAdministrator(profile);
		if (isAdminUser) {
			return true;
		}
		String owner = ((UserProfile) profile).getUserId().toString();
		return (owner != null && owner.equals(dataset.getOwner()));
	}

	public static boolean isExecutableByUser(String ownerDataSet, IEngUserProfile profile) {
		if (profile == null) {
			return false;
		}
		boolean isAdminUser = isAdministrator(profile);
		if (isAdminUser) {
			return true;
		}
		String owner = ((UserProfile) profile).getUserId().toString();
		return (owner != null && owner.equals(ownerDataSet));
	}

	public static boolean isAdministrator(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		try {
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	/**
	 * Retrieve the params default values from dataSet
	 *
	 * @param dataSet
	 * @return null if some errors occur (no exceptions thrown)
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, ParamDefaultValue> getParamsDefaultValues(IDataSet dataSet) {
		Helper.checkNotNull(dataSet, "dataSet");

		// Retrieve the default values directly from dataSet
		try {
			String params = dataSet.getParameters();
			if (params == null || params.isEmpty()) {
				return null;
			}

			Map<String, ParamDefaultValue> res = new HashMap<String, ParamDefaultValue>();
			DataSetParametersList dspl = DataSetParametersList.fromXML(params);
			for (DataSetParameterItem item : (List<DataSetParameterItem>) dspl.getItems()) {
				String defaultValue = item.getDefaultValue();
				if (defaultValue == null || defaultValue.isEmpty()) {
					continue;
				}
				String name = item.getName();
				String type = item.getType();
				res.put(name, new ParamDefaultValue(name, type, defaultValue));
			}
			return res;
		} catch (Exception e) {
			logger.warn(
					"Default parameters values can't be retrieved from dataSet. I try from dataSet persistence. Empty defaults values map will be returned.",
					e);
		}
		return null;
	}

	/**
	 * Return the default values from dataSet. If they are not present then retrieve them from {@link DataSetServiceProxy}.
	 *
	 * @return null if some errors occur (no exceptions thrown)
	 */
	public static Map<String, ParamDefaultValue> getParamsDefaultValuesUseAlsoService(IDataSet dataSet, String user, HttpSession session) {
		Helper.checkNotNull(dataSet, "dataSet");
		Helper.checkNotNull(session, "session");
		Helper.checkNotNullNotTrimNotEmpty(user, "user");

		Map<String, ParamDefaultValue> res = getParamsDefaultValues(dataSet);
		if (res != null) {
			return res;
		}
		// res=null, load dataset from service
		logger.warn("No params default values found on dataSet. I try from service.");
		try {
			String label = dataSet.getLabel();
			if (label == null) {
				logger.warn("Label not found -> no default values from service");
				return null;
			}
			DataSetServiceProxy proxy = new DataSetServiceProxy(user, session);
			IDataSet ds = proxy.getDataSetByLabel(label);
			if (ds == null) {
				logger.warn("Dataset not found -> no default values from service");
				return null;
			}

			res = getParamsDefaultValues(ds);
		} catch (Exception e) {
			logger.warn("Default parameters values can't be retrieved from dataSet service.", e);
		}
		return res;
	}

	/**
	 * Fill he parameters map with the default values if and only if they are not already present in the map.
	 *
	 * @param dataSet
	 *            can't be null
	 * @param parameters
	 *            can be null
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillDefaultValues(IDataSet dataSet, Map parameters) {
		if (disableDefaultParams) {
			return;
		}
		Helper.checkNotNull(dataSet, "dataset");
		if (parameters == null) {
			return;
		}

		// parameters != null
		int oldSize = parameters.size();
		Map<String, ParamDefaultValue> defaultValues = getParamsDefaultValues(dataSet);
		if (defaultValues == null) {
			// no changes
			return;
		}

		// default values != null
		for (String key : defaultValues.keySet()) {
			ParamDefaultValue paramDefaultValue = defaultValues.get(key);
			String defaultValue = paramDefaultValue.getDefaultValue();
			if (parameters.containsKey(key)) {
				Object oldValueObject = parameters.get(key);
				Assert.assertTrue(oldValueObject == null || oldValueObject instanceof String, "parameter is not a instance of string");
				String oldValue = (String) oldValueObject;
				if (isEmptyValue(oldValue, paramDefaultValue)) {
					parameters.put(key, getDefaultValueByType(defaultValue, paramDefaultValue));
				}
			} else {
				// parameters dosn't contain the default value
				parameters.put(key, getDefaultValueByType(defaultValue, paramDefaultValue));
			}
		}

		Assert.assertTrue(oldSize <= parameters.size(), "parameters can't be removed");
	}

	/**
	 * Return the default value by the type of param
	 *
	 * @param defaultValue
	 * @param paramDefaultValue
	 * @return
	 */
	private static String getDefaultValueByType(String defaultValue, ParamDefaultValue paramDefaultValue) {
		if (!STRING_TYPE.equals(paramDefaultValue.getType())) {
			return defaultValue;
		}
		return "'" + defaultValue + "'";
	}

	/**
	 * Check if it's a empty value already filled by the type (see ManageDataSets from knowage project)
	 *
	 * @param oldValue
	 * @param paramDefaultValue
	 * @return
	 */
	private static boolean isEmptyValue(String oldValue, ParamDefaultValue paramDefaultValue) {
		if (oldValue == null || oldValue.isEmpty()) {
			return true;
		}
		if (STRING_TYPE.equals(paramDefaultValue.getType())) {
			if (oldValue == "''") {
				return true;
			}
		}
		return false;
	}

	public static boolean isDisableDefaultParams() {
		return disableDefaultParams;
	}

	public static void setDisableDefaultParams(boolean disableDefaultParams) {
		DataSetUtilities.disableDefaultParams = disableDefaultParams;
	}

	public static Map<String, String> getParametersMap(String parameters) {
		Map<String, String> toReturn = null;

		if (parameters != null) {
			parameters = JSONUtils.escapeJsonString(parameters);
			JSONObject jsonParameters = ObjectUtils.toJSONObject(parameters);
			toReturn = getParametersMap(jsonParameters);
		} else {
			toReturn = new HashMap<String, String>();
		}
		return toReturn;
	}

	public static JSONObject parametersJSONArray2JSONObject(IDataSet dataSet, JSONArray parameters) throws Exception {
		String params = dataSet.getParameters();
		JSONObject jsonPar = new JSONObject();
		if (params != null && !params.equals("")) {
			SourceBean source = SourceBean.fromXMLString(params);
			if (source != null && source.getName().equals("PARAMETERSLIST")) {
				List<SourceBean> rows = source.getAttributeAsList("ROWS.ROW");

				for (int i = 0; i < rows.size(); i++) {
					SourceBean row = rows.get(i);
					String name = (String) row.getAttribute("NAME");
					String defaultValue = (String) row.getAttribute(DataSetParametersList.DEFAULT_VALUE_XML);

					jsonPar.put(name, defaultValue);
					if (parameters != null) {
						for (int j = 0; j < parameters.length(); j++) {
							JSONObject jsonParam = parameters.getJSONObject(j);
							if (name.equals(jsonParam.getString("name"))) {
								boolean jMultivalue = jsonParam.getBoolean("multiValue");
								if (jMultivalue) {
									Object opt = jsonParam.opt("value");
									if (opt == null) {
										logger.warn("Parameter [" + name
												+ "] is expected to be a JSON Array but its value is missing (please use an empty json array in case no values are set)");
										opt = new JSONArray();
									}
									// In AbstractDataSet opt will be distinguished between JSONArray and simple value
									jsonPar.put(name, opt);
								} else {
									jsonPar.put(name, jsonParam.optString("value"));
									break;
								}
							}
						}
					}
				}
			}
		}
		return jsonPar;
	}

	public static Map<String, String> getParametersMap(JSONObject jsonParameters) {
		Map<String, String> toReturn = new HashMap<String, String>();
		if (jsonParameters != null) {
			Iterator<String> keys = jsonParameters.keys();
			try {
				while (keys.hasNext()) {
					String key = keys.next();
					Object obj = jsonParameters.opt(key);
					String value = null;
					if (obj instanceof JSONArray) {
						value = ((JSONArray) obj).toString();
					} else {
						value = (obj.equals(JSONObject.NULL)) ? "" : jsonParameters.getString(key);
					}
					toReturn.put(key, value);
				}
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An unexpected exception occured while loading parameters [" + jsonParameters + "]", t);
			}
		}

		return toReturn;
	}

	public static Map<String, Object> getDriversMap(JSONObject driversJson) {

		Map<String, Object> ret = new HashMap<String, Object>();

		try {
			if (driversJson != null) {
				int length = driversJson.length();
				HashMap<String, Object> hashMapFromJSONObject = JSONObjectDeserializator.getHashMapFromJSONObject(driversJson);
				ret.putAll(hashMapFromJSONObject);
			}
		} catch (Exception e) {
			logger.error("Cannot read dataset drivers", e);
			throw new SpagoBIRuntimeException("Cannot read drivers", e);
		}

		return ret;
	}

	public static Object getValue(String value, Class type) {
		Object result = null;

		if (value != null && !value.equalsIgnoreCase("null")) {
			if (type.toString().toLowerCase().contains("timestamp")) {
				try {
					result = Timestamp.valueOf(value);
				} catch (IllegalArgumentException e) {
					try {
						Date date = new SimpleDateFormat(CockpitJSONDataWriter.TIMESTAMP_FORMAT).parse(value);
						String formattedValue = new SimpleDateFormat(CockpitJSONDataWriter.CACHE_TIMESTAMP_FORMAT).format(date);
						result = Timestamp.valueOf(formattedValue);
					} catch (ParseException | IllegalArgumentException ex) { // tries Solr date format
						try {
							DateTimeFormatter dateTime = ISODateTimeFormat.dateTimeNoMillis();
							DateTime parsedDateTime = dateTime.parseDateTime(value);
							Date dateToconvert = parsedDateTime.toDate();
							SimpleDateFormat sdf = new SimpleDateFormat(CockpitJSONDataWriter.CACHE_TIMESTAMP_FORMAT);
							String valuesToChange = sdf.format(dateToconvert);
							result = Timestamp.valueOf(valuesToChange);
						} catch (Exception ex2) {
							throw new SpagoBIRuntimeException(ex2);
						}
					}
				}
			} else if (Date.class.isAssignableFrom(type)) {
				try {
					result = new SimpleDateFormat(CockpitJSONDataWriter.DATE_FORMAT).parse(value);
				} catch (ParseException e) {
					try {
						result = new SimpleDateFormat(CockpitJSONDataWriter.CACHE_DATE_FORMAT).parse(value);
					} catch (ParseException pe) {
						throw new SpagoBIRuntimeException(pe);
					}
				}
			} else if (Boolean.class.isAssignableFrom(type)) {
				result = Boolean.valueOf(value);
			} else if (Byte.class.isAssignableFrom(type)) {
				result = Byte.valueOf(value.split("\\.")[0]);
			} else if (Short.class.isAssignableFrom(type)) {
				result = Short.valueOf(value.split("\\.")[0]);
			} else if (Integer.class.isAssignableFrom(type)) {
				result = Integer.valueOf(value.split("\\.")[0]);
			} else if (Long.class.isAssignableFrom(type)) {
				result = Long.valueOf(value.split("\\.")[0]);
			} else if (BigInteger.class.isAssignableFrom(type)) {
				result = new BigInteger(value.split("\\.")[0]);
			} else if (Float.class.isAssignableFrom(type)) {
				result = Float.valueOf(value);
			} else if (Double.class.isAssignableFrom(type)) {
				result = Double.valueOf(value);
			} else if (BigDecimal.class.isAssignableFrom(type)) {
				result = new BigDecimal(value);
			} else if (String.class.isAssignableFrom(type)) {
				result = value;
			}
		}

		return result;
	}

	public static IFieldMetaData getFieldMetaData(IDataSet dataSet, String columnName) {
		Assert.assertNotNull(dataSet, "Dataset can't be null");
		Assert.assertNotNull(columnName, "Column [" + columnName + "] can't be null");
		Assert.assertTrue(!columnName.isEmpty(), "Column [" + columnName + "] can't be empty");

		String fieldName;
		if (columnName.contains(AbstractDataBase.STANDARD_ALIAS_DELIMITER)) {
			String[] columnNames = StringUtilities.getSubstringsBetween(columnName, AbstractDataBase.STANDARD_ALIAS_DELIMITER);
			Assert.assertTrue(columnNames.length > 0, "Column [" + columnName + "] is not a valid calculated column");
			fieldName = columnNames[0];
		} else {
			fieldName = columnName;
		}

		IMetaData metadata = dataSet.getMetadata();
		int fieldIndex = metadata.getFieldIndex(fieldName);
		if (fieldIndex >= 0 && fieldIndex < metadata.getFieldCount()) {
			return metadata.getFieldMeta(fieldIndex);
		} else {
			for (int i = 0; i < metadata.getFieldCount(); i++) {
				IFieldMetaData fieldMeta = metadata.getFieldMeta(i);

				if (fieldMeta.getName().endsWith(columnName) || fieldMeta.getAlias().equals(columnName)) {
					return fieldMeta;
				}
			}
			throw new IllegalArgumentException("Column [" + columnName + "] not found");
		}
	}

	public static String getColumnNameWithoutQbePrefix(String columnName) {
		int indexOfQbeSeparator = columnName.indexOf(":");
		if (indexOfQbeSeparator != -1) {
			return columnName.substring(indexOfQbeSeparator + 1);
		} else {
			return columnName;
		}
	}
}
