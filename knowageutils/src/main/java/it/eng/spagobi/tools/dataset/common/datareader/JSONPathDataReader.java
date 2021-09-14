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
package it.eng.spagobi.tools.dataset.common.datareader;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.joda.time.Instant;
import org.json.JSONException;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.json.JSONUtils;

/**
 * This reader convert JSON string to an {@link IDataStore}. The JSON must contains the items to convert, they are found using {@link JsonPath}. The name of
 * each {@link IField} must be defined. The type can be fixed or can be defined dynamically by {@link JsonPath}. The value is found dynamically by
 * {@link JsonPath}. For an example of usage check the related Test class.
 *
 * @author fabrizio
 *
 */

public class JSONPathDataReader extends AbstractDataReader {

	private static final Class<String> ALL_OTHER_TYPES = String.class;

	private static final String DATE_FORMAT_FIELD_METADATA_PROPERTY = "dateFormat";

	private static final String JSON_PATH_VALUE_METADATA_PROPERTY = "jsonPathValue";

	private static final String JSON_PATH_TYPE_METADATA_PROPERTY = "jsonPathType";

	private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

	private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

	private static final String DEFAULT_TIMESTAMP_PATTERN_UNQUOTED = "yyyy-MM-ddTHH:mm:ss.SSSZ";

	private static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private static final String ATTRIBUTES_DIRECTLY = "attributesDirectly";

	private static final String ID_NAME = "id";

	private static final String ORION_JSON_PATH_ITEMS = "$";

	static private Logger logger = Logger.getLogger(JSONPathDataReader.class);

	public static class JSONPathAttribute {
		private final String name;
		private final String jsonPathValue;
		private final String jsonPathType;
		private final boolean multivalue;

		public static String getJsonPathTypeFromSolrFieldType(String solrFieldType) {
			String jsonPathType;
			if (solrFieldType.startsWith("boolean")) {
				jsonPathType = "boolean";
			} else if (solrFieldType.matches("^[pt]?date.*")) {
				jsonPathType = "iso8601";
			} else if (solrFieldType.matches("^[pt]?double.*")) {
				jsonPathType = "double";
			} else if (solrFieldType.matches("^[pt]?float.*")) {
				jsonPathType = "float";
			} else if (solrFieldType.matches("^[pt]?int.*")) {
				jsonPathType = "int";
			} else if (solrFieldType.matches("^[pt]?long.*")) {
				jsonPathType = "long";
			} else if (solrFieldType.equalsIgnoreCase("string")) {
				jsonPathType = "string";
			} else {
				jsonPathType = "text";
			}
			return jsonPathType;
		}

		public JSONPathAttribute(String name, String jsonPathValue, String jsonPathType) {
			this(name, jsonPathValue, jsonPathType, false);
		}

		public JSONPathAttribute(String name, String jsonPathValue, String jsonPathType, boolean multivalue) {
			this.name = name;
			this.jsonPathValue = jsonPathValue;
			this.jsonPathType = jsonPathType;
			this.multivalue = multivalue;
		}

		public boolean isMultivalue() {
			return multivalue;
		}

		public String getName() {
			return name;
		}

		public String getJsonPathValue() {
			return jsonPathValue;
		}

		public String getJsonPathType() {
			return jsonPathType;
		}

	}

	private final String jsonPathItems;
	private final List<JSONPathAttribute> jsonPathAttributes;
	private final boolean useDirectlyAttributes;

	private int idFieldIndex = -2; // not set

	private final boolean ngsi;

	private boolean dataReadFirstTime;

	private boolean ngsiDefaultItems;

	public JSONPathDataReader(String jsonPathItems, List<JSONPathAttribute> jsonPathAttributes, boolean useDirectlyAttributes, boolean ngsi) {
		this.jsonPathAttributes = jsonPathAttributes;
		this.useDirectlyAttributes = useDirectlyAttributes;
		this.ngsi = ngsi;

		if (ngsi && jsonPathItems == null) {
			this.jsonPathItems = ORION_JSON_PATH_ITEMS;
			this.ngsiDefaultItems = true;
		} else {
			Helper.checkNotNullNotTrimNotEmpty(jsonPathItems, "jsonPathItems");
			this.jsonPathItems = jsonPathItems;
		}
	}

