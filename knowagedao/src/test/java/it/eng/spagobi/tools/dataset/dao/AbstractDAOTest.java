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

import java.util.HashMap;
import java.util.Map;

import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractDAOTest extends TestCase {
	
	static private Logger logger = Logger.getLogger(AbstractDAOTest.class);
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("DataSetDAO", "it.eng.spagobi.tools.dataset.dao.DataSetDAOImpl");
		DAOConfig.setMappings(mappings);
		TenantManager.setTenant(new Tenant("SPAGOBI"));		
	}
	
	//Test cases
	
	public void testDaoInit(){
		IDataSetDAO dataSetDao = null;
		try {
			dataSetDao = DAOFactory.getDataSetDAO();
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while instatiating the DAO", t);
		} 
		assertNotNull("DataSetDAO correctly initialized", dataSetDao );
	}
	

}
