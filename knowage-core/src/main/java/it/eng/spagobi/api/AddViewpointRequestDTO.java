package it.eng.spagobi.api;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.eng.spagobi.services.validation.Alphanumeric;
import it.eng.spagobi.services.validation.ExtendedAlphanumeric;

class AddViewpointRequestDTO {
	private String name;
	private String description;
	private String scope;
	private String objLabel;
	private String role;
	private final Map<String,String> viewpoint = new LinkedHashMap<>();

	@JsonProperty("NAME")
	@ExtendedAlphanumeric
	@NotNull
	@Size(max = 40)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("DESCRIPTION")
	@ExtendedAlphanumeric
	@NotNull
	@Size(max = 160)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("SCOPE")
	@Alphanumeric
	@NotNull
	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	@JsonProperty("OBJECT_LABEL")
	@ExtendedAlphanumeric
	public String getObjLabel() {
		return objLabel;
	}

	public void setObjLabel(String object_label) {
		this.objLabel = object_label;
	}

	@JsonProperty("ROLE")
	@ExtendedAlphanumeric
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@JsonProperty("VIEWPOINT")
	@Size(min = 1)
	public Map<String, String> getViewpoint() {
		return viewpoint;
	}

}
