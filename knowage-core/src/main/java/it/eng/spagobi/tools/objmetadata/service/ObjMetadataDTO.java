package it.eng.spagobi.tools.objmetadata.service;

import javax.validation.constraints.NotNull;

import it.eng.spagobi.services.validation.ExtendedAlphanumeric;
import it.eng.spagobi.services.validation.Xss;

public class ObjMetadataDTO {

	private Integer id;

	@NotNull
	@ExtendedAlphanumeric
	private String label;

	@NotNull
	@ExtendedAlphanumeric
	private String name;

	@Xss
	private String description;

	@NotNull
	@ExtendedAlphanumeric
	private String dataType;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

}
