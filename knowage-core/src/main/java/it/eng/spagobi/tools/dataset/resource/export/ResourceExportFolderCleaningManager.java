package it.eng.spagobi.tools.dataset.resource.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.v2.export.ExportPathBuilder;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

public class ResourceExportFolderCleaningManager {

	private static final Logger LOGGER = Logger.getLogger(ResourceExportFolderCleaningManager.class);

	private static final Long DEFAULT_FOLDER_SIZE = 10737418240L; // 10 GB
	private static final Double DEFAULT_CLEANING_PERCENTAGE = 30.0;

	private static final String RESOURCE_EXPORT_FOLDER_SCHED_FULL_CLEAN_CLEANING_PERCENTAGE = "KNOWAGE.RESOURCE.EXPORT.FOLDER.CLEANING_PERCENTAGE";
	private static final String RESOURCE_EXPORT_FOLDER_SCHED_FULL_CLEAN_MAX_FOLDER_SIZE = "KNOWAGE.RESOURCE.EXPORT.FOLDER.MAX_FOLDER_SIZE";

	private String resourceExportPath = null;
	private Long maxResourceFolderSize = DEFAULT_FOLDER_SIZE;
	private Double cleaningPrecentage = DEFAULT_CLEANING_PERCENTAGE;
	private final List<String> allowedFilesNames = new ArrayList<>();

	public void executeCleaning(String resourceExportPath, Long maxResourceFolderSize, Double cleaningPrecentage)
			throws Exception {
		this.resourceExportPath = resourceExportPath;
		this.maxResourceFolderSize = maxResourceFolderSize;
		this.cleaningPrecentage = cleaningPrecentage;

		executeCleaning(false);
	}

	public void executeCleaning() throws Exception {
		executeCleaning(true);
	}

	public void executeCleaning(boolean readPropertiesFromConfig) throws Exception {
		LOGGER.debug("IN - executeCleaning");

		init();

		if (readPropertiesFromConfig)
			resourceExportPath = getExportTempFolderPath();

		if (resourceExportPath != null && new File(resourceExportPath).exists()) {

			if (readPropertiesFromConfig) {
				setMaxResourceFolderSize();

				setCleaningPrecentage();
			}

			File fileResourceExport = new File(resourceExportPath);

			Long actualFolderSize = folderSize(fileResourceExport);
			LOGGER.debug("actualFolderSize (Byte) is " + actualFolderSize);

			if (actualFolderSize > maxResourceFolderSize) {
				LOGGER.debug("Cleaning to quota needed");
				cleanToQuota(actualFolderSize, fileResourceExport);
			} else {
				LOGGER.debug("No cleaning needed");
			}
		} else {
			String message = "resourceExportPath does not exists";
			LOGGER.info(message);
		}

		LOGGER.debug("OUT - executeCleaning");
	}

	private void setCleaningPrecentage() throws Exception {
		IConfigDAO sbiConfigDAO = DAOFactory.getSbiConfigDAO();
		Config configValue;
		configValue = sbiConfigDAO
				.loadConfigParametersByLabel(RESOURCE_EXPORT_FOLDER_SCHED_FULL_CLEAN_CLEANING_PERCENTAGE);
		if (configValue != null && configValue.isActive()) {
			Double tmpPercentage = Double.valueOf(configValue.getValueCheck());
			if (tmpPercentage < 0)
				throw new RuntimeException(String.format("cleaningPercentage [%s] not valid", tmpPercentage));

			cleaningPrecentage = tmpPercentage;
			LOGGER.info("Set cleaningPrecentage parameter with value " + cleaningPrecentage);
		} else {
			LOGGER.info("Set cleaningPrecentage parameter with DEFAULT value " + cleaningPrecentage);
		}
	}

