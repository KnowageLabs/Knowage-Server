/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.georeport.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import it.eng.spagobi.engines.georeport.GeoReportEngine;
import it.eng.spagobi.engines.georeport.GeoReportEngineConfig;
import it.eng.spagobi.engines.georeport.GeoReportEngineInstance;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.AbstractEngineStartServlet;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import javax.servlet.RequestDispatcher;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * @authors Andrea Gioia (andrea.gioia@eng.it)
 */
public class GeoReportEngineStartEditAction extends AbstractEngineStartServlet {

	
	private static final String DEFAULT_MAP_NAME = "Sud Tirol";
	private static final String DEFAULT_ANALYSIS_TYPE = "choropleth"; //"proportionalSymbols";
	
	private static final long serialVersionUID = 1L;
	private static final String ENGINE_NAME = "GeoReportEngine";
	private static final String REQUEST_DISPATCHER_URL = "/WEB-INF/jsp/geoReport.jsp";
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(GeoReportEngineStartEditAction.class);
    
	
	public void doService( EngineStartServletIOManager servletIOManager ) throws SpagoBIEngineException {
		
		GeoReportEngineInstance engineInstance;
		RequestDispatcher requestDispatcher;
		
         
        logger.debug("IN");
        
        try {
        	String datasetLabel = (String)servletIOManager.getParameter("dataset_label");
        	
        	JSONObject template = null;
        	IDataSet dataSet = null;
        	
        	if(datasetLabel != null) {
        		logger.debug("Creating map on dataset [" + datasetLabel + "]");
        		dataSet = servletIOManager.getDataSetServiceProxy().getDataSetByLabel(datasetLabel);
        		if(dataSet == null) {
        			throw new RuntimeException("Impossible to load dataset [" + datasetLabel + "]");
        		}
        		template = buildTemplateOverDataset(dataSet);
        	} else {
        		logger.debug("Creating map on measure catalogue");
        		template = buildTemplateOverMeasureCatalogue();
        	}
     
        	
        	// create a new engine instance
        	engineInstance = GeoReportEngine.createInstance(
        			template.toString(), // servletIOManager.getTemplateAsString(), 
        			servletIOManager.getEnv()
        	);
        	
        	engineInstance.getEnv().put(EngineConstants.ENV_DATASET, dataSet);
        	
        	servletIOManager.getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
        	
        	// dispatch the request to the presentation layer
        	requestDispatcher = getServletContext().getRequestDispatcher( REQUEST_DISPATCHER_URL );
            try {
            	requestDispatcher.forward(servletIOManager.getRequest(), servletIOManager.getResponse());
    		} catch (Throwable t) {
    			throw new SpagoBIEngineException(ENGINE_NAME, "An error occurred while dispatching request to [" + REQUEST_DISPATCHER_URL + "]", t);
    		} 
        } catch(Throwable t) {
        	logger.error("Impossible to execute document", t);
        	t.printStackTrace();
        	throw new SpagoBIEngineException(ENGINE_NAME, t.getMessage(),t);
        } finally {
        	logger.debug("OUT");        	 
        }        

	}
	private JSONObject buildTemplateOverMeasureCatalogue() {
		JSONObject template;
		
		logger.debug("IN");
		
		template = new JSONObject();
		try {
			
			template.put("indicatorContainer", "store");
			template.put("storeType", "virtualStore");
			
			template.put("mapName", DEFAULT_MAP_NAME);
			template.put("analysisType", DEFAULT_ANALYSIS_TYPE);
			template.put("analysisConf", buildAnalysisConf(null));
			
			//Properties level = GeoReportEngineConfig.getInstance().getLevels().get(1);
			//String levelName = level.getProperty("name");
			//template.put("geoId", getGeoId(levelName));
			
			template.put("selectedBaseLayer", "OpenStreetMap");
			
			//template.put("targetLayerConf", buildTargetLayerConf(levelName));
			
			template.put("controlPanelConf", buildControlPanelConf(null));
			template.put("toolbarConf", buildToolbarConf(null));
			
			//Properties levelProps = GeoReportEngineConfig.getInstance().getLevelByName(levelName);
			//String centralPoint = levelProps.getProperty("layer_cetral_point");
			//template.put("lon", centralPoint.split(" ")[0]);
			//template.put("lat", centralPoint.split(" ")[1]);
			//template.put("zoomLevel", levelProps.getProperty("layer_zoom") );
		} catch (Throwable t) {
			throw new RuntimeException(
					"An unexpected error occured while executing building template",
					t);
		} finally {
			logger.debug("OUT");
		}
		
		return template;
	}
	
