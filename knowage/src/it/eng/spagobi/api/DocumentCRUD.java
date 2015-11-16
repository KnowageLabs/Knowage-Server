package it.eng.spagobi.api;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was  not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.AnalyticalModelDocumentManagementAPI;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.DocumentsJSONDecorator;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.services.security.service.SecurityServiceSupplierFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
@Path("/documents")
public class DocumentCRUD extends AbstractSpagoBIResource {

	public static final String OBJECT_ID = "docId";
	public static final String OBJECT_FUNCTS = "functs";
	public static final String COMMUNITY = "communityId";
	public static final String IS_SHARE = "isShare";
	public static final String USER = "user";
	public static final String DOCUMENT_TYPE = "docType";

	static private Logger logger = Logger.getLogger(DocumentCRUD.class);

	/**
	 * Service to clone a document
	 *
	 * @param req
	 * @return
	 */
	@POST
	@Path("/clone")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String cloneDocument(@Context HttpServletRequest req) {

		logger.debug("IN");
		String ids = req.getParameter(OBJECT_ID);
		Integer id = -1;
		try {
			id = new Integer(ids);
		} catch (Exception e) {
			logger.error("Error cloning the document.. Impossible to parse the id of the document " + ids, e);
			throw new SpagoBIRuntimeException("Error cloning the document.. Impossible to parse the id of the document " + ids, e);
		}
		IEngUserProfile profile = this.getUserProfile();

		AnalyticalModelDocumentManagementAPI documentManagementAPI = new AnalyticalModelDocumentManagementAPI(profile);
		logger.debug("Execute clone");
		documentManagementAPI.cloneDocument(id);
		logger.debug("OUT");
		return "{}";
	}

	/**
	 * Service to send e-mail Feedback about a document
	 *
	 * @param req
	 * @return
	 */
	@POST
	@Path("/sendFeedback")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String sendFeedback(@Context HttpServletRequest req) {

		logger.debug("IN");
		IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();

		// 1- Label of current document
		String label = req.getParameter("label");
		// Author of the document
		String documentCreationUser = null;
		// 2 - email address of creation user
		String emailAddressdocumentCreationUser = null;
		IBIObjectDAO biObjectDao;
		try {
			biObjectDao = DAOFactory.getBIObjectDAO();
			if ((label != null) && (!label.isEmpty())) {
				BIObject document = biObjectDao.loadBIObjectByLabel(label);
				documentCreationUser = document.getCreationUser();

				ISecurityServiceSupplier supplier = SecurityServiceSupplierFactory.createISecurityServiceSupplier();
				SpagoBIUserProfile userProfile = supplier.createUserProfile(documentCreationUser);
				HashMap userAttributes = userProfile.getAttributes();
				if (userAttributes.get("email") != null) {
					emailAddressdocumentCreationUser = (String) userAttributes.get("email");
				}

			}
			// 3 - content of the email to send
			String message = req.getParameter("msg");

			// 4 - User sending the feedback (from session)
			IEngUserProfile profile = this.getUserProfile();
			String userSendingFeedback = null;
			if (profile.getUserUniqueIdentifier() instanceof String) {
				userSendingFeedback = (String) profile.getUserUniqueIdentifier();
			}

			// Check if all the informations to send a mail are valorized
			if ((emailAddressdocumentCreationUser != null) && (!emailAddressdocumentCreationUser.isEmpty())) {
				if ((label != null) && (!label.isEmpty())) {
					if ((userSendingFeedback != null) && (!userSendingFeedback.isEmpty())) {
						String subject = msgBuilder.getMessage("document.feedback.msg.1", "messages") + " " + userSendingFeedback + " "
								+ msgBuilder.getMessage("document.feedback.msg.2", "messages") + "  " + label;
						sendMail(emailAddressdocumentCreationUser, subject, message);
					}
				}
			}
		} catch (EMFUserError ex) {
			logger.error("Error sending feedback for document " + label, ex);
			try {
				return (ExceptionUtilities.serializeException("Feedback not sent: " + ex.toString(), null));
			} catch (Exception e) {
				logger.debug("Error sending feedback for document " + label, e);
				throw new SpagoBIRuntimeException("Error sending feedback for document " + label, e);
			}
		} catch (Exception ex) {
			logger.error("Error sending feedback for document " + label, ex);
			try {
				return (ExceptionUtilities.serializeException("Feedback not sent: " + ex.toString(), null));
			} catch (Exception e) {
				logger.debug("Error sending feedback for document " + label, e);
				throw new SpagoBIRuntimeException("Error sending feedback for document " + label, e);
			}
		}

		logger.debug("OUT");
		return "{}";
	}

