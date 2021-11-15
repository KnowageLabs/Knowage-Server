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
package it.eng.spagobi.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.html.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONObjectDeserializator;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.exceptions.ActionNotPermittedException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.themes.ThemesManager;
import it.eng.spagobi.wapp.services.ChangeTheme;

@Path("/selfservicedatasetpreview")
public class SelfServiceDataSetPreviewResource extends AbstractSpagoBIResource {

	private static final String DATASET_VALUE_SUCCESS_REQUEST_DISPATCHER_URL = "datasetview.jsp";
	private static final int MAX_DATASET_PAGE_SIZE = 10;

	static private Logger logger = Logger.getLogger(SelfServiceDataSetPreviewResource.class);

	@GET
	@Path("/export/{label}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String exportData(@PathParam("label") String label, @Context HttpServletRequest req) {
		String res = loadData(label, req, 0, Integer.MAX_VALUE, 0, null, null, null, null, null, null, null, null);
		return res;
	}

	@GET
	@Path("/values/{label}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String loadData(@PathParam("label") String label, @Context HttpServletRequest req, @QueryParam("start") Integer start,
			@QueryParam("limit") Integer limit, @QueryParam("page") Integer page, @QueryParam("dataSetParameters") String dataSetParameters,
			@QueryParam("sort") JSONArray sortOptions, @QueryParam("valueFilter") String valueFilter, @QueryParam("columnsFilter") String columnsFilter,
			@QueryParam("columnsFilterDescription") String columnsFilterDescription, @QueryParam("typeValueFilter") String typeValueFilter,
			@QueryParam("typeFilter") String typeFilter, @QueryParam("DRIVERS") JSONObject driversMap) {

		logger.debug("Retriving data for the preview");

		UserProfile profile = getUserProfile();

		boolean isDatasetVisible = false;
		Map<String, Object> parametersMap = new HashMap<String, Object>();
		Map<String, Object> driversRuntimeMap;
		try {
			driversRuntimeMap = JSONObjectDeserializator.getHashMapFromJSONObject(driversMap);
		} catch (IOException e1) {
			logger.error("Getting Drivers has encoutered error");
			throw new SpagoBIRuntimeException(e1.getLocalizedMessage(), e1);
		}
		// Adding the parameters for sort, filters and dataset parameters
		logger.debug("Adding filters, sorting, parameters");
		addSortParam(sortOptions, parametersMap);
		addFilterParam(columnsFilterDescription, typeValueFilter, typeFilter, valueFilter, parametersMap);
		addDatasetParam(dataSetParameters, parametersMap);
		logger.debug("Added filters, sorting, parameters");

		// Load the dataset
		logger.debug("Loading the dataset");
		if (label == null || label.length() == 0) {
			logger.error("No dataset found with label ");
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.no.visible.dataset", buildLocaleFromSession(),
					"No dataset found with label " + label);
		}
		logger.debug("Dataset loaded");

		logger.debug("The parameters for the execution of the dataset are " + parametersMap);

		try {

			if (start == null) {
				start = 0;
			}
			if (limit == null || limit == 0) {
				limit = MAX_DATASET_PAGE_SIZE;
			}

			logger.debug("Loading the detaset");
			IDataSetDAO dao = DAOFactory.getDataSetDAO();
			dao.setUserProfile(profile);
			IDataSet ds = dao.loadDataSetByLabel(label);

			List<IDataSet> datsets = dao.loadMyDataDataSets(profile);

			if (ds == null) {
				logger.error("No dataset found with label " + label);
				throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.no.visible.dataset", buildLocaleFromSession(),
						"No dataset found with label " + label);
			}

			logger.debug("Checking if the user can see the dataset");
			if (datsets != null) {
				for (int i = 0; i < datsets.size(); i++) {
					IDataSet datset = datsets.get(i);
					if (datset.getLabel().equals(ds.getLabel())) {
						isDatasetVisible = true;
						break;
					}
				}
			}

