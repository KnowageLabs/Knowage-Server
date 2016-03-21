/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.knowage.meta.generator.jpamapping;

import it.eng.knowage.meta.generator.GenerationException;
import it.eng.knowage.meta.generator.jpamapping.wrappers.JpaProperties;
import it.eng.knowage.meta.generator.utils.Compiler;
import it.eng.spagobi.meta.model.ModelObject;
import it.eng.spagobi.meta.model.business.BusinessModel;

import java.io.File;

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

	public static final String DEFAULT_BIN_DIR = "build";
	public static final String DEFAULT_LIB_DIR = "libs";

	private String[] libs = { "org.eclipse.persistence.core_2.1.1.v20100817-r8050.jar", "javax.persistence_2.0.1.v201006031150.jar",
			"hibernate-spatial-1.1.1.jar", "jts-1.13.jar", "hibernate3.6.2.jar" };

	private static Logger logger = LoggerFactory.getLogger(JpaMappingClassesGenerator.class);

	public JpaMappingClassesGenerator() {
		super();
	}

	@Override
	public void generate(ModelObject o, String outputDir) {
		generate(o, outputDir, false);
	}

	@Override
	public void generate(ModelObject o, String outputDir, boolean isUpdatableMapping) {
		logger.trace("IN");

		try {
			BusinessModel model;

			super.generate(o, outputDir, isUpdatableMapping);

			binDir = (binDir == null) ? new File(outputDir, DEFAULT_BIN_DIR) : binDir;
			logger.debug("src dir is equal to [{}]", getSrcDir());
			libDir = (libDir == null) ? new File(outputDir, DEFAULT_LIB_DIR) : libDir;
			logger.debug("lib dir is equal to [{}]", libDir);

			model = (BusinessModel) o;

			// Get Package Name
			String packageName = model.getProperties().get(JpaProperties.MODEL_PACKAGE).getValue();

			// Call Java Compiler
			Compiler compiler;

			compiler = new Compiler(getSrcDir(), binDir, libDir, packageName.replace(".", "/"));
			compiler.addLibs(libs);

			boolean compiled = compiler.compile();

			if (!compiled) {
				throw new GenerationException(
						"Impossible to compile mapping code. Please check compilation errors in file [/log/spagobi/metacompiler_errors.log]");
			}

			FileUtilities.copyFile(new File(srcDir, "views.json"), binDir);
			FileUtilities.copyFile(new File(srcDir, "label.properties"), binDir);
			FileUtilities.copyFile(new File(srcDir, "qbe.properties"), binDir);
			FileUtilities.copyFile(new File(srcDir, "relationships.json"), binDir);
			FileUtilities.copyFile(new File(srcDir, "cfields_meta.xml"), binDir);
			FileUtilities.copyFile(new File(srcDir, "hierarchies.xml"), binDir);
			FileUtilities.copyFile(new File(srcDir, "META-INF/persistence.xml"), new File(binDir, "META-INF"));

		} catch (Throwable t) {
			logger.error("An error occur while generating JPA jar", t);
			throw new GenerationException("An error occur while generating JPA jar", t);
		} finally {
			logger.trace("OUT");
		}
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
}