	@GET
	@Path("/myAnalysisDocsList")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getMyAnalysisDocuments(@Context HttpServletRequest req) {
		logger.debug("IN");
		String user = req.getParameter(USER);
		String docType = req.getParameter(DOCUMENT_TYPE);

		logger.debug("Searching documents inside personal folder of user [" + user + "]");

		IEngUserProfile profile = this.getUserProfile();
		List userFunctionalties;
		LowFunctionality personalFolder = null;
		try {

			// Search personal folder of current user
			ILowFunctionalityDAO functionalitiesDAO = DAOFactory.getLowFunctionalityDAO();

			userFunctionalties = functionalitiesDAO.loadAllUserFunct();
			for (Iterator it = userFunctionalties.iterator(); it.hasNext();) {
				LowFunctionality funct = (LowFunctionality) it.next();
				if (UserUtilities.isPersonalFolder(funct, (UserProfile) profile)) {
					personalFolder = funct;
					break;
				}
			}

			List myObjects = new ArrayList();
			if (personalFolder != null) {
				Engine geoEngine = null;
				Engine wsEngine = null;
				Engine cockpitEngine = null;

				try {
					geoEngine = ExecuteAdHocUtility.getGeoreportEngine();
				} catch (SpagoBIRuntimeException r) {
					// the geo engine is not found
					logger.info("Engine not found. ", r);
				}
				try {
					wsEngine = ExecuteAdHocUtility.getWorksheetEngine();
				} catch (SpagoBIRuntimeException r) {
					// the ws engine is not found
					logger.info("Engine not found. ", r);
				}

				try {
					cockpitEngine = ExecuteAdHocUtility.getCockpitEngine();
				} catch (SpagoBIRuntimeException r) {
					// the cockpit engine is not found
					logger.info("Engine not found. ", r);
				}

				// return all documents inside the personal folder
				if ((docType == null) || (docType.equalsIgnoreCase("ALL"))) {
					List filteredMyObjects = new ArrayList();
					myObjects = DAOFactory.getBIObjectDAO().loadBIObjects(Integer.valueOf(personalFolder.getId()), profile, true);
					// Get only documents of type Worksheet and Map
					for (Iterator it = myObjects.iterator(); it.hasNext();) {
						BIObject biObject = (BIObject) it.next();
						String biObjectType = biObject.getBiObjectTypeCode();
						if ((wsEngine != null && biObject.getEngine().getId().equals(wsEngine.getId()))
								|| (geoEngine != null && biObject.getEngine().getId().equals(geoEngine.getId()))
								|| (cockpitEngine != null && biObject.getEngine().getId().equals(cockpitEngine.getId()))) {
							filteredMyObjects.add(biObject);
						}
					}
					myObjects = filteredMyObjects;

				} else if (docType.equalsIgnoreCase("Report") && wsEngine != null) {
					// return only Worksheets inside the personal folder
					myObjects = DAOFactory.getBIObjectDAO().loadBIObjects("WORKSHEET", "REL", personalFolder.getPath());

				} else if (docType.equalsIgnoreCase("Map") && geoEngine != null) {
					// return only Geo Map (GIS) documents inside the personal
					// folder
					myObjects = DAOFactory.getBIObjectDAO().loadBIObjects("MAP", "REL", personalFolder.getPath());

				} else if (docType.equalsIgnoreCase("Cockpit") && cockpitEngine != null) {
					// return only Cockpits inside the personal folder
					List filteredMyObjects = new ArrayList();
					myObjects = DAOFactory.getBIObjectDAO().loadBIObjects("DOCUMENT_COMPOSITE", "REL", personalFolder.getPath());
					for (Iterator it = myObjects.iterator(); it.hasNext();) {
						BIObject biObject = (BIObject) it.next();
						if (biObject.getEngine().getId().equals(cockpitEngine.getId())) {
							filteredMyObjects.add(biObject);
						}
					}
					myObjects = filteredMyObjects;
				}

				// Serialize documents list
				MessageBuilder m = new MessageBuilder();
				Locale locale = m.getLocale(req);
				JSONArray documentsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(myObjects, locale);
				DocumentsJSONDecorator.decorateDocuments(documentsJSON, profile, personalFolder);
				JSONObject documentsResponseJSON = createJSONResponseDocuments(documentsJSON);

				return documentsResponseJSON.toString();
			}

		} catch (EMFUserError e) {
			logger.error("Error in myAnalysisDocsList Service: " + e);
		} catch (SerializationException e) {
			logger.error("Serializing Error in myAnalysisDocsList Service: " + e);
		} catch (JSONException e) {
			logger.error("JSONException Error in myAnalysisDocsList Service: " + e);
		}

		logger.debug("OUT");
		return "{}";
	}

