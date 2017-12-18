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
import it.eng.knowage.meta.generator.jpamapping.JpaMappingClassesGenerator;

import java.io.File;

public class MySqlJpaMappingClassesGenerationTest extends AbstractKnowageMetaTest {

	static JpaMappingClassesGenerator jpaMappingClassesGenerator;

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

			if (jpaMappingClassesGenerator == null) {
				jpaMappingClassesGenerator = TestGeneratorFactory.createClassesGenerator();
				generator = jpaMappingClassesGenerator;
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
		File binFolder = jpaMappingClassesGenerator.getBinDir();
		assertNotNull("bin folder cannot be null", binFolder);
		assertTrue("bin folder [" + binFolder + "] does not exist", binFolder.exists());
		assertTrue("bin folder [" + binFolder + "] is a file not a folder as expected", binFolder.isDirectory());
		assertTrue("bin folder [" + binFolder + "] cannot be read", binFolder.canRead());
		assertTrue("bin folder [" + binFolder + "] cannot be write", binFolder.canWrite());
	}

	public void testMeatInfFolderExistence() {
		File metainfDir = new File(jpaMappingClassesGenerator.getBinDir(), "META-INF");
		assertTrue("Impossible to find META-INF folder in [" + jpaMappingClassesGenerator.getBinDir() + "]", metainfDir.exists());
	}

	public void testPersistenceFileExistence() {
		File metainfDir = new File(jpaMappingClassesGenerator.getBinDir(), "META-INF");
		File persistenceFile = new File(metainfDir, "persistence.xml");
		assertTrue("Impossible to find persistence.xml file in [" + metainfDir + "]", persistenceFile.exists());
	}

	public void testLabelFileExistence() {
		File labelsFile = new File(jpaMappingClassesGenerator.getBinDir(), "label.properties");
		assertTrue("Impossible to find labels.properties file in [" + jpaMappingClassesGenerator.getBinDir() + "]", labelsFile.exists());
	}

	public void testQbePropertiesFileExistence() {
		File propertiesFile = new File(jpaMappingClassesGenerator.getBinDir(), "qbe.properties");
		assertTrue("Impossible to find qbe.properties file in [" + jpaMappingClassesGenerator.getBinDir() + "]", propertiesFile.exists());
	}

	public void testViewFileExistence() {
		File viewFile = new File(jpaMappingClassesGenerator.getBinDir(), "views.json");
		assertTrue("Impossible to find view.json file in folder [" + jpaMappingClassesGenerator.getBinDir() + "]", viewFile.exists());
	}
}
