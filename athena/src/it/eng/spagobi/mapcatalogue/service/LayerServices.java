package it.eng.spagobi.mapcatalogue.service;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

public class LayerServices {
	private SingletonConfig configSingleton;
	private String path;
	private String resourcePath;
	
	
	LayerServices(){
		setPath();
		saveFile();
	}
	
	public void setPath(){
		configSingleton = SingletonConfig.getInstance();
		path  = configSingleton.getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
		resourcePath= SpagoBIUtilities.readJndiResource(path);
	}
	
	public void saveFile(){
		
	}
}
