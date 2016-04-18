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
package it.eng.spagobi.engine.chart.api;

import static it.eng.spagobi.engine.util.ChartEngineUtil.ve;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engine.chart.ChartEngine;
import it.eng.spagobi.engine.chart.ChartEngineInstance;
import it.eng.spagobi.engine.util.ChartEngineDataUtil;
import it.eng.spagobi.engine.util.ChartEngineUtil;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
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

	/**
	 * We are sending additional information about the web application from which we call the VM. This boolean will tell us if we are coming from the Highcharts
	 * Export web application. The value of "exportWebApp" input parameter contains this boolean. This information is useful when we have drilldown, i.e. more
	 * than one category for the Highcharts chart (BAR, LINE).
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 *
	 * @param jsonTemplate
	 * @param exportWebApp
	 *            Needed for the VM, since for Export web application we do not need some properties in it (the VM)
	 *
	 * @param driverParams
	 * @param jsonData
	 * @param servletResponse
	 * @return
	 */
	@POST
	@Path("/readChartTemplate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@SuppressWarnings("rawtypes")
	public String getJSONChartTemplate(@FormParam("jsonTemplate") String jsonTemplate, @FormParam("exportWebApp") String exportWebApp,
			@FormParam("datasetLabel") String datasetLabel, @FormParam("driverParams") String driverParams, @FormParam("jsonData") String jsonData,
			@Context HttpServletResponse servletResponse) {
		try {
			ChartEngineInstance engineInstance = getEngineInstance();
			IDataSet dataSet = engineInstance.getDataSet();

			/*
			 * https://production.eng.it/jira/browse/KNOWAGE-581 if the dataset is null and we have datasetlabel valorized, probabily we are calling this REST
			 * service from the cockpit. So, we need to get the dataset from his label
			 */
			if (dataSet == null && datasetLabel != null) {
				IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
				dataSet = dataSetDao.loadDataSetByLabel(datasetLabel);
			}
			Map analyticalDrivers = engineInstance.getAnalyticalDrivers();
			if (driverParams != null && !driverParams.isEmpty()) {
				refreshDriverParams(analyticalDrivers, driverParams);
				engineInstance = ChartEngine.createInstance(jsonTemplate, getEngineInstance().getEnv());
			}
			Map profileAttributes = UserProfileUtils.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE));

			if (StringUtilities.isEmpty(jsonData)) {
				jsonData = ChartEngineDataUtil.loadJsonData(jsonTemplate, dataSet, analyticalDrivers, profileAttributes, getLocale());
			}

			VelocityContext velocityContext = ChartEngineUtil.loadVelocityContext(jsonTemplate, jsonData, Boolean.parseBoolean(exportWebApp),
					engineInstance.getDocumentLabel(), getEngineInstance().getUserProfile());
			String chartType = ChartEngineUtil.extractChartType(jsonTemplate, velocityContext);
			Template velocityTemplate = ve.getTemplate(ChartEngineUtil.getVelocityModelPath(chartType));
			String jsonChartTemplate = ChartEngineUtil.applyTemplate(velocityTemplate, velocityContext);
			jsonChartTemplate = ChartEngineUtil.replaceParameters(jsonChartTemplate, analyticalDrivers);

			return jsonChartTemplate;

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
			return ChartEngineDataUtil.drilldown(jsonTemplate, breadcrumb, dataSet, analyticalDrivers, profileAttributes, getLocale(), getEngineInstance()
					.getDocumentLabel(), getEngineInstance().getUserProfile());
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