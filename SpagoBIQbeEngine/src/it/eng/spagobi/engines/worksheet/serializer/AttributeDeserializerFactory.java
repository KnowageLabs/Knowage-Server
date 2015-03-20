/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
