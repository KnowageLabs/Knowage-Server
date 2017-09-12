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
