/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.tools.catalogue.bo.MetaModel;

import java.util.Locale;

import org.json.JSONObject;

public class MetaModelJSONSerializer implements Serializer {
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DATA_SOURCE_LABEL = "data_source_label";
	public static final String DESCRIPTION = "description";
	public static final String CATEGORY = "category";

	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof MetaModel) ) {
			throw new SerializationException("MetaModelJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			MetaModel model = (MetaModel) o;
			result = new JSONObject();
			result.put(ID, model.getId() );
			result.put(NAME, model.getName() );
			result.put(DESCRIPTION, model.getDescription() );
			result.put(CATEGORY, model.getCategory() );
			result.put(DATA_SOURCE_LABEL, model.getDataSourceLabel() );
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}
	

}
