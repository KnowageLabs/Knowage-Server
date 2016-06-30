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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

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

	private final File logDir;

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
	public Compiler(File srcDir, File binDir, File libDir, String srcPackage, File logDir) {
		this.logDir = logDir.getAbsoluteFile();

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
	 */
	public boolean compile() {

		boolean result = false;

		// File[] files1 = ... ; // input for first compilation task
		// File[] files2 = ... ; // input for second compilation task

		List<File> files = new ArrayList<>();
		for (File f : Files.fileTreeTraverser().preOrderTraversal(this.srcDir).toList()) {
			if (f.isFile() && f.getName().endsWith("java")) {
				files.add(f);
			}
		}
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
		result = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits).call();

		// Iterable<? extends JavaFileObject> compilationUnits2 =
		// fileManager.getJavaFileObjects(files2);
		// compiler.getTask(null, fileManager, null, null, null, compilationUnits2).call();
		FileWriter fw = null;
		try {
			if (!diagnostics.getDiagnostics().isEmpty()) {
				fw = new FileWriter(logDir.getAbsolutePath() + File.separatorChar + "metacompiler_out.log", true);
				for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
					fw.append(MessageFormat.format("Error on line {0} in {1}\n", diagnostic.getLineNumber(), diagnostic.getSource().toUri()));
					diagnostic.getSource().getCharContent(true);
					// System.out.format("Error on line %d in %s%n",
					// diagnostic.getLineNumber(),
					// diagnostic.getSource().toUri());
				}
				fw.close();
			}
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		;

		logger.trace("IN");

		// String command = "\"" + srcDir + "\" -classpath \"" + getClasspath() + "\" -d \"" + binDir + "\" -source 1.5";
		// logger.info("Compile command is equal to [{}]", command);
		//
		// PrintWriter error;
		// PrintWriter out;
		// try {
		// error = new PrintWriter(new FileWriter(logDir.getAbsolutePath() + File.separatorChar + "metacompiler_errors.log", true));
		// out = new PrintWriter(new FileWriter(logDir.getAbsolutePath() + File.separatorChar + "metacompiler_out.log", true));
		// result = org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(command, out, error, null);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		logger.info("Mapping files compiled succesfully: [{}]", result);

		logger.trace("OUT");

		return result;
	}

	private String getClasspath() {
		String classPath = ".";
		for (File lib : libs) {
			// Assert.assertTrue("Impossible to locate lib [" + libFile + "]", libFile.exists() && libFile.isFile());
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
