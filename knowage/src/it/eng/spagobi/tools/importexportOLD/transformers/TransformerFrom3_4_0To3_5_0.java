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

public class TransformerFrom3_4_0To3_5_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom3_4_0To3_5_0.class);

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
			fixDomains(conn);
			fixParuses(conn);
			fixRoles(conn);
			fixDatasets(conn);
			fixLovs(conn);
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
	
	private void fixDomains(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "UPDATE SBI_DOMAINS SET value_cd='ECMAScript' WHERE domain_cd = 'SCRIPT_TYPE' AND value_cd='rhino-nonjdk';";
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			logger.error(
					"Error updating domains",
					e);
		}
		logger.debug("OUT");
	}

	private void fixLovs(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "ALTER TABLE SBI_LOV ADD COLUMN DATASET_ID INTEGER;";
			stmt.execute(sql);
		} catch (Exception e) {
			logger.error(
					"Error adding column: if add column fails may mean that column already esists; means you are not using an exact version spagobi DB",
					e);
		}
		logger.debug("OUT");
	}
	
	private void fixDatasets(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN QUERY_SCRIPT VARCHAR DEFAULT NULL;";
			stmt.execute(sql);
			sql = "ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN QUERY_SCRIPT_LANGUAGE VARCHAR DEFAULT NULL;";
			stmt.execute(sql);
		} catch (Exception e) {
			logger.error(
					"Error adding column: if add column fails may mean that column already esists; means you are not using an exact version spagobi DB",
					e);
		}
		logger.debug("OUT");
	}
	
	private void fixRoles(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "ALTER TABLE SBI_EXT_ROLES ADD COLUMN EDIT_WORKSHEET BOOLEAN DEFAULT TRUE;";
			stmt.execute(sql);
		} catch (Exception e) {
			logger.error(
					"Error adding column: if add column fails may mean that column already esists; means you are not using an exact version spagobi DB",
					e);
		}
		logger.debug("OUT");
	}

	private void fixParuses(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "ALTER TABLE SBI_PARUSE ADD COLUMN MAXIMIZER_ENABLED BOOLEAN DEFAULT FALSE;";
			stmt.execute(sql);
			sql = "UPDATE SBI_PARUSE SET MAXIMIZER_ENABLED = FALSE;";
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			logger.error(
					"Error adding column: if add column fails may mean that column already esists; means you are not using an exact version spagobi DB",
					e);
		}
		logger.debug("OUT");
	}

}

