/**
 * 
 */
package test;

import java.io.File;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author ghedin
 * 
 */
public class DbConfigContainer {

	public static String getMySqlCatalogue() {

		File userDir = new File("").getAbsoluteFile();
		File f = new File(userDir, "\\test\\test\\writeback\\resources\\FoodMartMySQL.xml");
		return f.getAbsolutePath();
	}

	public static String getOracleCatalogue() {

		File userDir = new File("").getAbsoluteFile();
		File f = new File(userDir, "\\test\\test\\writeback\\resources\\FoodMartOracleSQLTest.xml");
		return f.getAbsolutePath();
	}

	public static String getHSQLCatalogue() {

		File userDir = new File("").getAbsoluteFile();
		File f = new File(userDir, "\\test\\test\\writeback\\resources\\FoodMartHSQL.xml");
		return f.getAbsolutePath();
	}

	public static String getMySqlTemplate() {

		File userDir = new File("").getAbsoluteFile();
		File f = new File(userDir, "\\test\\test\\writeback\\resources\\tpl.xml");
		return f.getAbsolutePath();
	}

	public static String getOracleTemplate() {

		File userDir = new File("").getAbsoluteFile();
		File f = new File(userDir, "\\test\\test\\writeback\\resources\\tpl_oracle.xml");
		return f.getAbsolutePath();
	}

	public static String getHSQLTemplate() {

		File userDir = new File("").getAbsoluteFile();
		File f = new File(userDir, "\\test\\test\\writeback\\resources\\tpl_hsql.xml");
		return f.getAbsolutePath();
	}

}
