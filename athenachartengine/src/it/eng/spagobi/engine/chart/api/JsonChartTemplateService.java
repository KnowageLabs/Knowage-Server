package it.eng.spagobi.engine.chart.api;

import static it.eng.spagobi.engine.util.ChartEngineUtil.ve;
import it.eng.spagobi.engine.util.ChartEngineUtil;

import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

@Path("/1.0/jsonChartTemplate")
public class JsonChartTemplateService extends AbstractChartEngineResource {

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String doPost(@FormParam("jsonTemplate") String jsonTemplate, @FormParam("jsonData") String jsonData, @Context HttpServletResponse servletResponse) {
		VelocityContext velocityContext = ChartEngineUtil.loadVelocityContext(jsonTemplate, jsonData);
		String chartType = ChartEngineUtil.extractChartType(jsonTemplate, velocityContext);
		Template velocityTemplate = ve.getTemplate(ChartEngineUtil.getVelocityModelPath(chartType));
		return applyTemplate(velocityTemplate, velocityContext);
	}

	private String applyTemplate(Template velocityTemplate, VelocityContext velocityContext) {
		StringWriter jsonChartTemplate = new StringWriter();
		velocityTemplate.merge(velocityContext, jsonChartTemplate);
		return jsonChartTemplate.toString();
	}
}