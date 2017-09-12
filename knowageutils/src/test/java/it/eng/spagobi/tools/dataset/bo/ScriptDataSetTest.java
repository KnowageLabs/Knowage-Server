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

import it.eng.spagobi.test.AbstractSpagoBITestCase;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ScriptDataSetTest extends AbstractSpagoBITestCase {
	ScriptDataSet dataset;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		try {
			dataset = new ScriptDataSet();
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

	public void testLoadWithSimpleScript() {
		dataset.setScriptLanguage("ECMAScript");
		dataset.setScript("'<ROWS>" + "<ROW citta=\"Milano\" regione=\"Lombardia\"/>" + "<ROW citta=\"Padova\" regione=\"Veneto\"/>" + "</ROWS>'");
		dataset.loadData();
		IDataStore dataStore = dataset.getDataStore();
		assertNotNull(dataStore);
		assertEquals(2, dataStore.getRecordsCount());
		IMetaData metaData = dataStore.getMetaData();
		assertNotNull(metaData);
		assertEquals(2, metaData.getFieldCount());
		IFieldMetaData field1MetaData = metaData.getFieldMeta(0);
		assertNotNull(field1MetaData);
		assertEquals("citta", field1MetaData.getName());
		IFieldMetaData field2MetaData = metaData.getFieldMeta(1);
		assertNotNull(field2MetaData);
		assertEquals("regione", field2MetaData.getName());
		Iterator<IRecord> records = dataStore.iterator();
		while (records.hasNext()) {
			IRecord record = records.next();
			assertEquals(2, record.getFields().size());
		}

		List<IRecord> rcds = dataStore.findRecords(0, "Milano");
		assertNotNull(rcds);
		assertEquals(1, rcds.size());
		IRecord record = rcds.get(0);
		IField field = record.getFieldAt(1);
		assertEquals("Lombardia", field.getValue());

		rcds = dataStore.findRecords(0, "Padova");
		assertNotNull(rcds);
		assertEquals(1, rcds.size());
		record = rcds.get(0);
		field = record.getFieldAt(1);
		assertEquals("Veneto", field.getValue());
	}

	public void testLoadWithNonReturningXMLScript() {
		dataset.setScriptLanguage("ECMAScript");
		dataset.setScript("'Milano'");
		dataset.loadData();
		IDataStore dataStore = dataset.getDataStore();
		assertNotNull(dataStore);
		assertEquals(1, dataStore.getRecordsCount());
		IMetaData metaData = dataStore.getMetaData();
		assertNotNull(metaData);
		assertEquals(1, metaData.getFieldCount());
		IFieldMetaData field1MetaData = metaData.getFieldMeta(0);
		assertNotNull(field1MetaData);
		assertEquals("value", field1MetaData.getName());

		Iterator<IRecord> records = dataStore.iterator();
		while (records.hasNext()) {
			IRecord record = records.next();
			assertEquals(1, record.getFields().size());
		}

		List<IRecord> rcds = dataStore.findRecords(0, "Milano");
		assertNotNull(rcds);
		assertEquals(1, rcds.size());
		IRecord record = rcds.get(0);
		IField field = record.getFieldAt(0);
		assertEquals("Milano", field.getValue());
	}

	public void testLoadWithScriptThatCallAnImportedFunction() {
		dataset.setScriptLanguage("ECMAScript");
		dataset.setScript("returnValue('Milano')");
		dataset.loadData();
		IDataStore dataStore = dataset.getDataStore();
		assertNotNull(dataStore);
		assertEquals(1, dataStore.getRecordsCount());
		IMetaData metaData = dataStore.getMetaData();
		assertNotNull(metaData);
		assertEquals(1, metaData.getFieldCount());
		IFieldMetaData field1MetaData = metaData.getFieldMeta(0);
		assertNotNull(field1MetaData);
		assertEquals("value", field1MetaData.getName());
		List<IRecord> rcds = dataStore.findRecords(0, "Milano");
		assertNotNull(rcds);
		assertEquals(1, rcds.size());
		IRecord record = rcds.get(0);
		IField field = record.getFieldAt(0);
		assertEquals("Milano", field.getValue());
	}

	public void testLoadWithProfiledScript() {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("citta", "Milano");
		dataset.setUserProfileAttributes(attributes);
		dataset.setScriptLanguage("ECMAScript");
		dataset.setScript("returnValue('${citta}')");
		dataset.loadData();
		IDataStore dataStore = dataset.getDataStore();
		assertNotNull(dataStore);
		assertEquals(1, dataStore.getRecordsCount());
		IMetaData metaData = dataStore.getMetaData();
		assertNotNull(metaData);
		assertEquals(1, metaData.getFieldCount());
		IFieldMetaData field1MetaData = metaData.getFieldMeta(0);
		assertNotNull(field1MetaData);
		assertEquals("value", field1MetaData.getName());
		List<IRecord> rcds = dataStore.findRecords(0, "Milano");
		assertNotNull(rcds);
		assertEquals(1, rcds.size());
		IRecord record = rcds.get(0);
		IField field = record.getFieldAt(0);
		assertEquals("Milano", field.getValue());
	}

	public void testLoadWithParametricScript() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("citta", "Milano");
		dataset.setParamsMap(parameters);
		dataset.setScriptLanguage("ECMAScript");
		dataset.setScript("returnValue('$P{citta}')");
		dataset.loadData();
		IDataStore dataStore = dataset.getDataStore();
		assertNotNull(dataStore);
		assertEquals(1, dataStore.getRecordsCount());
		IMetaData metaData = dataStore.getMetaData();
		assertNotNull(metaData);
		assertEquals(1, metaData.getFieldCount());
		IFieldMetaData field1MetaData = metaData.getFieldMeta(0);
		assertNotNull(field1MetaData);
		assertEquals("value", field1MetaData.getName());
		List<IRecord> rcds = dataStore.findRecords(0, "Milano");
		assertNotNull(rcds);
		assertEquals(1, rcds.size());
		IRecord record = rcds.get(0);
		IField field = record.getFieldAt(0);
		assertEquals("Milano", field.getValue());
	}

	public void testLoadWithScriptThatUsesBindings() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("citta", "Milano");
		dataset.setParamsMap(parameters);
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("regione", "Lombardia");
		dataset.setUserProfileAttributes(attributes);

		dataset.setScriptLanguage("ECMAScript");
		dataset.setScript("'<ROWS>" + "<ROW citta=\"' + parameters.get('citta') + '\" " + "regione=\"' + attributes.get('regione') + '\"/>"
				+ "<ROW citta=\"Padova\" regione=\"Veneto\"/>" + "</ROWS>'");
		dataset.loadData();
		IDataStore dataStore = dataset.getDataStore();
		assertNotNull(dataStore);
		assertEquals(2, dataStore.getRecordsCount());
		IMetaData metaData = dataStore.getMetaData();
		assertNotNull(metaData);
		assertEquals(2, metaData.getFieldCount());
		IFieldMetaData field1MetaData = metaData.getFieldMeta(0);
		assertNotNull(field1MetaData);
		assertEquals("citta", field1MetaData.getName());
		IFieldMetaData field2MetaData = metaData.getFieldMeta(1);
		assertNotNull(field2MetaData);
		assertEquals("regione", field2MetaData.getName());
		Iterator<IRecord> records = dataStore.iterator();
		while (records.hasNext()) {
			IRecord record = records.next();
			assertEquals(2, record.getFields().size());
		}

		List<IRecord> rcds = dataStore.findRecords(0, "Milano");
		assertNotNull(rcds);
		assertEquals(1, rcds.size());
		IRecord record = rcds.get(0);
		IField field = record.getFieldAt(1);
		assertEquals("Lombardia", field.getValue());

		rcds = dataStore.findRecords(0, "Padova");
		assertNotNull(rcds);
		assertEquals(1, rcds.size());
		record = rcds.get(0);
		field = record.getFieldAt(1);
		assertEquals("Veneto", field.getValue());
	}
}
