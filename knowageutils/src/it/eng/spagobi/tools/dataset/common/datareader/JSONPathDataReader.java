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

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.json.JSONObject;

import com.jayway.jsonpath.JsonPath;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.json.JSONUtils;
import net.minidev.json.JSONArray;

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

	private static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	private static final String ATTRIBUTES_DIRECTLY = "attributesDirectly";

	private static final String ID_NAME = "id";

	private static final String ORION_JSON_PATH_ITEMS = "$.contextResponses[*].contextElement";

	public static class JSONPathAttribute {
		private final String name;
		private final String jsonPathValue;
		private final String jsonPathType;

		public JSONPathAttribute(String name, String jsonPathValue, String jsonPathType) {
			this.name = name;
			this.jsonPathValue = jsonPathValue;
			this.jsonPathType = jsonPathType;
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
		Helper.checkWithoutNulls(jsonPathAttributes, "pathAttributes");
		Helper.checkNotNull(jsonPathAttributes, "jsonPathAttributes");
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

	private static JSONObject isJSON(String responseBody) {
		try {
			JSONObject res = JSONUtils.toJSONObject(responseBody);
			return res;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public synchronized IDataStore read(Object data) {
		Helper.checkNotNull(data, "data");
		if (!(data instanceof String)) {
			throw new IllegalArgumentException("data must be a string");
		}

		String d = (String) data;
		JSONObject jsonData = isJSON(d);
		Assert.assertTrue(jsonData != null, String.format("Data must be a valid JSON: %s", d));

		try {
			DataStore dataStore = new DataStore();
			MetaData dataStoreMeta = new MetaData();
			dataStore.setMetaData(dataStoreMeta);
			List<Object> parsedData = getItems(d);
			addFieldMetadata(dataStoreMeta, parsedData);
			addData(d, dataStore, dataStoreMeta, parsedData);
			return dataStore;
		} catch (ParseException e) {
			throw new JSONPathDataReaderException(e);
		} catch (JSONPathDataReaderException e) {
			throw e;
		} catch (Exception e) {
			throw new JSONPathDataReaderException(e);
		}
	}

	private void addData(String data, DataStore dataStore, MetaData dataStoreMeta, List<Object> parsedData) throws ParseException {

		for (Object o : parsedData) {
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
				String stringValue = isRealJsonPath(jsonPathValue) ? getJSONPathValue(o, jsonPathValue) : jsonPathValue;
				IFieldMetaData fm = fieldMeta;
				Class<?> type = fm.getType();
				if (type == null) {
					// dinamically defined, from json data path
					String typeString = getJSONPathValue(o, (String) fieldMeta.getProperty(JSON_PATH_TYPE_METADATA_PROPERTY));
					Assert.assertNotNull(typeString, "type of jsonpath type");
					type = getType(typeString);
					fm.setType(type);
					if (type.equals(Date.class)) {
						setDateTypeFormat(fm, typeString);
					}
				}
				Assert.assertNotNull(type != null, "type!=null");

				IField field = new Field(getValue(stringValue, fm));
				record.appendField(field);
			}
			if (useDirectlyAttributes) {
				manageDirectlyAttributes(o, record, dataStoreMeta, dataStore);
			}

			dataStore.appendRecord(record);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Object> getItems(String data) {
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void manageDirectlyAttributes(Object data, IRecord rec, MetaData dsm, DataStore dataStore) {
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

	private static String getJSONPathValue(Object o, String jsonPathValue) {
		// can be an array with a single value, a single object or also null (not found)
		Object res = JsonPath.read(o, jsonPathValue);
		if (res == null) {
			return null;
		}

		if (res instanceof JSONArray) {
			JSONArray array = (JSONArray) res;
			if (array.size() > 1) {
				throw new IllegalArgumentException(String.format("There is no unique value: %s", array.toString()));
			}
			if (array.isEmpty()) {
				return null;
			}

			res = array.get(0);
		}

		return res.toString();
	}

	public synchronized int getIdFieldIndex() {
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
	 * @param parsedData
	 *            list of json object (net.minidev)
	 */
	private void addFieldMetadata(MetaData dataStoreMeta, List<Object> parsedData) {
		boolean idSet = false;

		manageNGSI(parsedData);

		for (int index = 0; index < jsonPathAttributes.size(); index++) {
			JSONPathAttribute jpa = jsonPathAttributes.get(index);
			FieldMetadata fm = new FieldMetadata();
			String header = jpa.name;
			fm.setAlias(header);
			fm.setName(header);
			if (ID_NAME.equalsIgnoreCase(header)) {
				if (idSet) {
					throw new JSONPathDataReaderException("There is no unique id field.");
				}
				idSet = true;
				dataStoreMeta.setIdField(index);
			}
			fm.setProperty(JSON_PATH_VALUE_METADATA_PROPERTY, jpa.jsonPathValue);
			if (isRealJsonPath(jpa.jsonPathType)) {
				// dinamically defined
				fm.setProperty(JSON_PATH_TYPE_METADATA_PROPERTY, jpa.jsonPathType);
				// type == null, defined later
			} else {
				Class<?> type = getType(jpa.jsonPathType);
				Assert.assertNotNull(type, "type");
				// type statically defined
				fm.setType(type);
				if (type.equals(Date.class)) {
					setDateTypeFormat(fm, jpa.jsonPathType);
				}

			}
			dataStoreMeta.addFiedMeta(fm);
		}
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
		List<Object> attributes = (List<Object>) element.get("attributes");
		Assert.assertTrue(attributes != null, "attributes!=null");
		if (attributes.isEmpty()) {
			return res;
		}

		for (Object attr : attributes) {
			Map<String, Object> attrMap = (Map<String, Object>) attr;
			String name = (String) attrMap.get("name");
			String jsonPathValue = "$.attributes[?(@.name==" + name + ")].value";
			String jsonPathType = "$.attributes[?(@.name==" + name + ")].type";
			res.add(new JSONPathAttribute(name, jsonPathValue, jsonPathType));
		}

		return res;
	}

	private void setDateTypeFormat(IFieldMetaData fm, String jsonPathType) {
		String dateFormat = getDateFormat(jsonPathType);
		fm.setProperty(DATE_FORMAT_FIELD_METADATA_PROPERTY, dateFormat);
	}

	private static Object getValue(String value, IFieldMetaData fmd) throws ParseException {
		if (value == null) {
			return null;
		}

		Class<?> fieldType = fmd.getType();
		if (fieldType.equals(String.class)) {
			return value;
		} else if (fieldType.equals(BigInteger.class)) {
			return Long.parseLong(value);
		} else if (fieldType.equals(Double.class)) {
			return Double.parseDouble(value);
		} else if (fieldType.equals(Date.class)) {
			String dateFormat = (String) fmd.getProperty(DATE_FORMAT_FIELD_METADATA_PROPERTY);
			Assert.assertNotNull(dateFormat != null, "dateFormat != null");
			return getSimpleDateFormat(dateFormat).parse(value);
		} else if (fieldType.equals(Boolean.class)) {
			return Boolean.valueOf(value);
		} else if (fieldType.equals(Long.class)) {
			return Long.valueOf(value);
		}
		Assert.assertUnreachable(String.format("Impossible to resolve field type: %s", fieldType));
		throw new RuntimeException(); // unreachable
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
			String res = typeString.substring(index).trim();
			if (!res.isEmpty()) {
				try {
					new SimpleDateFormat(res); // try the pattern
				} catch (IllegalArgumentException e) {
					throw new JSONPathDataReaderException("Invalid pattern: " + res, e);
				}
				return res;
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
		} else if (jsonPathType.equalsIgnoreCase("int") || jsonPathType.equalsIgnoreCase("long") || jsonPathType.equalsIgnoreCase("bigint")) {
			return Long.class;
		} else if (jsonPathType.equalsIgnoreCase("float") || jsonPathType.equalsIgnoreCase("double")) {
			return Double.class;
		} else if (jsonPathType.toLowerCase().startsWith("date")) {
			return Date.class;
		} else if (jsonPathType.toLowerCase().startsWith("timestamp")) {
			return Date.class;
		} else if (jsonPathType.toLowerCase().startsWith("time")) {
			return Date.class;
		} else if (jsonPathType.toLowerCase().startsWith("datetime")) {
			return Date.class;
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

	public synchronized List<JSONPathAttribute> getJsonPathAttributes() {
		if (ngsiDefaultItems && !dataReadFirstTime) {
			throw new IllegalStateException("NGSI Rest Data Reader needs a first read of data");
		}
		return jsonPathAttributes;
	}

	public boolean isUseDirectlyAttributes() {
		return useDirectlyAttributes;
	}
}
