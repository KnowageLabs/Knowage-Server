/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.georeport.features.provider;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.georeport.GeoReportEngine;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.assertion.NullReferenceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

/**
 * @authors Andrea Gioia (andrea.gioia@eng.it)
 */
public class FeaturesProviderDAOFileImpl implements IFeaturesProviderDAO {

	File resourceFolder;
	String indexOnAttribute;
	String indexOnFile;
	Map<String, SimpleFeature> lookupTable;
	
	public static final String GEOID_PNAME = "geoIdPName";
	public static final String GEOID_PVALUE = "geoIdPValue";

	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(FeaturesProviderDAOFileImpl.class);
    
    public FeaturesProviderDAOFileImpl(File resourceFolder) {
    	this.resourceFolder = resourceFolder;
    }
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.georeport.features.provider.IFeaturesProviderDAO#getFeatureById(java.lang.Object, java.util.Map)
	 */
	public SimpleFeature getFeatureById(Object featureProviderEndPoint, String layerName, Map parameters) {
		
		SimpleFeature feature;
		String geoIdPValue;
		
		logger.debug("IN");
		
		feature = null;
		geoIdPValue = null;
		try {
			String geoIdPName = (String)parameters.get(GEOID_PNAME);
			logger.debug("Parameter [" + GEOID_PNAME + "] is equal to [" + geoIdPName + "]");
			Assert.assertNotNull(geoIdPName, "Parameter [" + GEOID_PNAME + "] cannot be null");
				
			geoIdPValue = (String)parameters.get(GEOID_PVALUE);
			logger.debug("Parameter [" + GEOID_PVALUE + "] is equal to [" + geoIdPValue + "]");
			Assert.assertNotNull(geoIdPName, "Parameter [" + GEOID_PNAME + "] cannot be null");
			
			if(!((String)featureProviderEndPoint).equalsIgnoreCase(indexOnFile) 
					|| !geoIdPName.equalsIgnoreCase(indexOnAttribute)) {
				createIndex((String)featureProviderEndPoint, geoIdPName);
			}
			
			logger.debug("Searching for feature [" + geoIdPValue +"] ...");		
			feature = lookupTable.get(geoIdPValue);
			logger.debug("Feature [" + geoIdPValue +"] succesfully found");						
		} catch(FeaturesProviderRuntimeException t) {
			throw t;
		} catch(Throwable t) {
			throw new FeaturesProviderRuntimeException("An error occured while retrieving feature with id [" + geoIdPValue + "] from endpoint [" + featureProviderEndPoint + "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return feature;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.georeport.features.provider.IFeaturesProviderDAO#getAllFeatures(java.lang.Object)
	 */
	public FeatureCollection getAllFeatures(Object featureProviderEndPoint, String layerName) {
		FeatureCollection featureCollection;
		
		logger.debug("IN");
		
		featureCollection = null;
		try {
			String fileName = (String)featureProviderEndPoint;
			File targetFile = new File(resourceFolder, fileName);
			logger.debug("Target file full name is equal to [" + targetFile + "]");
			featureCollection = loadFeaturesFromFile( targetFile );
		} catch(FeaturesProviderRuntimeException t) {
			throw t;
		} catch(Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while retrieving features from endpoint [" + featureProviderEndPoint + "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return featureCollection;
	}
	
	
	// =======================================================================================================
	// Private methods
	// =======================================================================================================
	
	private void createIndex(String filename, String geoIdPName) {
		
		File targetFile;
		
		logger.debug("IN");
		
		try {
			Assert.assertTrue(!StringUtilities.isEmpty(filename), "Input parameter [filename] cannot be null or empty");
			Assert.assertTrue(!StringUtilities.isEmpty(geoIdPName), "Input parameter [filename] cannot be null or empty");
			
			logger.debug("Indexing file [" + filename + "] on attribute [" + geoIdPName + "] ...");
			
			indexOnFile = filename;
			indexOnAttribute = geoIdPName;
			lookupTable = new HashMap<String, SimpleFeature>();
			
			logger.debug("Resource dir is equal to [" + resourceFolder + "]");
			targetFile = new File(resourceFolder, filename);
			logger.debug("Target file full name is equal to [" + targetFile + "]");
			
			FeatureCollection featureCollection = loadFeaturesFromFile( targetFile );

			logger.debug("Target file contains [" + featureCollection.size() + "] features to index");
			if ( featureCollection.size() == 0) {
				throw new FeaturesProviderRuntimeException("Impossible to find features in file [" + filename +"]");
			}
			
			FeatureIterator iterator = featureCollection.features();
	    	while (iterator.hasNext()) {
	    		Feature feature = iterator.next();
	    		if( (feature instanceof SimpleFeature) == false ) {
	    			throw new FeaturesProviderRuntimeException("Feature [" + feature.getIdentifier() + "] is not a simple feature");
	    		}
	    		
	    		SimpleFeature simpleFeature = (SimpleFeature)feature; 
	    		Object idx = simpleFeature.getProperty(geoIdPName).getValue();
	    		lookupTable.put(idx.toString(), simpleFeature);
				logger.debug("Feature [" + idx + "] added to the index");
	    	}
			
			logger.debug("File [" + filename + "] indexed succesfully on attribute [" + geoIdPName + "]");
			
		} catch(FeaturesProviderRuntimeException t) {
			indexOnAttribute = null;
			lookupTable = null;
			throw new SpagoBIRuntimeException("Impossible to create index on file [" + filename + "]", t);
		} catch(Throwable t) {
			indexOnAttribute = null;
			lookupTable = null;
			throw new SpagoBIRuntimeException("An unexpected error occured while creating index on file [" + filename + "]", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	private FeatureCollection loadFeaturesFromFile(File targetFile) {
		
		FeatureCollection result;
		BufferedReader reader;
		StringBuffer buffer;
		String line;
		
		try {
			Assert.assertNotNull(targetFile, "Input parameter [targetFile] cannot be null");
			
			if(targetFile.exists() == false && targetFile.canRead() == false) {
				throw new FeaturesProviderRuntimeException("Impossible to load features. File [" + targetFile + "] cannot be read");
			}
			reader = new BufferedReader(new FileReader( targetFile ));
	        buffer = new StringBuffer();
	
	        while ((line = reader.readLine()) != null) {
	        	buffer.append(line);        
	        }
	        reader.close();
	        String featureStr = buffer.toString();		        

	        Reader strReader = new StringReader( featureStr );
	        FeatureJSON featureJ = new FeatureJSON();
	        result = featureJ.readFeatureCollection(strReader);
	    } catch(Throwable t) {
	    	throw new SpagoBIRuntimeException(t);
	    }
         
        return result;
	}
}
