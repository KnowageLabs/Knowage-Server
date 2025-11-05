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
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.stream.Collectors.toList;

/*
* Core implementation of LogManagerAPI.
* - Responsible for building folder tree, file listing, file reading and ZIP exports.
* - Uses ThreadContext tenant key to decide tenant-scoped visibility.
* - Security-critical: keep canSee(), resolveTenant() and path resolution logic consistent.
*/
@Component
public class LogManagerAPIImpl implements LogManagerAPI {

    private static final String METADATA_JSON = "metadata.json";
    private static final Logger LOGGER = Logger.getLogger(LogManagerAPIImpl.class);
    private final Map<String, HashMap<String, Object>> cachedNodesInfo = new HashMap<>();

    private static final String LOG_FUNCTIONALITY = "LogManagement";
    private static final String TREAD_CONTEXT_KEY_TENANT = "tenant";
    private static final String GLOBAL = "global";


    @Autowired
    HMACUtilities hmacUtilities;

    /*
    * Build and return the top-level LogFolderDTO tree.
    * - Ensure work directory existis and delegates recursion to createTree().
    */
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

    // Return a sensible default relative path for UI.
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

    // Return absolute default folder path (resolves relative against workDir).
    @Override
    public Path getDefaultFolderPath(SpagoBIUserProfile profile) throws ImpossibleToReadFolderListException {
        try {
            String relativePath = getDefaultFolderRelativePath(profile);
            return getTotalPath(relativePath, profile);
        } catch (IOException e) {
            throw new ImpossibleToReadFolderListException(e.getMessage(), e);
        }
    }

    /*
    * Ensure base log directory exists and return it.
    * - Creates directories when missing.
    */
    public Path getWorkDirectory(SpagoBIUserProfile profile) throws IOException {
        String logPathBase = ContextPropertiesConfig.getLogPath();
        Path totalPath = Paths.get(logPathBase);
        if (!Files.isDirectory(totalPath)) {
            LOGGER.info("The log folder is missing at [" + totalPath + "]. It will be created now.");
            Files.createDirectories(totalPath);
        }
        return totalPath;
    }

    // Read tenant from ThreadContext, fallback to GLOBAL.
    private String resolveTenant(SpagoBIUserProfile profile) {
        String tenant = ThreadContext.get(TREAD_CONTEXT_KEY_TENANT);
        if (tenant == null || tenant.trim().isEmpty()) {
            tenant = GLOBAL;
        }
        return tenant;
    }

    /*
    * Recursively build folder tree starting from parentFolder.
    * - Applies canSee() before adding each directory node
    */
    private LogFolderDTO createTree(LogFolderDTO parentFolder, SpagoBIUserProfile profile, String currentRelativePath) throws IOException {
        Path node = Paths.get(parentFolder.getFullPath());
        File nodeLog = node.toFile();

        if (nodeLog.isDirectory() && canSee(node, profile)) {
            String[] subNote = nodeLog.list();
            if (subNote != null) {
                for (String logName : subNote) {
                    Path path = node.resolve(logName);
                    if (Files.isDirectory(path) && canSee(path, profile)) {
                        LogFolderDTO folder = new LogFolderDTO(path);
                        folder.setKey(hmacUtilities.getKeyHashedValue(path.toString()));

                        String relativePath = Paths.get(currentRelativePath).resolve(logName).toString();
                        folder.setRelativePath(relativePath);

                        parentFolder.addChildren(folder);
                        createTree(folder, profile, relativePath);
                    }
                }
            }
        }
        return parentFolder;
    }

    /*
    * USE THIS COMMENTED METHOD TO TEST MANUALLY THE LOG MANAGER WITH ALL ROLES.
    * REMEMBER TO REVERT TO THE ORIGINAL METHOD BEFORE COMMITTING.
    * ADMIN canSee METHOD HAS List<String> adminTenants THAT NEED TO BE FILLED MANUALLY TO WORK PROPERLY.
    */

    // canSee always true to test manually all functionalities

//    @Override
//    public boolean canSee(Path path, SpagoBIUserProfile profile) throws IOException {
//        return true;
//    }

