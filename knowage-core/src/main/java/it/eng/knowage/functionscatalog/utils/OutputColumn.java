package it.eng.knowage.functionscatalog.utils;

import it.eng.spagobi.functions.metadata.IOutputColumn;

public class OutputColumn implements IOutputColumn {

	private String name;
	private String fieldType;
	private String type;

	public OutputColumn(String name, String fieldType, String type) {
		super();
		this.name = name;
		this.fieldType = fieldType;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "{" + name + ", " + fieldType + ", " + type + "}";
	}
}
