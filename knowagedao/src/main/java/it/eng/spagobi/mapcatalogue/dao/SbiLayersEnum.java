package it.eng.spagobi.mapcatalogue.dao;

public enum SbiLayersEnum {

	GEOJSON("geojson"), FILE("file"), TOPOJSON("topojson"), WKT("wkt");

	public final String key;

	SbiLayersEnum(String key) {
		this.key = key;
	}

}
