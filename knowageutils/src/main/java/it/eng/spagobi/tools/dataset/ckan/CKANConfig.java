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
package it.eng.spagobi.tools.dataset.ckan;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class CKANConfig {

	private static final Logger LOGGER = Logger.getLogger(CKANConfig.class);

	public static final String CKAN_CONFIG_PROPERTY = "ckan.config";
	private static final String CKAN_CONFIG_FILE = "ckan.config.properties";
	private static CKANConfig instance = null;
	private static Properties config = null;

	private CKANConfig() {
		config = loadConfig();
	}

	private static Properties loadConfig() {
		Properties p = new Properties();
		try {
			String configFilename = System.getProperty(CKAN_CONFIG_PROPERTY);
			if (configFilename != null) {
				LOGGER.debug("Loading CKAN configuration from system property config [" + configFilename + "]");
				try (InputStream source = new FileInputStream(configFilename)) {
					p.load(source);
				}
			} else {
				LOGGER.debug("Loading CKAN configuration from classpath config [" + CKAN_CONFIG_FILE + "]");
				try (InputStream source = CKANConfig.class.getResourceAsStream("/" + CKAN_CONFIG_FILE)) {
					p.load(source);
				}
			}
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Cannot load CKAN configuration", e);
		}
		return p;
	}

	public static CKANConfig getInstance() {
		if (instance == null) {
			instance = new CKANConfig();
		}
		return instance;
	}

	public Properties getConfig() {
		return config;
	}
}