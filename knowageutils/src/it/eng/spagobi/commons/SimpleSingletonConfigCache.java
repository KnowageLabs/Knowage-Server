package it.eng.spagobi.commons;

import java.util.HashMap;
import java.util.Map;

public class SimpleSingletonConfigCache implements ISingletonConfigCache {

	private final Map<String, String> properties = new HashMap<>();

	public void setProperty(String key, String value) {
		properties.put(key, value);
	}

	@Override
	public String get(String key) {
		return properties.get(key);
	}

}
