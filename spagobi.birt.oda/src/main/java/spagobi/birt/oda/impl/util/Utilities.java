package spagobi.birt.oda.impl.util;

import it.eng.spagobi.utilities.json.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class Utilities {

	public static String addToConfiguration(String configuration, String fieldName, String fieldValue) throws Exception{
		String toReturn = null;
		try{
			JSONObject jsonConf  = null;

			if(configuration == null) jsonConf  = new JSONObject();
			else{
				String config = JSONUtils.escapeJsonString(configuration);

				//jsonConf  = ObjectUtils.toJSONObject(config);
				jsonConf  = toJSONObject(config);
			}
			
			jsonConf.put(fieldName, fieldValue);
		
			toReturn = jsonConf.toString();

		}
		catch (Exception e) {
				throw e;
			}
		return toReturn;
	
	}
	

	// shold call ObjectUtils.toJSONObject but could not initialize class. TODO
	public static  JSONObject toJSONObject(Object o) throws JSONException {
		JSONObject toReturn = null;
		toReturn = null;
		toReturn = new JSONObject( toString(o) );
		return toReturn;	
	}
	
	public static  String toString(Object o) {
		String toReturn;
		toReturn = o.toString();
		return toReturn;	
	}
	

	
}
