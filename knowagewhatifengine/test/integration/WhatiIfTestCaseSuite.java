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
