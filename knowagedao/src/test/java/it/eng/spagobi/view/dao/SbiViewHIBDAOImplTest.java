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

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.UtilitiesForTest;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.BIObjectDAOHibImpl;
import it.eng.spagobi.commons.utilities.UtilitiesDAOForTest;
import it.eng.spagobi.view.metadata.SbiView;
import it.eng.spagobi.view.metadata.SbiViewHierarchy;

/**
 * @author Marco Libanori
 * @since KNOWAGE_TM-513
 */
public class SbiViewHIBDAOImplTest {

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
	public void create() throws EMFUserError, JSONException {

		ISbiViewHierarchyDAO dao1 = new SbiViewHierarchyHIBDAOImpl();
		ISbiViewDAO dao2 = new SbiViewHIBDAOImpl();
		BIObjectDAOHibImpl dao3 = new BIObjectDAOHibImpl();

		SbiViewHierarchy e1 = new SbiViewHierarchy();

		e1.setName("name");
		e1.setDescr("descr");
		e1.setProgr(0);

		dao1.create(e1);

		SbiView e2 = new SbiView();

		JSONObject drivers = new JSONObject();
		drivers.put("driver1", "");

		JSONObject settings = new JSONObject();
		settings.put("setting1", "");

		BIObject biObj = (BIObject) dao3.loadAllBIObjects().stream().findFirst().get();

		e2.setLabel("label");
		e2.setName("Name");
		e2.setDescr("Descr");
		e2.setDrivers(drivers);
		e2.setSettings(settings);

		e2.setParent(e1);
		e2.setBiObjId(biObj.getId());

		dao2.create(e2);

		dao2.delete(e2);

		dao1.delete(e1);
	}

}
