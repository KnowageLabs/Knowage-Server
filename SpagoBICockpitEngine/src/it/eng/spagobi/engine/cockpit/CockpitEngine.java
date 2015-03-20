/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engine.cockpit;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class CockpitEngine {

	private static boolean enabled;
	private static Date creationDate;
	private static CockpitEngineConfig engineConfig;

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(CockpitEngine.class);

	// init engine
	static {
		enabled = true;
		creationDate = new Date(System.currentTimeMillis());
		engineConfig = CockpitEngineConfig.getInstance();
	}

	public static CockpitEngineConfig getConfig() {
		return engineConfig;
	}

	/**
	 * Creates the instance.
	 * 
	 * @param template
	 *            the template
	 * @param env
	 *            the env
	 * 
	 * @return the geo report engine instance
	 */
	public static CockpitEngineInstance createInstance(String template, Map env) {
		CockpitEngineInstance cockpitEngineInstance = null;
		logger.debug("IN");
		cockpitEngineInstance = new CockpitEngineInstance(template, env);
		logger.debug("OUT");
		return cockpitEngineInstance;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean enabled) {
		CockpitEngine.enabled = enabled;
	}

	public static Date getCreationDate() {
		return creationDate;
	}

	public static void setCreationDate(Date creationDate) {
		CockpitEngine.creationDate = creationDate;
	}
}
