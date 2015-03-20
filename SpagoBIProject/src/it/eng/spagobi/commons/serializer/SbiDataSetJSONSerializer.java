/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;

import java.util.Locale;

import org.json.JSONObject;

public class SbiDataSetJSONSerializer implements Serializer {

	public static final String DATASET_ID = "id";
	public static final String VERSION_NUM = "versionNum";
	private static final String DATASET_NAME = "name";
	private static final String DATASET_DESCRIPTION = "description";
	private static final String DATASET_LABEL = "label";
	private static final String DATASET_CATEGORY = "catTypeVn";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof SbiDataSet) ) {
			throw new SerializationException("SbiDataSetJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			SbiDataSet ds = (SbiDataSet)o;
			result = new JSONObject();
			
			result.put(DATASET_ID, ds.getId().getDsId());
			result.put(VERSION_NUM, ds.getId().getVersionNum());
			result.put(DATASET_NAME, ds.getName() );
			result.put(DATASET_DESCRIPTION, ds.getDescription() );
			result.put(DATASET_LABEL, ds.getLabel() );		
//			SbiDomains categ = ds.getCategory() ;
//			
//			if (categ != null){
//				String valueCD = categ.getValueCd();
//				result.put(DATASET_CATEGORY, categ.getValueCd());
//			}
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}