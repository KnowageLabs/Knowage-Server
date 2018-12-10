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

import java.io.*;
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
		InputStream inputStream = StyleResource.class.getResourceAsStream(PATH_TO_SFNAS);

		JSONObject sfnas = convertToJson(getContent(inputStream));
		if (sfnas != null)
			allStyles.put(sfnas);
		File folder = new File(resourcePath + PATH_TO_STYLE);

		if (!folder.exists()) {
			return allStyles.toString();
		}

		File[] listOfFiles = folder.listFiles();

		for (File listOfFile : listOfFiles) {
			if (listOfFile.isFile()) {
				String pathToFile = folder.getPath() + File.separator + listOfFile.getName();

				JSONObject style = convertToJson(getContent(pathToFile));
				if (style != null) {
					allStyles.put(style);
				}
			}
		}

		return allStyles.toString();
	}

	private String getContent(String filepath) throws IOException {
		StringBuilder fileContent = new StringBuilder();

		BufferedReader br = new BufferedReader(new FileReader(filepath));
		String line;
		while ((line = br.readLine()) != null) {
			fileContent.append(line);
		}
		br.close();

		return fileContent.toString();
	}

	private String getContent(InputStream inputStream) throws IOException {
		StringBuilder fileContent = new StringBuilder();

		String line;
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
			while ((line = bufferedReader.readLine()) != null) {
				fileContent.append(line);
			}
		}

		return fileContent.toString();
	}

	private JSONObject convertToJson(String fileContent) {
		try {
			JSONObject obj = new JSONObject(Xml.xml2json(fileContent));
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
				for (String aResult : result) {
					String[] temp = aResult.split(":");
					if (temp.length > 1) {

						if (isNumeric(temp[1])) {
							if (temp[1].contains(".")) {
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

				if (keyValue.toString().contains(".")) {
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
			if (str.contains(".")) {
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
