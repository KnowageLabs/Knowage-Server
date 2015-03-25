/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.mapcatalogue.serializer;

import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class GeoLayerJSONSerializer extends JsonSerializer<GeoLayer> {

	static private Logger logger = Logger.getLogger(GeoLayerJSONSerializer.class);
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String NAME = "name";
	private static final String LABEL = "label";
	private static final String DESCRIPTION = "descr";
	private static final String IS_BASE_LAYER = "baseLayer";

	//private static final String LAYER_DEF = "layerDef";
	
	@Override
	public void serialize(GeoLayer value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException{

		jgen.writeStartObject();
		
		jgen.writeNumberField(ID,value.getLayerId());
		jgen.writeStringField(NAME, value.getName());
		jgen.writeStringField(LABEL, value.getLabel());
		jgen.writeStringField(DESCRIPTION, value.getDescr());
		jgen.writeStringField(TYPE, value.getType());
		jgen.writeBooleanField(IS_BASE_LAYER, value.isBaseLayer());
		
		if(value.getLayerDef()!=null){
			JSONObject js = null;
			try {
				js = new JSONObject(new String(value.getLayerDef()));
			} catch (JSONException e) {
				logger.error("Error serializing the definition of the layer"+value.getLabel(),e);
				throw new SpagoBIRuntimeException("Error serializing the definition of the layer"+value.getLabel(),e);
			}
			if(js!=null){
				String[] properties = JSONObject.getNames(js);
				if(properties!=null){
					//jgen.writeObjectFieldStart(LAYER_DEF);
					for(int i=0; i<properties.length;i++){
						try {
							jgen.writeObjectField(properties[i],  js.get(properties[i]));
						} catch (JSONException e) {
							logger.error("Error serializing the layer"+value.getLabel(),e);
							throw new SpagoBIRuntimeException("Error serializing the layer"+value.getLabel(),e);
						}
					}
					//jgen.writeEndObject();	
				}
			}
		}
		
		
		jgen.writeEndObject();
	}
}
