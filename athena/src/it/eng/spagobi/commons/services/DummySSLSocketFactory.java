/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.commons.services;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.StringUtilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.*;

import org.apache.log4j.Logger;


/**
 * DummySSLSocketFactory Used to Customize default SSLSocketFactory 
 * with the option to load custom certificates passed as files
 */
public class DummySSLSocketFactory extends SSLSocketFactory {
    private SSLSocketFactory factory;
    private static transient Logger logger = Logger.getLogger(DummySSLSocketFactory.class);
    
	/** formato dei certificati e delle CA nei file store: SunX509 */
	final  String _SSL_CERTIFICATE_FORMAT = "SunX509";
	
	/** formato dei file keystore e trustedstore: JKS */
	final  String _SSL_STORE_FORMAT = "JKS";
	
	/** tipo di SSL utilizzato: TLS (TLS 1.0) - da modificare in SSLv3 (SSL 3.0) per backward compatibility */
	public final static String _SSL_TYPE = "TLS";


	public DummySSLSocketFactory() {
		try {
			logger.debug("Initializing DummySSLSocketFactory for custom SSLSocket Connection");

			SSLContext mySSLContext = SSLContext.getInstance(_SSL_TYPE);
			TrustManager[] myTrustManagers = null;
			KeyManager[] myKeyManagers = null;


			//Custom Trusted Store Certificate Options
			String trustedStorePath = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.file"); 
			String trustedStorePassword = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.password"); 

			if ((!StringUtilities.isEmpty(trustedStorePath)) ) {
				if(StringUtilities.isEmpty(trustedStorePassword)){
					trustedStorePassword = null;
					logger.debug("No Trusted Store Password Definied");
				}

				/* configura dinamicamente il trustedstore per le CA
				 * (in alternativa ai parametri -Djavax.net.ssl.trustStore=[file.jks] e -Djavax.net.ssl.trustStorePassword=[password])
				 */
				// eventuale configurazione dinamica del trustedstore:
				logger.debug("Configuring Trusted Store with file "+trustedStorePath);
				TrustManagerFactory myTrustManager = TrustManagerFactory.getInstance(_SSL_CERTIFICATE_FORMAT);
				KeyStore myTrustStore = KeyStore.getInstance(_SSL_STORE_FORMAT);
				myTrustStore.load(new FileInputStream(trustedStorePath), trustedStorePassword.toCharArray());
				myTrustManager.init(myTrustStore);
				logger.debug("Truste Manager initilialized");
				myTrustManagers = myTrustManager.getTrustManagers();
			}

			String keyStorePath = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.keyStore.file"); 
			String keyStorePassword = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.keyStore.password"); 

			//Eventuale configurazione dinamica del keystore SOLO in mutua autenticazione:
			if ((!StringUtilities.isEmpty(keyStorePath))) {
				if(StringUtilities.isEmpty(keyStorePassword)){
					trustedStorePassword = null;
					logger.debug("No Key Store Password Definied");
				}
				logger.debug("Configuring Key Store with file "+keyStorePath);
				/* configura dinamicamente il keystore per il certificato del server
				 * (in alternativa ai parametri -Djavax.net.ssl.keyStore=[file.jks] e -Djavax.net.ssl.keyStorePassword=[password])
				 */
				KeyManagerFactory myKeyManager = KeyManagerFactory.getInstance(_SSL_CERTIFICATE_FORMAT);
				KeyStore myKeyStore = KeyStore.getInstance(_SSL_STORE_FORMAT);
				myKeyStore.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
				myKeyManagers = myKeyManager.getKeyManagers();

			}
			//Initialize SSLContext with custom KeyManagers and TrustManagers
			mySSLContext.init(myKeyManagers, myTrustManagers, null) ;

			factory = (SSLSocketFactory)mySSLContext.getSocketFactory();
		}
		catch(Exception ex) {
			logger.debug("Error in DummySSLSocketFactory: "+ex);
			ex.printStackTrace();
		}


	}

    public static SocketFactory getDefault() {
    return new DummySSLSocketFactory();
    }

    public Socket createSocket() throws IOException {
    return factory.createSocket();
    }

    public Socket createSocket(Socket socket, String s, int i, boolean flag)
                throws IOException {
    return factory.createSocket(socket, s, i, flag);
    }

    public Socket createSocket(InetAddress inaddr, int i,
                InetAddress inaddr1, int j) throws IOException {
    return factory.createSocket(inaddr, i, inaddr1, j);
    }

    public Socket createSocket(InetAddress inaddr, int i)
                throws IOException {
    return factory.createSocket(inaddr, i);
    }

    public Socket createSocket(String s, int i, InetAddress inaddr, int j)
                throws IOException {
    return factory.createSocket(s, i, inaddr, j);
    }

    public Socket createSocket(String s, int i) throws IOException {
    return factory.createSocket(s, i);
    }

    public String[] getDefaultCipherSuites() {
    return factory.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
    return factory.getSupportedCipherSuites();
    }
}
