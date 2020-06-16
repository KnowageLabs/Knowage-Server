/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.mail;

import java.security.Security;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Build a new {@link Session} to send email.
 *
 * @author Marco Libanori
 */
public class MailSessionBuilder {

	private static Logger logger = Logger.getLogger(MailSessionBuilder.class);

	/**
	 * Define the supported security modes.
	 */
	private static enum SecurityMode {
		NONE(25),
		SSL(465),
		STARTTLS(587);

		private final int defaultPort;

		private SecurityMode(int defaultPort) {
			this.defaultPort = defaultPort;
		}

		/**
		 * @return the defaultPort
		 */
		public int getDefaultPort() {
			return defaultPort;
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

	/**
	 * Facade for {@link Session} object.
	 *
	 * It simplifies the contract to get only the needed methods.
	 */
	public static class SessionFacade {

		private final Session session;
		private final InternetAddress from;
		private final SecurityMode securityMode;

		private SessionFacade(Session session, InternetAddress from, SecurityMode securityMode) {
			this.session = session;
			this.from = from;
			this.securityMode = securityMode;
		}

		public Transport getTransport() throws NoSuchProviderException {
			if (securityMode == SecurityMode.SSL) {
				return session.getTransport("smtps");
			} else {
				return session.getTransport("smtp");
			}
		}

		public Message createNewMimeMessage() throws MessagingException {
			MimeMessage ret = new MimeMessage(session);
			ret.setFrom(from);
			return ret;
		}

		public void sendMessage(Message message) throws NoSuchProviderException, MessagingException {
			Transport transport = null;
			Address[] allRecipients = null;
			try {
				allRecipients = message.getAllRecipients();

				transport = getTransport();
				transport.connect();
				transport.sendMessage(message, allRecipients);
			} catch (Exception e) {
				Properties properties = session.getProperties();
				LogMF.error(logger, e, "Error sending email from {0} to {1} with session having properties {2}", new String[] { from.toString(), String.valueOf(allRecipients), String.valueOf(properties) });
				throw e;
			} finally {
				if (transport != null) {
					try {
						transport.close();
					} catch (Exception e) {
						// Yes, it's muted!
					}
				}
			}
		}
	}

	private static final String DEFAULT_SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	private static final String CUSTOM_SSL_FACTORY = "it.eng.spagobi.commons.services.DummySSLSocketFactory";

	private static final String SMTP_HOST_TEMPLATE = "MAIL.PROFILES.%s.smtphost";
	private static final String SMTP_PORT_TEMPLATE = "MAIL.PROFILES.%s.smtpport";
	private static final String FROM_TEMPLATE = "MAIL.PROFILES.%s.from";
	private static final String USER_TEMPLATE = "MAIL.PROFILES.%s.user";
	private static final String PASSWORD_TEMPLATE = "MAIL.PROFILES.%s.password";
	private static final String SECURITY_TEMPLATE = "MAIL.PROFILES.%s.security";

	private final String trustedStoreFileKey = "MAIL.PROFILES.trustedStore.file";

	private final String trustedStoreFileValue;
	private String smtpHostValue;
	private String smtpPortValue;
	private String fromValue;
	private String userValue;
	private String passwordValue;
	private String securityValue;
	private String profileName;
	private Integer timeout;
	private Integer connectionTimeout;

	private MailSessionBuilder() {
		SingletonConfig config = SingletonConfig.getInstance();

		trustedStoreFileValue = config.getConfigValue(trustedStoreFileKey);
	}

	/**
	 * @return This builder
	 */
	public static MailSessionBuilder newInstance() {
		return new MailSessionBuilder();
	}

	/**
	 * @return This builder
	 */
	public MailSessionBuilder usingSchedulerProfile() {
		return usingProfile("scheduler");
	}

	/**
	 * @return This builder
	 */
	public MailSessionBuilder usingUserProfile() {
		return usingProfile("user");
	}

	/**
	 * @return This builder
	 */
	public MailSessionBuilder usingKpiAlarmProfile() {
		return usingProfile("kpi_alarm");
	}

	/**
	 * Use specific profile name, defined in the configuration.
	 *
	 * @param profileName Name of the profile
	 * @return This builder
	 */
	public MailSessionBuilder usingProfile(String profileName) {

		if (StringUtils.isEmpty(profileName)) {
			throw new SpagoBIRuntimeException("Email's profile name cannot be empty");
		}
		this.profileName = profileName;

		LogMF.info(logger, "Using profile name {0}", profileName);

		String smtpHostKey = String.format(SMTP_HOST_TEMPLATE, profileName);
		String smtpPortKey = String.format(SMTP_PORT_TEMPLATE, profileName);
		String fromKey     = String.format(FROM_TEMPLATE, profileName);
		String userKey     = String.format(USER_TEMPLATE, profileName);
		String passwordKey = String.format(PASSWORD_TEMPLATE, profileName);
		String securityKey = String.format(SECURITY_TEMPLATE, profileName);

		logger.debug("We got the following profile keys:");
		LogMF.debug(logger, "\tsmtpHostKey = {0}", smtpHostKey);
		LogMF.debug(logger, "\tsmtpPortKey = {0}", smtpPortKey);
		LogMF.debug(logger, "\tfromKey     = {0}", fromKey    );
		LogMF.debug(logger, "\tuserKey     = {0}", userKey    );
		LogMF.debug(logger, "\tpasswordKey = {0}", passwordKey);
		LogMF.debug(logger, "\tsecurityKey = {0}", securityKey);

		SingletonConfig config = SingletonConfig.getInstance();

		smtpHostValue = config.getConfigValue(smtpHostKey);
		smtpPortValue = config.getConfigValue(smtpPortKey);
		fromValue     = config.getConfigValue(fromKey);
		userValue     = config.getConfigValue(userKey);
		passwordValue = config.getConfigValue(passwordKey);
		securityValue = config.getConfigValue(securityKey);

		logger.debug("We got the following profile values:");
		LogMF.debug(logger, "\tsmtpHostValue = {0}", smtpHostValue);
		LogMF.debug(logger, "\tsmtpPortValue = {0}", smtpPortValue);
		LogMF.debug(logger, "\tfromValue     = {0}", fromValue    );
		LogMF.debug(logger, "\tuserValue     = {0}", userValue    );
		LogMF.debug(logger, "\tpasswordValue = {0}", passwordValue);
		LogMF.debug(logger, "\tsecurityValue = {0}", securityValue);

		return this;
	}

	public SessionFacade build() throws AddressException {

		Session session = null;

		Properties props = new Properties();

		LogMF.info(logger, "Creating a new SessionFacade for profile name {0}", profileName);

		SecurityMode securityValueEnum = SecurityMode.valueOf(securityValue);
		if (securityValueEnum == null) {
			securityValueEnum = SecurityMode.NONE;
		}
		if (securityValueEnum == SecurityMode.NONE) {
			props.put("mail.smtp.starttls.enable", "false");
		} else if (securityValueEnum == SecurityMode.SSL) {
			props.put("mail.smtp.starttls.enable", "false");
		} else if (securityValueEnum == SecurityMode.STARTTLS) {
			props.put("mail.smtp.starttls.enable", "true");
		}


		props.put("mail.smtp.host", smtpHostValue);


		if (StringUtils.isEmpty(smtpPortValue)) {
			int defaultPort = securityValueEnum.getDefaultPort();

			LogMF.warn(logger, "SMTP port is empty: using the default port {0}", defaultPort);
			smtpPortValue = Integer.toString(defaultPort);
		}
		props.put("mail.smtp.port", smtpPortValue);


		Authenticator auth = null;
		if (StringUtils.isNotEmpty(userValue)) {
			props.put("mail.smtp.user", userValue);
			props.put("mail.smtp.password", passwordValue);
		}


		if (StringUtils.isEmpty(fromValue)) {
			fromValue = "spagobi.scheduler@eng.it";
		}
		InternetAddress fromValueInternetAddress = new InternetAddress(fromValue);
		props.put("mail.smtp.from", fromValue);


		if (timeout != null) {
			props.put("mail.smtp.timeout", timeout.toString());
		}


		if (connectionTimeout != null) {
			props.put("mail.smtp.connectiontimeout", connectionTimeout.toString());
		}


		// create autheticator object
		if (StringUtils.isNotEmpty(userValue)) {
			auth = new SMTPAuthenticator(userValue, passwordValue);
			props.put("mail.smtp.auth", "true");
			// SSL Connection
			if (securityValueEnum == SecurityMode.SSL) {
				Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
				props.put("mail.smtps.auth", "true");
				props.put("mail.smtps.socketFactory.port", smtpPortValue);
				if ((!StringUtilities.isEmpty(trustedStoreFileValue))) {
					props.put("mail.smtps.socketFactory.class", CUSTOM_SSL_FACTORY);

				} else {
					props.put("mail.smtps.socketFactory.class", DEFAULT_SSL_FACTORY);
				}
				props.put("mail.smtp.socketFactory.fallback", "false");
			}
			session = Session.getInstance(props, auth);
		} else {
			session = Session.getInstance(props);
		}


		// TODO Debug???
		// session.setDebug(true);
		// session.setDebugOut(System.out);


		LogMF.debug(logger, "Session created using properties {0}", props);
		LogMF.info(logger, "End creating a new SessionFacade for profile name {0}", profileName);

		return new SessionFacade(session, fromValueInternetAddress, securityValueEnum);
	}

	public MailSessionBuilder withTimeout(int timeout) {
		this.timeout = timeout;

		return this;
	}

	public MailSessionBuilder withConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;

		return this;
	}

	public MailSessionBuilder overwritingFromAddress(String fromValue) {
		if (StringUtils.isNotEmpty(fromValue)) {
			logger.warn("Overwriting the from address...");
			LogMF.debug(logger, "... with {0} replacing {1}", fromValue, this.fromValue);
			this.fromValue = fromValue;
		}

		return this;
	}

	public MailSessionBuilder overwritingUser(String userValue) {
		if (StringUtils.isNotEmpty(userValue)) {
			logger.warn("Overwriting the username...");
			LogMF.debug(logger, "... with {0} replacing {1}", userValue, this.userValue);
			this.userValue = userValue;
		}

		return this;
	}

	public MailSessionBuilder overwritingPassword(String passwordValue) {
		if (StringUtils.isNotEmpty(passwordValue)) {
			logger.warn("Overwriting the password...");
			LogMF.debug(logger, "... with {0} replacing {1}", passwordValue, this.passwordValue);
			this.passwordValue = passwordValue;
		}

		return this;
	}
}
