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

public class TransformerFrom4_1_0To4_2_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom4_1_0To4_2_0.class);

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

			//fixSbiOrganizations(conn);
			fixSbiAuthorizations(conn);
			
			fixSbiObjects(conn);

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
	
	
//	private void fixSbiOrganizations(Connection conn) throws Exception {
//		logger.debug("IN");
//		Statement stmt = conn.createStatement();
//		String sql = "";
//		try {
//			sql = "ALTER TABLE SBI_ORGANIZATIONS ADD COLUMN THEME VARCHAR(100) DEFAULT 'SPAGOBI.THEMES.THEME.default';";
//			stmt.executeUpdate(sql);
//		
//		} catch (Exception e) {
//			logger.error(
//					"Error in altering sbi_organization",
//					e);
//		}
//		logger.debug("OUT");
//	}
	
	
	private void fixSbiObjects(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			
			sql = "ALTER TABLE SBI_OBJECTS DROP COLUMN IS_PUBLIC";
			stmt.executeUpdate(sql);
		
		} catch (Exception e) {
			logger.error(
					"Error in altering SBI_OBJECTS",
					e);
		}
		
		logger.debug("OUT");
		
	}

	private void fixSbiAuthorizations(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "CREATE MEMORY TABLE SBI_AUTHORIZATIONS ( "
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "NAME varchar(200) DEFAULT NULL, "
					+ "USER_IN varchar(100) NOT NULL, "
					+ "USER_UP varchar(100) DEFAULT NULL, "
					+ "USER_DE varchar(100) DEFAULT NULL, "
					+ "TIME_IN timestamp DEFAULT NULL, "
					+ "TIME_UP timestamp  DEFAULT NULL, "
					+ "TIME_DE timestamp  DEFAULT NULL, "
					+ "SBI_VERSION_IN varchar(10) DEFAULT NULL,"
					+ "SBI_VERSION_UP varchar(10) DEFAULT NULL, "
					+ "SBI_VERSION_DE varchar(10) DEFAULT NULL, "
					+ "META_VERSION varchar(100) DEFAULT NULL,  "
					+ "ORGANIZATION varchar(20) DEFAULT NULL);";
			stmt.executeUpdate(sql);
			
			sql = "CREATE MEMORY  TABLE SBI_AUTHORIZATIONS_ROLES ( "
					+ "AUTHORIZATION_ID INTEGER NOT NULL, "
					+ "ROLE_ID INTEGER NOT NULL, "
					+ "USER_IN varchar(100) NOT NULL, "
					+ "USER_UP varchar(100) DEFAULT NULL, "
					+ "USER_DE varchar(100) DEFAULT NULL, "
					+ "TIME_IN timestamp  DEFAULT current_timestamp NOT NULL, "
					+ "TIME_UP timestamp  DEFAULT NULL, "
					+ "TIME_DE timestamp  DEFAULT NULL, "
					+ "SBI_VERSION_IN varchar(10) DEFAULT NULL, "
					+ "SBI_VERSION_UP varchar(10) DEFAULT NULL, "
					+ "SBI_VERSION_DE varchar(10) DEFAULT NULL, "
					+ "META_VERSION varchar(100) DEFAULT NULL, "
					+ "ORGANIZATION varchar(20) DEFAULT NULL); ";
			stmt.executeUpdate(sql);

		
		} catch (Exception e) {
			logger.error(
					"Error in creationg sbi authorizations and sbi authorizations roles",
					e);
		}
		logger.debug("OUT");
	}
	


}

