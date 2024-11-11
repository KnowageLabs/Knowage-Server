package it.eng.knowage.mailsender.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;

import it.eng.knowage.mail.MailSessionBuilder;
import it.eng.knowage.mail.MailSessionBuilder.SessionFacade;
import it.eng.knowage.mailsender.IMailSender;
import it.eng.knowage.mailsender.dto.MessageMailDto;
import it.eng.knowage.mailsender.dto.ProfileNameMailEnum;
import it.eng.knowage.mailsender.dto.TypeMailEnum;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class JavaApiMailSender implements IMailSender {

	private static Logger logger = Logger.getLogger(JavaApiMailSender.class);

	private static final String NAME_SUFFIX = "NAME_SUFFIX";
	private static final String TEMP_FOLDER_PATH = "TEMP_FOLDER_PATH";

	@Override
	public void sendMail(MessageMailDto messageMailDto) throws Exception {

		SessionFacade facade = this.getSessionFacade(messageMailDto);

		// create a message
		Message msg = facade.createNewMimeMessage();
		this.setRecipients(msg, messageMailDto.getRecipients());
		this.setRecipientsCC(msg, messageMailDto.getRecipientsCC());
		this.setSubject(msg, messageMailDto.getSubject());

		if (messageMailDto.getTypeMailEnum().equals(TypeMailEnum.FOLDER)) {
			Multipart mp = this.getFolderContentMultipart(messageMailDto);
			// add the Multipart to the message
			msg.setContent(mp);
		} else if (messageMailDto.getTypeMailEnum().equals(TypeMailEnum.MULTIPART)) {
			Multipart mp = this.getContentMultipart(messageMailDto);
			// add the Multipart to the message
			msg.setContent(mp);
		} else if (messageMailDto.getTypeMailEnum().equals(TypeMailEnum.CONTENT)) {
			msg.setContent(messageMailDto.getText(), messageMailDto.getContentType());
		} else {
			throw new IllegalArgumentException("TypeMailEnum is not valid or empty");
		}

		// send message
		facade.sendMessage(msg);

	}

	private SessionFacade getSessionFacade(MessageMailDto messageMailDto) throws Exception {

		if (messageMailDto.getProfileName().equals(ProfileNameMailEnum.SCHEDULER)) {
			return MailSessionBuilder.newInstance().usingSchedulerProfile().build();
		} else if (messageMailDto.getProfileName().equals(ProfileNameMailEnum.USER)) {
			return MailSessionBuilder.newInstance().usingUserProfile().withTimeout(5000).withConnectionTimeout(5000).build();
		} else if (messageMailDto.getProfileName().equals(ProfileNameMailEnum.USER_NO_TIMEOUT)) {
			return MailSessionBuilder.newInstance().usingUserProfile().build();
		} else if (messageMailDto.getProfileName().equals(ProfileNameMailEnum.USER_FROM_PWS)) {
			return MailSessionBuilder.newInstance().usingUserProfile().setFromAddress(messageMailDto.getFrom()).setUser(messageMailDto.getLogin())
					.setPassword(messageMailDto.getPassword()).build();
		} else if (messageMailDto.getProfileName().equals(ProfileNameMailEnum.KPI)) {
			return MailSessionBuilder.newInstance().usingKpiAlarmProfile().withTimeout(5000).withConnectionTimeout(5000).build();
		} else {
			throw new IllegalArgumentException("There is not implementation SessionFacade for " + messageMailDto.getProfileName());
		}
	}

	private Multipart getContentMultipart(MessageMailDto messageMailDto) throws Exception {
		// create the Multipart and add its parts to it
		Multipart mp = new MimeMultipart();
		this.setText(mp, messageMailDto);
		this.setFile(mp, messageMailDto);
		return mp;

	}

	private Multipart getFolderContentMultipart(MessageMailDto messageMailDto) throws Exception {
		// create the Multipart and add its parts to it
		Multipart mp = new MimeMultipart();
		this.setText(mp, messageMailDto);
		this.setFolder(mp, messageMailDto);
		return mp;

	}

	private void setFolder(Multipart mp, MessageMailDto messageMailDto) throws Exception {
		if (messageMailDto.getTempFolder() != null) {

			if (messageMailDto.isZipMailDocument()) {
				mp.addBodyPart(this.zipFolder(messageMailDto.getZipFileName(), messageMailDto.getMailOptionFolder(), messageMailDto.getTempFolder()));
			} else {
				List<MimeBodyPart> listBodiPart = this.getListBodyPart(messageMailDto.getTempFolder(), messageMailDto.getContentType());
				for (MimeBodyPart mbp : listBodiPart) {
					mp.addBodyPart(mbp);
				}
			}

		}
	}

	private void setFile(Multipart mp, MessageMailDto messageMailDto) throws Exception {
		if (messageMailDto.getAttach() != null) {
			SchedulerDataSource sds = null;
			MimeBodyPart mbp = new MimeBodyPart();
			// if zip requested
			if (messageMailDto.isZipMailDocument()) {
				mbp = this.zipAttachment(messageMailDto.getAttach(), messageMailDto.getContainedFileName(), messageMailDto.getZipFileName(),
						messageMailDto.getNameSuffix(), messageMailDto.getFileExtension());
			}
			// else
			else {

				String fileName = messageMailDto.getContainedFileName();
				if (messageMailDto.getNameSuffix() != null) {
					fileName = fileName + messageMailDto.getNameSuffix();
				}

				sds = new SchedulerDataSource(messageMailDto.getAttach(), messageMailDto.getContentType(), fileName + messageMailDto.getFileExtension());
				mbp.setDataHandler(new DataHandler(sds));
				mbp.setFileName(sds.getName());
			}
			mp.addBodyPart(mbp);
		}
	}

	private void setText(Multipart mp, MessageMailDto messageMailDto) throws Exception {
		if (messageMailDto.getText() != null) {
			MimeBodyPart mbp = new MimeBodyPart();
			if (messageMailDto.getCharset() != null && !messageMailDto.getCharset().equals("")) {
				mbp.setText(messageMailDto.getText(), messageMailDto.getCharset(), messageMailDto.getSubtype());
			} else {
				mbp.setText(messageMailDto.getText());
			}

			mp.addBodyPart(mbp);
		}

	}

	private void setRecipients(Message msg, String[] recipients) throws Exception {

		if (recipients != null && recipients.length > 0) {
			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
		}

	}

	private void setRecipientsCC(Message msg, String[] recipientsCC) throws Exception {

		if (recipientsCC != null && recipientsCC.length > 0) {
			InternetAddress[] addressCC = new InternetAddress[recipientsCC.length];
			for (int i = 0; i < recipientsCC.length; i++) {
				String ccAdd = recipientsCC[i];
				if ((ccAdd != null) && !ccAdd.trim().equals("")) {
					addressCC[i] = new InternetAddress(recipientsCC[i]);
				}
			}

			msg.setRecipients(Message.RecipientType.CC, addressCC);
		}

	}

	private void setSubject(Message msg, String subject) throws Exception {
		if (subject != null) {
			msg.setSubject(subject);
		}
	}

	// zip single file and return MimeBodyPart to add Multipart
	private MimeBodyPart zipAttachment(byte[] attach, String containedFileName, String zipFileName, String nameSuffix, String fileExtension) throws Exception {
		MimeBodyPart messageBodyPart = null;

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ZipOutputStream zipOut = new ZipOutputStream(bout);
		String entryName = containedFileName + nameSuffix + fileExtension;
		zipOut.putNextEntry(new ZipEntry(entryName));
		zipOut.write(attach);
		zipOut.closeEntry();

		zipOut.close();

		messageBodyPart = new MimeBodyPart();
		DataSource source = new ByteArrayDataSource(bout.toByteArray(), "application/zip");
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(zipFileName + nameSuffix + ".zip");

		return messageBodyPart;
	}

	// zip a folder of file and return MimeBodyPart to add Multipart
	private MimeBodyPart zipFolder(String zipFileName, Map mailOptions, File tempFolder) throws Exception {
		logger.debug("IN");
		MimeBodyPart messageBodyPart = null;

		String nameSuffix = mailOptions.get(NAME_SUFFIX) != null ? (String) mailOptions.get(NAME_SUFFIX) : "";

		byte[] buffer = new byte[4096]; // Create a buffer for copying
		int bytesRead;

		// the zip
		String tempFolderPath = (String) mailOptions.get(TEMP_FOLDER_PATH);

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ZipOutputStream out = new ZipOutputStream(bout);

		logger.debug("File zip to write: " + tempFolderPath + File.separator + "zippedFile.zip");

		// files to zip
		String[] entries = tempFolder.list();

		for (int i = 0; i < entries.length; i++) {
			// File f = new File(tempFolder, entries[i]);
			File f = new File(tempFolder + File.separator + entries[i]);
			if (f.isDirectory()) {
				continue;// Ignore directory
			}
			logger.debug("insert file: " + f.getName());
			try (FileInputStream in = new FileInputStream(f)) {
				ZipEntry entry = new ZipEntry(f.getName()); // Make a ZipEntry
				out.putNextEntry(entry); // Store entry
				while ((bytesRead = in.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}

			} catch (Exception e) {
				logger.error("JavaApiMailSender - Error in zipFolder ", e);
				throw new SpagoBIRuntimeException("JavaApiMailSender - Error in zipFolder");
			}
		}
		out.close();

		messageBodyPart = new MimeBodyPart();
		DataSource source = new ByteArrayDataSource(bout.toByteArray(), "application/zip");
		messageBodyPart.setDataHandler(new DataHandler(source));

		messageBodyPart.setFileName(zipFileName + nameSuffix + ".zip");

		logger.debug("OUT");

		return messageBodyPart;
	}

	// return a list MimeBodyPart from folder to add Multipart
	private List<MimeBodyPart> getListBodyPart(File tempFolder, String contentType) throws Exception {
		logger.debug("Attach single files");
		SchedulerDataSource sds = null;
		MimeBodyPart bodyPart = null;
		List<MimeBodyPart> listBodyPart = new ArrayList<>();

		String[] entries = tempFolder.list();

		for (int i = 0; i < entries.length; i++) {
			logger.debug("Attach file " + entries[i]);
			File f = new File(tempFolder + File.separator + entries[i]);

			byte[] content = this.getBytesFromFile(f);

			bodyPart = new MimeBodyPart();

			sds = new SchedulerDataSource(content, contentType, entries[i]);
			// sds = new SchedulerDataSource(content, contentType, entries[i] + fileExtension);

			bodyPart.setDataHandler(new DataHandler(sds));
			bodyPart.setFileName(sds.getName());

			listBodyPart.add(bodyPart);
			// mp.addBodyPart(bodyPart);
		}

		return listBodyPart;

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

	private class SchedulerDataSource implements DataSource {

		byte[] content = null;
		String name = null;
		String contentType = null;

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			ByteArrayInputStream bais = new ByteArrayInputStream(content);
			return bais;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return null;
		}

		public SchedulerDataSource(byte[] content, String contentType, String name) {
			this.content = content;
			this.contentType = contentType;
			this.name = name;
		}
	}

}
