/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.tools.scheduler.dispatcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import it.eng.knowage.mail.MailSessionBuilder;
import it.eng.knowage.mail.MailSessionBuilder.SessionFacade;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Giulio Gavardi
 *
 */
public class UniqueMailDocumentDispatchChannel implements IDocumentDispatchChannel {

	private DispatchContext dispatchContext;

	// logger component
	private static Logger logger = Logger.getLogger(UniqueMailDocumentDispatchChannel.class);

	// COnfigurations stored in mailOptions Map
	public static final String MAIL_SUBJECT = "MAIL_SUBJECT";
	public static final String RECIPIENTS = "RECIPIENTS";
	public static final String DOCUMENT_NAME = "DOCUMENT_NAME";
	public static final String NAME_SUFFIX = "NAME_SUFFIX";
	public static final String MAIL_TXT = "MAIL_TXT";
	public static final String DESCRIPTION_SUFFIX = "DESCRIPTION_SUFFIX";
	public static final String CONTAINED_FILE_NAME = "CONTAINED_FILE_NAME";
	public static final String ZIP_FILE_NAME = "ZIP_FILE_NAME";
	public static final String FILE_EXTENSION = "FILE_EXTENSION";
	public static final String CONTENT_TYPE = "CONTENT_TYPE";
	public static final String TEMP_FOLDER_PATH = "TEMP_FOLDER_PATH";
	public static final String TEMP_FOLDER_NAME = "TEMP_FOLDER_NAME";
	public static final String DOCUMENT_STATE_CODE = "DOCUMENT_STATE_CODE";
	public static final String PARAMETERS_MAP = "PARAMETERS_MAP";
	public static final String REPORT_NAME_IN_SUBJECT = "REPORT_NAME_IN_SUBJECT";
	public static final String DOCUMENT_LABELS = "DOCUMENT_LABELS";
	public static final String IS_ZIP_DOCUMENT = "IS_ZIP_DOCUMENT";

	public UniqueMailDocumentDispatchChannel() {
	}

