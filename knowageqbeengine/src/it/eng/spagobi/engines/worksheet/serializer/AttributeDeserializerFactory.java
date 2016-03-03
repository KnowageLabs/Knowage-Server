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
package it.eng.spagobi.engines.worksheet.serializer;

import it.eng.qbe.serializer.IDeserializer;
import it.eng.qbe.serializer.IDeserializerFactory;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.engines.worksheet.bo.Attribute;
import it.eng.spagobi.engines.worksheet.serializer.json.AttributeJSONDeserializer;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class AttributeDeserializerFactory implements IDeserializerFactory {
	
	static AttributeDeserializerFactory instance;
	
	static AttributeDeserializerFactory getIntsnce() {
		return instance;
	}
	
	static {
		instance = new AttributeDeserializerFactory();
		SerializationManager.registerDeserializerFactory(Attribute.class, instance);
	}
	
	public static AttributeDeserializerFactory getInstance() {
		if (instance == null) {
			instance = new AttributeDeserializerFactory();
		}
		return instance;
	}
	
	private AttributeDeserializerFactory() {}

	public IDeserializer getDeserializer(String mimeType) {
		if (mimeType != null && !mimeType.equalsIgnoreCase("application/json")) {
			throw new SpagoBIRuntimeException("Deserializer for mimeType " + mimeType + " not implemented");
		}
		return new AttributeJSONDeserializer();
	}

}
