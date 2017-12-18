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
package it.eng.spagobi.commons.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class FileUtilities {
	
	static private Logger logger = Logger.getLogger(FileUtilities.class);
	
	public static boolean copyFile(File toBeCopied, File toDir, boolean overwrite, boolean renamePrevious) {
		logger.debug("IN: file to be copied: " + toBeCopied.getAbsolutePath() + "; " +
				"destination folder: " + toDir.getAbsolutePath() + "; overwrite: " + overwrite);
		FileOutputStream fos = null;
		InputStream is = null;
		boolean toReturn = true;
		try {
			File copy = new File(toDir.getAbsolutePath() + File.separatorChar + toBeCopied.getName());
			if (copy.exists()) {
				if (overwrite) {
					if (!renamePrevious) {
						boolean result = copy.delete();
						if (!result) {
							logger.debug("OUT: Could not delete file " + copy.getAbsolutePath() + ". Returning false");
							return false;
						}
					} else {
						Date date = new Date();
						SimpleDateFormat sdf = new SimpleDateFormat();
						sdf.applyPattern("yyyy-MM-dd");
						String dateStr = sdf.format(date);
						File renameTo = new File(toDir.getAbsolutePath() + File.separatorChar + toBeCopied.getName() + "." + dateStr);
						if (renameTo.exists()) {
							boolean result = renameTo.delete();
							if (!result) {
								logger.debug("OUT: Could not delete file " + renameTo.getAbsolutePath() + ". Returning false");
								return false;
							}
						}
						boolean result = copy.renameTo(renameTo);
						if (!result) {
							logger.debug("OUT: Could not rename file " + copy.getAbsolutePath() + ". Returning false");
							return false;
						}
					}
				} else {
					logger.debug("OUT: File " + copy.getAbsolutePath() + " exists but overwrite is false. Returning false");
					return false;
				}
			}
	        fos = new FileOutputStream(copy);
	        is = new FileInputStream(toBeCopied);
	        int read = 0;
	        while ((read = is.read()) != -1) {
	        	fos.write(read);
	        }
	        fos.flush();
		} catch (Exception e) {
			logger.debug(e);
			toReturn = false;
		} finally {
        	try {
	        	if (fos != null) {
	        		fos.close();
	        	}
	        	if (is != null) {
	        		is.close();
	        	}
        	} catch (Exception e) {
        	    logger.error("Error while closing streams " , e);
        	}
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	/**
	 * Copy all files and contained directories (if recursive is true) in the source directory into the destination directory.
	 * @param sourceDir The String representing the source directory 
	 * @param destDir The String representing the destination directory
	 * @param recursive Boolean: if it is true, contained directories are copied also recursively
	 * @param overwrite Boolean: if it is true, contained files are overwritten
	 * @throws Exception
	 */
	public static void copyDirectory(String sourceDirStr, String destDirStr, boolean recursive, boolean overwrite, boolean renamePrevious) throws Exception {
		logger.debug("IN: source folder: " + sourceDirStr + "; " +
				"destination folder: " + destDirStr + "; overwrite: " + overwrite + "; recursive: " + recursive);
		File destDir = new File(destDirStr);
		File sourceDir = new File(sourceDirStr);
		copyDirectory(sourceDir, destDir, recursive, overwrite, renamePrevious);
		logger.debug("OUT");
	}
	
	
	/**
	 * Copy all files and contained directories (if recursive is true) in the source directory into the destination directory. 
	 * @param destDir The destination directory File
	 * @param sourceDir The source directory File
	 * @param recursive Boolean: if it is true, contained directories are copied also recursively
	 * @param overwrite Boolean: if it is true, contained files are overwritten
	 * @throws Exception
	 */
	public static void copyDirectory(File sourceDir, File destDir, boolean recursive, boolean overwrite, boolean renamePrevious) throws Exception {
		logger.debug("IN: source folder: " + sourceDir.getAbsolutePath() + "; " +
				"destination folder: " + destDir.getAbsolutePath() + "; overwrite: " + overwrite + "; recursive: " + recursive);
		if (!destDir.exists() && !destDir.isDirectory()) {
			destDir.mkdirs();
		}
		File[] containedFiles = sourceDir.listFiles();
		for (int i = 0; i < containedFiles.length; i++) {
			File aFile = containedFiles[i];
			if (aFile.isFile()) copyFile(aFile, destDir, overwrite, renamePrevious);
			else {
				String dirName = aFile.getName();
				File newDir = new File(destDir.getAbsolutePath() + File.separatorChar + dirName);
				copyDirectory(aFile, newDir, recursive, overwrite, renamePrevious);
			}
		}
		logger.debug("OUT");
	}

    /**
     * Delete a folder and its contents.
     * 
     * @param dir The java file object of the directory
     * 
     * @return the result of the operation
     */
    public static boolean deleteDir(File dir) {
		logger.debug("IN");
		if (dir.isDirectory()) {
		    String[] children = dir.list();
		    for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
				    return false;
				}
		    }
		}
		logger.debug("OUT");
		return dir.delete();
    }
	
}
