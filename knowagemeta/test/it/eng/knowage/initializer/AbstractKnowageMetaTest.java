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
package it.eng.knowage.initializer;

import it.eng.knowage.common.TestConstants;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Assert;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public abstract class AbstractKnowageMetaTest extends TestCase {

	protected static BusinessModelInitializer businessModelInitializer;

	protected static DataSource dataSourceReading;
	protected static TestConstants.DatabaseType dbType;

	protected static Model rootModel;
	protected static PhysicalModel physicalModel;
	protected static BusinessModel businessModel;

	static private Logger logger = Logger.getLogger(AbstractKnowageMetaTest.class);

	public AbstractKnowageMetaTest() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Creating DataSources
		// this.createDataSources();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		// clean
		dataSourceReading = null;
	}

	/*
	 * ---------------------------------------------------- Test cases ----------------------------------------------------
	 */
	// add generic tests related to physical model here ...

	public void testModelInitializationSmoke() {
		assertNotNull("Metamodel cannot be null", rootModel);
	}

	public void testPhysicalModelInitializationSmoke() {
		assertTrue("Metamodel must have one physical model ", rootModel.getPhysicalModels().size() == 1);
	}

	public void testBusinessModelInitializationSmoke() {
		assertTrue("Metamodel must have one business model ", rootModel.getBusinessModels().size() == 1);
	}

	public void testPhysicalModelSourceDatabase() {
		Assert.assertNotNull("Database name connot be null", physicalModel.getDatabaseName());
		Assert.assertNotNull("Database version connot be null", physicalModel.getDatabaseVersion());
	}

	/*
	 * ---------------------------------------------------- Initialization Methods ----------------------------------------------------
	 */

	public void createDataSources() {
		// Must be overridden by specific implementation
		// dataSourceReading = DataSourceFactory.createDataSource(TestConstants.DatabaseType.MYSQL);
		logger.error("Specific DataSource must be specified in specialized Test");
	}

	public void setRootModel(Model model) {
		rootModel = model;
		if (rootModel != null && rootModel.getPhysicalModels() != null && rootModel.getPhysicalModels().size() > 0) {
			physicalModel = rootModel.getPhysicalModels().get(0);
		}
		if (rootModel != null && rootModel.getBusinessModels() != null && rootModel.getBusinessModels().size() > 0) {
			businessModel = rootModel.getBusinessModels().get(0);
		}
	}

}
