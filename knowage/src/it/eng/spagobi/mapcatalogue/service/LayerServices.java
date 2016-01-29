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
