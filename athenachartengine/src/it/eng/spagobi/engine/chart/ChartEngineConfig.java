/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engine.chart;

import it.eng.spago.base.SourceBean;
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

}
