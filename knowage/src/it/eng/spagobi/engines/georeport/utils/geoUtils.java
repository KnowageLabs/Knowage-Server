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

	static private Logger logger = Logger.getLogger(geoUtils.class);

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
