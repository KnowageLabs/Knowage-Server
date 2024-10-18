package it.eng.knowage.mailsender.dto;

public class AttachmentRestDto {

	private String nomeFile;
	private String estensione;
	private String attachment;

	public String getNomeFile() {
		return nomeFile;
	}

	public void setNomeFile(String nomeFile) {
		this.nomeFile = nomeFile;
	}

	public String getEstensione() {
		return estensione;
	}

	public void setEstensione(String estensione) {
		this.estensione = estensione;
	}

	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}


}
