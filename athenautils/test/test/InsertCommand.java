/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package test;


import java.util.ArrayList;
import java.util.List;

import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.StringUtils;

import org.apache.log4j.Logger;


public class InsertCommand {


	public static transient Logger logger = Logger.getLogger(InsertCommand.class);

	IMetaData metadata;
	IRecord record;
	String tableName;

	public InsertCommand(IMetaData metadata, String tableName) {
		this.metadata = metadata;
		this.tableName = tableName;
	}

	public void setRecord(IRecord record) {
		this.record = record;
	}

	public String getTableName() {
		return tableName;
	}


	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String createSQLQuery(List<String> selectedFields){
		
		if ( selectedFields == null ) {
			selectedFields = new ArrayList<String>();
		}
		
		StringBuffer buffer = new StringBuffer("INSERT INTO " + this.tableName + " VALUES (");
		
		int count = this.metadata.getFieldCount();
		for (int i = 0 ; i < count ; i++) {
			IFieldMetaData fieldMetadata = this.metadata.getFieldMeta(i);
			String fieldName = fieldMetadata.getName();
			if ( selectedFields.isEmpty() || selectedFields.contains(fieldName) ) {
			
				Class c = fieldMetadata.getType();
				IField field = this.record.getFieldAt(i);
				String value = field.getValue().toString();
				
				if ( String.class.isAssignableFrom(c) ) {
					value = StringUtils.escapeQuotes(value);
					buffer.append("'" + value + "'");
				} else {
					buffer.append(value);
				}
				
				buffer.append(",");
			
			}

		}
		
		buffer.delete(buffer.length() - 1, buffer.length()); // remove last ","
		
		buffer.append(")");
		String query = buffer.toString();
		logger.debug("Query is " + query);
		logger.debug("OUT");
		return query;
	}

//	public static void main (String[] args) {
//		StringBuffer buffer = new StringBuffer("ciao davide");
//		buffer.append(",");
//		buffer.delete(buffer.length() - 1, buffer.length());
//		String query = buffer.toString();
//		System.out.println("[" + query + "]");
//	}
	
}
