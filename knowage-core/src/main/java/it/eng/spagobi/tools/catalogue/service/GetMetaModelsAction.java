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
package it.eng.spagobi.tools.catalogue.service;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

public class GetMetaModelsAction extends AbstractSpagoBIAction {

	// logger component
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
			IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
			dao.setUserProfile(this.getUserProfile());
			List<MetaModel> allModels = null;

			if (requestContainsAttribute(FILTERS)) {
				String filterString = getAttributeAsString(FILTERS);
				JSONObject jsonObject = new JSONObject(filterString);
				allModels = getFilteredModels(jsonObject, dao);
			} else {
				allModels = dao.loadAllMetaModels();
			}

			logger.debug("Read " + allModels.size() + " existing models");

			Integer start = this.getStart();
			logger.debug("Start : " + start);
			Integer limit = this.getLimit();
			logger.debug("Limit : " + limit);

			int startIndex = Math.min(start, allModels.size());
			int stopIndex = (limit > 0) ? Math.min(start + limit, allModels.size()) : allModels.size();
			List<MetaModel> models = allModels.subList(startIndex, stopIndex);

			try {
				JSONArray modelsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(models, null);
				JSONObject rolesResponseJSON = createJSONResponse(modelsJSON, allModels.size());
				writeBackToClient(new JSONSuccess(rolesResponseJSON));
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			} catch (JSONException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects into a JSON object", e);
			} catch (SerializationException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects into a JSON object", e);
			}

		} catch (JSONException e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects into a JSON object", e);
		} finally {
			logger.debug("OUT");
		}

	}

	protected List<MetaModel> getFilteredModels(JSONObject jsonObject, IMetaModelsDAO dao) throws JSONException {
		List<MetaModel> metaModels = new ArrayList<MetaModel>();
		String columnFilter = jsonObject.getString("columnFilter");
		String valueFilter = jsonObject.getString("valueFilter");
		String typeFilter = jsonObject.getString("typeFilter");

		if (columnFilter.equals("category")) {
			if (typeFilter.equals("=")) {
				Integer categoryId = getCategoryIdbyName(valueFilter);
				if (categoryId != null) {
					List<Integer> categories = new ArrayList<Integer>();
					categories.add(categoryId);
					metaModels.addAll(dao.loadMetaModelByCategories(categories));
				}
			} else if (typeFilter.equals("like")) {
				List<Integer> categoryIds = getCategoryIdbyContainsName(valueFilter);
				if (!categoryIds.isEmpty()) {
					for (Integer categoryId : categoryIds) {
						List<Integer> categories = new ArrayList<Integer>();
						categories.add(categoryId);
						metaModels.addAll(dao.loadMetaModelByCategories(categories));
					}
				}
			}

		} else if (columnFilter.equals("name")) {
			String filter = getFilterString(columnFilter, typeFilter, valueFilter);
			metaModels.addAll(dao.loadMetaModelByFilter(filter));

		}
		return metaModels;
	}

	protected String getFilterString(String columnFilter, String typeFilter, String valueFilter) {
		String filterString = "";
		if (typeFilter.equals("=")) {
			filterString = " m." + columnFilter + " = '" + valueFilter + "'";
		} else if (typeFilter.equals("like")) {
			filterString = " m." + columnFilter + " like '%" + valueFilter + "%'";
		}
		return filterString;

	}

	protected Integer getCategoryIdbyName(String categoryName) {
		ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
		List<Domain> domains = categoryDao.getCategoriesForBusinessModel()
			.stream()
			.map(Domain::fromCategory)
			.collect(toList());

		for (Domain domainElement : domains) {
			if (domainElement.getValueName().equals(categoryName)) {
				return domainElement.getValueId();
			}
		}

		return null;

	}

	protected List<Integer> getCategoryIdbyContainsName(String categoryName) {
		List<Integer> categoryIds = new ArrayList<Integer>();

		ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
		List<Domain> domains = categoryDao.getCategoriesForBusinessModel()
			.stream()
			.map(Domain::fromCategory)
			.collect(toList());

		for (Domain domainElement : domains) {
			if (domainElement.getValueName().contains(categoryName)) {
				categoryIds.add(domainElement.getValueId());
			}
		}

		return categoryIds;

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
