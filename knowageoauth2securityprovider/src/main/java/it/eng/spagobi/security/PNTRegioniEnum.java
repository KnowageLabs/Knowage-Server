package it.eng.spagobi.security;

import java.util.Objects;

public enum PNTRegioniEnum {
	PIEMONTE("010", "Regione Piemonte"), AOSTA("020", "Regione Autonoma Val d'Aosta"), LOMBARDIA("030", "Regione Lombardia"),
	BOLZANO("041", "Provincia autonoma di Bolzano"), TRENTO("042", "Provincia autonoma di Trento"), VENETO("050", "Regione Veneto"),
	FRIULI("060", "Regione Friuli Venezia Giulia"), LIGURIA("070", "Regione Liguria"), EMILIA("080", "Regione Emilia Romagna"),
	TOSCANA("090", "Regione Toscana"), UMBRIA("100", "Regione Umbria"), MARCHE("110", "Regione Marche"), LAZIO("120", "Regione Lazio"),
	ABRUZZO("130", "Regione Abruzzo"), MOLISE("140", "Regione Molise"), CAMPANIA("150", "Regione Campania"), PUGLIA("160", "Regione Puglia"),
	BASILICATA("170", "Regione Basilicata"), CALABRIA("180", "Regione Calabria"), SICILIA("190", "Regione Sicilia"), SARDEGNA("200", "Regione Sardegna"),
	GENEOVA("001", "SASN sede di Genova"), NAPOLI("002", "SASN sede di Napoli"), ITALIA("000", "Italia");

	PNTRegioniEnum(String code, String description) {
		this.code = code;
		this.description = description;
	}

	private final String code;
	private final String description;


	public static String getDescriptionFromCode(String code) {
		for (PNTRegioniEnum e : values()) {
			if (Objects.equals(e.code, code)) {
				return e.description;
			}
		}
		return null;
	}


}
