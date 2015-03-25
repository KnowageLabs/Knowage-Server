/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.commons.utilities.GeneralUtilities;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class SnapshotJSONSerializer implements Serializer {
	
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String OWNER = "owner";
	public static final String CREATION_DATE = "creationDate";
	
	// dates are sent to the client using a fixed format, the one returned by GeneralUtilities.getServerDateFormat()
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( GeneralUtilities.getServerTimeStampFormat() );

	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof Snapshot) ) {
			throw new SerializationException("SnapshotJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			Snapshot snapshot = (Snapshot)o;
			result = new JSONObject();
			result.put(ID, snapshot.getId() );
			result.put(NAME, snapshot.getName() );
			result.put(DESCRIPTION, snapshot.getDescription() );			
			result.put(CREATION_DATE, DATE_FORMATTER.format(  snapshot.getDateCreation() ) );
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}
	
}
