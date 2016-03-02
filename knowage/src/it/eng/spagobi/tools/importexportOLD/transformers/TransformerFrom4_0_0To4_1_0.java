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
package it.eng.spagobi.tools.importexportOLD.transformers;

import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.tools.importexportOLD.ITransformer;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class TransformerFrom4_0_0To4_1_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom4_0_0To4_1_0.class);

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
			fixSbiObjects(conn);
			fixSbiDataSet(conn);
			fixSbiDataSource(conn);
			fixSbiEngines(conn);
			fixSbiSnapshot(conn);
			
			//fixSbiGeoLayers(conn);
			
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
	
	
	private void fixSbiObjects(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "ALTER TABLE SBI_OBJECTS ADD COLUMN PREVIEW_FILE VARCHAR(100);"
					+ " ALTER TABLE SBI_OBJECTS ADD COLUMN IS_PUBLIC BOOLEAN DEFAULT FALSE;"
					+ " UPDATE SBI_OBJECTS SET IS_PUBLIC = TRUE;";
			stmt.executeUpdate(sql);
		
		} catch (Exception e) {
			logger.error(
					"Error in altering sbi_Objects",
					e);
		}
		logger.debug("OUT");
	}
	
	
	private void fixSbiDataSet(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			
			sql = "ALTER TABLE SBI_DATA_SET ADD COLUMN PERSIST_TABLE_NAME VARCHAR(50);"
					+ " ALTER TABLE SBI_DATA_SET DROP COLUMN IS_FLAT_DATASET;"
					+ " ALTER TABLE SBI_DATA_SET DROP COLUMN FLAT_TABLE_NAME;"
					+ " ALTER TABLE SBI_DATA_SET DROP COLUMN DATA_SOURCE_FLAT_ID;"
					+ " ALTER TABLE SBI_DATA_SET DROP COLUMN DATA_SOURCE_PERSIST_ID;";
			stmt.executeUpdate(sql);
			
			
			sql ="ALTER TABLE SBI_DATA_SET ADD COLUMN SCOPE_ID INTEGER DEFAULT NULL;";
			stmt.executeUpdate(sql);
			
		
		} catch (Exception e) {
			logger.error(
					"Error in altering sbi_DataSet",
					e);
		}
		
//		try{
//			sql = "UPDATE SBI_DATA_SET "+
//					"SET SCOPE_ID = "+
//		                 "CASE  "+
//		                   "WHEN OWNER IN (SELECT  "+
//								"U.USER_ID "+
//								"FROM  "+
//								"SBI_USER U, "+
//								"SBI_EXT_USER_ROLES R, "+
//								"SBI_EXT_ROLES RO "+
//								"WHERE OWNER = U.USER_ID "+
//								"AND R.ID = U.ID "+
//								"AND RO.EXT_ROLE_ID = R.EXT_ROLE_ID "+
//								"AND RO.ROLE_TYPE_CD IN ('ADMIN', 'DEV_ROLE') "+
//								"AND IS_PUBLIC = false) THEN (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD='TECHNICAL' AND DOMAIN_CD='DS_SCOPE') "+
//		                   "WHEN OWNER IN (SELECT  "+
//								"U.USER_ID "+
//								"FROM  "+
//								"SBI_USER U, "+
//								"SBI_EXT_USER_ROLES R, "+
//								"SBI_EXT_ROLES RO "+
//								"WHERE OWNER = U.USER_ID "+
//								"AND R.ID = U.ID "+
//								"AND RO.EXT_ROLE_ID = R.EXT_ROLE_ID "+
//								"AND RO.ROLE_TYPE_CD IN ('ADMIN', 'DEV_ROLE') "+
//								"AND IS_PUBLIC = true) THEN (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD='ENTERPRISE' AND DOMAIN_CD='DS_SCOPE') "+
//		                   "WHEN OWNER IN (SELECT  "+
//								"U.USER_ID "+
//								"FROM  "+
//								"SBI_USER U, "+
//								"SBI_EXT_USER_ROLES R, "+
//								"SBI_EXT_ROLES RO "+
//								"WHERE OWNER = U.USER_ID "+
//								"AND R.ID = U.ID "+
//								"AND RO.EXT_ROLE_ID = R.EXT_ROLE_ID "+
//								"AND RO.ROLE_TYPE_CD ='USER') THEN (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD='USER' AND DOMAIN_CD='DS_SCOPE') "+
//		                   "ELSE (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD='TECHNICAL' AND DOMAIN_CD='DS_SCOPE') "+
//		                 "END; "+      
//		"commit;"; 
//		stmt.executeUpdate(sql);
//
//		}
//		catch (Exception e) {
//			logger.error(
//					"Error in assigning scope to sbi_DataSet: go on with black socpes",
//					e);
//		}
		
		logger.debug("OUT");
	}
	
	private void fixSbiDataSource(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			
			sql = "ALTER TABLE SBI_DATA_SOURCE ADD COLUMN READ_ONLY BOOLEAN DEFAULT FALSE;";
			stmt.executeUpdate(sql);
			
			sql = "ALTER TABLE SBI_DATA_SOURCE ADD COLUMN WRITE_DEFAULT BOOLEAN DEFAULT FALSE;";
			stmt.executeUpdate(sql);
		
		} catch (Exception e) {
			logger.error(
					"Error in altering sbi_Data_Source",
					e);
		}
		logger.debug("OUT");
	}

	private void fixSbiEngines(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "ALTER TABLE  SBI_ENGINES DROP COLUMN DEFAULT_DS_ID;";
			stmt.executeUpdate(sql);

		} catch (Exception e) {
			logger.error(
					"Error in altering sbi_Engines",
					e);
		}
		logger.debug("OUT");
	}
	
	private void fixSbiSnapshot(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "ALTER TABLE SBI_SNAPSHOTS ADD COLUMN CONTENT_TYPE VARCHAR(300) DEFAULT NULL;";
			stmt.executeUpdate(sql);

		} catch (Exception e) {
			logger.error(
					"Error in altering sbi_Snapshot",
					e);
		}
		logger.debug("OUT");
	}
	
	
//	private void fixSbiGeoLayers(Connection conn) throws Exception {
//		logger.debug("IN");
//		Statement stmt = conn.createStatement();
//		String sql = "";
//		try {
//			
//			sql = "ALTER TABLE SBI_GEO_LAYERS ADD COLUMN IS_BASE_LAYER BOOLEAN DEFAULT FALSE;";
//			stmt.executeUpdate(sql);
//
//		
//		} catch (Exception e) {
//			logger.error(
//					"Error in altering sbi_Geo_Layers",
//					e);
//		}
//		logger.debug("OUT");
//	}

}