	private JSONObject buildTemplateOverDataset(IDataSet dataSet) {
		JSONObject template;
		
		logger.debug("IN");
		
		template = new JSONObject();
		try {
			
			template.put("indicatorContainer", "store");
			template.put("storeType", "physicalStore");
			
			template.put("mapName", DEFAULT_MAP_NAME);
			template.put("analysisType", DEFAULT_ANALYSIS_TYPE);
			template.put("analysisConf", buildAnalysisConf(dataSet));
			template.put("feautreInfo", buildFeatureInfo(dataSet));
			template.put("indicators", buildIndicators(dataSet));
			
			IFieldMetaData geoIdFieldMeta = getGeoIdFiledMeta(dataSet);
			String businessId = (String)geoIdFieldMeta.getName();
			String levelName = (String)geoIdFieldMeta.getProperty("hierarchy_level");
			
			template.put("businessId", businessId);
			template.put("geoId", getGeoId(levelName));
			
			//template.put("selectedBaseLayer", "GoogleMap");
			template.put("selectedBaseLayer", "OpenStreetMap");
			template.put("targetLayerConf", buildTargetLayerConf(levelName));
			
			template.put("controlPanelConf", buildControlPanelConf(dataSet));
			template.put("toolbarConf", buildToolbarConf(dataSet));
			
			//template.put("role", "spagobi/admin");
			Properties levelProps = GeoReportEngineConfig.getInstance().getLevelByName(levelName);
			String centralPoint = levelProps.getProperty("layer_cetral_point");
			template.put("lon", centralPoint.split(" ")[0]);
			template.put("lat", centralPoint.split(" ")[1]);
			template.put("zoomLevel", levelProps.getProperty("layer_zoom") );
		} catch (Throwable t) {
			throw new RuntimeException(
					"An unexpected error occured while executing building template: "+t.getMessage(),
					t);
		} finally {
			logger.debug("OUT");
		}
		
		return template;
	}
	
	
	private IFieldMetaData getGeoIdFiledMeta(IDataSet dataSet) {
		List<IFieldMetaData> geoFieldsMeta = new ArrayList<IFieldMetaData>();
		for(int i = 0; i < dataSet.getMetadata().getFieldCount(); i++) {
			IFieldMetaData fieldMeta = dataSet.getMetadata().getFieldMeta(i);
			if(fieldMeta.getProperties().containsKey("hierarchy")) {
				String level = (String)fieldMeta.getProperty("hierarchy_level");
				if(level == null) {
					logger.warn("Field [] referentiate dimension geo but not specify at which level (i.e. attribute [hierarchy_level]). It will be ignored");
				}
				geoFieldsMeta.add(fieldMeta);
			}
		}
		
		if(geoFieldsMeta.size() == 0) {
			throw new RuntimeException("The dataset [" + dataSet.getName() + "] does not contain any columns that point to the geographical dimension");
		}
		
		if(geoFieldsMeta.size() > 1) {
			logger.warn("There are morethen one columns that point the geographical dimension. Only the first one will be considered as georef");
		}
		
		return geoFieldsMeta.get(0);
	}
	
	private String getGeoId(String levelName) {		
		Properties levelProps = null;
		levelProps = GeoReportEngineConfig.getInstance().getLevelByName(levelName);
		if(levelProps == null) {
			throw new RuntimeException("Impossible to load from the layers catalogue properties of hierachy level [" + levelName + "]");
		}
		
		String layerID = levelProps.getProperty("layerId");
		if(layerID == null) {
			throw new RuntimeException("Compulsary propery [layerId] not found in between the defined properties of of hierachy level [" + levelName + "]");
		}
		
		return layerID;
	}
	
	
	/**
	 * @param dataSet
	 * @return
	 */
	private JSONObject buildToolbarConf(IDataSet dataSet) {
		JSONObject toolbarConf;
		
		logger.debug("IN");
		
		toolbarConf = new JSONObject();
		try {
			toolbarConf.put("enabled", true);
			toolbarConf.put("zoomToMaxButtonEnabled", true);
			
			toolbarConf.put("mouseButtonGroupEnabled", true);
			toolbarConf.put("measureButtonGroupEnabled", false);
			toolbarConf.put("wmsGroupEnabled", true);
			toolbarConf.put("drawButtonGroupEnabled", false);
			toolbarConf.put("historyButtonGroupEnabled", false);
		} catch (Throwable t) {
			throw new RuntimeException(
					"An unexpected error occured while building toolbar conf block",
					t);
		} finally {
			logger.debug("OUT");
		}
		
		return toolbarConf;
	}
	
	/**
	 * @param dataSet
	 * @return
	 */
	private JSONObject buildControlPanelConf(IDataSet dataSet) {
		JSONObject controlPanelConf;
		
		logger.debug("IN");
		
		controlPanelConf = new JSONObject();
		try {
			controlPanelConf.put("layerPanelEnabled", true);
			
			JSONObject layerPanelConf = new JSONObject();
			layerPanelConf.put("collapsed", true);
			controlPanelConf.put("layerPanelConf", layerPanelConf);
			controlPanelConf.put("analysisPanelEnabled", true);
			controlPanelConf.put("measurePanelEnabled", false);
			controlPanelConf.put("legendPanelEnabled", true);
			controlPanelConf.put("logoPanelEnabled", false);
			controlPanelConf.put("earthPanelEnabled", false);
		} catch (Throwable t) {
			throw new RuntimeException(
					"An unexpected error occured while building control panel conf block",
					t);
		} finally {
			logger.debug("OUT");
		}
		
		return controlPanelConf;
	}	

