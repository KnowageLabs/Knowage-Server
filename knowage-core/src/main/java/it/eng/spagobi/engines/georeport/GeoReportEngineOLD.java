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

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.crossnavigation.dao.ICrossNavigationDAO;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GeoReportEngineOLD {

	private static GeoReportEngineConfig engineConfig;

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(GeoReportEngineOLD.class);

	// init engine
	static {
		engineConfig = GeoReportEngineConfig.getInstance();
	}

	public static GeoReportEngineConfig getConfig() {
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
	 * @throws EMFUserError
	 */
	public static GeoReportEngineInstance createInstance(String template, Map env) throws EMFUserError {
		GeoReportEngineInstance georeportEngineInstance = null;
		logger.debug("IN");
		// TO-DO ceck if document is crossable
		ICrossNavigationDAO crossDao = DAOFactory.getCrossNavigationDAO();
		// crossDao.setUserProfile(profile);
		if (crossDao.documentIsCrossable("")) {
			// velocityContext.internalPut("crossNavigation", true);
		} else {
			// velocityContext.internalPut("crossNavigation", "");
		}
		georeportEngineInstance = new GeoReportEngineInstance(template, env);
		logger.debug("OUT");
		return georeportEngineInstance;
	}
}
