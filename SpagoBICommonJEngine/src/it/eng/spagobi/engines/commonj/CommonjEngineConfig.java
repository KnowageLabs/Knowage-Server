/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.commonj;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.assertion.Assert;

import java.io.File;

import org.apache.log4j.Logger;


public class CommonjEngineConfig {

	private EnginConf engineConfig;

	private static CommonjEngineConfig instance;

	public static String COMMONJ_REPOSITORY_ROOT_DIR = "commonjRepository_root_dir";

	private static transient Logger logger = Logger.getLogger(CommonjEngineConfig.class);


	public static CommonjEngineConfig getInstance() {
		if(instance == null) {
			instance =  new CommonjEngineConfig();
		}

		return instance;
	}


	private CommonjEngineConfig() {
		setEngineConfig( EnginConf.getInstance() );
	}

	public SourceBean getConfigSourceBean() {
		return getEngineConfig().getConfig();
	}


	// core settings

	/**
	 * Checks if is absolute path.
	 * 
	 * @param path the path
	 * 
	 * @return true, if is absolute path
	 */
	public static boolean isAbsolutePath(String path) {
		if(path == null) return false;
		return (path.startsWith("/") || path.startsWith("\\") || path.charAt(1) == ':');
	}


	/**
	 * Gets the runtime repository root dir.
	 * 
	 * @return the runtime repository root dir
	 */
	public File getWorksRepositoryRootDir() {
		logger.debug("IN");	    
		String property = getProperty( COMMONJ_REPOSITORY_ROOT_DIR);

		SourceBean config = EnginConf.getInstance().getConfig();

		File dir = null;
		if( !isAbsolutePath(property) )  {
			property = getEngineResourcePath() + System.getProperty("file.separator") + property;
		}		

		if(property != null) dir = new File(property);
		logger.debug("OUT");	
		return dir;
	}



	// engine settings

	public String getEngineResourcePath() {
		String path = null;
		if(getEngineConfig().getResourcePath() != null) {
			path = getEngineConfig().getResourcePath() + System.getProperty("file.separator") + "commonj";
		} else {
			path = ConfigSingleton.getRootPath() + System.getProperty("file.separator") + "resources" + System.getProperty("file.separator") + "commonj";
		}

		return path;
	}




	// utils 

	private String getProperty(String propertName) {
		String propertyValue = null;		
		SourceBean sourceBeanConf;

		Assert.assertNotNull( getConfigSourceBean(), "Impossible to parse engine-config.xml file");

		sourceBeanConf = (SourceBean) getConfigSourceBean().getAttribute( propertName);
		if(sourceBeanConf != null) {
			propertyValue  = (String) sourceBeanConf.getCharacters();
			logger.debug("Configuration attribute [" + propertName + "] is equals to: [" + propertyValue + "]");
		}

		return propertyValue;		
	}




	// java properties
	/**
	 * Gets the java install dir.
	 * 
	 * @return the java install dir
	 */
	public String getJavaInstallDir() {
		SourceBean config = EnginConf.getInstance().getConfig();
		String installDir= (String)config.getCharacters("java_install_dir");
		return installDir;
	}



//	/**
//	 * Gets the word separator.
//	 * 
//	 * @return the word separator
//	 */
//	public String getWordSeparator() {
//		SourceBean config = EnginConf.getInstance().getConfig();
//		String wordS= (String)config.getCharacters("wordSeparator");			
//		return wordS;
//	}


	public EnginConf getEngineConfig() {
		return engineConfig;
	}

	public void setEngineConfig(EnginConf engineConfig) {
		this.engineConfig = engineConfig;
	}
}