			if (!isDatasetVisible) {
				logger.error("The dataset with label " + label + " is not visible to the user");
				throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.no.visible.dataset", buildLocaleFromSession(),
						"The dataset with label " + label + " is not visible to the user");

			}

			try {
				new DatasetManagementAPI(profile).canLoadData(ds);
			} catch (ActionNotPermittedException e) {
				logger.error("User " + profile.getUserId() + " cannot preview the dataset with label " + label);
				throw new SpagoBIRestServiceException(e.getI18NCode(), buildLocaleFromSession(),
						"User " + profile.getUserId() + " cannot preview the dataset with label " + label, e, "MessageFiles.messages");
			}

			logger.debug("The user can execute the dataset");

			logger.debug("Setting profile attributes");
			ds.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));

			logger.debug("Setting parameters");
			ds.setParamsMap(parametersMap);

			Set<String> driverUrlNames = driversRuntimeMap.keySet();
			for (String driverName : driverUrlNames) {
				Map mapOfValues = (Map) driversRuntimeMap.get(driverName);
				if (mapOfValues.containsKey("value")) {
					if (mapOfValues.get("value") instanceof List && mapOfValues.get("value") != null && !((List) mapOfValues.get("value")).isEmpty()
							|| !(mapOfValues.get("value") instanceof List) && !"".equals(mapOfValues.get("value"))) {
						logger.debug("Setting drivers");
						ds.setDrivers(driversRuntimeMap);
					}
				}
			}

			logger.debug("Loading the data");
			ds.loadData(start, limit, GeneralUtilities.getDatasetMaxResults());
			logger.debug("Data loaded");

			logger.debug("Writing the result set");
			Map<String, Object> properties = new HashMap<String, Object>();
			JSONDataWriter dataSetWriter = new JSONDataWriter(properties);
			// dataSetWriter.setLocale(buildLocaleFromSession());
			// JSONObject gridDataFeed = (JSONObject)
			// dataSetWriter.write(ds.getDataStore());
			JSONObject gridDataFeed = getDatasetTestResultList(ds, parametersMap, profile, start, limit);
			String stringFeed = gridDataFeed.toString();
			logger.debug("Data set written");

			return stringFeed;
		} catch (SpagoBIRestServiceException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error loading the dataset values ", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.generalerror", buildLocaleFromSession(), e);
		}

	}

	public JSONObject getDatasetTestResultList(IDataSet dataSet, Map<String, Object> parametersFilled, IEngUserProfile profile, int start, int limit) {

		JSONObject dataSetJSON;

		logger.debug("IN");

		dataSetJSON = null;
		try {

			dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));
			dataSet.setParamsMap(parametersFilled);

			IDataStore dataStore = null;
			try {
				if (dataSet.getTransformerId() != null) {
					dataStore = dataSet.test();
				} else {
					dataStore = dataSet.test(start, limit, GeneralUtilities.getDatasetMaxResults());
				}
				if (dataStore == null) {
					throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.generalerror", buildLocaleFromSession(), "General error loading dataset");
				}
			} catch (Throwable t) {
				Throwable rootException = t;
				while (rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String rootErrorMsg = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
				if (dataSet instanceof JDBCDataSet) {
					JDBCDataSet jdbcDataSet = (JDBCDataSet) dataSet;
					if (jdbcDataSet.getQueryScript() != null) {
						QuerableBehaviour querableBehaviour = (QuerableBehaviour) jdbcDataSet.getBehaviour(QuerableBehaviour.class.getName());
						String statement = querableBehaviour.getStatement();
						rootErrorMsg += "\nQuery statement: [" + statement + "]";
					}
				}

				throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.generalerror", buildLocaleFromSession(), t);
			}

			try {
				JSONDataWriter dataSetWriter = new JSONDataWriter();
				dataSetJSON = (JSONObject) dataSetWriter.write(dataStore);
				if (dataSetJSON == null) {
					throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.generalerror", buildLocaleFromSession(),
							"Impossible to read serialized resultset");
				}
			} catch (Exception t) {
				throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.generalerror", buildLocaleFromSession(), t);
			}
		} catch (Throwable t) {
			if (t instanceof SpagoBIServiceException) {
				throw (SpagoBIServiceException) t;
			}
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.generalerror", buildLocaleFromSession(), t);
		} finally {
			logger.debug("OUT");
		}

		return dataSetJSON;
	}

	/**
	 * Forward the request to the jsp
	 * @return
	 */
	@GET
	@Path("/getdataset")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public View loadData() {

		try {

			/* FORWARD THE REQUEST */
			logger.debug("Forwarding the request");

			String theme_name = (String) request.getAttribute(ChangeTheme.THEME_NAME);
			if (theme_name == null)
				theme_name = ThemesManager.getDefaultTheme();

			String url = "/themes/" + theme_name + "/jsp/";
			url = url + DATASET_VALUE_SUCCESS_REQUEST_DISPATCHER_URL;

			return new View(url);

		} catch (Exception e) {
			logger.error("Error loading the dataset values ", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.generalerror", buildLocaleFromSession(), e);
		}

	}

	private void addSortParam(JSONArray sortOptions, Map<String, Object> parametersMap) {
		logger.debug("Getting the sort options");
		String sortParam = "";
		if (sortOptions != null && sortOptions.length() > 0) {

			try {
				for (int i = 0; i < sortOptions.length(); i++) {
					JSONObject sortEntry = sortOptions.getJSONObject(i);
					sortParam = sortParam + " " + sortEntry.getString("property");
					sortParam = sortParam + " " + sortEntry.getString("direction");
					sortParam = sortParam + ",";
				}
				sortParam = sortParam.substring(0, sortParam.length() - 1);
				parametersMap.put("sortParam", sortParam);
			} catch (JSONException e) {
				logger.error("Error loading the sort options ", e);
				throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.sort.error", buildLocaleFromSession(), e);
			}
		}
		logger.debug("Sort options: " + sortParam);
	}

	private void addFilterParam(String columnsFilterDescription, String typeValueFilter, String typeFilter, String valueFilter,
			Map<String, Object> parametersMap) {
		logger.debug("Getting the filter options");
		String filter = "";

		if (columnsFilterDescription != null && columnsFilterDescription.length() > 0) {

			if (typeFilter.equals("contains") || typeFilter.equals("start") || typeFilter.equals("end")) {
				if (typeFilter.equals("contains")) {
					typeFilter = "LIKE %" + valueFilter + "%";
				}
				if (typeFilter.equals("start")) {
					typeFilter = "LIKE %" + valueFilter;
				}
				if (typeFilter.equals("end")) {
					typeFilter = "LIKE " + valueFilter + "%";
				}
				filter = " " + columnsFilterDescription + " " + typeFilter + " ";
			} else {

				if (typeFilter.equals("equal")) {
					typeFilter = "=";
				}
				if (typeFilter.equals("less")) {
					typeFilter = "<";
				}
				if (typeFilter.equals("greater")) {
					typeFilter = ">";
				}
				if (typeFilter.equals("lessequal")) {
					typeFilter = "<=";
				}
				if (typeFilter.equals("greaterequal")) {
					typeFilter = ">=";
				}
				filter = " " + columnsFilterDescription + " " + typeFilter + " " + valueFilter + " ";
			}

			parametersMap.put("filterParam", filter);
		}
		logger.debug("Filter options " + filter);
	}

	private void addDatasetParam(String dataSetParameters, Map<String, Object> parametersMap) {
		logger.debug("loading the parameters of the dataset");
		if (dataSetParameters != null && dataSetParameters.length() > 0) {
			dataSetParameters = "{" + dataSetParameters + "}";
			JSONObject parametersJSON;
			try {
				parametersJSON = new JSONObject(dataSetParameters);
				Iterator<String> keyIter = parametersJSON.keys();
				while (keyIter.hasNext()) {
					String key = keyIter.next();
					parametersMap.put(key, parametersJSON.get(key));
				}
			} catch (JSONException e) {
				logger.error("Error loading the parameters ", e);
				throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), e);
			}
		}
		logger.debug("Parameters fo the dataset loaded: " + parametersMap);
	}

	@Override
	public Locale buildLocaleFromSession() {
		Locale locale = null;

		String currLanguage = (String) getHttpSession().getAttribute(SpagoBIConstants.AF_LANGUAGE);
		String currCountry = (String) getHttpSession().getAttribute(SpagoBIConstants.AF_COUNTRY);
		String currScript = (String) getHttpSession().getAttribute(SpagoBIConstants.AF_SCRIPT);
		if (currLanguage != null && currCountry != null) {
			Builder tmpLocale = new Locale.Builder().setLanguage(currLanguage).setRegion(currCountry);

			if (StringUtils.isNotBlank(currScript)) {
				tmpLocale.setScript(currScript);
			}

			locale = tmpLocale.build();
		} else
			locale = GeneralUtilities.getDefaultLocale();

		return locale;
	}

	// private Map parseJsonDriversMap(JSONObject drivers) {
	// HashMap<String, Object> driversMap = new HashMap<>();
	// try {
	// for (int i = 0; i < JSONObject.getNames(drivers).length; i++) {
	// if (drivers.getString(JSONObject.getNames(drivers)[i]) != "" && (i & 1) == 0) {
	// if (drivers.get(JSONObject.getNames(drivers)[i]) instanceof JSONArray) {
	// String arrayValue = drivers.getJSONArray(JSONObject.getNames(drivers)[i]).getJSONObject(0).getString("value");
	// driversMap.put(JSONObject.getNames(drivers)[i], arrayValue);
	// } else
	// driversMap.put(JSONObject.getNames(drivers)[i], drivers.getString(JSONObject.getNames(drivers)[i]));
	// }
	// }
	// } catch (JSONException e) {
	// logger.debug("Unsuccessful parsing of JSONObject to map");
	// throw new JsonException(e.getLocalizedMessage(), e);
	// }
	// return driversMap;
	// }
}
