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
