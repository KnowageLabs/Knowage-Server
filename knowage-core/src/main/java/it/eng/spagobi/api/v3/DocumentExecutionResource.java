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
package it.eng.spagobi.api.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonSyntaxException;

import it.eng.knowage.security.ProductProfiler;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 *
 * @author albnale
 *
 */

@Path("/3.0/documentexecution")
public class DocumentExecutionResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(DocumentExecutionResource.class);

	private static final String DOCUMENT = "DOCUMENT";
	private static final String DATASET = "DATASET";
	private static final String DATAMART = "DATAMART";
	private static final String FEDERATED_DATASET = "FEDERATED_DATASET";
	private static final String QBE_DATASET = "QBE_DATASET";

	@GET
	@Path("/{id}/templates")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public JSONObject getDocumentTemplates(@PathParam("id") Integer id) throws JSONException, EMFInternalError {
		LOGGER.debug("IN");

		UserProfile userProfile = getUserProfile();
		if (userProfile == null) {
			String message = "Error while loading user profile";
			LOGGER.error(message);
			throw new SpagoBIRuntimeException(message);
		}
		byte[] temp = null;
		JSONObject jsonTemplate = null;

		try {
			IBIObjectDAO documentDao = DAOFactory.getBIObjectDAO();
			BIObject document = documentDao.loadBIObjectById(id);
			if (!ObjectsAccessVerifier.canExec(document, userProfile)) {
				String message = "User cannot exec the document";
				LOGGER.error(message);
				throw new SpagoBIRuntimeException(message);
			}

			ObjTemplate documentTemplate = document.getActiveTemplate();
			temp = documentTemplate.getContent();
			Assert.assertNotNull(document, "Document can not be null");
			Assert.assertNotNull(documentTemplate, "Document Template can not be null");
			jsonTemplate = new JSONObject(new String(temp));
		} catch (EMFUserError e) {
			LOGGER.debug("Could not get content from template", e);
			throw new SpagoBIRestServiceException("Could not get content from template", buildLocaleFromSession(), e);
		}
		LOGGER.debug("OUT");
		return jsonTemplate;
	}

	@GET
	@Path("/correctRolesForExecution")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getCorrectRolesForExecution(@QueryParam("typeCode") String typeCode, @QueryParam("id") Integer id, @QueryParam("label") String label) {
		LOGGER.debug("IN");

		UserProfile userProfile = UserProfileManager.getProfile();

		List<String> correctRoles = new ArrayList<String>();
		try {

			List<String> userRoles = new ArrayList<String>();
			userProfile.getRolesForUse().forEach(x -> userRoles.add((String) x));

			if (userRoles.size() > 0) {

				ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();

				if (DATAMART.equals(typeCode)) {
					MetaModel model = DAOFactory.getMetaModelsDAO().loadMetaModelById(id);
					List<String> rolesByCategory = getRolesByCategory(categoryDao, model.getCategory());
					userRoles.retainAll(rolesByCategory);
					correctRoles = userRoles;

					List<BIMetaModelParameter> drivers = model.getDrivers();
					if (correctRoles.size() > 0 && drivers.size() > 0) {
						List<String> rolesByModel = getModelRoles(userProfile, model);
						correctRoles.retainAll(rolesByModel);
					}
				} else if (DATASET.equals(typeCode) || FEDERATED_DATASET.equals(typeCode)) {

					IDataSet dataset = null;
					if (DATASET.equals(typeCode)) {
						dataset = id != null ? DAOFactory.getDataSetDAO().loadDataSetById(id) : DAOFactory.getDataSetDAO().loadDataSetByLabel(label);
						Integer categoryId = dataset.getCategoryId();
						correctRoles = manageRolesByCategory(userRoles, categoryDao, categoryId);
					} else {
						FederationDefinition federationDefinition = DAOFactory.getFedetatedDatasetDAO().loadFederationDefinition(id);
						Set<IDataSet> fedSourceDatasets = federationDefinition.getSourceDatasets();
						correctRoles = userRoles;
						for (IDataSet federatedDataset : fedSourceDatasets) {
							correctRoles = manageRolesByCategory(correctRoles, categoryDao, federatedDataset.getCategoryId());
						}
					}
				} else if (QBE_DATASET.equals(typeCode)) {
					IDataSet dataset = id != null ? DAOFactory.getDataSetDAO().loadDataSetById(id) : DAOFactory.getDataSetDAO().loadDataSetByLabel(label);

					String conf = dataset.getConfiguration();
					try {
						String modelLabel = new JSONObject(conf).getString("qbeDatamarts");

						MetaModel model = DAOFactory.getMetaModelsDAO().loadMetaModelByName(modelLabel);
						List<String> rolesByCategory = getRolesByCategory(categoryDao, model.getCategory());
						userRoles.retainAll(rolesByCategory);
						correctRoles = userRoles;

						List<BIMetaModelParameter> drivers = model.getDrivers();
						if (correctRoles.size() > 0 && drivers.size() > 0) {
							List<String> rolesByModel = getModelRoles(userProfile, model);
							correctRoles.retainAll(rolesByModel);
						}
					} catch (JsonSyntaxException | JSONException e) {
						LOGGER.error("An error occurred while parsing dataset configuration", e);
						throw new SpagoBIRuntimeException(e.getMessage(), e);
					}

				} else if (DOCUMENT.equals(typeCode)) {
					BIObject biobj = id != null ? DAOFactory.getBIObjectDAO().loadBIObjectById(id) : DAOFactory.getBIObjectDAO().loadBIObjectByLabel(label);

					checkExecRightsByProducts(biobj);

					correctRoles = id != null ? ObjectsAccessVerifier.getCorrectRolesForExecution(id, userProfile)
							: ObjectsAccessVerifier.getCorrectRolesForExecution(label, userProfile);

					if (biobj.getDrivers().size() == 0 && correctRoles.size() > 0) {
						correctRoles = Arrays.asList(correctRoles.get(0));
					}
				}
			}

		} catch (EMFInternalError e) {
			LOGGER.error("Cannot retrieve correct roles for execution", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		} catch (EMFUserError e) {
			LOGGER.error("Cannot retrieve correct roles for execution", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		}

		LOGGER.debug("OUT");
		return Response.ok().entity(correctRoles).build();
	}

	/**
	 * @param roles
	 * @param categoryDao
	 * @param categoryId
	 * @return
	 * @throws EMFUserError
	 */
	private List<String> manageRolesByCategory(List<String> roles, ICategoryDAO categoryDao, Integer categoryId) throws EMFUserError {
		List<String> correctRoles;
		if (categoryId != null) {
			List<String> rolesByCategory = getRolesByCategory(categoryDao, categoryId);
			roles.retainAll(rolesByCategory);
			correctRoles = roles;
		} else {
			correctRoles = roles.stream().collect(Collectors.toList());
		}
		return correctRoles;
	}

	private List<String> getRolesByCategory(ICategoryDAO categoryDao, Integer categoryId) throws EMFUserError {
		List<String> rolesByCategory = categoryDao.getRolesByCategory(categoryId).stream().map(SbiExtRoles::getName).collect(Collectors.toList());
		return rolesByCategory;
	}

	private List<String> getModelRoles(UserProfile userProfile, MetaModel model) throws EMFInternalError {
		List<String> modelsRoles = new ArrayList<String>();
		List<BIMetaModelParameter> drivers = model.getDrivers();

		for (BIMetaModelParameter biMetaModelParameter : drivers) {
			Integer parId = biMetaModelParameter.getParameter().getId();
			for (Object role : userProfile.getRolesForUse()) {

				try {
					DAOFactory.getParameterDAO().loadForExecutionByParameterIDandRoleName(parId, (String) role, false);

					if (!modelsRoles.contains(role)) {
						modelsRoles.add(String.valueOf(role));
					}
				} catch (Exception e) {
					LOGGER.debug("Role {} is not valid for model [{}] execution. It will be not added to the available roles list.", role, model.getName());
				}

			}
		}
		return modelsRoles;
	}

	private void checkExecRightsByProducts(BIObject biobj) throws EMFUserError {

		if (!ProductProfiler.canExecuteDocument(biobj)) {
			throw new SpagoBIRuntimeException("This document cannot be executed within the current product");
		}
	}
}
