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

public class TransformerFrom3_1_0To3_2_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom3_1_0To3_2_0.class);

	public byte[] transform(byte[] content, String pathImpTmpFolder, String archiveName) {
		logger.debug("IN");
		try {
			TransformersUtilities.decompressArchive(pathImpTmpFolder, archiveName, content);
		} catch(Exception e) {
			logger.error("Error while unzipping 3.1.0 exported archive", e);	
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
			fixParView(conn);
			fixDataSet(conn);
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

	private void fixParView(Connection conn) throws Exception {
		logger.debug("IN");

		Statement stmt = conn.createStatement();
		
		String sql = "CREATE TABLE SBI_OBJ_PARVIEW ( "+
				  "OBJ_PAR_ID INTEGER NOT NULL, "+
				   "OBJ_PAR_FATHER_ID  INTEGER NOT NULL, "+
				   "OPERATION  VARCHAR(20) NOT NULL, "+
				   "COMPARE_VALUE  VARCHAR(200) NOT NULL, "+
				   "VIEW_LABEL  VARCHAR(50), "+
				   "PROG INTEGER)";

		stmt.executeUpdate(sql);

		logger.debug("OUT");
	}

	private void fixDataSet(Connection conn) throws Exception {
		logger.debug("IN");

		Statement stmt = conn.createStatement();
		
		String sql = "ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN CUSTOM_DATA VARCHAR DEFAULT NULL;";

		stmt.executeUpdate(sql);

		logger.debug("OUT");
	}

}
