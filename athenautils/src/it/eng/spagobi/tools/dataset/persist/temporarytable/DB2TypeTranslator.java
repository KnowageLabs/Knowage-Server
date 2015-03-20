/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.persist.temporarytable;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class DB2TypeTranslator implements INativeDBTypeable{

	private static Logger logger = Logger.getLogger("DB2TypeTranslator");
	private static final int MAX_CHAR_SIZE = 254;
	private static Map<String, String> db2TypeMapping;
	static{
		db2TypeMapping = new HashMap<String, String>();
		db2TypeMapping.put("java.lang.Integer", "INTEGER");//no param
		db2TypeMapping.put("java.lang.String", "CHAR");// (n)n<32672  CHAR(n) n<=254
		//db2TypeMapping.put("java.lang.String4001", "CLOB");
		db2TypeMapping.put("java.lang.Boolean", "SMALLINT");//no param
		db2TypeMapping.put("java.lang.Float", "REAL");//no param
		db2TypeMapping.put("java.lang.Double", "DOUBLE");//no param
		db2TypeMapping.put("java.util.Date", "DATE");//no param
		db2TypeMapping.put("java.sql.Date", "DATE");//no param
		db2TypeMapping.put("java.sql.Timestamp", "TIMESTAMP");//no param
		db2TypeMapping.put("java.math.BigDecimal", "DECIMAL");//DECIMAL(p,s)
	}
	
	@SuppressWarnings("rawtypes")
	public String getNativeTypeString(String typeJavaName, Map properties) {
		logger.debug("Translating java type "+typeJavaName+" with properties "+properties);
		// convert java type in SQL type
		String queryType ="";
		String typeSQL ="";

		// proeprties
		Integer size = null;
		Integer precision = null;
		Integer scale = null;

		if(properties!=null){
			if(properties.get(SIZE) != null) 
				size = Integer.valueOf(properties.get(SIZE).toString());
			if(properties.get(PRECISION) != null) 
				precision = Integer.valueOf(properties.get(PRECISION).toString());
			if(properties.get(SCALE) != null) 
				scale = Integer.valueOf(properties.get(SCALE).toString());	
		}


		typeSQL = db2TypeMapping.get(typeJavaName);


		// write Type
		queryType +=" "+typeSQL+""; 

		if(typeJavaName.equalsIgnoreCase(String.class.getName())){
			if(size>MAX_CHAR_SIZE){
				logger.error("For DB2 the max size of a char column must be < "+MAX_CHAR_SIZE+". The size you've specified is "+size);
				throw new SpagoBIRuntimeException("For DB2 the max size of a char column must be < 254. The size you've specified is "+size);
			}
			if( size != null && size!= 0){
				queryType +="("+size+")";
			}
		}else if(typeJavaName.equalsIgnoreCase(BigDecimal.class.getName()) && (precision != null && scale != null)){
			queryType+="("+precision+","+scale+")";
		}

		queryType+=" ";
		
		logger.debug("The translated DB2 type is "+queryType);
		return queryType;
	}

}
