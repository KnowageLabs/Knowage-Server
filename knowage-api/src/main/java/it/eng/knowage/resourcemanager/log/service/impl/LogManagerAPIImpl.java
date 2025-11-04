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

@Component
public class LogManagerAPIImpl implements LogManagerAPI {

    private static final String METADATA_JSON = "metadata.json";
    private static final Logger LOGGER = Logger.getLogger(LogManagerAPIImpl.class);
    private final Map<String, HashMap<String, Object>> cachedNodesInfo = new HashMap<>();

    private static final String LOG_FUNCTIONALITY_DEV = "LogManagementDev";
    private static final String LOG_FUNCTIONALITY = "LogManagement";
    private static final String TREAD_CONTEXT_KEY_TENANT = "tenant";
    private static final String GLOBAL = "global";


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
            LOGGER.info("The log folder is missing at [" + totalPath + "]. It will be created now.");
            Files.createDirectories(totalPath);
        }
        return totalPath;
    }

    private String resolveTenant(SpagoBIUserProfile profile) {
        String tenant = ThreadContext.get(TREAD_CONTEXT_KEY_TENANT);
        if (tenant == null || tenant.trim().isEmpty()) {
            tenant = GLOBAL;
        }
        return tenant;
    }

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

/// USE THIS COMMENTED METHOD TO TEST MANUALLY THE LOG MANAGER WITH ALL ROLES
/// REMEMBER TO REVERT TO THE ORIGINAL METHOD BEFORE COMMITTING

/// ADMIN canSee METHOD HAS List<String> adminTenants THAT NEED TO BE FILLED MANUALLY TO WORK PROPERLY

    /// canSee always true to test manually all functionalities
//    @Override
//    public boolean canSee(Path path, SpagoBIUserProfile profile) throws IOException {
//        return true;
//    }

    /// canSee to test manually superadmin functionalities
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

    /// canSee to test manually admin functionalities
    @Override
    public boolean canSee(Path path, SpagoBIUserProfile profile) throws IOException {
        List<String> adminTenants = new ArrayList<String>();
        // adminTenants popolati per test; in produzione dovrebbero venire da profilo/config
        adminTenants.add("OLD_LOGS");

        Path baseLogPath = Paths.get(ContextPropertiesConfig.getLogPath()).normalize();
        Path logsRoot = baseLogPath.resolve("logs").normalize();
        Path target = path.normalize();

        Predicate<Path> checkAgainstRoot = (root) -> {
            if (!target.startsWith(root)) {
                return false;
            }
            Path relative = root.relativize(target);
            int nameCount = relative.getNameCount();

            // root itself
            if (nameCount == 0) {
                return true;
            }

            // elemento direttamente sotto root (es. <root>/file.log o <root>/someDir)
            if (nameCount == 1) {
                // file direttamente sotto root: consentito (requisito 1)
                if (Files.isRegularFile(target)) {
                    return true;
                }
                // directory direttamente sotto root:
                String first = relative.getName(0).toString();
                // sottocartella che raggruppa file non classificabili: consentita (requisito 2)
                if (GLOBAL.equals(first)) {
                    return true;
                }
                // sottocartelle il cui tenant è presente in adminTenants: consentite (requisito 4)
                if (adminTenants != null && adminTenants.contains(first)) {
                    return true;
                }
                return false;
            }

            // percorsi più profondi: consentiti solo se il primo segmento è 'default' (requisito 3)
            // o è uno dei tenant in adminTenants (requisito 5)
            String first = relative.getName(0).toString();
            if (GLOBAL.equals(first)) {
                return true;
            }
            return adminTenants != null && adminTenants.contains(first);
        };

        if (Files.exists(logsRoot) && Files.isDirectory(logsRoot)) {
            if (target.equals(baseLogPath)) {
                return true;
            }
            // prima prova contro logsRoot
            if (checkAgainstRoot.test(logsRoot)) {
                return true;
            }
            // fallback: se non è sotto logsRoot, prova anche contro baseLogPath (consente file direttamente in baseLogPath)
            return checkAgainstRoot.test(baseLogPath);
        }

        if (target.equals(baseLogPath)) {
            return true;
        }
        return checkAgainstRoot.test(baseLogPath);
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

    public Path getTotalPath(String path, SpagoBIUserProfile profile) throws IOException {
        Path workDir = getWorkDirectory(profile).normalize();
        Path resolved = workDir.resolve(path == null || path.isEmpty() ? "" : path).normalize();
        if (!resolved.startsWith(workDir)) {
            throw new IOException("Access denied: resolved path is outside tenant root");
        }
        return resolved;
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

            Path workDir = getWorkDirectory(profile).normalize();

            for (String rawPath : fullPaths) {
                if (rawPath == null || rawPath.trim().isEmpty()) {
                    continue;
                }
                String requested = rawPath.trim();

                try {
                    Path source = null;

                    // se contiene separatori viene considerato un percorso relativo (es. TENANT_1/app.log)
                    if (requested.contains("/") || requested.contains("\\")) {
                        source = getTotalPath(requested, profile);
                        if (!Files.exists(source) || !Files.isRegularFile(source)) {
                            LOGGER.debug("File not found: " + requested + " (skipping)");
                            continue;
                        }
                    } else {
                        // prima prova il file direttamente sotto workDir
                        Path candidate = workDir.resolve(requested).normalize();
                        if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                            source = candidate;
                        } else {
                            // fallback: cerca ricorsivamente per nome file (case-sensitive)
                            Optional<Path> found = findFileRecursively(workDir, requested);
                            if (found.isPresent()) {
                                source = found.get();
                            } else {
                                // fallback aggiuntivo: cerca dentro il tenant GLOBAL (es. 'global')
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

                    // controllo di visibilità: se il profilo non può vedere il file, skip
                    try {
                        if (!canSee(source, profile)) {
                            LOGGER.warn("Access denied to log file: " + requested);
                            continue;
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Unable to check visibility for '" + requested + "': " + e.getMessage(), e);
                        continue;
                    }

                    // crea la struttura di directory relativa dentro tempDirectory e copia il file lì
                    Path relative = workDir.relativize(source);
                    Path parentRelative = relative.getParent();
                    Path targetDir = (parentRelative == null) ? tempDirectory : tempDirectory.resolve(parentRelative);
                    FileUtils.forceMkdir(targetDir.toFile());
                    FileUtils.copyFileToDirectory(source.toFile(), targetDir.toFile());

                } catch (IOException e) {
                    // logga e continua con gli altri file invece di abortire l'intero flusso
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

    /// method used in POST request to download selected log files
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

    /// method used in POST request to download selected log files
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