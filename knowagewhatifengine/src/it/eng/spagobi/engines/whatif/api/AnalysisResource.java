/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it) 
 * 
 * @class AnalysisResource
 * 
 * Provides services to manage the analysis.

 * 
 */
package it.eng.spagobi.engines.whatif.api;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.writeback4j.sql.AnalysisExporter;

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

import com.eyeq.pivot4j.PivotModel;

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

		return Response
				.ok(csv, MediaType.APPLICATION_OCTET_STREAM)
				.header("content-disposition", "attachment; filename = " + fileName)
				.build();

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
