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
package it.eng.knowage.engines.svgviewer;

import it.eng.knowage.engines.svgviewer.api.AbstractSvgViewerEngineResource;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;

import javax.ws.rs.Path;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */

@Path("1.0/geo")
@ManageAuthorization
public class SvgViewer extends AbstractSvgViewerEngineResource {

	/*
	 * @Path("/getTargetDataset")
	 *
	 * @GET
	 *
	 * @Produces(MediaType.APPLICATION_JSON)
	 *
	 * @SuppressWarnings({ "rawtypes", "unchecked" }) public String getTargetDataset(@Context HttpServletRequest req) {
	 *
	 * logger.debug("IN");
	 *
	 * try { SvgViewerEngineInstance engineInstance = getEngineInstance();
	 *
	 * IDataSet dataSet = engineInstance.getDataSet();
	 *
	 * Map analyticalDrivers = engineInstance.getAnalyticalDrivers(); Map profileAttributes = UserProfileUtils.getProfileAttributes((UserProfile)
	 * this.getEnv().get(EngineConstants.ENV_USER_PROFILE)); analyticalDrivers.put("LOCALE", getLocale()); dataSet.setParamsMap(analyticalDrivers);
	 * dataSet.setUserProfileAttributes(profileAttributes); dataSet.loadData();
	 *
	 * // Datastore IDataStore dataStore = dataSet.getDataStore();
	 *
	 * for (int i = 0; i < dataStore.getMetaData().getFieldCount(); i++) { IFieldMetaData fieldMeta = dataStore.getMetaData().getFieldMeta(i);
	 * fieldMeta.setName(fieldMeta.getName()); if (fieldMeta.getAlias() != null) { fieldMeta.setAlias(fieldMeta.getAlias()); } // set the fieldType with the
	 * datasets' definition metadata (get settings from the user GUI) fieldMeta.setFieldType(getDsFieldType(dataSet.getDsMetadata(), fieldMeta.getName())); }
	 *
	 * JSONDataWriter dataWriter = new JSONDataWriter(); JSONObject dataSetJSON = (JSONObject) dataWriter.write(dataStore);
	 *
	 * JSONObject metaData = dataSetJSON.getJSONObject("metaData"); JSONArray fields = metaData.getJSONArray("fields"); for (int i = 1; i < fields.length();
	 * i++) { JSONObject field = fields.getJSONObject(i); IFieldMetaData fieldMeta = dataStore.getMetaData().getFieldMeta(i - 1); if
	 * (fieldMeta.getFieldType().equals(FieldType.MEASURE)) { field.put("role", "MEASURE"); } else { field.put("role", "ATTRIBUTE"); }
	 *
	 * }
	 *
	 * return dataSetJSON.toString();
	 *
	 * } catch (Throwable t) { t.printStackTrace(); } finally { logger.debug("OUT"); } return "{}";
	 *
	 * }
	 *
	 * @Path("/getTargetLayer")
	 *
	 * @POST
	 *
	 * @Produces(MediaType.APPLICATION_JSON)
	 *
	 * @SuppressWarnings({ "rawtypes", "unchecked" }) public String getTargetLayer(@Context HttpServletRequest req) throws IOException, JSONException {
	 * JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
	 *
	 * // Boolean featureSourceType = requestVal.has(geoUtils.FEATURE_SOURCE_TYPE); // Boolean featureSource = requestVal.has(geoUtils.FEATURE_SOURCE); Boolean
	 * layerName = requestVal.has(geoUtils.LAYER_NAME); Boolean layerjoinCol = requestVal.has(geoUtils.LAYER_JOIN_COLUMNS); Boolean featureIds =
	 * requestVal.has(geoUtils.FEATURE_IDS);
	 *
	 * if (layerName && layerjoinCol && featureIds) { return targetLayerAction(requestVal); } else { JSONObject err = new JSONObject(); JSONArray ja = new
	 * JSONArray(); err.put("status", "non ok"); if (!layerName) ja.put(geoUtils.LAYER_NAME); if (!layerjoinCol) ja.put(geoUtils.LAYER_JOIN_COLUMNS); if
	 * (!featureIds) ja.put(geoUtils.FEATURE_IDS); err.put("errors", ja); return err.toString(); }
	 *
	 * }
	 */
}
