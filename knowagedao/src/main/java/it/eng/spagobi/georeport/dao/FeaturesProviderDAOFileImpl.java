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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @authors Andrea Gioia (andrea.gioia@eng.it)
 */
public class FeaturesProviderDAOFileImpl extends AbstractHibernateDAO implements IFeaturesProviderFileDAO {

	/** Logger component. */
	private static final Logger LOGGER = Logger.getLogger(FeaturesProviderDAOFileImpl.class);

	public static final String GEOID_PNAME = "geoIdPName";
	public static final String GEOID_PVALUE = "geoIdPValue";

	File resourceFolder;
	String indexOnAttribute;
	String indexOnFile;
	Map<String, SimpleFeature> lookupTable;

	public FeaturesProviderDAOFileImpl(File resourceFolder) {
		this.resourceFolder = resourceFolder;
	}

	public FeaturesProviderDAOFileImpl() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.georeport.features.provider.IFeaturesProviderDAO#getFeatureById(java.lang.Object, java.util.Map)
	 */
	@Override
	public SimpleFeature getFeatureById(Object featureProviderEndPoint, String layerName, Map parameters) {

		SimpleFeature feature;
		String geoIdPValue;

		LOGGER.debug("IN");

		feature = null;
		geoIdPValue = null;
		try {
			String geoIdPName = (String) parameters.get(GEOID_PNAME);
			LOGGER.debug("Parameter [" + GEOID_PNAME + "] is equal to [" + geoIdPName + "]");
			Assert.assertNotNull(geoIdPName, "Parameter [" + GEOID_PNAME + "] cannot be null");

			geoIdPValue = (String) parameters.get(GEOID_PVALUE);
			LOGGER.debug("Parameter [" + GEOID_PVALUE + "] is equal to [" + geoIdPValue + "]");
			Assert.assertNotNull(geoIdPName, "Parameter [" + GEOID_PNAME + "] cannot be null");

			if (!((String) featureProviderEndPoint).equalsIgnoreCase(indexOnFile)
					|| !geoIdPName.equalsIgnoreCase(indexOnAttribute)) {
				createIndex((String) featureProviderEndPoint, geoIdPName);
			}

			LOGGER.debug("Searching for feature [" + geoIdPValue + "] ...");
			feature = lookupTable.get(geoIdPValue);
			LOGGER.debug("Feature [" + geoIdPValue + "] succesfully found");
		} catch (FeaturesProviderRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw new FeaturesProviderRuntimeException("An error occured while retrieving feature with id ["
					+ geoIdPValue + "] from endpoint [" + featureProviderEndPoint + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}

		return feature;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.georeport.features.provider.IFeaturesProviderDAO#getAllFeatures(java.lang.Object)
	 */

	@Override
	public FeatureCollection getAllFeatures(String srcFile) {
		FeatureCollection featureCollection;

		LOGGER.debug("IN");

		featureCollection = null;
		try {
			File targetFile = new File(srcFile);
			LOGGER.debug("Target file full name is equal to [" + targetFile + "]");
			featureCollection = loadFeaturesFromFile(targetFile);
		} catch (FeaturesProviderRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException(
					"An unexpected error occured while retrieving features from src file [" + srcFile + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}

		return featureCollection;
	}

	@Override
	public FeatureCollection getAllFeaturesOLD(Object featureProviderEndPoint, String layerName) {
		FeatureCollection featureCollection;

		LOGGER.debug("IN");

		featureCollection = null;
		try {
			String fileName = (String) featureProviderEndPoint;
			File targetFile = new File(resourceFolder, fileName);
			LOGGER.debug("Target file full name is equal to [" + targetFile + "]");
			featureCollection = loadFeaturesFromFile(targetFile);
		} catch (FeaturesProviderRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while retrieving features from endpoint ["
					+ featureProviderEndPoint + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}

		return featureCollection;
	}

	// =======================================================================================================
	// Private methods
	// =======================================================================================================

	private void createIndex(String filename, String geoIdPName) {

		File targetFile;

		LOGGER.debug("IN");

		try {
			Assert.assertTrue(!StringUtils.isEmpty(filename), "Input parameter [filename] cannot be null or empty");
			Assert.assertTrue(!StringUtils.isEmpty(geoIdPName), "Input parameter [filename] cannot be null or empty");

			LOGGER.debug("Indexing file [" + filename + "] on attribute [" + geoIdPName + "] ...");

			indexOnFile = filename;
			indexOnAttribute = geoIdPName;
			lookupTable = new HashMap<>();

			LOGGER.debug("Resource dir is equal to [" + resourceFolder + "]");
			targetFile = new File(resourceFolder, filename);
			LOGGER.debug("Target file full name is equal to [" + targetFile + "]");

			FeatureCollection featureCollection = loadFeaturesFromFile(targetFile);

			LOGGER.debug("Target file contains [" + featureCollection.size() + "] features to index");
			if (featureCollection.size() == 0) {
				throw new FeaturesProviderRuntimeException("Impossible to find features in file [" + filename + "]");
			}

			FeatureIterator iterator = featureCollection.features();
			while (iterator.hasNext()) {
				Feature feature = iterator.next();
				if (!(feature instanceof SimpleFeature)) {
					throw new FeaturesProviderRuntimeException(
							"Feature [" + feature.getIdentifier() + "] is not a simple feature");
				}

				SimpleFeature simpleFeature = (SimpleFeature) feature;
				Object idx = simpleFeature.getProperty(geoIdPName).getValue();
				lookupTable.put(idx.toString(), simpleFeature);
				LOGGER.debug("Feature [" + idx + "] added to the index");
			}

			LOGGER.debug("File [" + filename + "] indexed succesfully on attribute [" + geoIdPName + "]");

		} catch (FeaturesProviderRuntimeException t) {
			indexOnAttribute = null;
			lookupTable = null;
			throw new SpagoBIRuntimeException("Impossible to create index on file [" + filename + "]", t);
		} catch (Throwable t) {
			indexOnAttribute = null;
			lookupTable = null;
			throw new SpagoBIRuntimeException(
					"An unexpected error occured while creating index on file [" + filename + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	private FeatureCollection loadFeaturesFromFile(File targetFile) {

		FeatureCollection result;
		String line;

		try {
			Assert.assertNotNull(targetFile, "Input parameter [targetFile] cannot be null");

			if (!targetFile.exists() && !targetFile.canRead()) {
				throw new FeaturesProviderRuntimeException(
						"Impossible to load features. File [" + targetFile + "] cannot be read");
			}
			String featureStr = null;
			try (BufferedReader reader = new BufferedReader(new FileReader(targetFile))) {
				StringBuilder buffer = new StringBuilder();

				while ((line = reader.readLine()) != null) {
					buffer.append(line);
				}
				featureStr = buffer.toString();
			}

			Reader strReader = new StringReader(featureStr);
			FeatureJSON featureJ = new FeatureJSON();
			result = featureJ.readFeatureCollection(strReader);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException(t);
		}

		return result;
	}
}
