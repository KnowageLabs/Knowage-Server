/**
 * 
 */
package test.writeback;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.exception.WhatIfPersistingTransformationException;
import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformation;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.DefaultWeightedAllocationAlgorithm;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.writeback4j.mondrian.CacheManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import test.AbstractWhatIfTestCase;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author ghedin
 * 
 */
public abstract class AbstractWriteBackTestCase extends AbstractWhatIfTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public Double persistTransformations(String catalog, boolean useIn) throws Exception {
		WhatIfEngineInstance ei = getWhatifengineiEngineInstance(catalog);
		SpagoBIPivotModel pivotModel = (SpagoBIPivotModel) ei.getPivotModel();

		SpagoBICellSetWrapper cellSetWrapper = (SpagoBICellSetWrapper) pivotModel.getCellSet();
		SpagoBICellWrapper cellWrapper = (SpagoBICellWrapper) cellSetWrapper.getCell(0);

		Double value = (new Random()).nextFloat() * 1000000d;

		DefaultWeightedAllocationAlgorithm al = new DefaultWeightedAllocationAlgorithm(ei);
		al.setUseInClause(useIn);
		al.initAlgorithm();

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
		System.out.println("Execute query = " + al.getLastQuery());
		return ration;
	}

	public void testWithIn() throws Exception {

		long dateA = System.currentTimeMillis();
		Double ration = persistTransformations(getCatalogue(), true);
		long dateB = System.currentTimeMillis();

		System.out.println("Time with in " + (dateB - dateA));
		System.out.println("Ratio is " + ration);

		assertTrue(ration < accurancy);

	}

	public void testNoIn() throws Exception {

		long dateA = System.currentTimeMillis();
		Double ration = persistTransformations(getCatalogue(), false);
		long dateB = System.currentTimeMillis();

		System.out.println("Time no in " + (dateB - dateA));
		System.out.println("Ratio is " + ration);

		assertTrue(ration < accurancy && ration > -accurancy);

	}

	public abstract String getCatalogue();

}
