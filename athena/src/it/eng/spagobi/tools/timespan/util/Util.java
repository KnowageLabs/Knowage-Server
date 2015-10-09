package it.eng.spagobi.tools.timespan.util;

import it.eng.spagobi.tools.timespan.metadata.SbiTimespan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Util {

	private static final String FROM = "from";
	private static final String TO = "to";
	private static final String DEFINITION = "definition";


	public static JSONObject getAsJSON(SbiTimespan timespan) throws JSONException{
		JSONObject result = new JSONObject(timespan);
		String descr = result.getString(DEFINITION);
		result.put(DEFINITION, new JSONArray(descr));
		return result;
	}

	public JSONObject addSpan(JSONObject spanObj, String from, String to) throws JSONException{
		JSONObject span = new JSONObject();
		span.put(FROM, from).put(TO, to);
		spanObj.append(DEFINITION, span);
		return spanObj;
	}


}
