/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ResourceClassLoader extends ClassLoader {

	protected  ClassLoader parentClassLoader;
	protected  File resourceRootFolder;

	public ResourceClassLoader(String resourceRootFolderPath,ClassLoader parentClassLoader) {		
		this(new File(resourceRootFolderPath), parentClassLoader);
	}
	
	public ResourceClassLoader(File resourceRootFolder, ClassLoader parentClassLoader) {	
		super(parentClassLoader);
		this.resourceRootFolder = resourceRootFolder;
		
		if(!this.resourceRootFolder.exists()) throw new RuntimeException("Root folder [" + this.resourceRootFolder + "] does not exist");
		if(!this.resourceRootFolder.isDirectory()) throw new RuntimeException("Root folder [" + this.resourceRootFolder + "] is a file not a folder");
	}

	
	@Override
	public InputStream getResourceAsStream(String resourceFileName) {
		File resourceFile = new File(resourceRootFolder, resourceFileName);
		FileInputStream fis;
		try {
			fis = new FileInputStream(resourceFile);
		} catch (FileNotFoundException e) {
			return null;
		}
		return fis;
	}






}
