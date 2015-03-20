/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport.transformers;

import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.tools.importexport.ITransformer;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class TransformerFrom3_7_0To4_0_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom3_7_0To4_0_0.class);

	public byte[] transform(byte[] content, String pathImpTmpFolder, String archiveName) {
		logger.debug("IN");
		try {
			TransformersUtilities.decompressArchive(pathImpTmpFolder, archiveName, content);
		} catch(Exception e) {
			logger.error("Error while unzipping exported archive", e);	
		}
		archiveName = archiveName.substring(0, archiveName.lastIndexOf('.'));
		changeDatabase(pathImpTmpFolder, archiveName);
		// compress archive
		try {
			content = TransformersUtilities.createExportArchive(pathImpTmpFolder, archiveName);
		} catch (Exception e) {
			logger.error("Error while creating creating the export archive", e);	
		}
		// delete tmp dir content
		File tmpDir = new File(pathImpTmpFolder);
		GeneralUtilities.deleteContentDir(tmpDir);
		logger.debug("OUT");
		return content;
	}

	private void changeDatabase(String pathImpTmpFolder, String archiveName) {
		logger.debug("IN");
		Connection conn = null;
		try {
			conn = TransformersUtilities.getConnectionToDatabase(pathImpTmpFolder, archiveName);

			fixSbiMetaModels(conn);
			fixSbiDatasetHistory(conn);
			fixSbiDatasetTemp(conn);
			fixSbiEngines(conn);
			fixSbiDataSet(conn);
			
			
			conn.commit();
		} catch (Exception e) {
			logger.error("Error while changing database", e);	
		} finally {
			logger.debug("OUT");
			try {
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error closing connection to export database", e);
			}
		}
	}
	

	
	private void fixSbiMetaModels(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "ALTER TABLE SBI_META_MODELS ADD COLUMN CATEGORY_ID INTEGER;";
			stmt.executeUpdate(sql);
			
			sql = "ALTER TABLE SBI_META_MODELS ADD COLUMN DATA_SOURCE_ID INTEGER;";
			stmt.executeUpdate(sql);
			
		} catch (Exception e) {
			logger.error(
					"Error in altering sbi_meta_models",
					e);
		}
		logger.debug("OUT");
	}
	
	private void fixSbiDatasetHistory(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN IS_PERSISTED BOOLEAN DEFAULT FALSE;"+
					" ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN DATA_SOURCE_PERSIST_ID INTEGER NULL;"+
					" ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN IS_FLAT_DATASET BOOLEAN DEFAULT FALSE;"+
					" ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN FLAT_TABLE_NAME VARCHAR(50) NULL;"+
					" ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN USER_IN VARCHAR(100) DEFAULT '' NOT NULL;"+
					" ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN SBI_VERSION_IN VARCHAR(10);"+
					" ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN TIME_IN TIMESTAMP DEFAULT NOW NOT NULL;"+
					" ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN DATA_SOURCE_FLAT_ID INTEGER NULL;"+
					" ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN META_VERSION VARCHAR(100) NULL;"+
					" ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN ORGANIZATION VARCHAR(20) NULL;";			
			stmt.execute(sql);
		} catch (Exception e) {
			logger.error(
					"Error in altering sbi_data_set_history",
					e);
		}
		logger.debug("OUT");
	}
	
	private void fixSbiDatasetTemp(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "CREATE MEMORY TABLE SBI_DATA_SET_TEMP(DS_ID INTEGER NOT NULL, VERSION_NUM INTEGER NOT NULL, ACTIVE BOOLEAN NOT NULL, DESCR VARCHAR(160), LABEL VARCHAR(50) NOT NULL, NAME VARCHAR(50) NOT NULL,OBJECT_TYPE VARCHAR(50),CONFIGURATION VARCHAR,DS_METADATA VARCHAR,PARAMS VARCHAR(4000),CATEGORY_ID INTEGER,TRANSFORMER_ID INTEGER,PIVOT_COLUMN VARCHAR(50),PIVOT_ROW VARCHAR(50),PIVOT_VALUE VARCHAR(50),NUM_ROWS BOOLEAN DEFAULT false,META_VERSION VARCHAR(100),IS_PERSISTED BOOLEAN DEFAULT FALSE,DATA_SOURCE_PERSIST_ID INTEGER NULL,IS_FLAT_DATASET BOOLEAN DEFAULT FALSE,FLAT_TABLE_NAME VARCHAR(50) NULL,DATA_SOURCE_FLAT_ID INTEGER NULL,USER_IN VARCHAR(100) DEFAULT '' NOT NULL, USER_UP VARCHAR(100),USER_DE VARCHAR(100), TIME_IN TIMESTAMP DEFAULT NOW NOT NULL, TIME_UP TIMESTAMP, TIME_DE TIMESTAMP,SBI_VERSION_IN VARCHAR(10),SBI_VERSION_UP VARCHAR(10),SBI_VERSION_DE VARCHAR(10),ORGANIZATION VARCHAR(20),OWNER VARCHAR(50), IS_PUBLIC BOOLEAN DEFAULT FALSE, CONSTRAINT XPKSBI_DATA_SET_TEMP PRIMARY KEY(DS_ID, VERSION_NUM),CONSTRAINT XAK2SBI_DATA_SET UNIQUE(LABEL,VERSION_NUM,ORGANIZATION))";
			stmt.execute(sql);
		} catch (Exception e) {
			logger.error(
					"Error in adding sbi_data_set_temp",
					e);
		}
		


		stmt = conn.createStatement();
		sql = "";
		try {
			sql = " INSERT INTO SBI_DATA_SET_TEMP (DS_ID, VERSION_NUM, ACTIVE,  LABEL, DESCR, NAME, OBJECT_TYPE, DS_METADATA, PARAMS, CATEGORY_ID, TRANSFORMER_ID, PIVOT_COLUMN, PIVOT_ROW, PIVOT_VALUE, NUM_ROWS, IS_PERSISTED,"+ 
					" DATA_SOURCE_PERSIST_ID, IS_FLAT_DATASET, FLAT_TABLE_NAME, DATA_SOURCE_FLAT_ID, USER_IN, USER_UP, USER_DE, TIME_IN, TIME_UP, TIME_DE, SBI_VERSION_IN, SBI_VERSION_UP, SBI_VERSION_DE,"+
					" META_VERSION, ORGANIZATION, CONFIGURATION) "+
					" SELECT DS.DS_ID, ds_h.VERSION_NUM, ds_h.ACTIVE, ds.LABEL, ds.DESCR, ds.name,"+
					" ds_h.OBJECT_TYPE, ds_h.DS_METADATA,"+
					" ds_h.PARAMS, ds_h.CATEGORY_ID, ds_h.TRANSFORMER_ID, ds_h.PIVOT_COLUMN, ds_h.PIVOT_ROW,"+
					" ds_h.PIVOT_VALUE, ds_h.NUM_ROWS, ds_h.IS_PERSISTED, ds_h.DATA_SOURCE_PERSIST_ID, "+
					" ds_h.IS_FLAT_DATASET, ds_h.FLAT_TABLE_NAME, ds_h.DATA_SOURCE_FLAT_ID, ds_h.USER_IN, "+
					" null as USER_UP,null as USER_DE, ds_h.TIME_IN, null as TIME_UP, null as TIME_DE,"+
					" ds_h.SBI_VERSION_IN, null as SBI_VERSION_UP,  null as SBI_VERSION_DE, ds_h.META_VERSION,"+
					" ds_h.ORGANIZATION"+
					" ,case when ds_h.OBJECT_TYPE = 'SbiQueryDataSet' then "+
					" concat(concat(concat(concat(concat(concat(concat(concat('{\"Query\":\"',REPLACE(ds_h.QUERY,'\"','\\\"')),'\",\"queryScript\":\"'),REPLACE(COALESCE(DS_H.QUERY_SCRIPT,''),'\"','\\\"')),'\",\"queryScriptLanguage\":\"'),COALESCE(QUERY_SCRIPT_LANGUAGE,'')),'\",\"dataSource\":\"'),COALESCE(CAST((SELECT LABEL FROM SBI_DATA_SOURCE WHERE DS_ID = DATA_SOURCE_ID) AS CHAR),'')),'\"}')"+
					" WHEN ds_h.OBJECT_TYPE = 'SbiFileDataSet' then concat(concat('{\"fileName\":\"',COALESCE(DS_H.FILE_NAME,'')),'\"}')"+
					" WHEN ds_h.OBJECT_TYPE = 'SbiWSDataSet' then concat(concat(concat(concat(concat('{\"wsAddress\":\"',COALESCE(DS_H.ADRESS,'')),'\"}'),'\",\"wsOperation\":\"'),COALESCE(DS_H.OPERATION,'')),'\"}')"+
					" WHEN ds_h.OBJECT_TYPE = 'SbiScriptDataSet' then concat(concat(concat(concat(concat('{\"Script\":\"',REPLACE(COALESCE(DS_H.SCRIPT,''),'\"','\\\"')),'\"}'),'\",\"scriptLanguage\":\"'),COALESCE(DS_H.LANGUAGE_SCRIPT,'')),'\"}')"+
					" WHEN ds_h.OBJECT_TYPE = 'SbiCustomDataSet' then concat(concat(concat(concat(concat('{\"customData\":\"',REPLACE(COALESCE(DS_H.CUSTOM_DATA,''),'\"','\\\"')),'\"}'),'\",\"jClassName\":\"'),COALESCE(DS_H.JCLASS_NAME,'')),'\"}')"+
					" WHEN ds_h.OBJECT_TYPE = 'SbiQbeDataSet' then  concat(concat(concat(concat(concat(concat(concat('{\"qbeDatamarts\":\"',COALESCE(DS_H.DATAMARTS,'')),'\",\"qbeJSONQuery\":\"'),REPLACE(COALESCE(DS_H.JSON_QUERY,''),'\"','\\\"')),'\",\"qbeDataSource\":\"'),COALESCE(CAST((SELECT LABEL FROM SBI_DATA_SOURCE WHERE DS_ID = DATA_SOURCE_ID) AS CHAR))),''),'\"}')"+
					" end AS CONFIGURATION"+
					" FROM "+
					" SBI_DATA_SET DS INNER JOIN SBI_DATA_SET_HISTORY DS_H ON (DS.DS_ID = DS_H.DS_ID)"+
					" order by ds_id, version_num;";
			
			stmt.execute(sql);
		} catch (Exception e) {
			logger.error(
					"Error in transferring data_sets",
					e);
		}

		
		stmt = conn.createStatement();
		sql = "";
		try {
			sql = 	//" DROP TABLE SBI_DATA_SET_HISTORY CASCADE;   "+
					//" DROP TABLE SBI_DATA_SET CASCADE;"+
					" ALTER TABLE SBI_DATA_SET RENAME TO SBI_DATA_SET_OLD;"+
					" ALTER TABLE SBI_DATA_SET_HISTORY RENAME TO SBI_DATA_SET_HISTORY_OLD;"+
					" ALTER TABLE SBI_DATA_SET_TEMP RENAME TO SBI_DATA_SET;";
			
			
			stmt.execute(sql);
		} catch (Exception e) {
			logger.error(
					"Error in dropping contstraints and renaming",
					e);
		}
		
		
		logger.debug("OUT");
	}
	
	
	

	private void fixSbiEngines(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "UPDATE SBI_ENGINES SET USE_DATASET = TRUE WHERE DRIVER_NM = 'it.eng.spagobi.engines.drivers.worksheet.WorksheetDriver';";
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			logger.error(
					"Error in altering sbi engines",
					e);
		}
		logger.debug("OUT");
	}
	
	private void fixSbiDataSet(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
//		try {
//			sql = "ALTER TABLE SBI_DATA_SET ADD COLUMN OWNER VARCHAR(50) DEFAULT NULL;";
//			stmt.executeUpdate(sql);
//			sql = "ALTER TABLE SBI_DATA_SET ADD COLUMN IS_PUBLIC BOOLEAN DEFAULT FALSE;";
//			stmt.executeUpdate(sql);
//			
//		} catch (Exception e) {
//			logger.error(
//					"Error in altering sbi dataset",
//					e);
//		}
		logger.debug("OUT");
	}

	

	

}

