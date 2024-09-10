package it.eng.spagobi.mapcatalogue.dao;

public enum SbiLayersEnum {

	GEOJSON("File"), TOPOJSON("topojson");

	public final String key;

	SbiLayersEnum(String key) {
		this.key = key;
	}

}
