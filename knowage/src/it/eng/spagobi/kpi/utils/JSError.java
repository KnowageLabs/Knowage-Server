package it.eng.spagobi.kpi.utils;

import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;

import java.text.MessageFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSError {
	private static final String ERRORS = "errors";
	private static final String MESSAGE = "message";
	private final JSONObject jsError = new JSONObject();

	public JSError() {
		try {
			jsError.put(ERRORS, new JSONArray());
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public JSError addErrorKey(String msgKey, String... args) {
		try {
			getErrors().put(new JSONObject().put(MESSAGE, getMessage(msgKey, args)));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public JSError addError(String msg) {
		try {
			getErrors().put(new JSONObject().put(MESSAGE, getMessage(msg)));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public boolean hasErrors() {
		return getErrors().length() > 0;
	}

	@Override
	public String toString() {
		return getErrors().length() > 0 ? jsError.toString() : "";
	}

	private static String getMessage(String key, String... args) {
		String msg = MessageBuilderFactory.getMessageBuilder().getMessage(key);
		if (args.length > 0) {
			msg = MessageFormat.format(msg, args);
		}
		return msg;
	}

	private JSONArray getErrors() {
		try {
			return jsError.getJSONArray(ERRORS);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
