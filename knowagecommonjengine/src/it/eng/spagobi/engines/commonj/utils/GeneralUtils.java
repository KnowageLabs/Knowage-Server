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
