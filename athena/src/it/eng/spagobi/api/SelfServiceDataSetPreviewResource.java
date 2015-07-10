/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version.
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file.
 */
package it.eng.spagobi.api;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.themes.ThemesManager;
import it.eng.spagobi.wapp.services.ChangeTheme;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/selfservicedataset")
public class SelfServiceDataSetPreviewResource extends AbstractSpagoBIResource {

	private static final String DATASET_VALUE_SUCCESS_REQUEST_DISPATCHER_URL = "datasetview.jsp";
	private static final int MAX_DATASET_PAGE_SIZE = 10;

	static private Logger logger = Logger.getLogger(SelfServiceDataSetPreviewResource.class);

	@GET
	@Path("/values/{label}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String loadData(@PathParam("label") String label, @Context HttpServletRequest req, @QueryParam("start") Integer start,
			@QueryParam("limit") Integer limit, @QueryParam("page") Integer page, @QueryParam("dataSetParameters") String dataSetParameters,
			@QueryParam("sort") JSONArray sortOptions, @QueryParam("valueFilter") String valueFilter, @QueryParam("columnsFilter") String columnsFilter,
			@QueryParam("columnsFilterDescription") String columnsFilterDescription, @QueryParam("typeValueFilter") String typeValueFilter,
			@QueryParam("typeFilter") String typeFilter) {

		logger.debug("Retriving data for the preview");

		UserProfile profile = null;
		try {
			profile = (UserProfile) UserUtilities.getUserProfile("biadmin");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		boolean isDatasetVisible = false;
		Map<String, Object> parametersMap = new HashMap<String, Object>();

		// Adding the parameters for sort, filters and dataset parameters
		logger.debug("Adding filters, sorting, parameters");
		addSortParam(sortOptions, parametersMap);
		addFilterParam(columnsFilterDescription, typeValueFilter, typeFilter, valueFilter, parametersMap);
		addDatasetParam(dataSetParameters, parametersMap);
		logger.debug("Added filters, sorting, parameters");

		// Load the dataset
		logger.error("Loading the dataset");
		if (label == null || label.length() == 0) {
			logger.error("No dataset found with label ");
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.no.visible.dataset", buildLocaleFromSession(), "No dataset found with label "
					+ label);
		}
		logger.error("Dataset loaded");

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
			List<IDataSet> datsets = dao.loadMyDataDataSets(profile.getUserId().toString());

			if (ds == null) {
				logger.error("No dataset found with label " + label);
				throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.no.visible.dataset", buildLocaleFromSession(), "No dataset found with label "
						+ label);
			}

			logger.debug("Checking if the user can see the dataset");
			if (datsets != null) {
				for (int i = 0; i < datsets.size(); i++) {
					IDataSet datset = datsets.get(i);
					if (datset.getLabel().equals(ds.getLabel()) && (datset.isPublic() || datset.getOwner().equals(profile.getUserId().toString()))) {
						isDatasetVisible = true;
						break;
					}
				}
			}

			if (!isDatasetVisible) {
				logger.error("The dataset with label " + label + " is not visible to the user");
				throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.no.visible.dataset", buildLocaleFromSession(), "The dataset with label "
						+ label + " is not visible to the user");

			}

			logger.debug("The user can execute the dataset");

			logger.debug("Setting profile attributes");
			ds.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));

			logger.debug("Setting parameters");
			ds.setParamsMap(parametersMap);

			logger.debug("Loading the data");
			ds.loadData(start, limit, GeneralUtilities.getDatasetMaxResults());
			logger.debug("Data loaded");

			logger.debug("Writing the result set");
			Map<String, Object> properties = new HashMap<String, Object>();
			JSONDataWriter dataSetWriter = new JSONDataWriter(properties);
			// dataSetWriter.setLocale(buildLocaleFromSession());
			// JSONObject gridDataFeed = (JSONObject) dataSetWriter.write(ds.getDataStore());
			JSONObject gridDataFeed = getDatasetTestResultList(ds, parametersMap, profile, start, limit);
			String stringFeed = gridDataFeed.toString();
			logger.debug("Data set written");

			return stringFeed;

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
	 */
	@GET
	@Path("/getdataset")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public void loadData() {

		try {

			/* FORWARD THE REQUEST */
			logger.debug("Forwarding the request");

			String theme_name = (String) request.getAttribute(ChangeTheme.THEME_NAME);
			if (theme_name == null)
				theme_name = ThemesManager.getDefaultTheme();

			String url = "/themes/" + theme_name + "/jsp/";
			url = url + DATASET_VALUE_SUCCESS_REQUEST_DISPATCHER_URL;

			getServletRequest().getRequestDispatcher(url).forward(getServletRequest(), getServletResponse());

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

	public Locale buildLocaleFromSession() {
		Locale locale = null;
		Object countryO = getHttpSession().getAttribute(SpagoBIConstants.AF_COUNTRY);
		Object languageO = getHttpSession().getAttribute(SpagoBIConstants.AF_LANGUAGE);
		String country = countryO != null ? countryO.toString() : null;
		String language = languageO != null ? languageO.toString() : null;
		if (country != null && language != null) {
			locale = new Locale(language, country);
		} else {
			locale = Locale.ENGLISH;
		}
		return locale;
	}
}
