package it.eng.knowage.resourcemanager.log.service;


import it.eng.knowage.knowageapi.error.*;
import it.eng.knowage.resourcemanager.log.dto.LogFileDTO;
import it.eng.knowage.resourcemanager.log.dto.LogFolderDTO;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

// per elencare, cercare e scaricare i file di log da Tomcat
public interface LogManagerAPI {

    public LogFolderDTO getFolders(SpagoBIUserProfile profile) throws ImpossibleToReadFolderListException;

    /**
     * Elenca i file di log partendo dal relativo path della cartella
     */
    public List<LogFileDTO> getListOfLogs(String relativePath, SpagoBIUserProfile profile) throws ImpossibleToReadFilesListException, ImpossibleToReadFolderListException;

    java.nio.file.Path getDownloadLogFilePath(List<String> path, SpagoBIUserProfile profile) throws ImpossibleToDownloadFileException;

    boolean canSee(Path path, SpagoBIUserProfile profile) throws IOException;

    String getLogContent(String logName, SpagoBIUserProfile profile) throws ImpossibleToReadFilesListException;

    /**
     * @param key
     * @param path
     * @param profile
     * @return
     * @throws ImpossibleToCreateFileException
     */
    Path getDownloadFolderPath(String key, String path, SpagoBIUserProfile profile) throws ImpossibleToCreateFileException;

    /**
     * Ritorna il relativePath della root predefinita (prina root)
     */
    String getDefaultFolderRelativePath(SpagoBIUserProfile profile) throws ImpossibleToReadFolderListException;

    /**
     * Ritorna il Path completo (workDir + relativePath) della root predefinita)
     */
    Path getDefaultFolderPath(SpagoBIUserProfile profile) throws ImpossibleToReadFolderListException;
}
