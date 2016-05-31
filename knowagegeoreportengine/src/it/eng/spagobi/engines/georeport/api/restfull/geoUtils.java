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
package it.eng.spagobi.engines.georeport.api.restfull;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.georeport.utils.LayerCache;
import it.eng.spagobi.engines.georeport.utils.Monitor;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoLayersDAO;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class geoUtils {
	public static final String COLUMNLIST = "COLUMNLIST";
	public static final String COLUMN = "COLUMN";
	public static final String COLUMNS = "COLUMNS";
	public static final String DATASET = "DATASET";
	public static final String META = "META";
	public static final String NAME = "name";
	public static final String FIELD_TYPE = "fieldType";
	public static final String FEATURE_SOURCE_TYPE = "featureSourceType";
	public static final String FEATURE_SOURCE = "featureSource";
	public static final String FEATURE_IDS = "featureIds";
	public static final String LAYER_ID = "geoId";
	public static final String LAYER_NAME = "layer";
	public static final String LAYER_JOIN_COLUMNS = "layerJoinColumns";
	public static final String LAYER_URL = "layerUrl";
	public static final String NO_DATASET = "noDataset";

	static private Logger logger = Logger.getLogger(geoUtils.class);

	public static FieldType getDsFieldType(String xml, String fieldName) throws Exception {
		FieldType toReturn = IFieldMetaData.FieldType.ATTRIBUTE;
		SourceBean sbXML;

		sbXML = SourceBean.fromXMLString(xml);
		// Columns Metadata Properties
		SourceBean sbColumns = (SourceBean) sbXML.getAttribute(COLUMNLIST);
		List lst = sbColumns.getAttributeAsList(COLUMN);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			SourceBean sbRow = (SourceBean) iterator.next();
			String name = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
			if (fieldName.equalsIgnoreCase(name)) {
				String dsFieldType = sbRow.getAttribute(FIELD_TYPE) != null ? sbRow.getAttribute(FIELD_TYPE).toString() : null;
				if (dsFieldType.equalsIgnoreCase("MEASURE")) {
					toReturn = IFieldMetaData.FieldType.MEASURE;
				} else if (dsFieldType.equalsIgnoreCase("ATTRIBUTE")) {
					toReturn = IFieldMetaData.FieldType.ATTRIBUTE;
				}
				break;
			}
		}

		return toReturn;
	}

	public static String targetLayerAction(JSONObject req) throws JSONException {
		String layerName = req.getString(LAYER_NAME);
		String layerCol = req.optString(LAYER_JOIN_COLUMNS);
		String featureIds = req.optString(FEATURE_IDS);
		Boolean noDataset = req.optBoolean(geoUtils.NO_DATASET);

		try {
			Monitor.start("GetTargetLayerAction.doService");

			FeatureCollection outputFeatureCollection = LayerCache.cache.get(layerName);

			if (outputFeatureCollection == null) {
				logger.debug("Layer [" + FEATURE_SOURCE_TYPE + "] is not in cache");
				try {
					Monitor.start("GetTargetLayerAction.getFeature");
					// load layer from catalogue
					ISbiGeoLayersDAO geoLayersDAO = DAOFactory.getSbiGeoLayerDao();
					geoLayersDAO.setUserProfile(UserProfileManager.getProfile());
					GeoLayer geoLayer = geoLayersDAO.loadLayerByLabel(layerName);
					// TODO check if geolayer is not null

					JSONObject layerDef = new JSONObject(new String(geoLayer.getLayerDef()));
					String source = geoLayer.getType().equals("File") ? geoLayer.getPathFile() : layerDef.getString("layer_url");

					if (geoLayer.getType().equals("File")) {
						outputFeatureCollection = DAOFactory.getFeaturesProviderFileDAO().getAllFeatures(source);
					} else {
						outputFeatureCollection = DAOFactory.getFeaturesProviderWFSDAO().getAllFeatures(source);
					}
					Assert.assertNotNull(outputFeatureCollection, "The feature source returned a null object");
					logger.debug("GetTargetLayerAction.getFeature " + Monitor.elapsed("GetTargetLayerAction.getFeature"));

					LayerCache.cache.put(layerName, outputFeatureCollection);
				} catch (Throwable t2) {
					logger.error("Impossible to load layer [" + layerName + "] " + "from source of type [] " + "whose endpoint is [ ]", t2);

					Throwable root = t2;
					while (root.getCause() != null)
						root = root.getCause();

					String message = "Impossible to load layer [" + layerName + "] " + "from source of type [ ] " + "whose endpoint is [ ]: "
							+ root.getMessage();

					// servletIOManager.writeBackToClient(500, message,
					// true ,"service-response", "text/plain");
					JSONObject resObj = new JSONObject();
					resObj.put("status", "non ok");
					resObj.put("errors", message);
					return resObj.toString();
				}
			} else {
				logger.debug("Layer [" + FEATURE_SOURCE_TYPE + "] is in cache");
			}
			List<SimpleFeature> list = new ArrayList<SimpleFeature>();
			if (noDataset) {
				FeatureIterator it = outputFeatureCollection.features();
				while (it.hasNext()) {
					SimpleFeature f = (SimpleFeature) it.next();
					list.add(f);
				}
			} else {
				JSONArray featuresIdJSON = new JSONArray(featureIds);
				Map<String, String> idIndex = new HashMap<String, String>();
				for (int i = 0; i < featuresIdJSON.length(); i++) {
					String s = featuresIdJSON.getString(i);
					idIndex.put(s, s);
				}
				FeatureIterator it = outputFeatureCollection.features();
				while (it.hasNext()) {
					SimpleFeature f = (SimpleFeature) it.next();
					Property property = f.getProperty(layerCol);
					if (property != null) {
						String id = "" + property.getValue();
						if (idIndex.containsKey(id)) {
							list.add(f);
						}
					} else {
						logger.warn("Impossible to read attribute [" + layerCol + "] from feature [" + f + "]");
					}

				}
			}

			FeatureCollection<SimpleFeatureType, SimpleFeature> filteredOutputFeatureCollection = DataUtilities.collection(list);

			Monitor.start("GetTargetLayerAction.flushResponse");
			FeatureJSON featureJSON = new FeatureJSON();
			String responseFeature = featureJSON.toString(filteredOutputFeatureCollection);

			// servletIOManager.tryToWriteBackToClient(responseFeature);
			logger.debug("GetTargetLayerAction.flushResponse " + Monitor.elapsed("GetTargetLayerAction.flushResponse"));
			logger.debug("GetTargetLayerAction.doService " + Monitor.elapsed("GetTargetLayerAction.doService"));
			return responseFeature;

		} catch (Throwable t) {
			logger.error("An unexpected error occured while loading target layer", t);

			return "{status:'non ok', errors:'An unexpected error occured while loading target layer'}";
			// servletIOManager.writeBackToClient(500, "An unexpected error occured while loading target layer" ,true, "service-response", "text/plain");

		} finally {
			logger.debug("OUT");
		}
	}
}