	/**
	 * @param dataSet
	 * @return
	 */
	private JSONObject buildTargetLayerConf(String levelName) {
		JSONObject targetLayerConf;
		
		logger.debug("IN");
		
		Properties levelProps = GeoReportEngineConfig.getInstance().getLevelByName(levelName);
		
		targetLayerConf = new JSONObject();
		try {
			targetLayerConf.put("text", levelProps.getProperty("layerLabel"));
			targetLayerConf.put("name", levelProps.getProperty("layerName"));
			String layerFile = levelProps.getProperty("layer_file");
			String layerUrl = levelProps.getProperty("layer_url");
			if(layerFile != null) {
				targetLayerConf.put("data", layerFile);
			} else {
				targetLayerConf.put("url", layerUrl);
			}
			
		} catch (Throwable t) {
			throw new RuntimeException(
					"An unexpected error occured while building target layer conf block",
					t);
		} finally {
			logger.debug("OUT");
		}
		
		return targetLayerConf;
	}


	/**
	 * @param dataSet
	 * @return
	 */
	private JSONArray buildIndicators(IDataSet dataSet) {
		JSONArray indicators;
		
		logger.debug("IN");
		
		indicators = new JSONArray();
		try {
			
			List<IFieldMetaData> fields;
			fields = new ArrayList<IFieldMetaData>();
			for(int i = 0; i < dataSet.getMetadata().getFieldCount(); i++) {
				IFieldMetaData fieldMeta = dataSet.getMetadata().getFieldMeta(i);
				if(fieldMeta.getFieldType() ==  FieldType.MEASURE) {
					fields.add(fieldMeta);
				}
			}
			
			JSONArray info;
			for(IFieldMetaData field : fields) {
				info = new JSONArray();
				info.put(field.getName());
				info.put( field.getAlias() != null? field.getAlias(): field.getName() );
				indicators.put(info);
			}
			
		} catch (Throwable t) {
			throw new RuntimeException(
					"An unexpected error occured while building indicators block",
					t);
		} finally {
			logger.debug("OUT");
		}
		
		return indicators;
	}

	/**
	 * @param dataSet
	 * @return
	 */
	private JSONArray buildFeatureInfo(IDataSet dataSet) {
		JSONArray featureInfo;
		
		logger.debug("IN");
				
		featureInfo = new JSONArray();
		try {
			List<IFieldMetaData> fields;
			fields = new ArrayList<IFieldMetaData>();
			for(int i = 0; i < dataSet.getMetadata().getFieldCount(); i++) {
				IFieldMetaData fieldMeta = dataSet.getMetadata().getFieldMeta(i);
				if(fieldMeta.getFieldType() ==  FieldType.ATTRIBUTE) {
					fields.add(fieldMeta);
				}
			}
			
			JSONArray info;
			for(IFieldMetaData field : fields) {
				info = new JSONArray();
			
				info.put( field.getAlias() != null? field.getAlias(): field.getName() );
				info.put(field.getName());
				featureInfo.put(info);
			}
		} catch (Throwable t) {
			throw new RuntimeException(
					"An unexpected error occured while building feature info block",
					t);
		} finally {
			logger.debug("OUT");
		}
		
		return featureInfo;
	}

	/**
	 * @param dataSet
	 * @return
	 */
	private JSONObject buildAnalysisConf(IDataSet dataSet) {
		JSONObject analysisConf;
		
		logger.debug("IN");
		
		analysisConf = new JSONObject();
		try {
			
			analysisConf.put("type", "choropleth");
			
			// select the first indicator...
			String firstIndicatorName = null;
			if(dataSet != null) {
				for(int i = 0; i < dataSet.getMetadata().getFieldCount(); i++) {
					IFieldMetaData fieldMeta = dataSet.getMetadata().getFieldMeta(i);
					if(fieldMeta.getFieldType() ==  FieldType.MEASURE) {
						firstIndicatorName = fieldMeta.getName();
						break;
					}
				}
				analysisConf.put("indicator", firstIndicatorName);
			}
						
			
			analysisConf.put("method", "CLASSIFY_BY_QUANTILS"); // "CLASSIFY_BY_EQUAL_INTERVALS" 
			analysisConf.put("classes", "7");
			analysisConf.put("fromColor", "#FFFF00");
			analysisConf.put("toColor", "#008000");
		} catch (Throwable t) {
			throw new RuntimeException(
					"An unexpected error occured while building analysis conf block",
					t);
		} finally {
			logger.debug("OUT");
		}
		
		return analysisConf;
	}
}
