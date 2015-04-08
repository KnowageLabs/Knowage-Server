/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engine.chart;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.services.common.EnginConf;

import org.apache.log4j.Logger;

/**
 * @author
 */
public class ChartEngineConfig {

	public static final String CACHE_NAME_PREFIX_CONFIG = "SPAGOBI.CACHE.NAMEPREFIX";
	public static final String CACHE_SPACE_AVAILABLE_CONFIG = "SPAGOBI.CACHE.SPACE_AVAILABLE";
	public static final String CACHE_LIMIT_FOR_CLEAN_CONFIG = "SPAGOBI.CACHE.LIMIT_FOR_CLEAN";

	private static EnginConf engineConfig;

	private static transient Logger logger = Logger.getLogger(ChartEngineConfig.class);

	// -- singleton pattern --------------------------------------------
	private static ChartEngineConfig instance;

	public static ChartEngineConfig getInstance() {
		if (instance == null) {
			instance = new ChartEngineConfig();
		}
		return instance;
	}

	private ChartEngineConfig() {
		setEngineConfig(EnginConf.getInstance());
	}

	// -- singleton pattern --------------------------------------------

	// -- ACCESSOR Methods -----------------------------------------------
	public static EnginConf getEngineConfig() {
		return engineConfig;
	}

	private void setEngineConfig(EnginConf engineConfig) {
		this.engineConfig = engineConfig;
	}

	public static SourceBean getConfigSourceBean() {
		return getEngineConfig().getConfig();
	}

}
