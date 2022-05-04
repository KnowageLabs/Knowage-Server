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
package it.eng.spagobi.api.v2;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.knowage.monitor.IKnowageMonitor;
import it.eng.knowage.monitor.KnowageMonitorFactory;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */
@Path("/2.0/folders")
@ManageAuthorization
public class FolderResource extends AbstractSpagoBIResource {
	static protected Logger logger = Logger.getLogger(FolderResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getFolders(@DefaultValue("false") @QueryParam("includeDocs") Boolean recoverBIObjects, @QueryParam("perm") String permissionOnFolder,
			@QueryParam("dateFilter") String dateFilter, @QueryParam("status") String status) {
		logger.debug("IN");

		IKnowageMonitor monitor = KnowageMonitorFactory.getInstance().start("knowage.folders.list");

		try {
			FolderManagementAPI folderManagementUtilities = new FolderManagementAPI();
			String jsonObjects = folderManagementUtilities.getFoldersAsString(recoverBIObjects, permissionOnFolder, dateFilter, status);
			Response response = Response.ok(jsonObjects).build();

			monitor.stop();

			return response;
		} catch (Exception e) {
			monitor.stop(e);
			throw e;
		}

	}

}
