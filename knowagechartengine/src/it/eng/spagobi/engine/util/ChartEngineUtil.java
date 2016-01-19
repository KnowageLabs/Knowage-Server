package it.eng.spagobi.engine.util;

import it.eng.spagobi.engine.chart.ChartEngineConfig;
import it.eng.spagobi.engine.chart.model.conf.ChartConfig;
import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.Association.Field;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroupJSONSerializer;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceParameterException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChartEngineUtil {

	private static final String STYLE_TAG = "style";
	private static final String STYLES_SEPARATOR = ";";
	private static final String STYLE_KEY_VALUE_SEPARATOR = ":";

	public static transient Logger logger = Logger.getLogger(ChartEngineUtil.class);

	public static VelocityEngine ve;

	static {
		ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "true");
		ve.init();
	}

	public static String getLibraryInitializerPath(String jsonTemplateFromXML) {
		String chartType = extractChartType(jsonTemplateFromXML);
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

	private static String extractChartType(String jsonTemplate) {
		return extractChartType(jsonTemplate, loadVelocityContext(jsonTemplate));
	}

	public static String extractChartType(String jsonTemplate, VelocityContext velocityContext) {
		Template velocityTemplate = ve.getTemplate("/chart/templates/get_chart_type.vm");
		StringWriter chartType = new StringWriter();
		velocityTemplate.merge(velocityContext, chartType);

		String chartTypeString = chartType.toString();
		velocityContext.put("chartType", chartTypeString);
		return chartTypeString;
	}

	public static VelocityContext loadVelocityContext(String jsonToConvert) {
		return loadVelocityContext(jsonToConvert, null, false);
	}

	/**
	 * We are sending additional information about the web application from which we call the VM. This boolean will tell us if we are coming from the Highcharts
	 * Export web application. The value of "exportWebApp" input parameter contains this boolean. This information is useful when we have drilldown, i.e. more
	 * than one category for the Highcharts chart (BAR, LINE).
	 * 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	public static VelocityContext loadVelocityContext(String jsonToConvert, String jsonData, boolean exportWebApp) {
		VelocityContext velocityContext = new VelocityContext();

		Map<String, Object> mapTemplate = null;
		Map<String, Object> mapData = null;
		try {
			velocityContext.put("datasettransformer", new DataSetTransformer());
			if (jsonToConvert != null) {
				mapTemplate = convertJsonToMap(jsonToConvert, true);
				velocityContext.put("chart", mapTemplate.get("chart") != null ? mapTemplate.get("chart") : mapTemplate.get("CHART"));
			}
			if (jsonData != null) {
				mapData = convertJsonToMap(jsonData, false);
				velocityContext.put("data", mapData);
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

	private static Map<String, Object> convertJsonToMap(String json, boolean escape) throws JsonParseException, JsonMappingException, IOException {
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);

		TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
		};

		// TODO Aggiungere a questo livello StringEscapeUtils.escapeHtml per lettere
		Map<String, Object> result = mapper.readValue(json, typeRef);

		Map<String, Object> escapedMapStrings = escape ? escapeMapStrings(result) : result;

		// return result;
		return escapedMapStrings;
	}

	/**
	 *
	 * */
	private static LinkedHashMap<String, Object> escapeMapStrings(Map<String, Object> mapElement2) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

		for (String key : mapElement2.keySet()) {
			if (mapElement2.get(key) instanceof List) { // Se il tag individuato e' un array
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> mapsArray = (List<Map<String, Object>>) mapElement2.get(key);

				ArrayList<LinkedHashMap<String, Object>> mapsArrayOut = new ArrayList<LinkedHashMap<String, Object>>();

				for (Map<String, Object> mapElement : mapsArray) {
					LinkedHashMap<String, Object> escapedArrayMapElement = escapeMapStrings(mapElement);

					mapsArrayOut.add(escapedArrayMapElement);
				}
				result.put(key, mapsArrayOut);

			} else if (!(mapElement2.get(key) instanceof Map)) { // Se viene individuato un tag <style/>
				if (key.equals(STYLE_TAG)) {
					String value = (String) mapElement2.get(key);

					LinkedHashMap<String, String> changedValue = stylizeString(value);
					result.put(key, changedValue);
				} else {
					String escapedString = mapElement2.get(key).toString();
					result.put(key, escapedString);
				}

			} else { // Nel caso è un semplice nodo viene chiamata la funzione ricorsivamente
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
				throw new SpagoBIServiceParameterException(request.getPathInfo(), "Parameter [associationGroup] value [" + associationGroup
						+ "] is not a valid JSON object", t);
			}

			JSONObject selectionsJSON = new JSONObject(selections);

			Iterator<String> it = selectionsJSON.keys();
			while (it.hasNext()) {
				String associationName = it.next();
				List<String> valuesList = new ArrayList<String>();
				// TODO to check why sometimes 'selectionsJSON.get(associationName)' is a json object
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
						if (field.getDataSetLabel().equals(dataset)) {
							datasetColumn = field.getFieldName();
							datasetLabel = field.getDataSetLabel();
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

}