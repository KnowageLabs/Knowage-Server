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

