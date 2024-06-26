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
package it.eng.knowage.engines.svgviewer;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.services.common.EnginConf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 */
public class SvgViewerEngineConfig {

	private EnginConf engineConfig;
	private Map<String, Map<String, String>> windowsGuiPropertiesInEmbeddedMode;

	private Map<String, List> includes;
	private Set<String> enabledIncludes;

	List<Properties> levels;

	private static final Logger logger = Logger.getLogger(SvgViewerEngineConfig.class);

	// -- singleton pattern --------------------------------------------
	private static SvgViewerEngineConfig instance;

	public static SvgViewerEngineConfig getInstance() {
		if (instance == null) {
			instance = new SvgViewerEngineConfig();
		}
		return instance;
	}

	private SvgViewerEngineConfig() {
		setEngineConfig(EnginConf.getInstance());
	}

	// -- singleton pattern --------------------------------------------

	// -- CORE SETTINGS ACCESSOR Methods---------------------------------
	/*
	 * 
	 * public List getIncludes() { List results;
	 * 
	 * // includes = null; if (includes == null) { initIncludes(); }
	 * 
	 * results = new ArrayList(); Iterator<String> it = enabledIncludes.iterator(); while (it.hasNext()) { String includeName = it.next(); List urls =
	 * includes.get(includeName); results.addAll(urls); logger.debug("Added [" + urls.size() + "] for include [" + includeName + "]"); }
	 * 
	 * return results; }
	 * 
	 * public List<Properties> getLevels() { // From SpagoBI5 the layers are always loaded because they are got from the catalogue (db spagobi) // and so, they
	 * can change in every moment! // if(levels == null) { initGeoDimensionLevels(); // }
	 * 
	 * return levels; }
	 * 
	 * public Properties getLevelByName(String name) {
	 * 
	 * Properties levelProps = null;
	 * 
	 * if (name == null) return null;
	 * 
	 * // From SpagoBI5 the layers are always loaded because they are got from the catalogue (db spagobi) // and so, they can change in every moment! //
	 * if(levels == null) { initGeoDimensionLevels(); // }
	 * 
	 * for (Properties props : levels) { if (name.equals(props.getProperty("name"))) { levelProps = props; } }
	 * 
	 * return levelProps; }
	 */
	// -- PARSE Methods -------------------------------------------------

