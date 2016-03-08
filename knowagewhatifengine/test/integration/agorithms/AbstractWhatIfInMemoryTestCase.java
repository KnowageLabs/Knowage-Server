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
package integration.agorithms;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.engines.whatif.WhatIfEngine;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.exception.WhatIfPersistingTransformationException;
import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformation;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.IAllocationAlgorithm;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.writeback4j.mondrian.CacheManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;
import test.DbConfigContainer;
import db.HSQLDBEnviromentSingleton;

public abstract class AbstractWhatIfInMemoryTestCase extends TestCase {

	private String catalog;

	public static final Double accurancy = 0.00001d;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		HSQLDBEnviromentSingleton.getInstance().startDB();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		HSQLDBEnviromentSingleton.getInstance().closeDB();
	}

	public WhatIfEngineInstance getWhatifengineiEngineInstance(String c) {
		SourceBean template;
		try {
			catalog = c;
			template = SourceBean.fromXMLFile(getTemplate());
			return WhatIfEngine.createInstance(template, getEnv());
		} catch (SourceBeanException e) {
			e.printStackTrace();
			assertFalse(true);
		}
		return null;
	}

	public Map getEnv() {

		Map env = new HashMap();
		env.put(EngineConstants.ENV_OLAP_SCHEMA, catalog);
		env.put(EngineConstants.ENV_LOCALE, Locale.ITALIAN);

		return env;
	}

	public String getCatalogue() {
		return DbConfigContainer.getHSQLCatalogue();
	}

	public String getTemplate() {
		return DbConfigContainer.getHSQLTemplate();
	}

	public Double persistTransformations(WhatIfEngineInstance ei, IAllocationAlgorithm al) throws Exception {

		SpagoBIPivotModel pivotModel = (SpagoBIPivotModel) ei.getPivotModel();

		SpagoBICellSetWrapper cellSetWrapper = (SpagoBICellSetWrapper) pivotModel.getCellSet();
		SpagoBICellWrapper cellWrapper = (SpagoBICellWrapper) cellSetWrapper.getCell(0);

		Double value = (new Random()).nextFloat() * 1000000d;

		CellTransformation transformation = new CellTransformation(value, cellWrapper.getValue(), cellWrapper, al);
		cellSetWrapper.applyTranformation(transformation);

		Connection connection;
		IDataSource dataSource = ei.getDataSource();

		try {

			connection = dataSource.getConnection(null);
		} catch (Exception e) {
			fail();
			throw e;
		}

		try {
			pivotModel.persistTransformations(connection);
		} catch (WhatIfPersistingTransformationException e) {

			fail();
			throw e;
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				throw e;
			}
		}

		CacheManager.flushCache(pivotModel.getDataSource());
		String mdx = pivotModel.getMdx();
		pivotModel.setMdx(mdx);
		pivotModel.initialize();

		cellSetWrapper = (SpagoBICellSetWrapper) pivotModel.getCellSet();
		cellWrapper = (SpagoBICellWrapper) cellSetWrapper.getCell(0);

		Double newValue = (Double) cellWrapper.getValue();
		Double ration = 1 - newValue / value;
		return ration;
	}

}