/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.commonj.utils;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import commonj.work.WorkEvent;

public class GeneralUtils {

	static final String WORK_COMPLETED="Work completed";
	static final String WORK_STARTED="Work started";
	static final String WORK_ACCEPTED="Work accepted";
	static final String WORK_REJECTED="Work rejected";
	static final String WORK_NOT_STARTED="Work not started";


	static public String getEventMessage(int status){
		if(status==WorkEvent.WORK_COMPLETED){
			return WORK_COMPLETED;
		}
		else if(status==WorkEvent.WORK_STARTED){
			return WORK_STARTED;
		}
		else if(status==WorkEvent.WORK_ACCEPTED){
			return WORK_ACCEPTED;			
		}
		else if(status==WorkEvent.WORK_REJECTED){
			return WORK_REJECTED;			
		}
		else if(status==0){
			return WORK_NOT_STARTED;
		}
		return "";

	}

	static public JSONObject buildJSONObject (String pid,int statusCode) throws JSONException{
		String message=GeneralUtils.getEventMessage(statusCode);
		JSONObject info=new JSONObject();
		info.put("pid", pid);
		info.put("status_code", statusCode);
		info.put("status", message);
		info.put("time", (new Date()).toString());
		return info;

	}

}