	private static boolean isJSON(String responseBody) {
		try {
			JSONUtils.toJSONObject(responseBody);
			return true;
		} catch (Exception e) {
			logger.debug("Error parsing input String as JSONObject", e);
			return false;
		}
	}

	private static boolean isJSONArray(String responseBody) {
		try {
			JSONUtils.toJSONArray(responseBody);
			return true;
		} catch (Exception e) {
			logger.debug("Error parsing input String as JSONObject", e);
			return false;
		}
	}

	@Override
	public IDataStore read(Object data) {
		Helper.checkNotNull(data, "data");
		if (!(data instanceof String)) {
			throw new IllegalArgumentException("data must be a string");
		}

		String d = (String) data;
		if (!isJSONArray(d) && !isJSON(d)) {
			throw new JSONPathDataReaderException("Data is neither a JSON object nor a JSON array");
		}

		try {
			DataStore dataStore = new DataStore();
			MetaData dataStoreMeta = new MetaData();
			dataStore.setMetaData(dataStoreMeta);
			List<Object> parsedData = getItems(d);
			// List parsedData = getItems(d);
			addFieldMetadata(dataStoreMeta, parsedData);
			addData(d, dataStore, dataStoreMeta, parsedData, false);
			return dataStore;
		} catch (ParseException e) {
			throw new JSONPathDataReaderException(e);
		} catch (JSONPathDataReaderException e) {
			throw e;
		} catch (Exception e) {
			throw new JSONPathDataReaderException(e);
		}
	}

	protected void addData(String data, IDataStore dataStore, IMetaData dataStoreMeta, List<Object> parsedData, boolean skipPagination)
			throws ParseException, JSONException {

		boolean checkMaxResults = false;
		if ((maxResults > 0)) {
			checkMaxResults = true;
		}

		boolean paginated = false;
		logger.debug("Reading data ...");
		if ((isPaginationSupported() && getOffset() >= 0 && getFetchSize() >= 0)) {
			logger.debug("Offset is equal to [" + getOffset() + "] and fetchSize is equal to [" + getFetchSize() + "]");
			paginated = true;
		} else {
			logger.debug("Offset and fetch size not set");
		}

		int rowFetched = 0;
		boolean wasJson = false;

		if (parsedData instanceof net.minidev.json.JSONObject || parsedData instanceof net.minidev.json.JSONArray)
			wasJson = true;
		// for (Object o : parsedData) {
		for (int i = 0; i < parsedData.size(); i++) {
			Object o;
			if (wasJson) {
				o = getJSONFormat(parsedData.get(i));
			} else {
				o = parsedData.get(i);
			}

			if (skipPagination || (!paginated && (!checkMaxResults || (rowFetched < maxResults)))
					|| ((paginated && (rowFetched >= offset) && (rowFetched - offset < fetchSize))
							&& (!checkMaxResults || (rowFetched - offset < maxResults)))) {

				IRecord record = new Record(dataStore);

				for (int j = 0; j < dataStoreMeta.getFieldCount(); j++) {
					IFieldMetaData fieldMeta = dataStoreMeta.getFieldMeta(j);
					Object propAttr = fieldMeta.getProperty(ATTRIBUTES_DIRECTLY);
					if (propAttr != null && (Boolean) propAttr) {
						// managed after this process
						continue;
					}
					String jsonPathValue = (String) fieldMeta.getProperty(JSON_PATH_VALUE_METADATA_PROPERTY);
					Assert.assertNotNull(jsonPathValue != null, "jsonPathValue!=null");
					// can be fixed (not real JSONPath) or null (after value calculation)
					Object value = isRealJsonPath(jsonPathValue) ? getJSONPathValue(o, jsonPathValue) : jsonPathValue;
					Class<?> type = fieldMeta.getType();
					if (type == null) {
						// dinamically defined, from json data path
						String typeString = (String) getJSONPathValue(o, (String) fieldMeta.getProperty(JSON_PATH_TYPE_METADATA_PROPERTY));
						Assert.assertNotNull(typeString, "type of jsonpath type");
						type = getType(typeString);
						fieldMeta.setType(type);
						if (type.equals(Date.class) || type.equals(Timestamp.class)) {
							setDateTypeFormat(fieldMeta, typeString);
						}
					}
					Assert.assertNotNull(type != null, "type!=null");

					try {
						IField field = new Field(getValue(value, fieldMeta));
						record.appendField(field);
					} catch (Exception e) {
						String msg = String.format("Error getting value for field %s", fieldMeta.getName());
						throw new IllegalStateException(msg, e);
					}
				}
				if (useDirectlyAttributes) {
					manageDirectlyAttributes(o, record, dataStoreMeta, dataStore);
				}

				dataStore.appendRecord(record);
			}
			rowFetched++;
		}
		logger.debug("Read [" + rowFetched + "] records");
		logger.debug("Insert [" + dataStore.getRecordsCount() + "] records");

		if (this.isCalculateResultNumberEnabled())

		{
			logger.debug("Calculation of result set number is enabled");
			dataStore.getMetaData().setProperty("resultNumber", new Integer(rowFetched));
		} else {
			logger.debug("Calculation of result set number is NOT enabled");
		}
	}

