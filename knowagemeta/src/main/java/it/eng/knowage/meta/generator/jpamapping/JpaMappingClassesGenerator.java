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
package it.eng.knowage.meta.generator.jpamapping;

import it.eng.knowage.meta.generator.GenerationException;
import it.eng.knowage.meta.generator.jpamapping.wrappers.JpaProperties;
import it.eng.knowage.meta.generator.utils.Compiler;
import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.business.BusinessModel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JpaMappingClassesGenerator extends JpaMappingCodeGenerator {

	// List<File> libs;

	private File libDir;
	private File binDir;
	private File logDir;

	private PrintWriter errorLog;

	public static final String DEFAULT_BIN_DIR = "build";
	public static final String DEFAULT_LIB_DIR = "libs";
	public static final String DEFAULT_LOG_DIR = "logs";
	private static final String SBI_MODEL_FILE_NAME = "sbimodel";

	// private String[] libs = { "org.eclipse.persistence.core_2.1.1.v20100817-r8050.jar", "javax.persistence_2.0.1.v201006031150.jar",
	// "hibernate-spatial-1.1.1.jar", "jts-1.13.jar", "hibernate3.6.2.jar" };
	private String[] libs = { "javax.persistence-2.0.5.jar", "hibernate-core-3.6.2.Final.jar", "jts-1.13.jar" };

	private static Logger logger = LoggerFactory.getLogger(JpaMappingClassesGenerator.class);

	public JpaMappingClassesGenerator() {
		super();
	}

	@Override
	public void generate(ModelObject o, String outputDir) {
		generate(o, outputDir, false, false, null, null);
	}

	@Override
	public void generate(ModelObject o, String outputDir, boolean isUpdatableMapping, boolean includeSources, File libsDir, byte[] fileModel) {

		logger.trace("IN");

		// try {
		BusinessModel model;

		super.generate(o, outputDir, isUpdatableMapping, includeSources, libsDir, null);

		binDir = (binDir == null) ? new File(outputDir, DEFAULT_BIN_DIR) : binDir;
		logger.debug("src dir is equal to [{}]", getSrcDir());
		// libDir = (libDir == null) ? new File(outputDir, DEFAULT_LIB_DIR) : libDir;
		libDir = (libsDir == null) ? new File(outputDir, DEFAULT_LIB_DIR) : libsDir;
		logger.debug("lib dir is equal to [{}]", libDir);

		logDir = logDir == null ? new File(outputDir, DEFAULT_LOG_DIR) : logDir;

		model = (BusinessModel) o;

		// Get Package Name
		String packageName = model.getProperties().get(JpaProperties.MODEL_PACKAGE).getValue();

		// Call Java Compiler
		Compiler compiler;

		compiler = new Compiler(getSrcDir(), binDir, libDir, packageName.replace(".", "/"), errorLog);
		compiler.addLibs(libs);
		libs = new String[] {};

		boolean compiled = compiler.compile();

		if (!compiled) {
			logger.error("Impossible to compile mapping code. Please download compiler errors log");
			throw new GenerationException("Impossible to compile mapping code. Please download errors log");
		}

		FileUtilities.copyFile(new File(srcDir, "views.json"), binDir);
		FileUtilities.copyFile(new File(srcDir, "label.properties"), binDir);
		FileUtilities.copyFile(new File(srcDir, "qbe.properties"), binDir);
		FileUtilities.copyFile(new File(srcDir, "relationships.json"), binDir);
		FileUtilities.copyFile(new File(srcDir, "cfields_meta.xml"), binDir);
		if (new File(srcDir + "" + File.separator + "hierarchies.xml").exists()) {
			FileUtilities.copyFile(new File(srcDir, "hierarchies.xml"), binDir);
		}

		if (fileModel != null) {
			// model file is copied inside bin folder so that it will be included in jar
			String sbimodelName = binDir + File.separator + o.getName() + "." + SBI_MODEL_FILE_NAME;
			File sbimodel = new File(sbimodelName);
			try {
				FileUtils.writeByteArrayToFile(sbimodel, fileModel);
			} catch (IOException e) {
				throw new GenerationException("Error writing content to file [" + sbimodel + "]", e);
			}
		}

		FileUtilities.copyFile(new File(srcDir, "META-INF/persistence.xml"), new File(binDir, "META-INF"));

		if (includeSources) {
			try {
				// copy sources files to binDir (to include it in the jar)
				FileUtils.copyDirectory(srcDir, binDir);
			} catch (IOException e) {
				logger.error("Error copying source files to bin directory", e);
			}
		}

		// } catch (Throwable t) {
		// logger.error("An error occur while generating JPA jar", t);
		// throw new GenerationException("An error occur while generating JPA jar", t);
		// } finally {
		// logger.trace("OUT");
		// }
	}

	// =======================================================================
	// ACCESSOR METHODS
	// =======================================================================

	public File getBinDir() {
		return binDir;
	}

	public void setBinDir(File binDir) {
		this.binDir = binDir;
	}

	public void setLibDir(File libDir) {
		this.libDir = libDir;
	}

	public File getLibDir() {
		return libDir;
	}

	public void setLibs(String[] libs) {
		this.libs = libs;
	}

	/**
	 * @param errorLog
	 *            the errorLog to set
	 */
	public void setErrorLog(PrintWriter errorLog) {
		this.errorLog = errorLog;
	}
}
