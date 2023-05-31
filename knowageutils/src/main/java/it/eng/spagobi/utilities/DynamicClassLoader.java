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
package it.eng.spagobi.utilities;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

public class DynamicClassLoader extends URLClassLoader {

	private static final Logger LOGGER = Logger.getLogger(DynamicClassLoader.class);

	private File jar;

	/**
	 * Instantiates a new dynamic class loader.
	 *
	 * @param jarFileName the jar file name
	 * @param cl the cl
	 */
	public DynamicClassLoader(String jarFileName, ClassLoader cl) {
		this (new File(jarFileName), cl);
	}



	/**
	 * Instantiates a new dynamic class loader.
	 *
	 * @param jarFile the jar file
	 * @param cl the cl
	 */
	public DynamicClassLoader(File jarFile, ClassLoader cl) {
		super(new URL[0], cl);
		jar = jarFile;
	}

	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public Class loadClass(String className) throws ClassNotFoundException {
		return (loadClass(className, true));
	}


	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 */
	@Override
	public synchronized Class loadClass(String className, boolean resolve) throws ClassNotFoundException {

		Class classToReturn = null;
		try {
			classToReturn = super.loadClass(className, resolve);
		} catch (Exception e) {
			LOGGER.warn("Impossible to load class [" + className + "]");
		}
		if(classToReturn == null) {
			byte[] res = null;
			try (ZipFile zipFile = new ZipFile(jar)) {
				ZipEntry zipEntry = zipFile.getEntry(className.replace('.', '/')+".class");
				res = new byte[(int)zipEntry.getSize()];
				try (BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(zipEntry))) {
					bis.read(res, 0, res.length);
				}
			} catch (Exception ex) {
				LOGGER.warn("className: " + className + " Exception: " + ex);
			}

			if (res == null)
				return super.findSystemClass(className);

			classToReturn = defineClass(className, res, 0, res.length);
			if (classToReturn == null)
				throw new ClassFormatError();

			if (resolve)
				resolveClass(classToReturn);
		}
		return classToReturn;
	}



    /**
     * Returns an input stream for reading the specified resource.
     * We overwrite the parent method for get class from the datamart.jar file
     * @param The resource name
     * @return An input stream for reading the resource, or null if the resource could not be found
     */
	@Override
	public synchronized InputStream getResourceAsStream(String resourceName)  {
		InputStream bis = null;
		try {
			bis = super.getResourceAsStream(resourceName);
		} catch (Exception ex) {
		}
		if(bis==null){
			try (ZipFile zipFile = new ZipFile(jar)) {
				ZipEntry zipEntry = zipFile.getEntry(resourceName);
				bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
			} catch (Exception ex2) {
				LOGGER.warn("className: " + resourceName + " Exception: " + ex2);
			}
		}
		return bis;
	}

    /**
     * Finds the resource with the given name. A resource is some data (images, audio, text, etc)
     * that can be accessed by class code in a way that is independent of the location of the code.
     * The name of a resource is a '/'-separated path name that identifies the resource.
     * We overwrite the parent method for the persistence.xml from the datamart.jar file
     * @param The resource name
     * @return An enumeration of URL objects for the resource. If no resources could be found,
     * the enumeration will be empty. Resources that the class loader doesn't have access to will not be in the enumeration.
     */
	@Override
	public Enumeration<URL> getResources(String descriptorPath)  throws IOException{

		if(descriptorPath.equals("META-INF/persistence.xml")){
			//load the persistence.xml from the jar file
			try{
				String s = jar.getAbsolutePath().replace(File.separatorChar, '/');
				final URL jarUrl = new URL("jar","",-1,"file:/"+s+"!/META-INF/persistence.xml");
				//build the enumeration with only the URL with the location of the persistence.xml
				return new Enumeration<URL>() {
					private int position = 0;

					@Override
					public boolean hasMoreElements() {
						return position>=0;
					}

					@Override
					public URL nextElement() {
						if(position<0)
							throw new NoSuchElementException();
						position --;
						return jarUrl;
					}
				};
			}catch (Exception e) {
				LOGGER.error("Error loading the " + descriptorPath + " from the jar file " + jar.getAbsolutePath(), e);
				LOGGER.error("Use the default loader..");
				return super.getResources(descriptorPath);
			}
		}else{
			return super.getResources(descriptorPath);
		}
	}

}
