package it.eng.spagobi.api;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.eng.spagobi.services.validation.ExtendedAlphanumeric;

class DeleteViewpointRequestDTO {

	private String name = null;

	@JsonProperty("VIEWPOINT")
	@ExtendedAlphanumeric
	@NotNull
	public String getName() {
		return name;
	}

}
