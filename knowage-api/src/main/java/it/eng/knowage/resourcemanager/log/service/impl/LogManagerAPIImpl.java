package it.eng.knowage.resourcemanager.log.service.impl;

import it.eng.knowage.boot.error.KnowageRuntimeException;
import it.eng.knowage.boot.utils.ContextPropertiesConfig;
import it.eng.knowage.boot.utils.HMACUtilities;
import it.eng.knowage.knowageapi.error.*;
import it.eng.knowage.resourcemanager.log.dto.LogFileDTO;
import it.eng.knowage.resourcemanager.log.service.LogManagerAPI;
import it.eng.knowage.resourcemanager.log.dto.LogFolderDTO;
import it.eng.spagobi.services.security.SpagoBIUserProfile;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.stream.Collectors.toList;

@Component
public class LogManagerAPIImpl implements LogManagerAPI {

    private static final String METADATA_JSON = "metadata.json";
    private static final Logger LOGGER = Logger.getLogger(LogManagerAPIImpl.class);
    private final Map<String, HashMap<String, Object>> cachedNodesInfo = new HashMap<>();

    private static final String LOG_FUNCTIONALITY_DEV = "LogManagementDev";
    private static final String LOG_FUNCTIONALITY = "LogManagement";

    @Autowired
    HMACUtilities hmacUtilities;

    @Override
    public LogFolderDTO getFolders(SpagoBIUserProfile profile) throws ImpossibleToReadFolderListException {
        LOGGER.debug("Starting resource path json tree");
        try {
            Path logRoot = getWorkDirectory(profile);

            LogFolderDTO parentFolder = new LogFolderDTO(logRoot);
            parentFolder.setRelativePath("");
            parentFolder.setKey(hmacUtilities.getKeyHashedValue(logRoot.toString()));

            LogFolderDTO tree = createTree(parentFolder, profile, "");
            LOGGER.debug("Finished log path json tree");
            return tree;
        } catch (IOException e) {
            LOGGER.error("[LogManagerAPIImpl], [getFolders], ", e);
            throw new KnowageRuntimeException(e);
        }
    }

    @Override
    public String getDefaultFolderRelativePath(SpagoBIUserProfile profile) throws ImpossibleToReadFolderListException {
        LogFolderDTO root = getFolders(profile);

        if (root != null && root.getRelativePath() != null) {
                return root.getRelativePath();
        }

        String message = "No defaults log folder available";
        LOGGER.debug(message);
        throw new ImpossibleToReadFolderListException(message);
    }

    @Override
    public Path getDefaultFolderPath(SpagoBIUserProfile profile) throws ImpossibleToReadFolderListException {
        try {
            String relativePath = getDefaultFolderRelativePath(profile);
            return getTotalPath(relativePath, profile);
        } catch (IOException e) {
            throw new ImpossibleToReadFolderListException(e.getMessage(), e);
        }
    }

    public Path getWorkDirectory(SpagoBIUserProfile profile) throws IOException {
        String logPathBase = ContextPropertiesConfig.getLogPath();
        Path totalPath = Paths.get(logPathBase);
        if (!Files.isDirectory(totalPath)) {
            LOGGER.info("The log folder is missing. It will be created now.");
            Files.createDirectories(totalPath);
        }
        return totalPath;
    }

    private LogFolderDTO createTree(LogFolderDTO parentFolder, SpagoBIUserProfile profile, String currentRelativePath) throws IOException {
        Path node = Paths.get(parentFolder.getFullPath());
        File nodeLog = node.toFile();

        if (nodeLog.isDirectory() && canSee(node, profile)) {
            String[] subNote = nodeLog.list();
            for (String logName : subNote) {
                Path path = node.resolve(logName);
                if (Files.isDirectory(path)) {
                    LogFolderDTO folder = new LogFolderDTO(path);
                    folder.setKey(hmacUtilities.getKeyHashedValue(path.toString()));

                    String relativePath = Paths.get(currentRelativePath).resolve(logName).toString();
                    folder.setRelativePath(relativePath);

                    parentFolder.addChildren(folder);
                    createTree(folder, profile, relativePath);
                }
            }
        }
        return parentFolder;
    }

    @Override
    public boolean canSee(Path path, SpagoBIUserProfile profile) throws IOException {
        if (profile.isIsSuperadmin()) {
            return true;
        }
        return hasAdministratorFunction(profile);
    }

    // Admin functionalities, EE and CE
    public static boolean hasAdministratorFunction(SpagoBIUserProfile profile) {
        return profile.getFunctions().contains(LOG_FUNCTIONALITY);
    }

    // DEV functionalities, EE and CE
    public static boolean hasDevFunctionality(SpagoBIUserProfile profile) {
        return profile.getFunctions().contains(LOG_FUNCTIONALITY_DEV);
    }

    @Override
    public Path getDownloadFolderPath(String key, String path, SpagoBIUserProfile profile)
            throws ImpossibleToCreateFileException {
        Path workingPath = null;
        Path pathToReturn = null;
        try {
            Path workDirr = getFullRootByPath(path, profile);
            if (canSee(workDirr, profile)) {
                Path workDir = getWorkDirectory(profile);
                workingPath = workDir.resolve(path);
                pathToReturn = createZipFile(workingPath);
            }
        } catch (Exception e) {
            throw new ImpossibleToCreateFileException(e.getMessage(), e);
        }
        return pathToReturn;
    }

