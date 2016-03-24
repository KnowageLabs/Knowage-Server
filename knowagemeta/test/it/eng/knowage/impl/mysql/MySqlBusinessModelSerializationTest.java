/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.

 **/
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
