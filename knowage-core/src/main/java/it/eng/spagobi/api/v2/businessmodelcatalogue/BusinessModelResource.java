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
package it.eng.spagobi.api.v2.businessmodelcatalogue;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.log4j.Logger;
import org.json.JSONObjectDeserializator;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.catalogue.dao.SpagoBIDAOMetaModelNameExistingException;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.JSError;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/businessmodels")
@ManageAuthorization
public class BusinessModelResource extends AbstractSpagoBIResource {

	/**
	 *
	 */
	public static enum FILETYPE {
		JAR, LOG, SBIMODEL
	};

	private static final String LOG_SUFFIX = ".log";

	static protected Logger logger = Logger.getLogger(BusinessModelResource.class);

	/**
	 * Get all business models
	 **/
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<MetaModel> getBusinessModels(@QueryParam("fileExtension") String fileExtension) {
		logger.debug("IN");

		List<MetaModel> businessModelList = new ArrayList<MetaModel>();
		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		businessModelsDAO.setUserProfile(getUserProfile());
		try {
			if (getUserProfile().getFunctionalities().contains(SpagoBIConstants.META_MODELS_CATALOGUE_MANAGEMENT)) {
				businessModelList = businessModelsDAO.loadAllMetaModels();
			} else {
				IRoleDAO roleDao = DAOFactory.getRoleDAO();
				roleDao.setUserProfile(getUserProfile());

				List<String> roleNames = UserUtilities.getCurrentRoleNames(getUserProfile());
				List<Integer> categories = roleDao.getMetaModelCategoriesForRoles(roleNames);
				logger.debug("Found the following categories [" + categories + "].");
				if (categories != null && !categories.isEmpty()) {
					businessModelList = businessModelsDAO.loadMetaModelByCategories(categories);
				}
			}

			List<MetaModel> filteredBusinessModels = new ArrayList<MetaModel>();
			if (fileExtension != null) {
				for (MetaModel bm : businessModelList) {
					Content content = businessModelsDAO.loadActiveMetaModelContentById(bm.getId());
					if (content != null && content.getFileName().endsWith(fileExtension)) {
						filteredBusinessModels.add(bm);
					}
				}
			}

			return businessModelList;

		} catch (Exception e) {
			logger.error("An error occurred while getting all business models from database!", e);
			throw new SpagoBIRestServiceException("An error occurred while getting all business models from database!", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

	@GET
	@Path("/bmCategories")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<Domain> getProfiledCats() {

		IRoleDAO rolesDao = null;
		Role role = new Role();
		try {
			UserProfile profile = this.getUserProfile();
			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(profile);

			IDomainDAO domainDao = DAOFactory.getDomainDAO();
			ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();

			if (UserUtilities.hasDeveloperRole(profile) && !UserUtilities.hasAdministratorRole(profile)) {
				List<Domain> categoriesDev = new ArrayList<>();
				Collection<String> roles = profile.getRolesForUse();
				Iterator<String> itRoles = roles.iterator();
				while (itRoles.hasNext()) {
					String roleName = itRoles.next();
					role = rolesDao.loadByName(roleName);
					List<RoleMetaModelCategory> ds = rolesDao.getMetaModelCategoriesForRole(role.getId());

					List<Domain> array = categoryDao.getCategoriesForDataset()
						.stream()
						.map(Domain::fromCategory)
						.collect(toList());

					for (RoleMetaModelCategory r : ds) {
						for (Domain dom : array) {
							if (r.getCategoryId().equals(dom.getValueId())) {
								categoriesDev.add(dom);
							}
						}
					}
				}
				return categoriesDev;
			} else {
				return categoryDao.getCategoriesForDataset()
					.stream()
					.map(Domain::fromCategory)
					.collect(toList());
			}
		} catch (Exception e) {
			logger.error("Role with selected id: " + role.getId() + " doesn't exists", e);
			throw new SpagoBIRuntimeException("Item with selected id: " + role.getId() + " doesn't exists", e);
		}

	}

	/**
	 * Get all versions of business model with specified id
	 **/
	@GET
	@Path("{bmId}/versions")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getBusinessModelVersions(@PathParam("bmId") Integer bmId) {
		logger.debug("IN");
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		List<Content> versions = null;
		List<Content> versionsToShow = new ArrayList<Content>();
		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		businessModelsDAO.setUserProfile(getUserProfile());
		try {
			versions = businessModelsDAO.loadMetaModelVersions(bmId);
			for (Content version : versions) {
				String filename = version.getFileName();
				int typePos = filename.lastIndexOf(".");
				if (typePos > 0) {
					version.setFileName(filename.substring(0, typePos));
				}
				versionsToShow.add(version);
			}

			boolean togenerate = isBusinessModelToBeGenerated(bmId);

			// return versions;
			resultAsMap.put("versions", versionsToShow);
			resultAsMap.put("togenerate", togenerate);

		} catch (Exception e) {
			logger.error("An error occurred while getting versions of business model with id:" + bmId, e);
			throw new SpagoBIRestServiceException("An error occurred while getting versions of business model with id:" + bmId, buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
		return Response.ok(resultAsMap).build();
	}

	private boolean isBusinessModelToBeGenerated(Integer businessModelId) {
		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		Content activeContent = businessModelsDAO.loadActiveMetaModelContentById(businessModelId);
		// @formatter:off
		if (
				activeContent != null // model may have no active version, in case it is new
				&& activeContent.getFileModel() != null // model must have a model file
				&& (activeContent.getContent() == null || activeContent.getFileName().endsWith(LOG_SUFFIX))) {
			return true;
		}
		// @formatter:on
		return false;
	}

	/**
	 * Get business model with specified id
	 **/
	@GET
	@Path("{bmId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MetaModel getBusinessModelById(@PathParam("bmId") Integer bmId) {
		logger.debug("IN");

		MetaModel businessModel;
		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		businessModelsDAO.setUserProfile(getUserProfile());

		try {
			businessModel = businessModelsDAO.loadMetaModelById(bmId);

			return businessModel;
		} catch (Exception e) {
			logger.error("An error occurred while getting business model with id:" + bmId, e);
			throw new SpagoBIRestServiceException("An error occurred while getting business model with id:" + bmId, buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Get version of business model with {bmId} and with specified version id {vId}
	 **/
	@GET
	@Path("{bmId}/versions/{vId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Content getBusinessModelVersionById(@PathParam("bmId") Integer bmId, @PathParam("vId") Integer vId) {
		logger.debug("IN");
		Content content = null;

		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		businessModelsDAO.setUserProfile(getUserProfile());

		try {
			content = businessModelsDAO.loadMetaModelContentById(vId);

			return content;
		} catch (Exception e) {
			logger.error("An error occurred while getting version with id:" + vId + " of business model with id:" + bmId, e);
			throw new SpagoBIRestServiceException("An error occurred while getting version with id:" + vId + " of business model with id:" + bmId,
					buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * File upload
	 **/
	@POST
	@Path("/{bmId}/versions")
	@UserConstraint(functionalities = { SpagoBIConstants.META_MODELS_CATALOGUE_MANAGEMENT })
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(MultiPartBody body, @PathParam("bmId") int bmId) {

		Content content = new Content();
		byte[] bytes = null;

		IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
		dao.setUserProfile(getUserProfile());

		final FormFile file = body.getFormFileParameterValues("file")[0];

		content.setFileName(file.getFileName());
		bytes = file.getContent();

		if (file.getFileName().endsWith("sbimodel")) {
			// .sbimodel file
			content.setFileModel(bytes);
		} else {
			// .jar or other file
			content.setContent(bytes);
		}
		content.setCreationDate(new Date());
		content.setCreationUser(getUserProfile().getUserName().toString());

		dao.insertMetaModelContent(bmId, content);

		return Response.status(200).build();

	}

	/**
	 * Get file from data base for download with specified id (in progress)
	 **/
	@GET
	@Path("{bmId}/versions/{vId}/{filetype}/file")
	public Response downloadFile(@PathParam("vId") Integer vId, @PathParam("filetype") FILETYPE filetype) {
		logger.debug("IN");
		ResponseBuilder response = Response.ok();

		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		businessModelsDAO.setUserProfile(getUserProfile());
		Content content = businessModelsDAO.loadMetaModelContentById(vId);
		String filename = content.getFileName();
		int typePos = filename.lastIndexOf(".");
		if (typePos > 0) {
			filename = filename.substring(0, typePos) + "." + filetype.name().toLowerCase();
		}
		byte[] byteContent = null;
		switch (filetype) {
		case JAR:
			byteContent = content.getContent();
			response = Response.ok(byteContent);
			response.header("Content-Disposition", "attachment; filename=" + filename);
			response.header("Content-Type", "application/java-archive");
			break;
		case LOG:
			byteContent = content.getContent();
			response = Response.ok(byteContent);
			response.header("Content-Disposition", "attachment; filename=" + filename);
			break;
		case SBIMODEL:
			byteContent = content.getFileModel();
			response = Response.ok(byteContent);
			response.header("Content-Disposition", "attachment; filename=" + filename);
			break;
		default:
			response = Response.ok(new JSError().addError("Not valid filetype [" + filetype + "]"));
			break;
		}

		logger.debug("OUT");
		return response.build();
	}

	/**
	 * Insert new business model POST
	 **/
	@POST
	@Path("/")
	@Consumes("application/json")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public MetaModel insertNewBusinessModel(@Valid MetaModel body) {
		logger.debug("IN");
		MetaModel bm = body;

		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		businessModelsDAO.setUserProfile(getUserProfile());

		try {
			if (bm.getId() != null) {
				logger.error("New business model should not have id");
				bm = new MetaModel();
				return bm;
			}
			if (businessModelsDAO.loadMetaModelByName(bm.getName()) != null) {
				throw new SpagoBIDAOMetaModelNameExistingException("Error while trying to add new business model with existing name");
			}
			businessModelsDAO.insertMetaModel(bm);
			MetaModel insertedBM = businessModelsDAO.loadMetaModelByName(bm.getName());

			if (insertedBM.getModelLocked()) {
				businessModelsDAO.lockMetaModel(insertedBM.getId(), (String) getUserProfile().getUserId());
			}
			return insertedBM;
		} catch (SpagoBIDAOMetaModelNameExistingException e) {
			logger.error("Error while trying to add new business model with existing name", e);
			throw new SpagoBIRestServiceException("A model with same name already exists", buildLocaleFromSession(), e);
		} catch (Exception e) {
			logger.error("An error occurred while inserting new business model in database", e);
			throw new SpagoBIRestServiceException("An error occurred while inserting new business model in database", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Edit existing business model with specified id PUT
	 **/
	@PUT
	@Path("/{bmId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public MetaModel updateBusinessModel(@PathParam("bmId") Integer bmId, @Valid MetaModel body) {
		logger.debug("IN");

		MetaModel bm = body;
		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		boolean isLockedInDB = businessModelsDAO.loadMetaModelById(bmId).getModelLocked();
		businessModelsDAO.setUserProfile(getUserProfile());
		try {
			if (bm.getModelLocked() && !isLockedInDB) {
				businessModelsDAO.lockMetaModel(bmId, (String) getUserProfile().getUserId());
			} else if (isLockedInDB) {
				businessModelsDAO.unlockMetaModel(bmId, (String) getUserProfile().getUserId());
			}
			businessModelsDAO.modifyMetaModel(bm);
			// businessModelsDAO.setActiveVersion(bm.getId(), bm.);
			return bm;
		} catch (Exception e) {
			logger.error("An error occurred while updating business model with id:" + bmId, e);
			throw new SpagoBIRestServiceException("An error occurred while updating business model with id:" + bmId, buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Update active version
	 **/
	@PUT
	@Path("{bmId}/versions/{vId}")
	public Content updateActiveVersion(@PathParam("bmId") Integer bmId, @PathParam("vId") Integer vId) {
		logger.debug("IN");

		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		businessModelsDAO.setUserProfile(getUserProfile());

		try {
			businessModelsDAO = DAOFactory.getMetaModelsDAO();
			businessModelsDAO.setActiveVersion(bmId, vId);
			return businessModelsDAO.loadActiveMetaModelContentById(bmId);
		} catch (Exception e) {
			logger.error("An error occurred while updating active version of business model with id:" + bmId, e);
			throw new SpagoBIRestServiceException("An error occurred while updating active version of business model with id:" + bmId, buildLocaleFromSession(),
					e);

		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Delete business model with specified id
	 **/
	@DELETE
	@Path("/{bmId}")
	public Response deleteBusinessModel(@PathParam("bmId") Integer bmId) {
		logger.debug("IN");

		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		businessModelsDAO.setUserProfile(getUserProfile());
		IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
		List<IDataSet> dataSets;
		Map<String, Object> configurationMap = null;
		String sbiQbeDS = "SbiQbeDataSet";
		String qbeDatamarts = "qbeDatamarts";
		try {
			dsDao.setUserProfile(getUserProfile());
			dataSets = dsDao.loadFilteredDatasetByTypeList(getUserProfile().getUserId().toString(), sbiQbeDS);
			MetaModel businessModel = businessModelsDAO.loadMetaModelById(bmId);
			for (IDataSet dataSet : dataSets) {
				try {
					configurationMap = JSONObjectDeserializator.getHashMapFromString(dataSet.getConfiguration());
				} catch (Exception e) {
					logger.debug("Configuration cannot be transformed from string to map");
					throw new SpagoBIRestServiceException("Configuration cannot be transformed from string to map", buildLocaleFromSession(), e);
				}

				if (businessModel.getName().equals(configurationMap.get(qbeDatamarts))) {
					throw new SpagoBIRuntimeException("This business model cannot be deleted because there are datasets that were created on top of it :[ "+dataSet.getName()+ " ]");
				}
			}

			businessModelsDAO.eraseMetaModel(bmId);

			return Response.ok().build();
		} catch (SpagoBIRuntimeException e) {
			logger.error("An error occurred while deleting business model with id:" + bmId, e);
			throw new SpagoBIRestServiceException(e.getMessage(), buildLocaleFromSession(), e);

		} catch (Exception e) {
			logger.error("An error occurred while deleting business model with id:" + bmId, e);
			throw new SpagoBIRestServiceException("An error occurred while deleting business model with id:" + bmId, buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Deleting many business models
	 **/
	@DELETE
	@Path("/deletemany")
	public Response deleteBusinessModels(@QueryParam("id") List<Integer> ids) {

		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		businessModelsDAO.setUserProfile(getUserProfile());

		try {
			for (int i = 0; i < ids.size(); i++) {
				businessModelsDAO.eraseMetaModel(ids.get(i));
			}

			return Response.ok().build();
		} catch (Exception e) {
			logger.error("An error occurred while deleting many business models", e);
			throw new SpagoBIRestServiceException("An error occurred while deleting many business models", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Delete version with id {vId} of business model with id {bmId}
	 **/
	@DELETE
	@Path("{bmId}/versions/{vId}")
	public Response deleteBusinessModelVersion(@PathParam("bmId") Integer bmId, @PathParam("vId") Integer vId) {

		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		businessModelsDAO.setUserProfile(getUserProfile());

		try {
			businessModelsDAO.eraseMetaModelContent(vId);
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("An error occurred while deleting active version (" + vId + ") of  business model with id:" + bmId, e);
			throw new SpagoBIRestServiceException("An error occurred while deleting active version (" + vId + ") of business model with id:" + bmId,
					buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Delete many versions of business model with id {bmId}
	 **/
	@DELETE
	@Path("{bmId}/deleteManyVersions")
	public Response deleteBusinessModelVersions(@PathParam("bmId") Integer bmId, @QueryParam("id") List<Integer> ids) {

		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		businessModelsDAO.setUserProfile(getUserProfile());

		try {
			for (int i = 0; i < ids.size(); i++) {
				businessModelsDAO.eraseMetaModelContent(ids.get(i));
			}

			return Response.ok().build();
		} catch (Exception e) {
			logger.error("An error occurred while deleting many versions of business model with id:" + bmId, e);
			throw new SpagoBIRestServiceException("An error occurred while deleting many versions of business model with id:" + bmId, buildLocaleFromSession(),
					e);

		} finally {
			logger.debug("OUT");
		}
	}

	private String getFileName(MultivaluedMap<String, String> header) {
		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return null;
	}

	@Path("/{id}/drivers")
	@Produces("application/json")
	public BusinessModelDriversResource getBusinessModelDrivers(@PathParam("id") Integer id) {
		logger.debug("Getting DriversResource instance");
		return new BusinessModelDriversResource();
	}

	@Path("/{id}/datadependencies")
	@Produces("application/json")
	public BusinessModelDataDependenciesResource getDataDependencies(@PathParam("id") Integer id, @QueryParam("driverId") Integer driverId) {
		logger.debug("Getting DataDependenciesResource instance");
		return new BusinessModelDataDependenciesResource();
	}

	@Path("/{id}/visualdependencies")
	@Produces("application/json")
	public BusinessModelVisualDependenciesResource getVisualDependencies() {
		logger.debug("Getting VisualDependenciesResource instance");
		return new BusinessModelVisualDependenciesResource();
	}
}
