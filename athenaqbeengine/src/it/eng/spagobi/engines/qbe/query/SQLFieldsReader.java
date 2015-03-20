/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/**
 * Class that can get fields (i.e name and type) from a sql query string
 * 
 * @author Gioia
 */
public class SQLFieldsReader {

	/** The query. */
	private String query;
	
	/** The connection. */
	private Connection connection;

	/**
	 * Instantiates a new sQL fields reader.
	 * 
	 * @param query the query
	 * @param connection the connection
	 */
	public SQLFieldsReader(String query, Connection connection) {
		this.query = query;
		this.connection = connection;
	}
	
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.export.IFieldsReader#readFields()
	 */
	public Vector readFields() throws Exception {
		Vector queryFields = new Vector();
        
		PreparedStatement ps = null;
		
        try {
        	
        	
        	String queryToUpperCase = query.toUpperCase();
        	
        	if ( queryToUpperCase.indexOf("GROUP BY") > 0 ){
        		String before = query.substring(0, queryToUpperCase.indexOf("GROUP BY"));
        		String after =query.substring(queryToUpperCase.indexOf("GROUP BY")); 
        		
        		if (( before.indexOf("where") > 0) || (before.indexOf("WHERE") > 0)){
        			before += " and 1 = 0 ";
        		}else{
        			before += " where 1 = 0 ";
        		}
        		
        		
        		query = before + after; 
        	
        	}else if ( queryToUpperCase.indexOf("ORDER BY") > 0 ){
        		
        		String before = query.substring(0, queryToUpperCase.indexOf("ORDER BY"));
        		String after =query.substring(queryToUpperCase.indexOf("ORDER BY")); 
        		
        		if (( before.indexOf("where") > 0) || (before.indexOf("WHERE") > 0)){
        			before += " and 1 = 0 ";
        		}else{
        			before += " where 1 = 0 ";
        		}	
        		query = before + after;
        	}else{
        		if (( query.indexOf("where") > 0) || (query.indexOf("WHERE") > 0)){
        			query += " and 1 = 0 ";
        		}else{
        			query += " where 1 = 0 ";
        		}        		
        	}
        	
        	ps = connection.prepareStatement( query );
             
            // Some JDBC drivers don't supports this method...
            try { ps.setFetchSize(0); } catch(Exception e ) {}
             
             
             ResultSet rs = ps.executeQuery();             
             ResultSetMetaData rsmd = rs.getMetaData();             
             
             List columns = new ArrayList();
             for (int i=1; i <= rsmd.getColumnCount(); ++i) {
            	 Field field = new Field(
                         rsmd.getColumnLabel(i), 
                         getJdbcTypeClass(rsmd, i),
                         rsmd.getColumnDisplaySize(i));
                 
            	 queryFields.add( field );
             }
         }
         catch(Exception e) {
        	 e.printStackTrace();
         }
		
		
		return queryFields;
	}
	
	/**
	 * Gets the jdbc type class.
	 * 
	 * @param rsmd the rsmd
	 * @param t the t
	 * 
	 * @return the jdbc type class
	 */
	public static String getJdbcTypeClass(ResultSetMetaData rsmd, int t ) {
         String cls = "java.lang.Object";

         try {
             cls = rsmd.getColumnClassName(t);
             cls =  Field.getFieldType(cls);

         } catch (Exception ex) {
             // if getColumnClassName is not supported...
             try {
                 int type = rsmd.getColumnType(t);
                 switch( type ) {
                         case java.sql.Types.TINYINT:
                         case java.sql.Types.BIT:
                                 cls = "java.lang.Byte";
                                 break;
                         case java.sql.Types.SMALLINT:
                                 cls = "java.lang.Short";
                                 break;
                         case java.sql.Types.INTEGER:
                                 cls = "java.lang.Integer";
                                 break;
                         case java.sql.Types.FLOAT:
                         case java.sql.Types.REAL:
                         case java.sql.Types.DOUBLE:
                         case java.sql.Types.NUMERIC:
                         case java.sql.Types.DECIMAL:
                                 cls = "java.lang.Double";
                                 break;
                         case java.sql.Types.CHAR:
                         case java.sql.Types.VARCHAR:
                                 cls = "java.lang.String";
                                 break;

                         case java.sql.Types.BIGINT:
                                 cls = "java.lang.Long";
                                 break;
                         case java.sql.Types.DATE:
                                 cls = "java.util.Date";
                                 break;
                         case java.sql.Types.TIME:
                                 cls = "java.sql.Time";
                                 break;
                         case java.sql.Types.TIMESTAMP:
                                 cls = "java.sql.Timestamp";
                                 break;
                 }
             } catch (Exception ex2){
                 ex2.printStackTrace();
             }
         }
         return cls;
	 }
	
}
