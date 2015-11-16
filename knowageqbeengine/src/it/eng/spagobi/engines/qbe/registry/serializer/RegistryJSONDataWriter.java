/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.registry.serializer;

import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class RegistryJSONDataWriter extends JSONDataWriter {

	@Override
//	protected String getFieldName(IFieldMetaData fieldMetaData, int i) {
//		return super.getFieldHeader(fieldMetaData, i);
//	}
	

	protected String getFieldName(IFieldMetaData fieldMetaData, int i) {
		//String fieldName = "column_" + (i+1);
		String pathName = fieldMetaData.getName();
		// extract field name
		int index = pathName.lastIndexOf(':');
		
		String fieldName = pathName.substring(index+1);
		
		return fieldName;
	}
	
}

