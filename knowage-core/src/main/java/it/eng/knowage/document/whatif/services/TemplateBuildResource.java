/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.document.whatif.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

// this class was made to support Vue developments and substitute old jsp: templateBuild.jsp

@Path("/olap/designer")
public class TemplateBuildResource extends AbstractSpagoBIResource {

	private static Logger logger = Logger.getLogger(TemplateBuildResource.class);

	@GET
	@Path("/{objId}")
	public Response getDesignerInfo(@Context HttpServletRequest req, @PathParam("objId") Integer objId) {
		try {
			UserProfile profile = getUserProfile();
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(objId);
			String driverClassName = obj.getEngine().getDriverName();
			IEngineDriver aEngineDriver = (IEngineDriver) Class.forName(driverClassName).newInstance();
			EngineURL engineurl = aEngineDriver.getEditDocumentTemplateBuildUrl(obj, profile);
			return Response.ok(engineurl.getParameters()).build();
		} catch (Exception e) {
			logger.error("Error while getting olap designer info", e);
			throw new SpagoBIRuntimeException("Error while getting olap designer info", e);
		}
	}
}
