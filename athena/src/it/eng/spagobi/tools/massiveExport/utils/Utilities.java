/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.massiveExport.utils;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class Utilities {

	private static Logger logger = Logger.getLogger(Utilities.class);

	public static final String RESOURCE_MASSIVE_EXPORT_FOLDER = "massiveExport";

	public static List getContainedObjFilteredbyType(LowFunctionality funct, String docType){
		logger.debug("IN");
		List objList = funct.getBiObjects();
		// filteronly selected type
		List<BIObject> selectedObjects = new ArrayList<BIObject>();
		if(docType == null){
			selectedObjects = objList;
		}
		else {
			for (Iterator iterator = objList.iterator(); iterator.hasNext();) {
				BIObject biObject = (BIObject) iterator.next();
				if(biObject.getBiObjectTypeCode().equals(docType)){
					selectedObjects.add(biObject);
				}

			}
		}
		logger.debug("OUT");
		return selectedObjects;
	}



	
	public static File getMassiveExportFolder(){
		logger.debug("IN");

		String resourcePath = "";
		String jndiBean = SingletonConfig.getInstance().getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
		if (jndiBean != null) {
			resourcePath = SpagoBIUtilities.readJndiResource(jndiBean);
		}

		File file = new File(resourcePath);
		if(!file.exists()){
			throw new SpagoBIRuntimeException("Could not find resource directory, searching in "+resourcePath, null);
		}

		if(!resourcePath.endsWith(File.separator)){
			resourcePath+=File.separator;
		}

		resourcePath+=Utilities.RESOURCE_MASSIVE_EXPORT_FOLDER;

		File directory = new File(resourcePath);
		if(!directory.exists()){
			directory.mkdir();
		}
		logger.debug("OUT");
		return directory;

	}


	public static File getMassiveExportZipFile(String folderName, String fileName) {
		
		File zipFolder = getMassiveExportZipFolder(folderName);
		
		File zipFile = new File(zipFolder, fileName+".zip");
		if(!(zipFile.exists())){
			throw new SpagoBIRuntimeException("not existing zip file " + zipFile);
		}

		return zipFile;
	}

	public static File getMassiveScheduleZipFile(String userIdentifier, String folderLabel, String zipFileName) {
		File zipFolder = getMassiveScheduleZipFolder(userIdentifier, folderLabel);
		File zipFile = new File(zipFolder, zipFileName + ".zip");
		if(!(zipFile.exists())){
			throw new SpagoBIRuntimeException("not existing zip file " + zipFile);
		}

		return zipFile;
	}

	public static File getMassiveExportZipFolder(String folderName) {
		
		File massiveExportFolder = getMassiveExportFolder();

		File zipDirectory = new File(massiveExportFolder, folderName);
		if(!zipDirectory.exists()){
			zipDirectory.mkdir();
		}

		return zipDirectory;	
	}
	
	public static File getMassiveScheduleZipFolder(String userIdentifier, String folderLabel) {
		File massiveExportFolder = Utilities.getMassiveExportFolder();
		File userFolder = new File(massiveExportFolder, userIdentifier);
		File destinationFolder = new File(userFolder, folderLabel);
		return destinationFolder;
	}
	

	public static String addSeparatorIfNeeded(String path){
		if(!path.endsWith(File.separator)){
			path+=File.separator;
		}
		return path;
	}


	

	public static File createMassiveExportZip(String functionalityCd, String randomKey) throws IOException {
		logger.debug("IN");
		
		File zipFolder = getMassiveExportZipFolder(functionalityCd);
		String zipFolderPath = addSeparatorIfNeeded(zipFolder.getAbsolutePath());
		String filePath = zipFolderPath+randomKey+".zip";

		File zip = new File(filePath);
		zip.createNewFile();

		if(!(zip.exists())){
			logger.error("not existing zip file "+filePath);
			throw new SpagoBIRuntimeException("not existing zip file "+filePath, null);
		}

		logger.debug("OUT");
		return zip;
	}



	public static void deleteMassiveExportFolderIfEmpty(String functionalityCd) throws IOException{
		logger.debug("IN");
		File folder = getMassiveExportZipFolder(functionalityCd);

		if(folder.exists() && folder.isDirectory()){
			File[] files = 	folder.listFiles();
			if(files == null || files.length == 0){
				folder.delete();		
				logger.debug("directory deleted "+folder.getAbsolutePath());
			}
		}
		logger.debug("OUT");
	}

}
