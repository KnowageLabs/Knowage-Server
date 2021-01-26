package it.eng.knowage.functionscatalog.utils;

public class OutputColumnRuntime extends OutputColumn {

	private String alias;

	public OutputColumnRuntime(String name, String fieldType, String type, String alias) {
		super(name, fieldType, type);
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		return "{" + getName() + ", " + alias + ", " + getFieldType() + ", " + getType() + "}";
	}
}
