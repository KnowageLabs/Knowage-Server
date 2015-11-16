package it.eng.spagobi.tools.dataset.utils;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ParamDefaultValue implements Serializable {
	private final String name;
	private final String type;
	private final String defaultValue;

	public ParamDefaultValue(String name, String type, String defaultValue) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

}