	/**
	 * Service to share/unshare a document
	 *
	 * @param req
	 * @return
	 */
	@POST
	@Path("/share")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String shareDocument(@Context HttpServletRequest req) {

		logger.debug("IN");
		String ids = req.getParameter(OBJECT_ID);
		String isShare = req.getParameter(IS_SHARE);
		Integer id = -1;
		try {
			id = new Integer(ids);
		} catch (Exception e) {
			logger.error("Error sharing the document.. Impossible to parse the id of the document " + ids, e);
			throw new SpagoBIRuntimeException("Error sharing the document.. Impossible to parse the id of the document " + ids, e);
		}
		IEngUserProfile profile = this.getUserProfile();

		AnalyticalModelDocumentManagementAPI documentManagementAPI = new AnalyticalModelDocumentManagementAPI(profile);
		String oper = ("true".equalsIgnoreCase(isShare)) ? "Sharing" : "Unsharing";
		logger.debug("Execute " + oper);
		if (id != null) {
			BIObject document = documentManagementAPI.getDocument(id);
			List lstFuncts = new ArrayList();

			if ("true".equalsIgnoreCase(isShare)) {
				// share
				JSONArray functs = (req.getParameter(OBJECT_FUNCTS) == null) ? new JSONArray() : ObjectUtils.toJSONArray(req.getParameter(OBJECT_FUNCTS));
				String communityFCode = req.getParameter(COMMUNITY);
				if (communityFCode != null && !"".equals(communityFCode)) {
					try {
						// add community folder to functionalities community
						// folder
						LowFunctionality commF = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByCode(communityFCode, false);
						Integer commFId = commF.getId();
						functs.put(commFId);
					} catch (Exception e) {
						logger.error("Error sharing the document.. Impossible to parse the community ", e);
						throw new SpagoBIRuntimeException("Error sharing the document.. Impossible to parse the community", e);
					}
				}
				lstFuncts = JSONUtils.asList(functs);
			}
			// add personal folder for default
			LowFunctionality userFunc = null;
			try {
				ILowFunctionalityDAO functionalitiesDAO = DAOFactory.getLowFunctionalityDAO();
				userFunc = functionalitiesDAO.loadLowFunctionalityByPath("/" + profile.getUserUniqueIdentifier(), false);
			} catch (Exception e) {
				logger.error("Error " + oper + "  the document.. Impossible to get the id of the personal folder for document " + ids, e);
				throw new SpagoBIRuntimeException("Error " + oper + "  the document.. Impossible to get the id of the personal folder for document " + ids, e);
			}
			if (userFunc != null)
				lstFuncts.add(userFunc.getId());
			else
				logger.error("Error " + oper + " the document.. Impossible to get the id of the personal folder for document " + ids);

			document.setFunctionalities(lstFuncts);

			// save
			documentManagementAPI.saveDocument(document, null);
		}
		logger.debug("OUT");
		return "{}";
	}

