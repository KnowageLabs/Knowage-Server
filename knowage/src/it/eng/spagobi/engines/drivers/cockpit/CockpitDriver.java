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
package it.eng.spagobi.engines.drivers.cockpit;

import it.eng.spagobi.engines.drivers.generic.GenericDriver;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CockpitDriver extends GenericDriver {

	static private Logger logger = Logger.getLogger(CockpitDriver.class);

	@Override
	public ArrayList<String> getDatasetAssociated(byte[] contentTemplate) throws JSONException {
		logger.debug("IN");

		ArrayList<String> datasetsLabels = new ArrayList<String>();
		JSONObject templateContent = getTemplateAsJsonObject(contentTemplate);

		// get datasets from template

		JSONObject storesConf = (JSONObject) templateContent.get("storesConf");
		JSONArray stores = (JSONArray) storesConf.get("stores");

		for (int i = 0; i < stores.length(); i++) {
			JSONObject store = stores.getJSONObject(i);
			String dsLabel = store.getString("storeId");
			datasetsLabels.add(dsLabel);
		}

		logger.debug("OUT");
		return datasetsLabels;
	}

}
