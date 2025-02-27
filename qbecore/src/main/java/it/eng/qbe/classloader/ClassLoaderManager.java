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
package it.eng.qbe.classloader;


import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class ClassLoaderManager {

    private static final Logger LOGGER = Logger.getLogger(ClassLoaderManager.class);

    public static ClassLoader qbeClassLoader;

    /**
     * Updates the class loader of the thread and sets the class loader in the
     * variable qbeClassLoader..
     * NOTE: The qbeClassLoader is static
     *
     * @param jarFile
     * @return
     */
    public static ClassLoader updateCurrentWebClassLoader(File jarFile) {

        LOGGER.debug("IN");

        try {

            LOGGER.debug("Jar file to be loaded: " + jarFile.getAbsoluteFile());


            DynamicClassLoader previousCL = getPreviousClassLoader(qbeClassLoader, jarFile);
            if (previousCL != null) {
                // the jar file has already been loaded, we must verify if it has been changed
                LOGGER.debug("Found a previous class loader for file " + jarFile.getAbsolutePath());
                if (jarFile.lastModified() == previousCL.getJarFileLastModified()) {
                    // the file has not been changed, no need to update the class loader
                    LOGGER.debug("File " + jarFile.getAbsolutePath() + " has not been changed, no need to update the classloader");
                } else {
                    // we must remove the class loader associated to that jar file and then recreate it
                    LOGGER.debug("File " + jarFile.getAbsolutePath() + " has been changed, removing it ...");
                    removeClassLoader(previousCL);
                    // then update the class loader
                    qbeClassLoader = updateCurrentClassLoader(jarFile);
                }
            } else {
                // the jar file hasn't already been loaded, we must add it with a new class loader
                LOGGER.debug("File " + jarFile.getAbsolutePath() + " hasn't already been loaded, loading it ...");
                // update the class loader
                qbeClassLoader = updateCurrentClassLoader(jarFile);
            }

            Thread.currentThread().setContextClassLoader(qbeClassLoader);


        } catch (Exception e) {
            LOGGER.error("Impossible to update current class loader", e);
        }

        return qbeClassLoader;
    }

    private static void removeClassLoader(DynamicClassLoader previousCL) {
        if (qbeClassLoader instanceof DynamicClassLoader) {
            DynamicClassLoader start = (DynamicClassLoader) qbeClassLoader;
            ClassLoader genericClassLoader = start;
            // save all the class loaders already defined into a temp list, except the one that we have to remove,
            // untill we find the root web app class loader
            List<DynamicClassLoader> currentClassLoaders = new ArrayList<>();
            ClassLoader root = null;
            while (genericClassLoader instanceof DynamicClassLoader) {
                DynamicClassLoader aDynClassLoader = (DynamicClassLoader) genericClassLoader;
                if (!previousCL.equals(genericClassLoader)) {
                    currentClassLoaders.add(aDynClassLoader);
                }
                genericClassLoader = genericClassLoader.getParent();
            }
            root = genericClassLoader;

            // re-recreate all the dynamic class loaders
            ClassLoader previous = root;
            for (int i = 0; i < currentClassLoaders.size(); i++) {
                DynamicClassLoader aDynClassLoader = currentClassLoaders.get(i);
                DynamicClassLoader newDynClassLoader = new DynamicClassLoader(aDynClassLoader.getJarFile(), previous);
                previous = newDynClassLoader;
            }
            qbeClassLoader = previous;
        }
    }

    private static DynamicClassLoader getPreviousClassLoader(ClassLoader loader, File jarFile) {
        if (loader instanceof DynamicClassLoader) {
            DynamicClassLoader dynamicLoader = (DynamicClassLoader) loader;
            if (dynamicLoader.getJarFile().equals(jarFile)) {
                return dynamicLoader;
            } else {
                // we call recursively method on parent class loader
                return getPreviousClassLoader(dynamicLoader.getParent(), jarFile);
            }
        } else {
            return null;
        }
    }

    /**
     * Update the thread class loader with a dynamic class loader that
     * considers also the jar file
     *
     * @param file
     * @return
     */
    public static ClassLoader updateCurrentClassLoader(File file) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        boolean wasAlreadyLoaded = false;

        LOGGER.debug("IN");

        JarFile jarFile = null;

        int thresholdEntries = 10000;
        int thresholdSize = 100_000_000; // 100MB
        double thresholdRatio = 4;
        long totalSizeArchive = 0;
        int totalEntryArchive = 0;

        try {
            jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                InputStream in = jarFile.getInputStream(entry);

                totalEntryArchive++;

                if (totalEntryArchive > thresholdEntries) {
                    // too much entries in this archive, can lead to inodes exhaustion of the system
                    throw new SpagoBIRuntimeException("Error while unzip file. Invalid archive file: too much entries in this archive, can lead to inodes exhaustion of the system");
                }

                int totalSizeEntry = 0;

                byte[] buffer = new byte[1024];
                int len;

                while ((len = in.read(buffer)) >= 0) {
                    totalSizeEntry += len;
                    totalSizeArchive += len;

                    double compressionRatio = (double) totalSizeEntry / entry.getCompressedSize();
                    if (compressionRatio > thresholdRatio || totalSizeArchive > thresholdSize) {
                        // ratio between compressed and uncompressed data is highly suspicious, looks like a Zip Bomb Attack
                        throw new SpagoBIRuntimeException("Error while unzip file. Invalid archive file: the archive is too big or too compressed");
                    }
                }

                if (entry.getName().endsWith(".class")) {
                    String entryName = entry.getName();
                    String className = entryName.substring(0, entryName.lastIndexOf(".class"));
                    className = className.replaceAll("/", ".");
                    className = className.replaceAll("\\\\", ".");
                    try {
                        LOGGER.debug("loading class [" + className + "]" + " with class loader ["
                                + Thread.currentThread().getContextClassLoader().getClass().getName() + "]");
                        Thread.currentThread().getContextClassLoader().loadClass(className);
                        wasAlreadyLoaded = true;
                        LOGGER.debug("Class [" + className + "] has been already loaded (?)");
                        break;
                    } catch (Exception e) {
                        wasAlreadyLoaded = false;
                        LOGGER.debug("Class [" + className + "] hasn't be loaded yet (?)");
                        break;
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Impossible to update current class loader", e);
        } finally {
            try {
                if (jarFile != null) {
                    jarFile.close();
                }
            } catch (Exception e2) {
                LOGGER.error("Error closing the jar file", e2);
            }
        }

        LOGGER.debug("Jar file [" + file.getName() + "] already loaded: " + wasAlreadyLoaded);

        try {

            if (!wasAlreadyLoaded) {

                ClassLoader previous = cl;
                Thread.currentThread().getContextClassLoader();
                DynamicClassLoader current = new DynamicClassLoader(file, previous);
                Thread.currentThread().setContextClassLoader(current);
                cl = current;
            }

        } catch (Exception e) {
            LOGGER.error("Impossible to update current class loader", e);
        }

        return cl;
    }

    private ClassLoaderManager() {

    }
}

