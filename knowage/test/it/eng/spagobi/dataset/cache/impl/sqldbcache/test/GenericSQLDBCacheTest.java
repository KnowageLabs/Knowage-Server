/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.dataset.cache.impl.sqldbcache.test;

import it.eng.spagobi.dataset.cache.test.TestXmlFactory;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCacheConfiguration;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */
public class GenericSQLDBCacheTest extends AbstractCacheTest {

	static private final String XML_FILE_PATH = "test/resources/dataset/files/GenericSQLDBCacheTest.xml";

	static private Logger logger = Logger.getLogger(GenericSQLDBCacheTest.class);

	@Override
	public void createDataSources() {
		try {
			dataSourceReading = TestXmlFactory.createDataSource(XML_FILE_PATH, false);
			dataSourceWriting = TestXmlFactory.createDataSource(XML_FILE_PATH, true);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Override
	public JDBCDataSet createJDBCDataset() {
		try {
			String tableName = TestXmlFactory.getTableName(XML_FILE_PATH);
			JDBCDataSet jdbcDataSet = super.createJDBCDataset();
			jdbcDataSet.setQuery("select * from " + tableName);
			return jdbcDataSet;
		} catch (Exception e) {
			e.printStackTrace();
			fail();
			return null;
		}
	}

	@Override
	protected SQLDBCacheConfiguration getDefaultCacheConfiguration() {
		try {
			return TestXmlFactory.createCacheConfiguration(XML_FILE_PATH, dataSourceWriting);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
			return null;
		}
	}

	public void testJDBCDataSetFieldTypes() {
		Map<String, String> writingTypes;
		try {
			writingTypes = TestXmlFactory.getWritingTypes(XML_FILE_PATH);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
			return;
		}

		sqlDataset.loadData();
		IDataStore dataStore = sqlDataset.getDataStore();
		cache.put(sqlDataset, dataStore);
		dataStore = cache.get(sqlDataset.getSignature());
		checkJDBCDataSetFieldTypes(dataStore, writingTypes);
	}

	private void checkJDBCDataSetFieldTypes(IDataStore dataStore, Map<String, String> types) {
		IMetaData metaData = dataStore.getMetaData();
		for (int i = 0; i < metaData.getFieldCount(); i++) {
			IFieldMetaData fieldMeta = metaData.getFieldMeta(i);
			String fieldMetaName = fieldMeta.getName();
			if (fieldMetaName.equalsIgnoreCase(PersistedTableManager.getRowCountColumnName())) {
				continue;
			}
			assertTrue("Unexpected field [" + fieldMetaName + "]", types.containsKey(fieldMetaName));

			String expectedType = types.get(fieldMetaName);
			String actualType = fieldMeta.getType().toString();
			assertTrue("Field [" + fieldMetaName + "] expected:[" + expectedType + "] but was:[" + actualType + "]", actualType.contains(expectedType));
		}
	}
}
