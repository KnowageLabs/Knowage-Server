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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.knowage.knowageapi.error.KNRM001Exception;
import it.eng.knowage.knowageapi.error.KnowageRuntimeException;
import it.eng.knowage.knowageapi.utils.ContextPropertiesConfig;
import it.eng.knowage.resourcemanager.resource.dto.FileDTO;
import it.eng.knowage.resourcemanager.resource.dto.FolderDTO;
import it.eng.knowage.resourcemanager.resource.dto.MetadataDTO;
import it.eng.knowage.resourcemanager.resource.dto.RootFolderDTO;
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
	public RootFolderDTO getFolders(SpagoBIUserProfile profile, String path) throws KNRM001Exception {
		String totalPath = null;
		try {
			totalPath = getWorkDirectory(profile);
		} catch (KNRM001Exception k) {
			throw new KNRM001Exception("");
		}
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
			LOGGER.error("[ResourceManagerAPIImpl], [getFolders], ", e);
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

	public String getWorkDirectory(SpagoBIUserProfile profile) throws KNRM001Exception {
		String resourcePathBase = ContextPropertiesConfig.getResourcePath();
		String tenant = profile.getOrganization();
		String totalPath = resourcePathBase + File.separator + tenant;
		Path dirPath = Paths.get(totalPath);
		if (!Files.exists(dirPath)) {
			throw new KNRM001Exception("");
		}
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

	public FolderDTO createTree(FolderDTO parentFolder, SpagoBIUserProfile profile) throws IOException, KNRM001Exception {
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
				// if (function.equalsIgnoreCase("RESOURCE_FUNCTION")) {
				// canSee = true;
				// }
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
	public boolean createFolder(String path, SpagoBIUserProfile profile) throws KNRM001Exception {
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
	public boolean delete(String path, SpagoBIUserProfile profile) throws KNRM001Exception {
		String totalPath = getTotalPath(path, profile);
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
		return true;
	}

	@Override
	public Path getDownloadFolderPath(String path, SpagoBIUserProfile profile) throws KNRM001Exception {
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
	public Path getDownloadFilePath(List<String> path, SpagoBIUserProfile profile, boolean multi) throws KNRM001Exception {
		java.nio.file.Path pathToReturn = null;
		if (multi) {
			pathToReturn = createZipFileOfFiles(path, profile);
		} else {
			String pathFile = path.get(0);
			String workDirr = getWorkBaseDirByPath(pathFile, profile);

			if (canSee(Paths.get(workDirr), profile)) {
				String workDir = getWorkDirectory(profile);
				pathToReturn = Paths.get(workDir + File.separator + pathFile);
			}
		}
		return pathToReturn;
	}

	public String getWorkBaseDirByPath(String path, SpagoBIUserProfile profile) throws KNRM001Exception {
		String rootElement = path.split("/")[0];
		String rootPath = getTotalPath(rootElement, profile);
		String workDirr = Paths.get(rootPath).toString();
		return workDirr;
	}

	@Override
	public List<FileDTO> getListOfFiles(String path, SpagoBIUserProfile profile) throws KNRM001Exception {
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

	@Override
	public void importFile(InputStream archiveInputStream, String path, SpagoBIUserProfile profile) throws IOException, KNRM001Exception {

		String workDirr = getWorkBaseDirByPath(path, profile);
		Path filePath = Paths.get(getTotalPath(path, profile));

		if (canSee(Paths.get(workDirr), profile)) {
			Files.createFile(filePath);
			Path tempArchive = filePath.getParent().resolve(filePath.getFileName());
			FileUtils.copyInputStreamToFile(archiveInputStream, tempArchive.toFile());
		}
	}

	@Override
	public void importFileAndExtract(InputStream archiveInputStream, String path, SpagoBIUserProfile profile) throws IOException, KNRM001Exception {

		String workDirr = getWorkBaseDirByPath(path, profile);
		String totalPath = getTotalPath(path, profile);
		Path filePath = Paths.get(totalPath);
		String totalPathDestinationDir = filePath.getParent().toFile().getAbsolutePath();
		if (canSee(Paths.get(workDirr), profile)) {
			Files.createFile(filePath);
			Path tempArchive = filePath.getParent().resolve(filePath.getFileName());
			File zipFile = tempArchive.toFile();
			FileUtils.copyInputStreamToFile(archiveInputStream, zipFile);
			extractFolder(zipFile.getAbsolutePath(), totalPathDestinationDir);
		}

	}

	public String getTotalPath(String path, SpagoBIUserProfile profile) throws KNRM001Exception {
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

	public Path createZipFileOfFiles(List<String> fullPaths, SpagoBIUserProfile profile) throws KNRM001Exception {

		try {
			Path tempDirectory = Files.createTempDirectory("knowage-zip");
			Path tempFile = Files.createTempFile("knowage-zip", "temp");

			try (ZipOutputStream ret = new ZipOutputStream(Files.newOutputStream(tempFile))) {

				File fileDest = new File(tempDirectory.toString());
				for (String path : fullPaths) {
					String workDir = getWorkDirectory(profile);
					FileUtils.copyFileToDirectory(new File(workDir + File.separator + path), fileDest);
				}
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

	private void extractFolder(String zipFile, String extractFolder) {
		try {
			int BUFFER = 2048;
			File file = new File(zipFile);

			ZipFile zip = new ZipFile(file);
			String newPath = extractFolder;

			new File(newPath).mkdir();
			Enumeration zipFileEntries = zip.entries();

			// Process each entry
			while (zipFileEntries.hasMoreElements()) {
				// grab a zip file entry
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();

				File destFile = new File(newPath, currentEntry);
				// destFile = new File(newPath, destFile.getName());
				File destinationParent = destFile.getParentFile();

				// create the parent directory structure if needed
				destinationParent.mkdirs();

				if (!entry.isDirectory()) {
					BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
					int currentByte;
					// establish buffer for writing file
					byte data[] = new byte[BUFFER];

					// write the current file to disk
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

					// read and write until last byte is encountered
					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
				}

			}
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage());
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

	private boolean isStartingFromModel(String path, SpagoBIUserProfile profile) throws KNRM001Exception {
		Path workModelDir = Paths.get(getWorkDirectory(profile));
		Path modelPath = Paths.get(workModelDir + File.separator + "model");
		return modelPath.equals(Paths.get(path));
	}

	@Override
	public MetadataDTO getMetadata(String path, SpagoBIUserProfile profile) throws KNRM001Exception {
		String workPath = getWorkBaseDirByPath(path, profile);
		MetadataDTO metadata = null;
		Path totalPath = Paths.get(getTotalPath(path, profile) + File.separator + "metadata.json");

		if (isStartingFromModel(workPath, profile) && canSee(Paths.get(workPath), profile)) {

			try {
				// create object mapper instance
				ObjectMapper mapper = new ObjectMapper();
				metadata = mapper.readValue(totalPath.toFile(), MetadataDTO.class);

			} catch (Exception ex) {
				throw new KnowageRuntimeException(ex.getMessage());
			}

		}

		return metadata;
	}

	/**
	 * JSONObject jsonCode = new JSONObject(); jsonCode.put("name", fileDTO.getName()); jsonCode.put("version", fileDTO.getVersion()); jsonCode.put("type",
	 * fileDTO.getType()); jsonCode.put("opensource", fileDTO.isOpensource()); jsonCode.put("description", fileDTO.getDescription()); jsonCode.put("accuracy",
	 * fileDTO.getAccuracy()); jsonCode.put("usage", fileDTO.getUsage()); jsonCode.put("format", fileDTO.getFormat()); jsonCode.put("image",
	 * fileDTO.getImage());
	 *
	 * @throws KNRM001Exception
	 */
	@Override
	public MetadataDTO saveMetadata(MetadataDTO fileDTO, String path, SpagoBIUserProfile profile) throws KNRM001Exception {

		String workPath = getWorkBaseDirByPath(path, profile);
		if (isStartingFromModel(workPath, profile) && canSee(Paths.get(workPath), profile)) {
			Path totalPath = Paths.get(getTotalPath(path, profile) + File.separator + "metadata.json");
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				objectMapper.writeValue(totalPath.toFile(), fileDTO);
			} catch (JsonGenerationException e) {
				throw new KnowageRuntimeException(e.getMessage());
			} catch (JsonMappingException e) {
				throw new KnowageRuntimeException(e.getMessage());
			} catch (IOException e) {
				throw new KnowageRuntimeException(e.getMessage());
			}
		}
		return fileDTO;
	}

}