	private void setMaxResourceFolderSize() throws Exception {
		IConfigDAO sbiConfigDAO = DAOFactory.getSbiConfigDAO();
		Config configValue = sbiConfigDAO
				.loadConfigParametersByLabel(RESOURCE_EXPORT_FOLDER_SCHED_FULL_CLEAN_MAX_FOLDER_SIZE);
		if (configValue != null && configValue.isActive()) {

			Long tmpMaxResourceFolderSize = Long.valueOf(configValue.getValueCheck());
			if (tmpMaxResourceFolderSize < 0)
				throw new RuntimeException(
						String.format("maxResourceFolderSize [%s] not valid", tmpMaxResourceFolderSize));

			maxResourceFolderSize = tmpMaxResourceFolderSize;
			LOGGER.info("Set maxResourceFolderSize parameter with value " + maxResourceFolderSize);
		} else {
			LOGGER.info("Set maxResourceFolderSize parameter with DEFAULT value " + maxResourceFolderSize);
		}
	}

	private void init() {
		allowedFilesNames.add(ExportPathBuilder.DATA_FILENAME);
		allowedFilesNames.add(ExportPathBuilder.METADATA_FILENAME);
		allowedFilesNames.add(ExportPathBuilder.DOWNLOADED_PLACEHOLDER_FILENAME);

	}

	private void cleanToQuota(Long actualFolderSize, File folder) throws Exception {
		LOGGER.debug("IN - cleanToQuota");

		Comparator<File> creationTimeComparator = (o1, o2) -> {
			Path pathFile1 = o1.toPath();

			BasicFileAttributes attrFile1 = null;
			try {
				attrFile1 = Files.readAttributes(pathFile1, BasicFileAttributes.class);
			} catch (IOException e) {
				throw new Error("Error while retrieving creation date for file " + o1.getAbsolutePath());
			}

			Path pathFile2 = o2.toPath();
			BasicFileAttributes attrFile2 = null;
			try {
				attrFile2 = Files.readAttributes(pathFile2, BasicFileAttributes.class);
			} catch (IOException e) {
				throw new Error("Error while retrieving creation date for file " + o2.getAbsolutePath());
			}

			return attrFile1.creationTime().compareTo(attrFile2.creationTime());
		};

		/* Inside folders are one for every user. We have to bypass them to access to folders eligible for removal */
		File[] files = folder.listFiles();
		List<File> filesInUserFolders = new ArrayList<>();
		for (File tmpFile1 : files) {
			File[] tmpArray = tmpFile1.listFiles();
			for (File tmpFile2 : tmpArray) {
				filesInUserFolders.add(tmpFile2);
			}
		}

		File[] listToArray = filesInUserFolders.toArray(new File[0]);
		Arrays.sort(listToArray, creationTimeComparator);
		LOGGER.debug("cleanToQuota: Files sorted by creation time");

		Long desiredFolderSize = Math.round(maxResourceFolderSize * (1 - (cleaningPrecentage / 100)));
		Double toRemoveFilesSize = 0.0;
		List<String> fileOrFolderToRemove = new ArrayList<>();
		if (listToArray != null) {
			for (File f : listToArray) {
				if (actualFolderSize - toRemoveFilesSize > desiredFolderSize) {
					LOGGER.debug("cleanToQuota: desiredFolderSize dimension NOT reached");

					if (isPossibleToRemoveFolderOrFile(f)) {
						if (f.isDirectory()) {
							toRemoveFilesSize += folderSize(f);
						} else {
							toRemoveFilesSize += f.length();
						}
						fileOrFolderToRemove.add(f.getAbsolutePath());
					}
				} else {
					LOGGER.debug("cleanToQuota: desiredFolderSize dimension reached");
					break;
				}
			}
		}

		deleteFiles(fileOrFolderToRemove);

		if (actualFolderSize - toRemoveFilesSize > desiredFolderSize) {
			String message = String.format(
					"Impossible to reach desired size of " + desiredFolderSize + " Bytes for resource export folder",
					desiredFolderSize);
			LOGGER.info(message);
		}

		LOGGER.debug("OUT - cleanToQuota");
	}

