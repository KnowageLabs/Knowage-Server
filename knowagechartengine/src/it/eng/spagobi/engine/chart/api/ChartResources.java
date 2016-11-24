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

package it.eng.spagobi.engine.chart.api;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("1.0/template")
@ManageAuthorization
public class ChartResources {

	static private Logger logger = Logger.getLogger(ChartResources.class);
	
	@POST
	@Path("/save")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void saveTemplate(@FormParam("jsonTemplate") String jsonTemplate, @FormParam("docLabel") String docLabel, @FormParam("userId") String userId,
			@Context HttpServletResponse servletResponse) {
		try {
			JSONObject json = new JSONObject(jsonTemplate);
			ChartTemplateClient ctc = new ChartTemplateClient();
			ctc.saveTemplate(json, docLabel, userId);
		} catch (JSONException e) {
			logger.error("Error while reading JSON of the chart template.", e);
			throw new SpagoBIServiceException("Error while reading JSON of the chart template", e);
		} catch (Exception e) {
			logger.error("Error while saving template", e);
			throw new SpagoBIServiceException("Error while saving template", e);
		}
	}
	
	
}
