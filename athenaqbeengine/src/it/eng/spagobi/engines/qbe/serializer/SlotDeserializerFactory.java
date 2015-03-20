/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
