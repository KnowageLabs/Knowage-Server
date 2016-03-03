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
package integration.output;

import integration.agorithms.AbstractWhatIfInMemoryTestCase;
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

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class WhatIfExportResult extends AbstractWhatIfInMemoryTestCase {

	Connection connection;
	IDataSource dataSource;
	SpagoBICellWrapper cellWrapper;
	WhatIfEngineInstance ei;
	private final int VERSION_TO_EXPORT = 1;

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
		dataSource.setHibDialectClass("HSQL");
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
			byte[] content = ae.exportCSV(connection, VERSION_TO_EXPORT, "|", "");
			File userDir = new File("").getAbsoluteFile();
			File f = new File(userDir, "\\test\\integration\\output\\exportedFile.csv");
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(content);
			fos.flush();
			fos.close();
			assertEquals(374470602752F, f.getTotalSpace(), 1000);
			f.delete();
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
			ae.exportTable(connection, dataSource, dataSource, VERSION_TO_EXPORT, "expotedTable");

			Statement stmt = connection.createStatement();

			String sql = "SELECT * FROM expotedTable";
			ResultSet rs = stmt.executeQuery(sql);
			int columns = rs.getMetaData().getColumnCount();

			assertEquals(columns, 17);

			if (rs == null) {

				fail();
			} else {
				int count = 0;
				while (rs.next()) {
					count++;
				}
				assertEquals(32995, count);
			}

			stmt.executeUpdate("Drop table expotedTable ");
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

}
