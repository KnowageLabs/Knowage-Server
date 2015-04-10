package it.eng.spagobi.engine.chart.api;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringEscapeUtils;
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

@Path("/1.0/jsonChartTemplate")
public class JsonChartTemplateService extends AbstractChartEngineResource {

	private static final String STYLE_TAG = "style";
	private static final String STYLES_SEPARATOR = ";";
	private static final String STYLE_KEY_VALUE_SEPARATOR = ":";

	private VelocityEngine ve;
	{
		this.ve = new VelocityEngine();
		this.ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		this.ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		this.ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "true");
		this.ve.init();
	}

	@GET
	@Path("/{chartType}")
	@Produces(MediaType.APPLICATION_JSON)
	public String openPage(@PathParam("chartType") String chartType) {
		VelocityContext velocityContext = loadVelocityContext(request);
		Template velocityTemplate = loadVelocityTemplate(chartType);
		String jsonChartTemplate = applyTemplate(velocityTemplate, velocityContext);
		return jsonChartTemplate;
	}

	private VelocityContext loadVelocityContext(HttpServletRequest request) {
		VelocityContext velocityContext = new VelocityContext();

		String jsonToConvert = request.getParameter("jsonTemplate");

		Map<String, Object> mapTemplate = null;
		try {
			mapTemplate = convertJsonToMap(jsonToConvert);
		} catch (IOException e) {
			logger.error("Error in template to be converted: " + jsonToConvert);
			e.printStackTrace();
		}

		// velocityContext.put("template", mapTemplate);
		velocityContext.put("chart", mapTemplate.get("CHART")); // livello <CHART/>
		return velocityContext;
	}

	// TODO externalise file path
	private Template loadVelocityTemplate(String chartType) {
		Template velocityTemplate;
		switch (chartType) {
		case "bar":
			velocityTemplate = ve.getTemplate("/chart/templates/highcharts/bar_chart.vm");
			break;
		default:
			logger.error("Unsupported chart type: " + chartType);
			throw new RuntimeException("Unsupported chart type: " + chartType);
		}
		return velocityTemplate;
	}

	private String applyTemplate(Template velocityTemplate, VelocityContext velocityContext) {
		StringWriter jsonChartTemplate = new StringWriter();
		velocityTemplate.merge(velocityContext, jsonChartTemplate);
		return jsonChartTemplate.toString();
	}

	private Map<String, Object> convertJsonToMap(String json) throws JsonParseException, JsonMappingException, IOException {
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);

		TypeReference<LinkedHashMap<String, Object>> typeRef = new TypeReference<LinkedHashMap<String, Object>>() {
		};

		// TODO Aggiungere a questo livello StringEscapeUtils.escapeHtml per lettere
		LinkedHashMap<String, Object> result = mapper.readValue(json, typeRef);

		LinkedHashMap<String, Object> escapedMapStrings = escapeMapStrings(result);

		// return result;
		return escapedMapStrings;
	}

	private LinkedHashMap<String, Object> escapeMapStrings(LinkedHashMap<String, Object> map) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

		for (String key : map.keySet()) {

			if (map.get(key) instanceof ArrayList) {
				ArrayList<LinkedHashMap<String, Object>> mapsArray = (ArrayList<LinkedHashMap<String, Object>>) map.get(key);

				ArrayList<LinkedHashMap<String, Object>> mapsArrayOut = new ArrayList<LinkedHashMap<String, Object>>();

				for (LinkedHashMap<String, Object> mapElement : mapsArray) {
					LinkedHashMap<String, Object> escapedArrayMapElement = escapeMapStrings(mapElement);

					mapsArrayOut.add(escapedArrayMapElement);
				}
				result.put(key, mapsArrayOut);

			} else if (!(map.get(key) instanceof LinkedHashMap)) {
				if (key.equals(STYLE_TAG)) {
					String value = (String) map.get(key);

					LinkedHashMap<String, String> changedValue = stylizeString(value);
					result.put(key, changedValue);

					continue;
				}

				String escapedString = StringEscapeUtils.escapeHtml(map.get(key).toString());

				result.put(key, escapedString);
			} else {
				LinkedHashMap<String, Object> value = escapeMapStrings((LinkedHashMap<String, Object>) map.get(key));
				result.put(key, value);
			}
		}

		return result;
	}

	private LinkedHashMap<String, String> stylizeString(String value) {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

		String[] styles = value.split(STYLES_SEPARATOR);

		for (String styleKV : styles) {
			String[] kv = styleKV.split(STYLE_KEY_VALUE_SEPARATOR);
			result.put(kv[0], kv[1]);
		}

		return result;
	}
}