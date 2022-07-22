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
package it.eng.spagobi.tools.dataset.service;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.federateddataset.dao.ISbiFederationDefinitionDAO;
import it.eng.spagobi.tools.catalogue.service.GetMetaModelsAction;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

public class GetFederatedDatasetForFinalUserAction extends AbstractSpagoBIAction {

	public static Logger logger = Logger.getLogger(GetMetaModelsAction.class);

	public static String START = "start";
	public static String LIMIT = "limit";
	public static String FILTERS = "Filters";

	public static Integer START_DEFAULT = 0;
	public static Integer LIMIT_DEFAULT = 15;

	@Override
	public void doService() {

		logger.debug("IN");

		try {
			List<FederationDefinition> allFederatedDatasets = null;

			List<Integer> categories = getCategories();

			logger.debug("Read federated dataset");
			ISbiFederationDefinitionDAO federDsDao = DAOFactory.getFedetatedDatasetDAO();
			federDsDao.setUserProfile(this.getUserProfile());

			allFederatedDatasets = federDsDao.loadNotDegeneratedFederatedDataSets();
			if (allFederatedDatasets == null) {
				allFederatedDatasets = new ArrayList<FederationDefinition>();
			}
			logger.debug("Read " + allFederatedDatasets.size() + " existing federated datasets");

			Integer start = this.getStart();
			logger.debug("Start : " + start);
			// disable limit
			// Integer limit = this.getLimit();
			// logger.debug("Limit : " + limit);

			int startIndex = Math.min(start, allFederatedDatasets.size());
			int stopIndex = allFederatedDatasets.size();
			// (limit > 0) ? Math.min(start + limit, allFederatedDatasets.size()) : allFederatedDatasets.size();

			List<FederationDefinition> toReturnSublist = allFederatedDatasets.subList(startIndex, stopIndex);

			try {
				JSONArray dsArraysJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(toReturnSublist, null);
				JSONObject rolesResponseJSON = createJSONResponse(dsArraysJSON, allFederatedDatasets.size());
				writeBackToClient(new JSONSuccess(rolesResponseJSON));
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			} catch (JSONException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects into a JSON object", e);
			} catch (SerializationException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects into a JSON object", e);
			}

		} catch (EMFUserError eue) {
			logger.error("Error in getting federated datasets", eue);
			throw new SpagoBIServiceException(SERVICE_NAME, "Error in getting federated datasets", eue);
		} finally {
			logger.debug("OUT");
		}

	}

	protected List<Integer> getCategories() {

		List<Integer> categories = new ArrayList<Integer>();
		try {
			// NO CATEGORY IN THE DOMAINS
			ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();

			// TODO : Makes sense?
			List<Domain> dialects = categoryDao.getCategoriesForBusinessModel()
				.stream()
				.map(Domain::fromCategory)
				.collect(toList());
			if (dialects == null || dialects.size() == 0) {
				return null;
			}

			Collection userRoles = this.getUserProfile().getRoles();
			Iterator userRolesIter = userRoles.iterator();
			IRoleDAO roledao = DAOFactory.getRoleDAO();
			while (userRolesIter.hasNext()) {
				String roleName = (String) userRolesIter.next();
				Role role = roledao.loadByName(roleName);
				List<RoleMetaModelCategory> aRoleCategories = roledao.getMetaModelCategoriesForRole(role.getId());
				if (aRoleCategories != null) {
					for (Iterator iterator = aRoleCategories.iterator(); iterator.hasNext();) {
						RoleMetaModelCategory roleMetaModelCategory = (RoleMetaModelCategory) iterator.next();
						categories.add(roleMetaModelCategory.getCategoryId());
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error loading the categories visible from the roles of the user");
			throw new SpagoBIRuntimeException("Error loading the categories visible from the roles of the user");
		}
		return categories;
	}

	protected Integer getStart() {
		Integer start = START_DEFAULT;
		Object startObject = getAttribute(START);
		try {

			if (startObject != null && !startObject.equals("")) {
				start = getAttributeAsInteger(START);
			}
		} catch (NumberFormatException e) {
			logger.debug("Error getting the limit parameter. The value should be integer but it is [" + startObject + "]");
		}

		return start;
	}

	// No limit
	protected Integer getLimit() {
		Integer limit = LIMIT_DEFAULT;
		Object limitObject = getAttribute(LIMIT);
		try {
			if (limitObject != null && !limitObject.equals("")) {
				limit = getAttributeAsInteger(LIMIT);
			}
		} catch (NumberFormatException e) {
			logger.debug("Error getting the limit parameter. The value should be integer but it is [" + limitObject + "]");
		}

		return limit;
	}

	protected JSONObject createJSONResponse(JSONArray rows, Integer totalResNumber) throws JSONException {
		JSONObject results;
		results = new JSONObject();
		results.put("total", totalResNumber);
		results.put("title", "MetaModels");
		results.put("rows", rows);
		return results;
	}

}
