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
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCacheConfiguration;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.DataSource;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */
@RunWith(value = Parameterized.class)
public class GenericSQLDBCachePersistTableTest extends AbstractCacheTest {

	private static final String XML_TEST_FILE_SUFFIX = "CachePersistTest.xml";
	static private final String XML_FOLDER_PATH = "test/resources/dataset/files";
	static private Logger logger = Logger.getLogger(GenericSQLDBCachePersistTableTest.class);

	private String xmlFileAbsolutePath;

	public GenericSQLDBCachePersistTableTest(String xmlFileAbsolutePath) {
		super();
		this.xmlFileAbsolutePath = xmlFileAbsolutePath;
		try {
			setUp();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Parameters
	public static Collection data() {
		File xmlFolder = new File(XML_FOLDER_PATH);
		File[] xmlFiles = xmlFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(XML_TEST_FILE_SUFFIX.toLowerCase());
			}
		});
		Object[][] xmlFileAbsolutePaths = new Object[xmlFiles.length][1];
		for (int i = 0; i < xmlFiles.length; i++) {
			xmlFileAbsolutePaths[i][0] = xmlFiles[i].getAbsolutePath();
		}
		return Arrays.asList(xmlFileAbsolutePaths);
	}

	@Override
	public void createDataSources() {
		try {
			dataSourceReading = TestXmlFactory.createDataSource(xmlFileAbsolutePath, false);
			dataSourceWriting = TestXmlFactory.createDataSource(xmlFileAbsolutePath, true);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Override
	public JDBCDataSet createJDBCDataset() {
		try {
			String tableName = TestXmlFactory.getTableName(xmlFileAbsolutePath);
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
			DataSource dataSourceCache = TestXmlFactory.createDataSource(xmlFileAbsolutePath, true);
			return TestXmlFactory.createCacheConfiguration(xmlFileAbsolutePath, dataSourceCache);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
			return null;
		}
	}

	@Test
	public void testJDBCDataSetFieldTypes() {
		Map<String, String> writingTypes;
		try {
			writingTypes = TestXmlFactory.getWritingTypes(xmlFileAbsolutePath);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
			return;
		}

		sqlDataset.loadData();
		IDataStore dataStore = sqlDataset.getDataStore();
		cache = new CacheFactory().getCache(getDefaultCacheConfiguration());
		cache.deleteAll();
		cache.put(sqlDataset, dataStore);
		dataStore = cache.get(sqlDataset.getSignature());
		cache.deleteAll();
		// checkJDBCDataSetFieldTypes(dataStore, writingTypes);
	}

	private void checkJDBCDataSetFieldTypes(IDataStore dataStore, Map<String, String> types) {
		IMetaData metaData = dataStore.getMetaData();
		for (int i = 0; i < metaData.getFieldCount(); i++) {
			IFieldMetaData fieldMeta = metaData.getFieldMeta(i);
			String fieldMetaName = fieldMeta.getName();
			if (types.containsKey(fieldMetaName)) {
				String expectedType = types.get(fieldMetaName);
				String actualType = fieldMeta.getType().toString();
				assertTrue("Field [" + fieldMetaName + "] expected:[" + expectedType + "] but was:[" + actualType + "]", actualType.contains(expectedType));
			}
		}
	}
}
