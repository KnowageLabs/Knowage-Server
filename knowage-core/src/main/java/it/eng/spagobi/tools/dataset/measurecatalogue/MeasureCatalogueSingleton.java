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

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.event.DataSetEventManager;

/**
 *
 * This class is a singleton that contains the MeasureCatalogue
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class MeasureCatalogueSingleton {

	private static MeasureCatalogue measureCatologue;

	private static final Logger LOGGER = Logger.getLogger(MeasureCatalogueSingleton.class);

	public static synchronized MeasureCatalogue getMeasureCatologue() {
		LOGGER.debug("IN");

		if (measureCatologue == null) {
			LOGGER.debug("The measure catalogue is not defined yet. Creating it..");
			measureCatologue = new MeasureCatalogue();
			LOGGER.debug("Measure catalogue created");

			DataSetEventManager.getINSTANCE().addEventListener(measureCatologue);

		}

		LOGGER.debug("OUT");
		return measureCatologue;
	}

	public static synchronized void refreshCatologue() {
		LOGGER.debug("IN");

		LOGGER.debug("refresh catalogue");
		measureCatologue = new MeasureCatalogue();
	}

	private MeasureCatalogueSingleton() {

	}

}
