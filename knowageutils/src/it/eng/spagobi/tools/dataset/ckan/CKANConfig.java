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

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CKANConfig {

	static final String CONFIG_FILE_PATH = "ckan.config.properties";
	static final String URL_FILE_PATH = "ckan.url.properties";
	private static CKANConfig instance = null;
	private static Properties configs = null;
	private static Properties urls = null;

	private CKANConfig() {
		configs = loadConfigs();
		urls = loadUrls();
	}

	private static Properties loadConfigs() {
		InputStream source = CKANConfig.class.getResourceAsStream("/" + CONFIG_FILE_PATH);
		Properties p = new Properties();
		try {
			p.load(source);
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Cannot load configuration from " + CONFIG_FILE_PATH + " file", e);
		}
		return p;
	}

	private static Properties loadUrls() {
		InputStream source = CKANConfig.class.getResourceAsStream("/" + URL_FILE_PATH);
		Properties p = new Properties();
		try {
			p.load(source);
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Cannot load URLs from " + URL_FILE_PATH + " file", e);
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
		return configs;
	}

	public Properties getUrl() {
		return urls;
	}
}