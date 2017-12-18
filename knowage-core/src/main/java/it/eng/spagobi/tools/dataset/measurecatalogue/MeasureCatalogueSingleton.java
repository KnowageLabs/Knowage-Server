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

package it.eng.spagobi.tools.dataset.measurecatalogue;

import it.eng.spagobi.tools.dataset.event.DataSetEventManager;

import org.apache.log4j.Logger;

/**
 *
 * This class is a singleton that contains the MeasureCatalogue
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class MeasureCatalogueSingleton {
	private static MeasureCatalogue measureCatologue;

	public static Logger logger = Logger.getLogger(MeasureCatalogueSingleton.class);

	public synchronized static MeasureCatalogue getMeasureCatologue() {
		logger.debug("IN");

		if (measureCatologue == null) {
			logger.debug("The measure catalogue is not defined yet. Creating it..");
			measureCatologue = new MeasureCatalogue();
			logger.debug("Measure catalogue created");

			DataSetEventManager.getInstance().addEventListener(measureCatologue);

		}

		logger.debug("OUT");
		return measureCatologue;
	}

	public synchronized static void refreshCatologue() {
		logger.debug("IN");

		logger.debug("refresh catalogue");
		measureCatologue = new MeasureCatalogue();
	}

}
