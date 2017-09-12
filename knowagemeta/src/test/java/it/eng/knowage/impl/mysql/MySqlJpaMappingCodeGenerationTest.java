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
import it.eng.knowage.meta.generator.jpamapping.JpaMappingCodeGenerator;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable;
import it.eng.knowage.meta.generator.jpamapping.wrappers.impl.JpaModel;
import it.eng.knowage.meta.generator.utils.StringUtils;
import it.eng.knowage.meta.initializer.descriptor.BusinessViewInnerJoinRelationshipDescriptor;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalTable;
import it.eng.knowage.utils.ModelManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;

public class MySqlJpaMappingCodeGenerationTest extends AbstractKnowageMetaTest {

	static JpaMappingCodeGenerator jpaMappingCodeGenerator;
	static JpaModel jpaModel;

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
				setRootModel(TestModelFactory.createModel(dbType));
			}

			if (jpaMappingCodeGenerator == null) {
				jpaMappingCodeGenerator = TestGeneratorFactory.createCodeGenerator();
				jpaModel = new JpaModel(businessModel);
				generator = jpaMappingCodeGenerator;
			}
		} catch (Exception t) {
			System.err.println("An unespected error occurred during setUp: ");
			t.printStackTrace();
			throw t;
		}
	}

	public void testGeneratorState() {
		File templateFolder = jpaMappingCodeGenerator.getTemplateDir();
		assertNotNull("bin folder cannot be null", templateFolder);
		assertTrue("src folder [" + templateFolder + "] does not exist", templateFolder.exists());
		assertTrue("src folder [" + templateFolder + "] is a file not a folder as expected", templateFolder.isDirectory());
		assertTrue("src folder [" + templateFolder + "] cannot be read", templateFolder.canRead());
		assertTrue("src folder [" + templateFolder + "] cannot be write", templateFolder.canWrite());
	}

	public void testGenerationSmoke() {
		jpaMappingCodeGenerator.generate(businessModel, TestConstants.outputFolder.toString());
	}

	public void testSrcFolderExistence() {
		File srcFolder = jpaMappingCodeGenerator.getSrcDir();
		assertNotNull("src folder cannot be null", srcFolder);
		assertTrue("src folder [" + srcFolder + "] does not exist", srcFolder.exists());
		assertTrue("src folder [" + srcFolder + "] is a file not a folder as expected", srcFolder.isDirectory());
		assertTrue("src folder [" + srcFolder + "] cannot be read", srcFolder.canRead());
		assertTrue("src folder [" + srcFolder + "] cannot be write", srcFolder.canWrite());
	}

	public void testMeatInfFolderExistence() {
		File metainfDir = new File(jpaMappingCodeGenerator.getSrcDir(), "META-INF");
		assertTrue("Impossible to find META-INF folder in [" + jpaMappingCodeGenerator.getSrcDir() + "]", metainfDir.exists());
	}

	public void testPersistenceFileExistence() {
		File metainfDir = new File(jpaMappingCodeGenerator.getSrcDir(), "META-INF");
		File persistenceFile = new File(metainfDir, "persistence.xml");
		assertTrue("Impossible to find persistence.xml file in [" + metainfDir + "]", persistenceFile.exists());
	}

	public void testLabelFileExistence() {
		File labelsFile = new File(jpaMappingCodeGenerator.getSrcDir(), "label.properties");
		assertTrue("Impossible to find labels.properties file in [" + jpaMappingCodeGenerator.getSrcDir() + "]", labelsFile.exists());
	}

	public void testQbePropertiesFileExistence() {
		File propertiesFile = new File(jpaMappingCodeGenerator.getSrcDir(), "qbe.properties");
		assertTrue("Impossible to find qbe.properties file in [" + jpaMappingCodeGenerator.getSrcDir() + "]", propertiesFile.exists());
	}

	public void testViewFileExistence() {
		File viewFile = new File(jpaMappingCodeGenerator.getSrcDir(), "views.json");
		assertTrue("Impossible to find view.json file in folder [" + jpaMappingCodeGenerator.getSrcDir() + "]", viewFile.exists());
	}

	public void testJavaFilesExistence() {
		for (IJpaTable jpaTable : jpaModel.getTables()) {
			File outputDir = new File(jpaMappingCodeGenerator.getSrcDir(), StringUtils.strReplaceAll(jpaTable.getPackage(), ".", "/"));
			Assert.assertTrue("Output folder [" + outputDir + "] for table [" + jpaTable.getName() + "] does not exist", outputDir.exists());
			File outputFile = new File(outputDir, jpaTable.getClassName() + ".java");
			Assert.assertTrue("Output file [" + outputFile + "] for table [" + jpaTable.getName() + "] does not exist", outputFile.exists());
		}
	}

	public void testPersistenceUnitBaseContent() {
		String fileContents;

		fileContents = null;
		try {
			File metainfDir = new File(jpaMappingCodeGenerator.getSrcDir(), "META-INF");
			File persistenceFile = new File(metainfDir, "persistence.xml");
			InputStream in = new FileInputStream(persistenceFile);
			fileContents = StringUtils.getStringFromStream(in);
			in.close();
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}

		assertNotNull(fileContents);
		assertTrue(fileContents.contains("<provider>org.hibernate.ejb.HibernatePersistence</provider>"));

		// assertTrue(fileContents.contains("<class>it.eng.knowage.meta.Employee</class>"));
		// assertTrue(fileContents.contains("<class>it.eng.knowage.meta.Position</class>"));

		assertTrue(fileContents.contains("<class>it.eng.knowage.meta.Class_Object</class>"));
		assertTrue(fileContents.contains("<class>it.eng.knowage.meta.Product_class</class>"));

		assertTrue(fileContents.contains("<property name=\"eclipselink.session.customizer\" value=\"it.eng.qbe.datasource.jpa.JNDICustomizer\"/>"));
	}

	public void testLabelFileContent() {
		Properties properties = new Properties();
		File labelsFile = new File(jpaMappingCodeGenerator.getSrcDir(), "label.properties");
		try {
			FileInputStream in = new FileInputStream(labelsFile);
			properties.load(in);
			in.close();
		} catch (Throwable e) {
			fail(e.getMessage());
		}

		for (Object key : properties.keySet()) {
			String value = properties.getProperty(key.toString());
			Assert.assertFalse("The value [" + value + "] of property [" + key.toString() + "] contains char $", value.contains("$"));
		}
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

		jpaMappingCodeGenerator = TestGeneratorFactory.createCodeGenerator();
		generator = jpaMappingCodeGenerator;
		jpaMappingCodeGenerator.generate(filteredBusinessModel, TestConstants.outputFolder.toString());
	}

}
