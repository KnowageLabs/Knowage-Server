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
package it.eng.spagobi.engines.georeport;

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

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoLayersDAO;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GeoReportEngineConfig {

	private static final Logger LOGGER = Logger.getLogger(GeoReportEngineConfig.class);

	private EnginConf engineConfig;

	private Map<String, List> includes;
	private Set<String> enabledIncludes;

	List<Properties> levels;

	// -- singleton pattern --------------------------------------------
	private static GeoReportEngineConfig instance;

	public static GeoReportEngineConfig getInstance() {
		if (instance == null) {
			instance = new GeoReportEngineConfig();
		}
		return instance;
	}

	private GeoReportEngineConfig() {
		setEngineConfig(EnginConf.getInstance());
	}

	// -- singleton pattern --------------------------------------------

	// -- CORE SETTINGS ACCESSOR Methods---------------------------------

	public List getIncludes() {
		List results;

		if (includes == null) {
			initIncludes();
		}

		results = new ArrayList();
		Iterator<String> it = enabledIncludes.iterator();
		while (it.hasNext()) {
			String includeName = it.next();
			List urls = includes.get(includeName);
			results.addAll(urls);
			LOGGER.debug("Added [" + urls.size() + "] for include [" + includeName + "]");
		}

		return results;
	}

	public List<Properties> getLevels() {
		// From SpagoBI5 the layers are always loaded because they are got from the catalogue (db spagobi)
		// and so, they can change in every moment!
		initGeoDimensionLevels();

		return levels;
	}

	public Properties getLevelByName(String name) {

		Properties levelProps = null;

		if (name == null)
			return null;

		// From SpagoBI5 the layers are always loaded because they are got from the catalogue (db spagobi)
		// and so, they can change in every moment!
		initGeoDimensionLevels();

		for (Properties props : levels) {
			if (name.equals(props.getProperty("name"))) {
				levelProps = props;
			}
		}

		return levelProps;
	}

	// -- PARSE Methods -------------------------------------------------

	private static final String INCLUDES_TAG = "INCLUDES";
	private static final String INCLUDE_TAG = "INCLUDE";
	private static final String URL_TAG = "URL";

	public void initIncludes() {
		SourceBean includesSB;
		List includeSBList;
		SourceBean includeSB;
		List urlSBList;
		SourceBean urlSB;

		includes = new HashMap();
		enabledIncludes = new LinkedHashSet();

		includesSB = (SourceBean) getConfigSourceBean().getAttribute(INCLUDES_TAG);
		if (includesSB == null) {
			LOGGER.debug("Tag [" + INCLUDES_TAG + "] not specifeid in [engine-config.xml] file");
			return;
		}

		includeSBList = includesSB.getAttributeAsList(INCLUDE_TAG);
		if (includeSBList == null || includeSBList.isEmpty()) {
			LOGGER.debug("Tag [" + INCLUDES_TAG + "] does not contains any [" + INCLUDE_TAG + "] tag");
			return;
		}

		for (int i = 0; i < includeSBList.size(); i++) {
			includeSB = (SourceBean) includeSBList.get(i);
			String name = (String) includeSB.getAttribute("name");
			String bydefault = (String) includeSB.getAttribute("default");

			LOGGER.debug("Include [" + name + "]: [" + bydefault + "]");

			List urls = new ArrayList();

			urlSBList = includeSB.getAttributeAsList(URL_TAG);
			for (int j = 0; j < urlSBList.size(); j++) {
				urlSB = (SourceBean) urlSBList.get(j);
				String url = urlSB.getCharacters();
				urls.add(url);
				LOGGER.debug("Url [" + name + "] added to include list");
			}

			includes.put(name, urls);
			if (bydefault.equalsIgnoreCase("enabled")) {
				enabledIncludes.add(name);
			}
		}
	}

	public void initGeoDimensionLevels() {

		LOGGER.debug("IN");

		try {
			List<GeoLayer> geoLayers = new ArrayList<>();

			try {
				ISbiGeoLayersDAO geoLayersDAO = DAOFactory.getSbiGeoLayerDao();
				geoLayers = geoLayersDAO.loadAllLayers(null, UserUtilities.getUserProfile());
				if (geoLayers != null) {
					levels = new ArrayList<>();
					for (int i = 0; i < geoLayers.size(); i++) {
						GeoLayer level = geoLayers.get(i);
						String name = level.getName();
						String layerLabel = "";
						String layerId = "";
						String layerName = "";
						String layerFile = "";
						String layerUrl = "";
						String layerZoom = "";
						String layerCentralPoint = "";
						String layerParams = "";
						String layerOptions = "";

						if (level.getLayerDef() != null) {
							JSONObject js = null;
							try {
								js = new JSONObject(new String(level.getLayerDef()));
							} catch (JSONException e) {
								LOGGER.error("Error serializing the definition of the layer" + level.getLabel(), e);
								throw new SpagoBIRuntimeException(
										"Error serializing the definition of the layer" + level.getLabel(), e);
							}
							if (js != null) {
								String[] properties = JSONObject.getNames(js);
								if (properties != null) {
									layerId = (String) js.get("propsId");
									layerLabel = (String) js.get("propsLabel");
									layerName = (String) js.get("propsName");
									layerFile = (String) js.get("propsFile");
									layerUrl = (String) js.get("propsUrl");
									layerZoom = (String) js.get("propsZoom");
									layerCentralPoint = (String) js.get("propsCentralPoint");
									layerParams = (String) js.get("propsParams");
									layerOptions = (String) js.get("propsOptions");
								}
							}
						}

						Properties props = new Properties();
						if (name != null && !"".equals(name))
							props.setProperty("name", name);
						if (layerName != null && !"".equals(layerName))
							props.setProperty("layerName", layerName);
						if (layerLabel != null && !"".equals(layerLabel))
							props.setProperty("layerLabel", layerLabel);
						if (layerId != null && !"".equals(layerId))
							props.setProperty("layerId", layerId);
						if (layerFile != null && !"".equals(layerFile))
							props.setProperty("layer_file", layerFile);
						if (layerUrl != null && !"".equals(layerUrl))
							props.setProperty("layer_url", layerUrl);
						if (layerZoom != null && !"".equals(layerZoom))
							props.setProperty("layer_zoom", layerZoom);
						if (layerCentralPoint != null && !"".equals(layerCentralPoint))
							props.setProperty("layer_cetral_point", layerCentralPoint);
						if (layerParams != null && !"".equals(layerParams))
							props.setProperty("layer_params", layerParams);
						if (layerOptions != null && !"".equals(layerOptions))
							props.setProperty("layer_options", layerOptions);

						levels.add(props);
					}
				}

			} catch (EMFUserError e) {
				LOGGER.error("Error getting layer properties", e);
				throw new SpagoBIRuntimeException("Error getting layer properties", e);
			}
		} catch (Throwable t) {
			throw new RuntimeException(
					"An error occured while loading geo dimension levels' properties from layers catalogue", t);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	// -- ACCESS Methods -----------------------------------------------
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
