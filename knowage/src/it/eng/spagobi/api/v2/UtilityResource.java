/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.net.URLDecoder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 */
@Path("/2.0/utilities")
@ManageAuthorization
public class UtilityResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(UtilityResource.class);

	@GET
	@Path("/jndi")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Produces(MediaType.TEXT_PLAIN + "; charset=UTF-8")
	public String getJndiValue(@QueryParam("label") String jndiLabel) {
		logger.debug("IN");
		try {
			checkIsSuperadmin();
			String value = SpagoBIUtilities.readJndiResource(URLDecoder.decode(jndiLabel));
			return value;
		} catch (Exception e) {
			String message = "Error while getting the JNDI resource with label [" + jndiLabel + "]";
			logger.error(message, e);
			throw new SpagoBIRuntimeException(message, e);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/tenant")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getTenant() {
		logger.debug("IN");
		try {
			checkIsSuperadmin();
			Tenant tenant = TenantManager.getTenant();
			return JsonConverter.objectToJson(tenant, Tenant.class);
		} catch (Exception e) {
			String message = "Error while getting tenant";
			logger.error(message, e);
			throw new SpagoBIRuntimeException(message, e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void checkIsSuperadmin() throws Exception {
		UserProfile userProfile = getUserProfile();
		if (!userProfile.getIsSuperadmin()) {
			throw new Exception("Access denied, superadmin role required");
		}
	}
}
