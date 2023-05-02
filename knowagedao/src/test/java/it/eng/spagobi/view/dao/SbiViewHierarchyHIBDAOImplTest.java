/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.view.dao;

import static it.eng.spagobi.commons.utilities.UtilitiesDAOForTest.resetUserProfile;
import static it.eng.spagobi.commons.utilities.UtilitiesDAOForTest.setUpUserProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.UtilitiesForTest;
import it.eng.spagobi.commons.utilities.UtilitiesDAOForTest;
import it.eng.spagobi.view.metadata.SbiViewHierarchy;

/**
 * @author Marco Libanori
 * @since KNOWAGE_TM-513
 */
public class SbiViewHierarchyHIBDAOImplTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
		UtilitiesForTest.setUpMasterConfiguration();
		UtilitiesDAOForTest.setUpDatabaseTestJNDI();
	}

	@Before
	public void setUpProfileManager() throws EMFInternalError {
		setUpUserProfile();
	}

	@After
	public void resetProfileManager() {
		resetUserProfile();
	}

	@Test
	public void create() {

		ISbiViewHierarchyDAO dao = new SbiViewHierarchyHIBDAOImpl();

		SbiViewHierarchy e = new SbiViewHierarchy();

		e.setName("name");
		e.setDescr("descr");
		e.setProgr(0);

		dao.create(e);

		dao.delete(e);
	}

	@Test
	public void createParentAndChild() {

		ISbiViewHierarchyDAO dao = new SbiViewHierarchyHIBDAOImpl();

		SbiViewHierarchy parent = new SbiViewHierarchy();

		parent.setName("Parent");
		parent.setDescr("Parent description");
		parent.setProgr(0);

		dao.create(parent);

		SbiViewHierarchy child = new SbiViewHierarchy();

		child.setName("Child");
		child.setDescr("Child description");
		child.setProgr(0);

		child.setParent(parent);
		parent.getChildren().add(child);

		dao.create(child);

		dao.delete(parent);

	}


	@Test
	public void simpleTree() {

		ISbiViewHierarchyDAO dao = new SbiViewHierarchyHIBDAOImpl();

		SbiViewHierarchy parent = new SbiViewHierarchy();

		parent.setName("Parent");
		parent.setDescr("Parent description");
		parent.setProgr(0);

		dao.create(parent);

		// Child #1
		SbiViewHierarchy child1 = new SbiViewHierarchy();

		child1.setName("Child #1");
		child1.setDescr("Child description #1");
		child1.setProgr(1);

		child1.setParent(parent);
		parent.getChildren().add(child1);

		dao.create(child1);

		// Child #2
		SbiViewHierarchy child2 = new SbiViewHierarchy();

		child2.setName("Child #2");
		child2.setDescr("Child description #2");
		child2.setProgr(0);

		child2.setParent(parent);
		parent.getChildren().add(child2);

		dao.create(child2);

		// Child #2.1
		SbiViewHierarchy child2_1 = new SbiViewHierarchy();

		child2_1.setName("Child #2");
		child2_1.setDescr("Child description #2");
		child2_1.setProgr(0);

		child2_1.setParent(parent);
		child2.getChildren().add(child2_1);

		dao.create(child2_1);

		// Child #2.2
		SbiViewHierarchy child2_2 = new SbiViewHierarchy();

		child2_2.setName("Child #2");
		child2_2.setDescr("Child description #2");
		child2_2.setProgr(0);

		child2_2.setParent(parent);
		child2.getChildren().add(child2_2);

		dao.create(child2_2);

		// Delete parent
		dao.delete(parent);

	}

}
