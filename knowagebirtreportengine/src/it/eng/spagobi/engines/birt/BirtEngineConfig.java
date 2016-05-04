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

package it.eng.spagobi.engines.birt;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 **/

public class BirtEngineConfig {

	private static EnginConf engineConfig;

	private static transient Logger logger = Logger.getLogger(BirtEngineConfig.class);

	// -- singleton pattern --------------------------------------------
	private static BirtEngineConfig instance;

	public static BirtEngineConfig getInstance() {
		if (instance == null) {
			instance = new BirtEngineConfig();
		}
		return instance;
	}

	private BirtEngineConfig() {
	}

	static {
		logger.debug("IN");
		engineConfig = EnginConf.getInstance();
		instance = new BirtEngineConfig();
		logger.debug("OUT");
	}

	// -- singleton pattern --------------------------------------------

	// -- ACCESSOR Methods -----------------------------------------------
	public static EnginConf getEngineConfig() {
		return engineConfig;
	}

	public static SourceBean getConfigSourceBean() {
		return getEngineConfig().getConfig();
	}

	public static String getEngineResourcePath() {
		String path = null;
		if (getEngineConfig().getResourcePath() != null) {
			path = getEngineConfig().getResourcePath() + File.separatorChar + "birt_messages";
		} else {
			throw new SpagoBIRuntimeException("Impossible to get the resource path for the engine");
		}
		return path;
	}
}