	@SuppressWarnings("unchecked")
	protected List<Object> getItems(String data) {
		Object parsed = JsonPath.read(data, jsonPathItems);
		if (parsed == null) {
			throw new JSONPathDataReaderException(String.format("Items not found in %s with json path %s", data, jsonPathItems));
		}

		// can be an array or a single object
		List<Object> parsedData;
		if (parsed instanceof List) {
			parsedData = (List<Object>) parsed;
		} else {
			parsedData = Arrays.asList(parsed);
		}

		return parsedData;
	}

	private static void manageDirectlyAttributes(Object data, IRecord rec, IMetaData dsm, IDataStore dataStore) {
		Assert.assertTrue(data instanceof Map, "data instanceof Map");
		Map jsonObject = (Map) data;

		for (int j = 0; j < dsm.getFieldCount(); j++) {
			IFieldMetaData fieldMeta = dsm.getFieldMeta(j);
			Object propAttr = fieldMeta.getProperty(ATTRIBUTES_DIRECTLY);
			if (propAttr == null || !(Boolean) propAttr) {
				// managed before
				continue;
			}

			String name = fieldMeta.getName();
			if (jsonObject.containsKey(name)) {
				Object value = jsonObject.get(name);
				value = normalizeTimestamp(value);
				value = normalizeNumber(value);
				rec.appendField(new Field(value));
			} else {
				// add null value
				rec.appendField(new Field(null));
			}
		}

		// find new elements
		for (String key : new TreeSet<String>(jsonObject.keySet())) {
			int index = dsm.getFieldIndex(key);
			if (index != -1) {
				// already present,manged before
				continue;
			}

			// not found
			Object value = jsonObject.get(key);
			value = normalizeTimestamp(value);
			value = normalizeNumber(value);
			rec.appendField(new Field(value));

			// value can be null from json object
			Class<? extends Object> type = value == null ? ALL_OTHER_TYPES : value.getClass();
			if (Number.class.isAssignableFrom(type)) {
				// use always double for numbers, just to prevent problems
				// if it's represented as an integer without trailing 0
				type = Double.class;
			}
			FieldMetadata fm = new FieldMetadata(key, type);
			fm.setProperty(ATTRIBUTES_DIRECTLY, true);
			dsm.addFiedMeta(fm);

			// add null to previous records
			// current record not added
			for (int i = 0; i < dataStore.getRecordsCount(); i++) {
				IRecord previousRecord = dataStore.getRecordAt(i);
				Assert.assertTrue(previousRecord != rec, "previousRecord!=rec");
				previousRecord.appendField(new Field(null));
			}
		}
	}

	private static Object normalizeNumber(Object value) {
		if (value == null) {
			return value;
		}
		if (Number.class.isAssignableFrom(value.getClass())) {
			// use always double for numbers, just to prevent problems
			// if it's represented as an integer without trailing 0
			value = ((Number) value).doubleValue();
		}
		return value;
	}

	private static Object normalizeTimestamp(Object value) {
		if (value == null) {
			return value;
		}
		try {
			Timestamp ts = Timestamp.valueOf(value.toString());
			return ts;
		} catch (Exception e) {
			return value;
		}
	}

