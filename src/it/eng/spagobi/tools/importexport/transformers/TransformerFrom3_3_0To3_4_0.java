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

public class TransformerFrom3_3_0To3_4_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom3_3_0To3_4_0.class);

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
			fixExtRoles(conn);
			fixDataset(conn);
			fixDomains(conn);
			fixEngines(conn);
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
	 * Adjust ExtRoles Table
	 * 
	 * @param conn The jdbc connection to export database
	 * @throws Exception
	 */
	private void fixExtRoles(Connection conn) throws Exception {
		logger.debug("IN");

		Statement stmt = conn.createStatement();
		String sql =  "";
		try{
			sql =  "ALTER TABLE SBI_EXT_ROLES ADD COLUMN DO_MASSIVE_EXPORT BOOLEAN DEFAULT TRUE;";
			stmt.execute(sql);
		}
		catch (Exception e) {
			logger.error("Error adding column: if add column fails may mean that column already esists; means you ar enot using an exact version spagobi DB",e);	
		}

		logger.debug("OUT");
	}

	/**
	 *  This fix is referring to an update from version 3.1
	 *  Because of a bug in updating the scripts, it is needed that from 3.3 to 3.4 
	 *  the column CUSTOM_DATA is added,
	 *  if the exported version is lesser than 3.2 this fix will produce an error that is catched and traced, but the import goes on 
	 * @param conn
	 * @throws Exception
	 */
	private void fixDataset(Connection conn) throws Exception {
		logger.debug("IN");

		try{
			Statement stmt = conn.createStatement();

			String sql = "ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN CUSTOM_DATA VARCHAR DEFAULT NULL;";

			stmt.executeUpdate(sql);
		}
		catch (Exception e) {
			logger.warn("Could not add the table CUSTOM_DATA: this is just a bugfix from 3.3 to 3.4, if your exported version is lesser than 3.1 this is not needed");
		}

		logger.debug("OUT");
	}


	
	private void fixEngines(Connection conn) throws Exception {
		logger.debug("IN");	
		try{

			String[] engineUpdates = {	
					"update SBI_ENGINES set LABEL ='SpagoBIDashboardEng' , NAME ='Dashboard Engine'  WHERE LABEL = 'DashboardInternalEng'",
					"update SBI_ENGINES set LABEL ='SpagoBIJFreeChartEng' , NAME ='JFreeChart Engine'  WHERE LABEL = 'ChartInternalEng'",
					"update SBI_ENGINES set LABEL ='SpagoBIDossierEngine' , NAME ='Dossier Engine'  WHERE LABEL = 'DossierInternalEng'",
					"update SBI_ENGINES set LABEL ='SpagoBIOfficeEngine' , NAME ='Office Document Engine'  WHERE LABEL = 'OfficeInternalEng'",
					"update SBI_ENGINES set LABEL ='SpagoBICompositeDocE' , NAME ='Document Composition Engine'  WHERE LABEL = 'DocumentCompositionInternalEng'",
					"update SBI_ENGINES set LABEL ='SpagoBIKpiEngine' , NAME ='Kpi Engine'  WHERE LABEL = 'KpiEngine'",
					"update SBI_ENGINES set LABEL ='SpagoBIBirtReportEng' , NAME ='Birt Report Engine'  WHERE LABEL = 'BirtEngine'",
					"update SBI_ENGINES set LABEL ='SpagoBIGeoEngine' , NAME ='Geo Engine'  WHERE LABEL = 'GeoEngine'",
					"update SBI_ENGINES set LABEL ='SpagoBIJPivotEngine' , NAME ='JPivot Engine'  WHERE LABEL = 'JPivotEngine'",
					"update SBI_ENGINES set LABEL ='SpagoBIJasperReportE' , NAME ='Jasper Report Engine'  WHERE LABEL = 'JasperReportEngine'",
					"update SBI_ENGINES set LABEL ='SpagoBIQbeEngine' , NAME ='Qbe Engine'  WHERE LABEL = 'QbeEngine'",
					"update SBI_ENGINES set LABEL ='SpagoBITalendEngine' , NAME ='Talend Engine'  WHERE LABEL = 'TALEND'",
					"update SBI_ENGINES set LABEL ='SpagoBIWekaEngine' , NAME ='Weka Engine'  WHERE LABEL = 'WEKA_ENGINE'",
					"update SBI_ENGINES set LABEL ='SpagoBIAccessibleRep' , NAME ='Accessible Report Engine'  WHERE LABEL = 'AccessibilityEng'",
					"update SBI_ENGINES set LABEL ='SpagoBIProcessEngine' , NAME ='Process Engine'  WHERE LABEL = 'CommonJEngine'",
					"update SBI_ENGINES set LABEL ='SpagoBISmartFilterEn' , NAME ='Smart Filter Engine'  WHERE LABEL = 'SmartFilterEngine'",
					"update SBI_ENGINES set LABEL ='SpagoBIGisEngine' , NAME ='Gis Engine'  WHERE LABEL = 'GeoReportEngine'",
					"update SBI_ENGINES set LABEL ='SpagoBIConsoleEngine' , NAME ='Console Engine'  WHERE LABEL = 'ConsoleEngine'",
					"update SBI_ENGINES set LABEL ='SpagoBIWorksheetEng' , NAME ='Worksheet Engine'  WHERE LABEL = 'WorksheetEngine'",
					"update SBI_ENGINES set LABEL ='SpagoBIJSChartEngine' , NAME ='JSChart Engine'  WHERE LABEL = 'ChartExternalEngine'"
			};

			executeSQL(conn, engineUpdates);


		}
		catch (Exception e) {
			logger.error("Error in inserting new domains from previous version. ", e);
			throw e;
		}
		logger.debug("Insert new domains in 3.4");
		logger.debug("OUT");
	}
	
	

	private void fixDomains(Connection conn) throws Exception {
		logger.debug("IN");	
		try{
			int maxId = getDomainsMaxId(conn);

			String[] updates = {
					"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'MOBILE_REPORT', 'sbidomains.nm.mobile.report','BIOBJ_TYPE','BI Object type','sbidomains.ds.mobile.report');",
					"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'MOBILE_CHART', 'sbidomains.nm.mobile.chart','BIOBJ_TYPE','BI Object type','sbidomains.ds.mobile.chart');",
					"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'MOBILE_COCKPIT', 'sbidomains.nm.mobile.cockpit','BIOBJ_TYPE','BI Object type','sbidomains.ds.mobile.cockpit');",
					"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'CHART', 'sbidomains.nm.chart','BIOBJ_TYPE','BI Object type','sbidomains.ds.chart');"
			};

			executeSQL(conn, updates);	
		}
		catch (Exception e) {
			logger.error("Error in inserting new domains from previous version. ", e);
			throw e;
		}
		logger.debug("Insert new domains in 3.4");
		logger.debug("OUT");
	}



	/** In order to have import work, some domains have changed name: in particular
	 * @param conn
	 * @throws Exception
	 */
	/*
		private void fixDomains(Connection conn) throws Exception {
		logger.debug("IN");

		try{
			Statement stmt = conn.createStatement();

			// SOme domaiins have been changed
			logger.debug("change some domains name that have been changed in 3.4");

			String[] updates = {
					"update SBI_DOMAINS set VALUE_CD = 'FREE_INQUIRY'" +
					", VALUE_NM ='sbidomains.nm.freeinquiry', VALUE_DS = 'sbidomains.ds.freeinquiry'" +
					" where  VALUE_CD = 'DATAMART' AND DOMAIN_CD = 'BIOBJ_TYPE';",

					"update SBI_DOMAINS set VALUE_CD = 'ADHOC_REPORTING'" +
					", VALUE_NM ='sbidomains.nm.adhoc_reporting', VALUE_DS = 'sbidomains.ds.adhoc_reporting'" +
					" where  VALUE_CD = 'WORKSHEET' AND DOMAIN_CD = 'BIOBJ_TYPE';",

					"update SBI_DOMAINS set VALUE_CD = 'COCKPIT', VALUE_NM ='sbidomains.nm.cockpit', " +
					" VALUE_DS = 'sbidomains.ds.cockpit' " +
					"where  VALUE_CD = 'DOCUMENT_COMPOSITE' AND DOMAIN_CD = 'BIOBJ_TYPE';",

					"update SBI_DOMAINS set VALUE_CD = 'COLLABORATION', VALUE_NM ='sbidomains.nm.collaboration', " +
					"VALUE_DS = 'sbidomains.ds.collaboration' " +
					" where  VALUE_CD = 'DOSSIER' AND DOMAIN_CD = 'BIOBJ_TYPE';",

					"update SBI_DOMAINS set VALUE_CD = 'LOCATION_INTELLIGENCE', VALUE_NM ='sbidomains.nm.location_intelligence', " +
					"VALUE_DS = 'sbidomains.ds.location_intelligence' " +
					" where  VALUE_CD = 'MAP' AND DOMAIN_CD = 'BIOBJ_TYPE';",

					"update SBI_DOMAINS set VALUE_CD = 'EXTERNAL_PROCESS', VALUE_NM ='sbidomains.nm.external_process'" +
					" , VALUE_DS = 'sbidomains.ds.external_process' " +
					" where  VALUE_CD = 'PROCESS' AND DOMAIN_CD = 'BIOBJ_TYPE';"
			};

			executeSQL(conn, updates);	

			logger.debug("Insert new domains in 3.4");

			int maxId = getDomainsMaxId(conn);

			String[] sqls = {
					//"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'FREE_INQUIRY', 'sbidomains.nm.freeinquiry','BIOBJ_TYPE','BI Object type','sbidomains.ds.freeinquiry');",
					//"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'ADHOC_REPORTING', 'sbidomains.nm.adhoc_reporting','BIOBJ_TYPE','BI Object type','sbidomains.ds.adhoc_reporting');",
					"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'REAL_TIME', 'sbidomains.nm.realtime','BIOBJ_TYPE','BI Object type','sbidomains.ds.realtime');",
					//"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'LOCATION_INTELLIGENCE', 'sbidomains.nm.location_intelligence','BIOBJ_TYPE','BI Object type','sbidomains.ds.location_intelligence');",
					//"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'COCKPIT', 'sbidomains.nm.cockpit','BIOBJ_TYPE','BI Object type','sbidomains.ds.cockpit');",
					//"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'COLLABORATION', 'sbidomains.nm.collaboration','BIOBJ_TYPE','BI Object type','sbidomains.ds.collaboration');",
					//"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'EXTERNAL_PROCESS', 'sbidomains.nm.external_process','BIOBJ_TYPE','BI Object type','sbidomains.ds.external_process');",
					"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'MOBILE_REPORT', 'sbidomains.nm.mobile.report','BIOBJ_TYPE','BI Object type','sbidomains.ds.mobile.report');",
					"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'MOBILE_CHART', 'sbidomains.nm.mobile.chart','BIOBJ_TYPE','BI Object type','sbidomains.ds.mobile.chart');",
					"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'MOBILE_COCKPIT', 'sbidomains.nm.mobile.cockpit','BIOBJ_TYPE','BI Object type','sbidomains.ds.mobile.cockpit');",
					"INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM,VALUE_DS) values (" + ++maxId + ", 'CHART', 'sbidomains.nm.chart','BIOBJ_TYPE','BI Object type','sbidomains.ds.chart');"
			};
			executeSQL(conn, sqls);

			logger.debug("Change engine that have changed their domain type");

			String[] engineUpdates = {	
					"update SBI_ENGINES set BIOBJ_TYPE = (SELECT VALUE_ID FROM sbi_domains s WHERE VALUE_CD = 'CHART' AND DOMAIN_CD = 'BIOBJ_TYPE') WHERE CLASS_NM = 'it.eng.spagobi.engines.chart.SpagoBIChartInternalEngine'",
					"update SBI_ENGINES set BIOBJ_TYPE = (SELECT VALUE_ID FROM sbi_domains s WHERE VALUE_CD = 'REAL_TIME' AND DOMAIN_CD = 'BIOBJ_TYPE') WHERE CLASS_NM = 'it.eng.spagobi.engines.dashboard.SpagoBIDashboardInternalEngine'",
					"update SBI_ENGINES set BIOBJ_TYPE = (SELECT VALUE_ID FROM sbi_domains s WHERE VALUE_CD = 'REPORT' AND DOMAIN_CD = 'BIOBJ_TYPE') WHERE DRIVER_NM = 'it.eng.spagobi.engines.drivers.accessibility.AccessibilityDriver'",
					"update SBI_ENGINES set BIOBJ_TYPE = (SELECT VALUE_ID FROM sbi_domains s WHERE VALUE_CD = 'EXTERNAL_PROCESS' AND DOMAIN_CD = 'BIOBJ_TYPE') WHERE DRIVER_NM = 'it.eng.spagobi.engines.drivers.commonj.CommonjDriver'",
					"update SBI_ENGINES set BIOBJ_TYPE = (SELECT VALUE_ID FROM sbi_domains s WHERE VALUE_CD = 'REAL_TIME' AND DOMAIN_CD = 'BIOBJ_TYPE') WHERE DRIVER_NM = 'it.eng.spagobi.engines.drivers.generic.GenericDriver' AND MAIN_URL='/SpagoBIConsoleEngine/servlet/AdapterHTTP?ACTION_NAME=CONSOLE_ENGINE_START_ACTION'",
					"update SBI_ENGINES set BIOBJ_TYPE = (SELECT VALUE_ID FROM sbi_domains s WHERE VALUE_CD = 'FREE_INQUIRY' AND DOMAIN_CD = 'BIOBJ_TYPE') WHERE DRIVER_NM = 'it.eng.spagobi.engines.drivers.smartfilter.SmartFilterDriver'"
			};

			executeSQL(conn, engineUpdates);

			String[] objectUpdates = {	
					"update SBI_OBJECTS set BIOBJ_TYPE_CD = 'REAL_TIME' WHERE BIOBJ_TYPE_CD = 'DASH'",
					"update SBI_OBJECTS set BIOBJ_TYPE_CD = 'FREE_INQUIRY' WHERE BIOBJ_TYPE_CD = 'DATAMART'",
					"update SBI_OBJECTS set BIOBJ_TYPE_CD = 'ADHOC_REPORTING' WHERE BIOBJ_TYPE_CD = 'WORKSHEET'",
					"update SBI_OBJECTS set BIOBJ_TYPE_CD = 'LOCATION_INTELLIGENCE' WHERE BIOBJ_TYPE_CD = 'GEO'",
					"update SBI_OBJECTS set BIOBJ_TYPE_CD = 'COCKPIT' WHERE BIOBJ_TYPE_CD = 'DOCUMENT_COMPOSITE'",
					"update SBI_OBJECTS set BIOBJ_TYPE_CD = 'COLLABORATION' WHERE BIOBJ_TYPE_CD = 'DOSSIER'",
					"update SBI_OBJECTS set BIOBJ_TYPE_CD = 'LOCATION_INTELLIGENCE' WHERE BIOBJ_TYPE_CD = 'MAP'",
					"update SBI_OBJECTS set BIOBJ_TYPE_CD = 'EXTERNAL_PROCESS' WHERE BIOBJ_TYPE_CD = 'PROCESS'",
					"update SBI_OBJECTS set BIOBJ_TYPE_CD = 'REPORT' WHERE BIOBJ_TYPE_CD = 'ACCESSIBLE_HTML'",
					"update SBI_OBJECTS set BIOBJ_TYPE_CD = 'REAL_TIME' WHERE BIOBJ_TYPE_CD = 'CONSOLE'"

			};
			executeSQL(conn, objectUpdates);


		}
		catch (Exception e) {
			logger.error("Error in updating domains from previous version. ", e);
			throw e;
		}
		logger.debug("OUT");
	}
	 */

	private void executeSQL(Connection conn, String[] sqls) throws Exception {
		logger.debug("IN");
		for (int i = 0; i < sqls.length; i++) {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sqls[i]);
		}
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

}