	private void deleteFiles(List<String> fileToRemove) {
		LOGGER.debug("IN - deleteFiles");
		BasicFileAttributes attrFile1 = null;
		Path pathFile = null;
		for (String filePath : fileToRemove) {
			File f = new File(filePath);
			if (f.isDirectory()) {
				for (File fileToDelete : f.listFiles()) {
					fileToDelete.delete();

					pathFile = fileToDelete.toPath();

					try {
						attrFile1 = Files.readAttributes(pathFile, BasicFileAttributes.class);
					} catch (IOException e) {
						throw new Error(
								"Error while retrieving creation date for file " + fileToDelete.getAbsolutePath());
					}
					LOGGER.info(String.format("deleteFiles: %s with creation time %s deleted",
							fileToDelete.getAbsolutePath(), attrFile1.creationTime().toString()));
				}
			}
			f.delete();
			pathFile = f.toPath();

			try {
				attrFile1 = Files.readAttributes(pathFile, BasicFileAttributes.class);
			} catch (IOException e) {
				throw new Error("Error while retrieving creation date for file " + f.getAbsolutePath());
			}
			LOGGER.info(String.format("deleteFiles: %s with creation time %s deleted", f.getAbsolutePath(),
					attrFile1.creationTime().toString()));
		}
		LOGGER.debug("OUT - deleteFiles");
	}

	private boolean isPossibleToRemoveFolderOrFile(File fileOrFolder) {
		LOGGER.debug("IN - isPossibleToRemoveFolderOrFile");
		boolean remove = true;

		List<String> foundFilesList = new ArrayList<>();

		if (fileOrFolder.isDirectory()) {
			for (File fileInFolder : fileOrFolder.listFiles()) {
				remove &= Files.isRegularFile(fileInFolder.toPath());

				String fileInFolderName = fileInFolder.getName();
				if (!allowedFilesNames.contains(fileInFolderName)) {
					LOGGER.error(
							String.format("Found not allowed file [%s]. Folder can't be removed", fileInFolderName));
					return false;
				}
				foundFilesList.add(fileInFolderName);
			}

			boolean requiredFilesFound = foundFilesList.contains(ExportPathBuilder.DATA_FILENAME)
					&& foundFilesList.contains(ExportPathBuilder.METADATA_FILENAME);
			if (requiredFilesFound) {
				String message = String.format("[%s] and [%s] files found in folder", ExportPathBuilder.DATA_FILENAME,
						ExportPathBuilder.METADATA_FILENAME);
				LOGGER.debug(message);
			} else {
				String message = String.format("[%s] and [%s] files NOT found in folder",
						ExportPathBuilder.DATA_FILENAME, ExportPathBuilder.METADATA_FILENAME);
				LOGGER.error(message);
			}

			remove &= requiredFilesFound;

		} else {
			remove = false;

			String fileInFolderName = fileOrFolder.getName();
			LOGGER.error(String.format("Found file [%s] without temp export folder", fileInFolderName));
		}

		LOGGER.debug(String.format("%s %s  is %s removable", fileOrFolder.isDirectory() ? "Folder" : "File",
				fileOrFolder.getAbsolutePath(), remove ? "" : "NOT"));

		LOGGER.debug("OUT - isPossibleToRemoveFolderOrFile");

		return remove;
	}

	public long folderSize(File folder) {
		LOGGER.debug("IN - folderSize");
		long length = 0;
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				length += file.length();
				LOGGER.debug(String.format("folderSize: file %s - Length %s", file.getAbsolutePath(), file.length()));
			} else {
				length += folderSize(file);
			}
		}
		LOGGER.debug("OUT - folderSize");
		return length;
	}

	public String getExportTempFolderPath() {
		LOGGER.debug("IN");
		String resourcePath = SpagoBIUtilities.getResourcePath();
		return ExportPathBuilder.getInstance().getExportResourcePath(resourcePath).toString();
	}

}
