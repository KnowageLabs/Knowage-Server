/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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

