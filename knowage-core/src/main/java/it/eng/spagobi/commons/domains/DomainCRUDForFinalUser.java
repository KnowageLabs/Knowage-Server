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
package it.eng.spagobi.commons.domains;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

/**
 * Provide a subset of functionalities of the DomainCRUD. This replication of the code is useful to give more than one SpagoBI functionality to the same
 * physical functionality
 */
@Path("/domainsforfinaluser")
public class DomainCRUDForFinalUser extends DomainCRUD {

	@Override
	@GET
	@Path("/listValueDescriptionByType")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getListDomainsByType(@Context HttpServletRequest req) {
		return super.getListDomainsByType(req);
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/ds-categories")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public String getDataSetCategoriesByUser(@Context HttpServletRequest req) {
		Set<Domain> categories = new HashSet<Domain>();
		try {
			categories = UserUtilities.getDataSetCategoriesByUser(getUserProfile());
			return translate(categories, getLocale(req)).toString();
		} catch (Exception e) {
			logger.error("Impossible to get role dataset categories for user [" + getUserProfile() + "]", e);
			throw new SpagoBIRestServiceException("Impossible to get role dataset categories for user [" + getUserProfile() + "]", buildLocaleFromSession(), e);
		}
	}

	@GET
	@Path("/bm-categories")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public String getBusinessModelsCategoriesByUser(@Context HttpServletRequest req) {
		Set<Domain> categories = new HashSet<Domain>();
		try {
			categories = UserUtilities.getBusinessModelsCategoriesByUser(getUserProfile());
			return translate(categories, getLocale(req)).toString();
		} catch (Exception e) {
			logger.error("Impossible to get role models categories for user [" + getUserProfile() + "]", e);
			throw new SpagoBIRestServiceException("Impossible to get role models categories for user [" + getUserProfile() + "]", buildLocaleFromSession(), e);
		}
	}
}
