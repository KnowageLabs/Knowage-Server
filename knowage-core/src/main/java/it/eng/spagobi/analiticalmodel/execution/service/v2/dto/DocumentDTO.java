/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.analiticalmodel.execution.service.v2.dto;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.eng.spagobi.services.validation.Alphanumeric;
import it.eng.spagobi.services.validation.ExtendedAlphanumeric;

public class DocumentDTO {

	@JsonProperty("id")
	private int id;

	@JsonProperty("name")
	@ExtendedAlphanumeric
	@Size(max = 200)
	private String name;

	@JsonProperty("label")
	@NotNull
	@Alphanumeric
	@Size(max = 100)
	private String label;

	@JsonProperty("description")
	@ExtendedAlphanumeric
	@Size(max = 400)
	private String description;

	@JsonProperty("type")
	private String type;

	@JsonProperty("previewFile")
	private String previewFile;

	@JsonProperty("visibility")
	private String visibility;

	@JsonProperty("engineId")
	@NotNull
	private String engineId;

	private List<MetadataDTO> metadataPostObjects;

	public DocumentDTO() {

	}

	public DocumentDTO(Integer id, String name, String label, String description, String type, String previewFile, String visibility, String engineId,
			List<MetadataDTO> metadataPostObjects) {
		this.id = id;
		this.name = name;
		this.label = label;
		this.description = description;
		this.type = type;
		this.previewFile = previewFile;
		this.visibility = visibility;
		this.engineId = engineId;
		this.metadataPostObjects = metadataPostObjects;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPreviewFile() {
		return previewFile;
	}

	public void setPreviewFile(String previewFile) {
		this.previewFile = previewFile;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getEngineId() {
		return engineId;
	}

	public void setEngineId(String engineId) {
		this.engineId = engineId;
	}

	public List<MetadataDTO> getMetadataDTOs() {
		return metadataPostObjects;
	}

	public void setMetadataPostObjects(List<MetadataDTO> metadataPostObjects) {
		this.metadataPostObjects = metadataPostObjects;
	}

}
