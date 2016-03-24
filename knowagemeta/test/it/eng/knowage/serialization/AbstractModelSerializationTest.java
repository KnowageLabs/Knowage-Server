/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.knowage.serialization;

import it.eng.knowage.initializer.AbstractKnowageMetaTest;

public class AbstractModelSerializationTest extends AbstractKnowageMetaTest {

	public AbstractModelSerializationTest() {
		super();
	}

	// add generic tests related to model here ...

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
}
