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

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

public class ChartEngine {

	private static boolean enabled;
	private static Date creationDate;
	private static ChartEngineConfig engineConfig;

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(ChartEngine.class);

	// init engine
	static {
		enabled = true;
		creationDate = new Date(System.currentTimeMillis());
		engineConfig = ChartEngineConfig.getInstance();
	}

	public static ChartEngineConfig getConfig() {
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
	public static ChartEngineInstance createInstance(String template, Map<?, ?> env) {
		ChartEngineInstance chartEngineInstance = null;
		logger.debug("IN");
		chartEngineInstance = new ChartEngineInstance(template, env);
		logger.debug("OUT");
		return chartEngineInstance;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean enabled) {
		ChartEngine.enabled = enabled;
	}

	public static Date getCreationDate() {
		return creationDate;
	}

	public static void setCreationDate(Date creationDate) {
		ChartEngine.creationDate = creationDate;
	}
}
