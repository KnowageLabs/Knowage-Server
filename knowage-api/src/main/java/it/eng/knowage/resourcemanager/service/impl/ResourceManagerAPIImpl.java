/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.resourcemanager.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.error.KnowageRuntimeException;
import it.eng.knowage.knowageapi.utils.ContextPropertiesConfig;
import it.eng.knowage.resourcemanager.resource.utils.FileDTO;
import it.eng.knowage.resourcemanager.resource.utils.FolderDTO;
import it.eng.knowage.resourcemanager.resource.utils.RootFolderDTO;
import it.eng.knowage.resourcemanager.service.ResourceManagerAPI;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Component
public class ResourceManagerAPIImpl implements ResourceManagerAPI {
	private static final Logger LOGGER = Logger.getLogger(ResourceManagerAPIImpl.class);
	int count = 0;
	private static Map<String, List<String>> foldersForDevs = new HashMap<>();

	static {
		List<String> folders = new ArrayList<String>();
		folders.add("MODELS");
		folders.add("TALEND");
		folders.add("STATIC_MENU");
		folders.add("LAYER");
		folders.add("jasper_messages");
		folders.add("hierarchies");
		folders.add("birt_messages");
		folders.add("dataset");
		foldersForDevs.put("DEV", folders);
		foldersForDevs.put("TEST", folders);
	}

	@Override
	public RootFolderDTO getFolders(SpagoBIUserProfile profile, String path) {

		String totalPath = getWorkDirectory(profile);
		Path totalF = Paths.get(totalPath);
		FolderDTO parentFolder = new FolderDTO(totalPath);
		FolderDTO mylist = null;
		RootFolderDTO newRootFolder = null;

		LOGGER.debug("Starting resource path json tree testing");
		try {
			if (path == null)
				path = totalPath;
			Path f = Paths.get(path);
			mylist = createTree(parentFolder);
			parseFolders(mylist);
			clearFolders(mylist, f.getParent().toString());
			newRootFolder = new RootFolderDTO(mylist);
			String rootFolder = totalF.toString().replace(f.getParent().toString(), "");
			newRootFolder.getRoot().setLabel(rootFolder);
			boolean canSee = canSee(f.getParent().toString(), profile);

		} catch (IOException e) {
			throw new KnowageRuntimeException(e.getMessage());
		}
		return newRootFolder;
	}

	private void parseFolders(FolderDTO e) {
		setFolderLevel(e);
	}

	private void setFolderLevel(FolderDTO e) {
		e.setKey("" + count);
		// count = 0;
		count++;
		if (e.getChildren() != null && e.getChildren().size() > 0) {
			// lvl++;
			for (FolderDTO emp : e.getChildren()) {
				setFolderLevel(emp);
			}
		}
	}

	public String getWorkDirectory(SpagoBIUserProfile profile) {
		String resourcePathBase = ContextPropertiesConfig.getResourcePath();
		String tenant = profile.getOrganization();
		String totalPath = resourcePathBase + File.separator + tenant;
		return totalPath;
	}

	private void clearFolders(FolderDTO e, String path) {
		changeFolderPath(e, path);
	}

	private void changeFolderPath(FolderDTO e, String path) {
		e.setLabel(e.getLabel().replace(path, ""));
		if (e.getChildren() != null && e.getChildren().size() > 0) {
			for (FolderDTO emp : e.getChildren()) {
				changeFolderPath(emp, path);
			}
		}
	}

	public FolderDTO createTree(FolderDTO parentFolder) throws IOException {
		File node = new File(parentFolder.getLabel());
		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				String path = node + "\\" + filename;
				if (new File(path).isDirectory()) {
					FolderDTO folder = new FolderDTO(path);
					parentFolder.addChildren(folder);
					createTree(folder);
				} else {
					// parentFolder.addFile(new CustomFile(path));
				}
			}
		}
		return parentFolder;
	}

	@Override
	public boolean canSee(String path, SpagoBIUserProfile profile) {
		boolean canSee = false;
		if (profile.isIsSuperadmin() || hasAdministratorRole(profile)) {
			return true;
		} else {
			for (String function : profile.getFunctions()) {
				if (function.equalsIgnoreCase("RESOURCE_FUNCTION")) {
					canSee = true;
				}
			}
		}

		return canSee;
	}

	public static boolean hasAdministratorRole(SpagoBIUserProfile profile) {

		try {
			for (String role : profile.getRoles()) {
				if (role.equals("admin")) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {

		}

		return false;
	}

	// if user can't work with directory it is not necessary
	// if (canSee(pathToWork, profile)) ...
	@Override
	public boolean createFolder(String path, SpagoBIUserProfile profile) {
		String totalPath = getTotalPath(path, profile);
		File file = new File(totalPath);
		// Creating the directory
		boolean bool = file.mkdir();
		if (bool) {
			LOGGER.info("Directory created successfully");
		} else {
			LOGGER.info("Sorry couldn’t create specified directory");
		}

		return bool;
	}

	@Override
	public boolean delete(String path, SpagoBIUserProfile profile) {
		String totalPath = getTotalPath(path, profile);
		File file = new File(totalPath);
		if (file.isDirectory()) {
			try {
				FileUtils.deleteDirectory(new File(totalPath));
			} catch (IOException e) {
				throw new KnowageRuntimeException(e.getMessage());
			}
		} else {
			try {
				FileUtils.forceDelete(file);
			} catch (IOException e) {
				throw new KnowageRuntimeException(e.getMessage());
			}
		}
		return false;
	}

	@Override
	public Path getDownloadPath(String path, SpagoBIUserProfile profile) {
		String workDir = getWorkDirectory(profile);
		java.nio.file.Path pathToReturn = null;
		String directoryFullPath = workDir + File.separator + path;

		pathToReturn = Paths.get(directoryFullPath);

		return pathToReturn;
	}

	@Override
	public List<FileDTO> getListOfFiles(String path, SpagoBIUserProfile profile) {
		String totalPath = getTotalPath(path, profile);
		File folder = new File(totalPath);
		File[] listOfFiles = folder.listFiles();
		List<FileDTO> returnList = new ArrayList<FileDTO>();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				LOGGER.debug("File " + listOfFiles[i].getName());
				returnList.add(new FileDTO(listOfFiles[i].getName()));
			}
		}
		return returnList;
	}

	public String getTotalPath(String path, SpagoBIUserProfile profile) {
		String pathToWork = getWorkDirectory(profile);
		String totalPath = pathToWork + File.separator + path;
		return totalPath;
	}

}
