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
package it.eng.spagobi.engines.whatif.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.pivot4j.PivotModel;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.writeback4j.sql.AnalysisExporter;

@Path("/1.0/analysis")
public class AnalysisResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(AnalysisResource.class);

	private static final String EXPORT_FILE_NAME = "SpagoBIOlapExport";
	private static final String CSV_ROWS_SEPARATOR = "\r\n";

	@GET
	@Path("/csv/{version}/{fieldDelimiter}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportEditTableCSV(@PathParam("version") int version, @PathParam("fieldDelimiter") String fieldDelimiter) {

		byte[] csv = null;
		Connection connection;
		WhatIfEngineInstance ei = getWhatIfEngineInstance();

		PivotModel model = ei.getPivotModel();

		logger.debug("Exporting in CSV..");

		IDataSource dataSource = ei.getDataSource();
		try {
			logger.debug("Getting the connection to DB");
			connection = dataSource.getConnection(null);
		} catch (Exception e) {
			logger.error("Error opening connection to datasource " + dataSource.getLabel());
			throw new SpagoBIRuntimeException("Error opening connection to datasource " + dataSource.getLabel(), e);
		}
		try {
			AnalysisExporter esporter = new AnalysisExporter(model, ei.getWriteBackManager().getRetriver());
			csv = esporter.exportCSV(connection, version, fieldDelimiter, CSV_ROWS_SEPARATOR);
		} catch (Exception e) {
			logger.debug("Error exporting the output tbale in csv", e);
			throw new SpagoBIEngineRestServiceRuntimeException("sbi.olap.writeback.export.out.error", getLocale(), e);
		} finally {
			logger.debug("Closing the connection used to export the output table");
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("Error closing the connection to the db");
				throw new SpagoBIEngineRestServiceRuntimeException(getLocale(), e);
			}
			logger.debug("Closed the connection used to export the output table");
		}

		String fileName = EXPORT_FILE_NAME + "-" + (new Date()).toLocaleString() + ".csv";

		return Response.ok(csv, MediaType.APPLICATION_OCTET_STREAM).header("content-disposition", "attachment; filename = " + fileName).build();

	}

	@GET
	@Path("/table/{version}/{tableName}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public String exportEditTableTable(@PathParam("version") int version, @PathParam("tableName") String tableName) {

		Connection connection;
		WhatIfEngineInstance ei = getWhatIfEngineInstance();

		PivotModel model = ei.getPivotModel();

		logger.debug("Exporting in an external table..");

		IDataSource dataSource = ei.getDataSource();
		try {
			logger.debug("Getting the connection to DB");
			connection = dataSource.getConnection(null);
		} catch (Exception e) {
			logger.error("Error opening connection to datasource " + dataSource.getLabel());
			throw new SpagoBIRuntimeException("Error opening connection to datasource " + dataSource.getLabel(), e);
		}
		try {
			AnalysisExporter esporter = new AnalysisExporter(model, ei.getWriteBackManager().getRetriver());
			esporter.exportTable(connection, ei.getDataSource(), ei.getDataSourceForWriting(), version, tableName);
		} catch (Exception e) {
			logger.debug("Error exporting the output table in an external table", e);
			throw new SpagoBIEngineRestServiceRuntimeException("sbi.olap.writeback.export.out.error", getLocale(), e);
		} finally {
			logger.debug("Closing the connection used to export the output table");
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("Error closing the connection to the db");
				throw new SpagoBIEngineRestServiceRuntimeException(getLocale(), e);
			}
			logger.debug("Closed the connection used to export the output table");
		}

		return getJsonSuccess();

	}

}
