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

public class TransformerFrom2_0_0To2_1_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom2_0_0To2_1_0.class);
	
	public byte[] transform(byte[] content, String pathImpTmpFolder, String archiveName) {
		logger.debug("IN");
		try {
			TransformersUtilities.decompressArchive(pathImpTmpFolder, archiveName, content);
		} catch(Exception e) {
			logger.error("Error while unzipping 2.0.0 exported archive", e);	
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
			fixDatasets(conn);
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
	 * Adjust domains: in some SpagoBI 2.0.0 installations there were a hibernate dialect with name 'HQL' instead of 'HSQL'.
	 * Furthermore, hibernate dialects' value code was 'HSQL', 'MySQL', 'ORACLE' ... instead of the actual hibernate dialect java class
	 * 
	 * @param conn The jdbc connection to export database
	 * @throws Exception
	 */
	private void fixDomains(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql =  "UPDATE SBI_DOMAINS SET VALUE_CD = 'HSQL', VALUE_NM = 'HSQL' WHERE VALUE_CD = 'HQL' AND DOMAIN_CD = 'DIALECT_HIB'";
		stmt.executeUpdate(sql);
		sql =  "UPDATE SBI_DOMAINS SET VALUE_CD = '-1' WHERE VALUE_CD = 'DEFAULT' AND DOMAIN_CD = 'DIALECT_HIB'";
		stmt.executeUpdate(sql);
		sql =  "UPDATE SBI_DOMAINS SET VALUE_CD = 'org.hibernate.dialect.OracleDialect' WHERE VALUE_CD = 'ORACLE' AND DOMAIN_CD = 'DIALECT_HIB'";
		stmt.executeUpdate(sql);
		sql =  "UPDATE SBI_DOMAINS SET VALUE_CD = 'org.hibernate.dialect.Oracle9Dialect' WHERE VALUE_CD = 'ORACLE 9i/10g' AND DOMAIN_CD = 'DIALECT_HIB'";
		stmt.executeUpdate(sql);
		sql =  "UPDATE SBI_DOMAINS SET VALUE_CD = 'org.hibernate.dialect.SQLServerDialect' WHERE VALUE_CD = 'SQLSERVER' AND DOMAIN_CD = 'DIALECT_HIB'";
		stmt.executeUpdate(sql);
		sql =  "UPDATE SBI_DOMAINS SET VALUE_CD = 'org.hibernate.dialect.HSQLDialect' WHERE VALUE_CD = 'HSQL' AND DOMAIN_CD = 'DIALECT_HIB'";
		stmt.executeUpdate(sql);
		sql =  "UPDATE SBI_DOMAINS SET VALUE_CD = 'org.hibernate.dialect.MySQLInnoDBDialect' WHERE VALUE_CD = 'MYSQL' AND DOMAIN_CD = 'DIALECT_HIB'";
		stmt.executeUpdate(sql);
		sql =  "UPDATE SBI_DOMAINS SET VALUE_CD = 'org.hibernate.dialect.PostgreSQLDialect' WHERE VALUE_CD = 'POSTGRESQL' AND DOMAIN_CD = 'DIALECT_HIB'";
		stmt.executeUpdate(sql);
		sql =  "UPDATE SBI_DOMAINS SET VALUE_CD = 'org.hibernate.dialect.IngresDialect' WHERE VALUE_CD = 'INGRES' AND DOMAIN_CD = 'DIALECT_HIB'";
		stmt.executeUpdate(sql);
		logger.debug("OUT");
	}
	
	/*
	 * Add columns NUM_ROWS and LANGUAGE_SCRIPT to export database.
	 * 
	 * @param conn The jdbc connection to export database
	 * @throws Exception
	 */
	private void fixDatasets(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql =  "ALTER TABLE SBI_DATA_SET ADD COLUMN NUM_ROWS BOOLEAN DEFAULT FALSE";
		stmt.execute(sql);
		sql =  "UPDATE SBI_DATA_SET SET NUM_ROWS=FALSE";
		stmt.executeUpdate(sql);
		sql =  "ALTER TABLE SBI_DATA_SET ADD COLUMN LANGUAGE_SCRIPT VARCHAR(50) DEFAULT NULL";
		stmt.execute(sql);
		sql =  "UPDATE SBI_DATA_SET SET LANGUAGE_SCRIPT = 'groovy' where OBJECT_TYPE = 'SbiScriptDataSet'";
		stmt.executeUpdate(sql);
		logger.debug("OUT");
	}

}
