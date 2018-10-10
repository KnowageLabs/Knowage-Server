package it.eng.qbe.utility;

import org.json.JSONObject;

public class CustomFunctionsSingleton {

	private static CustomFunctionsSingleton instance;

	JSONObject customizedFunctionsJSON = null;

	public CustomFunctionsSingleton() {
	}

	public JSONObject getCustomizedFunctionsJSON() {
		return customizedFunctionsJSON;
	}

	public void setCustomizedFunctionsJSON(JSONObject customizedFunctionsJSON) {
		this.customizedFunctionsJSON = customizedFunctionsJSON;
	}

	public synchronized static CustomFunctionsSingleton getInstance() {
		if (instance == null) {
			instance = new CustomFunctionsSingleton();
		}
		return instance;
	}

}
