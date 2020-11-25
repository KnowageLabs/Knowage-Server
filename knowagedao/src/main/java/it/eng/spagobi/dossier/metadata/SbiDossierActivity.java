package it.eng.spagobi.dossier.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.massiveExport.metadata.SbiProgressThread;

public class SbiDossierActivity extends SbiHibernateModel {

	private static final long serialVersionUID = 1104724670349915546L;

	private Integer id;
	private Integer documentId;
	private String activity;
	private String parameters;
	private SbiProgressThread progress;
	private byte[] binContent;
	private byte[] docBinContent;
	private String configContent;

	public String getConfigContent() {
		return configContent;
	}

	public void setConfigContent(String configContent) {
		this.configContent = configContent;
	}

	public byte[] getBinContent() {
		return binContent;
	}

	public void setBinContent(byte[] binContent) {
		this.binContent = binContent;
	}

	public SbiDossierActivity() {

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public SbiProgressThread getProgress() {
		return progress;
	}

	public void setProgress(SbiProgressThread progress) {
		this.progress = progress;
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

}