    // canSee to test manually superadmin functionalities

//    @Override
//    public boolean canSee(Path path, SpagoBIUserProfile profile) {
//        Path baseLogPath = Paths.get(ContextPropertiesConfig.getLogPath()).normalize();
//        Path rootLogs = baseLogPath.resolve("logs").normalize();
//        Path target = path.normalize();
//
//        if (Files.exists(rootLogs) && Files.isDirectory(rootLogs)) {
//            return target.startsWith(rootLogs);
//        }
//
//        return target.startsWith(baseLogPath);
//    }

    // canSee to test manually admin functionalities

//    @Override
//    public boolean canSee(Path path, SpagoBIUserProfile profile) throws IOException {
//        List<String> adminTenants = new ArrayList<String>();
//        adminTenants.add("OLD_LOGS");
//
//        Path baseLogPath = Paths.get(ContextPropertiesConfig.getLogPath()).normalize();
//        Path logsRoot = baseLogPath.resolve("logs").normalize();
//        Path target = path.normalize();
//
//        Predicate<Path> checkAgainstRoot = (root) -> {
//            if (!target.startsWith(root)) {
//                return false;
//            }
//            Path relative = root.relativize(target);
//            int nameCount = relative.getNameCount();
//
//            if (nameCount == 0) {
//                return true;
//            }
//
//            if (nameCount == 1) {
//                if (Files.isRegularFile(target)) {
//                    return true;
//                }
//                String first = relative.getName(0).toString();
//                if (GLOBAL.equals(first)) {
//                    return true;
//                }
//                if (adminTenants != null && adminTenants.contains(first)) {
//                    return true;
//                }
//                return false;
//            }
//
//            String first = relative.getName(0).toString();
//            if (GLOBAL.equals(first)) {
//                return true;
//            }
//            return adminTenants != null && adminTenants.contains(first);
//        };
//
//        if (Files.exists(logsRoot) && Files.isDirectory(logsRoot)) {
//            if (target.equals(baseLogPath)) {
//                return true;
//            }
//            if (checkAgainstRoot.test(logsRoot)) {
//                return true;
//            }
//            return checkAgainstRoot.test(baseLogPath);
//        }
//
//        if (target.equals(baseLogPath)) {
//            return true;
//        }
//        return checkAgainstRoot.test(baseLogPath);
//    }

    // canSee to test manually non-admin, non-superadmin users functionalities

//    @Override
//    public boolean canSee(Path path, SpagoBIUserProfile profile) throws IOException {
//        return false;
//    }

    /*
    * Actual canSee() method
    * Permission check: superadmins see everything, admins limited to tenant/global/root rules.
    * - Important: the logic must match how logs are generated/partitioned.
    */
    @Override
    public boolean canSee(Path path, SpagoBIUserProfile profile) throws IOException {
        if (hasSuperadminFunctionality(profile)){
            return true;
        }

        if (hasAdminFunctionality(profile)){
            String tenant = resolveTenant(profile);
            Path baseLogPath = Paths.get(ContextPropertiesConfig.getLogPath()).normalize();
            Path logsRoot = baseLogPath.resolve("logs").normalize();
            Path target = path.normalize();

            Predicate<Path> checkAgainstRoot = (root) -> {
                if (!target.startsWith(root)) {
                    return false;
                }
                Path relative = root.relativize(target);
                int nameCount = relative.getNameCount();

                // root itself.
                if (nameCount == 0) {
                    return true;
                }

                // element directly under root (file or directory).
                if (nameCount == 1) {
                    if (Files.isRegularFile(target)) {
                        return true;
                    }
                    String first = relative.getName(0).toString();
                    if (GLOBAL.equals(first)) {
                        return true;
                    }
                    if (tenant != null && tenant.contains(first)) {
                        return true;
                    }
                    return false;
                }

                // deeper paths: allowed only if first segment is GLOBAL or current tenant.
                String first = relative.getName(0).toString();
                if (GLOBAL.equals(first)) {
                    return true;
                }
                return tenant != null && tenant.contains(first);
            };

            if (Files.exists(logsRoot) && Files.isDirectory(logsRoot)) {
                if (target.equals(baseLogPath)) {
                    return true;
                }
                // try against logsRoot first
                if (checkAgainstRoot.test(logsRoot)) {
                    return true;
                }
                // fallback: allow files directly under baseLogPath
                return checkAgainstRoot.test(baseLogPath);
            }

            if (target.equals(baseLogPath)) {
                return true;
            }
            return checkAgainstRoot.test(baseLogPath);
        }

        return false;
    }

