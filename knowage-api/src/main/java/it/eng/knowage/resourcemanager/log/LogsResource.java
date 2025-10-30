package it.eng.knowage.resourcemanager.log;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.error.KnowageBusinessException;
import it.eng.knowage.boot.error.KnowageRuntimeException;
import it.eng.knowage.boot.utils.ContextPropertiesConfig;
import it.eng.knowage.knowageapi.error.ImpossibleToDownloadFileException;
import it.eng.knowage.resourcemanager.log.dto.DownloadLogFilesDTO;
import it.eng.knowage.resourcemanager.log.dto.LogFileDTO;
import it.eng.knowage.resourcemanager.log.dto.LogFolderDTO;
import it.eng.knowage.resourcemanager.log.service.LogManagerAPI;
import it.eng.spagobi.services.security.SpagoBIUserProfile;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Path("/2.0/resources/logs")
@Component
@Validated

// per gestire le richieste REST
public class LogsResource {

    @Autowired
    LogManagerAPI logManagerAPIservice;

    @Autowired
    BusinessRequestContext businessContext;

    private static final Logger LOGGER = Logger.getLogger(LogsResource.class);

    @GET
    @Path("/folders")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LogFolderDTO> getLogFolders() throws KnowageBusinessException {
        SpagoBIUserProfile profile = businessContext.getUserProfile();
        try {
            LogFolderDTO root = logManagerAPIservice.getFolders(profile);
            if (root != null && root.getChildren() != null){
                return root.getChildren();
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            throw new KnowageRuntimeException(e);
        }
    }

    @GET
    @Path("/folders/{folder}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LogFileDTO> getLogs(@PathParam("folder") String folder) throws KnowageBusinessException {
        SpagoBIUserProfile profile = businessContext.getUserProfile();
        try {
            LOGGER.debug("getLogs raw folder: " + folder);
            if (folder == null) {
                folder = "";
            } else {
                // decode e trim di slash iniziali/finali
                folder = URLDecoder.decode(folder, StandardCharsets.UTF_8.name());
                folder = folder.replaceAll("^/+", "").replaceAll("/+$", "");
            }

            List<LogFileDTO> result = logManagerAPIservice.getListOfLogs(folder, profile);

            // se vuoto, prova un fallback su "logs" (utile se il client manda path diverso)
            if ((result == null || result.isEmpty()) && !"logs".equals(folder)) {
                LOGGER.debug("Empty result for folder '" + folder + "', trying fallback 'logs'");
                try {
                    result = logManagerAPIservice.getListOfLogs("logs", profile);
                } catch (Exception ex) {
                    LOGGER.debug("Fallback service call failed", ex);
                }
            }

            return result;
        } catch (Exception e) {
            throw new KnowageRuntimeException(e);
        }
    }

    @GET
    @Path("/folders/{folder}/view/{logName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String viewLog(@PathParam("folder") String folder, @PathParam("logName") String logName) throws KnowageBusinessException {
        SpagoBIUserProfile profile = businessContext.getUserProfile();
        String combined = "";
        try {
            LOGGER.debug("viewLog raw folder: " + folder + ", raw log name: " + logName);

            if (folder == null){
                folder = "";
            } else {
                folder = URLDecoder.decode(folder, StandardCharsets.UTF_8.name());
                folder = folder.replaceAll("^/+", "").replaceAll("/+$", "");
            }

            if (logName == null) {
                logName = "";
            } else {
                logName = URLDecoder.decode(logName, StandardCharsets.UTF_8.name());
                logName = logName.replaceAll("^/+", "").replaceAll("/+$", "");
            }

            combined = folder.isEmpty() ? logName : folder + "/" + logName;
            return logManagerAPIservice.getLogContent(combined, profile);
        } catch (Exception e) {
            LOGGER.error("Impossible to read log file: " + combined + " ", e);
            throw new KnowageBusinessException("Impossible to read log file", e);
        }
    }

    @GET
    @Path("/root")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LogFileDTO> getRootLogs() throws KnowageBusinessException {
        SpagoBIUserProfile profile = businessContext.getUserProfile();
        try {
            return logManagerAPIservice.getListOfLogs("", profile);
        } catch (Exception e) {
            throw new KnowageRuntimeException(e);
        }
    }

    @GET
    @Path("/view/{logName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String viewRootLog(@PathParam("logName") String logName) throws KnowageBusinessException {
        SpagoBIUserProfile profile = businessContext.getUserProfile();
        try {
            return logManagerAPIservice.getLogContent(logName, profile);
        } catch (Exception e) {
            throw new KnowageBusinessException("Impossible to read log file", e);
        }
    }

    @POST
    @Path("/download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/zip")
    public Response downloadLogs(@Valid DownloadLogFilesDTO dto, @Context HttpServletRequest request) {
        if (dto == null || dto.getSelectedLogsNames() == null || dto.getSelectedLogsNames().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No files selected").build();
        }

        SpagoBIUserProfile profile = businessContext.getUserProfile();

        // resolve logs root: prefer <logPath>/logs, fallback to logPath
        java.nio.file.Path logsRoot = Paths.get(ContextPropertiesConfig.getLogPath(), "logs");
        if (!Files.exists(logsRoot) || !Files.isDirectory(logsRoot)) {
            logsRoot = Paths.get(ContextPropertiesConfig.getLogPath());
        }

        final java.nio.file.Path finalLogsRoot = logsRoot;

        // create temporary zip file
        final java.nio.file.Path tempZip;
        try {
            tempZip = Files.createTempFile("knowage-zip", ".zip");
        } catch (IOException e) {
            LOGGER.error("Unable to create temp zip file", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unable to create temp file").build();
        }

        // fill zip: for each requested name check root then subfolders
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempZip))) {
            for (String rawName : dto.getSelectedLogsNames()) {
                if (rawName == null || rawName.trim().isEmpty()) {
                    continue;
                }
                String requested = rawName.trim();

                // 1) check directly in logs root
                java.nio.file.Path candidate = finalLogsRoot.resolve(requested).normalize();
                if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                    String entryName = finalLogsRoot.relativize(candidate).toString().replace('\\', '/');
                    logManagerAPIservice.addFileToZip(candidate, entryName, zos);
                    continue;
                }

                // 2) search recursively in subfolders (first match)
                Optional<java.nio.file.Path> found = logManagerAPIservice.findFileRecursively(finalLogsRoot, requested);
                if (found.isPresent()) {
                    java.nio.file.Path source = found.get();
                    String entryName = finalLogsRoot.relativize(source).toString().replace('\\', '/');
                    logManagerAPIservice.addFileToZip(source, entryName, zos);
                } else {
                    LOGGER.debug("File not found: " + requested + " (skipping)");
                    // se non trovato passa al file successivo
                }
            }
            zos.flush();
        } catch (IOException e) {
            LOGGER.error("Unable to create zip: " + e.getMessage(), e);
            try {
                Files.deleteIfExists(tempZip);
            } catch (IOException ex) {
                LOGGER.warn("Unable to delete temp zip " + tempZip, ex);
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unable to create zip: " + e.getMessage()).build();
        }

        // stream response and delete temp file after download
        final java.nio.file.Path temp = tempZip;
        StreamingOutput stream = output -> {
            try (InputStream in = Files.newInputStream(temp)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                }
                output.flush();
            } finally {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ex) {
                    LOGGER.warn("Unable to delete temp zip " + temp, ex);
                }
            }
        };

        String fileName = temp.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(".zip")) {
            fileName += ".zip";
        }

        return Response.ok(stream, "application/zip")
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .build();
    }

