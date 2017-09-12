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
import it.eng.knowage.initializer.TestModelFactory;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.serialization.AbstractModelSerializationTest;
import it.eng.knowage.serialization.EmfXmiSerializer;
import it.eng.knowage.serialization.IModelSerializer;

import java.io.File;

import org.junit.Assert;

public class MySqlBusinessModelSerializationTest extends AbstractModelSerializationTest {

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

	public void testSerializationSmoke() {
		File modelFile = new File(TestConstants.outputFolder, "emfmodel.xmi");
		if (modelFile.exists())
			modelFile.delete();
		try {
			IModelSerializer serializer = new EmfXmiSerializer();
			serializer.serialize(rootModel, modelFile);
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}

		Assert.assertTrue(modelFile.exists());
	}

	public void testDeserializationSmoke() {
		Model model = null;
		File modelFile = new File(TestConstants.outputFolder, "emfmodel.xmi");
		try {
			IModelSerializer serializer = new EmfXmiSerializer();
			model = serializer.deserialize(modelFile);
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}

		Assert.assertNotNull(model);
		Assert.assertNotNull(model.getBusinessModels());
		Assert.assertEquals(1, model.getPhysicalModels().size());
		Assert.assertEquals(1, model.getBusinessModels().size());
	}

}
