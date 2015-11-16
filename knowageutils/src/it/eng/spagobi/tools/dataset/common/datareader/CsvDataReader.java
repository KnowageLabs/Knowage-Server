/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.datareader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;

/**
 * @author Angelo Bernabei
 *         angelo.bernabei@eng.it
 */
public class CsvDataReader extends AbstractDataReader {
	
	
	final static String SEPARATOR=";";

	private static transient Logger logger = Logger.getLogger(CsvDataReader.class);
	
  

	public CsvDataReader() {
		super();
	}

	public IDataStore read( Object data ) {
		DataStore dataStore = null;
		MetaData dataStoreMeta;
		
		InputStream inputDataStream;
		LineNumberReader lineReader;
		String line;
		
		logger.debug("IN");
		
		inputDataStream = (InputStream)data;
		
		dataStore = new DataStore();
		dataStoreMeta = new MetaData();
		dataStore.setMetaData(dataStoreMeta);
		
		try {				
			lineReader = new LineNumberReader( new InputStreamReader( inputDataStream ) );
			while ( (line = lineReader.readLine()) != null ){
				IRecord record = new Record(dataStore);
								
				StringTokenizer tokenizer = new StringTokenizer(line, SEPARATOR);
				while(tokenizer.hasMoreElements()){
					String token = tokenizer.nextToken();
					if (lineReader.getLineNumber() == 1) {
						FieldMetadata fieldMeta = new FieldMetadata();
						fieldMeta.setName(token);
						fieldMeta.setType(String.class);
						dataStoreMeta.addFiedMeta(fieldMeta);
					} else {
						if (token != null) {
							IField field = new Field(token);
							record.appendField(field);
						}
					}
				}
				
				if (lineReader.getLineNumber() != 1){
					dataStore.appendRecord(record);				
				}
			}
				
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		return dataStore;
    }

}
