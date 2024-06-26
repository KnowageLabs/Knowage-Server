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
package it.eng.spagobi.engines.whatif.model;

import it.eng.spagobi.engines.whatif.serializer.ISerializer;
import it.eng.spagobi.engines.whatif.serializer.SerializationException;
import it.eng.spagobi.engines.whatif.serializer.SerializationManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class PivotJsonSerializer implements ISerializer {

	public static transient Logger logger = Logger.getLogger(PivotJsonSerializer.class);
	private static final String mimeType = "application/json";

	ObjectMapper mapper;

	public PivotJsonSerializer() {
		mapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
		// simpleModule.addSerializer(Member.class, new MemberJsonSerializer());
		simpleModule.addSerializer(PivotObjectForRendering.class, new PivotJsonHTMLSerializer());
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
