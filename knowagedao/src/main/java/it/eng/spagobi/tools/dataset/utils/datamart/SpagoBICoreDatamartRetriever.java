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
package it.eng.spagobi.tools.dataset.utils.datamart;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class SpagoBICoreDatamartRetriever implements IQbeDataSetDatamartRetriever {

	private static final Logger LOGGER = Logger.getLogger(SpagoBICoreDatamartRetriever.class);

	public File getDataMartDir() {
		String baseDirStr = SpagoBIUtilities.getResourcePath();
		File baseDir = new File(baseDirStr);
		String completePath = baseDir + File.separator + File.separator + "qbe" + File.separator + "datamarts";
		File qbeDataMartDir = new File(completePath);

		if (qbeDataMartDir.exists() && !qbeDataMartDir.isDirectory()) {
			throw new SpagoBIRuntimeException("Path [" + completePath + "] refers to a file.");
		}
		if (!qbeDataMartDir.exists()) {
			boolean created = qbeDataMartDir.mkdirs();
			if (!created) {
				throw new SpagoBIRuntimeException("Cannot create folder [" + completePath + "].");
			}
		}
		return qbeDataMartDir;
	}

	@Override
	public File retrieveDatamartFile(String metamodelName) {

		File metamodelJarFile;

		LOGGER.debug("IN");

		metamodelJarFile = null;
		try {
			Assert.assertTrue(StringUtilities.isNotEmpty(metamodelName), "Input parameter [metamodelName] cannot be null");
			LOGGER.debug("Load metamodel jar file for model [" + metamodelName + "]");

			File targetMetamodelFolder = new File(getDataMartDir(), metamodelName);
			metamodelJarFile = new File(targetMetamodelFolder, "datamart.jar");

			IMetaModelsDAO metamodelsDAO = DAOFactory.getMetaModelsDAO();

			if (metamodelJarFile.exists()) {
				LOGGER.debug("jar file for metamodel [" + metamodelName + "] has been already loaded in folder [" + targetMetamodelFolder + "]");
				long localVersionLastModified = metamodelJarFile.lastModified();
				long remoteVersionLastModified = metamodelsDAO.getActiveMetaModelContentLastModified(metamodelName);
				if (localVersionLastModified < remoteVersionLastModified) {
					downloadJarFile(metamodelName, targetMetamodelFolder);
				}
			} else {
				LOGGER.debug("jar file for metamodel [" + metamodelName + "] has not been already downloaded");
				downloadJarFile(metamodelName, targetMetamodelFolder);
			}

			Assert.assertTrue(metamodelJarFile.exists(), "After load opertion file [" + metamodelJarFile + "] must exist");
		} catch (SpagoBIEngineRuntimeException e) {
			throw e;
		} catch (Throwable t) {
			throw new SpagoBIEngineRuntimeException("An unexpected error occured while loading metamodel's jar file", t);
		}

		return metamodelJarFile;
	}

	/**
	 * Download the jarFile from SpagoBI metadata repository and store it on the local filesystem in the specified folder
	 *
	 * @param metamodelName
	 *            the name of the metamodel to download
	 * @param destinationFolder
	 *            the destination folder on the local filesystem
	 */
	private void downloadJarFile(String metamodelName, File destinationFolder) {
		Content content = null;
		try {
			IMetaModelsDAO metamodelsDAO = DAOFactory.getMetaModelsDAO();
			LOGGER.debug("Loading jar file for metamodel [" + metamodelName + "] from SpagoBI metadata repository ...");
			content = metamodelsDAO.loadActiveMetaModelContentByName(metamodelName);
			if (content == null)
				throw new SpagoBIEngineRuntimeException("Metamodel [" + metamodelName + "] not found");
			LOGGER.debug("jar file for metamodel [" + metamodelName + "] has been loaded succesfully from SpagoBI metadata repository");
		} catch (Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load jar file of metamodel [" + metamodelName + "] from repository", t);
		}

		LOGGER.debug("Copying jar file of metamodel [" + metamodelName + "] locally into folder [" + destinationFolder + "] ...");
		storeJarFile(content, destinationFolder);
		LOGGER.debug("jar file of metamodel [" + metamodelName + "] succesfully copied locally into folder [" + destinationFolder + "] ...");
	}

	/**
	 * Store the jarFile on local filesystem
	 *
	 * @param content
	 *            the jarFile content
	 * @param destinationFolder
	 *            the destination folder on the local filesystem
	 */
	private void storeJarFile(Content content, File destinationFolder) {
		File metamodelJarFile = new File(destinationFolder, "datamart.jar");

		if (metamodelJarFile.exists()) {
			metamodelJarFile.delete();
		}

		if (!destinationFolder.exists())
			destinationFolder.mkdirs();

		try (FileOutputStream fos = new FileOutputStream(metamodelJarFile);) {
			byte[] cont = content.getContent();
			fos.write(cont);
			fos.flush();
		} catch (Throwable t) {
			throw new SpagoBIEngineRuntimeException("An unexpected error occured while saving localy metamodel's jar file", t);
		}
	}

	public boolean isAJPADatamartJarFile(File metamodelJarFile) {
		ZipEntry zipEntry;

		try (ZipFile zipFile = new ZipFile(metamodelJarFile)) {
			zipEntry = zipFile.getEntry("META-INF/persistence.xml");
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to read jar file [" + metamodelJarFile + "]");
		}

		return zipEntry != null;
	}

}
