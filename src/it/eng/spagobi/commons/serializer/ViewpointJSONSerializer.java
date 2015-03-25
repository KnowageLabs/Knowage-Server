/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONObject;

import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.commons.utilities.GeneralUtilities;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class ViewpointJSONSerializer implements Serializer {
	
	
	
	private static final String ID = "id";
	private static final String DOCUMENT_ID = "documentId";
	private static final String OWNER = "owner";
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String SCOPE = "scope";
	private static final String PARAMETERS = "parameters";
	private static final String CREATION_DATE = "creationDate";
	
	// dates are sent to the client using a fixed format, the one returned by GeneralUtilities.getServerDateFormat()
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(GeneralUtilities.getServerTimeStampFormat() );

	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof Viewpoint) ) {
			throw new SerializationException("ViewpointJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			Viewpoint viewpoint = (Viewpoint)o;
			result = new JSONObject();
			
			
			result.put(ID, viewpoint.getVpId() );
			result.put(DOCUMENT_ID, viewpoint.getBiobjId() );
			result.put(OWNER, viewpoint.getVpOwner() );
			
			result.put(NAME, viewpoint.getVpName() );
			result.put(DESCRIPTION, viewpoint.getVpDesc() );			
			result.put(SCOPE, viewpoint.getVpScope() );
			
			result.put(CREATION_DATE, DATE_FORMATTER.format(  viewpoint.getVpCreationDate() ) );
			
			JSONObject parametersJSON = new JSONObject();
			String str = viewpoint.getVpValueParams();
			String[] parameters = str.split("%26");
			for(int i = 0; i < parameters.length; i++) {
				String[] parameter = parameters[i].split("%3D");
				if(parameter.length > 1 ) {
					parametersJSON.put(parameter[0], parameter[1]);
				}
			}
			result.put(PARAMETERS, parametersJSON);
			
			//contentVP = contentVP + labelUrl + "%3D" + value + "%26";
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}
	
	
}
