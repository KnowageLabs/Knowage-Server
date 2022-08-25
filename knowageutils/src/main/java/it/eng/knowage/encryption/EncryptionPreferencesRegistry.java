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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marco Libanori
 *
 */
public class EncryptionPreferencesRegistry {

	private static EncryptionPreferencesRegistry INSTANCE = new EncryptionPreferencesRegistry();

	public static String DEFAULT_CFG_KEY = "DEFAULT";

	public static EncryptionPreferencesRegistry getInstance() {
		return INSTANCE;
	}

	private final Map<String, EncryptionConfiguration> registry = new HashMap<>();

	private EncryptionPreferencesRegistry() {
		super();
	}

	public void addConfiguration(String key, EncryptionConfiguration cfg) {
		registry.put(key, cfg);
	}

	public EncryptionConfiguration getConfiguration(String key) {
		return registry.get(key);
	}

}
