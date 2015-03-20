/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.georeport;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoLayersDAO;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GeoReportEngineConfig {
	
	private EnginConf engineConfig;
	
	private Map<String, List> includes;
	private Set<String> enabledIncludes;
	
	List<Properties> levels;
	
	private static transient Logger logger = Logger.getLogger(GeoReportEngineConfig.class);

	
	// -- singleton pattern --------------------------------------------
	private static GeoReportEngineConfig instance;
	
	public static GeoReportEngineConfig getInstance(){
		if(instance==null) {
			instance = new GeoReportEngineConfig();
		}
		return instance;
	}
	
	private GeoReportEngineConfig() {
		setEngineConfig( EnginConf.getInstance() );
	}
	// -- singleton pattern  --------------------------------------------
	
	
	// -- CORE SETTINGS ACCESSOR Methods---------------------------------
	
	public List getIncludes() {
		List results;
		
		//includes = null;
		if(includes == null) {
			initIncludes();
		}
		
		results = new ArrayList();
		Iterator<String> it = enabledIncludes.iterator();
		while(it.hasNext()) {
			String includeName = it.next();
			List urls = includes.get( includeName );
			results.addAll(urls);
			logger.debug("Added [" + urls.size() + "] for include [" + includeName + "]");
		}
		
		
		return results;
	}
	
	public List<Properties> getLevels() {
		// From SpagoBI5 the layers are always loaded because they are got from the catalogue (db spagobi) 
		// and so, they can change in every moment!
//		if(levels == null) {
			initGeoDimensionLevels();
//		}
		
		return levels;
	}
	
	public Properties getLevelByName(String name) {
		
		Properties levelProps = null;
		
		if(name == null) return null;
		
		// From SpagoBI5 the layers are always loaded because they are got from the catalogue (db spagobi) 
		// and so, they can change in every moment!
//		if(levels == null) {
			initGeoDimensionLevels(); 
//		}
		
		for(Properties props : levels) {
			if(name.equals(props.getProperty("name"))) {
				levelProps = props;
			}
		}
		
		return levelProps;
	}
	
	
	// -- PARSE Methods -------------------------------------------------
	
	private final static String INCLUDES_TAG = "INCLUDES";
	private final static String INCLUDE_TAG = "INCLUDE";
	private final static String URL_TAG = "URL";
	
	public void initIncludes() {
		SourceBean includesSB;
		List includeSBList;
		SourceBean includeSB;
		List urlSBList;
		SourceBean urlSB;
		
		includes = new HashMap();
		enabledIncludes = new LinkedHashSet();
		
		includesSB = (SourceBean) getConfigSourceBean().getAttribute(INCLUDES_TAG);
		if(includesSB == null) {
			logger.debug("Tag [" + INCLUDES_TAG + "] not specifeid in [engine-config.xml] file");
			return;
		}
		
		includeSBList = includesSB.getAttributeAsList(INCLUDE_TAG);
		if(includeSBList == null || includeSBList.size() == 0) {
			logger.debug("Tag [" + INCLUDES_TAG + "] does not contains any [" + INCLUDE_TAG + "] tag");
			return;
		}
		
		for(int i = 0; i < includeSBList.size(); i++) {
			includeSB = (SourceBean)includeSBList.get(i);
			String name = (String)includeSB.getAttribute("name");
			String bydefault = (String)includeSB.getAttribute("default");
			
			logger.debug("Include [" + name + "]: [" + bydefault + "]");
			
			List urls = new ArrayList();
			
			urlSBList = includeSB.getAttributeAsList(URL_TAG);
			for(int j = 0; j < urlSBList.size(); j++) {
				urlSB = (SourceBean)urlSBList.get(j);
				String url = urlSB.getCharacters();
				urls.add(url);
				logger.debug("Url [" + name + "] added to include list");
			}
			
			includes.put(name, urls);
			if(bydefault.equalsIgnoreCase("enabled")) {
				enabledIncludes.add(name);
			}
		}		
	}
	
	public void initGeoDimensionLevels() {
		
		logger.debug("IN");
		
		try {
			List<GeoLayer> geoLayers = new ArrayList<GeoLayer>();
			
			try {
				ISbiGeoLayersDAO geoLayersDAO = DAOFactory.getSbiGeoLayerDao();
				geoLayers = geoLayersDAO.loadAllLayers();
				if (geoLayers != null){
					levels = new ArrayList<Properties>();
					for (int i=0; i<geoLayers.size(); i++){
						GeoLayer level = (GeoLayer)geoLayers.get(i);
						String name = (String)level.getName();
						String layerLabel = "";
						String layerId = "" ;
						String layerName = "";						
						String layer_file = "";
						String layer_url = "";
						String layer_zoom = "";
						String layer_cetral_point = "";
						String layer_params = "";
						String layer_options = "";
								
						if(level.getLayerDef()!=null){
							JSONObject js = null;
							try {
								js = new JSONObject(new String(level.getLayerDef()));
							} catch (JSONException e) {
								logger.error("Error serializing the definition of the layer"+level.getLabel(),e);
								throw new SpagoBIRuntimeException("Error serializing the definition of the layer"+level.getLabel(),e);
							}
							if(js!=null){
								String[] properties = JSONObject.getNames(js);
								if(properties!=null){
									layerId  = (String)js.get("propsId");
									layerLabel = (String)js.get("propsLabel");
									layerName = (String)js.get("propsName");
									layer_file = (String)js.get("propsFile");
									layer_url = (String)js.get("propsUrl");
									layer_zoom = (String)js.get("propsZoom");
									layer_cetral_point = (String)js.get("propsCentralPoint");
									layer_params = (String)js.get("propsParams");		
									layer_options = (String)js.get("propsOptions");		
								}
							}
						}
						
						Properties props = new Properties();
						if(name != null && !"".equals(name)) props.setProperty("name", name);
						if(layerName != null &&  !"".equals(layerName)) props.setProperty("layerName", layerName);
						if(layerLabel != null &&  !"".equals(layerLabel)) props.setProperty("layerLabel", layerLabel);
						if(layerId != null &&  !"".equals(layerId)) props.setProperty("layerId", layerId);
						if(layer_file != null &&  !"".equals(layer_file)) props.setProperty("layer_file", layer_file);
						if(layer_url != null &&  !"".equals(layer_url)) props.setProperty("layer_url", layer_url);
						if(layer_zoom != null &&  !"".equals(layer_zoom)) props.setProperty("layer_zoom", layer_zoom);
						if(layer_cetral_point != null &&  !"".equals(layer_cetral_point)) props.setProperty("layer_cetral_point", layer_cetral_point);
						if(layer_params != null &&  !"".equals(layer_params)) props.setProperty("layer_params", layer_params);
						if(layer_options != null &&  !"".equals(layer_options)) props.setProperty("layer_options", layer_options);
						
						levels.add(props);
					}
				}

			} catch (EMFUserError e) {
				logger.error("Error getting layer properties",e);
				throw new SpagoBIRuntimeException("Error getting layer properties",e);
			}
		} catch(Throwable t) {
			throw new RuntimeException("An error occured while loading geo dimension levels' properties from layers catalogue", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	
	// -- ACCESS Methods  -----------------------------------------------
	public EnginConf getEngineConfig() {
		return engineConfig;
	}

	private void setEngineConfig(EnginConf engineConfig) {
		this.engineConfig = engineConfig;
	}
	
	public SourceBean getConfigSourceBean() {
		return getEngineConfig().getConfig();
	}
}
