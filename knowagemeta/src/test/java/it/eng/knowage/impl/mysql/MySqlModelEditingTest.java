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
import it.eng.knowage.edit.AbstractModelEditingTest;
import it.eng.knowage.initializer.TestModelFactory;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalTable;

import java.util.List;

import org.junit.Assert;

public class MySqlModelEditingTest extends AbstractModelEditingTest {

	@Override
	public void setUp() throws Exception {
		super.setUp();
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

			if (rootModel == null) {
				rootModel = TestModelFactory.createModel(dbType);
				if (rootModel != null && rootModel.getPhysicalModels() != null && rootModel.getPhysicalModels().size() > 0) {
					physicalModel = rootModel.getPhysicalModels().get(0);
				}
				if (rootModel != null && rootModel.getBusinessModels() != null && rootModel.getBusinessModels().size() > 0) {
					businessModel = rootModel.getBusinessModels().get(0);
				}
			}
		} catch (Exception t) {
			System.err.println("An unespected error occurred during setUp: ");
			t.printStackTrace();
			throw t;
		}
	}

	@Override
	public void testModelInitializationSmoke() {
		super.testModelInitializationSmoke();
	}

	@Override
	public void testPhysicalModelInitializationSmoke() {
		super.testPhysicalModelInitializationSmoke();
	}

	@Override
	public void testBusinessModelInitializationSmoke() {
		super.testBusinessModelInitializationSmoke();
	}

	public void testBusinessTableGetByPhysicalTable() {
		PhysicalTable physicalTable = physicalModel.getTable("currency");
		List<BusinessTable> businessTables = businessModel.getBusinessTableByPhysicalTable(physicalTable);
		Assert.assertNotNull(businessTables);
		Assert.assertEquals(1, businessTables.size());
		BusinessTable businessTable = businessTables.get(0);

		Assert.assertNotNull(businessTable);
	}

	public void testBusinessColumnGetByPhysicalColumn() {
		PhysicalTable physicalTable = physicalModel.getTable("currency");
		BusinessTable businessTable = businessModel.getBusinessTableByPhysicalTable(physicalTable).get(0);

		PhysicalColumn physicalColumn = physicalTable.getColumn("currency");
		List<SimpleBusinessColumn> businessColumns = businessTable.getSimpleBusinessColumnsByPhysicalColumn(physicalColumn);
		Assert.assertNotNull(businessColumns);
		Assert.assertEquals(1, businessColumns.size());

		BusinessColumn businessColumn = businessColumns.get(0);
		Assert.assertNotNull(businessColumn);
		Assert.assertTrue(businessTable.getColumns().contains(businessColumn));
	}

	public void testBusinessColumnGetByIndex() {
		PhysicalTable physicalTable = physicalModel.getTable("currency");
		BusinessTable businessTable = businessModel.getBusinessTableByPhysicalTable(physicalTable).get(0);

		PhysicalColumn physicalColumn = physicalTable.getColumn("currency");
		BusinessColumn businessColumn = businessTable.getSimpleBusinessColumnsByPhysicalColumn(physicalColumn).get(0);

		int columnIndex = businessTable.getColumns().indexOf(businessColumn);
		Assert.assertTrue(columnIndex != -1);
		BusinessColumn column = businessTable.getColumns().get(columnIndex);
		Assert.assertNotNull(column);
		Assert.assertTrue(column.equals(businessColumn));
		Assert.assertTrue(column == businessColumn);
	}

	public void testBusinessColumnGetByName() {
		PhysicalTable physicalTable = physicalModel.getTable("currency");
		BusinessTable businessTable = businessModel.getBusinessTableByPhysicalTable(physicalTable).get(0);

		PhysicalColumn physicalColumn = physicalTable.getColumn("currency");
		BusinessColumn businessColumn = businessTable.getSimpleBusinessColumnsByPhysicalColumn(physicalColumn).get(0);

		String businessColumnUniqueName = businessColumn.getUniqueName();
		Assert.assertNotNull(businessColumnUniqueName);
		BusinessColumn column = businessTable.getSimpleBusinessColumnByUniqueName(businessColumnUniqueName);
		Assert.assertNotNull(column);
		Assert.assertTrue(column.equals(businessColumn));
		Assert.assertTrue(column == businessColumn);
	}

	public void testBusinessColumnDeletion() {
		PhysicalTable physicalTable = physicalModel.getTable("currency");
		PhysicalColumn physicalColumn = physicalTable.getColumn("currency");

		BusinessTable businessTable = businessModel.getBusinessTableByPhysicalTable(physicalTable).get(0);
		BusinessColumn businessColumn = businessTable.getSimpleBusinessColumnsByPhysicalColumn(physicalColumn).get(0);

		String businessColumnUniqueName = businessColumn.getUniqueName();

		Assert.assertEquals(4, businessTable.getColumns().size());
		Assert.assertTrue(businessTable.getColumns().remove(businessColumn));
		Assert.assertEquals(3, businessTable.getColumns().size());
		Assert.assertNotNull(businessTable.getSimpleBusinessColumnsByPhysicalColumn(physicalColumn));
		Assert.assertEquals(0, businessTable.getSimpleBusinessColumnsByPhysicalColumn(physicalColumn).size());
		Assert.assertNull(businessTable.getSimpleBusinessColumnByUniqueName(businessColumnUniqueName));
	}

}
