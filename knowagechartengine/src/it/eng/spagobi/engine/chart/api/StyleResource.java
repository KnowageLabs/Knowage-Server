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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.engine.chart.ChartEngineConfig;
import it.eng.spagobi.json.Xml;

@Path("/style")
public class StyleResource {
	static private Logger logger = Logger.getLogger(StyleResource.class);
	private static final String PATH_TO_STYLE = File.separator + "style";
	private static final String PATH_TO_SFNAS = "/chart/templates/styles/nostyle/sfnas.xml";

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getStyles() throws Exception {

		String resourcePath = ChartEngineConfig.getEngineResourcePath();

		JSONArray allStyles = new JSONArray();

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
		URL urlToSfnas = StyleResource.class.getResource(PATH_TO_SFNAS);
		JSONObject sfnas = convertToJson(urlToSfnas.getPath());
		if (sfnas != null)
			allStyles.put(sfnas);

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

}
