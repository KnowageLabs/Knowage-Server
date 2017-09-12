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
package it.eng.spagobi.georeport.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;

/**
 * @authors Andrea Gioia (andrea.gioia@eng.it), Fabio D'Ovidio (f.dovidio@inovaos.it)
 */
public class FeaturesProviderDAOWFSImpl extends AbstractHibernateDAO implements IFeaturesProviderWFSDAO {

	public static final String LAYER_NAME = "layerName";
	public static final String GEOID_PNAME = "geoIdPName";
	public static final String GEOID_PVALUE = "geoIdPValue";

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(FeaturesProviderDAOWFSImpl.class);

	@Override
	public SimpleFeature getFeatureById(Object fetureProviderEndPoint, String layerName, Map parameters) {
		FeatureCollection featureCollection;

		String wfsUrl = null;
		String geoIdPName = null;
		String geoIdPValue = null;

		logger.debug("IN");

		try {
			wfsUrl = (String) fetureProviderEndPoint;

			geoIdPName = (String) parameters.get(GEOID_PNAME);
			logger.debug("Parameter [" + GEOID_PNAME + "] is equal to [" + geoIdPName + "]");

			geoIdPValue = (String) parameters.get(GEOID_PVALUE);
			logger.debug("Parameter [" + GEOID_PVALUE + "] is equal to [" + geoIdPValue + "]");

			wfsUrl += "?request=GetFeature" + "&typename=" + layerName + "&Filter=<Filter><PropertyIsEqualTo><PropertyName>" + geoIdPName
					+ "</PropertyName><Literal>" + geoIdPValue + "</Literal></PropertyIsEqualTo></Filter>" + "&outputformat=json" + "&version=1.0.0";

			featureCollection = getFeatures(wfsUrl);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while executing service call [" + wfsUrl + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return (SimpleFeature) featureCollection.features().next();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.georeport.features.provider.IFeaturesProviderDAO#getAllFeatures(java.lang.Object)
	 */
	@Override
	public FeatureCollection getAllFeaturesOLD(Object fetureProviderEndPoint, String layerName) {
		FeatureCollection featureCollection = null;

		String wfsUrl = null;

		logger.debug("IN");

		try {
			wfsUrl = (String) fetureProviderEndPoint;

			wfsUrl += "?request=GetFeature" + "&typename=" + layerName + "&outputformat=json" + "&version=1.0.0";

			featureCollection = getFeatures(wfsUrl);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while executing service call [" + wfsUrl + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return featureCollection;
	}

	@Override
	public FeatureCollection getAllFeatures(String wfsUrl) {
		FeatureCollection featureCollection = null;

		logger.debug("IN");

		try {
			featureCollection = getFeatures(wfsUrl);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while executing service call [" + wfsUrl + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return featureCollection;
	}

	public FeatureCollection getFeatures(String wfsUrl) {
		FeatureCollection featureCollection = null;
		URL url = null;

		URLConnection connection = null;

		logger.debug("IN");

		try {
			String result = null;

			// wfs call
			url = new URL(wfsUrl);
			logger.debug("Opening connection with url [" + wfsUrl + "]...");
			connection = url.openConnection();

			// Get the response
			logger.debug("Loading layer ...");
			BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			result = sb.toString();

			logger.debug("Parseing response ...");
			Reader reader = new StringReader(result);
			FeatureJSON featureJSON = new FeatureJSON();
			featureCollection = featureJSON.readFeatureCollection(reader);

		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while executing service call [" + wfsUrl + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return featureCollection;
	}

	private URL buildURL(String wfsUrl) {
		URL url = null;
		URI uri = null;

		logger.debug("IN");

		try {
			// we use apache URI to properly encode the url string according to RFC2396
			uri = new URI(wfsUrl, false);
			url = new URL(uri.toString());
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while buildin url for address [" + wfsUrl + "]", t);
		}

		logger.debug("OUT");

		return url;
	}

}
