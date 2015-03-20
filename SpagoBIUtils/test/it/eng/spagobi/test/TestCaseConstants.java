/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.test;

import java.io.File;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class TestCaseConstants {
	public static String CONNECTION_DIALECT = "org.hibernate.dialect.MySQLDialect";
	public static String CONNECTION_DRIVER = "com.mysql.jdbc.Driver";
	public static String CONNECTION_URL = "jdbc:mysql://localhost:3306/foodmart";
	public static String CONNECTION_USER = "root";
	public static String CONNECTION_PWD = "mysql";
	
	public static final File TEST_FOLDER = new File("test-resources");
	public static final File TEST_OUPUT_ROOT_FOLDER = new File(TEST_FOLDER, "outs");
}
