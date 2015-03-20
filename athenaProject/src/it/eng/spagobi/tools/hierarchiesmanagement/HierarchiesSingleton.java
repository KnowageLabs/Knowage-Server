/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
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

		try {
			if (instance == null)
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
