/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.tools.catalogue.bo.Content;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

public class ContentJSONSerializer implements Serializer {
	
	public static final String ID = "id";
	public static final String CREATION_USER = "creationUser";
	public static final String CREATION_DATE = "creationDate";
	public static final String ACTIVE = "active";
	public static final String FILE_NAME = "fileName";
	public static final String DIMENSION = "dimension";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof Content) ) {
			throw new SerializationException("ContentJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			// dates are sent to the client using a fixed format, the one returned by GeneralUtilities.getServerDateFormat()
			SimpleDateFormat dateFormat =  new SimpleDateFormat();
			dateFormat.applyPattern(GeneralUtilities.getServerTimeStampFormat());
			
			Content content = (Content) o;
			result = new JSONObject();
			
			result.put(ID, content.getId());
			Date creationDate = content.getCreationDate();
			String creationDateStr = dateFormat.format(creationDate);
			result.put(CREATION_DATE, creationDateStr );
			result.put(CREATION_USER, content.getCreationUser());
			result.put(ACTIVE, content.getActive());
			result.put(FILE_NAME, content.getFileName());
			result.put(DIMENSION, content.getDimension());

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}
	

}
