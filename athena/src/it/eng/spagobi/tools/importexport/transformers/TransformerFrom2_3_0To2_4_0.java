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

public class TransformerFrom2_3_0To2_4_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom2_3_0To2_4_0.class);

	public byte[] transform(byte[] content, String pathImpTmpFolder, String archiveName) {
		logger.debug("IN");
		try {
			TransformersUtilities.decompressArchive(pathImpTmpFolder, archiveName, content);
		} catch(Exception e) {
			logger.error("Error while unzipping 2.3.0 exported archive", e);	
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
			fixObjNotes(conn);
			fixDatasets(conn);
			fixExtRole(conn);
			fixSbiObjects(conn);
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


	/*
	 * Adjust Obj Notes Table
	 * 
	 * @param conn The jdbc connection to export database
	 * @throws Exception
	 */
	private void fixObjNotes(Connection conn) throws Exception {
		logger.debug("IN");
		
		Statement stmt = conn.createStatement();

		// ALTER TABLE SBI_OBJECT_NOTES ADD COLUMN OWNER VARCHAR(50);
		String sql =  "ALTER TABLE SBI_OBJECT_NOTES ADD COLUMN OWNER VARCHAR(50) DEFAULT NULL";
		stmt.execute(sql);
		sql =  "UPDATE SBI_OBJECT_NOTES SET OWNER=NULL";
		stmt.executeUpdate(sql);

		// ALTER TABLE SBI_OBJECT_NOTES ADD COLUMN ISPUBLIC BOOLEAN;
		sql =  "ALTER TABLE SBI_OBJECT_NOTES ADD COLUMN ISPUBLIC BOOLEAN DEFAULT FALSE";
		stmt.executeUpdate(sql);
		sql =  "UPDATE SBI_OBJECT_NOTES SET ISPUBLIC=FALSE";
		stmt.executeUpdate(sql);

		// ALTER TABLE SBI_OBJECT_NOTES ADD COLUMN CREATION_DATE TIMESTAMP NOT NULL;
		sql =  "ALTER TABLE SBI_OBJECT_NOTES ADD COLUMN CREATION_DATE TIMESTAMP NOT NULL";
		stmt.executeUpdate(sql);
		sql =  "UPDATE SBI_OBJECT_NOTES SET CREATION_DATE='1900-01-01 00:00:00.000000000'";
		stmt.executeUpdate(sql);

		// ALTER TABLE SBI_OBJECT_NOTES ADD COLUMN LAST_CHANGE_DATE TIMESTAMP NOT NULL
		sql =  "ALTER TABLE SBI_OBJECT_NOTES ADD COLUMN LAST_CHANGE_DATE TIMESTAMP NOT NULL";
		stmt.executeUpdate(sql);
		sql =  "UPDATE SBI_OBJECT_NOTES SET LAST_CHANGE_DATE='1900-01-01 00:00:00.000000000'";
		stmt.executeUpdate(sql);

		logger.debug("OUT");
	}
	
	
	
	/*
	 * Adjust DataSet Table
	 * 
	 * @param conn The jdbc connection to export database
	 * @throws Exception
	 */
	private void fixDatasets(Connection conn) throws Exception {
		logger.debug("IN");
		
		Statement stmt = conn.createStatement();

		// ALTER TABLE SBI_DATA_SET ADD COLUMN DS_METADATA VARCHAR(2000) DEFAULT NULL;
		String sql =  "ALTER TABLE SBI_DATA_SET ADD COLUMN DS_METADATA VARCHAR(2000) DEFAULT NULL;";
		stmt.execute(sql);
		sql =  "UPDATE SBI_DATA_SET SET DS_METADATA=NULL";
		stmt.executeUpdate(sql);

		logger.debug("OUT");
	}

	

	/*
	 * Adjust ExtRoles Table
	 * 
	 * @param conn The jdbc connection to export database
	 * @throws Exception
	 */
	private void fixExtRole(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		
		// ALTER TABLE SBI_EXT_ROLES ADD COLUMN SAVE_METADATA BOOLEAN DEFAULT TRUE;
		String sql =  "ALTER TABLE SBI_EXT_ROLES ADD COLUMN SAVE_METADATA BOOLEAN DEFAULT TRUE";
		stmt.execute(sql);
		sql =  "UPDATE SBI_EXT_ROLES SET SAVE_METADATA=TRUE";
		stmt.executeUpdate(sql);
		logger.debug("OUT");
	}

	
	/*
	 * Adjust SbiObjects Table
	 * 
	 * @param conn The jdbc connection to export database
	 * @throws Exception
	 */
	private void fixSbiObjects(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		
		// ALTER TABLE SBI_OBJECTS DROP COLUMN DESCR_EXT;
		String sql =  "ALTER TABLE SBI_OBJECTS DROP COLUMN DESCR_EXT;";
		stmt.execute(sql);
		sql =  "ALTER TABLE SBI_OBJECTS DROP COLUMN OBJECTIVE;";
		stmt.executeUpdate(sql);
		sql =  "ALTER TABLE SBI_OBJECTS DROP COLUMN LANGUAGE;";
		stmt.executeUpdate(sql);
		sql =  "ALTER TABLE SBI_OBJECTS DROP COLUMN KEYWORDS;";
		stmt.executeUpdate(sql);
		
		logger.debug("OUT");
	}

	

}
