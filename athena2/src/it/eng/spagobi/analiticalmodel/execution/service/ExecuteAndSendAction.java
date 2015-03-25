/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionController;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ExecutionProxy;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class ExecuteAndSendAction extends AbstractHttpAction {

	private static transient Logger logger = Logger.getLogger(ExecuteAndSendAction.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.commons.services.BaseProfileAction#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean responseSb) throws Exception {
		logger.debug("IN");

		freezeHttpResponse();
		HttpServletResponse response = getHttpResponse();
		HttpServletRequest req = getHttpRequest();

		final String OK = "10";
		final String ERROR = "20";
		final String TONOTFOUND = "90";
		String retCode = "";
		final String DEFAULT_SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		final String CUSTOM_SSL_FACTORY = "it.eng.spagobi.commons.services.DummySSLSocketFactory";

		try {

			// GET PARAMETER
			String objid = "";
			String objLabel = "";
			String to = "";
			String cc = "";
			String object = "";
			String message = "";
			String queryStr = "";
			String userId = "";
			String login = "";
			String pass = "";
			String from = "";

			// Creo una lista con un suo iteratore con dentro i parametri della
			// request
			List params = request.getContainedAttributes();
			ListIterator it = params.listIterator();

			while (it.hasNext()) {

				Object par = it.next();
				SourceBeanAttribute p = (SourceBeanAttribute) par;
				String parName = p.getKey();
				logger.debug("got parName=" + parName);
				if (parName.equals("objlabel")) {
					objLabel = (String) request.getAttribute("objlabel");
					logger.debug("got objLabel from Request=" + objLabel);
				} else if (parName.equals("objid")) {
					objid = (String) request.getAttribute("objid");
					logger.debug("got objid from Request=" + objid);
				} else if (parName.equals("to")) {
					to = (String) request.getAttribute("to");
					logger.debug("got to from Request=" + to);
				} else if (parName.equals("cc")) {
					cc = (String) request.getAttribute("cc");
					logger.debug("got cc from Request=" + cc);
				} else if (parName.equals("object")) {
					object = (String) request.getAttribute("object");
					logger.debug("got object from Request=" + object);
				} else if (parName.equals("message")) {
					message = (String) request.getAttribute("message");
					logger.debug("got message from Request=" + message);
				} else if (parName.equals("userid")) {
					userId = (String) request.getAttribute("userid");
					logger.info("got userId from Request=" + userId);
				} else if (parName.equals("login")) {
					login = (String) request.getAttribute("login");
					logger.info("got user from Request" + login);
				} else if (parName.equals("pwd")) {
					pass = (String) request.getAttribute("pwd");
					logger.info("got pwd from Request");
				} else if (parName.equals("replyto")) {
					from = (String) request.getAttribute("replyto");
					logger.info("got email to reply to, from Request" + from);
				} else if (parName.equals("NEW_SESSION")) {
					continue;
				} else {
					String value = (String) request.getAttribute(parName);
					queryStr += parName + "=" + value + "&";
				}
			}

			if (to.equals("")) {
				retCode = TONOTFOUND;
				logger.error("To Address not found");
				throw new Exception("To Address not found");
			}

			String returnedContentType = "";
			String fileextension = "";
			byte[] documentBytes = null;

			IBIObjectDAO biobjdao = DAOFactory.getBIObjectDAO();
			BIObject biobj = null;
			if (objLabel != null && !objLabel.trim().equals("")) {
				biobj = biobjdao.loadBIObjectByLabel(objLabel);
			} else {
				biobj = biobjdao.loadBIObjectById(new Integer(objid));
			}
			// create the execution controller
			ExecutionController execCtrl = new ExecutionController();
			execCtrl.setBiObject(biobj);

			// fill parameters
			execCtrl.refreshParameters(biobj, queryStr);

			// set the description of the biobject parameters
			setParametersDescription(biobj.getBiObjectParameters(), params);

			// exec the document only if all its parameters are filled
			// Why???? if a parameter is not mandatory and the user did not fill it????
			// if (execCtrl.directExecution()) {
			ExecutionProxy proxy = new ExecutionProxy();
			proxy.setBiObject(biobj);

			IEngUserProfile profile = null;
			RequestContainer reqCont = RequestContainer.getRequestContainer();
			SessionContainer sessCont = reqCont.getSessionContainer();
			SessionContainer permSess = sessCont.getPermanentContainer();
			profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

			if (profile == null) {

				profile = UserUtilities.getUserProfile(req);

			}

			documentBytes = proxy.exec(profile, ExecutionProxy.SEND_MAIL_MODALITY, null);
			returnedContentType = proxy.getReturnedContentType();
			fileextension = proxy.getFileExtensionFromContType(returnedContentType);
			// } end if (execCtrl.directExecution()) {
			// SEND MAIL

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
				throw new Exception("Smtp PORT not configured");
			} else {
				smptPort = Integer.parseInt(smtpport);
			}
			if ((from == null) || from.trim().equals(""))
				from = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.from");
			if (login == null || login.trim().equals(""))
				login = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.user");
			if (pass == null || pass.trim().equals(""))
				pass = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.password");

			if ((from == null) || from.trim().equals(""))
				throw new Exception("From field missing from input form or not configured");

			// Set the host smtp address
			Properties props = new Properties();
			props.put("mail.smtp.host", smtphost);
			props.put("mail.smtp.port", Integer.toString(smptPort));

			Session session = null;
			if (StringUtilities.isEmpty(login) || StringUtilities.isEmpty(pass)) {
				props.put("mail.smtp.auth", "false");
				session = Session.getInstance(props);
				logger.debug("Connecting to mail server without authentication");
			} else {
				props.put("mail.smtp.auth", "true");
				Authenticator auth = new SMTPAuthenticator(login, pass);
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

				session = Session.getInstance(props, auth);
				// session.setDebug(true);
				// session.setDebugOut(null);
				logger.debug("Connecting to mail server with authentication");
			}

			logger.debug("properties: mail.smtp.host:" + smtphost + " mail.smtp.port:" + smtpport);

			// create a message
			Message msg = new MimeMessage(session);
			// set the from / to / cc address
			InternetAddress addressFrom = new InternetAddress(from);
			msg.setFrom(addressFrom);
			String[] recipients = to.split(",");
			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
			if ((cc != null) && !cc.trim().equals("")) {
				recipients = cc.split(",");
				InternetAddress[] addressCC = new InternetAddress[recipients.length];
				for (int i = 0; i < recipients.length; i++) {
					String cc_add = recipients[i];
					if ((cc_add != null) && !cc_add.trim().equals("")) {
						addressCC[i] = new InternetAddress(recipients[i]);
					}
				}
				msg.setRecipients(Message.RecipientType.CC, addressCC);
			}
			// Setting the Subject and Content Type
			msg.setSubject(object);

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(message);
			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();
			// attach the file to the message
			SchedulerDataSource sds = new SchedulerDataSource(documentBytes, returnedContentType, "result" + fileextension);
			mbp2.setDataHandler(new DataHandler(sds));
			mbp2.setFileName(sds.getName());
			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mp.addBodyPart(mbp2);
			// add the Multipart to the message
			msg.setContent(mp);
			// send message
			EMFErrorHandler errorHandler = getErrorHandler();
			if (errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
				if ((smtpssl.equals("true")) && (!StringUtilities.isEmpty(login)) && (!StringUtilities.isEmpty(pass))) {
					// USE SSL Transport comunication with SMTPS
					Transport transport = session.getTransport("smtps");
					transport.connect(smtphost, smptPort, login, pass);
					transport.sendMessage(msg, msg.getAllRecipients());
					transport.close();
				} else {
					// Use normal SMTP
					Transport.send(msg);
				}
				retCode = OK;
			} else {
				logger.error("Error while executing and sending object " + errorHandler.getStackTrace());
				retCode = ERROR;
			}

		} catch (Exception e) {
			logger.error("Error while executing and sending object ", e);
			if (retCode.equals("")) {
				retCode = ERROR;
			}
		} finally {
			try {
				response.getOutputStream().write(retCode.getBytes());
				response.getOutputStream().flush();
			} catch (Exception ex) {
				logger.error("Error while sending response to client", ex);
			}
		}
		logger.debug("OUT");
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

	/**
	 * Add the description to the BIObjectparameters
	 * 
	 * @param BIObjectParameters
	 * @param attributes
	 */
	public void setParametersDescription(List<BIObjectParameter> BIObjectParameters, List<SourceBeanAttribute> attributes) {
		Map<String, String> parameterNameDescriptionMap = new HashMap<String, String>();
		// we create a map: parameter name, parameter description
		for (int i = 0; i < attributes.size(); i++) {
			SourceBeanAttribute sba = attributes.get(i);
			// the name of parameter in the request with the description is parametername+ field_visible_description
			int descriptionPosition = sba.getKey().indexOf("field_visible_description");
			if (descriptionPosition > 0) {
				parameterNameDescriptionMap.put(sba.getKey().substring(0, descriptionPosition - 1), (String) sba.getValue());
			}
		}
		for (int i = 0; i < BIObjectParameters.size(); i++) {
			String bobjName = BIObjectParameters.get(i).getParameterUrlName();
			String value = parameterNameDescriptionMap.get(bobjName);
			if (value != null) {
				BIObjectParameters.get(i).setParameterValuesDescription(parseDescriptionString(value));
			}
		}
	}

	/**
	 * Parse a string with the description of the parameter and return a list with description.. This transformation is necessary because the multivalues
	 * parameters
	 * 
	 * @param s
	 *            the string with the description
	 * @return the list of descriptions
	 */
	public List<String> parseDescriptionString(String s) {
		List<String> descriptions = new ArrayList<String>();
		StringTokenizer stk = new StringTokenizer(s, ";");
		while (stk.hasMoreTokens()) {
			descriptions.add(stk.nextToken());
		}
		return descriptions;
	}

}
