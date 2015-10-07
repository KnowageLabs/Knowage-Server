/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
