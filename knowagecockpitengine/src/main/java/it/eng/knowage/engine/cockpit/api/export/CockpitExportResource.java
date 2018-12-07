package it.eng.knowage.engine.cockpit.api.export;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import it.eng.knowage.engine.cockpit.api.AbstractCockpitEngineResource;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;

@Path("/1.0/cockpit/export")
public class CockpitExportResource extends AbstractCockpitEngineResource {

	static private Logger logger = Logger.getLogger(CockpitExportResource.class);
	private static final String OUTPUT_TYPE = "outputType";

	@GET
	@Path("/excel")
	public void exportToExcel() {
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

			request.getRequestDispatcher(dispatchUrl).forward(request, response);
		} catch (Exception e) {
			logger.error("Cannot redirect to jsp", e);
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

}
