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
package it.eng.spagobi.engine.chart.util;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.generic.EscapeTool;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engine.chart.ChartEngineConfig;
import it.eng.spagobi.engine.chart.model.conf.ChartConfig;
import it.eng.spagobi.tools.crossnavigation.dao.ICrossNavigationDAO;
import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.Association.Field;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroupJSONSerializer;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceParameterException;

public class ChartEngineUtil {

	private static final String STYLE_TAG = "style";
	private static final String STYLES_SEPARATOR = ";";
	private static final String STYLE_KEY_VALUE_SEPARATOR = ":";

	private static final String MAP_STYLES_SEPARATOR = ",";
	private static final String MAP_STYLE_KEY_VALUE_SEPARATOR = "=";

	public static transient Logger logger = Logger.getLogger(ChartEngineUtil.class);

	public static VelocityEngine ve;

	static {

		try {
			ve = new VelocityEngine();
			ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
			ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "true");

			ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
			ve.setProperty("runtime.log.logsystem.log4j.category", "velocity");
			ve.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
			ve.init();
		} catch (Exception e) {
			e.toString();
		}

	}

	public static String getLibraryInitializerPath(String jsonTemplateFromXML, String documentLabel, IEngUserProfile profile) {
		String chartType = extractChartType(jsonTemplateFromXML, documentLabel, profile);
		ChartConfig chartConfig = ChartEngineConfig.getChartLibConf().get(chartType);
		return chartConfig == null ? null : chartConfig.getLibraryInitializerPath();
	}

	public static String getChartLibNamesConfig() {
		Map<String, ChartConfig> chartLibConf = ChartEngineConfig.getChartLibConf();
		Set<String> chartTypes = chartLibConf.keySet();
		String ret = "{";
		for (String chartType : chartTypes) {
			ret += chartType + ":'" + chartLibConf.get(chartType).getName() + "',";
		}
		ret += "}";
		return ret;
	}

	public static String getVelocityModelPath(String chartType) {
		ChartConfig chartConfig = ChartEngineConfig.getChartLibConf().get(chartType);
		return chartConfig.getVelocityModelPath();
	}

	private static String extractChartType(String jsonTemplate, String documentLabel, IEngUserProfile profile) {
		return extractChartType(jsonTemplate, loadVelocityContext(jsonTemplate, documentLabel, profile));
	}

	public static String extractChartType(String jsonTemplate, VelocityContext velocityContext) {
		Template velocityTemplate = ve.getTemplate("chart/templates/get_chart_type.vm");
		StringWriter chartType = new StringWriter();
		velocityTemplate.merge(velocityContext, chartType);

		String chartTypeString = chartType.toString();
		velocityContext.put("chartType", chartTypeString);
		return chartTypeString;
	}

	public static VelocityContext loadVelocityContext(String jsonToConvert, String documentLabel, IEngUserProfile profile) {
		return loadVelocityContext(jsonToConvert, null, false, documentLabel, profile);
	}

	/**
	 * We are sending additional information about the web application from which we call the VM. This boolean will tell us if we are coming from the Highcharts
	 * Export web application. The value of "exportWebApp" input parameter contains this boolean. This information is useful when we have drilldown, i.e. more
	 * than one category for the Highcharts chart (BAR, LINE).
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	public static VelocityContext loadVelocityContext(String jsonToConvert, String jsonData, boolean exportWebApp, String documentLabel,
			IEngUserProfile profile) {
		VelocityContext velocityContext = new VelocityContext();

		Map<String, Object> mapTemplate = null;
		Map<String, Object> mapData = null;
		try {
			velocityContext.put("datasettransformer", new DataSetTransformer());
			velocityContext.put("ChartEngineUtil", ChartEngineUtil.class);
			velocityContext.put(Integer.class.getSimpleName(), Integer.class);
			velocityContext.put("escapeTool", new EscapeTool());

			if (jsonToConvert != null) {
				mapTemplate = convertJsonToMap(jsonToConvert, true);
				velocityContext.put("chart", mapTemplate.get("chart") != null ? mapTemplate.get("chart") : mapTemplate.get("CHART"));
			}
			if (jsonData != null) {
				mapData = convertJsonToMap(jsonData, false);
				velocityContext.put("data", mapData);
			}
			ICrossNavigationDAO crossDao = DAOFactory.getCrossNavigationDAO();
			crossDao.setUserProfile(profile);
			if (crossDao.documentIsCrossable(documentLabel)) {
				velocityContext.internalPut("crossNavigation", true);
			} else {
				velocityContext.internalPut("crossNavigation", "");
			}

			/**
			 * We are sending additional information about the web application from which we call the VM. This boolean will tell us if we are coming from the
			 * Highcharts Export web application. The value of "exportWebApp" input parameter contains this boolean. This information is useful when we have
			 * drilldown, i.e. more than one category for the Highcharts chart (BAR, LINE).
			 *
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			velocityContext.put("exportWebApp", exportWebApp);

		} catch (IOException e) {
			logger.error("Error in template to be converted: " + jsonToConvert, e);
		}
		return velocityContext;
	}

	public static Map<String, Object> convertJsonToMap(String json, boolean escape) throws JsonParseException, JsonMappingException, IOException {
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);

		TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
		};

		// TODO Add at this level StringEscapeUtils.escapeHtml
		Map<String, Object> result = mapper.readValue(json, typeRef);

		result = changeDataByPrecision(result);

		Map<String, Object> escapedMapStrings = escape ? escapeMapStrings(result) : result;

		// return result;
		return escapedMapStrings;
	}

	private static Map<String, Object> changeDataByPrecision(Map<String, Object> resultToChange) {

		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
		HashMap<String, Integer> precisionFields = new HashMap<String, Integer>();
		boolean found = false;
		for (String key : resultToChange.keySet()) {
			if (key.equalsIgnoreCase("metaData")) {
				found = true;
				LinkedHashMap<String, Object> resultMEta = (LinkedHashMap<String, Object>) resultToChange.get(key);
				for (String keys : resultMEta.keySet()) {

					if (keys.equalsIgnoreCase("fields")) {
						List<Map<String, Object>> resultFields = (List<Map<String, Object>>) resultMEta.get(keys);

						for (int i = 0; i < resultFields.size(); i++) {

							if (resultFields.get(i) instanceof LinkedHashMap<?, ?>) {
								if (resultFields.get(i).get("scale") != null) {
									precisionFields.put((String) resultFields.get(i).get("name"), (Integer) resultFields.get(i).get("scale"));

								}
							}

						}

					}
				}

			}

		}
		if (!found)
			return resultToChange;
		for (String key : resultToChange.keySet()) {
			if (key.equalsIgnoreCase("rows")) {
				List<Map<String, Object>> resultFields = (List<Map<String, Object>>) resultToChange.get(key);
				for (int i = 0; i < resultFields.size(); i++) {

					if (resultFields.get(i) instanceof LinkedHashMap<?, ?>) {
						LinkedHashMap<String, Object> resultTemp = (LinkedHashMap<String, Object>) resultFields.get(i);
						for (String keys : resultTemp.keySet()) {

							if (precisionFields.get(keys) != null && precisionFields.get(keys) <= 0) {
								if (resultTemp.get(keys) instanceof Double) {
									BigDecimal newValue = new BigDecimal((Double) resultTemp.get(keys));
									resultTemp.put(keys, newValue.toPlainString());

								}
							}

						}

					}
				}

			}
		}

		return resultToChange;
	}

	/**
	 *
	 * */
	private static LinkedHashMap<String, Object> escapeMapStrings(Map<String, Object> mapElement2) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

		for (String key : mapElement2.keySet()) {
			if (mapElement2.get(key) instanceof List) { // if tag selected is an array
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> mapsArray = (List<Map<String, Object>>) mapElement2.get(key);

				ArrayList<LinkedHashMap<String, Object>> mapsArrayOut = new ArrayList<LinkedHashMap<String, Object>>();

				for (Map<String, Object> mapElement : mapsArray) {
					LinkedHashMap<String, Object> escapedArrayMapElement = escapeMapStrings(mapElement);

					mapsArrayOut.add(escapedArrayMapElement);
				}
				result.put(key, mapsArrayOut);

			} else if (!(mapElement2.get(key) instanceof Map)) { // if we found a tag <style/>
				if (key.equals(STYLE_TAG)) {
					String value = (String) mapElement2.get(key);

					LinkedHashMap<String, String> changedValue = stylizeString(value);
					result.put(key, changedValue);
				} else {
					Object obj = mapElement2.get(key);
					if (obj == null) {
						obj = 0;
					}
					String escapedString = obj.toString();
					result.put(key, escapedString);
				}

			} else { // in caso of simple node we call it recoursively
				@SuppressWarnings("unchecked")
				Map<String, Object> value = escapeMapStrings((Map<String, Object>) mapElement2.get(key));
				result.put(key, value);
			}
		}

		return result;
	}

	private static LinkedHashMap<String, String> stylizeString(String value) {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

		String[] styles = value.split(STYLES_SEPARATOR);

		for (String styleKV : styles) {
			String[] kv = styleKV.split(STYLE_KEY_VALUE_SEPARATOR);
			result.put(kv[0], kv.length > 1 ? kv[1] : "");
		}

		return result;
	}

	public static LinkedHashMap<String, String> stylizeMapString(String value) {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

		String[] styles = value.split(MAP_STYLES_SEPARATOR);

		for (String styleKV : styles) {
			String[] kv = styleKV.split(MAP_STYLE_KEY_VALUE_SEPARATOR);
			result.put(kv[0], kv.length > 1 ? kv[1] : "");
		}

		return result;
	}

	public static String applyTemplate(Template velocityTemplate, VelocityContext velocityContext) {
		StringWriter jsonChartTemplate = new StringWriter();
		velocityTemplate.merge(velocityContext, jsonChartTemplate);
		return jsonChartTemplate.toString();
	}

	public static JSONObject cockpitSelectionsFromAssociations(HttpServletRequest request, String selections, String associationGroup, String dataset) {

		JSONObject result = new JSONObject();

		try {

			AssociationGroup associationGroupObject = null;
			try {
				AssociationGroupJSONSerializer serializer = new AssociationGroupJSONSerializer();
				associationGroupObject = serializer.deserialize(new JSONObject(associationGroup));
			} catch (Throwable t) {
				throw new SpagoBIServiceParameterException(request.getPathInfo(),
						"Parameter [associationGroup] value [" + associationGroup + "] is not a valid JSON object", t);
			}

			JSONObject selectionsJSON = new JSONObject(selections);

			Iterator<String> it = selectionsJSON.keys();
			while (it.hasNext()) {
				String associationName = it.next();
				List<String> valuesList = new ArrayList<String>();
				// TODO to check why sometimes
				// 'selectionsJSON.get(associationName)' is a json object
				if (selectionsJSON.get(associationName) instanceof JSONObject) {
					JSONObject obj = selectionsJSON.getJSONObject(associationName);
					for (Iterator<String> iterator = obj.keys(); iterator.hasNext();) {
						String key = iterator.next();
						valuesList.add(obj.get(key).toString());
					}
				} else {
					JSONArray values = selectionsJSON.getJSONArray(associationName);
					for (int i = 0; i < values.length(); i++) {
						valuesList.add(values.get(i).toString());
					}
				}
				if (valuesList.isEmpty()) {
					continue;
				}

				String datasetColumn = null;
				String datasetLabel = null;
				Association association = associationGroupObject.getAssociation(associationName);

				if (association != null) {

					for (Field field : association.getFields()) {
						if (field.getLabel().equals(dataset)) {
							datasetColumn = field.getFieldName();
							datasetLabel = field.getLabel();
						}
					}
				}
				if (datasetLabel != null && datasetColumn != null) {
					result.put(datasetLabel, new JSONObject().put(datasetColumn, valuesList));
				}
			}

		} catch (Throwable t) {
			throw new SpagoBIServiceException(request.getPathInfo(), "An unexpected error occured while executing service", t);
		}

		return result;
	}

	public static String roundTo(double value, int nDigits) {
		double wholeValue = value * Math.pow(10, nDigits);

		wholeValue = Math.round(wholeValue);

		double resultValue = wholeValue / Math.pow(10, nDigits);

		String result = String.format(Locale.US, "%." + nDigits + "f", resultValue);

		return result;
	}

	public static String roundTo(String value, String nDigits) {
		Double doubleValue = new Double(value);
		Integer numberOfDecimals = new Integer(nDigits);

		String result = roundTo(doubleValue.doubleValue(), numberOfDecimals.intValue());
		return result;
	}

	public static String roundTo(Double value, String nDigits) {
		Integer numberOfDecimals = new Integer(nDigits);

		String result = roundTo(value, numberOfDecimals.intValue());
		return result;
	}

	public static String roundTo(String value) {
		String result = roundTo(new Double(value), 1);
		return result;
	}

	public static Double roundToDouble(String value, String nDigits) {
		Double result = new Double(roundTo(value, nDigits));
		return result;
	}

	/**
	 * This method converts the CSS format "#xxxxxx" (where "x" is a hexadecimal digit [0-F]) color returning the equivalent format "rgba(r, g, b, o)".
	 *
	 * @param colorStr
	 * @param opacity
	 * @return css color in "rgba(r, g, b, o)" format
	 */
	public static String hex2Rgb(String colorStr, String opacity) {
		String inputColor = colorStr.replace("#", "");
		String inputOpacity = opacity != null ? opacity : "100";

		float floatOpacity = Float.parseFloat(inputOpacity);

		Integer redValue = Integer.valueOf(inputColor.substring(0, 2), 16);
		Integer greenValue = Integer.valueOf(inputColor.substring(2, 4), 16);
		Integer blueValue = Integer.valueOf(inputColor.substring(4, 6), 16);

		String result = "rgba(" + redValue.intValue() + "," + greenValue.intValue() + "," + blueValue.intValue() + "," + (floatOpacity / 100) + ")";

		return result;
	}

	public static String hex2Rgb(String colorStr, Float opacity) {
		return hex2Rgb(colorStr, opacity.toString());
	}

	public static String hex2Rgb(String colorStr) {
		return hex2Rgb(colorStr, "100");
	}

	public static String replaceParameters(String jsonTemplate, Map analyticalDrivers) {
		Iterator it = analyticalDrivers.keySet().iterator();
		String updatedTemplate = jsonTemplate;
		while (it.hasNext()) {
			Object current = it.next();
			Object temp = analyticalDrivers.get(current.toString());
			String pattern = "$P{" + current.toString() + "}";

			if (updatedTemplate.contains(pattern)) {
				if (temp != null) {
					updatedTemplate = updatedTemplate.replace(pattern, temp.toString());
				}

			}

		}

		return updatedTemplate;
	}
}