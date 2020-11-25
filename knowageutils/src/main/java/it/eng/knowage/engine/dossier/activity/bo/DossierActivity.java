package it.eng.knowage.engine.dossier.activity.bo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DossierActivity {

	private Integer id;
	private Integer documentId;
	private String activity;
	private String parameters;
	private Integer partial;
	private Integer total;
	private String status;
	private Integer progressId;
	@JsonIgnore
	private byte[] binContent;
	private boolean hasBinContent;
	private boolean hasDocBinContent;
	private boolean pptExists;
	private Date creationDate;
	private String configContent;

	@JsonIgnore
	private byte[] docBinContent;

	public boolean isHasDocBinContent() {
		return hasDocBinContent;
	}

	public void setHasDocBinContent(boolean hasDocBinContent) {
		this.hasDocBinContent = hasDocBinContent;
	}

	public boolean isHasBinContent() {
		return hasBinContent;
	}

	public void setHasBinContent(boolean hasBinContent) {
		this.hasBinContent = hasBinContent;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public boolean isPptExists() {
		return pptExists;
	}

	public void setPptExists(boolean pptExists) {
		this.pptExists = pptExists;
	}

	public Integer getProgressId() {
		return progressId;
	}

	@JsonIgnore
	public byte[] getBinContent() {
		return binContent;
	}

	@JsonIgnore
	public void setBinContent(byte[] binContent) {
		this.binContent = binContent;
	}

	public void setProgressId(Integer progressId) {
		this.progressId = progressId;
	}

	public Integer getPartial() {
		return partial;
	}

	public void setPartial(Integer partial) {
		this.partial = partial;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public DossierActivity() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public byte[] getDocBinContent() {
		return docBinContent;
	}

	public void setDocBinContent(byte[] docBinContent) {
		this.docBinContent = docBinContent;
	}

	public String getConfigContent() {
		return configContent;
	}

	public void setConfigContent(String configContent) {
		this.configContent = configContent;
	}

}
