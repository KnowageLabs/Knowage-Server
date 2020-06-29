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
package it.eng.spagobi.security.google.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class GoogleSignInConfig {

	static private Logger logger = Logger.getLogger(GoogleSignInConfig.class);

	static protected final String GOOGLE_AUTHENTICATION_CONFIG = "google.signin.config";

	static protected final String GOOGLE_CLIENT_ID = "client_id";

	static protected Properties properties = new Properties();

	static {
		try {
			String filename = System.getProperty(GOOGLE_AUTHENTICATION_CONFIG);
			if (filename != null) {
				logger.info("Retrieved " + GOOGLE_AUTHENTICATION_CONFIG + " system property. Google SignIn configuration file is: [" + filename + "]");
				try {
					properties.load(new FileInputStream(filename));
				} catch (FileNotFoundException e) {
					logger.error("Could not find file with Google Sign-In config: file [" + filename + "] not found");
					throw new SpagoBIRuntimeException("Could not find file with Google Sign-In config: file [" + filename + "] not found", e);
				} catch (Exception e) {
					logger.error("Could not read file with Google Sign-In config [" + filename + "]");
					throw new SpagoBIRuntimeException("Could not read file with Google Sign-In config [" + filename + "] not found", e);
				}
				String clientId = properties.getProperty(GOOGLE_CLIENT_ID);
				logger.debug("Google Sign-In Client ID is [" + clientId + "]");
				Assert.assertNotBlank(clientId, "Google Sing-In Client ID was not found!");
			}
		} catch (Exception e) {
			logger.error("Error while loading Google Sing-In configuration file", e);
			throw new SpagoBIRuntimeException("Error while loading Google Sing-In configuration file", e);
		}
	}

	public static boolean isEnabled() {
		boolean toReturn = properties.containsKey(GOOGLE_CLIENT_ID);
		LogMF.debug(logger, "Google Sign-In enabled: {0}", toReturn);
		return toReturn;
	}

	public static String getClientId() {
		String toReturn = properties.getProperty(GOOGLE_CLIENT_ID);
		LogMF.debug(logger, "Google Sign-In Client ID: {0}", toReturn);
		return toReturn;
	}

}
