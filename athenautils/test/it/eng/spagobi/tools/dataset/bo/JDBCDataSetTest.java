/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.bo;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;
import it.eng.spagobi.test.AbstractSpagoBITestCase;
import it.eng.spagobi.test.TestCaseConstants;
import it.eng.spagobi.test.TestDataSetFactory;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.datareader.JSONDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import junit.framework.TestCase;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JDBCDataSetTest extends AbstractSpagoBITestCase {

	JDBCDataSet dataset;
	
	public void setUp() throws Exception {
		super.setUp();
		try {
			SpagoBiDataSet dataSetConfig = new SpagoBiDataSet();
			String conf = "{\"Query\":\"SELECT fullname as 'Full Name' FROM CUSTOMER LIMIT 10 \",\"queryScript\":\"\",\"queryScriptLanguage\":\"\",\"dataSource\":\"FoodMart\"}";
			dataSetConfig.setConfiguration(conf);
			//dataSetConfig.setQuery("SELECT fullname as 'Full Name' FROM CUSTOMER LIMIT 10");
			dataSetConfig.setDataSource( TestDataSetFactory.createSpagoBiDataSource() );
			dataset = new JDBCDataSet();
		} catch(Exception t) {
			System.err.println("An unespected error occurred during setUp: ");
			t.printStackTrace();
			throw t;
		}
	}
	
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
