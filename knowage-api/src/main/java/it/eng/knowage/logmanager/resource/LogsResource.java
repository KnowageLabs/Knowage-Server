package it.eng.knowage.logmanager.resource;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.error.KnowageBusinessException;
import it.eng.knowage.boot.error.KnowageRuntimeException;
import it.eng.knowage.knowageapi.error.ImpossibleToDownloadFileException;
import it.eng.knowage.logmanager.resource.dto.DownloadLogFilesDTO;
import it.eng.knowage.logmanager.resource.dto.LogFileDTO;
import it.eng.knowage.logmanager.resource.dto.LogFolderDTO;
import it.eng.knowage.logmanager.service.LogManagerAPI;
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
import java.util.Collections;
import java.util.List;

@Path("/2.0/resources/logs")
@Component("logsResourceLogManager")
@Validated

/*
* REST resource for log management.
* - Exposes endpoints for listing folders, listing files, viewing single files and downloading selected files.
* - Delegates actual FS access and permission checks to LogManagerAPI.
* - Streaming endpoint returns a temporary ZIP created by the service and deletes it after streaming.
*/
public class LogsResource {

    @Autowired
    LogManagerAPI logManagerAPIservice;

    @Autowired
    BusinessRequestContext businessContext;

    private static final Logger LOGGER = Logger.getLogger(LogsResource.class);

    /*
    * Get the list of logs subfolders (children of work dir).
    * - Returns JSON list of LogFolderDTO (UI tree nodes).
    */
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
            // Wrap unexpected errors as runtime exceptions for upper layers
            throw new KnowageRuntimeException(e);
        }
    }

    /*
    * List files inside a given subfolder (relative path provided by client).
    * - Decodes path, trims slashes and calls service to list files.
    * - If empty result, attempts a fallback to "logs" folder.
    */
    @GET
    @Path("/folders/{folder}/files")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LogFileDTO> getLogs(@PathParam("folder") String folder) throws KnowageBusinessException {
        SpagoBIUserProfile profile = businessContext.getUserProfile();
        try {
            LOGGER.debug("getLogs raw folder: " + folder);
            folder = getFileName(folder);

            List<LogFileDTO> result = logManagerAPIservice.getListOfLogs(folder, profile);

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

    /*
    * View raw content of a single log file inside a folder.
    * - Builds combined relative path and delegates to service.
    * - Service performs existence and permission checks.
    */
    @GET
    @Path("/folders/{folder}/files/{fileName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String viewLog(@PathParam("folder") String folder, @PathParam("fileName") String fileName) throws KnowageBusinessException {
        SpagoBIUserProfile profile = businessContext.getUserProfile();
        String combined = "";
        try {
            LOGGER.debug("viewLog raw folder: " + folder + ", raw log name: " + fileName);

            folder = getFileName(folder);

            fileName = getFileName(fileName);

            combined = folder.isEmpty() ? fileName : folder + "/" + fileName;
            return logManagerAPIservice.getLogContent(combined, profile);
        } catch (Exception e) {
            LOGGER.error("Impossible to read log file: " + combined + " ", e);
            throw new KnowageBusinessException("Impossible to read log file", e);
        }
    }

    private String getFileName(String fileName) {
        if (fileName == null) {
            fileName = "";
        } else {
            fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
            int start = 0;
            int end = fileName.length();
            while (start < end && fileName.charAt(start) == '/') start++;
            while (end > start && fileName.charAt(end - 1) == '/') end--;
            fileName = fileName.substring(start, end);
        }
        return fileName;
    }

    // List files in the root folder (workDir).
    @GET
    @Path("/root")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LogFileDTO> getRootLogs() {
        SpagoBIUserProfile profile = businessContext.getUserProfile();
        try {
            return logManagerAPIservice.getListOfLogs("", profile);
        } catch (Exception e) {
            throw new KnowageRuntimeException(e);
        }
    }

    // View raw content of a single log file directly under root folder (workDir).
    @GET
    @Path("/root/{fileName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String viewRootLog(@PathParam("fileName") String fileName) throws KnowageBusinessException {
        SpagoBIUserProfile profile = businessContext.getUserProfile();
        try {
            return logManagerAPIservice.getLogContent(fileName, profile);
        } catch (Exception e) {
            throw new KnowageBusinessException("Impossible to read log file", e);
        }
    }

    /*
    * Download selected log files as a ZIP.
    * - Accepts validated DTO with a list of file names.
    * - Delegates ZIP creation to LogManagerAPI which must validate per-file permissions.
    * - Streams the created temp ZIP and deletes it after streaming.
    */
    @POST
    @Path("/download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/zip")
    public Response downloadLogs(@Valid DownloadLogFilesDTO dto, @Context HttpServletRequest request) {
        // Validate request payload.
        if (dto == null || dto.getSelectedLogsNames() == null || dto.getSelectedLogsNames().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No files selected").build();
        }

        SpagoBIUserProfile profile = businessContext.getUserProfile();
        java.nio.file.Path zipPath;
        try {
            // Service returns a path to a temp zip, service must ensure log files are permitted.
            zipPath = logManagerAPIservice.getDownloadLogFilePath(dto.getSelectedLogsNames(), profile);
        } catch (ImpossibleToDownloadFileException e) {
            LOGGER.error("Unable to create zip for selected logs", e);
            Throwable cause = e.getCause();

            // Detect internal archive creation errors vs client errors.
            boolean internalZipError = cause instanceof KnowageRuntimeException
                    || (e.getMessage() != null && e.getMessage().contains("Error creating export ZIP archive"))
                    || (cause != null && cause.getMessage() != null && cause.getMessage().contains("Error creating export ZIP archive"));

            if (internalZipError) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Unable to create zip: internal error while creating archive").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Unable to create zip: " + e.getMessage()).build();
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error creating zip for selected logs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Impossible to create zip").build();
        }

        // Basic checks on created temp file.
        if (zipPath == null) {
            LOGGER.error("Zip file creation returned null");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Zip file not created").build();
        }
        if (!Files.exists(zipPath)) {
            LOGGER.error("Zip file missing: " + zipPath);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Zip file missing").build();
        }

        final java.nio.file.Path temp = zipPath;

        // Streaming output that sends zip bytes and removes temp file afterwards.
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
                    // Log deletion failure, do not fail the response.
                    LOGGER.warn("Unable to delete temp zip " + temp, ex);
                }
            }
        };

        // Prepare response headers (filename and optional content-length).
        String fileName = temp.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(".zip")) {
            fileName += ".zip";
        }
        long contentLength = -1;
        try {
            contentLength = Files.size(temp);
        } catch (IOException e) {
            LOGGER.debug("Unable to determine zip size", e);
        }

        Response.ResponseBuilder rb = Response.ok(stream, "application/zip")
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        if (contentLength >= 0) {
            rb.header("Content-Length", Long.toString(contentLength));
        }

        return rb.build();
    }
}