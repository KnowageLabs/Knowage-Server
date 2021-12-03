package it.eng.knowage.engine.cockpit.api.export;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.html.View;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.api.AbstractCockpitEngineResource;
import it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter;
import it.eng.knowage.engine.cockpit.api.export.pdf.PdfExporter;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/1.0/cockpit/export")
public class CockpitExportResource extends AbstractCockpitEngineResource {

	static private Logger logger = Logger.getLogger(CockpitExportResource.class);
	private static final String OUTPUT_TYPE = "outputType";
	private static final String USER_ID = "user_id";
	private static final String DOCUMENT_ID = "document";
	private static final String DOCUMENT_LABEL = "DOCUMENT_LABEL";

	@GET
	@Path("/excel")
	public View exportToExcel() {
		logger.debug("IN");
		response.setCharacterEncoding("UTF-8");
		String dispatchUrl = null;
		try {
			String outputType = request.getParameter(OUTPUT_TYPE);
			request.setAttribute("template", getIOManager().getTemplateAsString());
			// For now, only XLSX is supported
			if (outputType.equalsIgnoreCase("xlsx")) {
				dispatchUrl = "/WEB-INF/jsp/ngCockpitExportExcel.jsp";
				response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			}

			return new View(dispatchUrl);
		} catch (Exception e) {
			logger.error("Cannot redirect to jsp", e);
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/excel")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public void exportExcel(@Context HttpServletRequest req) {
		logger.debug("IN");
		response.setCharacterEncoding("UTF-8");
		try {
			JSONObject body = RestUtilities.readBodyAsJSONObject(req);
			String template = getIOManager().getTemplateAsString();
			body.put("template", template);
			String outputType = body.getString(OUTPUT_TYPE);
			String userId = body.getString(USER_ID);
			ExcelExporter excelExporter = new ExcelExporter(outputType, userId, body);
			String mimeType = excelExporter.getMimeType();
			if (mimeType != null) {
				Integer documentId = body.optInt(DOCUMENT_ID);
				String documentLabel = body.optString(DOCUMENT_LABEL);
				String options = body.optString("options");
				byte[] data;
				data = excelExporter.getBinaryData(documentId, documentLabel, template, options);

				response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				response.setHeader("Content-length", Integer.toString(data.length));
				response.setHeader("Content-Type", mimeType);
				response.setHeader("Content-Disposition", "attachment; fileName=" + documentLabel + "." + outputType);

				response.getOutputStream().write(data, 0, data.length);
				response.getOutputStream().flush();
				response.getOutputStream().close();
			}
		} catch (Exception e) {
			logger.error("Cannot export to Excel", e);
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}

	}

	@POST
	@Path("/pdf")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public void exportPdf(@Context HttpServletRequest req) {
		logger.debug("IN");
		response.setCharacterEncoding("UTF-8");
		try {
			JSONObject body = RestUtilities.readBodyAsJSONObject(req);
			String userId = body.getString(USER_ID);
			String documentLabel = body.optString(DOCUMENT_LABEL);
			PdfExporter pdfExporter = new PdfExporter(userId, body);
			Integer documentId = body.optInt(DOCUMENT_ID);
			String template = getIOManager().getTemplateAsString();
			body.put("template", template);
			byte[] data = pdfExporter.getBinaryData(documentId, documentLabel, template);

			response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			response.setHeader("Content-length", Integer.toString(data.length));
			response.setHeader("Content-Type", "application/pdf");
			response.setHeader("Content-Disposition", "attachment; fileName=" + documentLabel + ".pdf");

			response.getOutputStream().write(data, 0, data.length);
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot export to PDF", e);
		} finally {
			logger.debug("OUT");
		}

	}

}
