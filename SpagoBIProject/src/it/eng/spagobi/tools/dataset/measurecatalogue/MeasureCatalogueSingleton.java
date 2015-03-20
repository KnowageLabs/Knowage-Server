 /* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.measurecatalogue;

import it.eng.spagobi.tools.dataset.event.DataSetEventManager;
import it.eng.spagobi.tools.dataset.service.ManageDatasets;

import org.apache.log4j.Logger;

/**
 * 
 * This class is a singleton that contains the MeasureCatalogue
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class MeasureCatalogueSingleton {
	private static MeasureCatalogue measureCatologue;
	
	public static Logger logger = Logger.getLogger(ManageDatasets.class);
	
	public synchronized static MeasureCatalogue getMeasureCatologue() {
		logger.debug("IN");

		if (measureCatologue == null){
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
