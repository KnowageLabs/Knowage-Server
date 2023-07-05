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
package it.eng.spagobi.tools.importexport.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.tools.importexport.bo.AssociationFile;

public class AssociationFileDAO implements IAssociationFileDAO {

	private static final Logger LOGGER = Logger.getLogger(AssociationFileDAO.class);
	private static final String ASS_DIRECTORY = "Repository_Association_Files";

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO#loadFromID(java.lang.String)
	 */
	@Override
	public AssociationFile loadFromID(String id) {
		LOGGER.debug("IN");
		AssociationFile assFile = null;
		try {
			File fileAssRepDir = getFileOfAssRepDir();
			String pathBaseDirAss = fileAssRepDir.getPath() + "/" + id;
			String pathprop = pathBaseDirAss + "/association.properties";
			Properties props = new Properties();
			try (FileInputStream fis = new FileInputStream(pathprop)) {
				props.load(fis);
			}
			assFile = new AssociationFile();
			assFile.setName(props.getProperty("name"));
			assFile.setDescription(props.getProperty("description"));
			assFile.setId(props.getProperty("id"));
			assFile.setDateCreation(Long.parseLong(props.getProperty("creationDate")));
		} catch (Exception e) {
			LOGGER.error("Error while loading association file with id " + id + ", ", e);
			assFile = null;
		} finally {
			LOGGER.debug("OUT");
		}
		return assFile;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO#saveAssociationFile(it.eng.spagobi.tools.importexport.bo.AssociationFile, byte[])
	 */
	@Override
	public void saveAssociationFile(AssociationFile assfile, byte[] content) {
		LOGGER.debug("IN");
		try {
			String uuid = assfile.getId();
			File fileAssRepDir = getFileOfAssRepDir();
			String pathBaseAssFile = fileAssRepDir.getAbsolutePath() + "/" + uuid;
			File baseAssFile = new File(pathBaseAssFile);
			baseAssFile.mkdirs();
			String pathXmlAssFile = pathBaseAssFile + "/association.xml";
			String pathPropAssFile = pathBaseAssFile + "/association.properties";
			String properties = null;
			try (FileOutputStream fos = new FileOutputStream(pathXmlAssFile); ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
				SpagoBIUtilities.flushFromInputStreamToOutputStream(bais, fos, true);
				properties = "id=" + assfile.getId() + "\n";
				properties += "name=" + assfile.getName() + "\n";
				properties += "description=" + assfile.getDescription() + "\n";
				properties += "creationDate=" + assfile.getDateCreation() + "\n";
			}
			try (FileOutputStream fos = new FileOutputStream(pathPropAssFile)) {
				fos.write(properties.getBytes());
				fos.flush();
			}
		} catch (Exception e) {
			LOGGER.error("Error while saving association file, ", e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO#exists(java.lang.String)
	 */
	@Override
	public boolean exists(String id) {
		LOGGER.debug("IN");
		try {
			File fileAssRepDir = getFileOfAssRepDir();
			String pathBaseAssFile = fileAssRepDir.getAbsolutePath() + "/" + id;
			File baseAssFile = new File(pathBaseAssFile);
			// if the folder exists the association file exists
			// if a file with the same name exists then tries to delete it
			if (baseAssFile.exists()) {
				if (baseAssFile.isDirectory())
					return true;
				else {
					if (baseAssFile.delete())
						return false;
					else
						return true;
				}
			} else
				return false;
		} finally {
			LOGGER.debug("OUT");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO#deleteAssociationFile(it.eng.spagobi.tools.importexport.bo.AssociationFile)
	 */
	@Override
	public void deleteAssociationFile(AssociationFile assfile) {
		LOGGER.debug("IN");
		try {
			File fileAssRepDir = getFileOfAssRepDir();
			String pathBaseDirAss = fileAssRepDir.getPath() + "/" + assfile.getId();
			File fileBaseDirAss = new File(pathBaseDirAss);
			SpagoBIUtilities.deleteDir(fileBaseDirAss);
		} catch (Exception e) {
			LOGGER.error("Error while deleting association file, ", e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO#getAssociationFiles()
	 */
	@Override
	public List<AssociationFile> getAssociationFiles() {
		LOGGER.debug("IN");
		List<AssociationFile> assFiles = new ArrayList<>();
		try {
			File fileAssRepDir = getFileOfAssRepDir();
			File[] assBaseDirs = fileAssRepDir.listFiles();
			for (int i = 0; i < assBaseDirs.length; i++) {
				File assBaseDir = assBaseDirs[i];
				try {
					if (assBaseDir.isDirectory()) {
						String pathprop = assBaseDir.getPath() + "/association.properties";
						Properties props = new Properties();
						try (FileInputStream fis = new FileInputStream(pathprop)) {
							props.load(fis);
						}
						AssociationFile assFile = new AssociationFile();
						assFile.setName(props.getProperty("name"));
						assFile.setDescription(props.getProperty("description"));
						assFile.setId(props.getProperty("id"));
						assFile.setDateCreation(Long.parseLong(props.getProperty("creationDate")));
						assFiles.add(assFile);
					}
				} catch (Exception e) {
					LOGGER.error("Error while recovering info of the ass file with" + "id " + assBaseDir.getName() + "\n , ", e);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error while getting association file list, ", e);
		} finally {
			LOGGER.debug("OUT");
		}
		return assFiles;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO#getContent(it.eng.spagobi.tools.importexport.bo.AssociationFile)
	 */
	@Override
	public byte[] getContent(AssociationFile assfile) {
		LOGGER.debug("IN");
		byte[] byts = new byte[0];
		try {
			File fileAssRepDir = getFileOfAssRepDir();
			String pathBaseDirAss = fileAssRepDir.getPath() + "/" + assfile.getId();
			String pathAssFile = pathBaseDirAss + "/association.xml";
			try (FileInputStream fis = new FileInputStream(pathAssFile)) {
				byts = SpagoBIUtilities.getByteArrayFromInputStream(fis);
			}
		} catch (Exception e) {
			LOGGER.error("Error while getting content of association file with id " + assfile.getId() + ",\n ", e);
		} finally {
			LOGGER.debug("OUT");
		}
		return byts;
	}

	private File getFileOfAssRepDir() {
		LOGGER.debug("IN");
		File assrepdirFile = null;
		try {
			ConfigSingleton conf = ConfigSingleton.getInstance();
			SourceBean assRepo = (SourceBean) conf.getAttribute("IMPORTEXPORT.ASSOCIATIONS_REPOSITORY");
			String assRepoPath = (String) assRepo.getAttribute("path");
			if (!assRepoPath.startsWith("/")) {
				String pathcont = ConfigSingleton.getRootPath();
				assRepoPath = pathcont + "/" + assRepoPath;
			}
			// check if the file already exists and, if not, create the directory
			assrepdirFile = new File(assRepoPath);
			assrepdirFile.mkdirs();
		} catch (Exception e) {
			LOGGER.error("Error wile getting the associations repository dir file, ", e);
		} finally {
			LOGGER.debug("OUT");
		}
		return assrepdirFile;
	}

}
