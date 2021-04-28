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

package it.eng.spagobi.api.v3;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.profiling.bo.UserInformationDTO;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

@Path("2.0/currentuser")

public class CurrentUserResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public UserInformationDTO getCurrentUserInformation(@Context HttpServletRequest httpRequest) throws EMFInternalError {

		UserProfile userProfile = getUserProfile();

		if (userProfile == null) {
			String message = "UserProfile is null";
			logger.error(message);
			throw new SpagoBIEngineRuntimeException(message);
		}

		return new UserInformationDTO(userProfile);

	}

}