/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.DataSourceModel;

import java.util.Locale;

import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 */
public class DataSourceJSONSerializer implements Serializer {
	
	public static final String ID = "DATASOURCE_ID";
	public static final String LABEL = "DATASOURCE_LABEL";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String DRIVER = "DRIVER";
	public static final String DIALECT_ID = "DIALECT_ID";
	public static final String DIALECT_CLASS = "DIALECT_CLASS";
	public static final String DIALECT_NAME = "DIALECT_NAME";
	public static final String JNDI_URL = "JNDI_URL";
	public static final String USER = "USER";
	public static final String PASSWORD = "PASSWORD";	
	public static final String SCHEMA = "SCHEMA";
	public static final String CONNECTION_URL = "CONNECTION_URL";		
	public static final String READ_ONLY = "READ_ONLY";		
	public static final String WRITE_DEFAULT = "WRITE_DEFAULT";		
	public static final String USERIN = "USERIN";

	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof DataSource) || !(o instanceof DataSourceModel) ) {
			throw new SerializationException("DataSourceJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			DataSource dataSource = null;
			result = new JSONObject();
			
			if(o instanceof DataSourceModel){
				dataSource = (DataSourceModel)o;
				result.put(USERIN, ((DataSourceModel)dataSource).getUserIn());
			} else {
				dataSource = (DataSource)o;
			}
			
			result.put(ID, dataSource.getDsId() );
			result.put(LABEL, dataSource.getLabel() );	
			result.put(DESCRIPTION, dataSource.getDescr() );
			result.put(DIALECT_ID,dataSource.getDialectId() );
			result.put(DRIVER,dataSource.getDriver() );
			result.put(DIALECT_CLASS,dataSource.getHibDialectClass() );
			result.put(DIALECT_NAME,dataSource.getHibDialectName() );
			result.put(JNDI_URL,dataSource.getJndi() );
			result.put(USER,dataSource.getUser() );
			result.put(PASSWORD,dataSource.getPwd() );
			result.put(SCHEMA,dataSource.getSchemaAttribute() );
			result.put(CONNECTION_URL,dataSource.getUrlConnection() );
			result.put(READ_ONLY,dataSource.checkIsReadOnly());
			result.put(WRITE_DEFAULT,dataSource.checkIsWriteDefault());
			
		
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}
	
	
}
