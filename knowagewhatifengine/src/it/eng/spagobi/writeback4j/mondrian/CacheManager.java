/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.writeback4j.mondrian;

import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import mondrian.olap.CacheControl;
import mondrian.rolap.RolapConnection;

import org.apache.log4j.Logger;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;

public class CacheManager {

	public static transient Logger logger = Logger.getLogger(CacheManager.class);

	public static void flushCache(OlapDataSource olapDataSource) {
		logger.debug("IN");
		try {
			Assert.assertNotNull(olapDataSource, "No OLAP datasource found");
			OlapConnection connection = olapDataSource.getConnection();
			logger.debug("Got OlapConnection");
			RolapConnection rolapConnection = connection.unwrap(mondrian.rolap.RolapConnection.class);
			logger.debug("Got RolapConnection");
			CacheControl cacheControl = rolapConnection.getCacheControl(null);
			logger.debug("Got CacheControl");
			cacheControl.flushSchema(rolapConnection.getSchema());
			logger.debug("Cache flushed");
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("An error occurred while flushing cache", e);
		}
		logger.debug("OUT");

	}

}
