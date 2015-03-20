/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.persist.temporarytable;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class NativeTypeTranslatorFactory {
	
	public static final String DRIVER_ORACLE = "Oracle";
	public static final String DRIVER_HSQL = "HSQL";
	public static final String DRIVER_SQLSERVER = "mssqlserver4";
	public static final String DRIVER_SQLSERVERMICROSOFT_WITH_SPACE = "SQL Server";
	public static final String DRIVER_SQLSERVERMICROSOFT = "SQLServer";
	public static final String DRIVER_SQLSERVERSPRINTA = "TdsDriver";
	public static final String DRIVER_SQLSERVERJTURBO = "jturbo";
	public static final String DRIVER_DB2 = "DB2";
	public static final String DRIVER_MYSQL = "MySQL";
	public static final String TEMPORARY_TABLE_NATIVE_TYPES_CLASS = "TEMPORARY_TABLE_NATIVE_TYPES_CLASS";
	
	public static INativeDBTypeable getInstance(String driverName){
//		String className = getProperty( TEMPORARY_TABLE_NATIVE_TYPES_CLASS );
//		INativeDBTypeable toReturn;
//		try {
//			toReturn = (INativeDBTypeable)Class.forName(className).newInstance();
//		} catch (Exception e) {
//			throw new SpagoBIEngineRuntimeException("The db type mapping class "+ className +" is not supported yet for the temporary table creation..");
//		}
//		return toReturn;
		if(driverName.contains(DRIVER_DB2)){
			return new DB2TypeTranslator();
		}else if(driverName.contains(DRIVER_ORACLE)){
			return new OracleTypeTranslator();
		}else if(driverName.contains(DRIVER_SQLSERVER) || driverName.contains(DRIVER_SQLSERVERMICROSOFT) || driverName.contains(DRIVER_SQLSERVERMICROSOFT_WITH_SPACE) || driverName.contains(DRIVER_SQLSERVERSPRINTA) || driverName.contains(DRIVER_SQLSERVERJTURBO)){
			return new SqlServerTypeTranslator();
		}else if(driverName.contains(DRIVER_MYSQL) ){
			return new MySqlTypeTranslator();
		}else if(driverName.contains(DRIVER_HSQL) ){
			return new HSQLTypeTranslator();
		}
		throw new SpagoBIEngineRuntimeException("The db with dialect "+driverName+" is not supported yet for the temporary table creation..");
	}
	
	
	private static String getProperty(String propertName) {
		String propertyValue = null;		
		SourceBean sourceBeanConf;
		EnginConf engineConf = EnginConf.getInstance();
		Assert.assertNotNull(engineConf, "Impossible to parse engine-config.xml file");
		
		sourceBeanConf = (SourceBean) engineConf.getConfig().getAttribute( propertName);
		if(sourceBeanConf != null) {
			propertyValue  = (String) sourceBeanConf.getCharacters();
		}
		
		return propertyValue;		
	}

}
