/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.engine.util;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.Scorecard;
import it.eng.spagobi.services.serialization.JsonConverter;

public class KpiEngineDataUtil extends AbstractHibernateDAO {
	public static transient Logger logger = Logger.getLogger(KpiEngineDataUtil.class);

	public static String loadJsonData(JSONObject jsonTemplate) {

		JSONArray result = new JSONArray();
		try {
			JSONObject jo = jsonTemplate;
			JSONObject chart = jo.getJSONObject("chart");
			JSONArray array = new JSONArray();
			if (chart.getString("type").equals("scorecard")) {
				Scorecard card = DAOFactory.getNewKpiDAO().loadScorecard(chart.getJSONObject("data").getJSONObject("scorecard").getInt("id"));
				JSONObject object = new JSONObject(JsonConverter.objectToJson(card, card.getClass()));
				JSONObject tempResult = new JSONObject();
				tempResult.put("scorecard", object);
				result.put(tempResult);
			} else {

				if (chart.getJSONObject("data").get("kpi") instanceof JSONArray) {
					array = chart.getJSONObject("data").getJSONArray("kpi");
				} else {
					array.put(chart.getJSONObject("data").getJSONObject("kpi"));
				}

				for (int i = 0; i < array.length(); i++) {
					JSONObject temp = array.getJSONObject(i);
					JSONObject tempResult = new JSONObject();

					Kpi kpi = DAOFactory.getNewKpiDAO().loadKpi(temp.getInt("id"), temp.getInt("version"));
					if (DAOFactory.getNewKpiDAO().valueTargetbyKpi(kpi) != null) {
						Double valueTarget = new Double(DAOFactory.getNewKpiDAO().valueTargetbyKpi(kpi));
						tempResult.put("target", valueTarget);
					} else {
						tempResult.put("target", JSONObject.NULL);
					}

					JSONObject object = new JSONObject(JsonConverter.objectToJson(kpi, kpi.getClass()));
					object.remove("definition");
					object.remove("enableVersioning");
					object.remove("category");
					object.remove("cardinality");

					tempResult.put("kpi", object);

					result.put(tempResult);
				}
			}

			return result.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
