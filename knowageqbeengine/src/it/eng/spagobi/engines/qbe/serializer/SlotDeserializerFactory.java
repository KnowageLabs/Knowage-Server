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
package it.eng.spagobi.engines.qbe.serializer;

import it.eng.qbe.model.structure.ModelCalculatedField.Slot;
import it.eng.qbe.serializer.IDeserializer;
import it.eng.qbe.serializer.IDeserializerFactory;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.engines.qbe.serializer.json.SlotJSONDeserializer;
import it.eng.spagobi.engines.worksheet.bo.Measure;
import it.eng.spagobi.engines.worksheet.serializer.json.MeasureJSONDeserializer;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SlotDeserializerFactory implements IDeserializerFactory {
	
	static SlotDeserializerFactory instance;
	
	static SlotDeserializerFactory getIntsnce() {
		return instance;
	}
	
	static {
		instance = new SlotDeserializerFactory();
		SerializationManager.registerDeserializerFactory(Slot.class, instance);
	}
	
	public static SlotDeserializerFactory getInstance() {
		if (instance == null) {
			instance = new SlotDeserializerFactory();
		}
		return instance;
	}
	
	private SlotDeserializerFactory() {}

	public IDeserializer getDeserializer(String mimeType) {
		if (mimeType != null && !mimeType.equalsIgnoreCase("application/json")) {
			throw new SpagoBIRuntimeException("Deserializer for mimeType " + mimeType + " not implemented");
		}
		return new SlotJSONDeserializer();
	}

}
