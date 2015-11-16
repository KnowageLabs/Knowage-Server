/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.engines.georeport.features.provider;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.xmlbeans.impl.tool.XSTCTester.TestCase;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class FeaturesProviderTestSuite extends TestCase {
	static public Test suite() {
		TestSuite suite = new TestSuite("Features provider tests");
		suite.addTestSuite(FeaturesProviderDAOFileImplTest.class);
		suite.addTestSuite(FeaturesProviderDAOWFSImplTest.class);
		return suite;
	}
}