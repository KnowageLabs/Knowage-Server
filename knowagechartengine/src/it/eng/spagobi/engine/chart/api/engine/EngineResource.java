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
package it.eng.spagobi.engine.chart.api.engine;

import it.eng.spagobi.engine.chart.ChartEngine;
import it.eng.spagobi.engine.chart.api.AbstractChartEngineResource;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * TODO: dadav ci serve?
 * 
 */
@Path("/1.0/engine")
public class EngineResource extends AbstractChartEngineResource {

	static private Logger logger = Logger.getLogger(EngineResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getEngine() {

		logger.debug("IN");
		try {
			return serializeEngine();
		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);
		} finally {
			logger.debug("OUT");
		}
	}

	// =======================================================================
	// SERIALIZATION METHODS
	// =======================================================================

	private String serializeEngine() {
		try {
			JSONObject resultJSON = new JSONObject();
			resultJSON.put("enabled", ChartEngine.isEnabled());
			resultJSON.put("creationDate", ChartEngine.getCreationDate());
			long uptime = System.currentTimeMillis() - ChartEngine.getCreationDate().getTime();
			long days = uptime / 86400000;
			long remainder = uptime % 86400000;
			long hours = remainder / 3600000;
			remainder = remainder % 3600000;
			long minutes = remainder / 60000;
			remainder = remainder % 60000;
			long seconds = remainder / 1000;
			resultJSON.put("uptime", days + "d " + hours + "h " + minutes + "m " + seconds + "s");
			return resultJSON.toString();
		} catch (Exception e) {
			throw new RuntimeException("An unexpected error occured while serializing results", e);
		}
	}

}