	/*
	 * 
	 * private static final String INCLUDES_TAG = "INCLUDES"; private static final String INCLUDE_TAG = "INCLUDE"; private static final String URL_TAG = "URL";
	 * 
	 * public void initIncludes() { SourceBean includesSB; List includeSBList; SourceBean includeSB; List urlSBList; SourceBean urlSB;
	 * 
	 * includes = new HashMap(); enabledIncludes = new LinkedHashSet();
	 * 
	 * includesSB = (SourceBean) getConfigSourceBean().getAttribute(INCLUDES_TAG); if (includesSB == null) { logger.debug("Tag [" + INCLUDES_TAG +
	 * "] not specifeid in [engine-config.xml] file"); return; }
	 * 
	 * includeSBList = includesSB.getAttributeAsList(INCLUDE_TAG); if (includeSBList == null || includeSBList.size() == 0) { logger.debug("Tag [" + INCLUDES_TAG
	 * + "] does not contains any [" + INCLUDE_TAG + "] tag"); return; }
	 * 
	 * for (int i = 0; i < includeSBList.size(); i++) { includeSB = (SourceBean) includeSBList.get(i); String name = (String) includeSB.getAttribute("name");
	 * String bydefault = (String) includeSB.getAttribute("default");
	 * 
	 * logger.debug("Include [" + name + "]: [" + bydefault + "]");
	 * 
	 * List urls = new ArrayList();
	 * 
	 * urlSBList = includeSB.getAttributeAsList(URL_TAG); for (int j = 0; j < urlSBList.size(); j++) { urlSB = (SourceBean) urlSBList.get(j); String url =
	 * urlSB.getCharacters(); urls.add(url); logger.debug("Url [" + name + "] added to include list"); }
	 * 
	 * includes.put(name, urls); if (bydefault.equalsIgnoreCase("enabled")) { enabledIncludes.add(name); } } }
	 * 
	 * public void initGeoDimensionLevels() {
	 * 
	 * logger.debug("IN");
	 * 
	 * try { List<GeoLayer> geoLayers = new ArrayList<GeoLayer>();
	 * 
	 * try { ISbiGeoLayersDAO geoLayersDAO = DAOFactory.getSbiGeoLayerDao(); geoLayers = geoLayersDAO.loadAllLayers(null, UserUtilities.getUserProfile()); if
	 * (geoLayers != null) { levels = new ArrayList<Properties>(); for (int i = 0; i < geoLayers.size(); i++) { GeoLayer level = geoLayers.get(i); String name =
	 * level.getName(); String layerLabel = ""; String layerId = ""; String layerName = ""; String layer_file = ""; String layer_url = ""; String layer_zoom =
	 * ""; String layer_cetral_point = ""; String layer_params = ""; String layer_options = "";
	 * 
	 * if (level.getLayerDef() != null) { JSONObject js = null; try { js = new JSONObject(new String(level.getLayerDef())); } catch (JSONException e) {
	 * logger.error("Error serializing the definition of the layer" + level.getLabel(), e); throw new
	 * SpagoBIRuntimeException("Error serializing the definition of the layer" + level.getLabel(), e); } if (js != null) { String[] properties =
	 * JSONObject.getNames(js); if (properties != null) { layerId = (String) js.get("propsId"); layerLabel = (String) js.get("propsLabel"); layerName = (String)
	 * js.get("propsName"); layer_file = (String) js.get("propsFile"); layer_url = (String) js.get("propsUrl"); layer_zoom = (String) js.get("propsZoom");
	 * layer_cetral_point = (String) js.get("propsCentralPoint"); layer_params = (String) js.get("propsParams"); layer_options = (String)
	 * js.get("propsOptions"); } } }
	 * 
	 * Properties props = new Properties(); if (name != null && !"".equals(name)) props.setProperty("name", name); if (layerName != null &&
	 * !"".equals(layerName)) props.setProperty("layerName", layerName); if (layerLabel != null && !"".equals(layerLabel)) props.setProperty("layerLabel",
	 * layerLabel); if (layerId != null && !"".equals(layerId)) props.setProperty("layerId", layerId); if (layer_file != null && !"".equals(layer_file))
	 * props.setProperty("layer_file", layer_file); if (layer_url != null && !"".equals(layer_url)) props.setProperty("layer_url", layer_url); if (layer_zoom !=
	 * null && !"".equals(layer_zoom)) props.setProperty("layer_zoom", layer_zoom); if (layer_cetral_point != null && !"".equals(layer_cetral_point))
	 * props.setProperty("layer_cetral_point", layer_cetral_point); if (layer_params != null && !"".equals(layer_params)) props.setProperty("layer_params",
	 * layer_params); if (layer_options != null && !"".equals(layer_options)) props.setProperty("layer_options", layer_options);
	 * 
	 * levels.add(props); } }
	 * 
	 * } catch (EMFUserError e) { logger.error("Error getting layer properties", e); throw new SpagoBIRuntimeException("Error getting layer properties", e); } }
	 * catch (Throwable t) { throw new RuntimeException("An error occured while loading geo dimension levels' properties from layers catalogue", t); } finally {
	 * logger.debug("OUT"); } }
	 */
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

	private Map<String, Map<String, String>> getWindowsGuiPropertiesInEmbeddedMode() {
		if (windowsGuiPropertiesInEmbeddedMode == null) {
			windowsGuiPropertiesInEmbeddedMode = new HashMap<String, Map<String, String>>();
			List<SourceBean> windowsConfiguration = getConfigSourceBean().getAttributeAsList("EMBEDDED_MODE.WINDOW");

			// parse properties
			for (SourceBean windowConfiguration : windowsConfiguration) {
				String name = (String) windowConfiguration.getAttribute("name");
				Map<String, String> propertyMap = new HashMap<String, String>();
				windowsGuiPropertiesInEmbeddedMode.put(name, propertyMap);
				List<SourceBean> guiProperties = windowConfiguration.getAttributeAsList("PARAM");
				for (SourceBean guiProperty : guiProperties) {
					String pName = (String) guiProperty.getAttribute("name");
					String pValue = guiProperty.getCharacters();
					if (pName != null && pValue != null)
						propertyMap.put(pName, pValue);
				}
			}
		}
		return windowsGuiPropertiesInEmbeddedMode;
	}

	public Map<String, String> getWindowGuiPropertiesInEmbeddedMode(String windowName) {
		return getWindowsGuiPropertiesInEmbeddedMode().get(windowName);
	}

	public boolean isWindowVisibleInEmbeddedMode(String windowName, boolean defaultValue) {
		boolean isVisible = defaultValue;
		Map<String, String> propertyMap = getWindowGuiPropertiesInEmbeddedMode(windowName);
		if (propertyMap != null && propertyMap.get("visible") != null) {
			String visible = propertyMap.get("visible");
			isVisible = visible.equalsIgnoreCase("true");
		}

		return isVisible;
	}
}
