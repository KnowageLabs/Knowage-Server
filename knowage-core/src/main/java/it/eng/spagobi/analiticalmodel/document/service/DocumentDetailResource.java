/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.analiticalmodel.document.service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.knowage.security.ProductProfiler;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/1.0/document-detail")
public class DocumentDetailResource extends AbstractSpagoBIResource {

	static private Logger logger = Logger.getLogger(DocumentDetailResource.class);

	@GET
	@Path("/engines")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.READ_ENGINES_MANAGEMENT, SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public Response getEngines() {
		logger.debug("IN");

		try {
			IEngineDAO engineDao = DAOFactory.getEngineDAO();
			List<Engine> engines = ProductProfiler.filterEnginesByProduct(engineDao.loadAllEnginesByTenant());
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
	@Path("/types")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentTypes() {
		logger.debug("IN");

		try {
			IDomainDAO domainDao = DAOFactory.getDomainDAO();
			List docTypes = ProductProfiler.filterDocumentTypesByProduct(domainDao.loadListDomainsByTypeAndTenant("BIOBJ_TYPE"));
			ObjectMapper mapper = new ObjectMapper();
			return Response.ok(mapper.writeValueAsString(docTypes)).build();
		} catch (Exception e) {
			logger.error("Error while getting the list of document types", e);
			throw new SpagoBIRuntimeException("Error while getting the list of document types", e);
		} finally {
			logger.debug("OUT");
		}
	}
}
