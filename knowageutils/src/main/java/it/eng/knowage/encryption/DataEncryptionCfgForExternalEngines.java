/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.

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
package it.eng.knowage.encryption;

public class DataEncryptionCfgForExternalEngines {

	private static final String KEY_TEMPLATE_FOR_ALGORITHM = "engine.encryption.%s.algorithm";
	private static final String KEY_TEMPLATE_FOR_PASSWORD = "engine.encryption.%s.password";
	private static final DataEncryptionCfgForExternalEngines INSTANCE = new DataEncryptionCfgForExternalEngines();

	public static DataEncryptionCfgForExternalEngines getInstance() {
		return INSTANCE;
	}

	private DataEncryptionCfgForExternalEngines() {
		super();
	}

	public void setKeyTemplateForAlgorithm(String cfgKey, String algorithm) {
		String key = createKey(KEY_TEMPLATE_FOR_ALGORITHM, cfgKey);
		System.setProperty(key, algorithm);
	}

	public void setKeyTemplateForPassword(String cfgKey, String password) {
		String key = createKey(KEY_TEMPLATE_FOR_PASSWORD, cfgKey);
		System.setProperty(key, password);
	}

	public String getKeyTemplateForAlgorithm(String cfgKey) {
		String key = createKey(KEY_TEMPLATE_FOR_ALGORITHM, cfgKey);
		return System.getProperty(key);
	}

	public String getKeyTemplateForPassword(String cfgKey) {
		String key = createKey(KEY_TEMPLATE_FOR_PASSWORD, cfgKey);
		return System.getProperty(key);
	}

	private String createKey(String template, String cfgKey) {
		return String.format(template, cfgKey);
	}
}
