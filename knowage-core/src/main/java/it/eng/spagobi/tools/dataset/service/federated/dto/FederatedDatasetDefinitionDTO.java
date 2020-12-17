package it.eng.spagobi.tools.dataset.service.federated.dto;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;

import it.eng.spagobi.services.validation.Xss;

public class FederatedDatasetDefinitionDTO {

	private String label;
	private String name;
	private String description;
	private boolean degenerated;
	private List<List<Map<String, Object>>> relationships;

	public String getDescription() {
		return description;
	}

	@Xss
	@Size(max = 40)
	public String getLabel() {
		return label;
	}

	public String getName() {
		return name;
	}

	public List<List<Map<String, Object>>> getRelationships() {
		return relationships;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public boolean isDegenerated() {
		return degenerated;
	}

	public void setDegenerated(boolean degenerated) {
		this.degenerated = degenerated;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRelationships(List<List<Map<String, Object>>> relationships) {
		this.relationships = relationships;
	}

}