	protected Object getJSONPathValue(Object o, String jsonPathValue) throws JSONException {
		// can be an array with a single value, a single object or also null (not found)
		Object res = null;
		try {
			res = JsonPath.read(o, jsonPathValue);
		} catch (PathNotFoundException e) {
			logger.debug("JPath not found " + jsonPathValue);
		}

//		if (res == null) {
//			return null;
//		}
//
//		if (o instanceof net.minidev.json.JSONObject || o instanceof net.minidev.json.JSONArray) {
//			res = getJSONFormat(res);
//			return res.toString();
//		}
//
//		if (res instanceof JSONArray) {
//			JSONArray array = (JSONArray) res;
//			if (array.length() > 1) {
//				throw new IllegalArgumentException(String.format("There is no unique value: %s", array.toString()));
//			}
//			if (array.length() == 0) {
//				return null;
//			}
//
//			res = array.get(0);
//		}
//
//		return res.toString();
		return res;
	}

	private static Object getJSONFormat(Object res) {
		// reconvert in standard json object / json array
		if (res instanceof Map) {
			net.minidev.json.JSONObject jsonObject = new net.minidev.json.JSONObject();
			jsonObject.putAll((Map) res);
			res = jsonObject;
		} else if (res instanceof List) {
			net.minidev.json.JSONArray jsonArray = new net.minidev.json.JSONArray();
			jsonArray.addAll((List) res);
			res = jsonArray;
		}
		return res;
	}

	public int getIdFieldIndex() {
		if (idFieldIndex == -2) { // not set

			if (ngsiDefaultItems && !dataReadFirstTime) {
				throw new IllegalStateException("NGSI Rest Data Reader needs a first read of data");
			}

			idFieldIndex = -1;
			for (int i = 0; i < jsonPathAttributes.size(); i++) {
				if (ID_NAME.equalsIgnoreCase(jsonPathAttributes.get(i).name)) {
					idFieldIndex = i;
					break;
				}
			}
		}
		return idFieldIndex;
	}

	/**
	 *
	 * @param dataStoreMeta
	 * @param parsedData    list of json object (net.minidev)
	 */
	protected void addFieldMetadata(IMetaData dataStoreMeta, List<Object> parsedData) {
		boolean idSet = false;

		if (ngsiDefaultItems) {
			manageNGSI(parsedData);
		}

		for (int index = 0; index < jsonPathAttributes.size(); index++) {
			JSONPathAttribute jpa = jsonPathAttributes.get(index);
			FieldMetadata fm = new FieldMetadata();
			String header = jpa.name;
			boolean multiValue = jpa.multivalue;
			fm.setAlias(getAlias(header));
			fm.setName(getName(header));
			fm.setMultiValue(multiValue);
			if (ID_NAME.equalsIgnoreCase(header)) {
				if (idSet) {
					throw new JSONPathDataReaderException("There is no unique id field.");
				}
				idSet = true;
				dataStoreMeta.setIdField(index);
			}
			fm.setProperty(JSON_PATH_VALUE_METADATA_PROPERTY, jpa.jsonPathValue);
			fm.setProperty(JSON_PATH_TYPE_METADATA_PROPERTY, jpa.jsonPathType);
			if (isRealJsonPath(jpa.jsonPathType)) {
				// dinamically defined
				// type == null, defined later
			} else {
				Class<?> type = getType(jpa.jsonPathType);
				Assert.assertNotNull(type, "type");
				// type statically defined
				fm.setType(type);
				if (type.equals(Date.class) || type.equals(Timestamp.class)) {
					setDateTypeFormat(fm, jpa.jsonPathType);
				}
			}
			dataStoreMeta.addFiedMeta(fm);
		}
	}

	protected String getAlias(String name) {
		return name;
	}

	protected String getName(String name) {
		return name;
	}

	private void manageNGSI(List<Object> parsedData) {
		if (ngsiDefaultItems && !dataReadFirstTime) {
			List<JSONPathAttribute> ngsiAttributes = getNGSIAttributes(parsedData);
			updateAttributes(ngsiAttributes);
		}
		dataReadFirstTime = true;
	}

