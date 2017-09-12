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
package it.eng.spagobi.commons.deserializer;

import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.tools.dataset.crosstab.CrosstabDefinition;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JSONDeserializer implements Deserializer {

	Map<Class, Deserializer> mappings;

	public JSONDeserializer() {
		mappings = new HashMap();
		mappings.put(Engine.class, new EngineJSONDeserializer());
		mappings.put(CrosstabDefinition.class, new CrosstabJSONDeserializer());
		mappings.put(SbiUdp.class, new SbiUdpJSONDeserializer());

	}

	@Override
	public Object deserialize(Object o, Class clazz) throws DeserializationException {
		Object result = null;

		try {
			Assert.assertNotNull(o, "Input parameter [" + o + "] cannot be null");
			Assert.assertNotNull(o, "Input parameter [" + clazz + "] cannot be null");

			JSONObject json = null;
			if (o instanceof JSONObject) {
				json = (JSONObject) o;
			} else if (o instanceof String) {
				json = new JSONObject((String) o);
			} else {
				throw new DeserializationException("Impossible to deserialize from an object of type [" + o.getClass().getName() + "]");
			}

			Deserializer deserializer = mappings.get(clazz);
			if (deserializer == null) {
				throw new DeserializationException("Impossible to deserialize to an object of type [" + clazz.getName() + "]");
			}
			result = deserializer.deserialize(o, clazz);

		} catch (Throwable t) {
			throw new DeserializationException("An error occurred while deserializing object: " + o, t);
		} finally {

		}

		return result;
	}

}
