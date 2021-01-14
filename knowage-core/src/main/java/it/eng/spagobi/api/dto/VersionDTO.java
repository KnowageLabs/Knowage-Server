package it.eng.spagobi.api.dto;

import it.eng.knowage.wapp.Version;

public class VersionDTO {

	public String getVersion() {
		return Version.getCompleteVersion();
	}

	public String getReleaseDate() {
		return Version.getReleaseDate();
	}
}
