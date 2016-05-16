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

import java.io.IOException;
import java.util.ArrayList;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.knowage.engine.kpi.KpiEngineInstance;
import it.eng.knowage.engine.util.KpiEngineDataUtil;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.kpi.bo.KpiValue;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/1.0/jsonKpiTemplate")
@ManageAuthorization
public class JsonKpiTemplateService extends AbstractFullKpiEngineResource {
	private TreeMap parameterMap = new TreeMap<>();

	public TreeMap getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(TreeMap parameterMap) {
		this.parameterMap = parameterMap;
	}

	@POST
	@Path("/readKpiTemplate")
	@SuppressWarnings("rawtypes")
	public String getJSONKpiTemplate(@Context HttpServletRequest req, @Context HttpServletResponse servletResponse) {
		JSONArray array = new JSONArray();
		try {
			String result = "";

			KpiEngineInstance engineInstance = getEngineInstance();

			@SuppressWarnings("unchecked")
			Map<String, String> analyticalDrivers = engineInstance.getAnalyticalDrivers();
			Set<String> keySet = analyticalDrivers.keySet();
			for (String parName : keySet) {
				String parValue = analyticalDrivers.get(parName);
				parameterMap.put(parName, parValue);
			}

			Map profileAttributes = UserProfileUtils.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE));
			JSONObject jsonTemplate = RestUtilities.readBodyAsJSONObject(req);

			if (StringUtilities.isEmpty(result)) {
				result = KpiEngineDataUtil.loadJsonData(jsonTemplate);
			}
			if (jsonTemplate.getJSONObject("chart").getString("type").equals("kpi")) {
				Calendar startDate = Calendar.getInstance();
				JSONObject objTemp = new JSONObject();
				List<KpiValue> kpiValues;
				Map<String, String> attributesValues = new TreeMap<String, String>();
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

				if (jsonTemplate.getJSONObject("chart").getJSONObject("data").get("kpi") instanceof JSONObject) {
					objTemp = jsonTemplate.getJSONObject("chart").getJSONObject("data").getJSONObject("kpi");
					String s = parameterMap.toString();
					String[] couples = s.replace("{", "").replace("}", "").split(",");
					for (String c : couples) {
						String[] keyvalue = c.split("=");
						if (keyvalue.length != 1) {
							attributesValues.put(keyvalue[0].trim(), keyvalue[1].trim());
						}

					}
					kpiValues = DAOFactory.getNewKpiDAO().findKpiValues(objTemp.getInt("id"), objTemp.getInt("version"), startDate.getTime(), new Date(),
							attributesValues);
					String result2 = new ObjectMapper().writeValueAsString(kpiValues);
					array.put(result2);
				} else {
					kpiValues = new ArrayList<>();
					for (int i = 0; i < jsonTemplate.getJSONObject("chart").getJSONObject("data").getJSONArray("kpi").length(); i++) {
						objTemp = jsonTemplate.getJSONObject("chart").getJSONObject("data").getJSONArray("kpi").getJSONObject(i);
						String s = parameterMap.toString();
						String[] couples = s.replace("{", "").replace("}", "").split(",");
						for (String c : couples) {
							String[] keyvalue = c.split("=");
							if (keyvalue.length != 1) {
								attributesValues.put(keyvalue[0].trim(), keyvalue[1].trim());
							}

						}
						kpiValues = DAOFactory.getNewKpiDAO().findKpiValues(objTemp.getInt("id"), objTemp.getInt("version"), startDate.getTime(), new Date(),
								attributesValues);
						String result2 = new ObjectMapper().writeValueAsString(kpiValues);
						array.put(result2);
					}
				}
			}

			JSONObject objectResult = new JSONObject();
			objectResult.put("loadKpiValue", array.toString());
			objectResult.put("info", result);
			return objectResult.toString();

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

}