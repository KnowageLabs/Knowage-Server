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

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import it.eng.knowage.engine.cockpit.api.CockpitExecutionClient;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engine.chart.ChartEngine;
import it.eng.spagobi.engine.chart.ChartEngineInstance;
import it.eng.spagobi.engine.chart.util.ChartEngineDataUtil;
import it.eng.spagobi.engine.chart.util.ChartEngineUtil;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

import static it.eng.spagobi.engine.chart.util.ChartEngineUtil.ve;

@Path("/1.0/chart/jsonChartTemplate")
public class JsonChartTemplateService extends AbstractChartEngineResource {
    static protected Logger logger = Logger.getLogger(JsonChartTemplateService.class);

    /**
     * We are sending additional information about the web application from which we call the VM. This boolean will tell us if we are coming from the Highcharts
     * Export web application. The value of "exportWebApp" input parameter contains this boolean. This information is useful when we have drilldown, i.e. more
     * than one category for the Highcharts chart (BAR, LINE).
     *
     * @param jsonTemplate
     * @param exportWebApp    Needed for the VM, since for Export web application we do not need some properties in it (the VM)
     * @param driverParams
     * @param jsonData
     * @param servletResponse
     * @return
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    @POST
    @Path("/readChartTemplate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED + "; charset=UTF-8")
    @SuppressWarnings("rawtypes")
    @UserConstraint(functionalities = {SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY})
    public String getJSONChartTemplate(@FormParam("jsonTemplate") String jsonTemplate, @FormParam("exportWebApp") String exportWebApp,
                                       @FormParam("datasetLabel") String datasetLabel, @FormParam("driverParams") String driverParams, @FormParam("jsonData") String jsonData,
                                       @Context HttpServletResponse servletResponse) {
        logger.debug("IN");
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
            logger.debug("jsonData :" + jsonData);
            VelocityContext velocityContext = ChartEngineUtil.loadVelocityContext(jsonTemplate, jsonData, Boolean.parseBoolean(exportWebApp),
                    engineInstance.getDocumentLabel(), getEngineInstance().getUserProfile());
            String chartType = ChartEngineUtil.extractChartType(jsonTemplate, velocityContext);
            Template velocityTemplate = ve.getTemplate(ChartEngineUtil.getVelocityModelPath(chartType), "UTF-8");
            String jsonChartTemplate = ChartEngineUtil.applyTemplate(velocityTemplate, velocityContext);
            jsonChartTemplate = ChartEngineUtil.replaceParameters(jsonChartTemplate, analyticalDrivers);
            logger.debug("jsonChartTemplate " + jsonChartTemplate);
            logger.debug("jsonChartTemplate " + jsonChartTemplate.trim());
            // @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
            return jsonChartTemplate.trim();

        } catch (Throwable t) {
            logger.error("An unexpected error occured while executing service: JsonChartTemplateService.getJSONChartTemplate", t);
            throw new SpagoBIServiceException(this.request.getPathInfo(),
                    "An unexpected error occured while executing service: JsonChartTemplateService.getJSONChartTemplate", t);
        } finally {
            logger.debug("OUT");
        }
    }

    @POST
    @Path("/readChartTemplateForCockpit")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED + "; charset=UTF-8")
    @SuppressWarnings("rawtypes")
    @UserConstraint(functionalities = {SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY})
    public String getJSONChartTemplateForCockpit(@FormParam("jsonTemplate") String jsonTemplate, @FormParam("exportWebApp") String exportWebApp,
                                                 @FormParam("jsonData") String jsonData, @Context HttpServletResponse servletResponse) {
        logger.debug("IN");
        IDataSet dataSet = null;
        Map analyticalDrivers = new HashMap<>();
        Map profileAttributes = new HashMap<>();
        try {

            /*
             * https://production.eng.it/jira/browse/KNOWAGE-581 if the dataset is null and we have datasetlabel valorized, probabily we are calling this REST
             * service from the cockpit. So, we need to get the dataset from his label
             */

