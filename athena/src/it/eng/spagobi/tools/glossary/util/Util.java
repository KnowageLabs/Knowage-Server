package it.eng.spagobi.tools.glossary.util;

import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;

import org.json.JSONException;
import org.json.JSONObject;

public class Util {

	public static boolean isNumber(String str) {
		return str != null && str.matches("\\d+");
	}

	public static Integer getNumberOrNull(Object str) {
		if(str instanceof Integer)return (Integer)str;
		return isNumber(""+str) ? Integer.valueOf(""+str) : null;
	}

	public static JSONObject fromContentsLight(SbiGlContents sbiGlContents,boolean wordChild) throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put("CONTENT_ID", sbiGlContents.getContentId());
		ret.put("CONTENT_NM", sbiGlContents.getContentNm());
		ret.put("HAVE_WORD_CHILD", wordChild);
		
		return ret;
	}
}
