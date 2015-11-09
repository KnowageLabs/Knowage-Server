/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.mapcatalogue.serializer;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoLayersDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class GeoLayerJSONDeserializer {

	static private Logger logger = Logger.getLogger(GeoLayerJSONSerializer.class);
	private static final String ID = "layerId";
	private static final String NAME = "name";
	private static final String LABEL = "label";
	private static final String DESCRIPTION = "descr";
	private static final String TYPE = "type";
	private static final String IS_BASE_LAYER = "baseLayer";
	private static final String PATHFILE = "pathFile";
	private static final String LAYERDEF = "layerDef";
	private static final String LAYERLABEL = "layerLabel";
	private static final String LAYERNAME = "layerName";
	private static final String LAYERIDENTIFY = "layerIdentify";
	private static final String LAYERURL = "layerURL";
	private static final String LAYEROPTIONS = "layerOptions";
	private static final String LAYERPARAMS = "layerParams";
	private static final String LAYERORDER = "layerOrder";
	private static final String GEOCATEGORY = "category_id";
	private static final String ROLES = "roles";

	public static GeoLayer deserialize(JSONObject serialized) throws EMFUserError {

		if (serialized != null) {
			String[] properties = JSONObject.getNames(serialized);
			if (properties != null) {
				GeoLayer layer = new GeoLayer();
				JSONObject filebody = new JSONObject();
				JSONObject rolesJSON = new JSONObject();
				for (int i = 0; i < properties.length; i++) {
					try {
						if (properties[i].equals(ID)) {
							String id = serialized.getString(properties[i]);
							if (id != null && !id.equals("")) {
								layer.setLayerId(new Integer(id));
							}

						} else if (properties[i].equals(NAME)) {
							layer.setName(serialized.getString(properties[i]));
						} else if (properties[i].equals(LABEL)) {
							layer.setLabel(serialized.getString(properties[i]));
						} else if (properties[i].equals(DESCRIPTION)) {
							layer.setDescr(serialized.getString(properties[i]));
						} else if (properties[i].equals(TYPE)) {
							layer.setType(serialized.getString(properties[i]));
						} else if (properties[i].equals(IS_BASE_LAYER)) {
							layer.setBaseLayer(Boolean.parseBoolean(serialized.getString(properties[i])));
						} else if (properties[i].equals(PATHFILE)) {
							layer.setPathFile(serialized.getString(properties[i]));
						} else if (properties[i].equals(LAYERLABEL)) {
							layer.setLayerLabel(serialized.getString(properties[i]));
						} else if (properties[i].equals(LAYERNAME)) {
							layer.setLayerName(serialized.getString(properties[i]));
						} else if (properties[i].equals(LAYERIDENTIFY)) {
							layer.setLayerIdentify(serialized.getString(properties[i]));
						} else if (properties[i].equals(LAYERURL)) {
							layer.setLayerURL(serialized.getString(properties[i]));
						} else if (properties[i].equals(LAYEROPTIONS)) {
							layer.setLayerOptions(serialized.getString(properties[i]));
						} else if (properties[i].equals(LAYERPARAMS)) {
							layer.setLayerParams(serialized.getString(properties[i]));
						} else if (properties[i].equals(LAYERORDER)) {
							layer.setLayerOrder(new Integer(serialized.getString(properties[i])));
						} else if (properties[i].equals(GEOCATEGORY)) {
							if (serialized.getString(properties[i]) == "null") {
								layer.setCategory_id(null);
							} else {
								layer.setCategory_id(new Integer(serialized.getString(properties[i])));
							}

						} else if (properties[i].equals(ROLES)) {

							ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
							List<SbiExtRoles> objRoles = null;

							JSONArray refe = serialized.getJSONArray(properties[i]);
							Object[] link = new Object[refe.length()];
							if (refe.length() != 0) {

								for (int j = 0; j < refe.length(); j++) {
									link[j] = refe.getJSONObject(j).get("id");
								}
								objRoles = dao.listRolesFromId(link);
								layer.setRoles(objRoles);
							}

						} else {

							filebody.put(properties[i], serialized.get(properties[i]));
							// rolesJSON.put(properties[i], serialized.get(properties[i]));
						}
					} catch (JSONException e) {
						logger.error("Error deserializing the layer.", e);
						throw new SpagoBIRuntimeException("Error deserializing the layer.", e);
					}

				}
				logger.debug("Layer deserialized. Label: " + layer.getLabel());
				layer.setFilebody(filebody.toString().getBytes());
				// layer.setRoles(rolesJSON.toString());
				return layer;
			}
		}

		logger.debug("Impossible to deserialize layer. No field found");
		return null;
	}
}
