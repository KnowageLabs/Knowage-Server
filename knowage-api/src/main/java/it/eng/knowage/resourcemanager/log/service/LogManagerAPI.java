package it.eng.knowage.resourcemanager.log.service;


import it.eng.knowage.knowageapi.error.*;
import it.eng.knowage.resourcemanager.log.dto.LogFileDTO;
import it.eng.knowage.resourcemanager.log.dto.LogFolderDTO;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

/*
* Core interface for log file management.
* - Lists folders and files under the application log directory.
* - Provides methods to read single files and prepare ZIP downloads.
* - Permission checks are performed against the provided SpagoBIUserProfile.
* - Implementations must ensure path resolution never escapes tenant root.
*/
public interface LogManagerAPI {

    /*
    * Build and return the tree of available log folders for the caller.
    * - Should apply visibility rules (canSee) while building the tree.
    */
    LogFolderDTO getFolders(SpagoBIUserProfile profile) throws ImpossibleToReadFolderListException;

    /*
    * Return the base work directory (logs root) for the caller.
    * - Implementations must create the directory if missing.
    */
    Path getWorkDirectory(SpagoBIUserProfile profile) throws IOException;

    /*
    * List log files inside a relative path.
    * - Must validate that caller canSee() the requested folder.
    */
    List<LogFileDTO> getListOfLogs(String relativePath, SpagoBIUserProfile profile) throws ImpossibleToReadFilesListException, ImpossibleToReadFolderListException;

    /*
    * Create a temporary ZIP containing the requested paths and return its Path.
    * - Caller must ensure DTO is validated.
    * - Implementation must validate each file with canSee() before including it.
    */
    java.nio.file.Path getDownloadLogFilePath(List<String> path, SpagoBIUserProfile profile) throws ImpossibleToDownloadFileException;

    /*
    * Check if the given absolute path is visible to the user profile.
    * - Centralize tenant/role/superadmin rules here.
    */
    boolean canSee(Path path, SpagoBIUserProfile profile) throws IOException;

    /*
    * Return the textual content of a log file.
    * - Must validate file exists and caller canSee() it.
    */
    String getLogContent(String logName, SpagoBIUserProfile profile) throws ImpossibleToReadFilesListException;

    /*
    * Create a ZIP for a whole folder identified by a UI key/path.
    * - Implementations should validate permission and return a temp zip Path.
    */
    Path getDownloadFolderPath(String key, String path, SpagoBIUserProfile profile) throws ImpossibleToCreateFileException;

    /*
    * Return the default relative path that should be shown as root for the user.
    * - Used by UI to open a sensible starting folder.
    */
    String getDefaultFolderRelativePath(SpagoBIUserProfile profile) throws ImpossibleToReadFolderListException;

    /*
    * Return the absolute Path of the default root for the user (workDir + relative).
    */
    Path getDefaultFolderPath(SpagoBIUserProfile profile) throws ImpossibleToReadFolderListException;

    /*
    * Search a file by name recursively under a root.
    * - Used by the download endpoint when client supplies only a filename.
    * - Should be case-sensitive and return the first match.
    */
    Optional<Path> findFileRecursively(java.nio.file.Path root, String fileName);

    /*
    * Add a single file into an existing ZipOutputStream.
    * - Entry name must be provided relative to the chosen root inside the archive.
    */
    void addFileToZip(java.nio.file.Path source, String entryName, ZipOutputStream zos) throws IOException;
}