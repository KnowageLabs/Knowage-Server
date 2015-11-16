/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.sql;

import java.sql.Types;

/**
 * @see http://download.oracle.com/javase/1.4.2/docs/api/java/sql/Types.html
 * @see http://download.oracle.com/javase/1.3/docs/guide/jdbc/getstart/mapping.html
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JDBCTypeMapper {
	public static Class getJavaType(short jdbcType) {
		switch(jdbcType) {
			case Types.ARRAY: 			return java.sql.Array.class;
			case Types.BIGINT: 			return java.lang.Long.class;
			case Types.BINARY: 			return java.lang.Byte[].class;
			case Types.BIT: 			return java.lang.Boolean.class;
			case Types.BLOB: 			return java.sql.Blob.class;
			case Types.CHAR: 			return java.lang.String.class;
			case Types.CLOB: 			return java.sql.Clob.class;
			case Types.DATE: 			return java.sql.Date.class;
			case Types.DECIMAL: 		return java.math.BigDecimal.class;
			case Types.DISTINCT: 		return java.lang.Object.class;
			case Types.DOUBLE: 			return java.lang.Double.class;
			case Types.FLOAT: 			return java.lang.Double.class;
			case Types.INTEGER: 		return java.lang.Integer.class;
			case Types.JAVA_OBJECT: 	return java.lang.Object.class;
			case Types.LONGVARBINARY: 	return java.lang.Byte[].class;
			case Types.LONGVARCHAR: 	return java.lang.String.class;
			case Types.NULL: 			return java.lang.Object.class;
			case Types.NUMERIC: 		return java.math.BigDecimal.class;
			case Types.OTHER: 			return java.lang.Object.class;
			case Types.REAL: 			return java.lang.Float.class;
			case Types.REF: 			return java.sql.Ref.class;
			case Types.SMALLINT: 		return java.lang.Short.class;
			case Types.STRUCT: 			return java.sql.Struct.class;
			case Types.TIME: 			return java.sql.Time.class;
			case Types.TIMESTAMP: 		return java.sql.Timestamp.class;
			case Types.TINYINT: 		return java.lang.Byte.class;
			case Types.VARBINARY: 		return java.lang.Byte[].class;
			case Types.VARCHAR: 		return java.lang.String.class;
			default: 					return null;	
		}
	}
	
	public static String getModelType(short jdbcType) {
		switch(jdbcType) {
			case Types.ARRAY: 			return "ARRAY";
			case Types.BIGINT: 			return "BIGINT";
			case Types.BINARY: 			return "BINARY";
			case Types.BIT: 			return "BIT";
			case Types.BLOB: 			return "BLOB";
			case Types.CHAR: 			return "CHAR";
			case Types.CLOB: 			return "CLOB";
			case Types.DATE: 			return "DATE";
			case Types.DECIMAL: 		return "DECIMAL";
			case Types.DISTINCT: 		return "DISTINCT";
			case Types.DOUBLE: 			return "DOUBLE";
			case Types.FLOAT: 			return "FLOAT";
			case Types.INTEGER: 		return "INTEGER";
			case Types.JAVA_OBJECT: 	return "JAVA_OBJECT";
			case Types.LONGVARBINARY: 	return "LONGVARBINARY";
			case Types.LONGVARCHAR: 	return "LONGVARCHAR";
			case Types.NULL: 			return "NULL";
			case Types.NUMERIC: 		return "NUMERIC";
			case Types.OTHER: 			return "OTHER";
			case Types.REAL: 			return "REAL";
			case Types.REF: 			return "REF";
			case Types.SMALLINT: 		return "SMALLINT";
			case Types.STRUCT: 			return "STRUCT";
			case Types.TIME: 			return "TIME";
			case Types.TIMESTAMP: 		return "TIMESTAMP";
			case Types.TINYINT: 		return "TINYINT";
			case Types.VARBINARY: 		return "VARBINARY";
			case Types.VARCHAR: 		return "VARCHAR";
			default: 					return null;	
		}
	}
	
	public static String getJavaTypeName(String modelType) {
		Class javaClass = getJavaType(modelType);
		String classString=null;
		int pointIndex;
		if(javaClass != null){
			classString = javaClass.getName();
			if((pointIndex = classString.lastIndexOf('.'))>0){
				classString = classString.substring(pointIndex+1);
			}
		}
		if (needArray(modelType)) return classString+"[]";
		else return classString;
	}
	
	private static boolean needArray(String modelType){
		if( "BINARY".equals(modelType)) 		return true;
		else if( "LONGVARBINARY".equals(modelType))	return true;
		else if( "VARBINARY".equals(modelType)) 	return true;
		else  										return false;					
	}
	
	public static Class getJavaType(String modelType) {
		
		if("ARRAY".equals(modelType))				return java.sql.Array.class;
		else if( "BIGINT".equals(modelType)) 		return java.lang.Long.class;
		else if( "BINARY".equals(modelType)) 		return java.lang.Byte.class;  // tolto []
		else if( "BIT".equals(modelType))			return java.lang.Boolean.class;
		else if( "BLOB".equals(modelType)) 			return java.sql.Blob.class;
		else if( "CHAR".equals(modelType))			return java.lang.String.class;
		else if( "CLOB".equals(modelType))			return java.sql.Clob.class;
		else if( "DATE".equals(modelType)) 			return java.sql.Date.class;
		else if( "DECIMAL".equals(modelType)) 		return java.math.BigDecimal.class;
		else if( "DISTINCT".equals(modelType)) 		return java.lang.Object.class;
		else if( "DOUBLE".equals(modelType)) 		return java.lang.Double.class;
		else if( "FLOAT".equals(modelType)) 		return java.lang.Double.class;
		else if( "INTEGER".equals(modelType)) 		return java.lang.Integer.class;
		else if( "JAVA_OBJECT".equals(modelType)) 	return java.lang.Object.class;
		else if( "LONGVARBINARY".equals(modelType))	return java.lang.Byte.class;// tolto []
		else if( "LONGVARCHAR".equals(modelType)) 	return java.lang.String.class;
		else if( "NULL".equals(modelType))			return java.lang.Object.class;
		else if( "NUMERIC".equals(modelType)) 		return java.math.BigDecimal.class;
		else if( "OTHER".equals(modelType))			return java.lang.Object.class;
		else if( "REAL".equals(modelType)) 			return java.lang.Float.class;
		else if( "REF".equals(modelType))			return java.sql.Ref.class;
		else if( "SMALLINT".equals(modelType)) 		return java.lang.Short.class;
		else if( "STRUCT".equals(modelType))		return java.sql.Struct.class;
		else if( "TIME".equals(modelType))			return java.sql.Time.class;
		else if( "TIMESTAMP".equals(modelType))		return java.sql.Timestamp.class;
		else if( "TINYINT".equals(modelType))		return java.lang.Byte.class;
		else if( "VARBINARY".equals(modelType)) 	return java.lang.Byte.class;// tolto []
		else if( "VARCHAR".equals(modelType)) 		return java.lang.String.class;
		else  										return null;			
	}
	
}
