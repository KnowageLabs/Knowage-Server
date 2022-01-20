/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.backendservices.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import it.eng.knowage.security.ProductProfiler;

@Path("/2.0/backendservices/productprofiler")
public class ProductProfilerResource {

	static protected Logger logger = Logger.getLogger(ProductProfilerResource.class);

	@GET
	@Path("cockpit/widget")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean canCreateWidget(@QueryParam("type") String type) {
		return ProductProfiler.canCreateWidget(type);
	}

	@GET
	@Path("cockpit/functions")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean canUseFunctions() {
		return ProductProfiler.canUseFunctions();
	}

}
