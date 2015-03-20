/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.file;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;



/**
 * @author Andrea Gioia
 *
 */
public class FileUtils {
	
	private static transient Logger logger = Logger.getLogger(FileUtils.class);
	
	public static boolean isAbsolutePath(String path) {
		if(path == null) return false;
		return (path.startsWith("/") || path.startsWith("\\") || path.charAt(1) == ':');
	}
	
	 /**
	  * Utility method that gets the extension of a file from its name if it has one
	  */ 
	public static String getFileExtension(File file) {
    	try {
    		return getFileExtension(file.getCanonicalPath());
    	}catch(IOException e) {
    		return "";
    	}
    }
	
	/**
	  * Utility method that gets the extension of a file from its name if it has one
	  */ 
	public static String getFileExtension(String fileName) {
		if (fileName == null || fileName.lastIndexOf(".") < 0) {
			return "";
		}
		
		// Could be that the file name actually end with a '.' so lets check
		if(fileName.lastIndexOf(".") + 1 == fileName.length()) {
			return "";
		} 
		
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
		
		// Could be that the path actually had a '.' in it so lets check
		if(extension.contains(File.separator)) {
			extension = "";
		}
		
		return extension;
	}
	
	public static void doForEach(File rootDir, IFileTransformer transformer) {
		Assert.assertNotNull(rootDir, "rootDir parameters cannot be null");
		Assert.assertTrue(rootDir.exists() && rootDir.isDirectory(), "rootDir parameter [" + rootDir + "] is not an existing directory");
		Assert.assertNotNull(transformer, "transformer parameters cannot be null");
		
		File[] files = rootDir.listFiles() ;
		for(int i = 0; i < files.length; i ++) {
			File file = files[i];
			if(file.isDirectory()) {
				doForEach(file, transformer);
			} else {
				transformer.transform(file);
			}
		}
	}
	

	/**
	 * Checks if upload file:
	 * - is not empty;
	 * - does not exceed specified max size;
	 * - has one of the specified extensions.
	 * A SpagoBIRuntimeException is thrown in case one of the conditions is not satisfied.
	 * 
	 * @param uploaded The uploaded file
	 * @param maxSize The max size
	 * @param admissibleFilesExtension The admissible files extension
	 */
	public static void checkUploadedFile(FileItem uploaded, Integer maxSize, List<String> admissibleFilesExtension) {
		// check if the uploaded file is empty
		if (uploaded.getSize() == 0) {
			logger.error("The uploaded file is empty");
			throw new SpagoBIRuntimeException("The uploaded file is empty");
		}
		// check if the uploaded file exceeds the maximum dimension
		if (maxSize != null && uploaded.getSize() > maxSize) {
			logger.error("The uploaded file exceeds the maximum size, that is "
					+ maxSize + " bytes");
			throw new SpagoBIRuntimeException("The uploaded file exceeds the maximum size, that is "
							+ maxSize + " bytes");
		}

		if (admissibleFilesExtension != null) {
			// check if the extension is valid
			String fileExtension = FileUtils.getFileExtension( uploaded.getName() );
			if (!admissibleFilesExtension.contains(fileExtension.toLowerCase())
					&& !admissibleFilesExtension.contains(fileExtension
							.toUpperCase())) {
				logger.error("The uploaded file has an invalid extension. Choose a "
						+ admissibleFilesExtension.toString() + " file.");
				String msg = "The uploaded file has an invalid extension. Choose a "
						+ admissibleFilesExtension.toString() + " file.";
				throw new SpagoBIRuntimeException(msg);
			}
		}	
	}

	/**
	 * Check is the specified directory exists; in case it does not, it creates it.
	 * @param directory The directory path
	 * @return The File directory object
	 */
	public static File checkAndCreateDir(String directory) {
		File toReturn = null;
		try {
			toReturn = new File(directory);
			if (toReturn.exists() && !toReturn.isDirectory()) {
				throw new SpagoBIRuntimeException("File " + directory + " already exists and it is not a directory");
			}
			if (!toReturn.exists()){
				//Create Directory if doesn't exist
				boolean mkdirResult = toReturn.mkdirs();
				if (!mkdirResult) {
					logger.error("Cannot create directory " + directory + " into server resources");
					throw new SpagoBIRuntimeException("Cannot create directory " + directory + " into server resources");
				}
			}
		} catch (Throwable t) {
			logger.error("Error while creating directory", t);
			throw new SpagoBIRuntimeException("Error while creating directory", t);
		}
		return toReturn;
	}
	
