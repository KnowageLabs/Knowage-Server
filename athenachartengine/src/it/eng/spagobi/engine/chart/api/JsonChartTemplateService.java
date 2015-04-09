package it.eng.spagobi.engine.chart.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/1.0/jsonChartTemplate")
public class JsonChartTemplateService extends AbstractChartEngineResource {

	@GET
	@Path("/{jsonTemplate}")
	@Produces(MediaType.APPLICATION_JSON)
	public String openPage(@PathParam("jsonTemplate") String jsonTemplate) {
		return jsonTemplate;
	}
}