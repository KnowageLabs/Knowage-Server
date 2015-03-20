/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.engine.cockpit.api.engine;

import it.eng.spagobi.engine.cockpit.CockpitEngine;
import it.eng.spagobi.engine.cockpit.api.AbstractCockpitEngineResource;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
@Path("/1.0/engine")
public class EngineResource extends AbstractCockpitEngineResource {

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
			resultJSON.put("enabled", CockpitEngine.isEnabled());
			resultJSON.put("creationDate", CockpitEngine.getCreationDate());
			long uptime = System.currentTimeMillis() - CockpitEngine.getCreationDate().getTime();
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
