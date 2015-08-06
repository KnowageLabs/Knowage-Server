package it.eng.spagobi.engine.chart.api;

import static it.eng.spagobi.engine.util.ChartEngineUtil.ve;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engine.chart.ChartEngine;
import it.eng.spagobi.engine.chart.ChartEngineInstance;
import it.eng.spagobi.engine.util.ChartEngineDataUtil;
import it.eng.spagobi.engine.util.ChartEngineUtil;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/1.0/jsonChartTemplate")
public class JsonChartTemplateService extends AbstractChartEngineResource {

	@POST
	@Path("/readChartTemplate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@SuppressWarnings("rawtypes")
	public String getJSONChartTemplate(@FormParam("jsonTemplate") String jsonTemplate, @FormParam("driverParams") String driverParams,
			@FormParam("jsonData") String jsonData, @Context HttpServletResponse servletResponse) {
		try {
			ChartEngineInstance engineInstance = getEngineInstance();
			IDataSet dataSet = engineInstance.getDataSet();
			Map analyticalDrivers = engineInstance.getAnalyticalDrivers();
			if (driverParams != null && !driverParams.isEmpty()) {
				refreshDriverParams(analyticalDrivers, driverParams);
				engineInstance = ChartEngine.createInstance(jsonTemplate, getEngineInstance().getEnv());
			}
			Map profileAttributes = UserProfileUtils.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE));

			if (StringUtilities.isEmpty(jsonData)) {
				jsonData = ChartEngineDataUtil.loadJsonData(jsonTemplate, dataSet, analyticalDrivers, profileAttributes, getLocale());
			}

			VelocityContext velocityContext = ChartEngineUtil.loadVelocityContext(jsonTemplate, jsonData);
			String chartType = ChartEngineUtil.extractChartType(jsonTemplate, velocityContext);
			Template velocityTemplate = ve.getTemplate(ChartEngineUtil.getVelocityModelPath(chartType));
			return ChartEngineUtil.applyTemplate(velocityTemplate, velocityContext);

		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(),
					"An unexpected error occured while executing service: JsonChartTemplateService.getJSONChartTemplate", t);
		}
	}

	private void refreshDriverParams(Map analyticalDrivers, String driverParams) throws JSONException {
		if (driverParams != null && !driverParams.trim().isEmpty() && analyticalDrivers != null && !analyticalDrivers.isEmpty()) {
			JSONObject params = new JSONObject(driverParams);
			for (Object key : analyticalDrivers.keySet()) {
				Object newValue = params.opt((String) key);
				if (newValue instanceof JSONArray) {
					newValue = ((JSONArray) newValue).get(0);
				}
				if (newValue != null) {
					analyticalDrivers.put(key, newValue);
				}
			}
		}
	}

	@POST
	@Path("/drilldownHighchart")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@SuppressWarnings("rawtypes")
	public String drilldownHighchart(@FormParam("jsonTemplate") String jsonTemplate, @FormParam("breadcrumb") String breadcrumb) {
		try {
			IDataSet dataSet = getEngineInstance().getDataSet();
			Map analyticalDrivers = getEngineInstance().getAnalyticalDrivers();
			Map profileAttributes = UserProfileUtils.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE));
			return ChartEngineDataUtil.drilldown(jsonTemplate, breadcrumb, dataSet, analyticalDrivers, profileAttributes, getLocale());
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(),
					"An unexpected error occured while executing service: JsonChartTemplateService.drilldownHighchart", t);
		}
	}

	@GET
	@Path("/fieldsMetadata")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDatasetMetadata() {
		IDataSet dataSet = getEngineInstance().getDataSet();
		return ChartEngineDataUtil.loadMetaData(dataSet);
	}

}