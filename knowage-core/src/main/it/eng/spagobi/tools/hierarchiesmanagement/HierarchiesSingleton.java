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
package it.eng.spagobi.tools.hierarchiesmanagement;

import org.apache.log4j.Logger;

/**
 * This class is a singleton that contains the Hierarchies object
 *
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class HierarchiesSingleton {

	public static Logger logger = Logger.getLogger(HierarchiesSingleton.class);
	private static Hierarchies instance;

	public synchronized static Hierarchies getInstance() {
		logger.debug("IN");

		try {// temporarly creates always the instance (for environment test)
		// if (instance == null)
			instance = new Hierarchies();
		} catch (Exception e) {
			logger.error("Impossible to create the Hierarchies object", e);
		}
		logger.debug("OUT");

		return instance;
	}

	public synchronized static void refreshHierarchies() {
		logger.debug("IN");

		logger.debug("refresh hierarchies");
		instance = new Hierarchies();
		logger.debug("OUT");

	}

}
