package it.eng.spagobi.engines.georeport;

import static it.eng.spagobi.engines.georeport.api.restfull.geoUtils.getDsFieldType;
import static it.eng.spagobi.engines.georeport.api.restfull.geoUtils.targetLayerAction;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.georeport.api.AbstractChartEngineResource;
import it.eng.spagobi.engines.georeport.api.restfull.geoUtils;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

@Path("1.0/geo")
@ManageAuthorization
public class GeoResource extends AbstractChartEngineResource {

	@Path("/getTargetDataset")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getTargetDataset(@Context HttpServletRequest req) {

		logger.debug("IN");

		try {
			GeoReportEngineInstance engineInstance = getEngineInstance();

			IDataSet dataSet = engineInstance.getDataSet();

			Map analyticalDrivers = engineInstance.getAnalyticalDrivers();
			Map profileAttributes = UserProfileUtils.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE));
			analyticalDrivers.put("LOCALE", getLocale());
			dataSet.setParamsMap(analyticalDrivers);
			dataSet.setUserProfileAttributes(profileAttributes);
			dataSet.loadData();

			// Datastore
			IDataStore dataStore = dataSet.getDataStore();

			for (int i = 0; i < dataStore.getMetaData().getFieldCount(); i++) {
				IFieldMetaData fieldMeta = dataStore.getMetaData().getFieldMeta(i);
				fieldMeta.setName(fieldMeta.getName());
				if (fieldMeta.getAlias() != null) {
					fieldMeta.setAlias(fieldMeta.getAlias());
				}
				// set the fieldType with the datasets' definition metadata (get settings from the user GUI)
				fieldMeta.setFieldType(getDsFieldType(dataSet.getDsMetadata(), fieldMeta.getName()));
			}

			JSONDataWriter dataWriter = new JSONDataWriter();
			JSONObject dataSetJSON = (JSONObject) dataWriter.write(dataStore);

			JSONObject metaData = dataSetJSON.getJSONObject("metaData");
			JSONArray fields = metaData.getJSONArray("fields");
			for (int i = 1; i < fields.length(); i++) {
				JSONObject field = fields.getJSONObject(i);
				IFieldMetaData fieldMeta = dataStore.getMetaData().getFieldMeta(i - 1);
				if (fieldMeta.getFieldType().equals(FieldType.MEASURE)) {
					field.put("role", "MEASURE");
				} else {
					field.put("role", "ATTRIBUTE");
				}

			}

			return dataSetJSON.toString();

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			logger.debug("OUT");
		}
		return "{}";

	}

	@Path("/getTargetLayer")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getTargetLayer(@Context HttpServletRequest req) throws IOException, JSONException {
		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

		// Boolean featureSourceType = requestVal.has(geoUtils.FEATURE_SOURCE_TYPE);
		// Boolean featureSource = requestVal.has(geoUtils.FEATURE_SOURCE);
		Boolean layerName = requestVal.has(geoUtils.LAYER_NAME);
		Boolean layerjoinCol = requestVal.has(geoUtils.LAYER_JOIN_COLUMNS);
		Boolean featureIds = requestVal.has(geoUtils.FEATURE_IDS);

		if (layerName && layerjoinCol && featureIds) {
			return targetLayerAction(requestVal);
		} else {
			JSONObject err = new JSONObject();
			JSONArray ja = new JSONArray();
			err.put("status", "non ok");
			if (!layerName)
				ja.put(geoUtils.LAYER_NAME);
			if (!layerjoinCol)
				ja.put(geoUtils.LAYER_JOIN_COLUMNS);
			if (!featureIds)
				ja.put(geoUtils.FEATURE_IDS);
			err.put("errors", ja);
			return err.toString();
		}

	}

}
