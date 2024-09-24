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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import it.eng.knowage.mailsender.IMailSender;
import it.eng.knowage.mailsender.dto.MessageMailDto;
import it.eng.knowage.mailsender.dto.ProfileNameMailEnum;
import it.eng.knowage.mailsender.dto.TypeMailEnum;
import it.eng.knowage.mailsender.factory.FactoryMailSender;
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

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class MailDocumentDispatchChannel implements IDocumentDispatchChannel {

	private DispatchContext dispatchContext;

	// logger component
	private static Logger logger = Logger.getLogger(MailDocumentDispatchChannel.class);

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

	@Override
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
			containedFileName = dispatchContext.getContainedFileName() != null
					&& !dispatchContext.getContainedFileName().equals("") ? dispatchContext.getContainedFileName()
							: document.getName();
			zipFileName = dispatchContext.getZipMailName() != null && !dispatchContext.getZipMailName().equals("")
					? dispatchContext.getZipMailName()
					: document.getName();
			reportNameInSubject = dispatchContext.isReportNameInSubject();

			MessageMailDto messageMailDto = new MessageMailDto();
			messageMailDto.setProfileName((ProfileNameMailEnum.SCHEDULER));

			String mailSubj = dispatchContext.getMailSubj();
			mailSubj = StringUtilities.substituteParametersInString(mailSubj, parametersMap, null, false);

			String mailTxt = dispatchContext.getMailTxt();

			String[] recipients = findRecipients(dispatchContext, document, emailDispatchDataStore);
			if (recipients == null || recipients.length == 0) {
				logger.error("No recipients found for email sending!!!");
				return false;
			}

			messageMailDto.setRecipients(recipients);
			// Setting the Subject and Content Type

			String subject = mailSubj;

			if (reportNameInSubject) {
				subject += " " + document.getName() + nameSuffix;
			}

			messageMailDto.setTypeMailEnum(TypeMailEnum.MULTIPART);
			messageMailDto.setSubject(subject);
			messageMailDto.setZipMailDocument(dispatchContext.isZipMailDocument());
			messageMailDto.setAttach(executionOutput);
			messageMailDto.setContainedFileName(containedFileName);
			messageMailDto.setZipFileName(zipFileName);
			messageMailDto.setNameSuffix(nameSuffix);
			messageMailDto.setText(mailTxt + "\n" + descriptionSuffix);
			messageMailDto.setFileExtension(fileExtension);
			messageMailDto.setContentType(contentType);

			FactoryMailSender.getMailSender(SingletonConfig.getInstance().getConfigValue(IMailSender.MAIL_SENDER)).sendMail(messageMailDto);
			logger.info("Mail sent for document with label " + document.getLabel());

		} catch (Exception e) {
			logger.error("Error while sending schedule result mail", e);
			return false;
		} finally {
			logger.debug("OUT");
		}
		return true;
	}


	public static boolean canDispatch(DispatchContext dispatchContext, BIObject document,
			IDataStore emailDispatchDataStore) {
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
			if (validRecipients.contains(recipient)) {
				continue;
			}
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
				if (valueObj != null) {
					value = valueObj.toString();
				}
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

}
