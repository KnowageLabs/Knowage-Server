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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;

import org.apache.log4j.Logger;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class PostgresDataSetDAOImplTest extends DataSetDAOImplTest {
	
	private IDataSetDAO dataSetDao = null;
	
	static private Logger logger = Logger.getLogger(PostgresDataSetDAOImplTest.class);

	protected void setUp() throws Exception {
		super.setUp();
		DAOConfig.setHibernateConfigurationFile("hibernate.cfg.postgres.xml");
		DAOConfig.setResourcePath("D:/Documenti/Sviluppo/servers/tomcat6spagobi3postgres9.0/resources");
	}
	
	//Generic tests imported from parent class
	
	public void testDaoInit(){
		super.testDaoInit();
	}
	
	public void testLoadDataSetByLabel() {
		super.testLoadDataSetByLabel();
	}
	
	public void testLoadDataSetById() {
		super.testLoadDataSetById();
	}
	
	public void testLoadDataSetsByOwner() {
		super.testLoadDataSetsByOwner();
	}
	
	public void testLoadEnterpriseDataSets() {
		super.testLoadEnterpriseDataSets();
	}
	
	public void testLoadUserDataSets() {
		super.testLoadUserDataSets();
	}
	
	public void testLoadFlatDatasets() {
		super.testLoadFlatDatasets();
	}
	
	public void testLoadDataSetsOwnedByUser() {
		super.testLoadDataSetsOwnedByUser();
	}
	
	public void testLoadDatasetsSharedWithUser() {
		super.testLoadDatasetsSharedWithUser();
	}
	
	public void testLoadDatasetOwnedAndShared() {
		super.testLoadDatasetOwnedAndShared();
	}
	
	public void testLoadMyDataDataSets() {
		super.testLoadMyDataDataSets();
	}
	
	public void testLoadDataSets(){
		super.testLoadDataSets();
	}
}
