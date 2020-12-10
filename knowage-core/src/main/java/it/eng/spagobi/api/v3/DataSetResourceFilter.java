package it.eng.spagobi.api.v3;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;

public class DataSetResourceFilter {

	private String columnFilter;
	private String typeValueFilter;
	private String typeFilter;
	private String valueFilter;

	public String getColumnFilter() {
		return columnFilter;
	}

	public void setColumnFilter(String columnFilter) {
		this.columnFilter = columnFilter;
	}

	public String getTypeValueFilter() {
		return typeValueFilter;
	}

	public void setTypeValueFilter(String typeValueFilter) {
		this.typeValueFilter = typeValueFilter;
	}

	public String getTypeFilter() {
		return typeFilter;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public void setTypeFilter(String typeFilter) {
		this.typeFilter = typeFilter;
	}

	public String getValueFilter() {
		return valueFilter;
	}

	public void setValueFilter(String valueFilter) {
		this.valueFilter = valueFilter;
	}

	public static DataSetResourceFilter valueOf(String json) {
		return new Gson().fromJson(json, DataSetResourceFilter.class);
	}
}