	public UniqueMailDocumentDispatchChannel(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
		try {
			IEngUserProfile userProfile = this.dispatchContext.getUserProfile();
			// gets the dataset data about the email address
			IDataStore emailDispatchDataStore = null;
			if (dispatchContext.isUseDataSet()) {
				IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(dispatchContext.getDataSetLabel());
				// loadActiveDataSetByLabel(dispatchContext.getDataSetLabel());
				dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(userProfile));
				dataSet.loadData();
				emailDispatchDataStore = dataSet.getDataStore();
			}
			dispatchContext.setEmailDispatchDataStore(emailDispatchDataStore);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to instatiate MailDocumentDispatchChannel class", t);
		}
	}

	@Override
	public void setDispatchContext(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
	}

	@Override
	public void close() {

	}

	@Override
	public boolean canDispatch(BIObject document) {
		return canDispatch(dispatchContext, document, dispatchContext.getEmailDispatchDataStore());
	}

	/**
	 * dispatch in this case does not send mail, but store files in temporar folder
	 */

	@Override
	public boolean dispatch(BIObject document, byte[] executionOutput) {
		String fileExtension;
		String nameSuffix;
		String containedFileName;

		logger.debug("IN");
		try {
			fileExtension = dispatchContext.getFileExtension();
			nameSuffix = dispatchContext.getNameSuffix();
			containedFileName = dispatchContext.getContainedFileName() != null
					&& !dispatchContext.getContainedFileName().equals("") ? dispatchContext.getContainedFileName()
							: document.getName();

			// check if temp folder is already created otherwise create it

			String tempFolderPath = dispatchContext.getTempFolderPath();

			File folder = new File(tempFolderPath);

			if (!folder.exists()) {
				logger.debug("Temporary Folder not retrieved: " + folder.getAbsolutePath());
				throw new Exception("Temporary Folder not retrieved: " + folder.getAbsolutePath());
			}

			logger.debug("Temporary Folder retrieved: " + folder.getAbsolutePath());

			// create file inside temp directory

			String fileToCreate = containedFileName + nameSuffix + fileExtension;
			logger.debug("File to store in temporary folder: " + fileToCreate);
			String pathToCreate = folder.getAbsolutePath() + File.separator + fileToCreate;

			FileOutputStream fileOuputStream = new FileOutputStream(pathToCreate);
			fileOuputStream.write(executionOutput);
			fileOuputStream.close();

			logger.debug("File stored");

		} catch (Exception e) {
			logger.error("Error while sending schedule result mail", e);
			return false;
		} finally {
			logger.debug("OUT");
		}
		return true;
	}

	/**
	 * AFter all files are stored in temporary tabe takes them and sens as zip or as separate attachments
	 *
	 * @param mailOptions
	 * @return
	 */

	public boolean sendFiles(Map<String, Object> mailOptions, String allDocumentLabels) {

		logger.debug("IN");
		try {
			String tempFolderPath = (String) mailOptions.get(TEMP_FOLDER_PATH);

			File tempFolder = new File(tempFolderPath);

			if (!tempFolder.exists() || !tempFolder.isDirectory()) {
				logger.error(
						"Temp Folder " + tempFolderPath + " does not exist or is not a directory: stop sending mail");
				return false;
			}

			SessionFacade facade = MailSessionBuilder.newInstance().usingSchedulerProfile().build();

			String mailSubj = mailOptions.get(MAIL_SUBJECT) != null ? (String) mailOptions.get(MAIL_SUBJECT) : null;
			Map parametersMap = mailOptions.get(PARAMETERS_MAP) != null ? (Map) mailOptions.get(PARAMETERS_MAP) : null;
			mailSubj = StringUtilities.substituteParametersInString(mailSubj, parametersMap, null, false);

			String mailTxt = mailOptions.get(MAIL_TXT) != null ? (String) mailOptions.get(MAIL_TXT) : null;
			String[] recipients = mailOptions.get(RECIPIENTS) != null ? (String[]) mailOptions.get(RECIPIENTS) : null;

			// create a message
			Message msg = facade.createNewMimeMessage();

			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
			// Setting the Subject and Content Type

			String subject = mailSubj;

			String nameSuffix = mailOptions.get(NAME_SUFFIX) != null ? (String) mailOptions.get(NAME_SUFFIX) : null;
			Boolean reportNameInSubject = mailOptions.get(REPORT_NAME_IN_SUBJECT) != null
					&& !mailOptions.get(REPORT_NAME_IN_SUBJECT).toString().equals("")
							? (Boolean) mailOptions.get(REPORT_NAME_IN_SUBJECT)
							: null;
			// Boolean descriptionSuffix =mailOptions.get(DESCRIPTION_SUFFIX) != null && !mailOptions.get(DESCRIPTION_SUFFIX).toString().equals("")? (Boolean)
			// mailOptions.get(DESCRIPTION_SUFFIX) : null;
			String zipFileName = mailOptions.get(ZIP_FILE_NAME) != null ? (String) mailOptions.get(ZIP_FILE_NAME)
					: "Zipped Documents";
			String contentType = mailOptions.get(CONTENT_TYPE) != null ? (String) mailOptions.get(CONTENT_TYPE) : null;
			String fileExtension = mailOptions.get(FILE_EXTENSION) != null ? (String) mailOptions.get(FILE_EXTENSION)
					: null;

			if (reportNameInSubject) {
				subject += " " + nameSuffix;
			}

			msg.setSubject(subject);
			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(mailTxt);

			// attach the file to the message

			boolean isZipDocument = mailOptions.get(IS_ZIP_DOCUMENT) != null
					? (Boolean) mailOptions.get(IS_ZIP_DOCUMENT)
					: false;
			zipFileName = mailOptions.get(ZIP_FILE_NAME) != null ? (String) mailOptions.get(ZIP_FILE_NAME)
					: "Zipped Documents";

			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();

			mp.addBodyPart(mbp1);

			if (isZipDocument) {
				logger.debug("Make zip");
				// create the second message part
				MimeBodyPart mbp2 = new MimeBodyPart();
				mbp2 = zipAttachment(zipFileName, mailOptions, tempFolder);
				mp.addBodyPart(mbp2);
			} else {
				logger.debug("Attach single files");
				SchedulerDataSource sds = null;
				MimeBodyPart bodyPart = null;
				try {
					String[] entries = tempFolder.list();

					for (int i = 0; i < entries.length; i++) {
						logger.debug("Attach file " + entries[i]);
						File f = new File(tempFolder + File.separator + entries[i]);

						byte[] content = getBytesFromFile(f);

						bodyPart = new MimeBodyPart();

						sds = new SchedulerDataSource(content, contentType, entries[i]);
						// sds = new SchedulerDataSource(content, contentType, entries[i] + fileExtension);

						bodyPart.setDataHandler(new DataHandler(sds));
						bodyPart.setFileName(sds.getName());

						mp.addBodyPart(bodyPart);
					}

				} catch (Exception e) {
					logger.error("Error while attaching files", e);
				}

			}

			// add the Multipart to the message
			msg.setContent(mp);
			logger.debug("Preparing to send mail");

			// send message
			facade.sendMessage(msg);

			logger.info("Mail sent for documents with labels [" + allDocumentLabels + "]");

//			logger.debug("delete tempFolder path "+tempFolder.getPath());
//			boolean deleted = tempFolder.delete();
//			logger.debug("Temp folder deleted "+deleted);

		} catch (Exception e) {
			logger.error("Error while sending schedule result mail", e);
			return false;
		} finally {
			logger.debug("OUT");
		}
		return true;
	}

	public static MimeBodyPart zipAttachment(String zipFileName, Map mailOptions, File tempFolder) {
		logger.debug("IN");
		MimeBodyPart messageBodyPart = null;
		try {

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
				if (f.isDirectory())
					continue;// Ignore directory
				logger.debug("insert file: " + f.getName());
				FileInputStream in = new FileInputStream(f); // Stream to read file
				ZipEntry entry = new ZipEntry(f.getName()); // Make a ZipEntry
				out.putNextEntry(entry); // Store entry
				while ((bytesRead = in.read(buffer)) != -1)
					out.write(buffer, 0, bytesRead);
				in.close();
			}
			out.close();

			messageBodyPart = new MimeBodyPart();
			DataSource source = new ByteArrayDataSource(bout.toByteArray(), "application/zip");
			messageBodyPart.setDataHandler(new DataHandler(source));

			messageBodyPart.setFileName(zipFileName + nameSuffix + ".zip");

		} catch (Exception e) {
			logger.error("Error while creating the zip", e);
			return null;
		}

		logger.debug("OUT");

		return messageBodyPart;
	}

	private byte[] zipDocument(String fileZipName, byte[] content) {
		logger.debug("IN");

		ByteArrayOutputStream bos = null;
		ZipOutputStream zos = null;
		ByteArrayInputStream in = null;
		try {

			bos = new ByteArrayOutputStream();
			zos = new ZipOutputStream(bos);
			ZipEntry ze = new ZipEntry(fileZipName);
			zos.putNextEntry(ze);
			in = new ByteArrayInputStream(content);

			for (int c = in.read(); c != -1; c = in.read()) {
				zos.write(c);
			}

			return bos.toByteArray();

		} catch (IOException ex) {
			logger.error("Error zipping the document", ex);
			return null;
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					logger.error("Error closing output stream", e);
				}
			}
			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					logger.error("Error closing output stream", e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("Error closing output stream", e);
				}
			}
		}

	}

	public static boolean canDispatch(DispatchContext dispatchContext, BIObject document,
			IDataStore emailDispatchDataStore) {
		String[] recipients = findRecipients(dispatchContext, document, emailDispatchDataStore);
		return (recipients != null && recipients.length > 0);
	}

	public static String[] findRecipients(DispatchContext info, BIObject biobj, IDataStore dataStore) {
		logger.debug("IN");
		String[] toReturn = null;
		List<String> recipients = new ArrayList();
		try {
			recipients.addAll(findRecipientsFromFixedList(info));
		} catch (Exception e) {
			logger.error(e);
		}
		try {
			recipients.addAll(findRecipientsFromDataSet(info, biobj, dataStore));
		} catch (Exception e) {
			logger.error(e);
		}
		try {
			recipients.addAll(findRecipientsFromExpression(info, biobj));
		} catch (Exception e) {
			logger.error(e);
		}
		// validates addresses
		List<String> validRecipients = new ArrayList();
		Iterator it = recipients.iterator();
		while (it.hasNext()) {
			String recipient = (String) it.next();
			if (GenericValidator.isBlankOrNull(recipient) || !GenericValidator.isEmail(recipient)) {
				logger.error("[" + recipient + "] is not a valid email address.");
				continue;
			}
			if (validRecipients.contains(recipient))
				continue;
			validRecipients.add(recipient);
		}
		toReturn = validRecipients.toArray(new String[0]);
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	private static List<String> findRecipientsFromFixedList(DispatchContext info) throws Exception {
		logger.debug("IN");
		List<String> recipients = new ArrayList();
		if (info.isUseFixedRecipients()) {
			logger.debug("Trigger is configured to send mail to fixed recipients: " + info.getMailTos());
			if (info.getMailTos() == null || info.getMailTos().trim().equals("")) {
				throw new Exception("Missing fixed recipients list!!!");
			}
			// in this case recipients are fixed and separated by ","
			String[] fixedRecipients = info.getMailTos().split(",");
			logger.debug("Fixed recipients found: " + fixedRecipients);
			recipients.addAll(Arrays.asList(fixedRecipients));
		}
		logger.debug("OUT");
		return recipients;
	}

	private static List<String> findRecipientsFromExpression(DispatchContext info, BIObject biobj) throws Exception {
		logger.debug("IN");
		List<String> recipients = new ArrayList();
		if (info.isUseExpression()) {
			logger.debug("Trigger is configured to send mail using an expression: " + info.getExpression());
			String expression = info.getExpression();
			if (expression == null || expression.trim().equals("")) {
				throw new Exception("Missing recipients expression!!!");
			}
			// building a map for parameters value substitution
			Map parametersMap = new HashMap();
			List parameters = biobj.getDrivers();
			Iterator it = parameters.iterator();
			while (it.hasNext()) {
				BIObjectParameter parameter = (BIObjectParameter) it.next();
				List values = parameter.getParameterValues();
				if (values != null && !values.isEmpty()) {
					parametersMap.put(parameter.getLabel(), values.get(0));
				} else {
					parametersMap.put(parameter.getLabel(), "");
				}
			}
			// we must substitute parameter values on the expression
			String recipientStr = StringUtilities.substituteParametersInString(expression, parametersMap, null, false);
			logger.debug("The expression, after substitution, now is [" + recipientStr + "].");
			String[] recipientsArray = recipientStr.split(",");
			logger.debug("Recipients found with expression: " + recipientsArray);
			recipients.addAll(Arrays.asList(recipientsArray));
		}
		logger.debug("OUT");
		return recipients;
	}

	private static List<String> findRecipientsFromDataSet(DispatchContext info, BIObject biobj, IDataStore dataStore)
			throws Exception {
		logger.debug("IN");
		List<String> recipients = new ArrayList();
		if (info.isUseDataSet()) {
			logger.debug("Trigger is configured to send mail to recipients retrieved by a dataset");
			if (dataStore == null || dataStore.isEmpty()) {
				throw new Exception("The dataset in input is empty!! Cannot retrieve recipients from it.");
			}
			// in this case recipients must be retrieved by the dataset (which the datastore in input belongs to)
			// we must find the parameter value in order to filter the dataset
			String dsParameterLabel = info.getDataSetParameterLabel();
			logger.debug("The dataset will be filtered using the value of the parameter " + dsParameterLabel);
			// looking for the parameter
			List parameters = biobj.getDrivers();
			BIObjectParameter parameter = null;
			String codeValue = null;
			Iterator parameterIt = parameters.iterator();
			while (parameterIt.hasNext()) {
				BIObjectParameter aParameter = (BIObjectParameter) parameterIt.next();
				if (aParameter.getLabel().equalsIgnoreCase(dsParameterLabel)) {
					parameter = aParameter;
					break;
				}
			}
			if (parameter == null) {
				throw new Exception("The document parameter with label [" + dsParameterLabel
						+ "] was not found. Cannot filter the dataset.");
			}

			// considering the first value of the parameter
			List values = parameter.getParameterValues();
			if (values == null || values.isEmpty()) {
				throw new Exception("The document parameter with label [" + dsParameterLabel
						+ "] has no values. Cannot filter the dataset.");
			}

			codeValue = (String) values.get(0);
			logger.debug("Using value [" + codeValue + "] for dataset filtering...");

			Iterator<IRecord> it = dataStore.iterator();
			while (it.hasNext()) {
				String recipient = null;
				IRecord currRecord = it.next();
				// the parameter value is used to filter on the first dataset field
				IField valueField = currRecord.getFieldAt(0);
				Object valueObj = valueField.getValue();
				String value = null;
				if (valueObj != null)
					value = valueObj.toString();
				if (codeValue.equals(value)) {
					logger.debug("Found value [" + codeValue + "] on the first field of a record of the dataset.");
					// recipient address is on the second dataset field
					IField recipientField = currRecord.getFieldAt(1);
					Object recipientFieldObj = recipientField.getValue();
					if (recipientFieldObj != null) {
						recipient = recipientFieldObj.toString();
						// in this case recipients can be separated by ","
						String[] multiRecipients = recipient.split(",");

						recipients.addAll(Arrays.asList(multiRecipients));

						logger.debug("DataSet multi recipients found: " + Arrays.deepToString(multiRecipients));
					} else {
						logger.warn("The second field of the record is null.");
					}
				}
				if (recipient != null) {
					recipients.add(recipient);
				}
			}
			logger.debug("Recipients found from dataset: " + recipients.toArray());
		}
		logger.debug("OUT");
		return recipients;
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

	// Returns the contents of the file in a byte array.
	public static byte[] getBytesFromFile(File file) throws IOException {
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

		InputStream is = new FileInputStream(file);
		try {
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
		} finally {
			is.close();
		}

		return bytes;
	}
}
