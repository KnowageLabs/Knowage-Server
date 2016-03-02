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
package it.eng.spagobi.dataset.cache.impl.sqldbcache.test;

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.configuration.FileCreatorConfiguration;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.dataset.cache.test.FakeDatamartRetriever;
import it.eng.spagobi.dataset.cache.test.TestConstants;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.FlatDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.ScriptDataSet;
import it.eng.spagobi.tools.datasource.bo.DataSource;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public abstract class AbstractSQLDBCacheTest extends TestCase {
	
	protected JDBCDataSet sqlDataset;
	protected QbeDataSet qbeDataset;
	protected FileDataSet fileDataset;
	protected FlatDataSet flatDataset;
	protected ScriptDataSet scriptDataset;
	protected DataSource dataSourceReading;
	protected DataSource dataSourceWriting;
	
	static private Logger logger = Logger.getLogger(AbstractSQLDBCacheTest.class);
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("AF_CONFIG_FILE", TestConstants.AF_CONFIG_FILE);
    	ConfigSingleton.setConfigurationCreation( new FileCreatorConfiguration( TestConstants.WEBCONTENT_PATH ) );

    	TenantManager.setTenant(new Tenant("SPAGOBI"));

		//Creating DataSources and DataSets
		this.createDataSources();
		this.createDatasets();
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		//clean 
	}
	
	/*
	* ----------------------------------------------------
	* Test cases
	* ----------------------------------------------------
	*/

	
	
	/*
	* ----------------------------------------------------
	* Initialization Methods
	* ----------------------------------------------------
	*/
	
	public void createDataSources(){
		//Must be overridden by specific implementation
		//dataSourceReading = TestDataSourceFactory.createDataSource(TestConstants.DatabaseType.MYSQL, false);
		//dataSourceWriting = TestDataSourceFactory.createDataSource(TestConstants.DatabaseType.MYSQL, true);
		logger.error("Specific DataSource must be specified in specialized Test");
	}
	
	public JDBCDataSet createJDBCDataset(){
		//Create JDBCDataSet
		sqlDataset = new JDBCDataSet();
		sqlDataset.setQuery("select * from customer");
		sqlDataset.setQueryScript("");
		sqlDataset.setQueryScriptLanguage("");
		sqlDataset.setDataSource(dataSourceReading);
		sqlDataset.setLabel("test_jdbcDataset");
		return sqlDataset;
	}
	
	public FileDataSet createFileDataset(){
		fileDataset = new FileDataSet();

		try {
			fileDataset.setFileType("CSV");
			JSONObject jsonConf = new JSONObject();
			jsonConf.put("fileType", "CSV");
			jsonConf.put("fileName", "customers.csv");
			jsonConf.put("csvDelimiter", ",");
			jsonConf.put("csvDelimiter", ",");
			jsonConf.put("csvQuote", "\"");
			jsonConf.put("csvEncoding", "UTF-8");
			jsonConf.put("DS_SCOPE", "USER");	
			fileDataset.setDsMetadata("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><META version=\"1\"><COLUMNLIST><COLUMN alias=\"customer_id\" fieldType=\"ATTRIBUTE\" name=\"customer_id\" type=\"java.lang.Integer\"/><COLUMN alias=\"lname\" fieldType=\"ATTRIBUTE\" name=\"lname\" type=\"java.lang.String\"/><COLUMN alias=\"fname\" fieldType=\"ATTRIBUTE\" name=\"fname\" type=\"java.lang.String\"/><COLUMN alias=\"num_children_at_home\" fieldType=\"ATTRIBUTE\" name=\"num_children_at_home\" type=\"java.lang.Integer\"/></COLUMNLIST><DATASET><PROPERTY name=\"resultNumber\" value=\"49\"/> </DATASET></META>");			
			fileDataset.setConfiguration(jsonConf.toString());
			fileDataset.setResourcePath(TestConstants.RESOURCE_PATH);
			fileDataset.setFileName("customers.csv");
			fileDataset.setLabel("test_fileDataset");

		}
		catch(JSONException e){
			logger.error("JSONException when creating a FileDataset");
		}
		return fileDataset;


	}
	
	public QbeDataSet createQbeDataset(){
		qbeDataset = new QbeDataSet();
		qbeDataset.setJsonQuery("{\"catalogue\": {\"queries\": [{\"id\":\"q1390389018208\",\"distinct\":false,\"isNestedExpression\":false,\"fields\":[{\"alias\":\"Lname\",\"visible\":true,\"include\":true,\"type\":\"datamartField\",\"id\":\"it.eng.spagobi.meta.Customer:lname\",\"entity\":\"Customer\",\"field\":\"Lname\",\"longDescription\":\"Customer : Lname\",\"group\":\"\",\"funct\":\"NONE\",\"iconCls\":\"attribute\",\"nature\":\"attribute\"},{\"alias\":\"Fname\",\"visible\":true,\"include\":true,\"type\":\"datamartField\",\"id\":\"it.eng.spagobi.meta.Customer:fname\",\"entity\":\"Customer\",\"field\":\"Fname\",\"longDescription\":\"Customer : Fname\",\"group\":\"\",\"funct\":\"NONE\",\"iconCls\":\"attribute\",\"nature\":\"attribute\"},{\"alias\":\"City\",\"visible\":true,\"include\":true,\"type\":\"datamartField\",\"id\":\"it.eng.spagobi.meta.Customer:city\",\"entity\":\"Customer\",\"field\":\"City\",\"longDescription\":\"Customer : City\",\"group\":\"true\",\"funct\":\"NONE\",\"iconCls\":\"attribute\",\"nature\":\"attribute\"}],\"filters\":[],\"expression\":{},\"havings\":[],\"subqueries\":[]}]}, \t\"version\":7,\t\"generator\": \"SpagoBIMeta\" }\t");
		qbeDataset.setResourcePath(TestConstants.RESOURCE_PATH);
		qbeDataset.setDatamarts("MyModel41");
		qbeDataset.setDataSource(dataSourceReading);
		qbeDataset.setDataSourceForWriting(dataSourceWriting);
		Map params = new HashMap();
		FakeDatamartRetriever fakeDatamartRetriever = new FakeDatamartRetriever();
		fakeDatamartRetriever.setResourcePath(TestConstants.RESOURCE_PATH);
		params.put(SpagoBIConstants.DATAMART_RETRIEVER, fakeDatamartRetriever);
		qbeDataset.setParamsMap(params);
		qbeDataset.setLabel("test_qbeDataset");
		return qbeDataset;
	}
	
	public FlatDataSet createFlatDataset(){
		flatDataset = new FlatDataSet();
		flatDataset.setDataSource(dataSourceReading);
		flatDataset.setTableName("department"); //name of the table corresponding to the flat dataset (persisted dataset)
		flatDataset.setLabel("test_flatDataset");
		return flatDataset;
	}
	
	public ScriptDataSet createScriptDataSet(){
		scriptDataset=new ScriptDataSet();
		scriptDataset.setScriptLanguage("groovy");
		scriptDataset.setScript("returnValue(new Double(5).toString());\n");
		scriptDataset.setLabel("test_scriptDataset");
		return scriptDataset;
	}
	
	public void createDatasets() throws JSONException{
		createJDBCDataset();
		createFileDataset();
		createQbeDataset();
		createFlatDataset();
		createScriptDataSet();

	}
}
