/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.tools.catalogue.bo.Artifact;

import java.util.Locale;

import org.json.JSONObject;

public class ArtifactJSONSerializer implements Serializer {
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String TYPE = "type";
	public static final String LOCKED = "locked";
	public static final String LOCKER = "locker";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof Artifact) ) {
			throw new SerializationException("ArtifactJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			Artifact artifact = (Artifact) o;
			result = new JSONObject();
			result.put(ID, artifact.getId() );
			result.put(NAME, artifact.getName() );
			result.put(DESCRIPTION, artifact.getDescription() );
			result.put(TYPE, artifact.getType() );
			result.put(LOCKED, artifact.getModelLocked() );
			result.put(LOCKER, artifact.getModelLocker() );

		
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}
	

}
