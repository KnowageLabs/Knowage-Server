/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.birt;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.engines.birt.utilities.Utils;
import it.eng.spagobi.utilities.ResourceClassLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.IPlatformContext;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.core.framework.PlatformServletContext;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.utility.ParameterAccessor;
import org.xml.sax.InputSource;

public class BirtEngine {

    private static IReportEngine birtEngine = null;

    private static Properties configProps = new Properties();

    private final static String configFile = "BirtLogConfig.properties";

    protected static Logger logger = Logger.getLogger(BirtEngine.class);
    
    protected final static String engineConfigFile = "engine-config.xml";

    /**
     * Inits the birt config.
     */
    public static synchronized void initBirtConfig() {
	loadEngineProps();
    }

    /**
     * Gets the birt engine.
     * 
     * @param request the request
     * @param sc the sc
     * 
     * @return the birt engine
     */
    public static synchronized IReportEngine getBirtEngine(HttpServletRequest request, ServletContext sc) {
	logger.debug("IN");
	if (birtEngine == null) {
	    EngineConfig config = new EngineConfig();
	    if (configProps != null && !configProps.isEmpty()) {
		String logLevel = configProps.getProperty("logLevel");
		Level level = Level.OFF;
		if ("SEVERE".equalsIgnoreCase(logLevel)) {
		    level = Level.SEVERE;
		} else if ("WARNING".equalsIgnoreCase(logLevel)) {
		    level = Level.WARNING;
		} else if ("INFO".equalsIgnoreCase(logLevel)) {
		    level = Level.INFO;
		} else if ("CONFIG".equalsIgnoreCase(logLevel)) {
		    level = Level.CONFIG;
		} else if ("FINE".equalsIgnoreCase(logLevel)) {
		    level = Level.FINE;
		} else if ("FINER".equalsIgnoreCase(logLevel)) {
		    level = Level.FINER;
		} else if ("FINEST".equalsIgnoreCase(logLevel)) {
		    level = Level.FINEST;
		} else if ("ALL".equalsIgnoreCase(logLevel)) {
			level = Level.ALL;
		} else if ("OFF".equalsIgnoreCase(logLevel)) {
		    level = Level.OFF;
		}

		String logDir = configProps.getProperty("logDirectory");
		logDir = Utils.resolveSystemProperties(logDir);
		logger.debug("Birt LOG Dir:"+logDir);
		logger.debug("Log config: logDirectory = [" + logDir + "]; level = [" + level + "]");
		config.setLogConfig(logDir, level);
	    }

	    /*DefaultResourceLocator drl=new DefaultResourceLocator();
	    drl.findResource(birtEngine.openReportDesign(arg0), "messages_it_IT.properties", DefaultResourceLocator.MESSAGE_FILE);
	    */
	    //Commented for Birt 3.7 Upgrade see: http://wiki.eclipse.org/Birt_3.7_Migration_Guide#BIRT_3.7_API_Changes
	    //config.setEngineHome("");
	    IPlatformContext context = new PlatformServletContext(sc);
	    config.setPlatformContext(context);
	    config.setTempDir(System.getProperty("java.io.tmpdir") + "/birt/");

			
	   // ParameterAccessor.initParameters(sc);
	    //config.setResourcePath(ParameterAccessor.getResourceFolder(request));
	    // Prepare ScriptLib location
	    String scriptLibDir = ParameterAccessor.scriptLibDir;
	    ArrayList jarFileList = new ArrayList();
	    if (scriptLibDir != null) {
		File dir = new File(scriptLibDir);
		getAllJarFiles(dir, jarFileList);
	    }
	    String scriptlibClassPath = ""; //$NON-NLS-1$
	    for (int i = 0; i < jarFileList.size(); i++)
		scriptlibClassPath += EngineConstants.PROPERTYSEPARATOR + ((File) jarFileList.get(i)).getAbsolutePath();
	    if (scriptlibClassPath.startsWith(EngineConstants.PROPERTYSEPARATOR))
		scriptlibClassPath = scriptlibClassPath.substring(EngineConstants.PROPERTYSEPARATOR.length());
	    config.setProperty(EngineConstants.WEBAPP_CLASSPATH_KEY, scriptlibClassPath);

	    try {
		Platform.startup(config);
		logger.debug("Birt Platform started");
	    } catch (BirtException e) {
		logger.error("Error during Birt Platform start-up", e);
	    }

	    IReportEngineFactory factory = (IReportEngineFactory) Platform
		    .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
	    birtEngine = factory.createReportEngine(config);
	    logger.debug("Report engine created");

	}
	return birtEngine;
    }

    /**
     * Destroy birt engine.
     */
    public static synchronized void destroyBirtEngine() {
	if (birtEngine == null) {
	    return;
	}
	// birtEngine.shutdown();
	Platform.shutdown();
	birtEngine = null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }

    private static void loadEngineProps() {
	InputStream in = null;
	try {
	    // Config File must be in classpath
	    ClassLoader cl = Thread.currentThread().getContextClassLoader();
	    in = cl.getResourceAsStream(configFile);
	    configProps.load(in);
	} catch (IOException e) {
	    logger.error("Error during configFile properties file [" + configFile + "]", e);
	} finally {
	    if (in != null)
		try {
		    in.close();
		} catch (IOException e) {
		    logger.error("Error during closing input stream", e);
		}
	}
    }

    private static void getAllJarFiles(File dir, ArrayList fileList) {
	if (dir.exists() && dir.isDirectory()) {
	    File[] files = dir.listFiles();
	    if (files == null)
		return;
	    for (int i = 0; i < files.length; i++) {
		File file = files[i];
		if (file.isFile()) {
		    if (file.getName().endsWith(".jar")) //$NON-NLS-1$
			fileList.add(file);
		} else if (file.isDirectory()) {
		    getAllJarFiles(file, fileList);
		}
	    }
	}
    }

}
