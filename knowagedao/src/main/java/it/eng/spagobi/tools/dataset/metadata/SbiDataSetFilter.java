package it.eng.spagobi.tools.dataset.metadata;

public class SbiDataSetFilter {

	private String column;
	private String type;
	private String value;

	public String getColumn() {
		return column;
	}

	public void setColumn(String columnFilter) {
		this.column = columnFilter;
	}

	public String getType() {
		return type;
	}

	public void setType(String typeFilter) {
		this.type = typeFilter;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String valueFilter) {
		this.value = valueFilter;
	}

}