//    private Optional<java.nio.file.Path> findFileRecursively(java.nio.file.Path root, String fileName) {
//        try (Stream<java.nio.file.Path> walk = Files.walk(root)) {
//            return walk.filter(Files::isRegularFile)
//                    .filter(p -> p.getFileName().toString().equals(fileName))
//                    .findFirst();
//        } catch (IOException e) {
//            LOGGER.debug("Error searching file recursively: " + fileName, e);
//            return Optional.empty();
//        }
//    }
//
//    private void addFileToZip(java.nio.file.Path source, String entryName, ZipOutputStream zos) throws IOException {
//        ZipEntry entry = new ZipEntry(entryName);
//        zos.putNextEntry(entry);
//        try (InputStream in = Files.newInputStream(source)) {
//            byte[] buf = new byte[8192];
//            int read;
//            while ((read = in.read(buf)) > 0) {
//                zos.write(buf, 0, read);
//            }
//        } finally {
//            zos.closeEntry();
//        }
//    }

//    @POST
//    @Path("/download")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces("application/zip")
//    public Response downloadLogs(@Valid DownloadLogFilesDTO dto, @Context HttpServletRequest request) {
//        if (dto == null || dto.getSelectedLogsNames() == null || dto.getSelectedLogsNames().isEmpty()) {
//            return Response.status(Response.Status.BAD_REQUEST).entity("No files selected").build();
//        }
//
//        SpagoBIUserProfile profile = businessContext.getUserProfile();
//        java.nio.file.Path zipPath = null;
//        try {
//            zipPath = logManagerAPIservice.getDownloadLogFilePath(dto.getSelectedLogsNames(), profile);
//        } catch (ImpossibleToDownloadFileException e) {
//            LOGGER.error("Unable to create zip for selected logs: " + e.getMessage(), e);
//            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to create zip: " + e.getMessage()).build();
//        } catch (Exception e) {
//            LOGGER.error("Unexpected error creating zip for selected logs", e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Impossible to create zip").build();
//        }
//
//        if (zipPath == null || !Files.exists(zipPath)) {
//            LOGGER.error("Zip file not created or missing: " + zipPath);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Zip file not created or missing").build();
//        }
//
//        final java.nio.file.Path temp = zipPath;
//        StreamingOutput stream = output -> {
//            try (InputStream in = Files.newInputStream(temp)) {
//                byte[] buffer = new byte[8192];
//                int len;
//                while ((len = in.read(buffer)) != -1) {
//                    output.write(buffer, 0, len);
//                }
//                output.flush();
//            } finally {
//                try {
//                    Files.deleteIfExists(temp);
//                } catch (IOException ex) {
//                    LOGGER.warn("Unable to delete temp zip " + temp, ex);
//                }
//            }
//        };
//
//        String fileName = temp.getFileName().toString();
//        if (!fileName.toLowerCase().endsWith(".zip")) {
//            fileName += ".zip";
//        }
//
//        return Response.ok(stream, "application/zip")
//                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
//                .build();
//    }
}