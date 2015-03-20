/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.persist.temporarytable;


import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.persist.DataSetTableDescriptor;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;


public class DatasetTemporaryTableUtils {

	private static transient Logger logger = Logger.getLogger(DatasetTemporaryTableUtils.class);

	/**
	 * Creates a table with columns got from metadata.
	 * PAY ATTENTION TO THE FACT THAT THE INPUT CONNECTION WON'T BE CLOSED!!!!!
	 * @param conn The JDBC connection to be used
	 * @param meta The metadata of the dataset to be persisted on the database
	 * @param tableName The name of the table to be created
	 * @param list The list of the fields of the dataset to be included on table
	 * @return A DataSetTableDescriptor that contains the association between table's columns and dataset's fields.
	 */
	public static DataSetTableDescriptor createTemporaryTable(Connection conn, IMetaData meta, String tableName, List<String> selectedFields) {
		logger.debug("IN");

		DataSetTableDescriptor dstd = null;
		Statement st = null;
		String sqlQuery = null;

		if ( selectedFields == null ) {
			selectedFields = new ArrayList<String>();
		}
		
		try {
			CreateTableCommand createTableCommand = new CreateTableCommand(tableName, conn.getMetaData().getDriverName());

			// run through all columns in order to build the SQL columndefinition
			int count = meta.getFieldCount();
			logger.debug("The table tableName has "+count+" columns ");
			for (int i = 0 ; i < count ; i++) {
				IFieldMetaData fieldMeta = meta.getFieldMeta(i);
				String fieldName = fieldMeta.getName();
				if (selectedFields.isEmpty() || selectedFields.contains(fieldName)) {
					createTableCommand.addColumn(fieldMeta);
				}
			}

			// after built columns create SQL Query
			sqlQuery = createTableCommand.createSQLQuery();

			// execute 
			logger.debug("Executing the query "+sqlQuery+"...");
			st = conn.createStatement();
			st.execute(sqlQuery);
			logger.debug("Query executed");
			dstd = createTableCommand.getDsTableDescriptor();
			LogMF.debug(logger, "The query descriptor is {0}", dstd);

		} catch (SQLException e) {
			logger.error("Error in excuting statement " + sqlQuery, e);
			throw new SpagoBIRuntimeException("Error creating temporary table", e);
		}
		finally {
			try {
				if ( st != null ) {
					st.close();
				}
			} catch (SQLException e) {
				logger.error("could not free resources ", e);
			}
		}
		logger.debug("OUT");
		return dstd;
	}




	//	public static void main(String[] args) throws Exception {
	//
	//		MetaData meta = new MetaData();
	//
	//		IFieldMetaData fieldMetaDataIntero = new FieldMetadata();
	//		fieldMetaDataIntero.setAlias("Colonna alias intero");
	//		fieldMetaDataIntero.setName("ColonnaIntera");
	//		fieldMetaDataIntero.setType(java.math.BigDecimal.class);
	//
	//		IFieldMetaData fieldMetaDataStringa = new FieldMetadata();
	//		fieldMetaDataStringa.setAlias("Colonna alias stringa");
	//		fieldMetaDataStringa.setName("ColonnaStringa");
	//		fieldMetaDataStringa.setProperty("size", 50);
	//		fieldMetaDataStringa.setType(java.lang.String.class);
	//		
	//		IFieldMetaData fieldMetaDataBool = new FieldMetadata();
	//		fieldMetaDataBool.setAlias("Colonna alias Bool");
	//		fieldMetaDataBool.setName("ColonnaBool");
	//		fieldMetaDataBool.setType(java.lang.Boolean.class);
	//
	//		IFieldMetaData fieldMetaDataStringa2 = new FieldMetadata();
	//		fieldMetaDataStringa2.setAlias("Colonna alias text");
	//		fieldMetaDataStringa2.setName("ColonnaTextCLOb");
	//		fieldMetaDataStringa2.setType(java.lang.String.class);
	//
	//		IFieldMetaData fieldMetaDataFloat = new FieldMetadata();
	//		fieldMetaDataFloat.setAlias("Colonna alias text");
	//		fieldMetaDataFloat.setName("ColonnaFloatt");
	//		fieldMetaDataFloat.setProperty("precision", 10);
	//		fieldMetaDataFloat.setProperty("scale", 5);
	//		fieldMetaDataFloat.setType(java.lang.Float.class);
	//		
	//		IFieldMetaData fieldMetaDataDate = new FieldMetadata();
	//		fieldMetaDataDate.setAlias("Colonna alias Date");
	//		fieldMetaDataDate.setName("ColonnaDate");
	//		fieldMetaDataDate.setType(java.sql.Date.class);
	//		
	//		IFieldMetaData fieldMetaDataTime = new FieldMetadata();
	//		fieldMetaDataTime.setAlias("Colonna alias Time");
	//		fieldMetaDataTime.setName("ColonnaTime");
	//		fieldMetaDataTime.setType(oracle.sql.TIMESTAMP.class);
	//		
	//		
	//		meta.addFiedMeta(fieldMetaDataIntero);
	//		meta.addFiedMeta(fieldMetaDataStringa);
	//		meta.addFiedMeta(fieldMetaDataStringa2);
	//		meta.addFiedMeta(fieldMetaDataFloat);
	//		meta.addFiedMeta(fieldMetaDataBool);
	//		meta.addFiedMeta(fieldMetaDataDate);
	//		meta.addFiedMeta(fieldMetaDataTime);
	//		
	//		Connection connection = null;
	//		try{
	////			Class.forName( "com.mysql.jdbc.Driver" );
	////			connection = DriverManager.getConnection("jdbc:mysql://localhost/foodmart", "root", "admin");
	//			Class.forName( "oracle.jdbc.OracleDriver" );
	//			connection = DriverManager.getConnection("jdbc:oracle:thin:@sibilla2:1521:REPO", "spagobi", "bispago");
	//		}
	//		catch (Exception e) {
	//			// TODO: handle exception
	//		}
	//
	//		// map alias to index
	//
	//		int r =new Random().nextInt();
	//		if(r <0 ) r*=-1;
	//		String tableName = "DatasetName"+r;
	//		System.out.println(tableName);
	//		DatasetTempTable.createTemporaryTable(connection, meta, tableName);
	//	}



}


