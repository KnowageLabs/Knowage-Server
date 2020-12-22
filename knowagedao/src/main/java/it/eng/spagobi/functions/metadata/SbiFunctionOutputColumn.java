package it.eng.spagobi.functions.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiFunctionOutputColumn extends SbiHibernateModel {

	private SbiFunctionOutputColumnId id;
	private SbiCatalogFunction sbiCatalogFunction;
	private String colFieldType;
	private String colType;

	public SbiFunctionOutputColumn() {
	}

	public SbiFunctionOutputColumn(SbiFunctionOutputColumnId id, SbiCatalogFunction sbiCatalogFunction, String colFieldType, String colType) {
		this.id = id;
		this.sbiCatalogFunction = sbiCatalogFunction;
		this.colFieldType = colFieldType;
		this.colType = colType;
	}

	public SbiFunctionOutputColumnId getId() {
		return this.id;
	}

	public void setId(SbiFunctionOutputColumnId id) {
		this.id = id;
	}

	public SbiCatalogFunction getSbiCatalogFunction() {
		return this.sbiCatalogFunction;
	}

	public void setSbiCatalogFunction(SbiCatalogFunction sbiCatalogFunction) {
		this.sbiCatalogFunction = sbiCatalogFunction;
	}

	public String getColFieldType() {
		return colFieldType;
	}

	public void setColFieldType(String colFieldType) {
		this.colFieldType = colFieldType;
	}

	public String getColType() {
		return this.colType;
	}

	public void setColType(String colType) {
		this.colType = colType;
	}
}
