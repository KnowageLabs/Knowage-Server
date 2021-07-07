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
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public abstract class MLEngineUtils {

	private static Logger logger = Logger.getLogger(MLEngineUtils.class);

	public static String dataStore2DataFrame(String knowageDs) {
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
}
