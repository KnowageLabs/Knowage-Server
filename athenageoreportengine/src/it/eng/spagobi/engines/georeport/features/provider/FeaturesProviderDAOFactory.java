/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.georeport.features.provider;

import it.eng.spagobi.engines.georeport.GeoReportEngine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @authors Andrea Gioia (andrea.gioia@eng.it), Fabio D'Ovidio (f.dovidio@inovaos.it)
 */
public class FeaturesProviderDAOFactory {
	
	private static Map<String,IFeaturesProviderDAO> mappings;

	static {
		mappings = new HashMap();
		mappings.put("wfs", new FeaturesProviderDAOWFSImpl());
		File resourcesFolder = new File(GeoReportEngine.getConfig().getEngineConfig().getResourcePath() + "/georeport");
		mappings.put("file", new FeaturesProviderDAOFileImpl(resourcesFolder));
	}
	
	public static void initMappings() {
		mappings = new HashMap();
		mappings.put("wfs", new FeaturesProviderDAOWFSImpl());
		File resourcesFolder = new File(GeoReportEngine.getConfig().getEngineConfig().getResourcePath() + "/georeport");
		mappings.put("file", new FeaturesProviderDAOFileImpl(resourcesFolder));
	}
	
	public static IFeaturesProviderDAO getFeaturesProviderDAO(String featureSourceType) {
		//initMappings();
		return mappings.get(featureSourceType);
	}
}
