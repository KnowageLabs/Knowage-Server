/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.utils;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Andrea Gioia
 */
public class ZipUtils {

    private static final Logger LOGGER = LogManager.getLogger(ZipUtils.class);


    static int thresholdEntries = 10_000;
    static int thresholdSize = 100_000_000; // 100MB
    static double thresholdRatio = 4;

    /**
     * Copy input stream.
     *
     * @param in  the in
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void copyInputStream(InputStream in, OutputStream out, int totalSizeEntry, long totalSizeArchive, ZipEntry ze) throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            totalSizeEntry += len;
            totalSizeArchive += len;

            double compressionRatio = (double) totalSizeEntry / ze.getCompressedSize();
            if (compressionRatio > thresholdRatio || totalSizeArchive > thresholdSize) {
                // ratio between compressed and uncompressed data is highly suspicious, looks like a Zip Bomb Attack
                in.close();
                out.close();
                throw new SpagoBIRuntimeException("Error while unzipping file. Invalid archive file");
            }
            out.write(buffer, 0, len);
        }

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

            int totalEntryArchive = 0;
            long totalSizeArchive = 0;

            while (entries.hasMoreElements()) {
                totalEntryArchive ++;

                if (totalEntryArchive > thresholdEntries) {
                    // too much entries in this archive, can lead to inodes exhaustion of the system
                    throw new SpagoBIRuntimeException("Error while unzip file. Invalid archive file: too much entries in this archive, can lead to inodes exhaustion of the system");
                }
                ZipEntry entry = entries.nextElement();

                int totalSizeEntry = 0;

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
                            new BufferedOutputStream(new FileOutputStream(new File(destDir, entry.getName()))), totalSizeEntry, totalSizeArchive, entry);
                }
            }

            zipFile.close();
        } catch (IOException ioe) {
            LOGGER.error("Non-fatal error unzipping {} to directory {}", zipFile, destDir, ioe);
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
            int totalEntryArchive = 0;
            long totalSizeArchive = 0;
            while (entries.hasMoreElements()) {

                totalEntryArchive++;

                if (totalEntryArchive > thresholdEntries) {
                    // too much entries in this archive, can lead to inodes exhaustion of the system
                    throw new SpagoBIRuntimeException("Error while unzip file. Invalid archive file: too much entries in this archive, can lead to inodes exhaustion of the system");
                }

                ZipEntry entry = entries.nextElement();

                int totalSizeEntry = 0;

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
                            new BufferedOutputStream(new FileOutputStream(new File(destDir, destFileStr))), totalSizeEntry, totalSizeArchive, entry);

                }
            }

            zipFile.close();
        } catch (IOException ioe) {
            LOGGER.error("Non-fatal error unzipping {} to directory {}", zipFile, destDir, ioe);
        }
    }

    /**
     * Gets the directory name by level.
     *
     * @param zipFile the zip file
     * @param levelNo the level no
     * @return the directory name by level
     */
    public static String[] getDirectoryNameByLevel(ZipFile zipFile, int levelNo) {

        Set<String> names = new HashSet<>();

        try {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
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
