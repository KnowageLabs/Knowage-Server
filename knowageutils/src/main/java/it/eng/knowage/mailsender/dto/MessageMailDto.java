package it.eng.knowage.mailsender.dto;

import java.io.File;
import java.util.Map;

public class MessageMailDto {

	private String[] recipients;
	private String[] recipientsCC;
	private String subject;
	private String text;
	private String charset;
	private String subtype;
	private byte[] attach;
	private String containedFileName;
	private String zipFileName;
	private String nameSuffix;
	private String contentType;
	private String fileExtension;
	private ProfileNameMailEnum profileName;
	private String from;
	private String login;
	private String password;
	private Map<String, Object> mailOptionFolder;
	private File tempFolder;
	private boolean zipMailDocument;
	private TypeMailEnum typeMailEnum;

	public ProfileNameMailEnum getProfileName() {
		return profileName;
	}

	public void setProfileName(ProfileNameMailEnum profileName) {
		this.profileName = profileName;
	}

	public boolean isZipMailDocument() {
		return zipMailDocument;
	}

	public void setZipMailDocument(boolean zipMailDocument) {
		this.zipMailDocument = zipMailDocument;
	}

	public byte[] getAttach() {
		return attach;
	}

	public void setAttach(byte[] attach) {
		this.attach = attach;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public String getContainedFileName() {
		return containedFileName;
	}

	public void setContainedFileName(String containedFileName) {
		this.containedFileName = containedFileName;
	}

	public String getZipFileName() {
		return zipFileName;
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}

	public String getNameSuffix() {
		return nameSuffix;
	}

	public void setNameSuffix(String nameSuffix) {
		this.nameSuffix = nameSuffix;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String[] getRecipients() {
		return recipients;
	}

	public void setRecipients(String[] recipients) {
		this.recipients = recipients;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}


	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String[] getRecipientsCC() {
		return recipientsCC;
	}

	public void setRecipientsCC(String[] recipientsCC) {
		this.recipientsCC = recipientsCC;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public Map<String, Object> getMailOptionFolder() {
		return mailOptionFolder;
	}

	public void setMailOptionFolder(Map<String, Object> mailOptionFolder) {
		this.mailOptionFolder = mailOptionFolder;
	}

	public File getTempFolder() {
		return tempFolder;
	}

	public void setTempFolder(File tempFolder) {
		this.tempFolder = tempFolder;
	}

	public TypeMailEnum getTypeMailEnum() {
		return typeMailEnum;
	}

	public void setTypeMailEnum(TypeMailEnum typeMailEnum) {
		this.typeMailEnum = typeMailEnum;
	}

}
