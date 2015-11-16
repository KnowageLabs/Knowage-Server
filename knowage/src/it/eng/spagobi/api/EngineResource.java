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
package it.eng.spagobi.api;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 *
 */
@Path("/1.0/engines")
@ManageAuthorization
public class EngineResource extends AbstractSpagoBIResource {
	static private Logger logger = Logger.getLogger(EngineResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.READ_ENGINES_MANAGEMENT, SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public Response getEngines() {
		logger.debug("IN");
		IEngineDAO engineDao = null;
		List<Engine> engines = null;

		try {
			engineDao = DAOFactory.getEngineDAO();
			engines = engineDao.loadAllEngines();
			ObjectMapper mapper = new ObjectMapper();
			return Response.ok(mapper.writeValueAsString(engines)).build();
		} catch (Exception e) {
			logger.error("Error while getting the list of engines", e);
			throw new SpagoBIRuntimeException("Error while getting the list of engines", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.READ_ENGINES_MANAGEMENT, SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public Response getEngine(@PathParam("label") String label) {
		logger.debug("IN");
		IEngineDAO engineDao = null;
		Engine engine = null;

		try {
			engineDao = DAOFactory.getEngineDAO();
			engine = engineDao.loadEngineByLabel(label);
			ObjectMapper mapper = new ObjectMapper();
			return Response.ok(mapper.writeValueAsString(engine)).build();
		} catch (Exception e) {
			logger.error("Error while getting the engine with label [" + label + "]", e);
			throw new SpagoBIRuntimeException("Error while getting the engine with label [" + label + "]", e);
		} finally {
			logger.debug("OUT");
		}
	}
}
