package it.eng.spagobi.utilities.json;

import java.io.IOException;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import junit.framework.TestCase;

public class JSONUtilsTest extends TestCase {

	public void testToJSONObjectString() throws JsonMappingException, JsonParseException, JSONException, IOException {
		String json="{}";
		JSONUtils.toJSONObject(json);
		json="a";
		boolean done=false;
		try {
			JSONUtils.toJSONObject(json);
		} catch (Exception e) {
			done=true;
		}
		assertTrue(done);
		json="[]";
		done=false;
		try {
			JSONUtils.toJSONObject(json);
		} catch (Exception e) {
			done=true;
		}
		assertTrue(done);
	}

}
