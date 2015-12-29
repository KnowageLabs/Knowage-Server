/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.whatif.serializer;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
public class SerializationManager {

	// the first key is the output/input type, the second key is the version and
	// the value is the serializer
	static Map<String, Map<String, ISerializer>> serializerFactoryMappings;
	public static final String DEFAULT_VERSION = "-1";

	static {
		serializerFactoryMappings = new HashMap<String, Map<String, ISerializer>>();
	}

	/**
	 * Register a serializer in this manager.
	 * 
	 * @param mimeType
	 *            the type of the input/output
	 * @param serializer
	 *            the serializer to register
	 */
	public static void registerSerializer(String mimeType, ISerializer serializer) {

		String version = serializer.getVersion();

		if (version == null) {
			version = DEFAULT_VERSION;
		}

		Map<String, ISerializer> outputTypeSerializerMap = serializerFactoryMappings.get(mimeType);
		if (outputTypeSerializerMap == null) {
			outputTypeSerializerMap = new HashMap<String, ISerializer>();
			serializerFactoryMappings.put(mimeType, outputTypeSerializerMap);
		}

		outputTypeSerializerMap.put(version, serializer);
	}

	/**
	 * Gets the serializer for the specific version and input/output type
	 * 
	 * @param mimeType
	 *            the input/output type
	 * @param version
	 *            the version of the serializer
	 * @return the serializer
	 */
	public static ISerializer getSerializer(String mimeType, String version) {
		Map<String, ISerializer> outputTypeSerializerMap = serializerFactoryMappings.get(mimeType);
		if (outputTypeSerializerMap != null) {
			return outputTypeSerializerMap.get(version);
		}
		return null;
	}

	/**
	 * Gets the serializer for the specific input/output type and the default
	 * version
	 * 
	 * @param mimeType
	 *            the input/output type
	 * @return the serializer
	 */
	public static ISerializer getSerializer(String mimeType) {
		return getSerializer(mimeType, DEFAULT_VERSION);
	}

	/**
	 * Serialize the object with the serializer for the specific version and
	 * input/output type
	 * 
	 * @param mimeType
	 *            the input/output type
	 * @param version
	 *            the version of the serializer
	 * @param object
	 *            the object to serialize
	 * @return the serialized object
	 */
	public static Object serialize(String mimeType, String version, Object object) throws SerializationException {
		return getSerializer(mimeType, version).serialize(object);
	}

	/**
	 * Serialize the object with the serializer for the specific input/output
	 * type and the default version
	 * 
	 * @param mimeType
	 *            the input/output type
	 * @param object
	 *            the object to serialize
	 * @return the serialized object
	 */
	public static Object serialize(String mimeType, Object object) throws SerializationException {
		return getSerializer(mimeType).serialize(object);
	}

	/**
	 * Serialize the object with the serializer for the same version of the
	 * object to serialize and the specific input/output type
	 * 
	 * @param mimeType
	 *            the input/output type
	 * @param object
	 *            the object to serialize
	 * @return the serialized object
	 */
	public static Object serialize(String mimeType, Versionable object) throws SerializationException {
		return serialize(mimeType, object.getVersion(), object);
	}

	/**
	 * Deserialize the object with the serializer for the specific version and
	 * input/output type
	 * 
	 * @param mimeType
	 *            the input/output type
	 * @param version
	 *            the version of the serializer
	 * @param object
	 *            the object to serialize
	 * @param clazz
	 *            the resulting class type for the deserialization process
	 * @return the serialized object
	 */
	public static Object deserialize(String mimeType, String version, String object, Class clazz) throws SerializationException {
		return getSerializer(mimeType, version).deserialize(object, clazz);
	}

	/**
	 * Deserialize the object with the serializer for the specific input/output
	 * type and the default version
	 * 
	 * @param mimeType
	 *            the input/output type
	 * @param object
	 *            the object to serialize
	 * @param clazz
	 *            the resulting class type for the deserialization process
	 * @return the serialized object
	 */
	public static Object deserialize(String mimeType, String object, Class clazz) throws SerializationException {
		return getSerializer(mimeType).deserialize(object, clazz);
	}

	/**
	 * Deserialize the object with the serializer for the specific input/output
	 * type and the default version
	 * 
	 * @param mimeType
	 *            the input/output type
	 * @param object
	 *            the object to serialize
	 * @param clazz
	 *            the resulting type for the deserialization process
	 * @return the serialized object
	 */
	public static Object deserialize(String mimeType, String object, TypeReference clazz) throws SerializationException {
		return getSerializer(mimeType).deserialize(object, clazz);
	}

}
