package it.eng.spagobi.tools.alert.action;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;

import java.security.Security;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

public class SendMail extends AbstractAlertAction {

	private static Logger logger = Logger.getLogger(SendMail.class);

	final String DEFAULT_SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	final String CUSTOM_SSL_FACTORY = "it.eng.spagobi.commons.services.DummySSLSocketFactory";

	@Override
	public void execute(String jsonOptions, Map<String, String> externalParameters) throws SpagoBIException {
		InputParam params = (InputParam) JsonConverter.jsonToObject(jsonOptions, InputParam.class);
		String subject = params.getSubject();
		String[] recipients = params.getMailTo();
		StringBuilder body = params.getBody() != null ? new StringBuilder(params.getBody()) : new StringBuilder();
		if (body.length() > 0 && externalParameters != null && !externalParameters.isEmpty()) {
			for (Entry<String, String> entry : externalParameters.entrySet()) {
				int start = body.indexOf(entry.getKey());
				int end = start + entry.getKey().length();
				body.replace(start, end, entry.getValue());
			}
		}
		String mailTxt = body.toString();
		String host = getConfigValue("MAIL.PROFILES.kpi_alarm.smtphost");
		String port = getConfigValue("MAIL.PROFILES.kpi_alarm.smtpport");
		int portInt = Integer.parseInt(port);
		String from = getConfigValue("MAIL.PROFILES.kpi_alarm.from");
		String user = getConfigValue("MAIL.PROFILES.kpi_alarm.user");
		String pwd = getConfigValue("MAIL.PROFILES.kpi_alarm.password");
		String ssl = getConfigValue("MAIL.PROFILES.kpi_alarm.useSSL");
		String trustedStorePath = getConfigValue("MAIL.PROFILES.trustedStore.file");
		// open session
		Session session = null;
		// create autheticator object
		Authenticator auth = null;
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		// Set timeout limit for mail server to respond
		props.put("mail.smtp.timeout", "5000");
		props.put("mail.smtp.connectiontimeout", "5000");
		if (user.equals("")) {
			session = Session.getInstance(props);
		} else {
			auth = new SMTPAuthenticator(user, pwd);
			props.put("mail.smtp.auth", "true");
			if (ssl.equals("true")) {
				Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
				// props.put("mail.smtp.debug", "true");
				props.put("mail.smtps.auth", "true");
				props.put("mail.smtps.socketFactory.port", port);
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

			logger.info("Session.getInstance(props, auth)");
		}

		// create a message
		MimeMessage msg = new MimeMessage(session);

		try {
			// set the from and to address
			InternetAddress addressFrom = new InternetAddress(from);
			msg.setFrom(addressFrom);
			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);

			// Setting the Subject and Content Type
			msg.setSubject(subject);

			msg.setContent(mailTxt, "text/html");

			// send message
			if (("true".equals(ssl)) && (!StringUtilities.isEmpty(user)) && (!StringUtilities.isEmpty(pwd))) {
				// USE SSL Transport comunication with SMTPS
				Transport transport = session.getTransport("smtps");
				transport.connect(host, portInt, user, pwd);
				transport.sendMessage(msg, msg.getAllRecipients());
				transport.close();
			} else {
				// Use normal SMTP
				Transport.send(msg);
			}
		} catch (MessagingException e) {
			// TODO rise specific exception
			logger.error("Send mail failed", e);
			throw new SpagoBIException("Send mail failed", e);
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

	private static String getConfigValue(String key) {
		return SingletonConfig.getInstance().getConfigValue(key);
	}

}

class InputParam {
	// Comma (or semicolon) separated e-mail addresses
	private String[] mailTo;
	// E-mail subject
	private String subject;
	// Html e-mail body
	private String body;

	// At runtime values will replace keys in body content
	// private Map<String, String> parameterMap;

	/**
	 * @return the mailTo
	 */
	public String[] getMailTo() {
		return mailTo;
	}

	/**
	 * @param mailTo
	 *            the mailTo to set
	 */
	public void setMailTo(String[] mailTo) {
		this.mailTo = mailTo;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @return the parameterMap
	 */
	// public Map<String, String> getParameterMap() {
	// return parameterMap;
	// }

	/**
	 * @param parameterMap
	 *            the parameterMap to set
	 */
	// public void setParameterMap(Map<String, String> parameterMap) {
	// this.parameterMap = parameterMap;
	// }

}