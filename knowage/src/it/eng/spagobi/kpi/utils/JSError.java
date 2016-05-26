package it.eng.spagobi.kpi.utils;

import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;

import java.text.MessageFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSError {
	private static final String MESSAGE = "message";
	private final JSONObject jsError = new JSONObject();

	private static enum MSG_TYPE {
		errors, warnings
	};

	public JSError() {
	}

	public JSError addErrorKey(String msgKey, String... args) {
		return addMsgKey(msgKey, MSG_TYPE.errors, args);
	}

	public JSError addError(String msg) {
		return addMsg(msg, MSG_TYPE.errors);
	}

	public JSError addWarningKey(String msgKey, String... args) {
		return addMsgKey(msgKey, MSG_TYPE.warnings, args);
	}

	public JSError addWarning(String msg) {
		return addMsg(msg, MSG_TYPE.warnings);
	}

	public JSError addMsg(String msg, MSG_TYPE type) {
		try {
			if (!has(type)) {
				jsError.put(type.name(), new JSONArray());
			}
			get(type).put(new JSONObject().put(MESSAGE, getMessage(msg)));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public JSError addMsgKey(String msgKey, MSG_TYPE type, String... args) {
		try {
			if (!has(type)) {
				jsError.put(type.name(), new JSONArray());
			}
			get(type).put(new JSONObject().put(MESSAGE, getMessage(msgKey, args)));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public boolean hasErrors() {
		return has(MSG_TYPE.errors);
	}

	public boolean hasWarnings() {
		return has(MSG_TYPE.warnings);
	}

	private boolean has(MSG_TYPE type) {
		return jsError.has(type.name()) && get(type).length() > 0;
	}

	@Override
	public String toString() {
		return hasErrors() || hasWarnings() ? jsError.toString() : "";
	}

	private static String getMessage(String key, String... args) {
		String msg = MessageBuilderFactory.getMessageBuilder().getMessage(key);
		if (args.length > 0) {
			msg = MessageFormat.format(msg, args);
		}
		return msg;
	}

	private JSONArray getErrors() {
		return get(MSG_TYPE.errors);
	}

	private JSONArray getWarnings() {
		return get(MSG_TYPE.warnings);
	}

	private JSONArray get(MSG_TYPE msgType) {
		try {
			return jsError.getJSONArray(msgType.name());
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
