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
