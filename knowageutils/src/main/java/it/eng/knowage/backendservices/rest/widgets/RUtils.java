/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.backendservices.rest.widgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.IConfiguration;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities.Response;

public class RUtils extends MLEngineUtils {

	static protected Logger logger = Logger.getLogger(RUtils.class);

	static String getFinalResult(Response rEngineResponse, String outputType) {
		String rString = rEngineResponse.getResponseBody();
		String rOutput = rString.substring(2, rString.length() - 2);
		if (outputType.equals("img")) {
			return "<img src=\"data:image/;base64, " + rOutput + "\" style=\"width:100%;height:100%;\">";
		} else {
			return rOutput;
		}
	}

	static HashMap<String, String> createDriversMap(String driversString) {
		HashMap<String, String> driversMap = new HashMap<String, String>();
		try {
			JSONObject driversJSON = new JSONObject(driversString);
			Iterator<String> keys = driversJSON.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				String value = driversJSON.getString(key);
				driversMap.put(key, value);
			}
		} catch (Exception e) {
			logger.error("error while creating parameters map");
			throw new SpagoBIRuntimeException("error while creating parameters map", e);
		}
		return driversMap;
	}

	public static String getRAddress(String envLabel) {
		List<IConfiguration> allRConfigs = SingletonConfig.getInstance().getConfigsValueByCategory("R_CONFIGURATION");
		for (IConfiguration cfg : allRConfigs) {
			if (cfg.getLabel().equals(envLabel)) {
				return "http://" + cfg.getValueCheck() + "/";
			}
		}
		throw new SpagoBIRuntimeException("Cannot retrieve R address from label [" + envLabel + "]");
	}

	static String createREngineRequestBody(String dataset, String dsLabel, String script, String driversAsString, String outputVariable) {
		JSONObject jsonBody = new JSONObject();
		try {
			jsonBody.put("dataset", dataset);
			jsonBody.put("script", script);
			jsonBody.put("output_variable", outputVariable);
			jsonBody.put("dataset_name", dsLabel);
			jsonBody.put("drivers", driversAsString);
		} catch (Exception e) {
			logger.error("error while creating request body for R engine");
			throw new SpagoBIRuntimeException("error while creating request body for R engine", e);
		}
		return jsonBody.toString();
	}

	public static String getScriptFromTemplate(String base64template, String widgetId) throws JSONException {
		JSONObject widgetConf = getWidgetConfFromTemplate(base64template, widgetId);
		return widgetConf.getString("RCode");
	}
}
