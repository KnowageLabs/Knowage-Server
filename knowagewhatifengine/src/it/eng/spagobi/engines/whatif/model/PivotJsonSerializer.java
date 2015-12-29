/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it) 
 *
 */
package it.eng.spagobi.engines.whatif.model;

import it.eng.spagobi.engines.whatif.serializer.ISerializer;
import it.eng.spagobi.engines.whatif.serializer.SerializationException;
import it.eng.spagobi.engines.whatif.serializer.SerializationManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.apache.log4j.Logger;
import org.olap4j.OlapConnection;

import com.eyeq.pivot4j.PivotModel;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class PivotJsonSerializer implements ISerializer {

	public static transient Logger logger = Logger.getLogger(PivotJsonSerializer.class);
	private static final String mimeType = "application/json";

	ObjectMapper mapper;

	public PivotJsonSerializer(OlapConnection connection, ModelConfig config) {
		mapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
		// simpleModule.addSerializer(Member.class, new MemberJsonSerializer());
		simpleModule.addSerializer(PivotModel.class, new PivotJsonHTMLSerializer(connection, config));
		mapper.registerModule(simpleModule);
	}

	public String serialize(Object object) throws SerializationException {
		try {
			return mapper.writeValueAsString(object);
		} catch (Exception e) {
			logger.error("Error serializing the MemberEntry", e);
			throw new SpagoBIRuntimeException("Error serializing the MemberEntry", e);
		}
	}

	public Object deserialize(String toDeserialize, Class object) throws SerializationException {
		try {
			return mapper.readValue(toDeserialize, object);
		} catch (Exception e) {
			logger.error("Error deserializing the MemberEntry", e);
			throw new SpagoBIRuntimeException("Error deserializing the MemberEntry", e);
		}
	}

	public Object deserialize(String toDeserialize, TypeReference object) throws SerializationException {
		try {
			return mapper.readValue(toDeserialize, object);
		} catch (Exception e) {
			logger.error("Error deserializing the MemberEntry", e);
			throw new SpagoBIRuntimeException("Error deserializing the MemberEntry", e);
		}
	}

	public String getVersion() {
		return SerializationManager.DEFAULT_VERSION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.engines.whatif.services.serializer.ISerializer#getFormat()
	 */
	public String getFormat() {
		return mimeType;
	}

}
