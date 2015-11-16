/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport.dao;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.tools.importexport.bo.AssociationFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

public class AssociationFileDAO implements IAssociationFileDAO {

    private final String ASS_DIRECTORY = "Repository_Association_Files";

    static private Logger logger = Logger.getLogger(AssociationFileDAO.class);

    /* (non-Javadoc)
     * @see it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO#loadFromID(java.lang.String)
     */
    public AssociationFile loadFromID(String id) {
	logger.debug("IN");
	AssociationFile assFile = null;
	try {
	    File fileAssRepDir = getFileOfAssRepDir();
	    String pathBaseDirAss = fileAssRepDir.getPath() + "/" + id;
	    String pathprop = pathBaseDirAss + "/association.properties";
	    FileInputStream fis = new FileInputStream(pathprop);
	    Properties props = new Properties();
	    props.load(fis);
	    fis.close();
	    assFile = new AssociationFile();
	    assFile.setName(props.getProperty("name"));
	    assFile.setDescription(props.getProperty("description"));
	    assFile.setId(props.getProperty("id"));
	    assFile.setDateCreation(new Long(props.getProperty("creationDate")).longValue());
	} catch (Exception e) {
	    logger.error("Error while loading association file with id " + id + ", ", e);
	    assFile = null;
	} finally {
	    logger.debug("OUT");
	}
	return assFile;
    }

    /* (non-Javadoc)
     * @see it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO#saveAssociationFile(it.eng.spagobi.tools.importexport.bo.AssociationFile, byte[])
     */
    public void saveAssociationFile(AssociationFile assfile, byte[] content) {
	logger.debug("IN");
	try {
	    String uuid = assfile.getId();
	    File fileAssRepDir = getFileOfAssRepDir();
	    String pathBaseAssFile = fileAssRepDir.getAbsolutePath() + "/" + uuid;
	    File baseAssFile = new File(pathBaseAssFile);
	    baseAssFile.mkdirs();
	    String pathXmlAssFile = pathBaseAssFile + "/association.xml";
	    FileOutputStream fos = new FileOutputStream(pathXmlAssFile);
	    ByteArrayInputStream bais = new ByteArrayInputStream(content);
	    GeneralUtilities.flushFromInputStreamToOutputStream(bais, fos, true);
	    String pathPropAssFile = pathBaseAssFile + "/association.properties";
	    String properties = "id=" + assfile.getId() + "\n";
	    properties += "name=" + assfile.getName() + "\n";
	    properties += "description=" + assfile.getDescription() + "\n";
	    properties += "creationDate=" + assfile.getDateCreation() + "\n";
	    fos = new FileOutputStream(pathPropAssFile);
	    fos.write(properties.getBytes());
	    fos.flush();
	    fos.close();
	} catch (Exception e) {
	    logger.error("Error while saving association file, ", e);
	} finally {
	    logger.debug("OUT");
	}
    }

	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO#exists(java.lang.String)
	 */
	public boolean exists(String id) {
	logger.debug("IN");
	try {
		File fileAssRepDir = getFileOfAssRepDir();
		String pathBaseAssFile = fileAssRepDir.getAbsolutePath() + "/" + id;
		File baseAssFile = new File(pathBaseAssFile);
		// if the folder exists the association file exists
		// if a file with the same name exists then tries to delete it
		if (baseAssFile.exists()) {
			if (baseAssFile.isDirectory()) return true;
			else {
				if (baseAssFile.delete()) return false;
				else return true;
			}
		}
		else return false;
	} finally {
	    logger.debug("OUT");
	}
	}
    
    /* (non-Javadoc)
     * @see it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO#deleteAssociationFile(it.eng.spagobi.tools.importexport.bo.AssociationFile)
     */
    public void deleteAssociationFile(AssociationFile assfile) {
	logger.debug("IN");
	try {
	    File fileAssRepDir = getFileOfAssRepDir();
	    String pathBaseDirAss = fileAssRepDir.getPath() + "/" + assfile.getId();
	    File fileBaseDirAss = new File(pathBaseDirAss);
	    GeneralUtilities.deleteDir(fileBaseDirAss);
	} catch (Exception e) {
	    logger.error("Error while deleting association file, ", e);
	} finally {
	    logger.debug("OUT");
	}
    }

    /* (non-Javadoc)
     * @see it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO#getAssociationFiles()
     */
    public List getAssociationFiles() {
	logger.debug("IN");
	List assFiles = new ArrayList();
	try {
	    File fileAssRepDir = getFileOfAssRepDir();
	    File[] assBaseDirs = fileAssRepDir.listFiles();
	    for (int i = 0; i < assBaseDirs.length; i++) {
		File assBaseDir = assBaseDirs[i];
		try {
		    if (assBaseDir.isDirectory()) {
			String pathprop = assBaseDir.getPath() + "/association.properties";
			FileInputStream fis = new FileInputStream(pathprop);
			Properties props = new Properties();
			props.load(fis);
			fis.close();
			AssociationFile assFile = new AssociationFile();
			assFile.setName(props.getProperty("name"));
			assFile.setDescription(props.getProperty("description"));
			assFile.setId(props.getProperty("id"));
			assFile.setDateCreation(new Long(props.getProperty("creationDate")).longValue());
			assFiles.add(assFile);
		    }
		} catch (Exception e) {
		    logger.error("Error while recovering info of the ass file with" + "id " + assBaseDir.getName()
			    + "\n , ", e);
		}
	    }
	} catch (Exception e) {
	    logger.error("Error while getting association file list, ", e);
	} finally {
	    logger.debug("OUT");
	}
	return assFiles;
    }

    /* (non-Javadoc)
     * @see it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO#getContent(it.eng.spagobi.tools.importexport.bo.AssociationFile)
     */
    public byte[] getContent(AssociationFile assfile) {
	logger.debug("IN");
	byte[] byts = new byte[0];
	try {
	    File fileAssRepDir = getFileOfAssRepDir();
	    String pathBaseDirAss = fileAssRepDir.getPath() + "/" + assfile.getId();
	    String pathAssFile = pathBaseDirAss + "/association.xml";
	    FileInputStream fis = new FileInputStream(pathAssFile);
	    byts = GeneralUtilities.getByteArrayFromInputStream(fis);
	} catch (Exception e) {
	    logger.error("Error while getting content of association file with id " + assfile.getId() + ",\n ", e);
	} finally {
	    logger.debug("OUT");
	}
	return byts;
    }

    private File getFileOfAssRepDir() {
	logger.debug("IN");
	File assrepdirFile = null;
	try {
		ConfigSingleton conf = ConfigSingleton.getInstance();
		SourceBean assRepo = (SourceBean)conf.getAttribute("IMPORTEXPORT.ASSOCIATIONS_REPOSITORY");
		String assRepoPath = (String)assRepo.getAttribute("path");
		if(!assRepoPath.startsWith("/")){
			String pathcont = ConfigSingleton.getRootPath();
			assRepoPath = pathcont + "/" + assRepoPath;
		}
		// check if the file already exists  and, if not, create the directory
		assrepdirFile = new File(assRepoPath);
		assrepdirFile.mkdirs();
	} catch (Exception e) {
	    logger.error("Error wile getting the associations repository dir file, ", e);
	} finally {
	    logger.debug("OUT");
	}
	return assrepdirFile;
    }

}
