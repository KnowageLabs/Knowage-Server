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

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.IConfiguration;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.ConfigurationException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class PythonUtils extends MLEngineUtils {
	private static Logger logger = Logger.getLogger(PythonUtils.class);

	public static final String PYTHON_ENVIRONMENT_CATEGORY = "PYTHON_CONFIGURATION";

	public static String getPythonAddress(String envLabel) {
		List<IConfiguration> allPythonConfigs = SingletonConfig.getInstance().getConfigsValueByCategory(PYTHON_ENVIRONMENT_CATEGORY);
		for (IConfiguration cfg : allPythonConfigs) {
			if (cfg.getLabel().equals(envLabel)) {
				return cfg.getValueCheck() + "/";
			}
		}
		throw new ConfigurationException("Cannot retrieve Python address from label [" + envLabel + "]");
	}

	public static String createPythonEngineRequestBody(String datastore, String dsLabel, String script, Map<String, Object> drivers, String outputVariable) {
		JSONObject jsonBody = new JSONObject();
		try {
			jsonBody.put("datastore", datastore);
			jsonBody.put("script", getScriptJwtToken(script));
			jsonBody.put("output_variable", outputVariable);
			jsonBody.put("dataset_label", dsLabel);
			jsonBody.put("drivers", drivers);
		} catch (Exception e) {
			logger.error("error while creating request body for Python engine");
			throw new SpagoBIRuntimeException("error while creating request body for Python engine", e);
		}
		return jsonBody.toString();
	}

	public static String getScriptFromTemplate(String base64template, String widgetId) throws JSONException {
		JSONObject widgetConf = getWidgetConfFromTemplate(base64template, widgetId);
		return widgetConf.getJSONObject("pythonConf").getString("script");
	}

}
