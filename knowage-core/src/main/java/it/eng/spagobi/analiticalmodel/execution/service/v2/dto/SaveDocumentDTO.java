package it.eng.spagobi.analiticalmodel.execution.service.v2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SaveDocumentDTO {

	@JsonProperty("updateFromWorkspace")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private boolean updateFromWorkspace;
	@JsonProperty("document")
	private DocumentDTO document;
	@JsonProperty("action")
	private String action;
	@JsonProperty("pathInfo")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String pathInfo;
	@JsonProperty("customData")
	private CustomDataDTO customData;
	@JsonProperty("sourceData")
	private SourceDatasetDTO sourceData;
	@JsonProperty("folders")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<FolderDTO> folders;

	public SaveDocumentDTO() {

	}

	public SaveDocumentDTO(boolean updateFromWorkspace, DocumentDTO document, String action, String pathInfo, CustomDataDTO customData,
			SourceDatasetDTO sourceData, List<FolderDTO> folders) {
		this.updateFromWorkspace = updateFromWorkspace;
		this.document = document;
		this.action = action;
		this.pathInfo = pathInfo;
		this.customData = customData;
		this.sourceData = sourceData;
		this.folders = folders;
	}

	public boolean isUpdateFromWorkspace() {
		return updateFromWorkspace;
	}

	public void setUpdateFromWorkspace(boolean updateFromWorkspace) {
		this.updateFromWorkspace = updateFromWorkspace;
	}

	public DocumentDTO getDocumentDTO() {
		return document;
	}

	public void setDocumentPostObject(DocumentDTO documentPostObject) {
		this.document = documentPostObject;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	public CustomDataDTO getCustomDataDTO() {
		return customData;
	}

	public void setCustomDataPostObject(CustomDataDTO customDataPostObject) {
		this.customData = customDataPostObject;
	}

	public SourceDatasetDTO getSourceDatasetDTO() {
		return sourceData;
	}

	public void setSourceDatasetPostObject(SourceDatasetDTO sourceDatasetPostObject) {
		this.sourceData = sourceDatasetPostObject;
	}

	public List<FolderDTO> getFolders() {
		return folders;
	}

	public void setFolders(List<FolderDTO> folders) {
		this.folders = folders;
	}

}
