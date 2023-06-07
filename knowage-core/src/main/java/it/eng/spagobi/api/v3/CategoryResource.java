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
import it.eng.spagobi.api.dto.CategoryDTO;
import it.eng.spagobi.api.v3.service.CategoryService;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.Target;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * @author albnale
 *
 */
@Path("/3.0/category")
public class CategoryResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = Logger.getLogger(CategoryResource.class);

	@GET
	@Path("/listByCode/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Domain> getDomainsByCode(@PathParam("code") String type) {
		LOGGER.debug("IN");
		ICategoryDAO categoryDAO = null;

		List<Domain> categories;

		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(getUserProfile());
			categories = categoryDAO.getCategories(type).stream().map(Domain::fromCategory).collect(Collectors.toList());

		} catch (Exception e) {
			String message = "Error while getting categories " + type;
			LOGGER.error(message, e);
			throw new SpagoBIRuntimeException(message, e);
		} finally {
			LOGGER.debug("OUT");
		}
		return categories;
	}

	@GET
	@Path("/categories")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CATEGORY_MANAGEMENT })
	public List<CategoryDTO> getCategoriesDTO() {
		LOGGER.debug("IN");
		final UserProfile userProfile = getUserProfile();
		List<CategoryDTO> listToReturn = new ArrayList<>();
		try {
			CategoryService cs = new CategoryService();
			listToReturn = cs.getCategories(userProfile);
		} catch (Exception ex) {
			LogMF.error(LOGGER, "Cannot get available categories for user {0}", new Object[] { userProfile.getUserName() });
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", ex);
		} finally {
			LOGGER.debug("OUT");
		}

		return listToReturn;
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CATEGORY_MANAGEMENT })
	public List<SbiCategory> getCategories() {
		LOGGER.debug("IN");
		ICategoryDAO categoryDAO = null;
		final UserProfile userProfile = getUserProfile();
		List<SbiCategory> listToReturn = new ArrayList<>();
		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(getUserProfile());
			listToReturn = categoryDAO.getCategories().stream().collect(Collectors.toList());
		} catch (Exception ex) {
			LogMF.error(LOGGER, "Cannot get available categories for user {0}", new Object[] { userProfile.getUserName() });
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", ex);
		} finally {
			LOGGER.debug("OUT");
		}

		return listToReturn;
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CATEGORY_MANAGEMENT })
	public SbiCategory getCategoryById(@PathParam("id") Integer sbiCategoryId) {
		LOGGER.debug("IN");
		ICategoryDAO categoryDAO = null;
		SbiCategory toReturn = null;
		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(getUserProfile());
			toReturn = categoryDAO.getCategory(sbiCategoryId);
		} catch (Exception e) {
			throw new SpagoBIServiceException("Error getting category with id " + sbiCategoryId, e);
		} finally {
			LOGGER.debug("OUT");
		}
		return toReturn;

	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CATEGORY_MANAGEMENT })
	public SbiCategory categoryCreate(@Valid SbiCategory sbiCategory) {
		LOGGER.debug("IN");
		ICategoryDAO categoryDAO = null;
		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(getUserProfile());
			categoryDAO.create(sbiCategory);
		} catch (Exception e) {
			throw new SpagoBIServiceException("Cannot create sbiCategory " + Optional.ofNullable(sbiCategory).map(SbiCategory::getName).orElse("null"), e);
		} finally {
			LOGGER.debug("OUT");
		}
		return sbiCategory;

	}

	@POST
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CATEGORY_MANAGEMENT })
	public SbiCategory categoryUpdate(@Valid SbiCategory newSbiCategory) {

		LOGGER.debug("IN");
		ICategoryDAO categoryDAO = null;
		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(getUserProfile());
			categoryDAO.update(newSbiCategory);
		} catch (Exception e) {
			throw new SpagoBIServiceException("Error updating SbiCategory with id " + newSbiCategory.getId(), e);
		} finally {
			LOGGER.debug("OUT");
		}
		return newSbiCategory;

	}

	@DELETE
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CATEGORY_MANAGEMENT })
	public Response categoryDelete(@Valid SbiCategory newSbiCategory) {
		Response response = null;
		LOGGER.debug("IN");
		ICategoryDAO categoryDAO = null;
		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(getUserProfile());
			categoryDAO.delete(newSbiCategory);
			response = Response.status(Response.Status.OK).build();
		} catch (Exception e) {
			throw new SpagoBIServiceException("Error deleting SbiCategory with id " + newSbiCategory.getId(), e);
		} finally {
			LOGGER.debug("OUT");
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

	@GET
	@Path("/dataset/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CATEGORY_MANAGEMENT })
	public Response getDatasetsById(@PathParam("id") int sbiCategoryId) {
		LOGGER.debug("IN");
		Response response = null;
		List<IDataSet> toReturn = new ArrayList<>();
		try {
			CategoryService cs = new CategoryService();
			toReturn = cs.getDatasetsUsedByCategory(sbiCategoryId);
			List<CategoryObjectDTO> objToReturn = new ArrayList<>();
			for (IDataSet iDataSet : toReturn) {
				CategoryObjectDTO co = new CategoryObjectDTO(iDataSet.getLabel());
				objToReturn.add(co);
			}

			// TODO: create a FACADE for every different object ?

//			List<DataSetResourceSimpleFacade> collect = toReturn.stream().map(DataSetResourceSimpleFacade::new).collect(toList());
//
//			DataSetResourceResponseRoot<DataSetResourceSimpleFacade> of = new DataSetResourceResponseRoot<>(collect);

			response = Response.status(Response.Status.OK).entity(objToReturn).build();

		} catch (Exception e) {
			throw new SpagoBIServiceException("Error getting category with id " + sbiCategoryId, e);
		} finally {
			LOGGER.debug("OUT");
		}
		return response;

	}

	@GET
	@Path("/metamodel/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CATEGORY_MANAGEMENT })
	public Response getMetamodelsById(@PathParam("id") int sbiCategoryId) {
		LOGGER.debug("IN");
		List<MetaModel> toReturn = new ArrayList<>();
		Response response = null;
		try {
			CategoryService cs = new CategoryService();
			toReturn = cs.getMetaModelsUsedByCategory(sbiCategoryId);
			List<CategoryObjectDTO> objToReturn = new ArrayList<>();
			for (MetaModel meta : toReturn) {
				CategoryObjectDTO co = new CategoryObjectDTO(meta.getName());
				objToReturn.add(co);
			}
			response = Response.status(Response.Status.OK).entity(objToReturn).build();
		} catch (Exception e) {
			throw new SpagoBIServiceException("Error getting category with id " + sbiCategoryId, e);
		} finally {
			LOGGER.debug("OUT");
		}
		return response;

	}

	@GET
	@Path("/geolayer/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CATEGORY_MANAGEMENT })
	public Response getGeoLayersById(@PathParam("id") Integer sbiCategoryId) {
		LOGGER.debug("IN");
		Response response = null;
		List<GeoLayer> toReturn = new ArrayList<>();
		try {
			List<CategoryObjectDTO> objToReturn = new ArrayList<>();
			CategoryService cs = new CategoryService();
			toReturn = cs.getGeoLayersUsedByCategory(sbiCategoryId);
			for (GeoLayer geo : toReturn) {
				CategoryObjectDTO co = new CategoryObjectDTO(geo.getName());
				objToReturn.add(co);
			}
			response = Response.status(Response.Status.OK).entity(objToReturn).build();
		} catch (Exception e) {
			throw new SpagoBIServiceException("Error getting category with id " + sbiCategoryId, e);
		} finally {
			LOGGER.debug("OUT");
		}
		return response;

	}

	@GET
	@Path("/kpi/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CATEGORY_MANAGEMENT })
	public Response getKpiById(@PathParam("id") Integer sbiCategoryId) {
		LOGGER.debug("IN");
		List<Kpi> toReturn = new ArrayList<>();
		Response response = null;
		try {
			CategoryService cs = new CategoryService();
			toReturn = cs.getKPIUsedByCategory(sbiCategoryId);
			List<CategoryObjectDTO> objToReturn = new ArrayList<>();
			for (Kpi kpi : toReturn) {
				CategoryObjectDTO co = new CategoryObjectDTO(kpi.getName());
				objToReturn.add(co);
			}
			response = Response.status(Response.Status.OK).entity(objToReturn).build();
		} catch (Exception e) {
			throw new SpagoBIServiceException("Error getting category with id " + sbiCategoryId, e);
		} finally {
			LOGGER.debug("OUT");
		}
		return response;

	}

	@GET
	@Path("/kpitarget/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CATEGORY_MANAGEMENT })
	public Response getKpiTargetById(@PathParam("id") Integer sbiCategoryId) {
		LOGGER.debug("IN");
		List<Target> toReturn = new ArrayList<>();
		Response response = null;
		try {
			CategoryService cs = new CategoryService();
			toReturn = cs.getKPITargetUsedByCategory(sbiCategoryId);
			List<CategoryObjectDTO> objToReturn = new ArrayList<>();
			for (Target kpiT : toReturn) {
				CategoryObjectDTO co = new CategoryObjectDTO(kpiT.getName());
				objToReturn.add(co);
			}
			response = Response.status(Response.Status.OK).entity(objToReturn).build();
		} catch (Exception e) {
			throw new SpagoBIServiceException("Error getting category with id " + sbiCategoryId, e);
		} finally {
			LOGGER.debug("OUT");
		}
		return response;

	}

	@GET
	@Path("/kpiruleoutput/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CATEGORY_MANAGEMENT })
	public Response getKpiRuleOutputById(@PathParam("id") Integer sbiCategoryId) {
		LOGGER.debug("IN");
		List<RuleOutput> toReturn = new ArrayList<>();
		Response response = null;
		try {
			CategoryService cs = new CategoryService();
			toReturn = cs.getKpiRuleOutputUsedByCategory(sbiCategoryId);
			List<CategoryObjectDTO> objToReturn = new ArrayList<>();
			for (RuleOutput kpi : toReturn) {
				CategoryObjectDTO co = new CategoryObjectDTO(kpi.getAlias());
				objToReturn.add(co);
			}
			response = Response.status(Response.Status.OK).entity(objToReturn).build();
		} catch (Exception e) {
			throw new SpagoBIServiceException("Error getting category with id " + sbiCategoryId, e);
		} finally {
			LOGGER.debug("OUT");
		}
		return response;

	}
}
