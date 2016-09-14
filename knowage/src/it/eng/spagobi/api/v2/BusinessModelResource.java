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
package it.eng.spagobi.api.v2;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.utilities.JSError;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/2.0/businessmodels")
@ManageAuthorization
public class BusinessModelResource extends AbstractSpagoBIResource {

	/**
	 * 
	 */
	public static enum FILETYPE {
		JAR, LOG, SBIMODULE
	};

	private static final String MODEL_SUFFIX = ".sbimodel";
	private static final String JAR_SUFFIX = ".jar";
	private static final String LOG_SUFFIX = ".log";

	IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();

	static protected Logger logger = Logger.getLogger(BusinessModelResource.class);

	/**
	 * Get all business models
	 **/
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<MetaModel> getBusinessModels() {
		logger.debug("IN");

		List<MetaModel> businessModelList = null;
		businessModelsDAO.setUserProfile(getUserProfile());

		try {

			businessModelList = businessModelsDAO.loadAllMetaModels();

			return businessModelList;

		} catch (Exception e) {
			logger.error("An error occurred while getting all business models from databse!", e);
			throw new SpagoBIRestServiceException("An error occurred while getting all business models from databse!", buildLocaleFromSession(), e);

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
			if (UserUtilities.hasDeveloperRole(profile) && !UserUtilities.hasAdministratorRole(profile)) {
				List<Domain> categoriesDev = new ArrayList<>();
				Collection<String> roles = profile.getRolesForUse();
				Iterator<String> itRoles = roles.iterator();
				while (itRoles.hasNext()) {
					String roleName = itRoles.next();
					role = rolesDao.loadByName(roleName);
					List<RoleMetaModelCategory> ds = rolesDao.getMetaModelCategoriesForRole(role.getId());
					List<Domain> array = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.CATEGORY_DOMAIN_TYPE);
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
				return DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.CATEGORY_DOMAIN_TYPE);
			}
		} catch (Exception e) {
			logger.error("Role with selected id: " + role.getId() + " doesn't exists", e);
			throw new SpagoBIRuntimeException("Item with selected id: " + role.getId() + " doesn't exists", e);
		}

	}

	/**
	 * Get business models that have datamart
	 * 
	 */

	@GET
	@Path("bmforfinaluser")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<MetaModel> getBusinessModelsWithVersions() {
		logger.debug("IN");

		List<MetaModel> businessModelList = null;
		List<MetaModel> businessModelsWithDatamart = new ArrayList<>();
		businessModelsDAO.setUserProfile(getUserProfile());

		try {

			businessModelList = businessModelsDAO.loadAllMetaModels();
			for (MetaModel bm : businessModelList) {
				List<Content> versions = businessModelsDAO.loadMetaModelVersions(bm.getId());
				if (!versions.isEmpty()) {
					businessModelsWithDatamart.add(bm);
				}
			}

			return businessModelsWithDatamart;

		} catch (Exception e) {
			logger.error("An error occurred while getting all business models from databse!", e);
			throw new SpagoBIRestServiceException("An error occurred while getting all business models from databse!", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
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
			// last filemodel
			boolean togenerate = false;
			Content lastFileModelContent = businessModelsDAO.lastFileModelMeta(bmId);
			if (lastFileModelContent != null && lastFileModelContent.getFileName() != null) {
				if (lastFileModelContent.getFileModel() != null
						&& (lastFileModelContent.getContent() == null || lastFileModelContent.getFileName().endsWith(LOG_SUFFIX))) {
					togenerate = true;
				}

			}
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

	/**
	 * Get business model with specified id
	 **/
	@GET
	@Path("{bmId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MetaModel getBusinessModelById(@PathParam("bmId") Integer bmId) {
		logger.debug("IN");

		MetaModel businessModel;
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
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON })
	public Response uploadFile(@MultipartForm MultipartFormDataInput input, @PathParam("bmId") int bmId) {

		// System.out.println();
		Content content = new Content();
		byte[] bytes = null;

		businessModelsDAO.setUserProfile(getUserProfile());

		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		for (String key : uploadForm.keySet()) {

			List<InputPart> inputParts = uploadForm.get(key);

			for (InputPart inputPart : inputParts) {

				try {

					MultivaluedMap<String, String> header = inputPart.getHeaders();
					if (getFileName(header) != null) {
						content.setFileName(getFileName(header));

						// convert the uploaded file to input stream
						InputStream inputStream = inputPart.getBody(InputStream.class, null);

						bytes = IOUtils.toByteArray(inputStream);
						content.setContent(bytes);
						content.setCreationDate(new Date());
						content.setCreationUser(getUserProfile().getUserName().toString());

						businessModelsDAO.insertMetaModelContent(bmId, content);

					}

				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}

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
		case LOG:
			byteContent = content.getContent();
			response = Response.ok(byteContent);
			response.header("Content-Disposition", "attachment; filename=" + filename);
			break;
		case SBIMODULE:
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
		businessModelsDAO.setUserProfile(getUserProfile());

		try {
			if (bm.getId() != null) {
				logger.error("New business model should not have id");
				bm = new MetaModel();
				return bm;
			}

			businessModelsDAO.insertMetaModel(bm);
			MetaModel insertedBM = businessModelsDAO.loadMetaModelByName(bm.getName());

			if (insertedBM.getModelLocked()) {
				businessModelsDAO.lockMetaModel(insertedBM.getId(), (String) getUserProfile().getUserId());
			}
			return insertedBM;
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

		businessModelsDAO.setUserProfile(getUserProfile());

		try {
			businessModelsDAO = DAOFactory.getMetaModelsDAO();
			businessModelsDAO.setActiveVersion(bmId, vId);
			return businessModelsDAO.loadActiveMetaModelContentById(bmId);
		} catch (Exception e) {
			logger.error("An error occurred while updating active version of business model with id:" + bmId, e);
			throw new SpagoBIRestServiceException("An error occurred while updating active version of business model with id:" + bmId,
					buildLocaleFromSession(), e);

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

		businessModelsDAO.setUserProfile(getUserProfile());

		try {
			businessModelsDAO.eraseMetaModel(bmId);

			return Response.ok().build();
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
	public Response deleteBusinessModels(@QueryParam("id") int[] ids) {

		businessModelsDAO.setUserProfile(getUserProfile());

		try {
			for (int i = 0; i < ids.length; i++) {
				businessModelsDAO.eraseMetaModel(ids[i]);
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
	public Response deleteBusinessModelVersions(@PathParam("bmId") Integer bmId, @QueryParam("id") int[] ids) {

		businessModelsDAO.setUserProfile(getUserProfile());

		try {
			for (int i = 0; i < ids.length; i++) {
				businessModelsDAO.eraseMetaModelContent(ids[i]);
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
}
