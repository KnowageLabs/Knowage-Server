package it.eng.qbe.utility.bo;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CustomizedFunctionParameter {

	String name;
	String type;
	final static String NAME = "name";
	final static String TYPE = "type";

	static protected Logger logger = Logger.getLogger(CustomizedFunctionParameter.class);

	public CustomizedFunctionParameter(JSONObject json) {
		logger.debug("IN");
		name = json.optString(NAME);
		type = json.optString(TYPE);
		logger.debug("function parameter" + name + " with code " + type);
		logger.debug("OUT");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
