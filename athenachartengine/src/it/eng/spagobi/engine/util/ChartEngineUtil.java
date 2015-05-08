package it.eng.spagobi.engine.util;

import it.eng.spagobi.engine.chart.ChartEngineConfig;
import it.eng.spagobi.engine.chart.model.conf.ChartConfig;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
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
		return chartConfig.getLibraryInitializerPath();
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
		return loadVelocityContext(jsonToConvert, null);
	}

	public static VelocityContext loadVelocityContext(String jsonToConvert, String jsonData) {
		VelocityContext velocityContext = new VelocityContext();
		
		Map<String, Object> mapTemplate = null;
		Map<String, Object> mapData = null;
		try {
			mapTemplate = convertJsonToMap(jsonToConvert, true);
			velocityContext.put("datasettransformer", new DataSetTransformer());
			velocityContext.put("chart", mapTemplate.get("chart")!=null ? mapTemplate.get("chart") : mapTemplate.get("CHART"));
			if (jsonData != null) {
				mapData = convertJsonToMap(jsonData, false);
				velocityContext.put("data", mapData);
			}
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

					continue;
				}

				String escapedString = StringEscapeUtils.escapeHtml(mapElement2.get(key).toString());

				result.put(key, escapedString);
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
			result.put(kv[0], kv[1]);
		}

		return result;
	}
}
