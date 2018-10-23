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
package it.eng.knowage.meta.generator.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * This class is used by Knowage Meta to compile generated java class and to create JAR file.
 *
 * @authors Angelo Bernabei (angelo.bernabei@eng.it) Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class Compiler {

	private File srcDir;
	private File binDir;
	private File libDir;

	private final PrintWriter log;

	/**
	 * necessary libraries to compile Java Classes
	 */
	private final List<File> libs;

	private static Logger logger = LoggerFactory.getLogger(Compiler.class);

	/**
	 * Costructor
	 *
	 * @param srcDir
	 *            Source directory
	 * @param binDir
	 *            Class files directory
	 * @param libDir
	 *            Libraries directory
	 * @param srcPackage
	 *            the package of the source code
	 */
	public Compiler(File srcDir, File binDir, File libDir, String srcPackage, PrintWriter log) {
		if (log == null) {
			this.log = new PrintWriter(new ByteArrayOutputStream());
		} else {
			this.log = log;
		}

		this.srcDir = srcDir.getAbsoluteFile();
		logger.debug("src dir set to [{}]", this.srcDir);

		this.binDir = binDir.getAbsoluteFile();
		logger.debug("bin dir set to [{}]", this.binDir);

		this.libDir = libDir.getAbsoluteFile();
		logger.debug("lib dir set to [{}]", this.libDir);

		libs = new ArrayList<File>();
	}

	/**
	 * Compile all the generated java classes
	 *
	 * @return boolean : true if the compiler has worked well.
	 * @throws IOException
	 */
	public boolean compile() {
		logger.debug("Starting compile of java classes");
		boolean result = false;

		// File[] files1 = ... ; // input for first compilation task
		// File[] files2 = ... ; // input for second compilation task

		List<File> files = new ArrayList<>();
		logger.debug("Traversing files");
		for (File f : Files.fileTreeTraverser().preOrderTraversal(this.srcDir).toList()) {
			if (f.isFile() && f.getName().endsWith("java")) {
				logger.debug("File to compile added to the list: " + f.getName());
				files.add(f);
			}
		}
		logger.debug("Initialize diagnostic collector");
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		logger.debug("Searching java system compiler");
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		logger.debug("Java system compiler is: " + compiler);
		if (compiler == null) {
			logger.error("Cannot find Java System compiler during compilation of jpa classes, check if JDK is correctly installed.");
		}
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

		logger.debug("Get compilation units");
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
		try {
			binDir.mkdir();
			fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(binDir));
			fileManager.setLocation(StandardLocation.CLASS_PATH,libs);
		} catch (IOException e) {
			logger.error("Cannot set output directory / classpath for compiler ");
		}

		logger.debug("Execute java compiler task");
		result = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits).call();
		logger.debug("Result of executed task is: " + result);

		logger.debug("Checking diagnostic errors");

		if (!diagnostics.getDiagnostics().isEmpty()) {
			logger.error("Found compilation errors during metamodel generation");
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
				logger.error("Found error on " + diagnostic.getSource().toUri() + "- line " + diagnostic.getLineNumber() + "- Error: "+diagnostic.getMessage(null));
				log.append(MessageFormat.format(diagnostic.getKind().toString()+" on line {0} in {1}\n  Detail: {2} \n", diagnostic.getLineNumber(),diagnostic.getSource().toUri(),diagnostic.getMessage(null)));
				log.append("--------------------------------------------- \n");
				try {
					diagnostic.getSource().getCharContent(true);
				} catch (IOException e) {
					log.append("Error while reading java sources.\n");
				}
			}
		}
		log.flush();

		return result;
	}

	private String getClasspath() {
		String classPath = ".";
		for (File lib : libs) {
			Assert.assertTrue("Impossible to locate lib [" + lib + "]", lib.exists() && lib.isFile());
			// classPath = classPath + ";" + lib;
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
		for (int i = 0; i < libNames.length; i++) {
			File libFile = new File(libDir, libNames[i]);
			if (!libFile.exists()) {
				logger.warn("Library file [{}] does not exist", libFile);
			}
			libs.add(libFile);
		}
	}
}
