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
package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.test.AbstractSpagoBITestCase;
import it.eng.spagobi.test.TestDataSetFactory;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JDBCDataSetTest extends AbstractSpagoBITestCase {

	JDBCDataSet dataset;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		try {
			SpagoBiDataSet dataSetConfig = new SpagoBiDataSet();
			String conf = "{\"Query\":\"SELECT fullname as 'Full Name' FROM CUSTOMER LIMIT 10 \",\"queryScript\":\"\",\"queryScriptLanguage\":\"\",\"dataSource\":\"FoodMart\"}";
			dataSetConfig.setConfiguration(conf);
			// dataSetConfig.setQuery("SELECT fullname as 'Full Name' FROM CUSTOMER LIMIT 10");
			dataSetConfig.setDataSource(TestDataSetFactory.createSpagoBiDataSource());
			dataset = new JDBCDataSet();
		} catch (Exception t) {
			logger.error("An unespected error occurred during setUp: ");
			t.printStackTrace();
			throw t;
		}
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testDataSetLoad() {
		dataset.loadData();
		IDataStore dataStore = dataset.getDataStore();
		assertNotNull(dataStore);
		assertEquals(10, dataStore.getRecordsCount());
		IMetaData metaData = dataStore.getMetaData();
		assertNotNull(metaData);
		assertEquals(1, metaData.getFieldCount());
		IFieldMetaData fieldMetaData = metaData.getFieldMeta(0);
		assertEquals("Full Name", fieldMetaData.getName());
		assertEquals(null, fieldMetaData.getAlias());
		assertEquals(String.class, fieldMetaData.getType());
	}

	public void testSimpleScriptedQuery() {
		String injectedStatement = "SELECT lname AS \\\'Last Name\\\' FROM CUSTOMER LIMIT 30";
		String script = "'" + injectedStatement + "';";
		dataset.setQueryScript(script);
		dataset.setQueryScriptLanguage("ECMAScript");

		dataset.loadData();
		IDataStore dataStore = dataset.getDataStore();
		assertNotNull(dataStore);
		assertEquals(30, dataStore.getRecordsCount());
		IMetaData metaData = dataStore.getMetaData();
		assertNotNull(metaData);
		assertEquals(1, metaData.getFieldCount());
		IFieldMetaData fieldMetaData = metaData.getFieldMeta(0);
		assertEquals("Last Name", fieldMetaData.getName());
		assertEquals(null, fieldMetaData.getAlias());
		assertEquals(String.class, fieldMetaData.getType());
	}

	public void testScriptedQueryTransform() {
		String script = "query.replace(\"LIMIT 10\",\"LIMIT 30\");";
		dataset.setQueryScript(script);
		dataset.setQueryScriptLanguage("ECMAScript");

		dataset.loadData();
		IDataStore dataStore = dataset.getDataStore();
		assertNotNull(dataStore);
		assertEquals(30, dataStore.getRecordsCount());
		IMetaData metaData = dataStore.getMetaData();
		assertNotNull(metaData);
		assertEquals(1, metaData.getFieldCount());
		IFieldMetaData fieldMetaData = metaData.getFieldMeta(0);
		assertEquals("Full Name", fieldMetaData.getName());
		assertEquals(null, fieldMetaData.getAlias());
		assertEquals(String.class, fieldMetaData.getType());
	}

	public void testScriptedQueryWithParameters() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("limit", "5");
		dataset.setParamsMap(parameters);

		String script = "query.replace(\"LIMIT 10\",\"LIMIT \" + parameters.get('limit'));";
		dataset.setQueryScript(script);
		dataset.setQueryScriptLanguage("ECMAScript");

		dataset.loadData();
		IDataStore dataStore = dataset.getDataStore();
		assertNotNull(dataStore);
		assertEquals(5, dataStore.getRecordsCount());
		IMetaData metaData = dataStore.getMetaData();
		assertNotNull(metaData);
		assertEquals(1, metaData.getFieldCount());
		IFieldMetaData fieldMetaData = metaData.getFieldMeta(0);
		assertEquals("Full Name", fieldMetaData.getName());
		assertEquals(null, fieldMetaData.getAlias());
		assertEquals(String.class, fieldMetaData.getType());
	}

	public void testScriptedQueryWithAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("limit", "5");
		dataset.setUserProfileAttributes(attributes);

		String script = "query.replace(\"LIMIT 10\",\"LIMIT \" + attributes.get('limit'));";
		dataset.setQueryScript(script);
		dataset.setQueryScriptLanguage("ECMAScript");

		dataset.loadData();
		IDataStore dataStore = dataset.getDataStore();
		assertNotNull(dataStore);
		assertEquals(5, dataStore.getRecordsCount());
		IMetaData metaData = dataStore.getMetaData();
		assertNotNull(metaData);
		assertEquals(1, metaData.getFieldCount());
		IFieldMetaData fieldMetaData = metaData.getFieldMeta(0);
		assertEquals("Full Name", fieldMetaData.getName());
		assertEquals(null, fieldMetaData.getAlias());
		assertEquals(String.class, fieldMetaData.getType());
	}

}
