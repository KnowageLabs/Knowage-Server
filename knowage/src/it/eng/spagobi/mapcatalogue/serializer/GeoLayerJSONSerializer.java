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
	private static final String PATHFILE = "pathFile";
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
		jgen.writeStringField(PATHFILE, value.getPathFile());
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
