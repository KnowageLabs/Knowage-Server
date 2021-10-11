package it.knowage.api.datapreparation;

import java.util.HashSet;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.api.common.AbstractDataSetResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.service.ManageDataSetsForREST;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

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
	public String getDataSetInfo(@PathParam("label") String label) {
		JSONObject jsonResponse = new JSONObject();
		try {
			Monitor timing = MonitorFactory.start("Knowage.datapreparation.getDataSetInfo");
			JSONObject jsonData = new JSONObject();
			JSONArray jsonMetadataColumns = new JSONArray();
			JSONObject jsonMetadataColumn = new JSONObject();
			IDataSet dataSet = getDatasetManagementAPI().getDataSet(label);
			Set<String> qbeHiddenColumns = getQbeDataSetHiddenColumns(dataSet);

			IMetaData metadata = dataSet.getMetadata();
			for (int i = 0; i < metadata.getFieldCount(); i++) {
				IFieldMetaData fieldMetaData = metadata.getFieldMeta(i);
				String alias = fieldMetaData.getAlias();
				if (qbeHiddenColumns.contains(alias))
					continue;
				JSONObject json = new JSONObject();
				json.put("name", fieldMetaData.getName());
				json.put("alias", alias);
				json.put("type", fieldMetaData.getType().toString());
				jsonMetadataColumns.put(json);

			}
			jsonMetadataColumn.put("columns", jsonMetadataColumns);

			jsonData.put("id", dataSet.getId());
			jsonData.put("label", dataSet.getLabel());
			jsonData.put("name", dataSet.getName());
			jsonData.put("description", dataSet.getDescription());
			jsonData.put("catTypeCd", dataSet.getCategoryCd());
			jsonData.put("catTypeId", dataSet.getCategoryId());
			jsonData.put("pars", dataSet.getParameters());
			jsonData.put("meta", jsonMetadataColumn);
			jsonResponse.put("dataset", jsonData);
			timing.stop();
		} catch (JSONException e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} catch (Exception e) {
			logger.error("Error while previewing dataset " + label, e);
			throw new SpagoBIRuntimeException("Error while previewing dataset " + label + ". " + e.getMessage(), e);
		}
		return jsonResponse.toString();
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

			toReturnString = mdsfr.previewDatasetForDataPreparation(json.toString(), getUserProfile());

		} catch (JSONException e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} catch (Exception e) {
			logger.error("Error while previewing dataset " + label, e);
			throw new SpagoBIRuntimeException("Error while previewing dataset " + label + ". " + e.getMessage(), e);
		}
		return toReturnString;
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