    @Override
    public Path getDownloadLogFilePath(List<String> path, SpagoBIUserProfile profile) throws ImpossibleToDownloadFileException {
        try {
            return createZipFileOfLogs(path, profile);
        } catch (Exception e) {
            throw new ImpossibleToDownloadFileException(e.getMessage(), e);
        }
    }

    public Path getFullRootByPath(String path, SpagoBIUserProfile profile) throws IOException {
        String separator = File.separator.equals("\\") ? "\\\\" : File.separator;
        String rootElement = path.split(separator)[0];
        return getTotalPath(rootElement, profile);
    }



    private LogFolderDTO findNode(LogFolderDTO node, String key) {
        LogFolderDTO toReturn = null;
        if (node.getKey().equals(key))
            return node;

        List<LogFolderDTO> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            toReturn = findNode(children.get(i), key);
            if (toReturn != null)
                break;
        }

        return toReturn;
    }

    @Override
    public List<LogFileDTO> getListOfLogs(String relativePath, SpagoBIUserProfile profile) throws ImpossibleToReadFilesListException, ImpossibleToReadFolderListException {
        List<LogFileDTO> returnList = new ArrayList<>();
        try {
            Path totalPath = getTotalPath(relativePath, profile);
            File folder = totalPath.toFile();
            Path workDir = getWorkDirectory(profile);
            if (canSee(workDir, profile)) {
                File[] listOfLogs = folder.listFiles();
                if (listOfLogs != null) {
                    for (File l : listOfLogs) {
                        if (l.isFile()) {
                            LOGGER.debug("Log " + l.getName());
                            returnList.add(new LogFileDTO(l));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ImpossibleToReadFilesListException(e.getMessage(), e);
        }
        return returnList;
    }

    public Path getTotalPath(String path, SpagoBIUserProfile profile) throws IOException {
        return getWorkDirectory(profile).resolve(path);
    }

    public Path createZipFile(Path fullPath) {

        try {
            Path tempDirectory = Files.createTempDirectory("knowage-zip");
            Path tempLog = Files.createTempFile("knowage-zip", ".zip");

            File logDest = tempDirectory.resolve(fullPath.getName(fullPath.getNameCount() - 1)).toFile();
            FileUtils.copyDirectory(fullPath.toFile(), logDest);
            try (Stream<Path> walk = Files.walk(tempDirectory)) {
                List<Path> logs = walk.collect(toList());

                putEntries(tempDirectory, tempLog, logs);
            }

            cleanUpTempDirectory(tempDirectory);

            return tempLog;

        } catch (IOException e) {
            throw new KnowageRuntimeException("Error creating export ZIP archive", e);
        }
    }

    public Path createZipFileOfLogs(List<String> fullPaths, SpagoBIUserProfile profile) {

        try {
            Path tempDirectory = Files.createTempDirectory("knowage-zip");
            Path tempLog = Files.createTempFile("knowage-zip", ".zip");

            File logDest = new File(tempDirectory.toString());
            for (String path : fullPaths) {
                Path workDir = getTotalPath(path, profile);
                FileUtils.copyFileToDirectory(workDir.toFile(), logDest);
            }
            try (Stream<Path> walk = Files.walk(tempDirectory)) {
                List<Path> logs = walk.collect(toList());

                putEntries(tempDirectory, tempLog, logs);
            }
            cleanUpTempDirectory(tempDirectory);

            return tempLog;

        } catch (IOException e) {
            throw new KnowageRuntimeException("Error creating export ZIP archive", e);
        }
    }

    private void putEntries(Path tempDirectory, Path tempLog, List<Path> logs) throws IOException {
        try (ZipOutputStream ret = new ZipOutputStream(Files.newOutputStream(tempLog))) {

            for (Path currPath : logs) {
                Path relativize = tempDirectory.relativize(currPath);
                if (!relativize.toString().isEmpty()) {
                    if (Files.isDirectory(currPath)) {
                        ZipEntry zipEntry = new ZipEntry(relativize.toString() + "/");
                        ret.putNextEntry(zipEntry);
                        ret.closeEntry();
                    } else {
                        ZipEntry zipEntry = new ZipEntry(relativize.toString());
                        ret.putNextEntry(zipEntry);
                        try (InputStream currPathInputStream = Files.newInputStream(currPath)) {
                            copy(currPathInputStream, ret);
                        }
                    }
                    ret.closeEntry();
                }
            }
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
        try (Stream<Path> walk = Files.walk(tempDirectory)) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    @Override
    public String getLogContent(String logName, SpagoBIUserProfile profile) throws ImpossibleToReadFilesListException {
        try {
            Path logDir = getWorkDirectory(profile);
            Path logFile = logDir.resolve(logName);
            if (!Files.isRegularFile(logFile)) {
                throw new ImpossibleToReadFilesListException("The log file " + logName + " does not exist.");
            }
            if (!canSee(logFile, profile)){
                throw new ImpossibleToReadFilesListException("Access denied to log file " + logName);
            }
            return new String(Files.readAllBytes(logFile));
        } catch (Exception e) {
            throw new ImpossibleToReadFilesListException(e.getMessage(), e);
        }
    }
}