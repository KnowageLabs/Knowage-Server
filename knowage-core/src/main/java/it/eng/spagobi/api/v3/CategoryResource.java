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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

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

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public List<SbiCategory> getCategories() {
		logger.debug("IN");
		ICategoryDAO categoryDAO = null;
		final UserProfile userProfile = getUserProfile();
		List<SbiCategory> listToReturn = new ArrayList<SbiCategory>();
		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(getUserProfile());
			listToReturn = categoryDAO.getCategories().stream().collect(Collectors.toList());
		} catch (Exception ex) {
			LogMF.error(logger, "Cannot get available categories for user {0}", new Object[] { userProfile.getUserName() });
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", ex);
		} finally {
			logger.debug("OUT");
		}

		return listToReturn;
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public SbiCategory getCategoryById(@PathParam("id") Integer sbiCategoryId) {
		logger.debug("IN");
		ICategoryDAO categoryDAO = null;
		SbiCategory toReturn = null;
		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(getUserProfile());
			toReturn = categoryDAO.getCategory(sbiCategoryId);
		} catch (Exception e) {
			throw new SpagoBIServiceException("Error getting category with id " + sbiCategoryId, e);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;

	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public SbiCategory categoryCreate(@Valid SbiCategory sbiCategory) {
		logger.debug("IN");
		ICategoryDAO categoryDAO = null;
		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(getUserProfile());
			categoryDAO.create(sbiCategory);
		} catch (Exception e) {
			throw new SpagoBIServiceException("Cannot create sbiCategory " + Optional.ofNullable(sbiCategory).map(SbiCategory::getName).orElse("null"), e);
		} finally {
			logger.debug("OUT");
		}
		return sbiCategory;

	}

	@POST
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public SbiCategory categoryUpdate(@Valid SbiCategory newSbiCategory) {

		logger.debug("IN");
		ICategoryDAO categoryDAO = null;
		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(getUserProfile());
			categoryDAO.update(newSbiCategory);
		} catch (Exception e) {
			throw new SpagoBIServiceException("Error updating SbiCategory with id " + String.valueOf(newSbiCategory.getId()), e);
		} finally {
			logger.debug("OUT");
		}
		return newSbiCategory;

	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response categoryDelete(@Valid SbiCategory newSbiCategory) {
		Response response = null;
		logger.debug("IN");
		ICategoryDAO categoryDAO = null;
		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(getUserProfile());
			categoryDAO.delete(newSbiCategory);
			response = Response.status(Response.Status.OK).build();
		} catch (Exception e) {
			throw new SpagoBIServiceException("Error deleting SbiCategory with id " + String.valueOf(newSbiCategory.getId()), e);
		} finally {
			logger.debug("OUT");
		}
		return response;

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
