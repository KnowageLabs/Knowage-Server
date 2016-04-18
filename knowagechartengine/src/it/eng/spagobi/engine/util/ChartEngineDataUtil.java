/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.engine.util;

import static it.eng.spagobi.engine.util.ChartEngineUtil.ve;
import it.eng.qbe.query.Query;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IQuery;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class ChartEngineDataUtil {
	public static transient Logger logger = Logger.getLogger(ChartEngineDataUtil.class);

	@SuppressWarnings({ "rawtypes" })
	public static String loadJsonData(String jsonTemplate, IDataSet dataSet, Map analyticalDrivers, Map userProfile, Locale locale) throws Throwable {
		IQuery query = extractAggregatedQueryFromTemplate(jsonTemplate);
		return loadJsonData(query, dataSet, analyticalDrivers, userProfile, locale);
	}

	@SuppressWarnings({ "rawtypes" })
	private static String loadJsonData(IQuery query, IDataSet dataSet, Map analyticalDrivers, Map userProfile, Locale locale) throws Throwable {
		IDataStore dataStore = loadDatastore(query, dataSet, analyticalDrivers, userProfile, locale);

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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static IDataStore loadDatastore(IQuery query, IDataSet dataSet, Map analyticalDrivers, Map userProfile, Locale locale) throws JSONException {

		analyticalDrivers.put("LOCALE", locale);
		dataSet.setParamsMap(analyticalDrivers);
		dataSet.setUserProfileAttributes(userProfile);

		Monitor monitorLD = MonitorFactory.start("SpagoBI_Chart.GetChartDataAction.service.LoadData");

		dataSet.loadData();// start, limit, rowsLimit); //
							// ??????????????????????????

		monitorLD.stop();

		IDataStore dataStore = dataSet.getDataStore().aggregateAndFilterRecords(query);
		return dataStore;
	}

	@SuppressWarnings("rawtypes")
	public static String drilldown(String jsonTemplate, String breadcrumb, IDataSet dataSet, Map analyticalDrivers, Map userProfile, Locale locale,
			String documentLabel, IEngUserProfile profile) throws Throwable {

		String result = "";

		JSONObject jo = new JSONObject(jsonTemplate);
		JSONObject category = jo.getJSONObject("CHART").getJSONObject("VALUES").getJSONObject("CATEGORY");
		String groupBys = category.optString("groupby");
		String groupByNames = category.optString("groupbyNames");

		JSONArray jaBreadcrumb = new JSONArray(breadcrumb);
		if (groupBys != null) {
			String drilldownSerie = "";
			Map<String, Object> drilldownParams = new LinkedHashMap<>();
			String drilldownCategory = category.getString("column");
			String drilldownCategoryName = category.getString("name");
			String selectedCategory = "";
			String[] gbys = groupBys.split(",");
			String[] gbyNames = (groupByNames != null && !groupByNames.isEmpty()) ? groupByNames.split(",") : gbys;
			int i;
			for (i = 0; i < jaBreadcrumb.length(); i++) {
				JSONObject drilldown = (JSONObject) jaBreadcrumb.get(i);

				String selectedName = drilldown.getString("selectedName");
				String selectedSerie = drilldown.getString("selectedSerie");
				String gby = gbys[i];
				String gbyName = (gbyNames.length > i) ? gbyNames[i] : gbys[i];

				drilldownParams.put(drilldownCategory, selectedName);

				// Exiting the form we'll have last values
				if (i == 0)
					drilldownSerie = selectedSerie;
				drilldownCategory = gby;
				drilldownCategoryName = gbyName;
				selectedCategory = selectedName;
			}

			IQuery q = extractAggregatedQueryFromTemplate(jsonTemplate, true, drilldownSerie, drilldownCategory, drilldownParams);

			String jsonData = loadJsonData(q, dataSet, analyticalDrivers, userProfile, locale);
			boolean enableNextDrilldown = i < gbys.length;

			/**
			 * We are sending additional information about the web application from which we call the VM. This boolean will tell us if we are coming from the
			 * Highcharts Export web application. The value of "exportWebApp" input parameter contains this boolean. This information is useful when we have
			 * drilldown, i.e. more than one category for the Highcharts chart (BAR, LINE).
			 *
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			VelocityContext velocityContext = ChartEngineUtil.loadVelocityContext(null, jsonData, false, documentLabel, profile);

			velocityContext.put("selectedCategory", selectedCategory);
			velocityContext.put("drilldownSerie", drilldownSerie);
			velocityContext.put("drilldownCategory", drilldownCategory);
			velocityContext.put("drilldownCategoryName", drilldownCategoryName);
			velocityContext.put("enableNextDrilldown", enableNextDrilldown);

			Template velocityTemplate = ve.getTemplate("/chart/templates/highcharts414/drilldowndata.vm");
			result = ChartEngineUtil.applyTemplate(velocityTemplate, velocityContext);
		}

		return result;
	}

	private static IQuery extractAggregatedQueryFromTemplate(String jsonTemplate) throws JSONException {
		return extractAggregatedQueryFromTemplate(jsonTemplate, false, null, null, null);
	}

	private static IQuery extractAggregatedQueryFromTemplate(String jsonTemplate, boolean isDrilldown, String drilldownSerie, String drilldownCategory,
			Map<String, Object> drilldownParams) throws JSONException {

		IQuery q = new Query();

		JSONObject jo = new JSONObject(jsonTemplate);

		List<JSONObject> seriesList = new LinkedList<>();

		// Select fields

		JSONArray seriesArray = jo.getJSONObject("CHART").getJSONObject("VALUES").optJSONArray("SERIE");
		JSONObject singleSerie = jo.getJSONObject("CHART").getJSONObject("VALUES").optJSONObject("SERIE");

		String chartType = jo.getJSONObject("CHART").getString("type");

		if (seriesArray != null) {
			for (int i = 0; i < seriesArray.length(); i++) {
				JSONObject thisSerie = (JSONObject) seriesArray.get(i);

				seriesList.add(thisSerie);

			}
		} else {
			if (singleSerie != null) {
				seriesList.add(singleSerie);
			}
		}

		for (JSONObject serie : seriesList) {

			String serieColumn = serie.getString("column");
			String serieName = serie.getString("name");
			String serieFunction = StringUtilities.isNotEmpty(serie.optString("groupingFunction")) ? serie.optString("groupingFunction") : "SUM";

			/**
			 * parallel chart needs possibility to work without aggregation function
			 */
			if (chartType.equals("PARALLEL") && jo.getJSONObject("CHART").getJSONObject("LIMIT").getString("groupByCategory").equals("false")) {
				serieFunction = "NONE";
			}

			if (!isDrilldown || serieName.equalsIgnoreCase(drilldownSerie)) {
				String fieldAlias = serieColumn + (!isDrilldown ? "_" + serieFunction : "");

				String orderTypeFinal = (serie.opt("orderType") != null) ? orderTypeFinal = serie.opt("orderType").toString().toUpperCase() : null;

				q.addSelectFiled(serieColumn, serieFunction, fieldAlias, true, true, false, orderTypeFinal, null, null);
			}

		}

		// Category
		if (!isDrilldown) {

			JSONArray categories = new JSONArray();
			JSONObject category = jo.getJSONObject("CHART").getJSONObject("VALUES").optJSONObject("CATEGORY");

			if (category != null) {
				categories.put(category);
			}
			// multiple categories for non conventional charts es.
			// SUNBURST/TREEMAP
			else {
				JSONArray optJSONArray = jo.getJSONObject("CHART").getJSONObject("VALUES").optJSONArray("CATEGORY");
				categories = (optJSONArray != null) ? optJSONArray : categories;
			}

			for (int i = 0; i < categories.length(); i++) {
				JSONObject cat = (JSONObject) categories.get(i);

				if (chartType.equals("PARALLEL") && jo.getJSONObject("CHART").getJSONObject("LIMIT").getString("groupByCategory").equals("false")) {

					/**
					 * The PARALLEL chart does not handle the feature of ordering the chart X-axis by the specific column (attribute), hence we do not take
					 * 'orderType' and 'orderColumn' properties into count.
					 *
					 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					q.addSelectFiled(cat.getString("column"), null, cat.getString("column"), true, true, false, "ASC", null, null);

				} else {

					/**
					 * Taking into count the "order type" and "order by (column)" properties that are set for the first category of the chart.
					 *
					 * NOTE: This possibility of setting the ordering column should be enabled and provided for all layers, not just on the first one.
					 *
					 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					q.addSelectFiled(cat.getString("column"), null, cat.getString("column"), true, true, true, cat.getString("orderType"), null,
							cat.getString("orderColumn"));

				}
			}
		} else {

			/**
			 * The ordering type of the subsequent category (all those that follow the first category) will be fixed to ascending (ASC), since the current
			 * implementation does not cover ordering column and ordering type also for categories other than the first one.
			 *
			 * NOTE: This possibility of setting the ordering column should be enabled and provided for all layers, not just on the first one.
			 *
			 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			q.addSelectFiled(drilldownCategory, null, drilldownCategory, true, true, true, "ASC", null, null);

		}

		// Where clause
		if (isDrilldown) {
			Set<String> keySet = drilldownParams.keySet();
			for (String colName : keySet) {
				q.addWhereField(colName, "", false, new String[] { colName }, null, null, null, null, null, "EQUALS TO",
						new String[] { (String) drilldownParams.get(colName) }, null, null, null, null, null, "AND");
			}
		}

		logger.debug(q.toSql("SCHEMA", "TABLE"));

		return q;
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
		fieldColumnType = fieldColumnType.substring(fieldColumnType.lastIndexOf(".") + 1); // clean
																							// the
																							// class
																							// type
																							// name
		return fieldColumnType;
	}
}
