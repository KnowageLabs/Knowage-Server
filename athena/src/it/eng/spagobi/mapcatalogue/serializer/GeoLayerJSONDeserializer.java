/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.mapcatalogue.serializer;

import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoLayerJSONDeserializer {

	static private Logger logger = Logger.getLogger(GeoLayerJSONSerializer.class);
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String LABEL = "label";
	private static final String DESCRIPTION = "descr";
	private static final String TYPE = "type";
	private static final String IS_BASE_LAYER = "baseLayer";

	
	
	public static GeoLayer deserialize(JSONObject serialized){
		
		if(serialized!=null){
			String[] properties = JSONObject.getNames(serialized);
			if(properties!=null){
				GeoLayer layer = new GeoLayer();
				JSONObject layerDef = new JSONObject();
				for(int i=0; i<properties.length;i++){
					try {
						if(properties[i].equals(ID)){
							String id = serialized.getString(properties[i]);
							if(id!=null && !id.equals("")){
								layer.setLayerId(new Integer(id));
							}
							
						}else if(properties[i].equals(NAME)){
							layer.setName(serialized.getString(properties[i]));
						}else if(properties[i].equals(LABEL)){
							layer.setLabel(serialized.getString(properties[i]));
						}else if(properties[i].equals(DESCRIPTION)){
							layer.setDescr(serialized.getString(properties[i]));
						}else if(properties[i].equals(TYPE)){
							layer.setType(serialized.getString(properties[i]));
						}else if(properties[i].equals(IS_BASE_LAYER)){
							layer.setBaseLayer(Boolean.parseBoolean(serialized.getString(properties[i])));
						}
						else {
							layerDef.put(properties[i], serialized.get(properties[i]));
						}	
					} catch (JSONException e) {
						logger.error("Error deserializing the layer.",e);
						throw new SpagoBIRuntimeException("Error deserializing the layer.",e);
					}
					
				}
				logger.debug("Layer deserialized. Label: "+layer.getLabel());
				layer.setLayerDef(layerDef.toString().getBytes());
				return layer;
			}
		}
		
		logger.debug("Impossible to deserialize layer. No field found");
		return null;
	}
}