	private void updateAttributes(List<JSONPathAttribute> ngsiAttributes) {
		Map<String, JSONPathAttribute> localByName = new HashMap<String, JSONPathDataReader.JSONPathAttribute>(jsonPathAttributes.size());
		for (JSONPathAttribute jpa : jsonPathAttributes) {
			localByName.put(jpa.name, jpa);
		}

		for (JSONPathAttribute na : ngsiAttributes) {
			if (!localByName.containsKey(na.name)) {
				jsonPathAttributes.add(na);

				// just to assert to not insert duplicates
				localByName.put(na.name, na);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<JSONPathAttribute> getNGSIAttributes(List<Object> parsedData) {
		Assert.assertTrue(ngsiDefaultItems, "must be ngsi default items");

		List<JSONPathAttribute> res = new ArrayList<JSONPathAttribute>();
		if (parsedData.isEmpty()) {
			return res;
		}

		Map<String, Object> element = (Map<String, Object>) parsedData.get(0);
		res.add(new JSONPathAttribute("id", "$.id", "string")); // id of element
		res.add(new JSONPathAttribute("type", "$.type", "string")); // type of element
		for (Map.Entry<String, Object> entry : element.entrySet()) {
			String name = entry.getKey();
			if (!name.equalsIgnoreCase("id") && !name.equalsIgnoreCase("type")) {
				String jsonPathValue = "$." + name + ".value";
				String jsonPathType = "$." + name + ".type";
				res.add(new JSONPathAttribute(name, jsonPathValue, jsonPathType));
			}
		}

		return res;
	}

	private void setDateTypeFormat(IFieldMetaData fm, String jsonPathType) {
		String dateFormat = getDateFormat(jsonPathType);
		fm.setProperty(DATE_FORMAT_FIELD_METADATA_PROPERTY, dateFormat);
	}

	private static Object getSingleValue(String value, IFieldMetaData fmd) throws ParseException {
		Class<?> fieldType = fmd.getType();

		if (fieldType.equals(String.class)) {
			return value;
		} else if (fieldType.equals(Byte.class)) {
			return Byte.valueOf(value);
		} else if (fieldType.equals(Short.class)) {
			return Short.valueOf(value);
		} else if (fieldType.equals(Integer.class)) {
			/*
			 * In Solr, an integer value like the number "7" is returned as a value like "7.0": a good way to prevent problems in this case is to use the
			 * following.
			 */
			return new BigDecimal(value).intValue();
		} else if (fieldType.equals(BigInteger.class)) {
			return new BigInteger(value);
		} else if (fieldType.equals(Float.class)) {
			return Float.parseFloat(value);
		} else if (fieldType.equals(Double.class)) {
			return Double.parseDouble(value);
		} else if (fieldType.equals(BigDecimal.class)) {
			return new BigDecimal(value);
		} else if (fieldType.equals(Date.class)) {
			String dateFormat = (String) fmd.getProperty(DATE_FORMAT_FIELD_METADATA_PROPERTY);
			Assert.assertNotNull(dateFormat != null, "dateFormat != null");
			return getSimpleDateFormat(dateFormat).parse(value);
		} else if (fieldType.equals(Timestamp.class)) {
			if (value != null && !value.isEmpty())
				return new Timestamp(Instant.parse(value).getMillis());
			else
				return null;
		} else if (fieldType.equals(Boolean.class)) {
			return Boolean.valueOf(value);
		} else if (fieldType.equals(Long.class)) {
			return Long.valueOf(value);
		}

		Assert.assertUnreachable(String.format("Impossible to resolve field type: %s", fieldType));
		throw new RuntimeException(); // unreachable
	}

	private static Object getValue(Object value, IFieldMetaData fmd) throws ParseException {
		if (value == null) {
			return null;
		}

		String name = fmd.getName();
		Class<?> fieldType = fmd.getType();
		boolean multiValue = fmd.isMultiValue();

		if (multiValue) {
			Object ret[] = null;

			if (!(value instanceof net.minidev.json.JSONArray)) {
				throw new IllegalStateException(
						"Field " + name + " is multivalue but it's value is not a net.minidev.json.JSONArray: " + value + " of type " + value.getClass());
			}

			net.minidev.json.JSONArray arrayValue = (net.minidev.json.JSONArray) value;
			int length = arrayValue.size();

			ret = (Object[]) Array.newInstance(fieldType, length);

			for (int i = 0; i < length; i++) {
				Object currValue = null;
				currValue = arrayValue.get(i);
				ret[i] = getSingleValue(currValue.toString(), fmd);
			}

			return Arrays.asList(ret);
		} else {
			Object ret = null;

			ret = getSingleValue(value.toString(), fmd);

			return ret;
		}
	}

	private static SimpleDateFormat getSimpleDateFormat(String dateFormat) {
		SimpleDateFormat res = new SimpleDateFormat(dateFormat);
		res.setLenient(true);
		return res;
	}

	/**
	 * format like: 'date yyyyMMdd'
	 *
	 * @param typeString
	 * @return
	 */
	private String getDateFormat(String typeString) {
		int index = typeString.indexOf(' ');
		if (index >= 0) {
			while (typeString.charAt(index) == ' ') {
				++index;
			}
			String format = typeString.substring(index).trim();
			if (!format.isEmpty()) {
				if (DEFAULT_TIMESTAMP_PATTERN_UNQUOTED.equals(format)) {
					format = DEFAULT_TIMESTAMP_PATTERN;
				}
				try {
					new SimpleDateFormat(format); // try the pattern
				} catch (IllegalArgumentException e) {
					throw new JSONPathDataReaderException("Invalid pattern: " + format, e);
				}
				return format;
			}
		}
		if (typeString.toLowerCase().startsWith("datetime") || typeString.toLowerCase().startsWith("timestamp")) {
			return DEFAULT_TIMESTAMP_PATTERN;
		}

		if (typeString.toLowerCase().startsWith("date")) {
			return DEFAULT_DATE_PATTERN;
		}

		// time or everything else
		if (typeString.toLowerCase().startsWith("time")) {
			return DEFAULT_TIME_PATTERN;
		}

		if (typeString.toLowerCase().startsWith("iso8601")) {
			return DEFAULT_TIMESTAMP_PATTERN;
		}

		Assert.assertUnreachable("type date not recognized: " + typeString);
		return null;
	}

	private static boolean isRealJsonPath(String jsonPath) {
		// don't start with param substitution
		return jsonPath.startsWith("$")
				&& (!jsonPath.startsWith(StringUtilities.START_PARAMETER) && !jsonPath.startsWith(StringUtilities.START_USER_PROFILE_ATTRIBUTE));
	}

	private static Class<?> getType(String jsonPathType) {
		if (jsonPathType == null) {
			return ALL_OTHER_TYPES;
		}

		if (jsonPathType.equalsIgnoreCase("string")) {
			return String.class;
		} else if (jsonPathType.equalsIgnoreCase("text")) {
			return String.class;
		} else if (jsonPathType.equalsIgnoreCase("byte")) {
			return Byte.class;
		} else if (jsonPathType.equalsIgnoreCase("short")) {
			return Short.class;
		} else if (jsonPathType.toLowerCase().startsWith("int")) {
			return Integer.class;
		} else if (jsonPathType.equalsIgnoreCase("long")) {
			return Long.class;
		} else if (jsonPathType.toLowerCase().startsWith("bigint")) {
			return BigInteger.class;
		} else if (jsonPathType.toLowerCase().startsWith("bigdec")) {
			return BigDecimal.class;
		} else if (jsonPathType.equalsIgnoreCase("float")) {
			return Float.class;
		} else if (jsonPathType.equalsIgnoreCase("double")) {
			return Double.class;
		} else if (jsonPathType.toLowerCase().startsWith("datetime")) {
			return Timestamp.class;
		} else if (jsonPathType.toLowerCase().startsWith("date")) {
			return Date.class;
		} else if (jsonPathType.toLowerCase().startsWith("timestamp")) {
			return Timestamp.class;
		} else if (jsonPathType.toLowerCase().startsWith("iso8601")) {
			return Timestamp.class;
		} else if (jsonPathType.toLowerCase().startsWith("time")) {
			return Time.class;
		} else if (jsonPathType.equalsIgnoreCase("boolean")) {
			return Boolean.class;
		}

		// everything else
		return ALL_OTHER_TYPES;
	}

	public boolean isNgsi() {
		return ngsi;
	}

	public String getJsonPathItems() {
		return jsonPathItems;
	}

	public List<JSONPathAttribute> getJsonPathAttributes() {
		if (ngsiDefaultItems && !dataReadFirstTime) {
			throw new IllegalStateException("NGSI Rest Data Reader needs a first read of data");
		}
		return jsonPathAttributes;
	}

	public boolean isUseDirectlyAttributes() {
		return useDirectlyAttributes;
	}

	@Override
	public boolean isOffsetSupported() {
		return true;
	}

	@Override
	public boolean isFetchSizeSupported() {
		return true;
	}

	@Override
	public boolean isMaxResultsSupported() {
		return true;
	}

}
