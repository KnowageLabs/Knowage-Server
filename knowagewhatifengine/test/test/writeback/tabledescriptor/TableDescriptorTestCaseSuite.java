/**
 * 
 */
package test.writeback.tabledescriptor;

import test.writeback.TestConstants;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author ghedin
 *
 */
public class TableDescriptorTestCaseSuite extends TestCase {
	static public Test suite() {
		TestSuite suite = new TestSuite("TableDescriptorTestCaseSuite");
		if(TestConstants.enableTestsOnMySql){
			suite.addTestSuite(MySqlTestCase.class);
		}
		if(TestConstants.enableTestsOnPostgres){
			suite.addTestSuite(OracleTestCase.class);
		}

		return suite;
	}
}
