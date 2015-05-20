package it.eng.spagobi.engine.chart.api;

import static it.eng.spagobi.engine.util.ChartEngineUtil.ve;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engine.util.ChartEngineDataUtil;
import it.eng.spagobi.engine.util.ChartEngineUtil;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
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
	@Path("/readChartTemplate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@SuppressWarnings("rawtypes")
	public String getJSONChartTemplate(@FormParam("jsonTemplate") String jsonTemplate, @Context HttpServletResponse servletResponse) {
		try {
			IDataSet dataSet = getEngineInstance().getDataSet();
			Map analyticalDrivers = getEngineInstance().getAnalyticalDrivers();
			Map profileAttributes = UserProfileUtils.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE));

			String jsonData = ChartEngineDataUtil.loadJsonData(jsonTemplate, dataSet, analyticalDrivers, profileAttributes, getLocale());

			VelocityContext velocityContext = ChartEngineUtil.loadVelocityContext(jsonTemplate, jsonData);
			String chartType = ChartEngineUtil.extractChartType(jsonTemplate, velocityContext);
			Template velocityTemplate = ve.getTemplate(ChartEngineUtil.getVelocityModelPath(chartType));
			return applyTemplate(velocityTemplate, velocityContext);

		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(),
					"An unexpected error occured while executing service: JsonChartTemplateService.getJSONChartTemplate", t);
		}
	}

	@GET
	@Path("/fieldsMetadata")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDatasetMetadata() {
		IDataSet dataSet = getEngineInstance().getDataSet();
		return ChartEngineDataUtil.loadMetaData(dataSet);
	}

	private String applyTemplate(Template velocityTemplate, VelocityContext velocityContext) {
		StringWriter jsonChartTemplate = new StringWriter();
		velocityTemplate.merge(velocityContext, jsonChartTemplate);
		return jsonChartTemplate.toString();
	}

}