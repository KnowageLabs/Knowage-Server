package it.eng.knowage.backendservices.rest.widgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.api.v2.ConfigResource;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities.Response;

public class RUtils {

	static protected Logger logger = Logger.getLogger(RUtils.class);

	private RUtils() {
	}

	static String getFinalResult(Response rEngineResponse, String outputType) {
		String rString = rEngineResponse.getResponseBody();
		String rOutput = rString.substring(2, rString.length() - 2);
		if (outputType.equals("img")) {
			return "<img src=\"data:image/;base64, " + rOutput + "\" style=\"width:100%;height:100%;\">";
		} else {
			return rOutput;
		}
	}

	static String DataSet2DataFrame(String knowageDs) {
		JSONObject oldDataset;
		JSONArray newDataframe = new JSONArray();
		try {
			oldDataset = new JSONObject(knowageDs);
			Map<String, String> columnNames = new HashMap<String, String>();
			JSONObject metaData = oldDataset.getJSONObject("metaData");
			JSONArray fields = (JSONArray) metaData.get("fields");
			for (int i = 1; i < fields.length(); i++) {
				JSONObject col = fields.getJSONObject(i);
				columnNames.put(col.get("name").toString(), col.get("header").toString());
			}
			JSONArray rows = (JSONArray) oldDataset.get("rows");
			for (int j = 0; j < rows.length(); j++) {
				JSONObject row = rows.getJSONObject(j);
				Iterator<String> keys = row.keys();
				JSONObject newDataframeRow = new JSONObject();
				while (keys.hasNext()) {
					String key = keys.next();
					if (columnNames.get(key) != null) {
						newDataframeRow.put(columnNames.get(key), row.get(key));
					}
				}
				newDataframe.put(newDataframeRow);
			}
		} catch (Exception e) {
			logger.error("error while converting json to dataframe format");
			throw new SpagoBIRuntimeException("error while converting json to dataframe format", e);
		}
		return newDataframe.toString();
	}

	static String getRAddress(String envLabel) {
		ConfigResource configResource = new ConfigResource();
		List<Config> allRConfigs = configResource.getConfigsByCategory("R_CONFIGURATION");
		for (Config cfg : allRConfigs) {
			if (cfg.getLabel().equals(envLabel)) {
				return "http://" + cfg.getValueCheck() + "/";
			}
		}
		throw new SpagoBIRuntimeException("Cannot retrieve R address from label [" + envLabel + "]");
	}

	static String createREngineRequestBody(String dataset, String dsLabel, String script, String outputVariable) {
		JSONObject jsonBody = new JSONObject();
		try {
			jsonBody.put("dataset", dataset);
			jsonBody.put("script", script);
			jsonBody.put("output_variable", outputVariable);
			jsonBody.put("dataset_name", dsLabel);
		} catch (Exception e) {
			logger.error("error while creating request body for R engine");
			throw new SpagoBIRuntimeException("error while creating request body for R engine", e);
		}
		return jsonBody.toString();
	}

	static String getRCodeFromTemplate(String base64template, String widgetId) {
		JSONObject templateJson;
		try {
			byte[] decodedBytes = Base64.decodeBase64(base64template);
			String template = new String(decodedBytes, "UTF-8");
			templateJson = new JSONObject(new String(decodedBytes, "UTF-8"));
			JSONArray sheets = (JSONArray) templateJson.get("sheets");
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				JSONArray widgets = (JSONArray) sheet.get("widgets");
				for (int j = 0; j < widgets.length(); j++) {
					JSONObject widget = widgets.getJSONObject(j);
					String id = widget.getString("id");
					if (id.equals(widgetId)) {
						return widget.get("RCode").toString();
					}
				}
			}
		} catch (Exception e) {
			logger.error("error while retrieving code from template");
			throw new SpagoBIRuntimeException("error while retrieving code from template", e);
		}
		throw new SpagoBIRuntimeException("Couldn't retrieve code from template for widgetId [" + widgetId + "]");
	}
}
