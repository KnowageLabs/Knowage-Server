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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.engines.drivers.generic.GenericDriver;

public class CockpitDriver extends GenericDriver {

	static private Logger logger = Logger.getLogger(CockpitDriver.class);

	@Override
	public ArrayList<String> getDatasetAssociated(byte[] contentTemplate) throws JSONException {
		logger.debug("IN");

		ArrayList<String> datasetsLabels = new ArrayList<String>();
		JSONObject templateContent = getTemplateAsJsonObject(contentTemplate);
		if (templateContent == null) {
			logger.error("Template content non returned. Impossible get associated dataset. Check the template!");
			return datasetsLabels;
		}

		// get datasets from template
		JSONArray stores = null;
		String dsFieldName = null;
		if (templateContent.has("templateContent")) {
			templateContent = templateContent.getJSONObject("templateContent");
		}
		if (templateContent.has("configuration")) {
			// new cockpit
			stores = templateContent.getJSONObject("configuration").getJSONArray("datasets");
			dsFieldName = "dsLabel";
		} else {
			// old cockpit
			JSONObject storeConfJSON = templateContent.getJSONObject("storesConf");
			stores = storeConfJSON.getJSONArray("stores");
			dsFieldName = "storeId";
		}

		for (int i = 0; i < stores.length(); i++) {
			JSONObject store = stores.getJSONObject(i);
			String dsLabel = store.getString(dsFieldName);
			datasetsLabels.add(dsLabel);
		}

		logger.debug("OUT");
		return datasetsLabels;
	}

	@Override
	public ArrayList<Integer> getFunctionsAssociated(byte[] contentTemplate) throws JSONException {
		logger.debug("IN");

		ArrayList<Integer> functionIds = new ArrayList<Integer>();
		JSONObject templateContent = getTemplateAsJsonObject(contentTemplate);
		if (templateContent == null) {
			logger.error("Template content non returned. Impossible get associated functions. Check the template!");
			return functionIds;
		}

		JSONArray sheets = templateContent.getJSONArray("sheets");
		for (int i = 0; i < sheets.length(); i++) {
			JSONObject sheet = sheets.getJSONObject(i);
			JSONArray widgets = sheet.getJSONArray("widgets");
			for (int j = 0; j < widgets.length(); j++) {
				JSONObject widget = widgets.getJSONObject(j);
				String widgetId = widget.getString("id");
				JSONArray columnSelectedOfDataset = new JSONArray();
				try {
					if (widget.has("content")) {
						columnSelectedOfDataset = widget.getJSONObject("content").optJSONArray("columnSelectedOfDataset");
					} else {
						// case chart widget
						columnSelectedOfDataset = widget.optJSONArray("columnSelectedOfDatasetAggregations");
					}
				} catch (JSONException e) {
					logger.error("Something went wrong while getting functions associated to widget: " + widgetId, e);
					// if something went wrong the template is malformed, just skip
				}
				// loop on dataset columns and look for function ids
				if (columnSelectedOfDataset != null) {
					for (int k = 0; k < columnSelectedOfDataset.length(); k++) {
						JSONObject column = columnSelectedOfDataset.getJSONObject(k);
						if (column.has("boundFunction")) {
							int funcId = column.getJSONObject("boundFunction").getInt("id");
							if (!functionIds.contains(funcId))
								functionIds.add(funcId);
						}
					}
				}
			}
		}

		logger.debug("OUT");
		return functionIds;
	}

	@Override
	public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		logger.debug("IN");
		BIObject obj = null;
		try {
			obj = (BIObject) biobject;
		} catch (ClassCastException cce) {
			logger.error("The input object is not a BIObject type", cce);
			return null;
		}
		Engine engine = obj.getEngine();
		String url = engine.getUrl();
		HashMap parameters = new HashMap();
		String documentId = obj.getId().toString();
		parameters.put("document", documentId);
		applySecurity(parameters, profile);
		EngineURL engineURL = new EngineURL(url.replace("/execute", "/edit"), parameters);
		logger.debug("OUT");
		return engineURL;
		// return new EngineURL("pippo.jsp", new HashMap<>());
	}

	@Override
	public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		// TODO Auto-generated method stub
		logger.debug("IN");
		BIObject obj = null;
		try {
			obj = (BIObject) biobject;
		} catch (ClassCastException cce) {
			logger.error("The input object is not a BIObject type", cce);
			return null;
		}
		Engine engine = obj.getEngine();
		String url = engine.getUrl();
		HashMap parameters = new HashMap();
		String documentId = obj.getId().toString();
		parameters.put("document", documentId);
		applySecurity(parameters, profile);
		EngineURL engineURL = new EngineURL(url.replace("/execute", "/edit"), parameters);
		logger.debug("OUT");
		return engineURL;
	}
}
