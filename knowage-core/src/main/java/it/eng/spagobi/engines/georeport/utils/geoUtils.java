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
package it.eng.spagobi.engines.georeport.utils;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.georeport.dao.IFeaturesProviderFileDAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class geoUtils {
	public static final String LAYER_URL = "layerUrl";

	private static Logger logger = Logger.getLogger(geoUtils.class);

	public static String getFileLayerAction(String layerUrl) throws JSONException, IOException, EMFUserError {
		IFeaturesProviderFileDAO featuresProvider = DAOFactory.getFeaturesProviderFileDAO();
		FeatureCollection outputFeatureCollection = featuresProvider.getAllFeatures(layerUrl);
		FeatureIterator it = outputFeatureCollection.features();
		List<SimpleFeature> list = new ArrayList<SimpleFeature>();
		while (it.hasNext()) {
			SimpleFeature f = (SimpleFeature) it.next();
			list.add(f);
		}

		FeatureCollection<SimpleFeatureType, SimpleFeature> filteredOutputFeatureCollection = DataUtilities.collection(list);

		Monitor.start("GetTargetLayerAction.flushResponse");
		FeatureJSON featureJSON = new FeatureJSON();
		String responseFeature = featureJSON.toString(filteredOutputFeatureCollection);

		return responseFeature;
	}

}
