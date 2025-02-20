package it.eng.spagobi.mapcatalogue.dao;

public enum SbiLayersEnum {

	GEOJSON("File"), TOPOJSON("topojson"), WKT("wkt");

	public final String key;

	SbiLayersEnum(String key) {
		this.key = key;
	}

}
