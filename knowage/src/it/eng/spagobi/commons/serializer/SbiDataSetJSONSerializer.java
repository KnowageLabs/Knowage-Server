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