/**
 * 
 */
package test.writeback.export;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.exception.WhatIfPersistingTransformationException;
import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformation;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.DefaultWeightedAllocationAlgorithm;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.writeback4j.ISchemaRetriver;
import it.eng.spagobi.writeback4j.sql.AnalysisExporter;

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
public abstract class AbstractWhatIfExportResult extends AbstractWhatIfTestCase {

	Connection connection;
	IDataSource dataSource;
	SpagoBICellWrapper cellWrapper;
	WhatIfEngineInstance ei;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		ei = getWhatifengineiEngineInstance(getCatalogue());
		SpagoBIPivotModel pivotModel = (SpagoBIPivotModel) ei.getPivotModel();

		SpagoBICellSetWrapper cellSetWrapper = (SpagoBICellSetWrapper) pivotModel.getCellSet();
		cellWrapper = (SpagoBICellWrapper) cellSetWrapper.getCell(0);

		Double value = (new Random()).nextFloat() * 1000000d;

		DefaultWeightedAllocationAlgorithm al = new DefaultWeightedAllocationAlgorithm(ei);
		CellTransformation transformation = new CellTransformation(value, cellWrapper.getValue(), cellWrapper, al);
		cellSetWrapper.applyTranformation(transformation);

		dataSource = ei.getDataSource();
		try {

			connection = dataSource.getConnection(null);
		} catch (Exception e) {
			fail();
			throw e;
		}
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testExportCSV() throws Exception {
		try {
			ISchemaRetriver retriver = ei.getWriteBackManager().getRetriver();
			AnalysisExporter ae = new AnalysisExporter(ei.getPivotModel(), retriver);
			ae.exportCSV(connection, 2, "|", "");
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

	}

	public void testExportTable() throws Exception {
		try {
			ISchemaRetriver retriver = ei.getWriteBackManager().getRetriver();
			AnalysisExporter ae = new AnalysisExporter(ei.getPivotModel(), retriver);
			ae.exportTable(connection, dataSource, dataSource, 2, "expotedTable");
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

	}

	public abstract String getCatalogue();

}
