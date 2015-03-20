/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
