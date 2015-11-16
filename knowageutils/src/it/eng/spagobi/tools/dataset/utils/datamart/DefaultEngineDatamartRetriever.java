/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.utils.datamart;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.services.proxy.MetamodelServiceProxy;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;

/**
 * @authors Andrea Gioia
 * 			Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class DefaultEngineDatamartRetriever implements
		IQbeDataSetDatamartRetriever {
	
	private static transient Logger logger = Logger.getLogger(DefaultEngineDatamartRetriever.class);
	
	private MetamodelServiceProxy metamodelProxy = null;
	
	public DefaultEngineDatamartRetriever(MetamodelServiceProxy metamodelProxy) {
		this.metamodelProxy = metamodelProxy;
	}

    public String getResourcePath() {
    	String resPath = EnginConf.getInstance().getResourcePath();
		if (resPath == null) {
			throw new SpagoBIRuntimeException("Resource path not found!!!");
		}
		return resPath;
	}
	
	public File getDataMartDir() {
		File qbeDataMartDir;
		
		qbeDataMartDir = null;
		
		String baseDirStr = this.getResourcePath();
		File baseDir = new File(baseDirStr);																			
		String completePath = baseDir + File.separator + File.separator + "qbe"
				+ File.separator + "datamarts";
		qbeDataMartDir = new File(completePath);
		
		if (qbeDataMartDir.exists() && !qbeDataMartDir.isDirectory()) {
			throw new SpagoBIRuntimeException("Path [" + completePath + "] refers to a file.");
		}
		if (!qbeDataMartDir.exists()) {
			boolean created = qbeDataMartDir.mkdirs();
			if (!created) {
				throw new SpagoBIRuntimeException("Cannot create folder [" + completePath + "].");
			}
		}
		return qbeDataMartDir;		
	}
	
	public File retrieveDatamartFile(String metamodelName) {
		
		File metamodelJarFile;
		
		logger.debug("IN");
		
		metamodelJarFile = null;
		try {
			Assert.assertTrue(StringUtilities.isNotEmpty(metamodelName), "Input parameter [metamodelName] cannot be null");
			logger.debug("Load metamodel jar file for model [" + metamodelName + "]");
			
			File targetMetamodelFolder = new File(getDataMartDir(), metamodelName);		
			metamodelJarFile = new File(targetMetamodelFolder, "datamart.jar");
			
			if (metamodelJarFile.exists()) {
				logger.debug("jar file for metamodel [" + metamodelName + "] has been already loaded in folder [" + targetMetamodelFolder + "]");
				long localVersionLastModified = metamodelJarFile.lastModified();
				long remoteVersionLastModified = this.metamodelProxy.getMetamodelContentLastModified(metamodelName);
				if(localVersionLastModified < remoteVersionLastModified) {
					downloadJarFile(metamodelName, targetMetamodelFolder);
				}
			} else {
				logger.debug("jar file for metamodel [" + metamodelName + "] has not been already downloaded");
				downloadJarFile(metamodelName, targetMetamodelFolder);
			}
			
			Assert.assertTrue(metamodelJarFile.exists(), "After load opertion file [" + metamodelJarFile + "] must exist");
		} catch (Throwable t) {
			if(t instanceof SpagoBIEngineRuntimeException) throw (SpagoBIEngineRuntimeException)t;
			throw new SpagoBIEngineRuntimeException("An unexpected error occured while loading metamodel's jar file", t);
		}
		
		return metamodelJarFile;
	}
	
	
	/**
	 * Download the jarFile from SpagoBI server and store it on the local filesystem in the
	 * specified folder
	 * 
	 * @param metamodelName the name of the metamodel to download
	 * @param destinationFolder the destination folder on the local filesystem
	 */
	private void downloadJarFile(String metamodelName, File destinationFolder) {
		DataHandler handler = null;
		try {
			logger.debug("Loading jar file for metamodel [" + metamodelName + "] from SpagoBI server...");
			handler = this.metamodelProxy.getMetamodelContentByName(metamodelName);
			if(handler == null) throw new SpagoBIEngineRuntimeException("Metamodel Service returns null value");
			logger.debug("jar file for metamodel [" + metamodelName + "] has been loaded succesfully from SpagoBI server");
		} catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load jar file of metamodel [" + metamodelName + "] from SpagoBiServer", t);
		}
		
		logger.debug("Copying jar file of metamodel [" + metamodelName + "] locally into folder [" + destinationFolder + "] ...");
		storeJarFile(handler, destinationFolder);
		logger.debug("jar file of metamodel [" + metamodelName + "] succesfully copied locally into folder [" + destinationFolder + "] ...");
	}
	
	/**
	 * Store the jarFile on local filesystem
	 * 
	 * @param dataHandler the jarFile content
	 * @param destinationFolder the destination folder on the local filesystem
	 */
	private void storeJarFile(DataHandler dataHandler, File destinationFolder) {
		
		File metamodelJarFile = new File(destinationFolder, "datamart.jar");
		
		if(metamodelJarFile.exists()) {
			metamodelJarFile.delete();
		}
		
		if(!destinationFolder.exists()) destinationFolder.mkdirs();
		
		FileOutputStream fos = null;
		InputStream is = null;
		try {
			fos = new FileOutputStream(metamodelJarFile);
		       is = dataHandler.getInputStream();
			int c = 0;
			byte[] b = new byte[1024];
			while ((c = is.read(b)) != -1) {
				if (c == 1024)
					fos.write(b);
				else
					fos.write(b, 0, c);
			}
		       fos.flush();
		} catch (Throwable t) {
			throw new SpagoBIEngineRuntimeException("An unexpected error occured while saving localy metamodel's jar file", t);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				logger.error("Error while closing DataHandler input stream", e);
			}
			try {
				fos.close();
			} catch (IOException e) {
				logger.error("Error while closing file output stream", e);
			}
		}
	}
	
	public boolean isAJPADatamartJarFile(File metamodelJarFile) {
		ZipFile zipFile;
		ZipEntry zipEntry;
		
		try {
			zipFile = new ZipFile(metamodelJarFile);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to read jar file [" + metamodelJarFile + "]", t);
		} 
		
		zipEntry = zipFile.getEntry("META-INF/persistence.xml");
		
		return zipEntry!=null;
	}
	
}