	/**
	 * Creates a json array with children document informations
	 *
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	public JSONObject createJSONResponse(JSONObject folders, JSONObject documents, JSONObject canAdd) throws JSONException {
		JSONObject results = new JSONObject();
		JSONArray folderContent = new JSONArray();

		// folderContent.put(folders);
		folderContent.put(documents);
		if (canAdd != null) {
			folderContent.put(canAdd);
		}
		results.put("folderContent", folderContent);

		return results;
	}

	/**
	 * Creates a json array with children folders informations
	 *
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	public JSONObject createJSONResponseFolders(JSONArray rows) throws JSONException {
		JSONObject results;

		results = new JSONObject();
		results.put("title", "Folders");
		results.put("icon", "folder.png");
		results.put("samples", rows);
		return results;
	}

	/**
	 * Creates a json array with children document informations
	 *
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	public JSONObject createJSONResponseDocuments(JSONArray rows) throws JSONException {
		JSONObject results;

		results = new JSONObject();
		// results.put("title", "Documents");
		// results.put("icon", "document.png");
		results.put("root", rows);
		return results;
	}

	// sending email to emailAddress with passed subject and emailContent
	private void sendMail(String emailAddress, String subject, String emailContent) throws Exception {

		final String DEFAULT_SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		final String CUSTOM_SSL_FACTORY = "it.eng.spagobi.commons.services.DummySSLSocketFactory";

		String smtphost = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.smtphost");
		String smtpport = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.smtpport");
		String smtpssl = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.useSSL");
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

		String from = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.from");
		if ((from == null) || from.trim().equals(""))
			from = "spagobi@eng.it";
		String user = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.user");
		if ((user == null) || user.trim().equals("")) {
			logger.debug("Smtp user not configured");
			user = null;
		}
		String pass = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.password");
		if ((pass == null) || pass.trim().equals("")) {
			logger.debug("Smtp password not configured");
		}

		// Set the host smtp address
		Properties props = new Properties();
		props.put("mail.smtp.host", smtphost);
		props.put("mail.smtp.port", Integer.toString(smptPort));
		// Set timeout limit for mail server to respond
		props.put("mail.smtp.timeout", "5000");
		props.put("mail.smtp.connectiontimeout", "5000");

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
				props.put("mail.smtps.auth", "true");
				props.put("mail.smtps.socketFactory.port", Integer.toString(smptPort));
				if ((!StringUtilities.isEmpty(trustedStorePath))) {
					/*
					 * Dynamic configuration of trustedstore for CA Using Custom SSLSocketFactory to inject certificates directly from specified files
					 */

					props.put("mail.smtps.socketFactory.class", CUSTOM_SSL_FACTORY);

				} else {

					props.put("mail.smtps.socketFactory.class", DEFAULT_SSL_FACTORY);
				}
				props.put("mail.smtp.socketFactory.fallback", "false");
			}

			session = Session.getInstance(props, auth);
			logger.info("Session.getInstance(props, auth)");

		} else {
			session = Session.getInstance(props);
			logger.info("Session.getInstance(props)");
		}

		// create a message
		Message msg = new MimeMessage(session);
		// set the from and to address
		InternetAddress addressFrom = new InternetAddress(from);
		msg.setFrom(addressFrom);
		InternetAddress addressTo = new InternetAddress(emailAddress);

		msg.setRecipient(Message.RecipientType.TO, addressTo);

		// Setting the Subject and Content Type
		msg.setSubject(subject);
		// create and fill the first message part
		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setText(emailContent);
		// create the Multipart and add its parts to it
		Multipart mp = new MimeMultipart();
		mp.addBodyPart(mbp1);
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

}
