/**
 * 
 */
package integration.versions;

import integration.agorithms.AbstractWhatIfInMemoryTestCase;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformation;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.DefaultWeightedAllocationAlgorithm;
import it.eng.spagobi.engines.whatif.version.SbiVersion;
import it.eng.spagobi.engines.whatif.version.VersionDAO;
import it.eng.spagobi.engines.whatif.version.VersionManager;
import it.eng.spagobi.writeback4j.sql.SqlQueryStatement;

import java.sql.Connection;
import java.util.List;
import java.util.Random;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author ghedin
 * 
 */
public class VersionManagerTestCase extends AbstractWhatIfInMemoryTestCase {

	private static final Integer VERSION = 11001;

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	private Double duplicateVersion() throws Exception {
		WhatIfEngineInstance ei = getWhatifengineiEngineInstance(getCatalogue());
		SpagoBIPivotModel pivotModel = (SpagoBIPivotModel) ei.getPivotModel();

		SpagoBICellSetWrapper cellSetWrapper = (SpagoBICellSetWrapper) pivotModel.getCellSet();
		SpagoBICellWrapper cellWrapper = (SpagoBICellWrapper) cellSetWrapper.getCell(0);

		Double value = (new Random()).nextFloat() * 1000000d;

		DefaultWeightedAllocationAlgorithm al = new DefaultWeightedAllocationAlgorithm(ei);
		CellTransformation transformation = new CellTransformation(value, cellWrapper.getValue(), cellWrapper, al);
		cellSetWrapper.applyTranformation(transformation);

		VersionManager versionManager = new VersionManager(ei);
		versionManager.persistNewVersionProcedure(VERSION, VERSION.toString(), VERSION.toString());

		cellSetWrapper = (SpagoBICellSetWrapper) pivotModel.getCellSet();
		cellWrapper = (SpagoBICellWrapper) cellSetWrapper.getCell(0);

		Integer newVersion = getDbVersion(ei);

		assertEquals(newVersion.intValue(), (VERSION.intValue()));

		Double newValue = (Double) cellWrapper.getValue();
		Double ration = 1 - newValue / value;
		System.out.println("Execute query = " + al.getLastQuery());
		return ration;

	}

	private Integer getDbVersion(WhatIfEngineInstance ei) throws Exception {
		Connection connection = ei.getDataSource().getConnection();
		try {

			String statement = "select MAX(" + ei.getWriteBackManager().getRetriver().getVersionColumnName() + ") as "
					+ ei.getWriteBackManager().getRetriver().getVersionColumnName() + " from " + ei.getWriteBackManager().getRetriver().getEditCubeTableName();
			SqlQueryStatement queryStatement = new SqlQueryStatement(statement);
			Integer i = (Integer) queryStatement.getSingleValue(connection, ei.getWriteBackManager().getRetriver().getVersionColumnName());

			return i;
		} finally {
			connection.close();
		}
	}

	public void testNewVersion() throws Exception {

		Double ration = duplicateVersion();

		assertTrue(ration < accurancy);

	}

	public void testGetAllVersions() throws Exception {
		String catalog = getCatalogue();
		WhatIfEngineInstance ei = getWhatifengineiEngineInstance(catalog);
		Connection connection = null;
		try {
			connection = ei.getDataSource().getConnection();
			VersionDAO dao = new VersionDAO(ei);
			List<SbiVersion> versions = dao.getAllVersions(connection);
			assertEquals(versions.size(), 3);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			connection.close();
		}

	}

}
