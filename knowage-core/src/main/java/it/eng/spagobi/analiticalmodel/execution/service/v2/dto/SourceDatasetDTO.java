package it.eng.spagobi.analiticalmodel.execution.service.v2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SourceDatasetDTO {

	@JsonProperty("label")
	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public SourceDatasetDTO() {

	}

	public SourceDatasetDTO(String label) {
		this.label = label;
	}

}
