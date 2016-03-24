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
package it.eng.knowage.impl.mysql;

import it.eng.knowage.common.TestConstants;
import it.eng.knowage.initializer.AbstractKnowageMetaTest;
import it.eng.knowage.initializer.TestModelFactory;
import it.eng.knowage.meta.generator.jpamapping.wrappers.JpaProperties;
import it.eng.knowage.meta.initializer.properties.BusinessModelPropertiesFromFileInitializer;
import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

public class MySQLBusinessModelInizializationTest extends AbstractKnowageMetaTest {

	public MySQLBusinessModelInizializationTest() {
		this.setName("Busienss model initialization tests on MySql");
	}

	@Override
	public void setUp() throws Exception {
		try {
			// if this is the first test on postgres after the execution
			// of tests on an other database force a tearDown to clean
			// and regenerate properly all the static variables contained in
			// parent class AbstractSpagoBIMetaTest
			// if (dbType != TestConstants.DatabaseType.MYSQL) {
			// doTearDown();
			// }
			super.setUp();

			if (dbType == null)
				dbType = TestConstants.DatabaseType.MYSQL;

			if (rootModel == null) {
				setRootModel(TestModelFactory.createModel(dbType));
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
		assertNotNull("Metamodel cannot be null", rootModel);
	}

	@Override
	public void testPhysicalModelInitializationSmoke() {
		assertTrue("Metamodel must have one physical model ", rootModel.getPhysicalModels().size() == 1);
	}

	@Override
	public void testBusinessModelInitializationSmoke() {
		assertTrue("Metamodel must have one business model ", rootModel.getBusinessModels().size() == 1);
	}

	// =======================================================
	// PROPERTIES
	// =======================================================

	public void testBusinessModelPakageProperty() {
		ModelProperty packageProperty = businessModel.getProperties().get(JpaProperties.MODEL_PACKAGE);
		assertNotNull("Property [" + JpaProperties.MODEL_PACKAGE + "] is not defined in business model", packageProperty);

		String packageName = null;
		// check if property is set, else get default value
		if (packageProperty.getValue() != null) {
			packageName = packageProperty.getValue();
		} else {
			packageName = packageProperty.getPropertyType().getDefaultValue();
		}
		Assert.assertNotNull("Property [" + JpaProperties.MODEL_PACKAGE + "] have no value", packageName);

		ModelProperty initializerNameProperty = businessModel.getProperties().get(BusinessModelPropertiesFromFileInitializer.MODEL_INITIALIZER_NAME);
		assertNotNull("Property [" + BusinessModelPropertiesFromFileInitializer.MODEL_INITIALIZER_NAME + "] is not defined in business model",
				initializerNameProperty);
		assertEquals(this.businessModelInitializer.INITIALIZER_NAME, initializerNameProperty.getValue());

		ModelProperty initializerVersionProperty = businessModel.getProperties().get(BusinessModelPropertiesFromFileInitializer.MODEL_INITIALIZER_VERSION);
		assertNotNull("Property [" + BusinessModelPropertiesFromFileInitializer.MODEL_INITIALIZER_VERSION + "] is not defined in business model",
				initializerVersionProperty);
		assertEquals(this.businessModelInitializer.INITIALIZER_VERSION, initializerVersionProperty.getValue());

	}

	// =======================================================
	// TABLES
	// =======================================================

	public void testBusinessModelTables() {

		Assert.assertEquals(TestConstants.MYSQL_TABLE_NAMES.length, businessModel.getTables().size());

		for (int i = 0; i < TestConstants.MYSQL_TABLE_NAMES.length; i++) {
			List<BusinessTable> businessTables = businessModel.getBusinessTableByPhysicalTable(TestConstants.MYSQL_TABLE_NAMES[i]);
			Assert.assertNotNull(businessTables);
			Assert.assertFalse("Business model does not contain table [" + TestConstants.MYSQL_TABLE_NAMES[i] + "]", businessTables.size() == 0);
			Assert.assertFalse("Business model contains table [" + TestConstants.MYSQL_TABLE_NAMES[i] + "] more than one time", businessTables.size() > 1);
		}
	}

	public void testBusinessModelTableUniqueNames() {
		Map<String, String> tableUniqueNames = new HashMap<String, String>();

		for (BusinessTable table : businessModel.getBusinessTables()) {
			String tableUniqueName = table.getUniqueName();
			String physicalTableName = table.getPhysicalTable().getName();

			Assert.assertNotNull("Business table associated with physical table [" + physicalTableName + "] have no unique name", tableUniqueName);
			Assert.assertNotNull("Business table associated with physical table [" + physicalTableName + "] have an empty unique name", physicalTableName
					.trim().length() > 0);

			char firstChar = tableUniqueName.charAt(0);
			Assert.assertTrue("The unique name [" + tableUniqueName + "] of business table associated with physical table [" + physicalTableName
					+ "] does not start with a letter", Character.isLetter(firstChar));

			Assert.assertFalse("Unique name [" + tableUniqueName + "] of table [" + physicalTableName + "] is equal to unique name of table ["
					+ tableUniqueNames.get(tableUniqueName.toLowerCase()) + "]", tableUniqueNames.containsKey(tableUniqueName.toLowerCase()));

			// note: name must be unique in a case unsensitive way: !name1.equalsIgnoreCase(name2)
			tableUniqueNames.put(tableUniqueName.toLowerCase(), physicalTableName);
		}
	}

	public void testBusinessModelTableNames() {
		for (BusinessTable table : businessModel.getBusinessTables()) {
			String tableUniqueName = table.getName();
			Assert.assertNotNull("Business table associated with physical table [" + table.getPhysicalTable().getName() + "] have no name", tableUniqueName);
		}
	}

	// =======================================================
	// COLUMNS
	// =======================================================

	public void testBusinessModelColumnUniqueNames() {
		for (BusinessTable table : businessModel.getBusinessTables()) {
			String physicalTableName = table.getPhysicalTable().getName();
			Map<String, String> columnUniqueNames = new HashMap<String, String>();
			for (SimpleBusinessColumn column : table.getSimpleBusinessColumns()) {
				String columnUniqueName = column.getUniqueName();
				String physicalColumnName = column.getPhysicalColumn().getName();

				Assert.assertNotNull("Business column associated to column [" + physicalColumnName + "] of physical table [" + physicalTableName
						+ "] have no  unique name", columnUniqueName);
				Assert.assertFalse("Column [" + physicalColumnName + "] and column [" + columnUniqueNames.get(columnUniqueName) + "] of table ["
						+ physicalTableName + "] have the same unique name [" + columnUniqueName + "]", columnUniqueNames.containsKey(columnUniqueName));

				columnUniqueNames.put(columnUniqueName, physicalColumnName);
			}
		}
	}

	public void testBusinessModelColumnNames() {
		for (BusinessTable table : businessModel.getBusinessTables()) {
			String physicalTableName = table.getPhysicalTable().getName();
			for (SimpleBusinessColumn column : table.getSimpleBusinessColumns()) {
				String columnName = column.getName();
				Assert.assertNotNull("Business column associated with physical column [" + column.getPhysicalColumn().getName() + "] of table ["
						+ physicalTableName + "] have no name ", columnName);
			}
		}
	}

	// =======================================================
	// IDENTIFIERS
	// =======================================================

	// TODO

	// =======================================================
	// RELATIONSHIPS
	// =======================================================

	// TODO
}
