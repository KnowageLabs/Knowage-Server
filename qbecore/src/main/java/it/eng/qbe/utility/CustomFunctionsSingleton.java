package it.eng.qbe.utility;

import org.json.JSONObject;

public class CustomFunctionsSingleton {

	private static final CustomFunctionsSingleton INSTANCE = new CustomFunctionsSingleton();

	public static synchronized CustomFunctionsSingleton getInstance() {
		return INSTANCE;
	}

	JSONObject customizedFunctionsJSON = null;

	private CustomFunctionsSingleton() {
	}

	public JSONObject getCustomizedFunctionsJSON() {
		return customizedFunctionsJSON;
	}

	public void setCustomizedFunctionsJSON(JSONObject customizedFunctionsJSON) {
		this.customizedFunctionsJSON = customizedFunctionsJSON;
	}

}
