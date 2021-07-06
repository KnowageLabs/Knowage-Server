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

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	private PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*");

	@Autowired
	private ObjectMapper objectMapper;

	static {
		List<String> folders = new ArrayList<String>();
		folders.add("models");
		folders.add("talend");
		folders.add("static_menu");
		folders.add("layer");
		folders.add("jasper_messages");
		folders.add("hierarchies");
		folders.add("birt_messages");
		folders.add("dataset");
		foldersForDevs.put("DEV", folders);
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
			mylist = createTree(parentFolder, profile);
			parseFolders(mylist);
			clearFolders(mylist, f.getParent().toString());
			newRootFolder = new RootFolderDTO(mylist);
			String rootFolder = totalF.toString().replace(f.getParent().toString(), "");
			newRootFolder.getRoot().setLabel(rootFolder);

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

	public FolderDTO createTree(FolderDTO parentFolder, SpagoBIUserProfile profile) throws IOException {
		File node = new File(parentFolder.getLabel());
		Path nodePath = Paths.get(node.getAbsolutePath());
		Path workDir = Paths.get(getWorkDirectory(profile));
		boolean canSee = true;
		if (nodePath.getParent().equals(workDir)) {
			if (!canSee(nodePath, profile)) {
				canSee = false;
			}
		}
		if (node.isDirectory() && canSee) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				String path = node + "\\" + filename;
				Path pathNode = Paths.get(path);
				boolean canSeeNode = true;
				if (pathNode.getParent().equals(workDir)) {
					if (!canSee(pathNode, profile)) {
						canSeeNode = false;
					}
				}
				if (new File(path).isDirectory() && canSeeNode) {
					FolderDTO folder = new FolderDTO(path);
					parentFolder.addChildren(folder);
					createTree(folder, profile);
				} else {
					// parentFolder.addFile(new CustomFile(path));
				}
			}
		}
		return parentFolder;
	}

	@Override
	public boolean canSee(Path path, SpagoBIUserProfile profile) {
		boolean canSee = false;
		if (profile.isIsSuperadmin() || hasAdministratorRole(profile)) {
			return true;
		} else {
			for (String function : profile.getFunctions()) {
				// functionality must be present
//				if (function.equalsIgnoreCase("RESOURCE_FUNCTION")) {
//					canSee = true;
//				}
			}
			if (hasDevRole(profile)) {
				if (foldersForDevs.get("DEV").contains(path.getFileName().toString())) {
					return true;
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
			throw new KnowageRuntimeException(e.getMessage());
		}
	}

	public static boolean hasDevRole(SpagoBIUserProfile profile) {
		try {
			for (String role : profile.getRoles()) {
				if (role.equals("dev")) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage());
		}
	}

	// if user can't work with directory it is not necessary
	// if (canSee(pathToWork, profile)) ...
	@Override
	public boolean createFolder(String path, SpagoBIUserProfile profile) {
		String totalPath = getTotalPath(path, profile);
		boolean bool = false;
		String workDir = getWorkBaseDirByPath(path, profile);
		if (canSee(Paths.get(workDir), profile)) {
			File file = new File(totalPath);
			// Creating the directory
			bool = file.mkdir();
			if (bool) {
				LOGGER.info("Directory created successfully");
			} else {
				LOGGER.info("Sorry couldn’t create specified directory");
			}
		}
		return bool;
	}

	@Override
	public boolean delete(String path, SpagoBIUserProfile profile) {
		String totalPath = getTotalPath(path, profile);
		boolean bool = false;
		String workDir = getWorkBaseDirByPath(path, profile);
		if (canSee(Paths.get(workDir), profile)) {
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
		}
		return false;
	}

	@Override
	public Path getDownloadFolderPath(String path, SpagoBIUserProfile profile) {
		String workDirr = getWorkBaseDirByPath(path, profile);
		java.nio.file.Path workingPath = null;
		java.nio.file.Path pathToReturn = null;
		if (canSee(Paths.get(workDirr), profile)) {
			String workDir = getWorkDirectory(profile);
			String directoryFullPath = workDir + File.separator + path;

			workingPath = Paths.get(directoryFullPath);

			pathToReturn = createZipFile(path, workingPath);
		}
		return pathToReturn;
	}

	@Override
	public Path getDownloadFilePath(List<String> path, SpagoBIUserProfile profile, boolean multi) {
		java.nio.file.Path pathToReturn = null;
		if (multi) {

		} else {
			String pathFile = path.get(0);
			String workDirr = getWorkBaseDirByPath(pathFile, profile);

			if (canSee(Paths.get(workDirr), profile)) {
				String workDir = getWorkDirectory(profile);
				pathToReturn = Paths.get(workDir + File.separator + path);
			}
		}
		return pathToReturn;
	}

	public String getWorkBaseDirByPath(String path, SpagoBIUserProfile profile) {
		String rootElement = path.split("/")[0];
		String rootPath = getTotalPath(rootElement, profile);
		String workDirr = Paths.get(rootPath).toString();
		return workDirr;
	}

	@Override
	public List<FileDTO> getListOfFiles(String path, SpagoBIUserProfile profile) {
		String totalPath = getTotalPath(path, profile);
		File folder = new File(totalPath);
		File[] listOfFiles = folder.listFiles();
		List<FileDTO> returnList = new ArrayList<FileDTO>();
		String workDir = getWorkBaseDirByPath(path, profile);
		if (canSee(Paths.get(workDir), profile)) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					LOGGER.debug("File " + listOfFiles[i].getName());
					returnList.add(new FileDTO(listOfFiles[i].getName()));
				}
			}
		}
		return returnList;
	}

	public String getTotalPath(String path, SpagoBIUserProfile profile) {
		String pathToWork = getWorkDirectory(profile);
		String totalPath = pathToWork + File.separator + path;
		return totalPath;
	}

	public Path createZipFile(String fileName, java.nio.file.Path fullPath) {

		try {
			Path tempDirectory = Files.createTempDirectory("knowage-zip");
			Path tempFile = Files.createTempFile("knowage-zip", fileName);

			try (ZipOutputStream ret = new ZipOutputStream(Files.newOutputStream(tempFile))) {

				File fileDest = new File(tempDirectory.toString() + File.separator + fileName);
				FileUtils.copyDirectory(fullPath.toFile(), fileDest);
				List<Path> files = Files.walk(tempDirectory).collect(toList());

				for (Path currPath : files) {

					Path relativize = tempDirectory.relativize(currPath);
					if (Files.isDirectory(currPath)) {
						ZipEntry zipEntry = new ZipEntry(relativize.toString() + "/");
						ret.putNextEntry(zipEntry);
					} else {
						ZipEntry zipEntry = new ZipEntry(relativize.toString());
						ret.putNextEntry(zipEntry);
						InputStream currPathInputStream = Files.newInputStream(currPath);
						copy(currPathInputStream, ret);
					}
					ret.closeEntry();
				}
			}

			cleanUpTempDirectory(tempDirectory);

			return tempFile;

		} catch (IOException e) {
			throw new KnowageRuntimeException("Error creating export ZIP archive", e);
		}
	}

	private void copy(InputStream source, OutputStream target) throws IOException {
		byte[] buf = new byte[8192];
		int length;
		while ((length = source.read(buf)) > 0) {
			target.write(buf, 0, length);
		}
	}

	private void cleanUpTempDirectory(Path tempDirectory) throws IOException {
		// Common way to delete recursively
		Files.walk(tempDirectory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}
}
