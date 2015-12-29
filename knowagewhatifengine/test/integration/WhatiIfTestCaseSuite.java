/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author ghedin
 *
 */
package integration;

import integration.agorithms.AbstractWhatIfInMemoryTestCase;
import integration.agorithms.DefaultWeightedAllocationAlgorithmTestCase;
import integration.agorithms.EqualPartitioningOnLeafsAllocationAlgorithmTestCase;
import integration.agorithms.LeafsNodesTestCase;
import integration.output.WhatIfExportResult;
import integration.versions.VersionManagerTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import db.HSQLDBEnviromentSingleton;

public class WhatiIfTestCaseSuite extends AbstractWhatIfInMemoryTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		HSQLDBEnviromentSingleton.getInstance().startDB();
		HSQLDBEnviromentSingleton.getInstance().setLeaveOpen(true);
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		HSQLDBEnviromentSingleton.getInstance().closeDB();
		HSQLDBEnviromentSingleton.getInstance().setLeaveOpen(false);
	}

	static public Test suite() {
		TestSuite suite = new TestSuite("Db writeback tests");

		suite.addTestSuite(DefaultWeightedAllocationAlgorithmTestCase.class);
		suite.addTestSuite(LeafsNodesTestCase.class);
		suite.addTestSuite(EqualPartitioningOnLeafsAllocationAlgorithmTestCase.class);
		suite.addTestSuite(WhatIfExportResult.class);
		suite.addTestSuite(VersionManagerTestCase.class);
		return suite;
	}

}
