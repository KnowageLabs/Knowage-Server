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
package it.eng.qbe.serializer;



import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class SerializationManager {
	
	static Map<Class<? extends Object>, ISerializerFactory> serializerFactoryMappings;
	static Map<Class<? extends Object>, IDeserializerFactory> deserializerFactoryMappings;
	
	static {
		serializerFactoryMappings = new HashMap<Class<? extends Object>, ISerializerFactory>();
		deserializerFactoryMappings = new HashMap<Class<? extends Object>, IDeserializerFactory>();
	}
	
	public static void registerSerializerFactory(Class<? extends Object> c, ISerializerFactory serializerFactory) {
		serializerFactoryMappings.put(c, serializerFactory);
	}
	
	public static void registerDeserializerFactory(Class<? extends Object> c, IDeserializerFactory deserializerFactory) {
		deserializerFactoryMappings.put(c, deserializerFactory);
	}
	
	public static Object serialize(Object o, String mimeType) throws SerializationException {
		return getSerializer(o.getClass(), mimeType).serialize(o);
	}
	
	public static Object deserialize(Object o, String mimeType, Class<? extends Object> c) throws SerializationException {
		return getDeserializer(c, mimeType).deserialize(o);
	}
	
	public static ISerializer getSerializer(Class<? extends Object> c, String mimeType) {
		return getSerializerFactory(c).getSerializer(mimeType);
	}
	
	public static IDeserializer getDeserializer(Class<? extends Object> c, String mimeType) {
		return getDeserializerFactory(c).getDeserializer(mimeType);
	}
	
	public static ISerializerFactory getSerializerFactory(Class<? extends Object> c) {
		return serializerFactoryMappings.get( c );
	}
	
	public static IDeserializerFactory getDeserializerFactory(Class<? extends Object> c) {
		return deserializerFactoryMappings.get( c );
	}
}

