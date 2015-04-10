package it.eng.spagobi.engine.chart.api;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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

	// @POST
	// @Produces(MediaType.APPLICATION_JSON)
	// @Consumes(MediaType.APPLICATION_JSON)
	// public String doPost(@FormParam("chartType") String chartType, @FormParam("jsonTemplate") String jsonTemplate, @Context HttpServletResponse
	// servletResponse) {
	// VelocityContext velocityContext = loadVelocityContext(jsonTemplate);
	// Template velocityTemplate = loadVelocityTemplate(chartType);
	// String jsonChartTemplate = applyTemplate(velocityTemplate, velocityContext);
	// return jsonChartTemplate;
	// }

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

		LinkedHashMap<String, Object> result = mapper.readValue(json, typeRef);

		return result;
	}
}