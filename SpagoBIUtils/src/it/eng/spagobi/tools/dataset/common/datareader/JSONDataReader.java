/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.datareader;

import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JSONDataReader extends AbstractDataReader {

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( "dd/MM/yyyy" );
	private static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
	
	private static transient Logger logger = Logger.getLogger(JSONDataReader.class);

	public JSONDataReader() {
		super();        
	}

	public IDataStore read( Object data ) {
		DataStore dataStore;
		MetaData dataStoreMeta;
		JSONObject parsedData;

		logger.debug("IN");
		
		dataStore = null;
		parsedData = null;

		dataStore = new DataStore();
		dataStoreMeta = new MetaData();
		dataStore.setMetaData(dataStoreMeta);


		try {
			if (!(data instanceof JSONObject)) {
				parsedData = new JSONObject( (String)data );
			}
			else{
				parsedData = (JSONObject)data;
			}
			JSONObject parsedMeta = parsedData.optJSONObject("metaData");
			
			
			// init meta 
			if(parsedMeta == null) {
				throw new RuntimeException("Malformed data. Impossible to find attribute [" + "metaData" + "]");
			}
			String root = parsedMeta.optString("root");
			if(root == null) {
				throw new RuntimeException("Malformed meta data. Impossible to find attribute [" + "root" + "]");
			}
			JSONArray parsedFieldsMeta = parsedMeta.optJSONArray("fields");
			if(parsedFieldsMeta == null) {
				throw new RuntimeException("Malformed meta data. Impossible to find attribute [" + "fields" + "]");
			}
			for(int i = 1; i < parsedFieldsMeta.length(); i++) { // skip "recNo"
				JSONObject parsedFieldMeta = parsedFieldsMeta.getJSONObject(i);
				String columnName = parsedFieldMeta.getString("dataIndex");
			    String columnHeader = parsedFieldMeta.getString("header");
			    String columnType = parsedFieldMeta.getString("type");
			    
			    FieldMetadata fieldMeta = new FieldMetadata();
				fieldMeta.setName( columnName );
				fieldMeta.setAlias(columnHeader);
				if(columnType.equalsIgnoreCase("string")) {
					fieldMeta.setType( String.class );
				}else if(columnType.equalsIgnoreCase("int")) {
					fieldMeta.setType( BigInteger.class );
				} else if(columnType.equalsIgnoreCase("float")) {
					fieldMeta.setType( Double.class );
				} else if(columnType.equalsIgnoreCase("date")) {
					String subtype = parsedFieldMeta.optString("subtype");
					if(subtype != null && subtype.equalsIgnoreCase("timestamp")) {
						fieldMeta.setType( Timestamp.class );
					} else {
						fieldMeta.setType( Date.class );
					}
				} else if(columnType.equalsIgnoreCase("boolean")) {
					fieldMeta.setType( Boolean.class );
				} else {
					throw new RuntimeException("Impossible to resolve column type [" + columnType + "]");
				}
				
				dataStoreMeta.addFiedMeta(fieldMeta);
			}
			
			
			// init results 
			JSONArray parsedRows = parsedData.optJSONArray(root);
			if(parsedRows == null) {
				throw new RuntimeException("Malformed data. Impossible to find attribute [" + root + "]");
			}

			int rowNumber = parsedRows.length();
			for (int i = 0; i < rowNumber; i++) {
				JSONObject parsedRow = parsedRows.getJSONObject(i);
				IRecord record = new Record(dataStore);
				for(int j = 0; j < dataStoreMeta.getFieldCount(); j++) {
					String columnName = dataStoreMeta.getFieldName(j);
					String columnValue = parsedRow.getString(columnName);
					IFieldMetaData fieldMeta = dataStoreMeta.getFieldMeta(j);
			
					
					Class fieldType = fieldMeta.getType();
					Object value = null;
					if(fieldType == String.class) {
						value = columnValue;
					} else if(fieldType == BigInteger.class ) {
						value = new BigInteger(columnValue);
					} else if(fieldType == Double.class) {
						value = new Double(columnValue);
					} else if(fieldType ==  Timestamp.class) {
						value = TIMESTAMP_FORMATTER.parse(columnValue);
					} else if(fieldType ==  Date.class) {
						value = DATE_FORMATTER.parse(columnValue);
					} else if(fieldType ==  Boolean.class) {
						value = new Boolean(columnValue);
					} else {
						throw new RuntimeException("Impossible to resolve field type [" + fieldType + "]");
					}
					IField field = new Field(value);
					record.appendField(field);
				}
			    dataStore.appendRecord(record);
			}
			 
			 
		} catch (Throwable t) {
			logger.error("Exception reading data", t);
		} finally{
			logger.debug("OUT");
		}

		return dataStore;
	}
}
