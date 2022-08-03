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

/**
 *
 */
package it.eng.spagobi.api.v3;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author albnale
 *
 */
@Path("/3.0/category")

public class CategoryResource extends AbstractSpagoBIResource {
// logger component-
	private static final Logger logger = Logger.getLogger(CategoryResource.class);

	@GET
	@Path("/listByCode/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Domain> getDomainsByCode(@PathParam("code") String type) {
		logger.debug("IN");
		ICategoryDAO categoryDAO = null;

		List<Domain> categories;

		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(getUserProfile());
			categories = categoryDAO.getCategories(type).stream().map(Domain::fromCategory).collect(Collectors.toList());

		} catch (Exception e) {
			String message = "Error while getting categories " + type;
			logger.error(message, e);
			throw new SpagoBIRuntimeException(message, e);
		} finally {
			logger.debug("OUT");
		}
		return categories;
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return request;
	}

	@Override
	public HttpServletResponse getServletResponse() {
		return response;
	}

	@Override
	public UserProfile getUserProfile() {
		return UserProfileManager.getProfile();
	}
}
