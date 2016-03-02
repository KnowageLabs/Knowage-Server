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
package it.eng.spagobi.dataset.cache.impl.sqldbcache.test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;

import objectexplorer.MemoryMeasurer;
import objectexplorer.ObjectGraphMeasurer;
import objectexplorer.ObjectGraphMeasurer.Footprint;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public abstract class AbstractSQLDBCacheMetadatTest extends AbstractCacheTest {
	
	static private Logger logger = Logger.getLogger(AbstractSQLDBCacheMetadatTest.class);
	
	
	//Test cases
	
	public void testGetRequiredMemory() {
		
		
		long start = 0;
		long elapsed = 0;
		IDataStore dataStore = null;
		
		try {
			start = System.currentTimeMillis();
			sqlDataset.loadData();
			dataStore = sqlDataset.getDataStore();
			elapsed = System.currentTimeMillis() - start;
			logger.debug("dataset loaded in: " + formatInterval(elapsed) + "(" + elapsed + ")");
			Footprint footprint = ObjectGraphMeasurer.measure(dataStore);
			logger.debug("dataset footprint: " + footprint); 
			logger.debug("dataset bytes: " + MemoryMeasurer.measureBytes(dataStore)); 
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		
		start = System.currentTimeMillis();
		BigDecimal requiredMemory = cache.getMetadata().getRequiredMemory(dataStore);
		elapsed = System.currentTimeMillis() - start;
		logger.debug("required memory calculated in: " + formatInterval(elapsed) + "(" + elapsed + ")");
	
		JSONObject gridDataFeed  = null;

		try {
			start = System.currentTimeMillis();
			Map<String, Object> properties = new HashMap<String, Object>();
			JSONArray fieldOptions = null;
			fieldOptions = new JSONArray("[{id: 1, options: {measureScaleFactor: 0.5}}]");
			properties.put(JSONDataWriter.PROPERTY_FIELD_OPTION, fieldOptions);
			JSONDataWriter dataSetWriter = new JSONDataWriter(properties);
			gridDataFeed = (JSONObject)dataSetWriter.write(dataStore);
			elapsed = System.currentTimeMillis() - start;
			logger.debug("serialization completed in: " + formatInterval(elapsed) + "(" + elapsed + ")");
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		
		try {
			if(gridDataFeed != null) {
				start = System.currentTimeMillis();
				gridDataFeed.toString();	
				elapsed = System.currentTimeMillis() - start;
				logger.debug("stringify completed in: " + formatInterval(elapsed) + "(" + elapsed + ")");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		assertNotNull("Cache correctly initialized", cache );
	}
	
	/*
	* ----------------------------------------------------
	* Initialization Methods
	* ----------------------------------------------------
	*/
	
	public JDBCDataSet createJDBCDataset(){
		//Create JDBCDataSet
		sqlDataset = new JDBCDataSet();
		sqlDataset.setQuery("select * from sales_fact_1998");
		sqlDataset.setQueryScript("");
		sqlDataset.setQueryScriptLanguage("");
		sqlDataset.setDataSource(dataSourceReading);
		sqlDataset.setLabel("test_jdbcDataset");
		return sqlDataset;
	}
	
	/*
	* ----------------------------------------------------
	* Utilities
	* ----------------------------------------------------
	*/
	
	private static String formatInterval(final long l)
    {
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
    }
}
