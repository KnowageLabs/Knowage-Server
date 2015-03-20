/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.file.FileUtils;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia
 *
 */
public class TalendEngineConfig {
	
	private EnginConf engineConfig;
	
	private static TalendEngineConfig instance;
	
	public static String SPAGOBI_SERVER_URL = "SPAGOBI_SERVER_URL";
	public static String DEFAULT_SPAGOBI_SERVER_URL = "http://localhost:8080/SpagoBI";
	
	public static String RUNTIMEREPOSITORY_ROOT_DIR = "RUNTIMEREPOSITORY_ROOT_DIR";
	
	private static transient Logger logger = Logger.getLogger(TalendEngineConfig.class);

	
	public static TalendEngineConfig getInstance() {
		if(instance == null) {
			instance =  new TalendEngineConfig();
		}
		
		return instance;
	}
	
	private TalendEngineConfig() {
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
	public File getRuntimeRepositoryRootDir() {
	    
		String property = getProperty( RUNTIMEREPOSITORY_ROOT_DIR );
		
		SourceBean config = EnginConf.getInstance().getConfig();
	        
	        File dir = null;
		if( !isAbsolutePath(property) )  {
			property = getEngineResourcePath() + System.getProperty("file.separator") + property;
		}		
		
		if(property != null) dir = new File(property);
		
		return dir;
	}
	
	
	
	// engine settings
	
	public String getEngineResourcePath() {
		String path = null;
		if(getEngineConfig().getResourcePath() != null) {
			path = getEngineConfig().getResourcePath() + System.getProperty("file.separator") + "talend";
		} else {
			path = ConfigSingleton.getRootPath() + System.getProperty("file.separator") + "resources" + System.getProperty("file.separator") + "talend";
		}
		
		return path;
	}
	
	public String getSpagoBIServerUrl() {
		
		String spagoBIServerURL = null;
		SourceBean sourceBeanConf;
		
		Assert.assertNotNull( getConfigSourceBean(), "Impossible to parse engine-config.xml file");
		
		sourceBeanConf = (SourceBean) getConfigSourceBean().getAttribute(SPAGOBI_SERVER_URL);
		if(sourceBeanConf != null) {
			spagoBIServerURL = (String) sourceBeanConf.getCharacters();
			logger.debug("Configuration attribute [" + SPAGOBI_SERVER_URL + "] is equals to: [" + spagoBIServerURL + "]");
		}
		
		if (spagoBIServerURL == null) {
			logger.warn("Configuration attribute [" + SPAGOBI_SERVER_URL + "] is not defined in file engine-config.xml");
			spagoBIServerURL = DEFAULT_SPAGOBI_SERVER_URL;
			logger.debug("The default value [" + DEFAULT_SPAGOBI_SERVER_URL +"] will be used for configuration attribute [" + SPAGOBI_SERVER_URL + "]");
		} 
		
		return spagoBIServerURL;
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
	
	
	
	
	
	
	

	
	
	
	
	/**
	 * Gets the spagobi target functionality label.
	 * 
	 * @return the spagobi target functionality label
	 */
	public String getSpagobiTargetFunctionalityLabel() {
		String label = null;
        SourceBean config = EnginConf.getInstance().getConfig();
        label= (String)config.getCharacters("spagobi_functionality_label");
		return label;
	}
	
	/**
	 * Gets the spagobi url.
	 * 
	 * @return the spagobi url
	 */
	public String getSpagobiUrl() {
		/*
		String url = null;
        SourceBean config = EnginConf.getInstance().getConfig();
        url= (String)config.getCharacters("spagobi_context_path");
		return url;
		*/
		return getSpagoBIServerUrl();
	}
	
	/**
	 * Checks if is auto publish active.
	 * 
	 * @return true, if is auto publish active
	 */
	public boolean isAutoPublishActive() {
		String autoPublishProp = null;
        SourceBean config = EnginConf.getInstance().getConfig();
        autoPublishProp= (String)config.getCharacters("spagobi_autopublish");
        if(autoPublishProp != null && autoPublishProp.equalsIgnoreCase("true")) return true;
		return false;
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
        logger.info("java_install_dir:"+installDir);
		return installDir;
	}
	
	/**
	 * Gets the java bin dir.
	 * 
	 * @return the java bin dir
	 */
	public String getJavaBinDir() {
        SourceBean config = EnginConf.getInstance().getConfig();
        String binDir= (String)config.getCharacters("java_bin_dir");
        logger.info("java_bin_dir:"+binDir);
		return binDir;
	}
	
	/**
	 * Gets the java command.
	 * 
	 * @return the java command
	 */
	public String getJavaCommand() {
        SourceBean config = EnginConf.getInstance().getConfig();
        SourceBean sbTmp = (SourceBean)config.getAttribute("java_command");
        String command = (String) sbTmp.getCharacters();
        //String command= (String)config.getCharacters("java_command");
        logger.info("java_command:"+command);
		return command;
	}
	
	/**
	 * Gets the java command option.
	 * 
	 * @param optionName the option name
	 * 
	 * @return the java command option
	 */
	public String getJavaCommandOption(String optionName) {
        SourceBean config = EnginConf.getInstance().getConfig();
        String commandOption= (String)config.getCharacters("java_command_option_"+optionName);	
        logger.info("java_command_option_:"+commandOption);
        return commandOption;
	}
	
	// perl properties
	/**
	 * Gets the job separator.
	 * 
	 * @return the job separator
	 */
	public String getJobSeparator() {
        SourceBean config = EnginConf.getInstance().getConfig();
        String jobSeparator= (String)config.getCharacters("jobSeparator");
		return jobSeparator;
	}

	/**
	 * Gets the perl bin dir.
	 * 
	 * @return the perl bin dir
	 */
	public String getPerlBinDir() {
        SourceBean config = EnginConf.getInstance().getConfig();
        String binDir= (String)config.getCharacters("perl_bin_dir");
		return binDir;
	}

	/**
	 * Gets the perl command.
	 * 
	 * @return the perl command
	 */
	public String getPerlCommand() {
        SourceBean config = EnginConf.getInstance().getConfig();
        String command= (String)config.getCharacters("perl_command");
		return command;
	}

	/**
	 * Gets the perl ext.
	 * 
	 * @return the perl ext
	 */
	public String getPerlExt() {
        SourceBean config = EnginConf.getInstance().getConfig();
        String ext= (String)config.getCharacters("perlExt");
		return ext;
	}

	/**
	 * Gets the perl install dir.
	 * 
	 * @return the perl install dir
	 */
	public String getPerlInstallDir() {
        SourceBean config = EnginConf.getInstance().getConfig();
        String install= (String)config.getCharacters("perl_install_dir");	
		return install;
	}

	/**
	 * Gets the word separator.
	 * 
	 * @return the word separator
	 */
	public String getWordSeparator() {
        SourceBean config = EnginConf.getInstance().getConfig();
        String wordS= (String)config.getCharacters("wordSeparator");			
		return wordS;
	}


	public EnginConf getEngineConfig() {
		return engineConfig;
	}

	public void setEngineConfig(EnginConf engineConfig) {
		this.engineConfig = engineConfig;
	}
}
