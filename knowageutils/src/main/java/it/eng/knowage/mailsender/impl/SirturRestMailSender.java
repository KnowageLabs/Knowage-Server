package it.eng.knowage.mailsender.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.knowage.mailsender.IMailSender;
import it.eng.knowage.mailsender.dto.AttachmentRestDto;
import it.eng.knowage.mailsender.dto.MessageMailDto;
import it.eng.knowage.mailsender.dto.MessageMailRestDto;
import it.eng.knowage.mailsender.dto.TypeMailEnum;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SirturRestMailSender implements IMailSender {

	private static Logger logger = Logger.getLogger(SirturRestMailSender.class);

	public static final String LABEL_MAILSENDER = "SIRTUR";

	private static final String TOKEN_URL = "MAIL_SENDER.SIRTUR.TOKEN_URL";
	private static final String CLIENT_ID = "MAIL_SENDER.SIRTUR.CLIENT_ID";
	private static final String CLIENT_SECRET = "MAIL_SENDER.SIRTUR.PASSWORD";
	private static final String SENDMAIL_URL = "MAIL_SENDER.SIRTUR.SENDMAIL_URL";
	private static final String NAME_SUFFIX = "NAME_SUFFIX";

	@Override
	public void sendMail(MessageMailDto messageMailDto) throws Exception {
		logger.debug("Start RestMailSender");
		String token = this.getToken();

		MessageMailRestDto messageMailRestDto = this.mapperDto(messageMailDto);

		this.sendMail(token, messageMailRestDto);
		logger.debug("END RestMailSender");

	}

	private MessageMailRestDto mapperDto(MessageMailDto messageMailDto) throws Exception {
		MessageMailRestDto messageMailRestDto = new MessageMailRestDto();

		messageMailRestDto.setEmail(Arrays.asList(messageMailDto.getRecipients()));

		if (messageMailDto.getRecipientsCC() != null && messageMailDto.getRecipientsCC().length > 0) {
			messageMailRestDto.setCc(Arrays.asList(messageMailDto.getRecipientsCC()));
		}

		messageMailRestDto.setMailSubject(messageMailDto.getSubject());
		messageMailRestDto.setMailText(messageMailDto.getText());

		this.setAttachmentBySingleFile(messageMailDto, messageMailRestDto);
		this.setAttachmentByFolder(messageMailDto, messageMailRestDto);

		return messageMailRestDto;
	}

	private void setAttachmentByFolder(MessageMailDto messageMailDto, MessageMailRestDto messageMailRestDto) throws Exception {
		if (messageMailDto.getTypeMailEnum().equals(TypeMailEnum.FOLDER) && messageMailDto.getTempFolder() != null) {

			if (messageMailDto.getTempFolder() == null) {
				throw new IllegalArgumentException("The Folder temp not can be null");
			}

			if (messageMailDto.isZipMailDocument()) {
				AttachmentRestDto attachmentRestDto = new AttachmentRestDto();
				attachmentRestDto.setEstensione("zip");
				String nameSuffix = messageMailDto.getMailOptionFolder().get(NAME_SUFFIX) != null
						? (String) messageMailDto.getMailOptionFolder().get(NAME_SUFFIX)
						: "";
				attachmentRestDto.setNomeFile(messageMailDto.getZipFileName() + nameSuffix);
				attachmentRestDto.setAttachment(Base64.getEncoder().encodeToString(this.zipFolder(messageMailDto.getTempFolder())));
				messageMailRestDto.setAttachment(Arrays.asList(attachmentRestDto));
			} else {
				this.addAllFilesFolder(messageMailDto, messageMailRestDto);
			}
		}

	}

	private void setAttachmentBySingleFile(MessageMailDto messageMailDto, MessageMailRestDto messageMailRestDto) throws Exception {
		if (messageMailDto.getTypeMailEnum().equals(TypeMailEnum.MULTIPART)) {

			if (messageMailDto.getAttach() == null) {
				throw new IllegalArgumentException("The attach file not can be null");
			}

			AttachmentRestDto attachmentRestDto = new AttachmentRestDto();
			if (messageMailDto.isZipMailDocument()) {
				attachmentRestDto.setEstensione("zip");
				attachmentRestDto.setNomeFile(messageMailDto.getZipFileName() + messageMailDto.getNameSuffix());
				attachmentRestDto.setAttachment(Base64.getEncoder().encodeToString(this.zipAttachment(messageMailDto)));

			} else {

				String fileName = messageMailDto.getContainedFileName();
				if (messageMailDto.getNameSuffix() != null) {
					fileName = fileName + messageMailDto.getNameSuffix();
				}
				String fileFullName = fileName + messageMailDto.getFileExtension();
				attachmentRestDto.setEstensione(FilenameUtils.getExtension(fileFullName));
				attachmentRestDto.setNomeFile(FilenameUtils.getBaseName(fileFullName));
				attachmentRestDto.setAttachment(Base64.getEncoder().encodeToString(messageMailDto.getAttach()));
			}
			messageMailRestDto.setAttachment(Arrays.asList(attachmentRestDto));
		}

	}

	private void sendMail(String token, MessageMailRestDto messageMailRestDto) {

		Client restClient = ClientBuilder.newClient();
		WebTarget target = restClient.target(SingletonConfig.getInstance().getConfigValue(SENDMAIL_URL));
		MultivaluedMap<String, Object> myHeaders = new MultivaluedHashMap<>();
		myHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer  " + token);
		myHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");

		Response response = target.request().headers(myHeaders).post(Entity.json(messageMailRestDto));

		if (response.getStatus() != Status.OK.getStatusCode()) {
			logger.error("Impossible sendMail  - status " + response.getStatus() + " - reason " + response.getStatusInfo().getReasonPhrase());
			throw new ServerErrorException(response);
		}

		logger.info("sendMail response "+response.readEntity(String.class));

	}

	private String getToken() throws Exception {

		String key = SingletonConfig.getInstance().getConfigValue(CLIENT_ID) + ":" + SingletonConfig.getInstance().getConfigValue(CLIENT_SECRET);

		String keyEncoded = Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));

		Client restClient = ClientBuilder.newClient();
		WebTarget target = restClient.target(SingletonConfig.getInstance().getConfigValue(TOKEN_URL));

		MultivaluedMap<String, Object> myHeaders = new MultivaluedHashMap<>();
		myHeaders.add(HttpHeaders.AUTHORIZATION, "Basic " + keyEncoded);
		myHeaders.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

		Form form = new Form().param("grant_type", "client_credentials");

		Response response = target.request().headers(myHeaders).post(Entity.form(form));

		if (response.getStatus() != Status.OK.getStatusCode()) {
			logger.error("Impossible get token  - status " + response.getStatus() + " - reason " + response.getStatusInfo().getReasonPhrase());
			throw new ServerErrorException(response);
		}

		String token = response.readEntity(String.class);
		JSONObject obj = new JSONObject(token);

		return obj.getString("access_token");

	}

	private byte[] zipAttachment(MessageMailDto messageMailDto) throws Exception {

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ZipOutputStream zipOut = new ZipOutputStream(bout);
		String entryName = messageMailDto.getContainedFileName() + messageMailDto.getNameSuffix() + messageMailDto.getFileExtension();
		zipOut.putNextEntry(new ZipEntry(entryName));
		zipOut.write(messageMailDto.getAttach());
		zipOut.closeEntry();

		zipOut.close();

		return bout.toByteArray();
	}

	public byte[] zipFolder(File tempFolder) throws Exception {

		byte[] buffer = new byte[4096]; // Create a buffer for copying
		int bytesRead;

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ZipOutputStream out = new ZipOutputStream(bout);

		// files to zip
		String[] entries = tempFolder.list();

		for (int i = 0; i < entries.length; i++) {
			// File f = new File(tempFolder, entries[i]);
			File f = new File(tempFolder + File.separator + entries[i]);
			if (f.isDirectory()) {
				continue;// Ignore directory
			}

			try (FileInputStream in = new FileInputStream(f)) {
				ZipEntry entry = new ZipEntry(f.getName()); // Make a ZipEntry
				out.putNextEntry(entry); // Store entry
				while ((bytesRead = in.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
			} catch (Exception e) {
				logger.error("SirturRestMailSender - Error in zipFolder ", e);
				throw new SpagoBIRuntimeException("SirturRestMailSender - Error in zipFolder");
			}
		}
		out.close();

		return bout.toByteArray();
	}

	private void addAllFilesFolder(MessageMailDto messageMailDto, MessageMailRestDto messageMailRestDto) throws Exception {

		String[] entries = messageMailDto.getTempFolder().list();
		List<AttachmentRestDto> listAttach = new ArrayList<>();
		for (int i = 0; i < entries.length; i++) {

			File f = new File(messageMailDto.getTempFolder() + File.separator + entries[i]);

			byte[] content = getBytesFromFile(f);

			AttachmentRestDto attachmentRestDto = new AttachmentRestDto();
			attachmentRestDto.setNomeFile(FilenameUtils.getBaseName(entries[i]));
			attachmentRestDto.setEstensione(FilenameUtils.getExtension(entries[i]));
			attachmentRestDto.setAttachment(Base64.getEncoder().encodeToString(content));
			listAttach.add(attachmentRestDto);

		}

		messageMailRestDto.setAttachment(listAttach);

	}

	// Returns the contents of the file in a byte array.
	private byte[] getBytesFromFile(File file) throws IOException {
		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
			throw new IOException("File is too large!");
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;

		try (InputStream is = new FileInputStream(file)) {
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
		}

		return bytes;
	}

}
