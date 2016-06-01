/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.engines.svgviewer.api.restful;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.api.AbstractSvgViewerEngineResource;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;

import java.io.File;
import java.nio.file.Files;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */

@Path("1.0/svgviewer")
@ManageAuthorization
public class SvgViewerResource extends AbstractSvgViewerEngineResource {

	@Path("/drawMap")
	@GET
	@Produces(SvgViewerEngineConstants.SVG_MIME_TYPE + "; charset=UTF-8")
	public Response drawMap(@Context HttpServletRequest req) {
		logger.debug("IN");
		try {
			// TODO: let the output format to be configurable with a parameter
			File maptmpfile = getEngineInstance().renderMap("dsvg");
			byte[] data = Files.readAllBytes(maptmpfile.toPath());

			ResponseBuilder response = Response.ok(data);
			response.header("Content-Disposition", "inline; filename=map.svg");
			return response.build();

		} catch (Exception e) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}

		// TODO: to finish
	}

}
