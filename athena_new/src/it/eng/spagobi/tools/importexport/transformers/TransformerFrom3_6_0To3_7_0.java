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

public class TransformerFrom3_6_0To3_7_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom3_6_0To3_7_0.class);

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
			fixAnalyticaDrivers(conn);
			fixParuses(conn);
			fixModels(conn);
			
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
	
	private void fixParuses(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {
			sql = "ALTER TABLE SBI_PARUSE ADD COLUMN DEFAULT_LOV_ID INTEGER NULL;";
			stmt.execute(sql);
			sql = "ALTER TABLE SBI_PARUSE ADD COLUMN DEFAULT_FORMULA VARCHAR(4000) NULL;";
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			logger.error(
					"Error adding column: if add column fails may mean that column already esists; means you are not using an exact version spagobi DB",
					e);
		}
		logger.debug("OUT");
		
	}

	private void fixAnalyticaDrivers(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {				
			sql = "UPDATE sbi_obj_par SET MULT_FL=0;";
			stmt.execute(sql);
			
			sql = "UPDATE sbi_obj_par SET MULT_FL=1 WHERE par_id IN (SELECT a.par_id FROM   sbi_parameters a, sbi_paruse m  WHERE a.par_id = m.par_id and selection_type = 'CHECK_LIST');";
			stmt.executeUpdate(sql);
			
			sql = "UPDATE sbi_paruse SET selection_type='LOOKUP' WHERE selection_type = 'CHECK_LIST'OR selection_type = 'LIST'";
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			logger.error(
					"Error adding column: if add column fails may mean that column already esists; means you are not using an exact version spagobi DB",
					e);
		}
		logger.debug("OUT");
	}
	
	
	private void fixModels(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();
		String sql = "";
		try {				
			sql = "CREATE MEMORY TABLE SBI_META_MODELS ("+
					" ID                   INTEGER NOT NULL,"+
					" NAME                 VARCHAR(100) NOT NULL,"+
					" DESCR                VARCHAR(500) NULL,"+
					" USER_IN              VARCHAR(100) NOT NULL,"+
					" USER_UP              VARCHAR(100),"+
					" USER_DE              VARCHAR(100),"+
					" TIME_IN              TIMESTAMP NOT NULL,"+
					" TIME_UP              TIMESTAMP DEFAULT NULL,"+
					" TIME_DE              TIMESTAMP DEFAULT NULL,"+
					" SBI_VERSION_IN       VARCHAR(10),"+
					" SBI_VERSION_UP       VARCHAR(10),"+
					" SBI_VERSION_DE       VARCHAR(10),"+
					" META_VERSION         VARCHAR(100),"+
					" ORGANIZATION         VARCHAR(20),"+
					" CONSTRAINT XAK1SBI_META_MODELS UNIQUE (NAME, ORGANIZATION),"+
					" PRIMARY KEY (ID))";
			stmt.execute(sql);
		} catch (Exception e) {
			logger.error(
					"Error creating table SBI_META_MODELS in export DB",
					e);
		}
		
		stmt = conn.createStatement();
		sql = "";
		try {				
			sql = "CREATE MEMORY TABLE SBI_META_MODELS_VERSIONS ("+
        " ID                   INTEGER NOT NULL,"+
					" MODEL_ID             INTEGER NOT NULL,"+
					" CONTENT              LONGVARBINARY NOT NULL,"+
					" NAME                 VARCHAR(100),  "+
					" PROG                 INTEGER,"+
					" CREATION_DATE        TIMESTAMP DEFAULT NULL,"+
					" CREATION_USER        VARCHAR(50) NOT NULL, "+
					" ACTIVE               BOOLEAN, "+
					" USER_IN              VARCHAR(100) NOT NULL,"+
					" USER_UP              VARCHAR(100),"+
					" USER_DE              VARCHAR(100),"+
					" TIME_IN              TIMESTAMP NOT NULL,"+
					" TIME_UP              TIMESTAMP DEFAULT NULL,"+
					" TIME_DE              TIMESTAMP DEFAULT NULL,"+
					" SBI_VERSION_IN       VARCHAR(10),"+
					" SBI_VERSION_UP       VARCHAR(10),"+
					" SBI_VERSION_DE       VARCHAR(10),"+
					" META_VERSION         VARCHAR(100),"+
					" ORGANIZATION         VARCHAR(20),"+
					" PRIMARY KEY (ID))";
			stmt.execute(sql);
		} catch (Exception e) {
			logger.error(
					"Error creating table TABLE SBI_META_MODELS_VERSIONS in export DB",
					e);
		}
		
		stmt = conn.createStatement();
		sql = "";
		try {				
			sql = "CREATE MEMORY TABLE SBI_ARTIFACTS ("+
					" ID                   INTEGER NOT NULL,"+
					" NAME                 VARCHAR(100) NOT NULL,"+
					" DESCR                VARCHAR(500) NULL,"+
					" TYPE                 VARCHAR(50) NULL,"+
					" USER_IN              VARCHAR(100) NOT NULL,"+
					" USER_UP              VARCHAR(100),"+
					" USER_DE              VARCHAR(100),"+
					"  TIME_IN              TIMESTAMP NOT NULL,"+
					"  TIME_UP              TIMESTAMP DEFAULT NULL,"+
					" TIME_DE              TIMESTAMP DEFAULT NULL,"+
					" SBI_VERSION_IN       VARCHAR(10),"+
					" SBI_VERSION_UP       VARCHAR(10),"+
					" SBI_VERSION_DE       VARCHAR(10),"+
					" META_VERSION         VARCHAR(100),"+
					" ORGANIZATION         VARCHAR(20),"+
					" CONSTRAINT XAK1SBI_ARTIFACTS UNIQUE (NAME, TYPE, ORGANIZATION),"+	
					"  PRIMARY KEY (ID))";
			stmt.execute(sql);
		} catch (Exception e) {
			logger.error(
					"Error creating table TABLE SBI_ARTIFACTS in export DB",
					e);
		}
		
		stmt = conn.createStatement();
		sql = "";
		try {				
			sql = "CREATE MEMORY TABLE SBI_ARTIFACTS_VERSIONS ("+
					" ID                 	INTEGER NOT NULL,"+
					" ARTIFACT_ID          INTEGER NOT NULL,"+
					" CONTENT              LONGVARBINARY NOT NULL,"+
					" NAME                 VARCHAR(100),  "+
					" PROG                 INTEGER,"+
					" CREATION_DATE        TIMESTAMP DEFAULT NULL,"+
					" CREATION_USER        VARCHAR(50) NOT NULL, "+
					" ACTIVE               BOOLEAN, "+
					" USER_IN              VARCHAR(100) NOT NULL,"+
					" USER_UP              VARCHAR(100),"+
					" USER_DE              VARCHAR(100),"+
					" TIME_IN              TIMESTAMP NOT NULL,"+
					" TIME_UP              TIMESTAMP DEFAULT NULL,"+
					" TIME_DE              TIMESTAMP DEFAULT NULL,"+
					" SBI_VERSION_IN       VARCHAR(10),"+
					" SBI_VERSION_UP       VARCHAR(10),"+
					" SBI_VERSION_DE       VARCHAR(10),"+
					" META_VERSION         VARCHAR(100),"+
					" ORGANIZATION         VARCHAR(20),"+
					" PRIMARY KEY (ID))";

			stmt.execute(sql);
		} catch (Exception e) {
			logger.error(
					"Error creating table TABLE SBI_ARTIFACTS in export DB",
					e);
		}
		
		logger.debug("OUT");
	}
	
	

}

