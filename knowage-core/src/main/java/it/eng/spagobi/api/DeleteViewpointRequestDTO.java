package it.eng.spagobi.api;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.eng.spagobi.services.validation.ExtendedAlphanumeric;

class DeleteViewpointRequestDTO {

	private final List<String> names = new ArrayList<>();

	@JsonProperty("VIEWPOINT")
	@ExtendedAlphanumeric
	@NotNull
	@Size(max = 40)
	public List<String> getNames() {
		return names;
	}

}
