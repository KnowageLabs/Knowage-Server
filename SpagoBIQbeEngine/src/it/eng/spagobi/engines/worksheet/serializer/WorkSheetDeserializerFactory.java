/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.serializer;

import it.eng.qbe.serializer.IDeserializer;
import it.eng.qbe.serializer.IDeserializerFactory;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.engines.worksheet.bo.WorkSheetDefinition;
import it.eng.spagobi.engines.worksheet.serializer.json.WorkSheetJSONDeserializer;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class WorkSheetDeserializerFactory implements IDeserializerFactory {
	
	static WorkSheetDeserializerFactory instance;
	
	static WorkSheetDeserializerFactory getIntsnce() {
		return instance;
	}
	
	static {
		instance = new WorkSheetDeserializerFactory();
		SerializationManager.registerDeserializerFactory(WorkSheetDefinition.class, instance);
		
	}
	
	public static WorkSheetDeserializerFactory getInstance() {
		if (instance == null) {
			instance = new WorkSheetDeserializerFactory();
		}
		return instance;
	}
	
	private WorkSheetDeserializerFactory() {}

	public IDeserializer getDeserializer(String mimeType) {
		if (mimeType != null && !mimeType.equalsIgnoreCase("application/json")) {
			throw new SpagoBIRuntimeException("Deserializer for mimeType " + mimeType + " not implemented");
		}
		return new WorkSheetJSONDeserializer();
	}

}
