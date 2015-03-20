/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.georeport.services;

import it.eng.spagobi.engines.georeport.GeoReportEngineInstance;
import it.eng.spagobi.engines.georeport.features.provider.FeaturesProviderDAOFactory;
import it.eng.spagobi.engines.georeport.features.provider.IFeaturesProviderDAO;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.BaseServletIOManager;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.service.AbstractBaseServlet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;


/**
 * @authors Andrea Gioia (andrea.gioia@eng.it), Fabio D'Ovidio (f.dovidio@inovaos.it)
 */
public class MapOlAction extends AbstractBaseServlet {
	
	public static final String FEATURE_SOURCE_TYPE = "featureSourceType";
	public static final String FEATURE_SOURCE = "featureSource";
	
	public static final String LAYER_NAME = "layer";
	public static final String BUSINESSID_PNAME = "businessId";
	public static final String GEOID_PNAME = "geoId";
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(MapOlAction.class);
    
    
	public void doService( BaseServletIOManager servletIOManager ) throws SpagoBIEngineException {
		 
		// srsName=EPSG:4326
		
		String featureSourceType;
		String featureSource;
		String layerName;
		String businessIdPName;
		String geoIdPName;
		String geoIdPValue;
		
		GeoReportEngineInstance engineInstance;
		
		IDataSet dataSet;
		IDataStore dataStore;
		IMetaData dataStoreMeta;
		
		logger.debug("IN");
		
		try {
			
			featureSourceType = servletIOManager.getParameterAsString(FEATURE_SOURCE_TYPE); 
			logger.debug("Parameter [" + FEATURE_SOURCE_TYPE + "] is equal to [" + featureSourceType + "]");
			Assert.assertNotNull(featureSourceType, "Input parameter [" + FEATURE_SOURCE_TYPE + "] cannot be null");
						
			featureSource = servletIOManager.getParameterAsString(FEATURE_SOURCE); 
			logger.debug("Parameter [" + FEATURE_SOURCE + "] is equal to [" + featureSourceType + "]");
			Assert.assertNotNull(featureSourceType, "Input parameter [" + FEATURE_SOURCE + "] cannot be null");
			
			layerName = servletIOManager.getParameterAsString(LAYER_NAME); 
			logger.debug("Parameter [" + LAYER_NAME + "] is equal to [" + layerName + "]");
			Assert.assertNotNull(layerName, "Input parameter [" + LAYER_NAME + "] cannot be null");
			
			businessIdPName = servletIOManager.getParameterAsString(BUSINESSID_PNAME); 
			logger.debug("Parameter [" + BUSINESSID_PNAME + "] is equal to [" + businessIdPName + "]");
			Assert.assertNotNull(businessIdPName, "Input parameter [" + BUSINESSID_PNAME + "] cannot be null");
			
			geoIdPName = servletIOManager.getParameterAsString(GEOID_PNAME); 
			logger.debug("Parameter [" + GEOID_PNAME + "] is equal to [" + geoIdPName + "]");
			Assert.assertNotNull(geoIdPName, "Input parameter [" + GEOID_PNAME + "] cannot be null");
			
			engineInstance =  (GeoReportEngineInstance)servletIOManager.getHttpSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
			
			//DataSet
			dataSet = engineInstance.getDataSet();
			dataSet.setParamsMap(engineInstance.getEnv());
			dataSet.loadData();
			
			//Datastore 
			dataStore = dataSet.getDataStore();
			dataStoreMeta = dataStore.getMetaData();
			      
			// # COL NUMBER
			int nc = dataStoreMeta.getFieldCount();

			//Create Output Collection of Features
			SimpleFeatureCollection outputFeatureCollection = FeatureCollections.newCollection();
			SimpleFeatureCollection features  = FeatureCollections.newCollection();
			// used to avoid multiple creation of the same geographical feature
			Set geoIdPValues = new HashSet();			

			// for each row
			Iterator it = dataStore.iterator();
			while(it.hasNext()) { // itera sui record del datasert 
			       
				IRecord record = (IRecord)it.next();
			    IField field;
			    Map<String, String> parameters = new HashMap<String, String>();
			    
			    field = record.getFieldAt( dataStoreMeta.getFieldIndex(businessIdPName) ); //recupera il campo di join
		        
			    //IDfetaure
				geoIdPValue = "" + field.getValue();
				
				if(geoIdPValues.contains(geoIdPValue)) { //aggiunge alla hahkmap tutti i valori per quella colonna di join
					continue;
				} else {
					geoIdPValues.add(geoIdPValue);
				}
				parameters.put("layerName", layerName);
			    parameters.put("geoIdPName", geoIdPName);
			    parameters.put("geoIdPValue", geoIdPValue);
	    			
				// geoserver call
			    try {
			    	//loads the feature of the layer with the value specified.
				    String attrDesc = "";
				    //defines the new featureType (it should be the merge of the db with the file/WFS properties)
				    for(int j=0; j<nc; j++){
				    	//loads from db 
	        			String propName = dataStoreMeta.getFieldAlias(j).toUpperCase();
	        			Object propValue = record.getFieldAt( dataStoreMeta.getFieldIndex(dataStoreMeta.getFieldAlias(j)) ).getValue();			
	        			Object propType = propValue.getClass();
			            //simple property type: the only types managed by the lib are : String, Double, Point,..
				    	String simpleType = propValue.getClass().getSimpleName();
				    	if (simpleType.equals("Long") || simpleType.equals("BigDecimal")){
				    		simpleType = "Double";
				    	}
				    	attrDesc += propName+":"+simpleType;
				    	if (j < nc-1){
				    		attrDesc += ",";
				    	}
	        			logger.debug("propName: " + propName + " - propType: " + String.valueOf(propType));
			        }
			    	SimpleFeature feature = this.getFeature(featureSourceType, featureSource, layerName, geoIdPName, geoIdPValue);			    	
			    	//loads props from file/WFS
			    	List fileAttrs = feature.getFeatureType().getAttributeDescriptors();	
			    	for(int k=0; k<fileAttrs.size(); k++){
			    		if (k==0 && !attrDesc.equals("")) attrDesc += ",";
			    		AttributeDescriptor attr = (AttributeDescriptor)fileAttrs.get(k);			    		
			            //simple property type: the only types managed by the lib are : String, Double, Point,.. 		        
		            	String simpleType = attr.getType().getBinding().getSimpleName();
		            	if (simpleType.equals("Long") || simpleType.equals("BigDecimal")){
				    		simpleType = "Double";
				    	}
		            	attrDesc += attr.getName()+":"+simpleType;
			    		if (k < fileAttrs.size()-1){
				    		attrDesc += ",";
				    	}
			    	}
			    	//create new feature type with ALL properties (db + file/WFS)
			    	SimpleFeatureType TYPE = DataUtilities.createType("NewType", attrDesc);
			    	SimpleFeature newSF = SimpleFeatureBuilder.retype(feature, TYPE);
			        //for each dataset column sets the property values to the new feature
			        for(int j=0; j<nc; j++){
	        			String propName = dataStoreMeta.getFieldAlias(j).toUpperCase();
	        			Object propValue = record.getFieldAt( dataStoreMeta.getFieldIndex(dataStoreMeta.getFieldAlias(j)) ).getValue();			
	        			newSF.setAttribute(propName, propValue);
			        }
			        //adds the new feature to the collection
			        outputFeatureCollection.add(newSF);
			        
			      } catch (Exception e) {
			    	  e.printStackTrace();
			      }
				}
				FeatureJSON featureJ = new FeatureJSON();				
			    String responseFeature = featureJ.toString(outputFeatureCollection);
			    servletIOManager.tryToWriteBackToClient(responseFeature);
			
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {
			logger.debug("OUT");
		}
	}

	public void handleException(BaseServletIOManager servletIOManager,
			Throwable t) {
		t.printStackTrace();		
	}
	
	/**
	 * Returns the feature where the property has the value in input.
	 * 
	 * @param featureSourceType the type of source 
	 * @param featureSource the real source
	 * @param layerName the name of the layer 
	 * @param geoIdPName the name of the property of the join
	 * @param geoIdPValue the value to match
	 * 
	 * @return a simpleFeature object; null if the value isn't matched
	 */
	private SimpleFeature getFeature(String featureSourceType, String featureSource, 
			String layerName, String geoIdPName, String geoIdPValue) {	
		
		IFeaturesProviderDAO featuresProvider = FeaturesProviderDAOFactory.getFeaturesProviderDAO(featureSourceType);
		SimpleFeature feature;
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("geoIdPName", geoIdPName);
		parameters.put("geoIdPValue", geoIdPValue);
		    
		feature = featuresProvider.getFeatureById(featureSource, layerName, parameters);
		
        return feature;
	}
	
	private IDataSet getDataSet(BaseServletIOManager servletIOManager) {
		IDataSet dataSet;
		DataSetServiceProxy datasetProxy;
		String user;
		String label;
		
		user = servletIOManager.getParameterAsString("userId");
		label = servletIOManager.getParameterAsString("label");
		
		datasetProxy = new DataSetServiceProxy(user, servletIOManager.getHttpSession());
		dataSet =  datasetProxy.getDataSetByLabel(label);
		
		return dataSet;
	}

}

