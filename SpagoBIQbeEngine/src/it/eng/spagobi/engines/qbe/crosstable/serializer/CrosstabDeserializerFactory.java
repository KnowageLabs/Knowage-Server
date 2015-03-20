/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.crosstable.serializer;


import it.eng.qbe.serializer.IDeserializer;
import it.eng.qbe.serializer.IDeserializerFactory;

import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.engines.qbe.crosstable.serializer.json.CrosstabJSONDeserializer;
import it.eng.spagobi.engines.worksheet.widgets.CrosstabDefinition;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class CrosstabDeserializerFactory implements IDeserializerFactory {
	
	static CrosstabDeserializerFactory instance;
	
	static CrosstabDeserializerFactory getIntsnce() {
		return instance;
	}
	
	static {
		instance = new CrosstabDeserializerFactory();
		SerializationManager.registerDeserializerFactory(CrosstabDefinition.class, instance);
		
	}
	
	public static CrosstabDeserializerFactory getInstance() {
		if (instance == null) {
			instance = new CrosstabDeserializerFactory();
		}
		return instance;
	}
	
	private CrosstabDeserializerFactory() {}

	public IDeserializer getDeserializer(String mimeType) {
		return new CrosstabJSONDeserializer();
	}

}
