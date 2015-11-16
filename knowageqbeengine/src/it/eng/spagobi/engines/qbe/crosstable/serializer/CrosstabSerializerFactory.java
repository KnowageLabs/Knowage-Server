/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.crosstable.serializer;

import it.eng.qbe.serializer.ISerializer;
import it.eng.qbe.serializer.ISerializerFactory;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.engines.qbe.crosstable.serializer.json.CrosstabJSONSerializer;
import it.eng.spagobi.engines.worksheet.widgets.CrosstabDefinition;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class CrosstabSerializerFactory implements ISerializerFactory{

	static CrosstabSerializerFactory instance;
	
	static {
		instance = new CrosstabSerializerFactory();
		SerializationManager.registerSerializerFactory(CrosstabDefinition.class, instance);	
	}
	
	
	public static CrosstabSerializerFactory getInstance() {
		if (instance == null) {
			instance = new CrosstabSerializerFactory();
		}
		return instance;
	}
	
	private CrosstabSerializerFactory() {}

	
	public ISerializer getSerializer(String mimeType) {
		return new CrosstabJSONSerializer();
	}

}
