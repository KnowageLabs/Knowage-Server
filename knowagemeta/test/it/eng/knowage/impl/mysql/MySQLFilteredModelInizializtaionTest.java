/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.

 **/
package it.eng.knowage.impl.mysql;

import it.eng.knowage.common.TestConstants;
import it.eng.knowage.initializer.AbstractKnowageMetaTest;
import it.eng.knowage.initializer.TestModelFactory;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.physical.PhysicalTable;

import java.util.List;

import org.junit.Assert;

public class MySQLFilteredModelInizializtaionTest extends AbstractKnowageMetaTest {

	@Override
	public void setUp() throws Exception {

		try {
			// if this is the first test on postgres after the execution
			// of tests on an other database force a tearDown to clean
			// and regenerate properly all the static variables contained in
			// parent class AbstractSpagoBIMetaTest
			if (dbType != TestConstants.DatabaseType.MYSQL) {
				doTearDown();
			}
			super.setUp();

			if (dbType == null)
				dbType = TestConstants.DatabaseType.MYSQL;

			if (filteredModel == null) {
				setFilteredModel(TestModelFactory.createFilteredModel(dbType));
			}
		} catch (Exception t) {
			System.err.println("An unespected error occurred during setUp: ");
			t.printStackTrace();
			throw t;
		}
	}

	// add specific test here...

	@Override
	public void testModelInitializationSmoke() {
		assertNotNull("Metamodel cannot be null", filteredModel);
	}

	@Override
	public void testPhysicalModelInitializationSmoke() {
		assertTrue("Metamodel must have one physical model ", filteredModel.getPhysicalModels().size() == 1);
	}

	@Override
	public void testBusinessModelInitializationSmoke() {
		assertTrue("Metamodel must have one business model ", filteredModel.getBusinessModels().size() == 1);
	}

	// =======================================================
	// TABLES
	// =======================================================

	public void testPhysicalModelTables() {

		Assert.assertEquals(TestConstants.MYSQL_FILTERED_TABLES_FOR_PMODEL.length, filteredPhysicalModel.getTables().size());

		for (int i = 0; i < TestConstants.MYSQL_FILTERED_TABLES_FOR_PMODEL.length; i++) {
			PhysicalTable physicalTable = filteredPhysicalModel.getTable(TestConstants.MYSQL_FILTERED_TABLES_FOR_PMODEL[i]);
			Assert.assertNotNull(physicalTable);
		}
	}

	public void testBusinessModelTables() {

		Assert.assertEquals(TestConstants.MYSQL_FILTERED_TABLES_FOR_BMODEL.length, filteredBusinessModel.getTables().size());

		for (int i = 0; i < TestConstants.MYSQL_FILTERED_TABLES_FOR_BMODEL.length; i++) {
			List<BusinessTable> businessTables = businessModel.getBusinessTableByPhysicalTable(TestConstants.MYSQL_FILTERED_TABLES_FOR_BMODEL[i]);
			Assert.assertNotNull(businessTables);
			Assert.assertFalse("Business model does not contain table [" + TestConstants.MYSQL_TABLE_NAMES[i] + "]", businessTables.size() == 0);
			Assert.assertFalse("Business model contains table [" + TestConstants.MYSQL_TABLE_NAMES[i] + "] more than one time", businessTables.size() > 1);
		}
	}

	// =======================================================
	// COLUMNS
	// =======================================================

	// =======================================================
	// IDENTIFIERS
	// =======================================================

	// =======================================================
	// RELATIONSHIPS
	// =======================================================
}
