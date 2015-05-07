package it.eng.spagobi.engine.util;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class ChartEngineDataUtil {
	public static transient Logger logger = Logger.getLogger(ChartEngineDataUtil.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String loadJsonData(IDataSet dataSet, Map analyticalDrivers, Map userProfile, Locale locale) {

		analyticalDrivers.put("LOCALE", locale);
		dataSet.setParamsMap(analyticalDrivers);
		dataSet.setUserProfileAttributes(userProfile);

		Monitor monitorLD = MonitorFactory.start("SpagoBI_Chart.GetChartDataAction.service.LoadData");

		dataSet.loadData();// start, limit, rowsLimit); // ??????????????????????????

		monitorLD.stop();
		IDataStore dataStore = dataSet.getDataStore();

		JSONObject dataSetJSON = new JSONObject();
		try {
			JSONDataWriter writer = new JSONDataWriter();

			Object resultNumber = dataStore.getMetaData().getProperty("resultNumber");
			if (resultNumber == null)
				dataStore.getMetaData().setProperty("resultNumber", new Integer((int) dataStore.getRecordsCount()));
			dataSetJSON = (JSONObject) writer.write(dataStore);
		} catch (Throwable e) {
			throw new SpagoBIServiceException("Impossible to serialize datastore", e);
		}

		return dataSetJSON.toString();

	}

	public static String loadMetaData(IDataSet dataSet) {

		JSONObject metadataJSON = new JSONObject();
		try {
			IMetaData metadata = dataSet.getMetadata();
			List<IFieldMetaData> fieldsMetaData = new ArrayList<IFieldMetaData>();
			int fieldCount = metadata.getFieldCount();
			for (int i = 0; i < fieldCount; i++) {
				IFieldMetaData fieldMetaData = metadata.getFieldMeta(i);
				fieldsMetaData.add(fieldMetaData);
			}

			JSONArray fieldsJSON = writeFieldsMetadata(fieldsMetaData);
			metadataJSON.put("results", fieldsJSON);

		} catch (Throwable e) {
			throw new SpagoBIServiceException("Impossible to serialize datastore metadata", e);
		}

		return metadataJSON.toString();

	}

	// PROPERTIES TO LOOK FOR INTO THE FIELDS
	public static final String PROPERTY_VISIBLE = "visible";
	public static final String PROPERTY_CALCULATED_EXPERT = "calculatedExpert";
	public static final String PROPERTY_IS_SEGMENT_ATTRIBUTE = "isSegmentAttribute";
	public static final String PROPERTY_IS_MANDATORY_MEASURE = "isMandatoryMeasure";
	public static final String PROPERTY_AGGREGATION_FUNCTION = "aggregationFunction";

	public static JSONArray writeFieldsMetadata(List<IFieldMetaData> fieldsMetaData) throws Exception {

		// field's meta
		JSONArray fieldsMetaDataJSON = new JSONArray();

		List<JSONObject> attributesList = new ArrayList<JSONObject>();
		List<JSONObject> measuresList = new ArrayList<JSONObject>();

		int fieldCount = fieldsMetaData.size();
		logger.debug("Number of fields = " + fieldCount);
		Assert.assertTrue(fieldCount > 0, "Dataset has no fields!!!");

		for (IFieldMetaData fieldMetaData : fieldsMetaData) {

			logger.debug("Evaluating field with name [" + fieldMetaData.getName() + "], alias [" + fieldMetaData.getAlias() + "] ...");

			Boolean isCalculatedExpert = (Boolean) fieldMetaData.getProperty(PROPERTY_CALCULATED_EXPERT);

			if (isCalculatedExpert != null && isCalculatedExpert) {
				logger.debug("The field is a expert calculated field so we skip it");
				// continue;
			}

			Object propertyRawValue = fieldMetaData.getProperty(PROPERTY_VISIBLE);
			logger.debug("Read property " + PROPERTY_VISIBLE + ": its value is [" + propertyRawValue + "]");

			if (propertyRawValue != null && !propertyRawValue.toString().equals("") && (Boolean.parseBoolean(propertyRawValue.toString()) == false)) {
				logger.debug("The field is not visible");
				continue;
			} else {
				logger.debug("The field is visible");
			}
			String fieldName = getFieldName(fieldMetaData);
			String fieldHeader = getFieldAlias(fieldMetaData);
			String fieldColumnType = getFieldColumnType(fieldMetaData);
			JSONObject fieldMetaDataJSON = new JSONObject();
			fieldMetaDataJSON.put("id", fieldName);
			fieldMetaDataJSON.put("alias", fieldHeader);
			fieldMetaDataJSON.put("colType", fieldColumnType);
			FieldType type = fieldMetaData.getFieldType();
			logger.debug("The field type is " + type.name());
			switch (type) {
			case ATTRIBUTE:
				Object isSegmentAttributeObj = fieldMetaData.getProperty(PROPERTY_IS_SEGMENT_ATTRIBUTE);
				logger.debug("Read property " + PROPERTY_IS_SEGMENT_ATTRIBUTE + ": its value is [" + propertyRawValue + "]");
				String attributeNature = (isSegmentAttributeObj != null && (Boolean.parseBoolean(isSegmentAttributeObj.toString()) == true)) ? "segment_attribute"
						: "attribute";

				logger.debug("The nature of the attribute is recognized as " + attributeNature);
				fieldMetaDataJSON.put("nature", attributeNature);
				fieldMetaDataJSON.put("funct", AggregationFunctions.NONE);
				fieldMetaDataJSON.put("iconCls", attributeNature);
				break;
			case MEASURE:
				Object isMandatoryMeasureObj = fieldMetaData.getProperty(PROPERTY_IS_MANDATORY_MEASURE);
				logger.debug("Read property " + PROPERTY_IS_MANDATORY_MEASURE + ": its value is [" + isMandatoryMeasureObj + "]");
				String measureNature = (isMandatoryMeasureObj != null && (Boolean.parseBoolean(isMandatoryMeasureObj.toString()) == true)) ? "mandatory_measure"
						: "measure";
				logger.debug("The nature of the measure is recognized as " + measureNature);
				fieldMetaDataJSON.put("nature", measureNature);
				String aggregationFunction = (String) fieldMetaData.getProperty(PROPERTY_AGGREGATION_FUNCTION);
				logger.debug("Read property " + PROPERTY_AGGREGATION_FUNCTION + ": its value is [" + aggregationFunction + "]");
				fieldMetaDataJSON.put("funct", AggregationFunctions.get(aggregationFunction).getName());
				fieldMetaDataJSON.put("iconCls", measureNature);
				String decimalPrecision = (String) fieldMetaData.getProperty(IFieldMetaData.DECIMALPRECISION);
				if (decimalPrecision != null) {
					fieldMetaDataJSON.put("precision", decimalPrecision);
				} else {
					fieldMetaDataJSON.put("precision", "2");
				}
				break;
			}

			if (type.equals(it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType.MEASURE)) {
				measuresList.add(fieldMetaDataJSON);
			} else {
				attributesList.add(fieldMetaDataJSON);
			}
		}

		// put first measures and only after attributes

		for (Iterator<JSONObject> iterator = measuresList.iterator(); iterator.hasNext();) {
			JSONObject jsonObject = iterator.next();
			fieldsMetaDataJSON.put(jsonObject);
		}

		for (Iterator<JSONObject> iterator = attributesList.iterator(); iterator.hasNext();) {
			JSONObject jsonObject = iterator.next();
			fieldsMetaDataJSON.put(jsonObject);
		}

		return fieldsMetaDataJSON;
	}

	protected static String getFieldAlias(IFieldMetaData fieldMetaData) {
		String fieldAlias = fieldMetaData.getAlias() != null ? fieldMetaData.getAlias() : fieldMetaData.getName();
		return fieldAlias;
	}

	protected static String getFieldName(IFieldMetaData fieldMetaData) {
		String fieldName = fieldMetaData.getName();
		return fieldName;
	}

	protected static String getFieldColumnType(IFieldMetaData fieldMetaData) {
		String fieldColumnType = fieldMetaData.getType().toString();
		fieldColumnType = fieldColumnType.substring(fieldColumnType.lastIndexOf(".") + 1); // clean the class type name
		return fieldColumnType;
	}
}