            if (StringUtilities.isEmpty(jsonData)) {
                jsonData = ChartEngineDataUtil.loadJsonData(jsonTemplate, dataSet, analyticalDrivers, profileAttributes, null);
            }
            logger.debug("jsonData :" + jsonData);
            VelocityContext velocityContext = ChartEngineUtil.loadVelocityContext(jsonTemplate, jsonData, Boolean.parseBoolean(exportWebApp), null,
                    getIOManager().getUserProfile());
            String chartType = ChartEngineUtil.extractChartType(jsonTemplate, velocityContext);
            Template velocityTemplate = ve.getTemplate(ChartEngineUtil.getVelocityModelPath(chartType), "UTF-8");
            String jsonChartTemplate = ChartEngineUtil.applyTemplate(velocityTemplate, velocityContext);
            logger.debug(jsonChartTemplate);
            logger.debug(jsonChartTemplate.trim());
            // @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
            return jsonChartTemplate.trim();

        } catch (Throwable t) {
            logger.error("An unexpected error occured while executing service: JsonChartTemplateService.getJSONChartTemplateForCockpit", t);
            throw new SpagoBIServiceException(this.request.getPathInfo(),
                    "An unexpected error occured while executing service: JsonChartTemplateService.getJSONChartTemplateForCockpit", t);
        } finally {
            logger.debug("OUT");
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
    @UserConstraint(functionalities = {SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY})
    public String drilldownHighchart(@FormParam("breadcrumb") String breadcrumb) {
        logger.debug("IN");
        try {

            IDataSet dataSet = getEngineInstance().getDataSet();
            Map analyticalDrivers = getEngineInstance().getAnalyticalDrivers();
            Map profileAttributes = UserProfileUtils.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE));
            String jsonTemplate = getEngineInstance().getTemplate().toString();
            logger.debug("jsonTemplate " + jsonTemplate);
            return ChartEngineDataUtil.drilldown(jsonTemplate, breadcrumb, dataSet, analyticalDrivers, profileAttributes, getLocale(),
                    getEngineInstance().getDocumentLabel(), getEngineInstance().getUserProfile(), "", "", "", null);
        } catch (Throwable t) {
            logger.error("An unexpected error occured while executing service: JsonChartTemplateService.drilldownHighchart", t);
            throw new SpagoBIServiceException(this.request.getPathInfo(),
                    "An unexpected error occured while executing service: JsonChartTemplateService.drilldownHighchart", t);
        } finally {
            logger.debug("OUT");
        }
    }

    @POST
    @Path("/drilldownHighchartForCockpit")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @SuppressWarnings("rawtypes")
    @UserConstraint(functionalities = {SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY})
    public String drilldownHighchartForCockpit(@FormParam("breadcrumb") String breadcrumb, @FormParam("widgetData") String widgetData,
                                               @FormParam("selections") String selections, @FormParam("parameters") String parameters, @FormParam("aggregations") String aggregations,
                                               @DefaultValue("-1") @QueryParam("offset") int offset, @DefaultValue("-1") @QueryParam("size") int fetchSize,
                                               @QueryParam("nearRealtime") boolean isNearRealtime) {
        logger.debug("IN");
        logger.debug("Breadcrumb parameter value: " + breadcrumb);
        logger.debug("WidgetData parameter value: " + widgetData);
        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("offset", offset);
        queryParams.put("size", fetchSize);
        queryParams.put("nearRealtime", isNearRealtime);
        JSONObject widgetDataJson = null;
        String jsonTemplate = null;
        UserProfile userProfile = getIOManager().getUserProfile();
        IDataSet dataSet = null;
        IDataSetDAO dataSetDao = null;
        String datasetLabel = null;
        Map analyticalDrivers = new HashMap<>();
        Map profileAttributes = new HashMap<>();
        try {
            widgetDataJson = new JSONObject(widgetData);

            logger.debug("WidgetDataJson created!");
            datasetLabel = widgetDataJson.getString("datasetLabel");
            logger.debug("Property datasetLabel of object widgetDataJson is: " + datasetLabel);
            dataSetDao = DAOFactory.getDataSetDAO();
            logger.debug("Dataset dao object created!");
            dataSet = dataSetDao.loadDataSetByLabel(datasetLabel);
            logger.debug("Dataset with datasetLabel: " + datasetLabel + " is loaded!");
            jsonTemplate = widgetDataJson.getString("chartTemplate");
            logger.debug("Property chartTemplate of object widgetDataJson is: " + jsonTemplate);
            return ChartEngineDataUtil.drilldown(jsonTemplate, breadcrumb, dataSet, analyticalDrivers, profileAttributes, getIOManager().getLocale(), null,
                    userProfile, selections, aggregations, parameters, queryParams);

        } catch (JSONException e) {
            logger.error("Error creating json object from query param widgetData");
            throw new SpagoBIServiceException(this.request.getPathInfo(),
                    "An unexpected error occured while executing service: Error creating json object from query param widgetData", e);
        } catch (EMFUserError e) {
            logger.error("Error while creating dataset dao");
            throw new SpagoBIServiceException(this.request.getPathInfo(),
                    "An unexpected error occured while executing service: Error while creating dataset dao", e);
        } catch (Throwable e) {
            logger.error("Error while trying to drilldown");
            throw new SpagoBIServiceException(this.request.getPathInfo(),
                    "An unexpected error occured while executing service: Error while trying to drilldown", e);
        } finally {
            logger.debug("OUT");
        }

    }

    @GET
    @Path("/fieldsMetadata")
    @Produces(MediaType.APPLICATION_JSON)
    @UserConstraint(functionalities = {SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY})
    public String getDatasetMetadata() {
        logger.debug("IN");
        try {
            IDataSet dataSet = getEngineInstance().getDataSet();
            return ChartEngineDataUtil.loadMetaData(dataSet);
        } catch (Throwable t) {
            logger.error("An unexpected error occured while executing service: JsonChartTemplateService.getDatasetMetadata", t);
            throw new SpagoBIServiceException(this.request.getPathInfo(),
                    "An unexpected error occured while executing service: JsonChartTemplateService.getDatasetMetadata", t);
        } finally {
            logger.debug("OUT");
        }
    }

    @GET
    @Path("/fieldsMetadataforCockpit/{datasetId}")
    @Produces(MediaType.APPLICATION_JSON)
    @UserConstraint(functionalities = {SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY})
    public String getDatasetMetadataForCockpit(@PathParam("datasetId") String datasetId) {
        logger.debug("IN");
        IDataSet dataSet = null;
        IDataSetDAO dataSetDao = null;
        try {
            if (datasetId != null) {
                dataSetDao = DAOFactory.getDataSetDAO();
                dataSet = dataSetDao.loadDataSetById(Integer.parseInt(datasetId));
                return ChartEngineDataUtil.loadMetaData(dataSet);
            } else {
                logger.error("JsonChartTemplateService.fieldsMetadataforCockpit : datasetID is null");
                throw new SpagoBIServiceException(this.request.getPathInfo(), "JsonChartTemplateService.fieldsMetadataforCockpit : datasetID is null");
            }
        } catch (Throwable t) {
            logger.error("An unexpected error occured while executing service: JsonChartTemplateService.fieldsMetadataforCockpit", t);
            throw new SpagoBIServiceException(this.request.getPathInfo(),
                    "An unexpected error occured while executing service: JsonChartTemplateService.fieldsMetadataforCockpit", t);
        } finally {
            logger.debug("OUT");
        }

    }

    @GET
    @Path("/usedDataset")
    @Produces(MediaType.APPLICATION_JSON)
    @UserConstraint(functionalities = {SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY})
    public String isDatasetRealTime() {
        logger.debug("IN");
        try {
            IDataSet dataSet = getEngineInstance().getDataSet();
            return String.valueOf(dataSet.isRealtime());
        } catch (Throwable t) {
            logger.error("An unexpected error occured while executing service: JsonChartTemplateService.isDatasetRealTime", t);
            throw new SpagoBIServiceException(this.request.getPathInfo(),
                    "An unexpected error occured while executing service: JsonChartTemplateService.isDatasetRealTime", t);
        } finally {
            logger.debug("OUT");
        }
    }

    @GET
    @Path("/usedDataset/{datasetId}")
    @Produces(MediaType.APPLICATION_JSON)
    @UserConstraint(functionalities = {SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY})
    public String isDatasetForCockpitRealTime(@PathParam("datasetId") String datasetId) {
        logger.debug("IN");
        IDataSet dataSet = null;
        IDataSetDAO dataSetDao = null;
        try {
            if (datasetId != null) {
                dataSetDao = DAOFactory.getDataSetDAO();
                dataSet = dataSetDao.loadDataSetById(Integer.parseInt(datasetId));
                return String.valueOf(dataSet.isRealtime());
            } else {
                logger.error("JsonChartTemplateService.isDatasetForCockpitRealTime : datasetID is null");
                throw new SpagoBIServiceException(this.request.getPathInfo(), "JsonChartTemplateService.isDatasetForCockpitRealTime : datasetID is null");
            }
        } catch (Throwable t) {
            logger.error("An unexpected error occured while executing service: JsonChartTemplateService.isDatasetForCockpitRealTime", t);
            throw new SpagoBIServiceException(this.request.getPathInfo(),
                    "An unexpected error occured while executing service: JsonChartTemplateService.isDatasetForCockpitRealTime", t);
        } finally {
            logger.debug("OUT");
        }
    }

    @POST
    @Path("/{label}/getDataAndConf")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @SuppressWarnings("rawtypes")
    @UserConstraint(functionalities = {SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY})
    public String getDataAndConf(@PathParam("label") String label, String body, @Context HttpServletResponse servletResponse,
                                 @DefaultValue("-1") @QueryParam("offset") int offset, @DefaultValue("-1") @QueryParam("limit") int limit,
                                 @DefaultValue("-1") @QueryParam("size") int fetchSize, @QueryParam("nearRealtime") boolean isNearRealtime) {
        logger.debug("IN");
        Monitor getDataAndChartConfMonitor = MonitorFactory.start("Knowage.Cockpit.Chart.Get.Data.And.Chartconf");
        String jsonTemplate = null;
        String exportWebApp = null;
        String jsonData = null;
        String aggregations = null;
        String chartConf = null;
        JSONObject responseObject = new JSONObject();
        CockpitExecutionClient cockpitExecutionClient;
        String userId = (String) getUserProfile().getUserUniqueIdentifier();
        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("offset", offset);
        queryParams.put("size", fetchSize);
        queryParams.put("nearRealtime", isNearRealtime);
        queryParams.put("limit", limit);

        try {
            if (StringUtilities.isNotEmpty(body)) {
                JSONObject jsonBody = new JSONObject(body);
                JSONObject jsonChartTemp = jsonBody.optJSONObject("chartTemp");
                jsonTemplate = jsonChartTemp != null ? jsonChartTemp.toString() : null;
                aggregations = jsonBody.getString("aggregations");
                JSONObject jsonexportWebApp = jsonBody.optJSONObject("exportWebApp");
                exportWebApp = jsonexportWebApp != null ? jsonexportWebApp.toString() : null;
            }
            cockpitExecutionClient = new CockpitExecutionClient();
            Monitor getData = MonitorFactory.start("Knowage.Cockpit.Chart.Get.Data");
            jsonData = cockpitExecutionClient.getDataFromDataset(aggregations, label, userId, queryParams);
            getData.stop();
            responseObject.put("jsonData", jsonData);
            Monitor getChartConf = MonitorFactory.start("Knowage.Cockpit.Chart.Get.Chartconf");
            chartConf = getJSONChartTemplateForCockpit(jsonTemplate, exportWebApp, jsonData, servletResponse);
            getChartConf.stop();
            responseObject.put("chartConf", chartConf);

        } catch (Throwable t) {
            logger.error("An unexpected error occured while executing service: JsonChartTemplateService.getDataAndConf", t);
            throw new SpagoBIServiceException(this.request.getPathInfo(),
                    "An unexpected error occured while executing service: JsonChartTemplateService.getDataAndConf", t);
        } finally {
            logger.debug("OUT");
        }
        getDataAndChartConfMonitor.stop();
        return responseObject.toString();
    }

}