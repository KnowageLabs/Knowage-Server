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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.knowage.utils.zip.ZipUtilsForSonar;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia
 *
 */
public class ZipUtils {

	private static final Logger LOGGER = LogManager.getLogger(ZipUtils.class);

	/**
	 * Copy input stream.
	 *
	 * @param in  the in
	 * @param out the out
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
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

		try {

			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipUtilsForSonar zipUtilsForSonar = new ZipUtilsForSonar();
				
				if(zipUtilsForSonar.doThresholdCheck(zipFile.getName())) {
					ZipEntry entry = entries.nextElement();
	
					if (!entry.isDirectory()) {
						File destFile = new File(destDir, entry.getName());
						File destFileDir = destFile.getParentFile();
						if (!destFileDir.exists()) {
							LOGGER.warn("Extracting directory: {}",
									entry.getName().substring(0, entry.getName().lastIndexOf('/')));
							destFileDir.mkdirs();
						}
	
						LOGGER.warn("Extracting file: {}", entry.getName());
						copyInputStream(zipFile.getInputStream(entry),
								new BufferedOutputStream(new FileOutputStream(new File(destDir, entry.getName()))));
					}
				} else {
					LOGGER.error("Error while unzip file. Invalid archive file");
					throw new SpagoBIRuntimeException("Error while unzip file. Invalid archive file");
				}
			}

			zipFile.close();
		} catch (IOException ioe) {
			LOGGER.error("Non-fatal error unzipping {} to directory {}", zipFile, destDir, ioe);
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
		try {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipUtilsForSonar zipUtilsForSonar = new ZipUtilsForSonar();
				
				if(zipUtilsForSonar.doThresholdCheck(zipFile.getName())) {
					ZipEntry entry = entries.nextElement();
	
					if (!entry.isDirectory()) {
						String destFileStr = entry.getName();
	
						destFileStr = (destFileStr.indexOf('/') > 0) ? destFileStr.substring(destFileStr.indexOf('/'))
								: null;
						if (destFileStr == null)
							continue;
						File destFile = new File(destDir, destFileStr);
						File destFileDir = destFile.getParentFile();
						if (!destFileDir.exists()) {
							LOGGER.warn("Extracting directory: {}",
									entry.getName().substring(0, entry.getName().lastIndexOf('/')));
							destFileDir.mkdirs();
						}
	
						LOGGER.warn("Extracting file: {}", entry.getName());
						copyInputStream(zipFile.getInputStream(entry),
								new BufferedOutputStream(new FileOutputStream(new File(destDir, destFileStr))));
					}
				} else {
					LOGGER.error("Error while unzip file. Invalid archive file");
					throw new SpagoBIRuntimeException("Error while unzip file. Invalid archive file");
				}
			}

			zipFile.close();
		} catch (IOException ioe) {
			LOGGER.error("Non-fatal error unzipping {} to directory {}", zipFile, destDir, ioe);
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

		Set<String> names = new HashSet<>();

		try {

			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipUtilsForSonar zipUtilsForSonar = new ZipUtilsForSonar();
				
				if(zipUtilsForSonar.doThresholdCheck(zipFile.getName())) {
					ZipEntry entry = entries.nextElement();
	
					if (!entry.isDirectory()) {
						String fileName = entry.getName();
						String[] components = fileName.split("/");
	
						if (components.length == (levelNo + 1)) {
							String dirNam = components[components.length - 2];
							names.add(dirNam);
						}
	
						LOGGER.warn("Current entry is {}", entry.getName());
					}
				} else {
					LOGGER.error("Error while unzip file. Invalid archive file");
					throw new SpagoBIRuntimeException("Error while unzip file. Invalid archive file");
				}
			}

			zipFile.close();
		} catch (IOException ioe) {
			LOGGER.error("Non-fatal error getting directory name by level using zip file {} and level {}", zipFile,
					levelNo, ioe);
			return null;
		}

		return names.toArray(new String[0]);
	}

	private ZipUtils() {
	}
}
