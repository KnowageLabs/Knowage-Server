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

import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
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
import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.security.utils.EncryptionPBEWithMD5AndDESManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Build a new {@link Session} to send email.
 *
 * @author Marco Libanori
 */
public class MailSessionBuilder {

	private static final Logger LOGGER = LogManager.getLogger(MailSessionBuilder.class);

	/**
	 * Define the supported security modes.
	 */
	private enum SecurityMode {
		NONE(25), SSL(465), STARTTLS(587);

		private final int defaultPort;

		SecurityMode(int defaultPort) {
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
		private final String host;
		private final int port;
		private final String user;
		private final String password;

		private SessionFacade(Session session, InternetAddress from, SecurityMode securityMode, String host, int port,
				String user, String password) {
			this.session = session;
			this.from = from;
			this.securityMode = securityMode;
			this.host = host;
			this.port = port;
			this.user = user;
			this.password = password;
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

		public void sendMessage(Message message) throws MessagingException {
			Transport transport = null;
			Address[] allRecipients = null;
			try {
				allRecipients = message.getAllRecipients();

				transport = getTransport();
				if (securityMode == SecurityMode.SSL) {
					transport.connect(host, port, user, password);
				} else {
					transport.connect();
				}
				transport.sendMessage(message, allRecipients);
			} catch (Exception e) {
				Properties properties = session.getProperties();
				LOGGER.error("Error sending email with a session having properties {}", properties, e);
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
	private static final String TRUSTED_STORE_FILE_KEY = "MAIL.PROFILES.trustedStore.file";

	private String trustedStoreFileValue;
	private String smtpHostValue;
	private String smtpPortValue;
	private String fromValue;
	private String userValue;
	private String passwordValue;
	private String securityValue;
	private String profileName;
	private Integer timeout;
	private Integer connectionTimeout;
	private boolean debug = false;

	private MailSessionBuilder() {
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

		LOGGER.info("Using profile name {}", profileName);

		String smtpHostKey = String.format(SMTP_HOST_TEMPLATE, profileName);
		String smtpPortKey = String.format(SMTP_PORT_TEMPLATE, profileName);
		String fromKey = String.format(FROM_TEMPLATE, profileName);
		String userKey = String.format(USER_TEMPLATE, profileName);
		String passwordKey = String.format(PASSWORD_TEMPLATE, profileName);
		String securityKey = String.format(SECURITY_TEMPLATE, profileName);

		LOGGER.debug("We got the following profile keys:");
		LOGGER.debug("\tsmtpHostKey = {}", smtpHostKey);
		LOGGER.debug("\tsmtpPortKey = {}", smtpPortKey);
		LOGGER.debug("\tfromKey     = {}", fromKey);
		LOGGER.debug("\tuserKey     = {}", userKey);
		LOGGER.debug("\tpasswordKey = {}", passwordKey);
		LOGGER.debug("\tsecurityKey = {}", securityKey);

		SingletonConfig config = SingletonConfig.getInstance();

		smtpHostValue = config.getConfigValue(smtpHostKey);
		smtpPortValue = config.getConfigValue(smtpPortKey);
		fromValue = config.getConfigValue(fromKey);
		userValue = config.getConfigValue(userKey);
		passwordValue = EncryptionPBEWithMD5AndDESManager.decrypt(config.getConfigValue(passwordKey));
		securityValue = config.getConfigValue(securityKey);

		LOGGER.debug("We got the following profile values:");
		LOGGER.debug("\tsmtpHostValue = {}", smtpHostValue);
		LOGGER.debug("\tsmtpPortValue = {}", smtpPortValue);
		LOGGER.debug("\tfromValue     = {}", fromValue);
		LOGGER.debug("\tuserValue     = {}", userValue);
		LOGGER.debug("\tpasswordValue = {}", passwordValue);
		LOGGER.debug("\tsecurityValue = {}", securityValue);

		trustedStoreFileValue = config.getConfigValue(TRUSTED_STORE_FILE_KEY);

		return this;
	}

	public SessionFacade build() throws AddressException {

		Session session = null;

		Properties props = new Properties();

		LOGGER.info("Creating a new SessionFacade for profile name {}", profileName);

		SecurityMode securityValueEnum = SecurityMode.valueOf(securityValue);
		if (securityValueEnum == null) {
			securityValueEnum = SecurityMode.NONE;
		}

		String prefix = "mail.smtp";

		if (securityValueEnum == SecurityMode.SSL) {
			prefix = "mail.smtps";
		}

		if (securityValueEnum == SecurityMode.NONE) {
			props.put(prefix + ".starttls.enable", "false");
		} else if (securityValueEnum == SecurityMode.SSL) {
			props.put(prefix + ".starttls.enable", "false");
		} else if (securityValueEnum == SecurityMode.STARTTLS) {
			props.put(prefix + ".starttls.enable", "true");
		}

		props.put(prefix + ".host", smtpHostValue);

		if (StringUtils.isEmpty(smtpPortValue)) {
			int defaultPort = securityValueEnum.getDefaultPort();

			LOGGER.warn("SMTP port is empty: using the default port {}", defaultPort);
			smtpPortValue = Integer.toString(defaultPort);
		}
		props.put(prefix + ".port", smtpPortValue);

		Authenticator auth = null;
		if (StringUtils.isNotEmpty(userValue)) {
			props.put(prefix + ".user", userValue);
			props.put(prefix + ".password", passwordValue);
		}

		if (StringUtils.isEmpty(fromValue)) {
			fromValue = "spagobi.scheduler@eng.it";
		}
		InternetAddress fromValueInternetAddress = new InternetAddress(fromValue);
		props.put(prefix + ".from", fromValue);

		if (timeout != null) {
			props.put(prefix + ".timeout", timeout.toString());
		}

		if (connectionTimeout != null) {
			props.put(prefix + ".connectiontimeout", connectionTimeout.toString());
		}

		// create autheticator object
		if (StringUtils.isNotEmpty(userValue)) {
			auth = new SMTPAuthenticator(userValue, passwordValue);
			props.put(prefix + ".auth", "true");
			// SSL Connection
			if (securityValueEnum == SecurityMode.SSL) {
				try {
					SSLContext sslContext = SSLContext.getInstance("TLS", "SunJSSE");
					Provider sslProvider = sslContext.getProvider();
					Security.addProvider(sslProvider);
					props.put(prefix + ".auth", "true");
					props.put(prefix + ".socketFactory.port", smtpPortValue);
					if ((!StringUtils.isEmpty(trustedStoreFileValue))) {
						props.put(prefix + ".socketFactory.class", CUSTOM_SSL_FACTORY);

					} else {
						props.put(prefix + ".socketFactory.class", DEFAULT_SSL_FACTORY);
					}
					props.put(prefix + ".socketFactory.fallback", "false");
				} catch (NoSuchAlgorithmException | java.security.NoSuchProviderException e) {
					// TODO: handle exception
				}
			}
			session = Session.getInstance(props, auth);
		} else {
			session = Session.getInstance(props);
		}

		if (debug) {
			session.setDebug(true);
			PrintStream ps = IoBuilder.forLogger(LOGGER).setLevel(Level.DEBUG).buildPrintStream();
			session.setDebugOut(ps);
		}

		LOGGER.debug("Session created using properties {}", props);
		LOGGER.info("End creating a new SessionFacade for profile name {}", profileName);

		return new SessionFacade(session, fromValueInternetAddress, securityValueEnum, smtpHostValue,
				Integer.parseInt(smtpPortValue), userValue, passwordValue);
	}

	public MailSessionBuilder withTimeout(int timeout) {
		this.timeout = timeout;

		return this;
	}

	public MailSessionBuilder withConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;

		return this;
	}

	public MailSessionBuilder setFromAddress(String fromValue) {
		if (StringUtils.isNotEmpty(fromValue)) {
			LOGGER.warn("Overwriting the from address...");
			LOGGER.debug("... with {} replacing {}", fromValue, this.fromValue);
			this.fromValue = fromValue;
		}

		return this;
	}

	public MailSessionBuilder setUser(String userValue) {
		if (StringUtils.isNotEmpty(userValue)) {
			LOGGER.warn("Overwriting the username...");
			LOGGER.debug("... with {} replacing {}", userValue, this.userValue);
			this.userValue = userValue;
		}

		return this;
	}

	public MailSessionBuilder setPassword(String passwordValue) {
		if (StringUtils.isNotEmpty(passwordValue)) {
			LOGGER.warn("Overwriting the password...");
			LOGGER.debug("... with {} replacing {}", passwordValue, this.passwordValue);
			this.passwordValue = passwordValue;
		}

		return this;
	}

	public MailSessionBuilder setHost(String smtpHostValue) {
		if (StringUtils.isNotEmpty(smtpHostValue)) {
			LOGGER.warn("Overwriting the host...");
			LOGGER.debug("... with {} replacing {}", smtpHostValue, this.smtpHostValue);
			this.smtpHostValue = smtpHostValue;
		}

		return this;
	}

	public MailSessionBuilder setPort(String smtpPortValue) {
		if (StringUtils.isNotEmpty(smtpPortValue)) {
			LOGGER.warn("Overwriting the port...");
			LOGGER.debug("... with {} replacing {}", smtpPortValue, this.smtpPortValue);
			this.smtpPortValue = smtpPortValue;
		}

		return this;
	}

	public MailSessionBuilder setSecurityMode(String securityValue) {
		if (StringUtils.isNotEmpty(securityValue)) {
			LOGGER.warn("Overwriting the security mode...");
			LOGGER.debug("... with {} replacing {}", securityValue, this.securityValue);
			this.securityValue = securityValue;
		}

		return this;
	}

	public MailSessionBuilder enableDebug() {
		debug = true;
		return this;
	}

}