    // Helper: check if profile has admin functionality.
    public static boolean hasAdminFunctionality(SpagoBIUserProfile profile) {
        return profile.getFunctions().contains(LOG_FUNCTIONALITY);
    }

    // Helper: check if profile is superadmin.
    public static boolean hasSuperadminFunctionality(SpagoBIUserProfile profile) {
        return profile.isIsSuperadmin();
    }

    // Create zip for a folder identified by UI key/path after permission check.
    @Override
    public Path getDownloadFolderPath(String key, String path, SpagoBIUserProfile profile) throws ImpossibleToCreateFileException {
        Path pathToReturn = null;
        try {
            Path workDirr = getFullRootByPath(path, profile);
            if (canSee(workDirr, profile)) {
                pathToReturn = createZipFile(workDirr);
            } else {
                throw new ImpossibleToCreateFileException("Access denied to log folder " + path + " for download");
            }
        } catch (Exception e) {
            throw new ImpossibleToCreateFileException(e.getMessage(), e);
        }
        return pathToReturn;
    }

    // Entry point used by REST download of individual files.
    @Override
    public Path getDownloadLogFilePath(List<String> path, SpagoBIUserProfile profile) throws ImpossibleToDownloadFileException {
        try {
            return createZipFileOfLogs(path, profile);
        } catch (Exception e) {
            throw new ImpossibleToDownloadFileException(e.getMessage(), e);
        }
    }

    // Resolve a root element (first segment) into an absolute path.
    public Path getFullRootByPath(String path, SpagoBIUserProfile profile) throws IOException {
        String separator = File.separator.equals("\\") ? "\\\\" : File.separator;
        String rootElement = path.split(separator)[0];
        return getTotalPath(rootElement, profile);
    }

    // Helper: find node in folder tree by key.
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

    // List files in a relative path after permission check.
    @Override
    public List<LogFileDTO> getListOfLogs(String relativePath, SpagoBIUserProfile profile) throws ImpossibleToReadFilesListException, ImpossibleToReadFolderListException {
        List<LogFileDTO> returnList = new ArrayList<>();
        try {
            Path totalPath = getTotalPath(relativePath, profile);
            File folder = totalPath.toFile();
            if (canSee(totalPath, profile)) {
                File[] listOfLogs = folder.listFiles();
                if (listOfLogs != null) {
                    for (File l : listOfLogs) {
                        if (l.isFile()) {
                            LOGGER.debug("Log " + l.getName());
                            returnList.add(new LogFileDTO(l));
                        }
                    }
                }
            } else {
                throw new ImpossibleToReadFilesListException("Access denied to log folder " + relativePath);
            }
        } catch (Exception e) {
            throw new ImpossibleToReadFilesListException(e.getMessage(), e);
        }
        return returnList;
    }

    // Resolve a relative path against workDir and prevent traversal outside workDir.
    public Path getTotalPath(String path, SpagoBIUserProfile profile) throws IOException {
        Path workDir = getWorkDirectory(profile).normalize();
        Path resolved = workDir.resolve(path == null || path.isEmpty() ? "" : path).normalize();
        if (!resolved.startsWith(workDir)) {
            throw new IOException("Access denied: resolved path is outside tenant root");
        }
        return resolved;
    }


    // Create a ZIP archive from a folder (recursively copy into temp dir then zip).
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

