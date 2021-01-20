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
package it.eng.spagobi.tools.dataset.common.datawriter;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.tools.dataset.bo.DataSetVariable;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.solr.SolrDataStore;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * @author Marco Libanori
 */
public class JSONDataWriter implements IDataWriter {

	public static final String TOTAL_PROPERTY = "results";
	public static final String ROOT = "rows";

	public static final String PROPERTY_PUT_IDS = "putIDs";

	public static final String DATE_FORMAT = "dd/MM/yyyy";
	public static final String CACHE_DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "HH:mm:ss";
	public static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;
	public static final String CACHE_DATE_TIME_FORMAT = CACHE_DATE_FORMAT + " " + TIME_FORMAT;
	public static final String TIMESTAMP_FORMAT = DATE_TIME_FORMAT + ".SSS";
	public static final String CACHE_TIMESTAMP_FORMAT = CACHE_DATE_TIME_FORMAT + ".SSS";

	private boolean putIDs = true;
	private boolean adjust = false;
	private boolean setRenderer = false;
	private boolean writeDataOnly = false;
	private JSONArray fieldsOptions;
	private Locale locale;
	private boolean useIdProperty;
	private boolean preserveOriginalDataTypes = false;

	protected final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);
	protected final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat(TIMESTAMP_FORMAT);
	protected final SimpleDateFormat CACHE_TIMESTAMP_FORMATTER = new SimpleDateFormat(CACHE_TIMESTAMP_FORMAT);
	protected final SimpleDateFormat CACHE_TIMEONLY_FORMATTER = new SimpleDateFormat(TIME_FORMAT);

	// public static final String WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_OPTIONS = "options";
	// public static final String WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR = "measureScaleFactor";

	public static final String METADATA = "metaData";
	public static final String PROPERTY_FIELD_OPTION = "PROPERTY_FIELD_OPTION";
	public static final String PROPERTY_ADJUST = "PROPERTY_ADJUST";
	public static final String PROPERTY_WRITE_DATA_ONLY = "PROPERTY_WRITE_DATA_ONLY";
	private static final String ID_PROPERTY = "id";
	private static final Object USE_ID_PROPERTY = "USE_ID_PROPERTY";

	public JSONDataWriter() {
	}

	public JSONDataWriter(Map<String, Object> properties) {
		if (properties != null) {
			Object o = properties.get(PROPERTY_PUT_IDS);
			if (o != null) {
				this.putIDs = Boolean.parseBoolean(o.toString());
			}
			o = properties.get(PROPERTY_FIELD_OPTION);
			if (o != null) {
				this.fieldsOptions = (JSONArray) o;
			}
			o = properties.get(PROPERTY_ADJUST);
			if (o != null) {
				this.adjust = Boolean.parseBoolean(o.toString());
			}
			o = properties.get(PROPERTY_WRITE_DATA_ONLY);
			if (o != null) {
				this.writeDataOnly = Boolean.parseBoolean(o.toString());
			}

			o = properties.get(USE_ID_PROPERTY);
			if (o != null) {
				this.useIdProperty = Boolean.parseBoolean(o.toString());
			}

		}
	}

	/**
	 * Logger component.
	 */
	public static transient Logger logger = Logger.getLogger(JSONDataWriter.class);

	/**
	 * @param dataStore
	 * @return
	 * @throws RuntimeException
	 */
	@Override
	public Object write(IDataStore dataStore) throws RuntimeException {
		if (writeDataOnly) {
			return writeOnlyData(dataStore);
		} else {
			if (dataStore instanceof SolrDataStore) {
				return write((SolrDataStore) dataStore);
			} else {
				return writeDataAndMeta(dataStore);
			}
		}
	}

	private Object write(SolrDataStore dataStore) throws RuntimeException {
		JSONObject result;
		try {
			result = writeDataAndMeta(dataStore);
			JSONObject facets = new JSONObject();
			for (String columnName : dataStore.getFacets().keySet()) {
				facets.put(columnName, writeDataAndMeta(dataStore.getFacets().get(columnName)));
			}

			result.put("facets", facets);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	public JSONArray writeOnlyData(IDataStore dataStore) throws RuntimeException {

		int recNo;

		int resultNumber;
		Object propertyRawValue;

		Assert.assertNotNull(dataStore, "Object to be serialized connot be null");

		try {
			JSONArray results = new JSONArray();

			propertyRawValue = dataStore.getMetaData().getProperty("resultNumber");
			if (propertyRawValue == null) {
				propertyRawValue = new Integer(1);
			}
			Assert.assertNotNull(propertyRawValue, "DataStore property [resultNumber] cannot be null");
			Assert.assertTrue(propertyRawValue instanceof Integer, "DataStore property [resultNumber] must be of type [Integer]");
			resultNumber = ((Integer) propertyRawValue).intValue();
			Assert.assertTrue(resultNumber >= 0,
					"DataStore property [resultNumber] cannot be equal to [" + resultNumber + "]. It must be greater or equal to zero");

			// records
			recNo = 0;
			Iterator records = dataStore.iterator();
			while (records.hasNext()) {
				IRecord record = (IRecord) records.next();
				JSONObject recordJSON = writeRecord(dataStore, record);

				if (this.putIDs) {
					recordJSON.put("id", ++recNo);
				}

				results.put(recordJSON);
			}

			return results;
		} catch (Throwable t) {
			throw new RuntimeException("An unpredicted error occurred while serializing dataStore", t);
		}

	}

	public JSONObject writeRecord(IDataStore dataStore, IRecord record) throws JSONException {

		JSONObject recordJSON = new JSONObject();
		IMetaData metaData = dataStore.getMetaData();

		for (int i = 0; i < metaData.getFieldCount(); i++) {
			IFieldMetaData fieldMetaData = metaData.getFieldMeta(i);

			Object propertyRawValue = fieldMetaData.getProperty("visible");
			if (propertyRawValue != null && (propertyRawValue instanceof Boolean) && ((Boolean) propertyRawValue).booleanValue() == false) {
				continue;
			}

			IField field = new Field();
			try {
				field = record.getFieldAt(i);
			} catch (IndexOutOfBoundsException idxEx) {
				logger.info("Unavailable field " + fieldMetaData.getName());
				field.setValue(null);
				continue;
			}

			String fieldName = adjust ? fieldMetaData.getName() : getFieldName(fieldMetaData, i);

			Object fieldValue = getFieldValue(field, fieldMetaData);
			if (fieldValue == null) {
				recordJSON.put(fieldName, "");
			} else {
				recordJSON.put(fieldName, fieldValue);
			}
		}
		return recordJSON;
	}

	protected Object getFieldValue(IField field, IFieldMetaData fieldMetaData) {

		if (preserveOriginalDataTypes) {
			Object toReturn;
			if (BigDecimal.class.isAssignableFrom(fieldMetaData.getType()) && field.getValue() != null) {
				toReturn = Float.parseFloat(field.getValue().toString());
			} else {
				toReturn = field.getValue();
			}
			return toReturn;

		} else {
			Object result = "";
			if (field.getValue() != null && !field.getValue().equals("")) {
				if (fieldMetaData.isMultiValue()) {
					JSONArray _result = new JSONArray();
					List list = (List) field.getValue();

					for (Object obj : list) {
						_result.put(obj);
					}
					result = _result;
				} else if (Timestamp.class.isAssignableFrom(fieldMetaData.getType())) {
					result = TIMESTAMP_FORMATTER.format(field.getValue());
				} else if (Time.class.isAssignableFrom(fieldMetaData.getType())) {
					result = CACHE_TIMEONLY_FORMATTER.format(field.getValue());
				} else if (Date.class.isAssignableFrom(fieldMetaData.getType())) {
					result = DATE_FORMATTER.format(field.getValue());
				} else if (Boolean.class.isAssignableFrom(fieldMetaData.getType())) {
					result = Boolean.valueOf(field.getValue().toString());
				} else if (Byte.class.isAssignableFrom(fieldMetaData.getType())) {
					result = Byte.valueOf(field.getValue().toString());
				} else if (Short.class.isAssignableFrom(fieldMetaData.getType())) {
					result = Short.valueOf(field.getValue().toString());
				} else if (Integer.class.isAssignableFrom(fieldMetaData.getType())) {
					result = Integer.valueOf(field.getValue().toString());
				} else if (Long.class.isAssignableFrom(fieldMetaData.getType())) {
					result = Long.valueOf(field.getValue().toString());
				} else if (BigInteger.class.isAssignableFrom(fieldMetaData.getType())) {
					result = new BigInteger(field.getValue().toString());
				} else if (Float.class.isAssignableFrom(fieldMetaData.getType())) {
					result = Float.valueOf(field.getValue().toString());
				} else if (Double.class.isAssignableFrom(fieldMetaData.getType())) {
					result = Double.valueOf(field.getValue().toString());
				} else if (BigDecimal.class.isAssignableFrom(fieldMetaData.getType())) {
					result = new BigDecimal(field.getValue().toString());
				} else {
					result = field.getValue().toString();
				}
			}

			return result;

		}

	}

	public JSONObject writeDataAndMeta(IDataStore dataStore) throws RuntimeException {
		JSONObject result = null;
		JSONObject metadata;
		IField field;
		IRecord record;
		JSONObject recordJSON;
		int recNo = 0;
		JSONArray recordsJSON;
		int resultNumber;
		Object propertyRawValue;

		Assert.assertNotNull(dataStore, "Object to be serialized cannot be null");

		metadata = (JSONObject) write(dataStore.getMetaData());

		try {
			result = new JSONObject();

			Date cacheDate = dataStore.getCacheDate();
			if (cacheDate != null) {
				String date = CACHE_TIMESTAMP_FORMATTER.format(cacheDate);
				metadata.put("cacheDate", date);
			}

			result.put(METADATA, metadata);

			propertyRawValue = dataStore.getMetaData().getProperty("resultNumber");
			if (propertyRawValue == null) {
				propertyRawValue = new Integer(1);
			}
			Assert.assertNotNull(propertyRawValue, "DataStore property [resultNumber] cannot be null");
			Assert.assertTrue(propertyRawValue instanceof Integer, "DataStore property [resultNumber] must be of type [Integer]");
			resultNumber = ((Integer) propertyRawValue).intValue();
			Assert.assertTrue(resultNumber >= 0,
					"DataStore property [resultNumber] cannot be equal to [" + resultNumber + "]. It must be greater or equal to zero");
			result.put(TOTAL_PROPERTY, resultNumber);

			recordsJSON = new JSONArray();
			result.put(ROOT, recordsJSON);

			// records
			recNo = 0;
			Iterator records = dataStore.iterator();
			while (records.hasNext()) {
				int recordSize = -1;
				record = (IRecord) records.next();
				recordJSON = new JSONObject();
				if (this.putIDs) {
					recordJSON.put("id", ++recNo);
				}

				if (dataStore.getMetaData().getProperty("isSparse") != null) {
					recordSize = record.getFields().size();
				}

				for (int i = 0, j = 0; i < dataStore.getMetaData().getFieldCount(); i++) {
					IFieldMetaData fieldMetaData = dataStore.getMetaData().getFieldMeta(i);

					propertyRawValue = fieldMetaData.getProperty("visible");
					if (propertyRawValue != null && (propertyRawValue instanceof Boolean) && ((Boolean) propertyRawValue).booleanValue() == false) {
						continue;
					}

					String fieldHeader = this.getFieldHeader(fieldMetaData);
					if ("sbicache_row_id".equalsIgnoreCase(fieldHeader)) {
						continue;
					}

					int fieldPosition = i;
					if (recordSize < 0 || fieldPosition < recordSize) {
						field = record.getFieldAt(fieldPosition);
					} else {
						field = new Field("");
					}

					String fieldName = adjust ? fieldMetaData.getName() : getFieldName(fieldMetaData, j++);
					Object fieldValue = getFieldValue(field, fieldMetaData);

					try {
						if (oracle.jdbc.OracleClob.class.isAssignableFrom(fieldMetaData.getType())
								|| oracle.sql.CLOB.class.isAssignableFrom(fieldMetaData.getType())) { // Can we add a limit?
							Reader r = ((Clob) field.getValue()).getCharacterStream();
							StringBuffer buffer = new StringBuffer();
							int ch;
							while ((ch = r.read()) != -1) {
								buffer.append("" + (char) ch);
							}
							fieldValue = buffer.toString();
						} // provided
					} catch (NoClassDefFoundError t) {
						logger.debug("Class not found error", t);
					} catch (Exception e) {
						logger.error("An unpredicted error occurred at recno [" + recNo + "] while serializing dataStore", e);
						throw new SpagoBIRuntimeException("An unpredicted error occurred at recno [" + recNo + "] while serializing dataStore", e);
					}

					if (fieldValue == null) {
						recordJSON.put(fieldName, "");
					} else {
						recordJSON.put(fieldName, fieldValue);
					}
				}

				recordsJSON.put(recordJSON);

			}

		} catch (Throwable t) {
			throw new RuntimeException("An unpredicted error occurred at recno [" + recNo + "] while serializing dataStore", t);
		} finally {

		}

		return result;
	}

	protected String getFieldHeader(IFieldMetaData fieldMetaData) {
		String fieldHeader = fieldMetaData.getAlias() != null ? fieldMetaData.getAlias() : fieldMetaData.getName();
		return fieldHeader;
	}

	protected String getFieldName(IFieldMetaData fieldMetaData, int i) {
		String fieldName = "column_" + (i + 1);
		return fieldName;
	}

	private Object write(IMetaData metadata) {

		try {

			JSONObject toReturn = new JSONObject();

			toReturn.put("totalProperty", TOTAL_PROPERTY);
			toReturn.put("root", ROOT);
			if (this.putIDs) {
				toReturn.put("id", "id");
			}

			// field's meta
			JSONArray fieldsMetaDataJSON = new JSONArray();
			fieldsMetaDataJSON.put("recNo"); // counting column
			for (int i = 0, j = 0; i < metadata.getFieldCount(); i++) {
				IFieldMetaData fieldMetaData = metadata.getFieldMeta(i);

				Object propertyRawValue = fieldMetaData.getProperty("visible");
				if (propertyRawValue != null && (propertyRawValue instanceof Boolean) && ((Boolean) propertyRawValue).booleanValue() == false) {
					continue;
				}

				String fieldHeader = getFieldHeader(fieldMetaData);
				if ("sbicache_row_id".equalsIgnoreCase(fieldHeader))
					continue;

				String fieldName = getFieldName(fieldMetaData, j++);

				JSONObject fieldMetaDataJSON = new JSONObject();
				if (adjust) {
					fieldMetaDataJSON.put("name", fieldHeader);
				} else {
					fieldMetaDataJSON.put("name", fieldName);

					/**
					 * Id field is recognized as column_x
					 */
					if (useIdProperty && "id".equals(fieldHeader)) {
						toReturn.put("idProperty", fieldName);
					}
				}
				fieldMetaDataJSON.put("header", fieldHeader);
				fieldMetaDataJSON.put("dataIndex", fieldName);
				// This will enable the use of a custom renderer in a Ext Grid
				if (isSetRenderer()) {
					fieldMetaDataJSON.put("renderer", "myRenderer");
				}

				Class clazz = fieldMetaData.getType();
				if (clazz == null) {
					logger.debug("Metadata class is null; considering String as default");
					clazz = String.class;
				} else {
					logger.debug("Column [" + (i + 1) + "] class is equal to [" + clazz.getName() + "]");
				}

				if (Number.class.isAssignableFrom(clazz)) {
					// BigInteger, Integer, Long, Short, Byte
					if (Integer.class.isAssignableFrom(clazz) || BigInteger.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)
							|| Short.class.isAssignableFrom(clazz) || Byte.class.isAssignableFrom(clazz)) {
						logger.debug("Column [" + (i + 1) + "] type is equal to [" + "INTEGER" + "]");
						fieldMetaDataJSON.put("type", "int");
					} else {
						logger.debug("Column [" + (i + 1) + "] type is equal to [" + "FLOAT" + "]");
						fieldMetaDataJSON.put("type", "float");
					}
					int precision = fieldMetaData.getPrecision();
					fieldMetaDataJSON.put("precision", precision);
					int scale = fieldMetaData.getScale();
					fieldMetaDataJSON.put("scale", scale);
					String format = (String) fieldMetaData.getProperty("format");
					if (format != null) {
						fieldMetaDataJSON.put("format", format);
					}
					String decimalPrecision = (String) fieldMetaData.getProperty(IFieldMetaData.DECIMALPRECISION);
					if (decimalPrecision != null) {
						fieldMetaDataJSON.put("format", "{" + IFieldMetaData.DECIMALPRECISION + ": " + decimalPrecision + "}");
					}

				} else if (String.class.isAssignableFrom(clazz)) {
					String jsonPathType = (String) fieldMetaData.getProperty("jsonPathType");
					if ("text".equals(jsonPathType)) {
						logger.debug("Column [" + (i + 1) + "] type is equal to [TEXT]");
						fieldMetaDataJSON.put("type", "text");
					} else {
						logger.debug("Column [" + (i + 1) + "] type is equal to [" + "STRING" + "]");
						fieldMetaDataJSON.put("type", "string");
					}
				} else if (Timestamp.class.isAssignableFrom(clazz) || "oracle.sql.TIMESTAMP".equals(clazz.getName())) {
					logger.debug("Column [" + (i + 1) + "] type is equal to [" + "TIMESTAMP" + "]");
					fieldMetaDataJSON.put("type", "date");
					fieldMetaDataJSON.put("subtype", "timestamp");
					fieldMetaDataJSON.put("dateFormat", "d/m/Y H:i:s.uuu");
					fieldMetaDataJSON.put("dateFormatJava", "dd/MM/yyyy HH:mm:ss.SSS");
				} else if (Time.class.isAssignableFrom(clazz)) {
					logger.debug("Column [" + (i + 1) + "] type is equal to [" + "DATE" + "]");
					fieldMetaDataJSON.put("type", "time");
					fieldMetaDataJSON.put("dateFormat", "H:i:s.uuu");
					fieldMetaDataJSON.put("dateFormatJava", "HH:mm:ss.SSS");
				} else if (Date.class.isAssignableFrom(clazz)) {
					logger.debug("Column [" + (i + 1) + "] type is equal to [" + "DATE" + "]");
					fieldMetaDataJSON.put("type", "date");
					fieldMetaDataJSON.put("dateFormat", "d/m/Y");
					fieldMetaDataJSON.put("dateFormatJava", "dd/MM/yyyy");
				} else if (Boolean.class.isAssignableFrom(clazz)) {
					logger.debug("Column [" + (i + 1) + "] type is equal to [" + "BOOLEAN" + "]");
					fieldMetaDataJSON.put("type", "boolean");
				} else {
					logger.warn("Column [" + (i + 1) + "] type is equal to [" + "???" + "]");
					fieldMetaDataJSON.put("type", "string");
				}

				Boolean calculated = (Boolean) fieldMetaData.getProperty("calculated");
				calculated = calculated == null ? Boolean.FALSE : calculated;
				if (calculated.booleanValue() == true) {
					DataSetVariable variable = (DataSetVariable) fieldMetaData.getProperty("variable");
					if (variable.getType().equalsIgnoreCase(DataSetVariable.HTML)) {
						fieldMetaDataJSON.put("type", "auto");
						fieldMetaDataJSON.remove("type");
						fieldMetaDataJSON.put("subtype", "html");
					}

				}

				String detailProperty = (String) metadata.getProperty("detailProperty");
				if (detailProperty != null && fieldHeader.equalsIgnoreCase(detailProperty)) {
					toReturn.put("detailProperty", fieldName);
					fieldMetaDataJSON.put("hidden", true);
				}

				boolean multiValue = fieldMetaData.isMultiValue();
				fieldMetaDataJSON.put("multiValue", multiValue);

				fieldsMetaDataJSON.put(fieldMetaDataJSON);
			}
			toReturn.put("fields", fieldsMetaDataJSON);

			return toReturn;

		} catch (Throwable t) {
			throw new RuntimeException("An unpredicted error occurred while serializing dataStore", t);
		} finally {

		}
	}

	/**
	 * @return the setRenderer
	 */
	public boolean isSetRenderer() {
		return setRenderer;
	}

	/**
	 * @param setRenderer the setRenderer to set
	 */
	public void setSetRenderer(boolean setRenderer) {
		this.setRenderer = setRenderer;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public boolean isAdjust() {
		return adjust;
	}

	public void setAdjust(boolean adjust) {
		this.adjust = adjust;
	}

	public void setPreserveOriginalDataTypes(boolean preserveOriginalDataTypes) {
		this.preserveOriginalDataTypes = preserveOriginalDataTypes;
	}
}
