/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.jpa.keys;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;



/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class KeyJpaDataSourceTestSuite extends TestCase {
	static public Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(NoKeyJpaDataSourceTestCase.class);
		suite.addTestSuite(SimpleKeyJpaDataSourceTestCase.class);
		suite.addTestSuite(CompKeyJpaDataSourceTestCase.class);
		//suite.addTestSuite(RelNoKeyJpaDataSourceTestCase.class);
		suite.addTestSuite(RelSimpleKeyJpaDataSourceTestCase.class);
		//suite.addTestSuite(RelCompKeyJpaDataSourceTestCase.class);
		return suite;
	}
}
