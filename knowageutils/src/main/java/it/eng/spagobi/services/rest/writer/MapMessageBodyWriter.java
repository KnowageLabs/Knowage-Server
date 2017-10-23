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
package it.eng.spagobi.services.rest.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * serializer for lists 
 */

@Provider
@Produces("application/json")
public class MapMessageBodyWriter implements MessageBodyWriter<Map<Object,Object>> {

	
	static private Logger logger = Logger.getLogger(MapMessageBodyWriter.class);
	
    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {
        return (Map.class).isAssignableFrom(type);
    }

    @Override
    public long getSize(Map<Object,Object> object, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
        // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
        return 0;
    }

    @Override
    public void writeTo(Map<Object,Object> objects, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream out) throws IOException, WebApplicationException {

    	ObjectMapper mo = new ObjectMapper();
    	
    	
        Writer writer = new PrintWriter(out);
        
       // MapWrapper wrapper = new MapWrapper(objects);
        String aVlaue = mo.writeValueAsString(objects);
        //JSONObject jo;
//		try {
			//jo = new JSONObject(aVlaue);
			 writer.write(aVlaue.toString());
//		} catch (JSONException e) {
//			logger.error("error serializing map",e);
//		}        
       
        writer.flush();
        writer.close();
    }
    
    
    private class MapWrapper{
    	private Map<Object, Object> map;

    	
		public MapWrapper(Map<Object, Object> map) {
			super();
			this.map = map;
		}

		public Map<Object, Object> getMap() {
			return map;
		}

		public void setMap(Map<Object, Object> map) {
			this.map = map;
		}
    	
    	
    }
}