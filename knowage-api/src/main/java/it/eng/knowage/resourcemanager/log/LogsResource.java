package it.eng.knowage.resourcemanager.log;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.error.KnowageBusinessException;
import it.eng.knowage.boot.error.KnowageRuntimeException;
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
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

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
    public LogFolderDTO getLogFolders() throws KnowageBusinessException {
        SpagoBIUserProfile profile = businessContext.getUserProfile();
        try {
            return logManagerAPIservice.getFolders(profile);
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
            return logManagerAPIservice.getListOfLogs(folder, profile);
        } catch (Exception e) {
            throw new KnowageRuntimeException(e);
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
    public String viewLog(@PathParam("logName") String logName) throws KnowageBusinessException {
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
    public Response downloadLogs(DownloadLogFilesDTO dto, @Context HttpServletRequest request) {
        try {
            SpagoBIUserProfile profile = businessContext.getUserProfile();
            if (dto == null || dto.getSelectedLogsNames() == null || dto.getSelectedLogsNames().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("No files selected").build();
            }

            java.nio.file.Path zipPath = logManagerAPIservice.getDownloadLogFilePath(dto.getSelectedLogsNames(), profile);
            if (zipPath == null || !Files.exists(zipPath)) {
                LOGGER.error("Zip file not created or missing");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Impossible to create zip").build();
            }

            final java.nio.file.Path temp = zipPath;
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

        } catch (Exception e) {
            LOGGER.error("Error downloading logs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Impossible to download file").build();
        }
    }
}