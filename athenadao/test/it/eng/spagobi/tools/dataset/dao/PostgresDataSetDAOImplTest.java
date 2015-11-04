/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spagobi.tools.dataset.dao;

import it.eng.spagobi.commons.dao.DAOConfig;

import org.apache.log4j.Logger;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class PostgresDataSetDAOImplTest extends DataSetDAOImplTest {

	private final IDataSetDAO dataSetDao = null;

	static private Logger logger = Logger.getLogger(PostgresDataSetDAOImplTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		DAOConfig.setHibernateConfigurationFile("hibernate.cfg.postgres.xml");
		DAOConfig.setResourcePath("D:/Documenti/Sviluppo/servers/tomcat6spagobi3postgres9.0/resources");
	}

	// Generic tests imported from parent class

	@Override
	public void testDaoInit() {
		super.testDaoInit();
	}

	@Override
	public void testLoadDataSetByLabel() {
		super.testLoadDataSetByLabel();
	}

	@Override
	public void testLoadDataSetById() {
		super.testLoadDataSetById();
	}

	// public void testLoadDataSetsByOwner() {
	// super.testLoadDataSetsByOwner();
	// }

	@Override
	public void testLoadEnterpriseDataSets() {
		super.testLoadEnterpriseDataSets();
	}

	@Override
	public void testLoadUserDataSets() {
		super.testLoadUserDataSets();
	}

	@Override
	public void testLoadFlatDatasets() {
		super.testLoadFlatDatasets();
	}

	// public void testLoadDataSetsOwnedByUser() {
	// super.testLoadDataSetsOwnedByUser();
	// }
	//
	// public void testLoadDatasetsSharedWithUser() {
	// super.testLoadDatasetsSharedWithUser();
	// }

	@Override
	public void testLoadDatasetOwnedAndShared() {
		super.testLoadDatasetOwnedAndShared();
	}

	@Override
	public void testLoadMyDataDataSets() {
		super.testLoadMyDataDataSets();
	}

	@Override
	public void testLoadDataSets() {
		super.testLoadDataSets();
	}
}
