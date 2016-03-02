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

import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.hotlink.rememberme.bo.RememberMe;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class FavouritesJSONSerializer implements Serializer {
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String DOCUMENT_LABEL = "documentLabel";
	public static final String DOCUMENT_NAME = "documentName";
	public static final String DOCUMENT_DESCRIPTION = "documentDescription";
	public static final String DOCUMENT_TYPE = "documentType";
	public static final String SUBOBJECT_ID = "subobjectId";
	public static final String SUBOBJECT_NAME = "subobjectName";
	public static final String PARAMETERS = "parameters";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof RememberMe) ) {
			throw new SerializationException("SnapshotJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			RememberMe favourite = (RememberMe)o;
			result = new JSONObject();
			result.put(ID, favourite.getId() );
			result.put(NAME, favourite.getName() );
			result.put(DESCRIPTION, favourite.getDescription() );
			result.put(DOCUMENT_LABEL, favourite.getDocumentLabel() );			
			result.put(DOCUMENT_NAME,  favourite.getDocumentName());
			result.put(DOCUMENT_DESCRIPTION,  favourite.getDocumentDescription());
			result.put(DOCUMENT_TYPE,  favourite.getDocumentType());
			result.put(SUBOBJECT_ID,  favourite.getSubObjId());
			result.put(SUBOBJECT_NAME,  favourite.getSubObjName());
			result.put(PARAMETERS,  favourite.getParameters());
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}
	
}
