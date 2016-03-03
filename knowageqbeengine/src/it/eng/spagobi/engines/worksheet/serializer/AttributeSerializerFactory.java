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

import it.eng.qbe.serializer.ISerializer;
import it.eng.qbe.serializer.ISerializerFactory;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.engines.worksheet.bo.Attribute;
import it.eng.spagobi.engines.worksheet.serializer.json.AttributeJSONSerializer;
import it.eng.spagobi.engines.worksheet.widgets.CrosstabDefinition;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 * 
 */
public class AttributeSerializerFactory implements ISerializerFactory{

	static AttributeSerializerFactory instance;
	
	static AttributeSerializerFactory getIntsnce() {
		return instance;
	}
	
	static {
		instance = new AttributeSerializerFactory();
		SerializationManager.registerSerializerFactory(Attribute.class, instance);
		SerializationManager.registerSerializerFactory(CrosstabDefinition.Row.class, instance);
		SerializationManager.registerSerializerFactory(CrosstabDefinition.Column.class, instance);
	}
	
	
	public static AttributeSerializerFactory getInstance() {
		if (instance == null) {
			instance = new AttributeSerializerFactory();
		}
		return instance;
	}
	
	private AttributeSerializerFactory() {}

	
	public ISerializer getSerializer(String mimeType) {
		if (mimeType != null && !mimeType.equalsIgnoreCase("application/json")) {
			throw new SpagoBIRuntimeException("Serializer for mimeType " + mimeType + " not implemented");
		}
		return new AttributeJSONSerializer();
	}

}
