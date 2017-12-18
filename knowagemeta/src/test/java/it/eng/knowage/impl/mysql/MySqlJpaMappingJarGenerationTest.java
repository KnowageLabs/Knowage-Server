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
import it.eng.knowage.generator.TestGeneratorFactory;
import it.eng.knowage.initializer.AbstractKnowageMetaTest;
import it.eng.knowage.initializer.TestModelFactory;
import it.eng.knowage.meta.generator.jpamapping.JpaMappingJarGenerator;
import it.eng.knowage.meta.initializer.BusinessModelInitializer;
import it.eng.knowage.meta.initializer.descriptor.BusinessViewInnerJoinRelationshipDescriptor;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalTable;
import it.eng.knowage.utils.ModelManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class MySqlJpaMappingJarGenerationTest extends AbstractKnowageMetaTest {

	static JpaMappingJarGenerator jpaMappingJarGenerator;

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

			if (rootModel == null) {
				setRootModel(TestModelFactory.createModel(dbType));
			}

			if (jpaMappingJarGenerator == null) {
				jpaMappingJarGenerator = TestGeneratorFactory.createJarGenerator();
				generator = jpaMappingJarGenerator;
			}
		} catch (Exception t) {
			System.err.println("An unespected error occurred during setUp: ");
			t.printStackTrace();
			throw t;
		}
	}

	public void testGenerationSmoke() {
		generator.generate(businessModel, TestConstants.outputFolder.toString());
	}

	public void testBinFolderExistence() {
		File distFolder = jpaMappingJarGenerator.getDistDir();
		assertNotNull("dist folder cannot be null", distFolder);
		assertTrue("dist folder [" + distFolder + "] does not exist", distFolder.exists());
		assertTrue("dist folder [" + distFolder + "] is a file not a folder as expected", distFolder.isDirectory());
		assertTrue("dist folder [" + distFolder + "] cannot be read", distFolder.canRead());
		assertTrue("dist folder [" + distFolder + "] cannot be write", distFolder.canWrite());
	}

	public void testJarFileExistence() {
		File jarFile = jpaMappingJarGenerator.getJarFile();
		assertTrue("Impossible to find datamart.jar file in [" + jarFile.getParent() + "]", jarFile.exists());
	}

	public void testJarFileContent() {
		try {
			JarFile jarFile = new JarFile(jpaMappingJarGenerator.getJarFile());
			assertNotNull("Impossible to find file persistence.xml in jar file [" + jpaMappingJarGenerator.getJarFile() + "]",
					jarFile.getJarEntry("META-INF/persistence.xml"));
			assertNotNull("Impossible to find file labels.properties in jar file [" + jpaMappingJarGenerator.getJarFile() + "]",
					jarFile.getJarEntry("label.properties"));
			assertNotNull("Impossible to find file qbe.properties in jar file [" + jpaMappingJarGenerator.getJarFile() + "]",
					jarFile.getJarEntry("qbe.properties"));
			assertNotNull("Impossible to find file views.json in jar file [" + jpaMappingJarGenerator.getJarFile() + "]", jarFile.getJarEntry("views.json"));

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test the model generation when to the business model the same physical table is added more than one time.
	 *
	 * @see BusinessModelInitializer row 374 for identifier problems
	 * @see BusinessModelInitializer row 492 for relationship problems
	 */
	public void testDoubleImportedTableModelGeneration() {
		ModelManager modelManager = new ModelManager(rootModel);
		PhysicalTable physicalTable = physicalModel.getTable("currency");
		modelManager.addBusinessTable(physicalTable);
		modelManager.addBusinessTable(physicalTable);

		List<BusinessTable> businessTables = businessModel.getBusinessTableByPhysicalTable(physicalTable);
		assertEquals(3, businessTables.size());

		BusinessTable businessTable1 = businessTables.get(0);
		BusinessTable businessTable2 = businessTables.get(1);
		BusinessTable businessTable3 = businessTables.get(2);

		assertTrue(businessTable1.getName() != businessTable2.getName() && businessTable1.getName() != businessTable3.getName());
		assertTrue(businessTable2.getName() != businessTable3.getName() && businessTable2.getName() != businessTable1.getName());
		assertTrue(businessTable3.getName() != businessTable2.getName() && businessTable3.getName() != businessTable1.getName());

		assertTrue(businessTable1.getUniqueName() != businessTable2.getUniqueName() && businessTable1.getUniqueName() != businessTable3.getUniqueName());
		assertTrue(businessTable2.getUniqueName() != businessTable3.getUniqueName() && businessTable2.getUniqueName() != businessTable1.getUniqueName());
		assertTrue(businessTable3.getUniqueName() != businessTable1.getUniqueName() && businessTable3.getUniqueName() != businessTable2.getUniqueName());

		assertTrue(!businessTable1.equals(businessTable2) && !businessTable1.equals(businessTable3));
		assertTrue(!businessTable2.equals(businessTable3) && !businessTable2.equals(businessTable1));
		assertTrue(!businessTable3.equals(businessTable1) && !businessTable3.equals(businessTable2));

		generator.generate(businessModel, TestConstants.outputFolder.toString());
	}

	// =============================================
	// TESTS ON VIEW MODEL
	// =============================================
	public void testViewGenerationSmoke() {
		setFilteredModel(TestModelFactory.createFilteredModel(dbType, "VIEW_MODEL_TEST"));

		// create view here....
		ModelManager modelManager = new ModelManager(filteredModel);
		PhysicalTable source = filteredPhysicalModel.getTable("product");
		PhysicalTable destination = filteredPhysicalModel.getTable("product_class");
		BusinessTable businessTable = filteredBusinessModel.getBusinessTableByPhysicalTable(source).get(0);

		List<PhysicalColumn> sourceCol = new ArrayList<PhysicalColumn>();
		sourceCol.add(source.getColumn("product_class_id"));
		List<PhysicalColumn> destinationCol = new ArrayList<PhysicalColumn>();
		destinationCol.add(destination.getColumn("product_class_id"));
		int cardinality = 0;
		String relationshipName = "inner_join_test";
		BusinessViewInnerJoinRelationshipDescriptor innerJoinRelationshipDescriptor = new BusinessViewInnerJoinRelationshipDescriptor(source, destination,
				sourceCol, destinationCol, cardinality, relationshipName);

		BusinessView businessView = modelManager.createView(businessTable, innerJoinRelationshipDescriptor);
		modelManager.addBusinessColumn(destination.getColumn("product_family"), businessView);

		jpaMappingJarGenerator = TestGeneratorFactory.createJarGenerator();
		generator = jpaMappingJarGenerator;
		jpaMappingJarGenerator.generate(filteredBusinessModel, TestConstants.outputFolder.toString());
	}
}
