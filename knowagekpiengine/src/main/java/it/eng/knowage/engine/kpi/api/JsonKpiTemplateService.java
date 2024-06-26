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
package it.eng.knowage.engine.kpi.api;

import it.eng.knowage.engine.kpi.KpiEngineInstance;
import it.eng.knowage.engine.util.KpiEngineDataUtil;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.KpiValue;
import it.eng.spagobi.kpi.dao.IKpiDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/1.0/jsonKpiTemplate")
@ManageAuthorization
public class JsonKpiTemplateService extends AbstractFullKpiEngineResource {

	private static Logger logger = Logger.getLogger(JsonKpiTemplateService.class);

	private TreeMap parameterMap = new TreeMap<>();

	public TreeMap getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(TreeMap parameterMap) {
		this.parameterMap = parameterMap;
	}

	@POST
	@Path("/readKpiTemplate")
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getJSONKpiTemplate(@Context HttpServletRequest req, @Context HttpServletResponse servletResponse) {
		logger.debug("IN");

		JSONObject toReturn;
		try {
			IKpiDAO kpiDAO = DAOFactory.getKpiDAO();
			kpiDAO.setUserProfile(getUserProfile());

			logger.debug("Getting the engine instance");
			KpiEngineInstance engineInstance = getEngineInstance();
			Assert.assertNotNull(engineInstance, "The engine instance is null. Impossible to continue with the current request.");

			logger.debug("Getting the analytical drivers from the engine instance");
			Map<String, String> analyticalDrivers = engineInstance.getAnalyticalDrivers();
			Set<String> keySet = analyticalDrivers.keySet();
			for (String parName : keySet) {
				String parValue = analyticalDrivers.get(parName);
				parameterMap.put(parName, parValue);
			}

			logger.debug("Getting profile attributes from UserProfile...");
			Map profileAttributes = UserProfileUtils.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE));
			JSONObject jsonTemplate = RestUtilities.readBodyAsJSONObject(req);
			Assert.assertNotNull(jsonTemplate, "Templace cannot be null.");

			logger.debug(jsonTemplate.toString(3));
			Calendar startDate = Calendar.getInstance();

			// Loading Scorecard or Target
			logger.debug("Getting attributes values");
			Map<String, String> attributesValues = buildAttributeValuesMap(jsonTemplate, startDate);
			logger.debug(attributesValues);

			JSONArray resultArray = KpiEngineDataUtil.loadJsonData(jsonTemplate, attributesValues);
			if (resultArray == null) {
				logger.debug("The resulting array is [null]. Returning [null] then.");
				return null;
			}
			JSONArray array = new JSONArray();
			if (jsonTemplate.getJSONObject("chart").getString("type").equals("kpi")) {
				for (int i = 0; i < resultArray.length(); i++) {
					JSONObject objTemp = resultArray.getJSONObject(i).getJSONObject("kpi");
					int kpiId = objTemp.getInt("id");
					logger.debug("Loading KPI with ID " + kpiId);
					Kpi kpi = kpiDAO.loadLastActiveKpi(kpiId);
					List<KpiValue> kpiValues = kpiDAO.findKpiValues(kpiId, kpi.getVersion(), startDate.getTime(), new Date(), attributesValues);
					String kpiValueJson = JsonConverter.objectToJson(kpiValues, kpiValues.getClass());
					array.put(new JSONArray(kpiValueJson));
				}
			}

			toReturn = new JSONObject();
			toReturn.put("loadKpiValue", array);
			toReturn.put("info", resultArray);
			return toReturn.toString();
		} catch (JSONException | IOException | EMFUserError ex) {
			throw new SpagoBIRuntimeException("Error while read KPI template", ex);
		} finally {
			logger.debug("OUT");
		}
	}

	Map<String, String> buildAttributeValuesMap(JSONObject jsonTemplate, Calendar startDate) throws JSONException {
		Map<String, String> attributesValues = new TreeMap<String, String>();
		if (jsonTemplate.getJSONObject("chart").getString("type").equals("kpi")) {
			String historycalSeries = jsonTemplate.getJSONObject("chart").getJSONObject("options").getJSONObject("history").getString("units");
			if (historycalSeries.equals("day")) {
				startDate.add(Calendar.DAY_OF_MONTH, -1);
			} else if (historycalSeries.equals("week")) {
				startDate.add(Calendar.DAY_OF_MONTH, -7);
			} else if (historycalSeries.equals("month")) {
				startDate.add(Calendar.MONTH, -1);
			} else if (historycalSeries.equals("quarter")) {
				startDate.add(Calendar.MONTH, -3);
			} else if (historycalSeries.equals("year")) {
				startDate.add(Calendar.YEAR, -1);
			}
		}

		String s = parameterMap.toString();
		String[] couples = s.replace("{", "").replace("}", "").split(",");
		for (String c : couples) {
			String[] keyvalue = c.split("=");
			if (keyvalue.length != 1) {
				attributesValues.put(keyvalue[0].trim(), keyvalue[1].trim());
			}

		}
		return attributesValues;
	}

}