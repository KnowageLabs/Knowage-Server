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

import java.net.URI;
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
import org.apache.livy.LivyClient;
import org.apache.livy.LivyClientBuilder;
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
import it.eng.spagobi.tools.dataset.service.ManageDataSetsForDataPrep;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/1.0/datapreparation")
public class DataPreparationResource extends AbstractDataSetResource {

	static protected Logger logger = Logger.getLogger(DataPreparationResource.class);
	static private String canNotFillResponseError = "error.mesage.description.generic.can.not.responce";

	private static final String LIVY_URL = "http://localhost:8998";

	private static final String DATASET_FILE_CSV = "/mnt/d/toy_dataset.csv";

	private static LivyClient client;

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
			JSONObject jsonResponse = new JSONObject();
			if (json.has("config")) {
				JSONObject configs = json.getJSONObject("config");
				JSONArray transformations = configs.getJSONArray("transformations");

				JSONObject trasf1 = transformations.getJSONObject(0);
				String type = trasf1.getString("type");

				client = new LivyClientBuilder().setConf("livy.client.http.job.initial-poll-interval", "100ms")
						.setConf("livy.client.http.job.max-poll-interval", "500ms").setURI(new URI(LIVY_URL)).build();

//				KnowageDataPreparationPreview job;
//				ObjectNode ret;
//
//				job = new KnowageDataPreparationPreview(config, inputPath, isRest, pageSize, isLimit);
//				ret = client.submit(job).get();

				jsonResponse.put("transformations", transformations);
			} else {
				ManageDataSetsForDataPrep mdsfr = new ManageDataSetsForDataPrep();
				jsonResponse = mdsfr.previewDatasetForDataPreparation(json.toString(), getUserProfile());
			}
			toReturnString = jsonResponse.toString();

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
			ManageDataSetsForDataPrep mdsfr = new ManageDataSetsForDataPrep();

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
	private JSONArray createConfigForSqlDataset() {
		JSONArray config = new JSONArray();
		try {
			replace(config, "M", "Male", "GENDER");
		} catch (Exception e) {

		}
//		replace(config, "F", "Female", "GENDER");
//		drop(config, "MI", "ADDRESS1", "ADDRESS2", "ADDRESS3", "ADDRESS4", "CITY", "STATE", "PROVINCE", "POSTAL_CODE", "COUNTRY", "CUSTOMER_REGION_ID",
//				"PHONE1", "PHONE2", "BIRTHDATE", "MARITIAL_STATUS", "YEARLY_INCOME", "TOTAL_CHILDREN", "NUM_CHILDREN_AT_HOME", "EDUCATION", "DATE_ACCNT_OPENED",
//				"MEMBER_CARD", "OCCUPATION", "HOUSEOWNER", "NUM_CARS_OWNED", "FULLNAME");

		// padding(config, ">>>", "<<<", "email");
		// padding(config, " ", null, "email");
		// trim(config, "email");
		// replace(config, ">>>", "", "email");
		// replace(config, "<<<", "", "email");
		// removeNull(config, "date_of_birth");

		return config;
	}

	private void replace(JSONArray config, String a, String b, String... columns) throws JSONException {
		JSONObject step = null;

		step = new JSONObject();

		JSONObject columnParam = new JSONObject();

		for (String column : columns) {

			columnParam.append("columns", column);

		}
		columnParam.put("oldChar", a);
		columnParam.put("newChar", b);

		step.append("parameters", columnParam);
		step.put("type", "replace");

		config.put(step);
	}

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
