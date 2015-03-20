/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.analiticalmodel.document.bo.ObjNote;
import it.eng.spagobi.commons.utilities.GeneralUtilities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

/**
 * @author Antonella Giachino
 */
public class ObjectNotesJSONSerializer implements Serializer {
	
    
	public static final String ID = "id";
	public static final String BIN_ID = "binId";
	public static final String BIOBJ_ID = "biobjId";
	public static final String NOTES = "notes";  //content in string format
	public static final String EXEC_REC = "execReq";
	public static final String OWNER = "owner";
	public static final String CREATION_DATE = "creationDate";
	public static final String LAST_MODIFICATION_DATE = "lastModificationDate";
	public static final String VISIBILITY = "visibility";
	public static final String DELETABLE = "deletable";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof ObjNote) ) {
			throw new SerializationException("ObjectNotesJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			// dates are sent to the client using a fixed format, the one returned by GeneralUtilities.getServerDateFormat()
			SimpleDateFormat dateFormat =  new SimpleDateFormat();
			dateFormat.applyPattern(GeneralUtilities.getServerTimeStampFormat());
			
			ObjNote note = (ObjNote) o;
			result = new JSONObject();
			result.put(ID, note.getId() );
			result.put(BIN_ID, note.getBinId() );
			result.put(BIOBJ_ID, note.getBiobjId() );
			result.put(NOTES, note.getNotes() );
			result.put(OWNER, note.getOwner() );
			Date creationDate = note.getCreationDate();
			String creationDateStr = dateFormat.format(creationDate);
			result.put(CREATION_DATE, creationDateStr );
			Date lastChangeDate = note.getLastChangeDate();
			String lastChangeDateStr = dateFormat.format(lastChangeDate);
			result.put(LAST_MODIFICATION_DATE, lastChangeDateStr );
			result.put(VISIBILITY, note.getIsPublic() );
			result.put(DELETABLE, note.getIsDeletable() );
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}
