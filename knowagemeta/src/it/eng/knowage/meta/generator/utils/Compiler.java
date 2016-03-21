/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.generator.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is used by SpagoBI Meta to compile generated java class and to create JAR file.
 * 
 * @authors 
 * Angelo Bernabei (angelo.bernabei@eng.it)
 * Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class Compiler {

	private File srcDir;
	private File binDir;
	private File libDir;

	/**
	 * necessary libraries to compile Java Classes
	 */
	private List<File> libs;
	

	private static Logger logger = LoggerFactory.getLogger(Compiler.class);
	
	
	/**
	 * Costructor
	 * 
	 * @param srcDir Source directory
	 * @param binDir Class files directory
	 * @param libDir Libraries directory
	 * @param srcPackage the package of the source code
	 */
	public Compiler(File srcDir, File binDir, File libDir, String srcPackage){
		this.srcDir=srcDir.getAbsoluteFile();
		logger.debug("src dir set to [{}]", this.srcDir);
		
		this.binDir= binDir.getAbsoluteFile();
		logger.debug("bin dir set to [{}]", this.binDir);
	    
		this.libDir = libDir.getAbsoluteFile();
		logger.debug("lib dir set to [{}]", this.libDir);
		
		libs = new ArrayList<File>();
	}
	
	/**
	 * Compile all the generated java classes
	 * @return boolean : true if the compiler has worked well.
	 */
	public boolean compile(){
		boolean result = false;
		
		logger.trace("IN");
		
		String command = "\"" +srcDir + "\" -classpath \"" + getClasspath() + "\" -d \"" + binDir + "\" -source 1.5";
		logger.info("Compile command is equal to [{}]", command);
		
		PrintWriter error;
		PrintWriter out;
		try {
			error = new PrintWriter(new FileWriter("./log/spagobi/metacompiler_errors.log", true));
			out=new PrintWriter(new FileWriter("./log/spagobi/metacompiler_out.log", true));
			result = org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(command, out,error , null);
		} catch (IOException e) {
			e.printStackTrace();
		}


		logger.info("Mapping files compiled succesfully: [{}]", result);
		
		logger.trace("OUT");
		
		return result;
	}
	
	private String getClasspath(){
		String classPath = ".";
		for (File lib: libs){
			//Assert.assertTrue("Impossible to locate lib [" + libFile + "]", libFile.exists() && libFile.isFile());
			//classPath = classPath + ";" + lib;
			classPath = classPath + java.io.File.pathSeparator + lib;
		}
		
		logger.debug("Classpath is equal to [{}]", classPath);
		
		return classPath;
	}

	
	
	// ==========================================================================================
	// ACCESSOR METHODS
	// ==========================================================================================
	
	public File getSrcDir() {
		return srcDir;
	}

	public void setSrcDir(File srcDir) {
		this.srcDir = srcDir;
	}

	public File getBinDir() {
		return binDir;
	}

	public void setBinDir(File binDir) {
		this.binDir = binDir;
	}

	
	public File getLibDir() {
		return libDir;
	}

	public void setLibDir(File libDir) {
		this.libDir = libDir.getAbsoluteFile();
	}

	public void addLibs(String[] libNames) {
		for(int i = 0; i < libNames.length; i++) {
			File libFile = new File(libDir, libNames[i]);
			if(!libFile.exists()) {
				logger.warn("Library file [{}] does not exist", libFile);	
			}
			libs.add( libFile );
		}
	}
}
