/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.service;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JSONSuccess extends JSONResponse {
	
	public JSONSuccess(JSONObject content, String callback) {
		super(JSONResponse.SUCCESS, content, callback);
	}
	
	public JSONSuccess(JSONArray content, String callback) {
		super(JSONResponse.SUCCESS, content, callback);
	}

	public JSONSuccess(String content, String callback) {
		super(JSONResponse.SUCCESS, content, callback);
	}
	
	public JSONSuccess(String content) {
		super(JSONResponse.SUCCESS, content );
	}
	
	public JSONSuccess(JSONObject content) {
		super(JSONResponse.SUCCESS, content);
	}
	
	public JSONSuccess(JSONArray content) {
		super(JSONResponse.SUCCESS, content);
	}

}
