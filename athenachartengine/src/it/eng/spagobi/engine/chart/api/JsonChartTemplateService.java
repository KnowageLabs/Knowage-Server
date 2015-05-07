package it.eng.spagobi.engine.chart.api;

import static it.eng.spagobi.engine.util.ChartEngineUtil.ve;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engine.chart.ChartEngineInstance;
import it.eng.spagobi.engine.util.ChartEngineUtil;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.io.StringWriter;
import java.util.Map;

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
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@Path("/1.0/jsonChartTemplate")
public class JsonChartTemplateService extends AbstractChartEngineResource {

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String doPost(@FormParam("jsonTemplate") String jsonTemplate, @Context HttpServletResponse servletResponse) {

		String jsonData = loadJsonData();

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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String loadJsonData() {
		ChartEngineInstance engineInstance = getEngineInstance();
		IDataSet dataSet = engineInstance.getDataSet();
		IDataStore dataStore;

		Map params = engineInstance.getAnalyticalDrivers();
		params.put("LOCALE", getLocale());
		dataSet.setParamsMap(params);
		dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE)));
		Monitor monitorLD = MonitorFactory.start("SpagoBI_Chart.GetChartDataAction.service.LoadData");

		dataSet.loadData();// start, limit, rowsLimit);

		monitorLD.stop();
		dataStore = dataSet.getDataStore();

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
}