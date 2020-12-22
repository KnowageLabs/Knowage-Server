package it.eng.spagobi.functions.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiFunctionInputColumn extends SbiHibernateModel {

	private SbiFunctionInputColumnId id;
	private SbiCatalogFunction sbiCatalogFunction;
	private String colType;

	public SbiFunctionInputColumn() {
	}

	public SbiFunctionInputColumn(SbiFunctionInputColumnId id, SbiCatalogFunction sbiCatalogFunction, String colType) {
		this.id = id;
		this.sbiCatalogFunction = sbiCatalogFunction;
		this.colType = colType;
	}

	public SbiFunctionInputColumnId getId() {
		return this.id;
	}

	public void setId(SbiFunctionInputColumnId id) {
		this.id = id;
	}

	public SbiCatalogFunction getSbiCatalogFunction() {
		return this.sbiCatalogFunction;
	}

	public void setSbiCatalogFunction(SbiCatalogFunction sbiCatalogFunction) {
		this.sbiCatalogFunction = sbiCatalogFunction;
	}

	public String getColType() {
		return this.colType;
	}

	public void setColType(String colType) {
		this.colType = colType;
	}
}
