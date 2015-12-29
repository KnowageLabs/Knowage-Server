/**
 * 
 */
package test;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.engines.whatif.WhatIfEngine;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.utilities.engines.EngineConstants;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author ghedin
 *
 */
public abstract class AbstractWhatIfTestCase extends TestCase {

	
	private String catalog;
	
	public static final Double accurancy = 0.00001d;
	
	
	
	public WhatIfEngineInstance getWhatifengineiEngineInstance(String c){
		SourceBean template;
		try {
			catalog = c;
			template = SourceBean.fromXMLFile( getTemplate());
			return WhatIfEngine.createInstance(template, getEnv());
		} catch (SourceBeanException e) {
			e.printStackTrace();
			assertFalse(true);
		}
		return   null;
	}
	
	public Map getEnv() {
		

		
		
		Map env = new HashMap();
		env.put(EngineConstants.ENV_OLAP_SCHEMA, catalog);
		env.put(EngineConstants.ENV_LOCALE, Locale.ITALIAN);

		return env;
	}
	
	protected void executeQuery(String sql){
		try {
			java.sql.Connection  connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/foodmart_key?user=root&password=root");
			Statement statement = connection.createStatement();
			statement.executeUpdate(sql);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected ResultSet executeQuery(java.sql.Connection  connection, String sql){
		try {
			Statement statement = connection.createStatement();
			statement.execute(sql);
			return statement.getResultSet();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public abstract String getTemplate();
	
	
}
