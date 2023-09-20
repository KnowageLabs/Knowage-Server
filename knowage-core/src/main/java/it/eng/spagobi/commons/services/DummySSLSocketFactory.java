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
package it.eng.spagobi.commons.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.security.utils.EncryptionPBEWithMD5AndDESManager;

/**
 * DummySSLSocketFactory Used to Customize default SSLSocketFactory with the option to load custom certificates passed as files
 */
public class DummySSLSocketFactory extends SSLSocketFactory {
	private SSLSocketFactory factory;
	private static transient Logger logger = Logger.getLogger(DummySSLSocketFactory.class);

	/** formato dei certificati e delle CA nei file store: SunX509 */
	final String _SSL_CERTIFICATE_FORMAT = "SunX509";

	/** formato dei file keystore e trustedstore: JKS */
	final String _SSL_STORE_FORMAT = "JKS";

	/** tipo di SSL utilizzato: TLS (TLS 1.0) - da modificare in SSLv3 (SSL 3.0) per backward compatibility */
	public static final String _SSL_TYPE = "TLS";

	public DummySSLSocketFactory() {
		try {
			logger.debug("Initializing DummySSLSocketFactory for custom SSLSocket Connection");

			SSLContext mySSLContext = SSLContext.getInstance(_SSL_TYPE);
			TrustManager[] myTrustManagers = null;
			KeyManager[] myKeyManagers = null;

			// Custom Trusted Store Certificate Options
			String trustedStorePath = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.file");
			String trustedStorePassword = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.password");

			if ((!StringUtilities.isEmpty(trustedStorePath))) {
				if (StringUtilities.isEmpty(trustedStorePassword)) {
					trustedStorePassword = null;
					logger.debug("No Trusted Store Password Definied");
				} else {
					trustedStorePassword = EncryptionPBEWithMD5AndDESManager.decrypt(trustedStorePassword);
				}

				/*
				 * configura dinamicamente il trustedstore per le CA (in alternativa ai parametri -Djavax.net.ssl.trustStore=[file.jks] e
				 * -Djavax.net.ssl.trustStorePassword=[password])
				 */
				// eventuale configurazione dinamica del trustedstore:
				logger.debug("Configuring Trusted Store with file " + trustedStorePath);
				TrustManagerFactory myTrustManager = TrustManagerFactory.getInstance(_SSL_CERTIFICATE_FORMAT);
				KeyStore myTrustStore = KeyStore.getInstance(_SSL_STORE_FORMAT);
				myTrustStore.load(new FileInputStream(trustedStorePath), trustedStorePassword.toCharArray());
				myTrustManager.init(myTrustStore);
				logger.debug("Truste Manager initilialized");
				myTrustManagers = myTrustManager.getTrustManagers();
			}

			String keyStorePath = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.keyStore.file");
			String keyStorePassword = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.keyStore.password");

			// Eventuale configurazione dinamica del keystore SOLO in mutua autenticazione:
			if ((!StringUtilities.isEmpty(keyStorePath))) {
				if (StringUtilities.isEmpty(keyStorePassword)) {
					trustedStorePassword = null;
					logger.debug("No Key Store Password Definied");
				} else {
					trustedStorePassword = EncryptionPBEWithMD5AndDESManager.decrypt(trustedStorePassword);
				}
				logger.debug("Configuring Key Store with file " + keyStorePath);
				/*
				 * configura dinamicamente il keystore per il certificato del server (in alternativa ai parametri -Djavax.net.ssl.keyStore=[file.jks] e
				 * -Djavax.net.ssl.keyStorePassword=[password])
				 */
				KeyManagerFactory myKeyManager = KeyManagerFactory.getInstance(_SSL_CERTIFICATE_FORMAT);
				KeyStore myKeyStore = KeyStore.getInstance(_SSL_STORE_FORMAT);
				myKeyStore.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
				myKeyManagers = myKeyManager.getKeyManagers();

			}
			// Initialize SSLContext with custom KeyManagers and TrustManagers
			mySSLContext.init(myKeyManagers, myTrustManagers, null);

			factory = mySSLContext.getSocketFactory();
		} catch (Exception ex) {
			logger.debug("Error in DummySSLSocketFactory: " + ex);
			ex.printStackTrace();
		}

	}

	public static SocketFactory getDefault() {
		return new DummySSLSocketFactory();
	}

	@Override
	public Socket createSocket() throws IOException {
		try {
			return factory.createSocket();
		} catch (SSLException sslException) {
			logger.error("SSLException occurred while creating socket in DummySSLSocketFactory: ", sslException);
			throw sslException;
		} catch (IOException ioException) {
			logger.error("IOException occurred while creating socket in DummySSLSocketFactory: ", ioException);
			throw ioException;
		}
	}

	@Override
	public Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException {
		try {
			return factory.createSocket(socket, s, i, flag);
		} catch (SSLException sslException) {
			logger.error("SSLException occurred while creating socket in DummySSLSocketFactory: ", sslException);
			throw sslException;
		} catch (IOException ioException) {
			logger.error("IOException occurred while creating socket in DummySSLSocketFactory: ", ioException);
			throw ioException;
		}
	}

	@Override
	public Socket createSocket(InetAddress inaddr, int i, InetAddress inaddr1, int j) throws IOException {
		try {
			return factory.createSocket(inaddr, i, inaddr1, j);
		} catch (SSLException sslException) {
			logger.error("SSLException occurred while creating socket in DummySSLSocketFactory: ", sslException);
			throw sslException;
		} catch (IOException ioException) {
			logger.error("IOException occurred while creating socket in DummySSLSocketFactory: ", ioException);
			throw ioException;
		}
	}

	@Override
	public Socket createSocket(InetAddress inaddr, int i) throws IOException {
		try {
			return factory.createSocket(inaddr, i);
		} catch (SSLException sslException) {
			logger.error("SSLException occurred while creating socket in DummySSLSocketFactory: ", sslException);
			throw sslException;
		} catch (IOException ioException) {
			logger.error("IOException occurred while creating socket in DummySSLSocketFactory: ", ioException);
			throw ioException;
		}
	}

	@Override
	public Socket createSocket(String s, int i, InetAddress inaddr, int j) throws IOException {
		try {
			return factory.createSocket(s, i, inaddr, j);
		} catch (SSLException sslException) {
			logger.error("SSLException occurred while creating socket in DummySSLSocketFactory: ", sslException);
			throw sslException;
		} catch (IOException ioException) {
			logger.error("IOException occurred while creating socket in DummySSLSocketFactory: ", ioException);
			throw ioException;
		}
	}

	@Override
	public Socket createSocket(String s, int i) throws IOException {
		try {
			return factory.createSocket(s, i);
		} catch (SSLException sslException) {
			logger.error("SSLException occurred while creating socket in DummySSLSocketFactory: ", sslException);
			throw sslException;
		} catch (IOException ioException) {
			logger.error("IOException occurred while creating socket in DummySSLSocketFactory: ", ioException);
			throw ioException;
		}
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return factory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return factory.getSupportedCipherSuites();
	}
}
