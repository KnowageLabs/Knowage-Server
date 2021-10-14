/*
* Knowage, Open Source Business Intelligence suite
* Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

* Knowage is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.

* Knowage is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.

* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package it.knowage.api.datapreparation;

import java.util.HashSet;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.api.common.AbstractDataSetResource;
import it.eng.spagobi.api.v3.DataSetForWorkspaceDTO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.service.ManageDataSetsForREST;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/1.0/datapreparation")
public class DataPreparationResource extends AbstractDataSetResource {

	static protected Logger logger = Logger.getLogger(DataPreparationResource.class);

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;

	@GET
	@Path("/{label}/datasetinfo")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public DataSetForWorkspaceDTO getDataSetInfo(@PathParam("label") String label, @DefaultValue("-1") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("fetchSize") int fetchSize) {
		try {
			SbiDataSet dataSet = DAOFactory.getSbiDataSetDAO().loadMyDataSetByLabel(offset, fetchSize, getUserProfile(), label);

			return transform(dataSet);
		} catch (Exception t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		}

	}

	@POST
	@Path("/{label}/preview")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String preview(@PathParam("label") String label, String body) {
		String toReturnString = null;
		try {
			JSONObject json = new JSONObject(body);
			ManageDataSetsForREST mdsfr = new ManageDataSetsForREST();
			JSONObject jsonResponse = mdsfr.previewDatasetForDataPreparation(json.toString(), getUserProfile());
			JSONObject transformationsConfig = new JSONObject();
			JSONArray configs = new JSONArray();
			JSONObject transformationsMock = new JSONObject();

			JSONArray parameters = new JSONArray();
			JSONObject change1 = new JSONObject();

			change1.put("column", "QUARTER");
			change1.put("operator", "=");
			change1.put("value", "Q1");

			JSONObject change2 = new JSONObject();

			change2.put("column", "STORE_ID");
			change2.put("operator", ">");
			change2.put("value", "4");

			parameters.put(change1);

			parameters.put(change2);

			transformationsConfig.put("parameters", parameters);
			transformationsMock.put("config", transformationsConfig);

			configs.put(transformationsMock);

			jsonResponse.put("transformations", configs);

			toReturnString = jsonResponse.toString();

		} catch (JSONException e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} catch (Exception e) {
			logger.error("Error while previewing dataset " + label, e);
			throw new SpagoBIRuntimeException("Error while previewing dataset " + label + ". " + e.getMessage(), e);
		}
		return toReturnString;
	}

	@POST
	@Path("/{label}/saveDataset")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String saveDataset(@PathParam("label") String label, String body) {
		String toReturnString = null;
		try {
			JSONObject json = new JSONObject(body);
			ManageDataSetsForREST mdsfr = new ManageDataSetsForREST();

//			toReturnString = mdsfr.previewDatasetForDataPreparation(json.toString(), getUserProfile());

		} catch (JSONException e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} catch (Exception e) {
			logger.error("Error while previewing dataset " + label, e);
			throw new SpagoBIRuntimeException("Error while previewing dataset " + label + ". " + e.getMessage(), e);
		}
		return toReturnString;
	}

	/*
	 * TODO: to develop depending from BE
	 */
	@GET
	@Path("/{label}/statistics")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getStatistics(@PathParam("label") String label, String body) {
		String toReturnString = null;
		try {
			JSONObject json = new JSONObject(body);
			ManageDataSetsForREST mdsfr = new ManageDataSetsForREST();

//			toReturnString = mdsfr.previewDatasetForDataPreparation(json.toString(), getUserProfile());

		} catch (JSONException e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} catch (Exception e) {
			logger.error("Error while previewing dataset " + label, e);
			throw new SpagoBIRuntimeException("Error while previewing dataset " + label + ". " + e.getMessage(), e);
		}
		return toReturnString;
	}

	/*
	 * Utility methods
	 */
	private DataSetForWorkspaceDTO transform(SbiDataSet dataSet) {
		DataSetForWorkspaceDTO dsToReturn = new DataSetForWorkspaceDTO(dataSet);
		return dsToReturn;
	}

	@Override
	protected DatasetManagementAPI getDatasetManagementAPI() {
		DatasetManagementAPI managementAPI = new DatasetManagementAPI(getUserProfile());
		return managementAPI;
	}

	@Override
	public UserProfile getUserProfile() {
		return UserProfileManager.getProfile();
	}

	@Override
	public Locale buildLocaleFromSession() {
		Locale locale = null;
		HttpSession httpSession = getHttpSession();
		if (httpSession != null) {

			String currLanguage = (String) httpSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String currCountry = (String) httpSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
			String currScript = (String) httpSession.getAttribute(SpagoBIConstants.AF_SCRIPT);
			if (currLanguage != null && currCountry != null) {
				Builder tmpLocale = new Locale.Builder().setLanguage(currLanguage).setRegion(currCountry);

				if (StringUtils.isNotBlank(currScript)) {
					tmpLocale.setScript(currScript);
				}

				locale = tmpLocale.build();
			} else
				locale = new Locale("en_US");
		}
		return locale;
	}

	@Override
	public HttpSession getHttpSession() {
		if (getServletRequest() != null) {
			return getServletRequest().getSession(false);
		} else {
			return null;
		}
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return request;
	}

	@Override
	public HttpServletResponse getServletResponse() {
		return response;
	}

	private Set<String> getQbeDataSetHiddenColumns(IDataSet dataSet) {
		Set<String> hiddenColumns = new HashSet<String>();
		if (dataSet.getDsType().equals("SbiQbeDataSet")) {
			try {
				JSONObject dsConfig = new JSONObject(dataSet.getConfiguration());
				JSONObject qbeQuery = new JSONObject(dsConfig.getString("qbeJSONQuery"));
				JSONArray fields = qbeQuery.getJSONObject("catalogue").getJSONArray("queries").getJSONObject(0).getJSONArray("fields");
				for (int i = 0; i < fields.length(); i++) {
					JSONObject field = fields.getJSONObject(i);
					if (field.has("visible") && field.getBoolean("visible") == false)
						hiddenColumns.add(field.getString("alias"));
				}
			} catch (Exception e) {
				logger.error("Error while getting list of hidden QBE columns.", e);
			}
		}
		return hiddenColumns;
	}
}
