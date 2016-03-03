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
package it.eng.spagobi.tools.dataset.dao;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.List;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DataSetDAOImplTest extends AbstractDAOTest {

	private IDataSetDAO dataSetDao = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		try {
			dataSetDao = DAOFactory.getDataSetDAO();
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while instatiating the DAO", t);
		}
	}

	// Test cases

	public void testLoadDataSetByLabel() {
		IDataSet dataSet = dataSetDao.loadDataSetByLabel("ds__1869115");
		assertNotNull("Impossible to load dataset", dataSet);
		assertEquals("Impossible to load dataset", dataSet.getLabel(), "ds__1869115");
	}

	public void testLoadDataSetById() {
		IDataSet dataSet = dataSetDao.loadDataSetById(68);
		assertNotNull("Impossible to load dataset", dataSet);
		assertEquals("Impossible to load dataset", dataSet.getLabel(), "ds__1869115");
	}

	/*
	 * public void testLoadDataSetsByOwner() { List<IDataSet> dataSets = dataSetDao.loadDataSetsByOwner("astatuser", true, false);
	 * assertNotNull("Impossible to load dataset", dataSets ); }
	 */

	public void testLoadEnterpriseDataSets() {
		List<IDataSet> dataSets = dataSetDao.loadEnterpriseDataSets();
		assertNotNull("Impossible to load dataset", dataSets);
	}

	public void testLoadUserDataSets() {
		List<IDataSet> dataSets = dataSetDao.loadUserDataSets("astatuser");
		assertNotNull("Impossible to load dataset", dataSets);
	}

	public void testLoadFlatDatasets() {
		List<IDataSet> dataSets = dataSetDao.loadFlatDatasets();
		assertNotNull("Impossible to load dataset", dataSets);
	}

	// public void testLoadDataSetsOwnedByUser() {
	// List<IDataSet> dataSets = dataSetDao.loadDataSetsOwnedByUser("astatuser");
	// assertNotNull("Impossible to load dataset", dataSets );
	// }

	// public void testLoadDatasetsSharedWithUser() {
	// List<IDataSet> dataSets = dataSetDao.loadDatasetsSharedWithUser("astatuser");
	// assertNotNull("Impossible to load dataset", dataSets );
	// }

	public void testLoadDatasetOwnedAndShared() {
		List<IDataSet> dataSets = dataSetDao.loadDatasetOwnedAndShared("astatuser");
		assertNotNull("Impossible to load dataset", dataSets);
	}

	public void testLoadMyDataDataSets() {
		List<IDataSet> dataSets = dataSetDao.loadMyDataDataSets("astatuser");
		assertNotNull("Impossible to load dataset", dataSets);
	}

	public void testLoadDataSets() {
		List<IDataSet> dataSets = dataSetDao.loadDataSets();
		assertNotNull("Impossible to load dataset", dataSets);
		assertEquals("Impossible to load dataset", 74, dataSets.size());
	}
}
