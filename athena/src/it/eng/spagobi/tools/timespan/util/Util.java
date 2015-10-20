package it.eng.spagobi.tools.timespan.util;

import it.eng.spagobi.tools.timespan.metadata.SbiTimespan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Util {

	private static final String DEFINITION = "definition";


	public static JSONObject getAsJSON(SbiTimespan timespan) throws JSONException{
		JSONObject result = new JSONObject(timespan);
		String descr = result.getString(DEFINITION);
		if(descr.startsWith("[")){
			result.put(DEFINITION, new JSONArray(descr));
		} else if(descr.startsWith("{")){
			result.put(DEFINITION, new JSONObject(descr));
		}
		return result;
	}


}
