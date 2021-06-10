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
package it.eng.spagobi.security.azure.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class AzureSignInConfig {

	static private Logger logger = Logger.getLogger(AzureSignInConfig.class);

	static private final String AZURE_AUTHENTICATION_BASE_URL = "https://login.microsoftonline.com/";

	static protected final String AZURE_AUTHENTICATION_CONFIG = "azure.signin.config";

	static protected final String AZURE_CLIENT_ID = "client_id";

	static protected final String AZURE_TENANT_ID = "tenant_id";

	static protected final String IS_ENABLED = "enabled";

	static protected Properties properties = new Properties();

	static {
		try {
			String filename = System.getProperty(AZURE_AUTHENTICATION_CONFIG);
			if (filename != null) {
				logger.info("Retrieved " + AZURE_AUTHENTICATION_CONFIG + " system property. Azure SignIn configuration file is: [" + filename + "]");
				try {
					properties.load(new FileInputStream(filename));
				} catch (FileNotFoundException e) {
					logger.error("Could not find file with Azure Sign-In config: file [" + filename + "] not found");
					throw new SpagoBIRuntimeException("Could not find file with Azure Sign-In config: file [" + filename + "] not found", e);
				} catch (Exception e) {
					logger.error("Could not read file with Azure Sign-In config [" + filename + "]");
					throw new SpagoBIRuntimeException("Could not read file with Azure Sign-In config [" + filename + "] not found", e);
				}
				String clientId = properties.getProperty(AZURE_CLIENT_ID);
				logger.debug("Azure Sign-In Client ID is [" + clientId + "]");
				Assert.assertNotBlank(clientId, "Azure Sing-In Client ID was not found!");
			}
		} catch (Exception e) {
			logger.error("Error while loading Azure Sing-In configuration file", e);
			throw new SpagoBIRuntimeException("Error while loading Azure Sing-In configuration file", e);
		}
	}

	public static boolean isEnabled() {
		String enabled = properties.getProperty(IS_ENABLED, "false");
		LogMF.debug(logger, "Azure Sign-In enabled: {0}", enabled);
		return new Boolean(enabled);
	}

	public static String getClientId() {
		String toReturn = properties.getProperty(AZURE_CLIENT_ID);
		LogMF.debug(logger, "Azure Sign-In Client ID: {0}", toReturn);
		return toReturn;
	}

	public static String getTenantId() {
		String toReturn = properties.getProperty(AZURE_TENANT_ID);
		LogMF.debug(logger, "Azure Sign-In Tenant ID: {0}", toReturn);
		return toReturn;
	}

	public static String getAuthorityId() {
		String toReturn = AZURE_AUTHENTICATION_BASE_URL + getTenantId();
		LogMF.debug(logger, "Azure Sign-In Authority ID: {0}", toReturn);
		return toReturn;
	}

	public static String getJwkProviderUrl() {
		String toReturn = AZURE_AUTHENTICATION_BASE_URL + getTenantId() + "/discovery/v2.0/keys";
		LogMF.debug(logger, "Azure JWK provider url: {0}", toReturn);
		return toReturn;
	}

}
