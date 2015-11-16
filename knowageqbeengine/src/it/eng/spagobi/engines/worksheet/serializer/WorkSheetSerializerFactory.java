/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.serializer;

import it.eng.qbe.serializer.ISerializer;
import it.eng.qbe.serializer.ISerializerFactory;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.engines.worksheet.bo.WorkSheetDefinition;
import it.eng.spagobi.engines.worksheet.serializer.json.WorkSheetJSONSerializer;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class WorkSheetSerializerFactory implements ISerializerFactory {

	static WorkSheetSerializerFactory instance;
	
	static WorkSheetSerializerFactory getIntsnce() {
		return instance;
	}
	
	static {
		instance = new WorkSheetSerializerFactory();
		SerializationManager.registerSerializerFactory(WorkSheetDefinition.class, instance);		
	}
	
	
	public static WorkSheetSerializerFactory getInstance() {
		if (instance == null) {
			instance = new WorkSheetSerializerFactory();
		}
		return instance;
	}
	
	private WorkSheetSerializerFactory() {}

	
	public ISerializer getSerializer(String mimeType) {
		if (mimeType != null && !mimeType.equalsIgnoreCase("application/json")) {
			throw new SpagoBIRuntimeException("Serializer for mimeType " + mimeType + " not implemented");
		}
		return new WorkSheetJSONSerializer();
	}

}
