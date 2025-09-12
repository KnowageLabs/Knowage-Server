package it.eng.knowage.dashboardexport;

import it.eng.knowage.engine.api.export.dashboard.excel.DashboardExcelExporter;
import it.eng.knowage.engine.api.export.dashboard.pdf.DashboardPdfExporter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.mime.MimeUtils;
import it.eng.spagobi.utilities.rest.RestUtilities;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import static java.nio.charset.StandardCharsets.UTF_8;

@Path("/1.0/dashboardExport")
public class DashboardExportResource {

    protected static Logger logger = Logger.getLogger(DashboardExportResource.class);

    @Context
    protected HttpServletRequest request;
    @Context
    protected HttpServletResponse response;

    private static final String TOKEN_HEADER = "x-kn-authorization";

    @POST
    @Path("/spreadsheet")
    public void downloadExcel(@Context HttpServletRequest req) {
        logger.debug("IN");
        response.setCharacterEncoding(UTF_8.name());
        try {
            JSONObject body = RestUtilities.readBodyAsJSONObject(req);
            String token = request.getHeader(TOKEN_HEADER);
            String userId = token.substring(7);
            DashboardExcelExporter excelExporter = new DashboardExcelExporter(userId, body);
            String mimeType = excelExporter.getMimeType();
            String optionalWidgetId = body.optString("id");
            boolean isDashboardSingleWidgetExport = !optionalWidgetId.isEmpty();

            if (!MimeUtils.isValidMimeType(mimeType))
                throw new SpagoBIRuntimeException("Invalid mime type: " + mimeType);

            if (mimeType != null) {
                byte[] data;
                data = excelExporter.getDashboardBinaryData(body, isDashboardSingleWidgetExport);
                if (!isDashboardSingleWidgetExport) {
                    String documentLabel = body.getJSONObject("document").getString("label");
                    response.setHeader("Content-Disposition", "attachment; fileName=" + documentLabel + ".xlsx");
                } else {
                    String widgetName = body.getJSONObject("settings").getJSONObject("style").getJSONObject("title")
                            .optString("text");
                    response.setHeader("Content-Disposition", "attachment; fileName=" + widgetName + "." + "xlsx");
                }
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                response.setHeader("Content-length", Integer.toString(data.length));
                response.setHeader("Content-Type", mimeType);

                response.getOutputStream().write(data, 0, data.length);
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
        } catch (Exception e) {
            logger.error("Cannot export to Excel", e);
            throw new SpagoBIRuntimeException("Cannot export to Excel", e);
        } finally {
            logger.debug("OUT");
        }
    }

    @POST
    @Path("/pdf")
    public void downloadPdf(@Context HttpServletRequest req) {
        logger.debug("IN");
        response.setCharacterEncoding(UTF_8.name());
        try {
            JSONObject body = RestUtilities.readBodyAsJSONObject(req);
            String token = request.getHeader(TOKEN_HEADER);
            String userId = token.substring(7);
            DashboardPdfExporter dashboardPdfExporter = new DashboardPdfExporter(userId, body);
            String mimeType = dashboardPdfExporter.getMimeType();

            if (!MimeUtils.isValidMimeType(mimeType))
                throw new SpagoBIRuntimeException("Invalid mime type: " + mimeType);

            if (mimeType != null) {
                byte[] data;
                data = dashboardPdfExporter.getBinaryData(body);
                String widgetName = body.getJSONObject("settings").getJSONObject("style").getJSONObject("title")
                        .optString("text");
                response.setHeader("Content-Disposition", "attachment; fileName=" + widgetName + "." + "pdf");
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                response.setHeader("Content-length", Integer.toString(data.length));
                response.setHeader("Content-Type", mimeType);

                response.getOutputStream().write(data, 0, data.length);
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
        } catch (Exception e) {
            logger.error("Cannot export to PDF", e);
            throw new SpagoBIRuntimeException("Cannot export to PDF", e);
        } finally {
            logger.debug("OUT");
        }
    }

}
