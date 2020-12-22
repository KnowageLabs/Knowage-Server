package it.eng.knowage.functionscatalog.utils;

import it.eng.spagobi.functions.metadata.IInputVariable;

public class InputVariable implements IInputVariable {
	private String name;
	private String type;
	private String value;

	public InputVariable(String name, String type, String value) {
		super();
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "{" + name + ", " + type + ", " + value + "}";
	}

}
