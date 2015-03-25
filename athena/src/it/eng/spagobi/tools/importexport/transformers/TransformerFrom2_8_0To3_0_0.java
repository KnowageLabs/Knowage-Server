/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport.transformers;

import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.tools.importexport.ITransformer;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class TransformerFrom2_8_0To3_0_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom2_8_0To3_0_0.class);

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
			fixPermissionOnFolderDomains(conn);
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

	/**
	 * Since SpagoBI 3.0.0, domains for document STATE (DEV, TEST, REL and SUSP) are different from 
	 * permission on folders (DEVELOPMENT, TEST, EXECUTION, CREATION)
	 * @param conn The connection to the exported database
	 * @throws Exception
	 */
	private void fixPermissionOnFolderDomains(Connection conn) throws Exception {
		logger.debug("IN");

		int maxId = getDomainsMaxId(conn);

		String[] sqls = {
				"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'DEVELOPMENT', 'sbidomains.permissiononfolder.nm.dev','PERMISSION_ON_FOLDER','Permission on folder','sbidomains.permissiononfolder.ds.dev');",
				"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'TEST', 'sbidomains.permissiononfolder.nm.test','PERMISSION_ON_FOLDER','Permission on folder','sbidomains.permissiononfolder.ds.test');",
				"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'EXECUTION', 'sbidomains.permissiononfolder.nm.execute','PERMISSION_ON_FOLDER','Permission on folder','sbidomains.permissiononfolder.ds.execute');",
				"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'CREATION', 'sbidomains.permissiononfolder.nm.create','PERMISSION_ON_FOLDER','Permission on folder','sbidomains.permissiononfolder.ds.create');",
				"UPDATE SBI_FUNC_ROLE set STATE_ID = (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'DEVELOPMENT' AND DOMAIN_CD = 'PERMISSION_ON_FOLDER'), STATE_CD = 'DEVELOPMENT' WHERE STATE_ID = (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'DEV' AND DOMAIN_CD = 'STATE');",
				"UPDATE SBI_FUNC_ROLE set STATE_ID = (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'TEST' AND DOMAIN_CD = 'PERMISSION_ON_FOLDER'), STATE_CD = 'TEST' WHERE STATE_ID = (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'TEST' AND DOMAIN_CD = 'STATE');",
				"UPDATE SBI_FUNC_ROLE set STATE_ID = (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'EXECUTION' AND DOMAIN_CD = 'PERMISSION_ON_FOLDER'), STATE_CD = 'EXECUTION' WHERE STATE_ID = (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'REL' AND DOMAIN_CD = 'STATE');"
		};
		executeSQL(conn, sqls);

		logger.debug("OUT");
	}

	private int getDomainsMaxId(Connection conn) throws Exception {
		logger.debug("IN");
		Statement statement = null;
		ResultSet resultSet = null;
		int toReturn = 0;
		try {
			statement = conn.createStatement();
			String sql = "SELECT MAX(VALUE_ID) FROM SBI_DOMAINS";
			resultSet = statement.executeQuery(sql);
			if (resultSet.next()) {
				toReturn = resultSet.getInt(1);
			} else {
				throw new Exception("Query SELECT MAX(VALUE_ID) FROM SBI_DOMAINS did not get any result!!!!");
			} 
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	private void executeSQL(Connection conn, String[] sqls) throws Exception {
		logger.debug("IN");
		for (int i = 0; i < sqls.length; i++) {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sqls[i]);
		}
		logger.debug("OUT");
	}


}
