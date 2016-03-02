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
package it.eng.spagobi.mapcatalogue.service;

import org.apache.commons.codec.binary.Base64;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

public class LayerServices {
	private SingletonConfig configSingleton;
	private String path;
	private String resourcePath;

	LayerServices() {
		setPath();

	}

	public void setPath() {
		configSingleton = SingletonConfig.getInstance();
		path = configSingleton.getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
		resourcePath = SpagoBIUtilities.readJndiResource(path);
	}

	public String getResourcePath(byte[] data) {
		return resourcePath;
	}

	public byte[] decode64(byte[] data) {
		byte[] result = new byte[0];
		try {
			result = Base64.decodeBase64(data);
		} catch (Exception e) {

		}
		return result;
	}
}
