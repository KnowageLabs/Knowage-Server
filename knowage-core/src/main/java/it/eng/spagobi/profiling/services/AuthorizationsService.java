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
package it.eng.spagobi.profiling.services;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.knowage.security.ProductProfiler;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.dao.ITenantsDAO;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/authorizations")
public class AuthorizationsService {

	static private Logger logger = Logger.getLogger(AuthorizationsService.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	public String getAuthorizations(@Context HttpServletRequest req) {
		JSONObject documentJSON = new JSONObject();

		try {
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			if (profile instanceof UserProfile) {
				UserProfile spagoBIUserProfile = (UserProfile) profile;
				String organization = spagoBIUserProfile.getOrganization();

				ITenantsDAO tenantsDAO = DAOFactory.getTenantsDAO();
				List<Integer> productTypeIds = tenantsDAO.loadSelectedProductTypesIds(organization);
				IRoleDAO roleDAO = DAOFactory.getRoleDAO();
				List<String> authorizationsWithDuplicates = roleDAO.loadAllAuthorizationsNamesByProductTypes(productTypeIds);
				Set<String> authorizations = ProductProfiler.filterAuthorizationsByProduct(authorizationsWithDuplicates);
				JSONArray authorizationsJSONArray = new JSONArray();

				for (String authorization : authorizations) {
					JSONObject authorizationJSONObject = new JSONObject();
					authorizationJSONObject.put("name", authorization);
					authorizationsJSONArray.put(authorizationJSONObject);
				}

				documentJSON.put("root", authorizationsJSONArray);
			}
			return documentJSON.toString();
		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving authorizations");
			throw new SpagoBIServiceException("An unexpected error occured while authorizations", t);
		}

	}

}
