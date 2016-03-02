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
