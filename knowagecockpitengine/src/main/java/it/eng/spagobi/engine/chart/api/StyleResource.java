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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engine.chart.ChartEngineConfig;
import it.eng.spagobi.json.Xml;
import it.eng.spagobi.services.rest.annotations.UserConstraint;

@Path("/chart/style")
public class StyleResource {
	static private Logger logger = Logger.getLogger(StyleResource.class);
	private static final String PATH_TO_STYLE = File.separator + "style";
	private static final String PATH_TO_SFNAS = "/chart/templates/styles/nostyle/default.xml";

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY })
	public String getStyles() throws Exception {

		String resourcePath = ChartEngineConfig.getEngineResourcePath();

		JSONArray allStyles = new JSONArray();
		URL urlToSfnas = StyleResource.class.getResource(PATH_TO_SFNAS);
		String path = urlToSfnas.getPath();
		path = path.replaceAll("%20", " ");

		JSONObject sfnas = convertToJson(path);
		if (sfnas != null)
			allStyles.put(sfnas);
		File folder = new File(resourcePath + PATH_TO_STYLE);

		if (!folder.exists()) {
			return allStyles.toString();
		}

		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String pathToFile = folder.getPath() + File.separator + listOfFiles[i].getName();

				JSONObject style = convertToJson(pathToFile);
				if (style != null) {
					allStyles.put(style);
				}
			}
		}

		return allStyles.toString();
	}

	private JSONObject convertToJson(String filepath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		StringBuilder fileContent = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			fileContent.append(line);
		}
		br.close();

		try {

			String template = fileContent.toString();
			JSONObject obj = new JSONObject(Xml.xml2json(template));
			Iterator keys = obj.keys();

			String key = (String) keys.next();
			Object keyValue = obj.get(key);
			JSONObject style = (JSONObject) keyValue;
			if (key.equalsIgnoreCase("chart")) {
				obj.remove(key);
				obj.put("CHARTSTYLE", style);
			}

			obj = parseTemplate(obj);

			// if empty json object is returned it would not be added to styles
			if (!obj.toString().equals(new JSONObject().toString())) {
				return obj;
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Invalid xml style template");
		}
		return null;
	}

	private static JSONObject parseTemplate(JSONObject jsonObj) throws JSONException {

		Iterator keys = jsonObj.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object keyValue = jsonObj.get(key);
			if (key.equalsIgnoreCase("style")) {

				String value = keyValue.toString();
				String[] result = value.split(";");
				JSONObject obj = new JSONObject();
				for (int i = 0; i < result.length; i++) {
					String[] temp = result[i].split(":");
					if (temp.length > 1) {

						if (isNumeric(temp[1])) {
							if (temp[1].indexOf(".") != -1) {
								double num = Double.parseDouble(temp[1]);
								obj.put(temp[0], num);
							} else {
								int num = Integer.parseInt(temp[1]);
								obj.put(temp[0], num);
							}

						} else if (temp[1].equals("true") || temp[1].equals("false")) {
							boolean bool = Boolean.parseBoolean(temp[1]);
							obj.put(temp[0], bool);
						} else {
							obj.put(temp[0], temp[1]);
						}

					} else {
						obj.put(temp[0], "");
					}

				}
				jsonObj.put(key, obj);

			}

			if (isNumeric(keyValue.toString())) {

				if (keyValue.toString().indexOf(".") != -1) {
					jsonObj.put(key, Double.parseDouble(keyValue.toString()));
				} else {
					jsonObj.put(key, Integer.parseInt(keyValue.toString()));
				}

			}

			if (keyValue.toString().equals("true") || keyValue.toString().equals("false")) {
				jsonObj.put(key, Boolean.parseBoolean(keyValue.toString()));
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

	private static boolean isNumeric(String str) {
		try {
			if (str.indexOf(".") != -1) {
				double num = Double.parseDouble(str);
			} else {
				int num = Integer.parseInt(str);
			}

		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
