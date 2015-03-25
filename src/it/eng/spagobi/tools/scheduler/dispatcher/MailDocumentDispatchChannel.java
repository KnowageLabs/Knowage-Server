/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.dispatcher;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class MailDocumentDispatchChannel implements IDocumentDispatchChannel {

	private DispatchContext dispatchContext;

	// logger component
	private static Logger logger = Logger.getLogger(MailDocumentDispatchChannel.class);
	final String DEFAULT_SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	final String CUSTOM_SSL_FACTORY = "it.eng.spagobi.commons.services.DummySSLSocketFactory";

	public MailDocumentDispatchChannel(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
		try {
			IEngUserProfile userProfile = this.dispatchContext.getUserProfile();
			// gets the dataset data about the email address
			IDataStore emailDispatchDataStore = null;
			if (dispatchContext.isUseDataSet()) {
				IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(dispatchContext.getDataSetLabel());
				dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(userProfile));
				dataSet.loadData();
				emailDispatchDataStore = dataSet.getDataStore();
			}
			dispatchContext.setEmailDispatchDataStore(emailDispatchDataStore);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to instatiate MailDocumentDispatchChannel class", t);
		}
	}

	public void setDispatchContext(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
	}

	public void close() {

	}

	public boolean canDispatch(BIObject document) {
		return canDispatch(dispatchContext, document, dispatchContext.getEmailDispatchDataStore());
	}

	public boolean dispatch(BIObject document, byte[] executionOutput) {
		Map parametersMap;
		String contentType;
		String fileExtension;
		IDataStore emailDispatchDataStore;
		String nameSuffix;
		String descriptionSuffix;
		String containedFileName;
		String zipFileName;
		boolean reportNameInSubject;

		logger.debug("IN");
		try {
			parametersMap = dispatchContext.getParametersMap();
			contentType = dispatchContext.getContentType();
			fileExtension = dispatchContext.getFileExtension();
			emailDispatchDataStore = dispatchContext.getEmailDispatchDataStore();
			nameSuffix = dispatchContext.getNameSuffix();
			descriptionSuffix = dispatchContext.getDescriptionSuffix();
			containedFileName = dispatchContext.getContainedFileName() != null && !dispatchContext.getContainedFileName().equals("") ? dispatchContext
					.getContainedFileName() : document.getName();
			zipFileName = dispatchContext.getZipMailName() != null && !dispatchContext.getZipMailName().equals("") ? dispatchContext.getZipMailName()
					: document.getName();
			reportNameInSubject = dispatchContext.isReportNameInSubject();

			String smtphost = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.smtphost");
			String smtpport = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.smtpport");
			String smtpssl = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.useSSL");
			logger.debug(smtphost + " " + smtpport + " use SSL: " + smtpssl);

			// Custom Trusted Store Certificate Options
			String trustedStorePath = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.file");
			String trustedStorePassword = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.password");

			int smptPort = 25;

			if ((smtphost == null) || smtphost.trim().equals(""))
				throw new Exception("Smtp host not configured");
			if ((smtpport == null) || smtpport.trim().equals("")) {
				throw new Exception("Smtp host not configured");
			} else {
				smptPort = Integer.parseInt(smtpport);
			}

			String from = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.from");
			if ((from == null) || from.trim().equals(""))
				from = "spagobi.scheduler@eng.it";
			String user = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.user");
			if ((user == null) || user.trim().equals("")) {
				logger.debug("Smtp user not configured");
				user = null;
			}
			// throw new Exception("Smtp user not configured");
			String pass = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.password");
			if ((pass == null) || pass.trim().equals("")) {
				logger.debug("Smtp password not configured");
			}
			// throw new Exception("Smtp password not configured");

			String mailSubj = dispatchContext.getMailSubj();
			mailSubj = StringUtilities.substituteParametersInString(mailSubj, parametersMap, null, false);

			String mailTxt = dispatchContext.getMailTxt();

			String[] recipients = findRecipients(dispatchContext, document, emailDispatchDataStore);
			if (recipients == null || recipients.length == 0) {
				logger.error("No recipients found for email sending!!!");
				return false;
			}

			// Set the host smtp address
			Properties props = new Properties();
			props.put("mail.smtp.host", smtphost);
			props.put("mail.smtp.port", Integer.toString(smptPort));

			// open session
			Session session = null;

			// create autheticator object
			Authenticator auth = null;
			if (user != null) {
				auth = new SMTPAuthenticator(user, pass);
				props.put("mail.smtp.auth", "true");
				// SSL Connection
				if (smtpssl.equals("true")) {
					Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
					// props.put("mail.smtp.debug", "true");
					props.put("mail.smtps.auth", "true");
					props.put("mail.smtps.socketFactory.port", Integer.toString(smptPort));
					if ((!StringUtilities.isEmpty(trustedStorePath))) {
						/*
						 * Dynamic configuration of trustedstore for CA Using Custom SSLSocketFactory to inject certificates directly from specified files
						 */
						// System.setProperty("java.security.debug","certpath");
						// System.setProperty("javax.net.debug","ssl ");
						props.put("mail.smtps.socketFactory.class", CUSTOM_SSL_FACTORY);

					} else {
						// System.setProperty("java.security.debug","certpath");
						// System.setProperty("javax.net.debug","ssl ");
						props.put("mail.smtps.socketFactory.class", DEFAULT_SSL_FACTORY);
					}
					props.put("mail.smtp.socketFactory.fallback", "false");
				}

				// session = Session.getDefaultInstance(props, auth);
				session = Session.getInstance(props, auth);
				// session.setDebug(true);
				// session.setDebugOut(null);
				logger.info("Session.getInstance(props, auth)");

			} else {
				// session = Session.getDefaultInstance(props);
				session = Session.getInstance(props);
				logger.info("Session.getInstance(props)");
			}

			// create a message
			Message msg = new MimeMessage(session);
			// set the from and to address
			InternetAddress addressFrom = new InternetAddress(from);
			msg.setFrom(addressFrom);
			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
			// Setting the Subject and Content Type

			String subject = mailSubj;

			if (reportNameInSubject) {
				subject += " " + document.getName() + nameSuffix;
			}

			msg.setSubject(subject);
			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(mailTxt + "\n" + descriptionSuffix);
			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();
			// attach the file to the message

			SchedulerDataSource sds = null;
			// if zip requested
			if (dispatchContext.isZipMailDocument()) {
				mbp2 = zipAttachment(executionOutput, containedFileName, zipFileName, nameSuffix, fileExtension);
			}
			// else
			else {
				sds = new SchedulerDataSource(executionOutput, contentType, containedFileName + nameSuffix + fileExtension);
				mbp2.setDataHandler(new DataHandler(sds));
				mbp2.setFileName(sds.getName());
			}

			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mp.addBodyPart(mbp2);
			// add the Multipart to the message
			msg.setContent(mp);
			// send message
			if ((smtpssl.equals("true")) && (!StringUtilities.isEmpty(user)) && (!StringUtilities.isEmpty(pass))) {
				// USE SSL Transport comunication with SMTPS
				Transport transport = session.getTransport("smtps");
				transport.connect(smtphost, smptPort, user, pass);
				transport.sendMessage(msg, msg.getAllRecipients());
				transport.close();
			} else {
				// Use normal SMTP
				Transport.send(msg);
			}
			logger.info("Mail sent for document with label " + document.getLabel());

		} catch (Exception e) {
			logger.error("Error while sending schedule result mail", e);
			return false;
		} finally {
			logger.debug("OUT");
		}
		return true;
	}

	private MimeBodyPart zipAttachment(byte[] attach, String containedFileName, String zipFileName, String nameSuffix, String fileExtension) {
		MimeBodyPart messageBodyPart = null;
		try {

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

		} catch (Exception e) {
			// TODO: handle exception
		}
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

	public static boolean canDispatch(DispatchContext dispatchContext, BIObject document, IDataStore emailDispatchDataStore) {
		String[] recipients = findRecipients(dispatchContext, document, emailDispatchDataStore);
		return (recipients != null && recipients.length > 0);
	}

	private static String[] findRecipients(DispatchContext info, BIObject biobj, IDataStore dataStore) {
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
			List parameters = biobj.getBiObjectParameters();
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

	private static List<String> findRecipientsFromDataSet(DispatchContext info, BIObject biobj, IDataStore dataStore) throws Exception {
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
			List parameters = biobj.getBiObjectParameters();
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
				throw new Exception("The document parameter with label [" + dsParameterLabel + "] was not found. Cannot filter the dataset.");
			}

			// considering the first value of the parameter
			List values = parameter.getParameterValues();
			if (values == null || values.isEmpty()) {
				throw new Exception("The document parameter with label [" + dsParameterLabel + "] has no values. Cannot filter the dataset.");
			}

			codeValue = (String) values.get(0);
			logger.debug("Using value [" + codeValue + "] for dataset filtering...");

			Iterator it = dataStore.iterator();
			while (it.hasNext()) {
				String recipient = null;
				IRecord record = (IRecord) it.next();
				// the parameter value is used to filter on the first dataset field
				IField valueField = record.getFieldAt(0);
				Object valueObj = valueField.getValue();
				String value = null;
				if (valueObj != null)
					value = valueObj.toString();
				if (codeValue.equals(value)) {
					logger.debug("Found value [" + codeValue + "] on the first field of a record of the dataset.");
					// recipient address is on the second dataset field
					IField recipientField = record.getFieldAt(1);
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

	private class SMTPAuthenticator extends javax.mail.Authenticator {
		private String username = "";
		private String password = "";

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password);
		}

		public SMTPAuthenticator(String user, String pass) {
			this.username = user;
			this.password = pass;
		}
	}

	private class SchedulerDataSource implements DataSource {

		byte[] content = null;
		String name = null;
		String contentType = null;

		public String getContentType() {
			return contentType;
		}

		public InputStream getInputStream() throws IOException {
			ByteArrayInputStream bais = new ByteArrayInputStream(content);
			return bais;
		}

		public String getName() {
			return name;
		}

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
