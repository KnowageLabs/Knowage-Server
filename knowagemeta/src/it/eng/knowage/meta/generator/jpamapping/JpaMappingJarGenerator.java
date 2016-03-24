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
import it.eng.knowage.meta.generator.utils.Zipper;
import it.eng.knowage.meta.model.ModelObject;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JpaMappingJarGenerator extends JpaMappingClassesGenerator {

	private File distDir;
	private String jarFileName;
	private org.eclipse.core.internal.resources.File modelFile;

	public static final String DEFAULT_DIST_DIR = "dist";
	public static final String DEFAULT_JAR_FILE_NAME = "datamart.jar";

	private static Logger logger = LoggerFactory.getLogger(JpaMappingJarGenerator.class);

	public JpaMappingJarGenerator() {
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
			// The output dir is the model directory plus the business model name
			outputDir = outputDir + File.separator + o.getName();
			super.generate(o, outputDir, isUpdatableMapping);

			distDir = (distDir == null) ? new File(baseOutputDir, DEFAULT_DIST_DIR) : distDir;
			jarFileName = (jarFileName == null) ? DEFAULT_JAR_FILE_NAME : jarFileName;

			// if model file is selected copy model file inside bin folder so that it will be included in jar
			if (getModelFile() != null) {
				File javaFile = getModelFile().getRawLocation().toFile();
				FileUtilities.copyFile(javaFile, getBinDir());
			}
			// zip bin folder into jar
			Zipper zipper = new Zipper();
			zipper.compressToJar(getBinDir(), getJarFile());

		} catch (Throwable t) {
			logger.error("An error occur while generating JPA jar", t);
			throw new GenerationException("An error occur while generating JPA jar", t);
		} finally {
			logger.trace("OUT");
		}
	}

	/**
	 * Commented for porting META WEB
	 */
	/*
	 * @Override public void hideTechnicalResources() { logger.debug("IN"); super.hideTechnicalResources();
	 *
	 * File baseOutputDir = getBaseOutputDir(); File distDir = getDistDir(); IWorkspace workspace = ResourcesPlugin.getWorkspace(); try { if (baseOutputDir !=
	 * null && baseOutputDir.exists()) { IProject proj = workspace.getRoot().getProject(baseOutputDir.getParentFile().getParentFile().getName()); IFolder
	 * iFolder = proj.getFolder(baseOutputDir.getParentFile().getName() + "\\" + baseOutputDir.getName()); if (iFolder.exists()) { iFolder.setHidden(true);
	 * iFolder.setTeamPrivateMember(true); iFolder.setDerived(true, null); } if (distDir != null && distDir.exists()) { IFolder iFolderDist =
	 * proj.getFolder(baseOutputDir.getParentFile().getName() + "\\" + baseOutputDir.getName() + "\\" + distDir.getName()); if (iFolderDist.exists()) {
	 * iFolderDist.setHidden(true); iFolderDist.setDerived(true, null); IFolder iFolderSource = proj.getFolder(baseOutputDir.getParentFile().getName() + "\\" +
	 * baseOutputDir.getName() + "\\" + JpaMappingCodeGenerator.DEFAULT_SRC_DIR); iFolderSource.setHidden(true); iFolderSource.setDerived(true, null); } if
	 * (iFolder.exists() || iFolderDist.exists()) { workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
	 * proj.refreshLocal(IResource.DEPTH_INFINITE, null); } } else { logger.warn("Exception occurred before creating distDir: no resource to hide"); }
	 *
	 * } else { logger.warn("Exception occurred before creating baseoutputDir: no resource to hide"); }
	 *
	 * } catch (CoreException e) { logger.error("Error in hiding technical model folders ", e); throw new
	 * GenerationException("Error in hiding technical model folders", e); } logger.debug("OUT");
	 *
	 * }
	 */

	// =======================================================================
	// ACCESSOR METHODS
	// =======================================================================

	@Override
	public File getDistDir() {
		return distDir;
	}

	@Override
	public void setDistDir(File distDir) {
		this.distDir = distDir;
	}

	public File getJarFile() {
		return new File(distDir, jarFileName);
	}

	public String getJarFileName() {
		return jarFileName;
	}

	public void setJarFileName(String jarFileName) {
		this.jarFileName = jarFileName;
	}

	public org.eclipse.core.internal.resources.File getModelFile() {
		return modelFile;
	}

	public void setModelFile(org.eclipse.core.internal.resources.File modelFile) {
		this.modelFile = modelFile;
	}

}