    // Create a ZIP from an explicit list of files (validates permission per file).
    public Path createZipFileOfLogs(List<String> fullPaths, SpagoBIUserProfile profile) {

        try {
            Path tempDirectory = Files.createTempDirectory("knowage-zip");
            Path tempLog = Files.createTempFile("knowage-zip", ".zip");

            Path workDir = getWorkDirectory(profile).normalize();

            for (String rawPath : fullPaths) {
                if (rawPath == null || rawPath.trim().isEmpty()) {
                    continue;
                }
                String requested = rawPath.trim();

                try {
                    Path source = null;

                    // If contains separators treat as relative path.
                    if (requested.contains("/") || requested.contains("\\")) {
                        source = getTotalPath(requested, profile);
                        if (!Files.exists(source) || !Files.isRegularFile(source)) {
                            LOGGER.debug("File not found: " + requested + " (skipping)");
                            continue;
                        }
                    } else {
                        // Try file directly under workDir.
                        Path candidate = workDir.resolve(requested).normalize();
                        if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                            source = candidate;
                        } else {
                            // Recursive search by filename (case-sensitive).
                            Optional<Path> found = findFileRecursively(workDir, requested);
                            if (found.isPresent()) {
                                source = found.get();
                            } else {
                                // Fallback: search under GLOBAL tenant.
                                try {
                                    Path globalTenant = workDir.resolve(GLOBAL).resolve(requested).normalize();
                                    if (Files.exists(globalTenant) && Files.isRegularFile(globalTenant)) {
                                        source = globalTenant;
                                    }
                                } catch (Exception e) {
                                    LOGGER.debug("Error while searching under tenant '" + GLOBAL + "': " + e.getMessage(), e);
                                }
                            }

                            if (source == null) {
                                LOGGER.debug("File not found: " + requested + " (skipping)");
                                continue;
                            }
                        }
                    }

                    // Visibility check: skip files not visible to the caller
                    try {
                        if (!canSee(source, profile)) {
                            LOGGER.warn("Access denied to log file: " + requested);
                            continue;
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Unable to check visibility for '" + requested + "': " + e.getMessage(), e);
                        continue;
                    }

                    // copy file into tempDirectory preserving relative folder structure
                    Path relative = workDir.relativize(source);
                    Path parentRelative = relative.getParent();
                    Path targetDir = (parentRelative == null) ? tempDirectory : tempDirectory.resolve(parentRelative);
                    FileUtils.forceMkdir(targetDir.toFile());
                    FileUtils.copyFileToDirectory(source.toFile(), targetDir.toFile());

                } catch (IOException e) {
                    // log and continue with other files (do not abort ZIP creation)
                    LOGGER.warn("Unable to copy selected log '" + requested + "': " + e.getMessage(), e);
                }
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

    // Create zip entries for collected paths.
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

    // Stream copy helper.
    private void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) > 0) {
            target.write(buf, 0, length);
        }
    }

    // Delete temp directoy tree created for zipping.
    private void cleanUpTempDirectory(Path tempDirectory) throws IOException {
        // Common way to delete recursively
        try (Stream<Path> walk = Files.walk(tempDirectory)) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    // Read file content after permission check.
    @Override
    public String getLogContent(String logName, SpagoBIUserProfile profile) throws ImpossibleToReadFilesListException {
        try {
            Path logFile = getTotalPath(logName, profile);
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

    // Find first file matching filename under root (case-sensitive).
    @Override
    public Optional<java.nio.file.Path> findFileRecursively(java.nio.file.Path root, String fileName) {
        try (Stream<java.nio.file.Path> walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile)
                    .peek(p -> LOGGER.trace("Visiting: " + p))
                    .filter(p -> p.getFileName().toString().equals(fileName))
                    .findFirst();
        } catch (IOException e) {
            LOGGER.debug("Error searching file recursively: " + fileName, e);
            return Optional.empty();
        }
    }

    // Add a single file entry into an existing ZipOutputStream.
    @Override
    public void addFileToZip(java.nio.file.Path source, String entryName, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        try (InputStream in = Files.newInputStream(source)) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) > 0) {
                zos.write(buf, 0, read);
            }
        } finally {
            zos.closeEntry();
        }
    }
}