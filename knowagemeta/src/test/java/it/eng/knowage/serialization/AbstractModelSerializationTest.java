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
