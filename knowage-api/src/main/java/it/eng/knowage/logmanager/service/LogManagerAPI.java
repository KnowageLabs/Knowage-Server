package it.eng.knowage.logmanager.service;


import it.eng.knowage.knowageapi.error.*;
import it.eng.knowage.logmanager.resource.dto.LogFileDTO;
import it.eng.knowage.logmanager.resource.dto.LogFolderDTO;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
    * Return the textual content of a log file.
    * - Must validate file exists and caller canSee() it.
    */
    String getLogContent(String logName, SpagoBIUserProfile profile) throws ImpossibleToReadFilesListException;
}