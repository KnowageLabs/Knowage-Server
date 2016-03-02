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
package it.eng.spagobi.engine.chart;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.engine.chart.model.conf.ChartConfig;
import it.eng.spagobi.services.common.EnginConf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author
 */
@SuppressWarnings("unchecked")
public class ChartEngineConfig {

	public static final String CACHE_NAME_PREFIX_CONFIG = "SPAGOBI.CACHE.NAMEPREFIX";
	public static final String CACHE_SPACE_AVAILABLE_CONFIG = "SPAGOBI.CACHE.SPACE_AVAILABLE";
	public static final String CACHE_LIMIT_FOR_CLEAN_CONFIG = "SPAGOBI.CACHE.LIMIT_FOR_CLEAN";

	private static Map<String, ChartConfig> chartLibConf = new HashMap<>();

	private static EnginConf engineConfig;

	private static transient Logger logger = Logger.getLogger(ChartEngineConfig.class);

	// -- singleton pattern --------------------------------------------
	private static ChartEngineConfig instance;

	public static ChartEngineConfig getInstance() {
		return instance;
	}

	static {
		logger.trace("IN");

		engineConfig = EnginConf.getInstance();

		instance = new ChartEngineConfig();

		SourceBean chartLibraries = (SourceBean) getConfigSourceBean().getAttribute("chartConfiguration");
		if (chartLibraries != null) {
			List<SourceBean> chartLibrariesItems = chartLibraries.getAttributeAsList("chart");
			for (SourceBean chart : chartLibrariesItems) {
				String type = (String) chart.getAttribute("type");
				String name = (String) chart.getAttribute("name");
				String vmPath = (String) chart.getAttribute("vmPath");
				String vmName = (String) chart.getAttribute("vmName");
				String libIniPath = (String) chart.getAttribute("libIniPath");
				String libIniName = (String) chart.getAttribute("libIniName");

				chartLibConf.put(type, new ChartConfig(type, name, vmPath, vmName, libIniPath, libIniName));
			}
		}
		logger.trace("OUT");
	}

	private ChartEngineConfig() {

	}

	// -- singleton pattern --------------------------------------------

	// -- ACCESSOR Methods -----------------------------------------------
	public static EnginConf getEngineConfig() {
		return engineConfig;
	}

	public static SourceBean getConfigSourceBean() {
		return getEngineConfig().getConfig();
	}

	public static Map<String, ChartConfig> getChartLibConf() {
		return chartLibConf;
	}
	
	public static String getEngineResourcePath(){
		 String path = null;
		  if(getEngineConfig().getResourcePath() != null) {
		   path = getEngineConfig().getResourcePath() + System.getProperty("file.separator") + "chart";
		  } else {
		   path = ConfigSingleton.getRootPath() + System.getProperty("file.separator") + "resources" + System.getProperty("file.separator") + "chart";
		  }
		  
		  return path;
	}
	}


