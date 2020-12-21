package it.eng.spagobi.api.dto;

import java.util.List;

public class FunctionExecutionResourceDTO {

	private int id;
	private Integer subobjectId;
	private List<FunctionExecutionResourceMetadataDTO> jsonMeta;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Integer getSubobjectId() {
		return subobjectId;
	}

	public void setSubobjectId(int subobjectId) {
		this.subobjectId = subobjectId;
	}

	public List<FunctionExecutionResourceMetadataDTO> getJsonMeta() {
		return jsonMeta;
	}

	public void setJsonMeta(List<FunctionExecutionResourceMetadataDTO> jsonMeta) {
		this.jsonMeta = jsonMeta;
	}

}
