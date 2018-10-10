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

import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("1.0/chart/template")
public class ChartResources extends AbstractChartEngineResource {

	static private Logger logger = Logger.getLogger(ChartResources.class);

	@POST
	@Path("/save")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@UserConstraint(functionalities = { SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY })
	public String saveTemplate(@FormParam("jsonTemplate") String jsonTemplate, @FormParam("docLabel") String docLabel,
			@Context HttpServletResponse servletResponse) {
		JSONObject newTemplate;
		try {
			UserProfile userProfile = this.getUserProfile();
			JSONObject json = new JSONObject(jsonTemplate);
			json = parseTemplate(json);
			ChartTemplateClient ctc = new ChartTemplateClient();
			newTemplate = ctc.saveTemplate(json, docLabel, (String) userProfile.getUserUniqueIdentifier());
			return newTemplate.toString();
		} catch (JSONException e) {
			logger.error("Error while reading JSON of the chart template.", e);
			throw new SpagoBIServiceException("Error while reading JSON of the chart template", e);
		} catch (Exception e) {
			logger.error("Error while saving template", e);
			throw new SpagoBIServiceException("Error while saving template", e);
		}
	}

	private static JSONObject parseTemplate(JSONObject jsonObj) throws JSONException {

		Iterator keys = jsonObj.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object keyValue = jsonObj.get(key);
			if (key.equalsIgnoreCase("style")) {
				JSONObject styleObject = (JSONObject) keyValue;
				Iterator styleKeys = styleObject.keys();
				String newStyle = "";
				while (styleKeys.hasNext()) {
					String styleKey = (String) styleKeys.next();
					String styleValue = styleObject.optString(styleKey);
					newStyle = newStyle + styleKey + ":" + styleValue + ";";
				}
				jsonObj.put("style", newStyle);
			}

			if (keyValue instanceof JSONArray) {

				JSONArray array = (JSONArray) keyValue;
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					parseTemplate(obj);
				}

			}
			if (keyValue instanceof JSONObject) {
				parseTemplate((JSONObject) keyValue);
			}
		}

		return jsonObj;
	}

}
