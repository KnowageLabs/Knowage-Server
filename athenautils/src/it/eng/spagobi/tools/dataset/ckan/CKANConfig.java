/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.ckan;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CKANConfig {

	static final String CONFIG_FILE_PATH = "ckan.config.properties";
	private static CKANConfig instance = null;
	private static Properties configs = null;

	private CKANConfig() {
		configs = loadConfigs();
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

	public static CKANConfig getInstance() {
		if (instance == null) {
			instance = new CKANConfig();
		}
		return instance;
	}

	public Properties getConfig() {
		return configs;
	}
}