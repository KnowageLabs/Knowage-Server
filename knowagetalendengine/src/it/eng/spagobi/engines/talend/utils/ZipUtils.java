/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Andrea Gioia
 * 
 */
public class ZipUtils {

	/**
	 * Copy input stream.
	 * 
	 * @param in the in
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static final void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	
	/**
	 * Unzip.
	 * 
	 * @param zipFile the zip file
	 * @param destDir the dest dir
	 */
	public static void unzip(ZipFile zipFile, File destDir) {
		
		Enumeration entries;
		
		try {
			
			entries = zipFile.entries();
			
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();

				if (!entry.isDirectory()) {
					File destFile = new File(destDir,entry.getName());
					File destFileDir = destFile.getParentFile();
					if(!destFileDir.exists()) {
						System.err.println("Extracting directory: " + entry.getName().substring(0, entry.getName().lastIndexOf('/')));
						destFileDir.mkdirs();
					}
					
					System.err.println("Extracting file: " + entry.getName());
					copyInputStream(zipFile.getInputStream(entry),
							new BufferedOutputStream(new FileOutputStream(
									new File(destDir,entry.getName()))));
				}				
			}

			zipFile.close();
		} catch (IOException ioe) {
			System.err.println("Unhandled exception:");
			ioe.printStackTrace();
			return;
		}
	}
	
	/**
	 * Unzip skip first level.
	 * 
	 * @param zipFile the zip file
	 * @param destDir the dest dir
	 */
	public static void unzipSkipFirstLevel(ZipFile zipFile, File destDir) {
		
		Enumeration entries = null;;
		
		try {
		entries = zipFile.entries();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
	
			
			
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();

				if (!entry.isDirectory()) {
					String destFileStr = entry.getName();
					
					destFileStr = (destFileStr.indexOf('/') > 0)
									? destFileStr.substring(destFileStr.indexOf('/')) 
									: null;
					if(destFileStr == null) continue;
					File destFile = new File(destDir, destFileStr);
					File destFileDir = destFile.getParentFile();
					if(!destFileDir.exists()) {
						System.err.println("Extracting directory: " + entry.getName().substring(0, entry.getName().lastIndexOf('/')));
						destFileDir.mkdirs();
					}
					
					System.err.println("Extracting file: " + entry.getName());
					copyInputStream(zipFile.getInputStream(entry),
							new BufferedOutputStream(new FileOutputStream(
									new File(destDir, destFileStr))));
				}				
			}

			zipFile.close();
		} catch (IOException ioe) {
			System.err.println("Unhandled exception:");
			ioe.printStackTrace();
			return;
		}
	}
	
	/**
	 * Gets the directory name by level.
	 * 
	 * @param zipFile the zip file
	 * @param levelNo the level no
	 * 
	 * @return the directory name by level
	 */
	public static String[] getDirectoryNameByLevel(ZipFile zipFile, int levelNo) {
		
		Set names = new HashSet();
		Enumeration entries;
		
		try {
			
			entries = zipFile.entries();
			
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();

				if (!entry.isDirectory()) {
					String fileName = entry.getName();
					String[] components = fileName.split("/");
					
					if(components.length == (levelNo+1)) {
						String dirNam = components[components.length-2];
						names.add(dirNam);
					}
					
					System.out.println(entry.getName());
				}				
			}

			zipFile.close();
		} catch (IOException ioe) {
			System.err.println("Unhandled exception:");
			ioe.printStackTrace();
			return null;
		}
		
		return (String[])names.toArray(new String[0]);
	}
	
	
	
	
	

}
