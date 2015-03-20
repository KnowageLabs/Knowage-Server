/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.kpi.model.bo.Resource;

import java.util.Locale;
import org.json.JSONObject;

public class ResourceJSONSerializer implements Serializer {

	public static final String RESOURCE_ID = "id";
	private static final String RESOURCE_NAME = "name";
	private static final String RESOURCE_DESCRIPTION = "description";
	private static final String RESOURCE_CODE = "code";
	private static final String RESOURCE_COL_NAME = "columnname";
	private static final String RESOURCE_TAB_NAME = "tablename";
	private static final String RESOURCE_TYPE_ID = "typeId";
	private static final String RESOURCE_TYPE_CD = "typeCd";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof Resource) ) {
			throw new SerializationException("ResourceJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			Resource res = (Resource)o;
			result = new JSONObject();
			
			result.put(RESOURCE_ID, res.getId() );
			result.put(RESOURCE_NAME, res.getName() );
			result.put(RESOURCE_DESCRIPTION, res.getDescr() );
			result.put(RESOURCE_CODE, res.getCode() );
			result.put(RESOURCE_COL_NAME, res.getColumn_name() );
			result.put(RESOURCE_TAB_NAME, res.getTable_name() );
			result.put(RESOURCE_TYPE_ID, res.getTypeId() );
			result.put(RESOURCE_TYPE_CD, res.getType());			
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}
