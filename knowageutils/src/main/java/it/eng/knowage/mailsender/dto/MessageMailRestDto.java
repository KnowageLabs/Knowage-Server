package it.eng.knowage.mailsender.dto;

import java.util.ArrayList;
import java.util.List;

public class MessageMailRestDto {

	private List<String> email;
	private List<String> bcc = new ArrayList<>();
	private List<String> cc = new ArrayList<>();
	private String mailSubject;
	private String mailText;
	private List<AttachmentRestDto> attachment = new ArrayList<>();

	public List<String> getEmail() {
		return email;
	}

	public void setEmail(List<String> email) {
		this.email = email;
	}

	public List<String> getBcc() {
		return bcc;
	}

	public void setBcc(List<String> bcc) {
		this.bcc = bcc;
	}

	public List<String> getCc() {
		return cc;
	}

	public void setCc(List<String> cc) {
		this.cc = cc;
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	public String getMailText() {
		return mailText;
	}

	public void setMailText(String mailText) {
		this.mailText = mailText;
	}

	public List<AttachmentRestDto> getAttachment() {
		return attachment;
	}

	public void setAttachment(List<AttachmentRestDto> attachment) {
		this.attachment = attachment;
	}


}
