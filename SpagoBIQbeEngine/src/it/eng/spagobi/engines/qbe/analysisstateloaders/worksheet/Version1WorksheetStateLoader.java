/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.analysisstateloaders.worksheet;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class Version1WorksheetStateLoader extends AbstractWorksheetStateLoader {

	public final static String FROM_VERSION = "1";
    public final static String TO_VERSION = "2";
    
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(Version1WorksheetStateLoader.class);
	
    public Version1WorksheetStateLoader() {
    	super();
    }
    
    public Version1WorksheetStateLoader(IWorksheetStateLoader loader) {
    	super(loader);
    }
    
	@Override
	public JSONObject convert(JSONObject data) {
		logger.debug("IN");
		
		try {
			logger.debug( "Converting from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] ..." );
			
			putOptions(data);

			logger.debug( "Conversion from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] terminated succesfully" );
			
		} catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + data + "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return data;
	}

	private void putOptions(JSONObject data) throws Exception {
		JSONArray options = new JSONArray();
		data.put("fieldsOptions", options);
	}

}
