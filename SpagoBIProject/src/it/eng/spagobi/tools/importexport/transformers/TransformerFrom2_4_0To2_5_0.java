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

public class TransformerFrom2_4_0To2_5_0 implements ITransformer {

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
			fixDatasets(conn);
			fixKpiModel(conn);
			fixModelInstance(conn);
			fixThresholdValue(conn);
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
	private void fixKpiModel(Connection conn) throws Exception {
		logger.debug("IN");

		Statement stmt = conn.createStatement();
		String sql =  "";
		// ALTER TABLE SBI_OBJECT_NOTES ADD COLUMN OWNER VARCHAR(50);
		try{
			sql =  "ALTER TABLE SBI_KPI_MODEL ADD COLUMN KPI_MODEL_LBL VARCHAR(1000)";
			stmt.execute(sql);
		}
		catch (Exception e) {
			logger.error("Error adding column: if add column fails may mean that column already esists; means you ar enot using an exact version spagobi DB",e);	
		}
		//sql =  "UPDATE SBI_KPI_MODEL SET KPI_MODEL_LBL=KPI_MODEL_CD WHERE KPI_MODEL_LBL IS NULL";
		//sql =  "UPDATE SBI_KPI_MODEL SET KPI_MODEL_LBL=KPI_MODEL_CD";
		//stmt.executeUpdate(sql);

		logger.debug("OUT");
	}



	/*
	 * Adjust ModelInstance Table
	 * 
	 * @param conn The jdbc connection to export database
	 * @throws Exception
	 */
	private void fixModelInstance(Connection conn) throws Exception {
		logger.debug("IN");

		Statement stmt = conn.createStatement();

		// ALTER TABLE SBI_DATA_SET ADD COLUMN DS_METADATA VARCHAR(2000) DEFAULT NULL;
		String sql =  "";
		try{
			sql = "ALTER TABLE SBI_KPI_MODEL_INST ADD COLUMN MODELUUID VARCHAR(400) DEFAULT NULL;";
			stmt.execute(sql);
		}
		catch (Exception e) {
			logger.error("Error adding column: if add column fails may mean that column already esists; means you ar enot using an exact version spagobi DB",e);	
		}

		sql =  "UPDATE SBI_KPI_MODEL_INST SET MODELUUID=NULL";
		stmt.executeUpdate(sql);
		logger.debug("OUT");
	}

	/*
	 * Adjust DataSet Table
	 * 
	 * @param conn The jdbc connection to export database
	 * @throws Exception
	 */
	private void fixDatasets(Connection conn) {
		logger.debug("IN");
		try {
			Statement stmt = conn.createStatement();

			// ALTER TABLE SBI_DATA_SET ADD COLUMN DS_METADATA VARCHAR(2000) DEFAULT NULL;
			String sql =  "";
			try{
				sql = "ALTER TABLE SBI_DATA_SET ADD COLUMN DS_METADATA VARCHAR(2000) DEFAULT NULL;";
				stmt.execute(sql);
			}
			catch (Exception e) {
				logger.error("Error adding column: if add column fails may mean that column already esists; means you ar enot using an exact version spagobi DB",e);	
			}

			sql =  "UPDATE SBI_DATA_SET SET DS_METADATA=NULL";
			stmt.executeUpdate(sql);

		} catch (SQLException e) {
			logger.error("Already existent column");
			e.printStackTrace();
		}

		logger.debug("OUT");
	}


	/*
	 * Adjust Threshold Value Table
	 * 
	 * @param conn The jdbc connection to export database
	 * @throws Exception
	 */
	private void fixThresholdValue(Connection conn) throws Exception {
		logger.debug("IN");
		Statement stmt = conn.createStatement();

		String sql =  "";
		try{
			sql = "ALTER TABLE SBI_THRESHOLD_VALUE ADD COLUMN MIN_CLOSED BOOLEAN DEFAULT FALSE";
			stmt.execute(sql);

			sql =  "UPDATE SBI_THRESHOLD_VALUE SET MIN_CLOSED=FALSE";
			stmt.executeUpdate(sql);

			sql =  "ALTER TABLE SBI_THRESHOLD_VALUE ADD COLUMN MAX_CLOSED BOOLEAN DEFAULT FALSE";
			stmt.execute(sql);
			sql =  "UPDATE SBI_THRESHOLD_VALUE SET MAX_CLOSED=FALSE";
			stmt.executeUpdate(sql);

			sql =  "ALTER TABLE SBI_THRESHOLD_VALUE ADD COLUMN TH_VALUE BOOLEAN DEFAULT NULL";
			stmt.execute(sql);
			sql =  "UPDATE SBI_THRESHOLD_VALUE SET TH_VALUE=FALSE";
			stmt.executeUpdate(sql);
		}
		catch (Exception e) {
			logger.error("Error adding column: if add column fails may mean that column already esists; means you ar enot using an exact version spagobi DB",e);	
		}

		logger.debug("OUT");
	}


}