	/**
	 * Check if the specified folder in input has a number of files that exceeds the specified max allowed number.
	 * @param targetDirectory The folder to check
	 * @param maxAllowed The max allowed number of contained files
	 */
	public static void checkIfFilesNumberExceedsInDirectory(File targetDirectory, int maxAllowed) {
		if (!targetDirectory.exists() || !targetDirectory.isDirectory()) {
			throw new SpagoBIRuntimeException("File " + targetDirectory + " isn't a directory");
		}
		File[] existingImages = getContainedFiles(targetDirectory);
		int existingImagesNumber = existingImages.length;
		if (existingImagesNumber >= maxAllowed) {
			throw new SpagoBIEngineRuntimeException("Max files number exceeded");
		}
	}

	/**
	 * Gets the files contained by the specified folder
	 * @param targetDirectory The directory to look for
	 * @return the files contained by the specified folder
	 */
	public static File[] getContainedFiles(File targetDirectory) {
		logger.debug("IN");
		File[] files = targetDirectory.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.isFile()) {
					return true;
				}
				return false;
			}
		});
		logger.debug("OUT");
		return files;
	}
	
	public static File saveFileIntoDirectory(FileItem uploaded, File targetDirectory) {
		File toReturn = null;
		if (!targetDirectory.exists() || !targetDirectory.isDirectory()) {
			throw new SpagoBIRuntimeException("File " + targetDirectory + " isn't a directory");
		}
		// uuid generation
		String uuid = createNewExecutionId();
		String fileExtension = FileUtils.getFileExtension( uploaded.getName() );
		logger.debug("Recognized file extension is " + fileExtension);
		if (StringUtilities.isEmpty(fileExtension)) {
			logger.error("Unrecognized file extension : " + fileExtension);
			throw new SpagoBIRuntimeException("Unrecognized file extension : " + fileExtension);
		}
		String fileName = uuid + "." + fileExtension;
		toReturn = new File(targetDirectory, fileName);
		try {
			uploaded.write(toReturn);
		} catch (Throwable t) {
			logger.error("Error while saving file into server", t);
			throw new SpagoBIRuntimeException("Error while saving file into server", t);
		}
		return toReturn;
	}
	
	public static boolean checkFileIfExist(File file) {
		return (file.exists());
	}
	
	public static String createNewExecutionId() {
		String executionId = null;
		UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
		UUID uuidObj = uuidGen.generateTimeBasedUUID();
		executionId = uuidObj.toString();
		executionId = executionId.replaceAll("-", "");
		return executionId;
	}
	
	/**
	 * Throws a SecurityException if a path traversal attack is detected.
	 * See https://wiki.spagobi.org/xwiki/bin/view/spagobi_standard_dev/Sicurezza?srid=TP2i4TZR#HPathtraversal
	 * @param fileName The file name to be checked
	 * @param targetDirectory The folder to check
	 */
	public static void checkPathTraversalAttack(String fileName, File targetDirectory) {
		if (StringUtilities.isEmpty(fileName)) {
			throw new SpagoBIRuntimeException("File name not specified.");
		}
		if (!targetDirectory.exists() || !targetDirectory.isDirectory()) {
			throw new SpagoBIRuntimeException("File " + targetDirectory + " isn't a directory");
		}
		String completeFileName = targetDirectory.getAbsolutePath() + File.separator
				+ fileName;
		File file = new File(completeFileName);
		File parent = file.getParentFile();
		// Prevent directory traversal (path traversal) attacks
		if (!targetDirectory.equals(parent)) {
			logger.error("Trying to access the file [" + file.getAbsolutePath()
					+ "] that is not inside [" + targetDirectory.getAbsolutePath()
					+ "]!!!");
			throw new SecurityException("Trying to access a the file in an unexpected position!!!");
		}
	}
	
	
	
}
