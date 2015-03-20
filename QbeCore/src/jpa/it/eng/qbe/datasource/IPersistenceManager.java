/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource;

import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration;

import org.json.JSONObject;

public interface IPersistenceManager {
	
	void updateRecord(JSONObject aRecord,
			RegistryConfiguration registryConf);
	
	Integer insertRecord(JSONObject aRecord,
			RegistryConfiguration registryConf, boolean autoLoadPK, String tableForPkMax, String columnForPkMax);

	void deleteRecord(JSONObject aRecord,
			RegistryConfiguration registryConf);

	
	String getKeyColumn(JSONObject aRecord, RegistryConfiguration registryConf);
	
}
