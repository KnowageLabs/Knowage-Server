/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.utilities.GeneralUtilities;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Imlements utilities for export purposes
 */
public class ExportUtilities {

    static private Logger logger = Logger.getLogger(ExportUtilities.class);
    
	/**
	 * Copy the metadata script of the exported database into the export folder.
	 * 
	 * @param pathDBFolder Path of the export database folder
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public static void copyMetadataScript(String pathDBFolder) throws EMFUserError {
	    logger.debug("IN");
		FileOutputStream fos = null;
		InputStream ismetadata = null;
		try {
	        Thread curThread = Thread.currentThread();
	        ClassLoader classLoad = curThread.getContextClassLoader();
	        String resource = "it/eng/spagobi/tools/importexport/metadata/exportdb/metadata.script";
	        ismetadata = classLoad.getResourceAsStream(resource);
	        String pathDBFile = pathDBFolder + "/metadata.script";
	        fos = new FileOutputStream(pathDBFile);
	        int read = 0;
	        while( (read = ismetadata.read()) != -1) {
	        	fos.write(read);
	        }
	        fos.flush();
        } catch (Exception e) {
        	logger.error("Error during the copy of the metadata exportdatabase script " , e);
        	throw new EMFUserError(EMFErrorSeverity.ERROR, "100", ImportManager.messageBundle);
        } finally {
        	try{
	        	if(fos!=null){
	        		fos.close();
	        	}
	        	if(ismetadata!=null) {
	        		ismetadata.close();
	        	}
        	} catch (Exception e) {
        	    logger.error("Error while closing streams " , e);
        	}
        	logger.debug("OUT");
        }
	}
	
	
	
	/**
	 * Copy the properties file of the exported database into the export folder.
	 * 
	 * @param pathDBFolder Path of the export database folder
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public static void copyMetadataScriptProperties(String pathDBFolder) throws EMFUserError {
	    logger.debug("IN");
		FileOutputStream fos = null;
		InputStream ismetadata = null;
		try {
	        Thread curThread = Thread.currentThread();
	        ClassLoader classLoad = curThread.getContextClassLoader();
	        String resource = "it/eng/spagobi/tools/importexport/metadata/exportdb/metadata.properties";
	        ismetadata = classLoad.getResourceAsStream(resource);
	        String pathDBFile = pathDBFolder + "/metadata.properties";
	        fos = new FileOutputStream(pathDBFile);
	        int read = 0;
	        while( (read = ismetadata.read()) != -1) {
	        	fos.write(read);
	        }
	        fos.flush();
        } catch (Exception e) {
            logger.error("Error during the copy of the metadata exportdatabase properties " , e);
        	throw new EMFUserError(EMFErrorSeverity.ERROR, "100", ImportManager.messageBundle);
        } finally {
        	try{
	        	if(fos!=null){
	        		fos.close();
	        	}
	        	if(ismetadata!=null) {
	        		ismetadata.close();
	        	}
        	} catch (Exception e) {
        	    logger.error("Error while closing streams " , e);
        	}
        	logger.debug("OUT");
        }
	}
	
	
	
	/**
	 * Creates an Hibernate session factory for the export database.
	 * 
	 * @param pathDBFolder Path of the export database folder
	 * 
	 * @return The Hibernate Session Factory
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public static SessionFactory getHibSessionExportDB(String pathDBFolder) throws EMFUserError {
	    logger.debug("IN");
		Configuration conf = new Configuration();
		String resource = "it/eng/spagobi/tools/importexport/metadata/hibernate.cfg.hsql.export.xml";
		conf = conf.configure(resource);
		String hsqlJdbcString = "jdbc:hsqldb:file:" + pathDBFolder + "/metadata;shutdown=true";
		conf.setProperty("hibernate.connection.url",hsqlJdbcString);
		SessionFactory sessionFactory = conf.buildSessionFactory();
		logger.debug("OUT");
		return sessionFactory;
	}
	
	
	/**
	 * Creates a sql connection for the exported database.
	 * 
	 * @param pathDBFolder  Path of the export database folder
	 * 
	 * @return Connection to the export database
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public static Connection getConnectionExportDB(String pathDBFolder) throws EMFUserError {
	    logger.debug("IN");
		Connection sqlconn = null;
		try {
			String driverName = "org.hsqldb.jdbcDriver";
			Class.forName(driverName);
	        String url = "jdbc:hsqldb:file:" + pathDBFolder + "/metadata;shutdown=true";
	        String username = "sa";
	        String password = "";
	         sqlconn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
        	logger.error("Error while getting connection to export database " , e);
        	throw new EMFUserError(EMFErrorSeverity.ERROR, "100", ImportManager.messageBundle);
        }finally{
            logger.debug("OUT");
        }
        return sqlconn;
	}
	
	public static String getExportTempFolderPath() {
		logger.debug("IN");
		String toReturn = null;
		try {
		    ConfigSingleton conf = ConfigSingleton.getInstance();
		    SourceBean importerSB = (SourceBean) conf.getAttribute("IMPORTEXPORT.EXPORTER");
		    toReturn = (String) importerSB.getAttribute("exportFolder");
		    toReturn = GeneralUtilities.checkForSystemProperty(toReturn);
		    if (!toReturn.startsWith("/") && toReturn.charAt(1) != ':') {
		    	String root = ConfigSingleton.getRootPath();
		    	toReturn = root + "/" + toReturn;
		    }
		} catch (Exception e) {
			logger.error("Error while retrieving export temporary folder path", e);
		} finally {
			logger.debug("OUT: export temporary folder path = " + toReturn);
		}
		return toReturn;
	}
	
	
	public static IExportManager getExportManagerInstance() throws Exception {
		logger.debug("IN");
		IExportManager toReturn = null;
		try {
		    ConfigSingleton conf = ConfigSingleton.getInstance();
		    SourceBean exporterSB = (SourceBean) conf.getAttribute("IMPORTEXPORT.EXPORTER");
		    String expClassName = (String) exporterSB.getAttribute("class");
		    Class expClass = Class.forName(expClassName);
		    toReturn = (IExportManager) expClass.newInstance();
		} catch (Exception e) {
			logger.error("Error while instantiating export manager", e);
			throw e;
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}
	
	/**
	 * Creates a Spago DataConnection object starting from a connection to the export database
	 * @param con Connection to the export database
	 * @return The Spago DataConnection Object
	 * @throws EMFInternalError
	 */
	/*
	public static DataConnection getDataConnection(Connection con) throws EMFInternalError {
	    logger.debug("IN");
		DataConnection dataCon = null;
		try {
			Class mapperClass = Class.forName("it.eng.spago.dbaccess.sql.mappers.OracleSQLMapper");
			SQLMapper sqlMapper = (SQLMapper)mapperClass.newInstance();
			dataCon = new DataConnection(con, "2.1", sqlMapper);
		} catch(Exception e) {
			logger.error("Error while getting Spago  DataConnection " , e);
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "cannot build DataConnection object");
		}finally{
		    logger.debug("OUT");
		}
		return dataCon;
	}
	*/
	

}
