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
package it.eng.spagobi.sdk.importexport.impl;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.sdk.AbstractSDKService;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.importexport.ImportExportSDKService;
import it.eng.spagobi.sdk.importexport.bo.SDKFile;

import it.eng.spagobi.tools.importexportOLD.ImportResultInfo;
import it.eng.spagobi.tools.importexportOLD.ImportUtilities;
import it.eng.spagobi.tools.importexportOLD.MetadataAssociations;
import it.eng.spagobi.tools.importexportOLD.TransformManager;
import it.eng.spagobi.tools.importexportOLD.IImportManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.log4j.Logger;

public class ImportExportSDKServiceImpl extends AbstractSDKService implements ImportExportSDKService {

	static private Logger logger = Logger.getLogger(ImportExportSDKServiceImpl.class);

	public SDKFile importDocuments(SDKFile importExportFile,
			SDKFile associationsFile, boolean overwrite) throws NotAllowedOperationException {
		SDKFile toReturn = null;
		logger.debug("IN");
		
		this.setTenant();
		
        try {
            super.checkUserPermissionForFunctionality(SpagoBIConstants.IMPORT_EXPORT_MANAGEMENT, "User cannot use the import / export functionality.");
            toReturn = this.internalImportDocuments(importExportFile, associationsFile, overwrite);
        } catch(NotAllowedOperationException e) {
        	throw e;
        } catch(Throwable t) {
            logger.error("Error during service execution", t);
            logger.debug("Returning null");
            return null;
        } finally {
        	this.unsetTenant();
        	logger.debug("OUT");
        }
        return toReturn;
	}
	
	public SDKFile internalImportDocuments(SDKFile importExportSDKFile,
			SDKFile associationsSDKFile, boolean overwrite) throws NotAllowedOperationException {
		logger.debug("IN");
		SDKFile toReturn = null;
		IImportManager importManager = null;
        try {
        	String archiveName = importExportSDKFile.getFileName();
        	
        	byte[] importExportFileContent = this.getFileContent(importExportSDKFile);
        	byte[] associationsFileContent = this.getFileContent(associationsSDKFile);
        	IEngUserProfile profile = this.getUserProfile();
        	
    	    String pathImpTmpFolder = ImportUtilities.getImportTempFolderPath();
    		   
    	    // apply transformation
    	    TransformManager transManager = new TransformManager();
    	    byte[] archiveBytes = transManager.applyTransformations(importExportFileContent, archiveName, pathImpTmpFolder);
    	    logger.debug("Transformation applied succesfully");
    	    
    	    importManager = ImportUtilities.getImportManagerInstance();
    	    // set into import manager the association import mode
    		importManager.setImpAssMode(IImportManager.IMPORT_ASS_PREDEFINED_MODE);
    	    importManager.setUserProfile(profile);
    	    importManager.init(pathImpTmpFolder, archiveName, archiveBytes);
    	    importManager.openSession();
    	    
			// if the associations file has been uploaded fill the association keeper
			if (associationsFileContent != null) {
				String assFileStr = new String(associationsFileContent);
				importManager.getUserAssociation().fillFromXml(assFileStr);
			}
			
			MetadataAssociations metadataAssociations = importManager.getMetadataAssociation();
			
		    // check role associations
			importManager.checkRoleReferences(metadataAssociations.getRoleIDAssociation());
			
			importManager.checkExistingMetadata();
			
			importManager.importObjects(overwrite);
		    ImportResultInfo iri = importManager.commitAllChanges();
		    
    		String logFileName = iri.getLogFileName();
    		String folderName = iri.getFolderName();
    		String importBasePath = ImportUtilities.getImportTempFolderPath();
    		String folderPath = importBasePath + "/" + folderName;
    		String fileExtension = "log";
    		String completeFileName = logFileName + "." + fileExtension;
    		File exportedFile = new File(folderPath + "/" + completeFileName);
    		FileDataSource fileDataSource = new FileDataSource(exportedFile);
    		DataHandler dataHandler = new DataHandler(fileDataSource);			
    		toReturn = new SDKFile();
    		toReturn.setFileName(completeFileName);
    		toReturn.setContent(dataHandler);
    		
        } catch(NotAllowedOperationException e) {
        	throw e;
        } catch (Exception e) {
        	if (importManager != null) {
        		importManager.stopImport();
        	}
        	throw new SpagoBIRuntimeException("Error while importing documents", e);
        } finally {
        	logger.debug("OUT");
        	if (importManager != null) {
        		importManager.closeSession();
        	}
        }
        return toReturn;
	}

	private byte[] getFileContent(SDKFile importExportSDKFile) throws NotAllowedOperationException {
		byte[] toReturn = null;
		logger.debug("IN");
		int maxSize = ImportUtilities.getImportFileMaxSize();
		logger.debug("Import/export file max size: " + maxSize);
		if (importExportSDKFile != null) {
			InputStream is = null;
			try {
				DataHandler dh = importExportSDKFile.getContent();
				is = dh.getInputStream();
				try {
					toReturn = SpagoBIUtilities.getByteArrayFromInputStream(is, maxSize);
				} catch (SecurityException e) {
					logger.error("A security exception was thrown when getting import/export file content: file is too big", e);
		    		NotAllowedOperationException ex = new NotAllowedOperationException();
		    		ex.setFaultString("File in input is too big");
		    		throw ex;
				}
	        } catch (NotAllowedOperationException e) {
	        	throw e;
			} catch (Exception e) {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ioe) {
						logger.error("Error closing input stream of attachment", ioe);
					}
				}
				throw new SpagoBIRuntimeException("Error while getting SDK file content", e);
			} finally {
				logger.debug("OUT");
			}
		}
		return toReturn;
	}

}
