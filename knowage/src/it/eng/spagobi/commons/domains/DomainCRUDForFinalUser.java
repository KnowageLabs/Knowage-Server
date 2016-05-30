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

import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Provide a subset of functionalities of the DomainCRUD. This replication of the code is useful to give more than one SpagoBI functionality to the same
 * physical functionality
 */
@Path("/domainsforfinaluser")
@ManageAuthorization
public class DomainCRUDForFinalUser extends DomainCRUD {

	@Override
	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	@Path("/listValueDescriptionByType")
	@Produces(MediaType.APPLICATION_JSON)
	public String getListDomainsByType(@Context HttpServletRequest req) {
		return super.getListDomainsByType(req);
	}

	@SuppressWarnings("unchecked")
	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	@Path("/ds-categories")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public String getDataSetCategoriesByUser(@Context HttpServletRequest req) {
		IRoleDAO rolesDao = null;
		Set<Domain> categories = new HashSet<Domain>();
		try {
			List<String> roleNames = (List<String>) getUserProfile().getRoles();
			if (!roleNames.isEmpty()) {
				rolesDao = DAOFactory.getRoleDAO();
				rolesDao.setUserProfile(getUserProfile());
				List<Domain> array = DAOFactory.getDomainDAO().loadListDomainsByType("CATEGORY_TYPE");
				for (String roleName : roleNames) {
					Role role = rolesDao.loadByName(roleName);
					List<RoleMetaModelCategory> ds = rolesDao.getMetaModelCategoriesForRole(role.getId());
					for (RoleMetaModelCategory r : ds) {
						for (Domain dom : array) {
							if (r.getCategoryId().equals(dom.getValueId())) {
								categories.add(dom);
							}
						}

					}
				}
			}
			return translate(categories, getLocale(req)).toString();
		} catch (Exception e) {
			logger.error("Impossible to get role dataset categories for user [" + getUserProfile() + "]", e);
			throw new SpagoBIRestServiceException("Impossible to get role dataset categories for user [" + getUserProfile() + "]", buildLocaleFromSession(), e);
		}
	}
}
