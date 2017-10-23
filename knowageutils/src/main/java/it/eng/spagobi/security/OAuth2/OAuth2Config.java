/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.security.OAuth2;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class OAuth2Config {
	static final String CONFIG_FILE_PATH = "configs.properties";
	private static OAuth2Config instance = null;
	private static Properties configs = null;

	private OAuth2Config() {
		configs = loadConfigs();
	}

	private static Properties loadConfigs() {
		InputStream source = OAuth2Config.class.getResourceAsStream(CONFIG_FILE_PATH);
		Properties p = new Properties();
		try {
			p.load(source);
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Cannot load configuration from " + CONFIG_FILE_PATH + " file", e);
		}
		return p;
	}

	public static OAuth2Config getInstance() {
		if (instance == null) {
			instance = new OAuth2Config();
		}
		return instance;
	}

	public Properties getConfig() {
		return configs;
	}
}