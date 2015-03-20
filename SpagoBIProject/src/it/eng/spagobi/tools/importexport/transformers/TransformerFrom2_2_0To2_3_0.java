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

public class TransformerFrom2_2_0To2_3_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom2_2_0To2_3_0.class);

	public byte[] transform(byte[] content, String pathImpTmpFolder, String archiveName) {
		logger.debug("IN");
		try {
			TransformersUtilities.decompressArchive(pathImpTmpFolder, archiveName, content);
		} catch(Exception e) {
			logger.error("Error while unzipping 2.2.0 exported archive", e);	
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
			fixDataSource(conn);
			fixExtRole(conn);
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
	 * Adjust DataSource Table
	 * 
	 * @param conn The jdbc connection to export database
	 * @throws Exception
	 */
	private void fixDataSource(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql =  "ALTER TABLE SBI_DATA_SOURCE ADD COLUMN MULTI_SCHEMA BOOLEAN DEFAULT FALSE";
		stmt.execute(sql);
		sql =  "UPDATE SBI_DATA_SOURCE SET MULTI_SCHEMA=FALSE";
		stmt.executeUpdate(sql);
		sql =  "ALTER TABLE SBI_DATA_SOURCE ADD COLUMN ATTR_SCHEMA VARCHAR(45) DEFAULT NULL";
		stmt.execute(sql);
		sql =  "UPDATE SBI_DATA_SOURCE SET ATTR_SCHEMA = NULL ";
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
		String sql =  "ALTER TABLE SBI_EXT_ROLES ADD COLUMN BUILD_QBE_QUERY BOOLEAN DEFAULT TRUE";
		stmt.execute(sql);
		sql =  "UPDATE SBI_EXT_ROLES SET BUILD_QBE_QUERY=TRUE";
		stmt.executeUpdate(sql);
		logger.debug("OUT");
	}
	

}
