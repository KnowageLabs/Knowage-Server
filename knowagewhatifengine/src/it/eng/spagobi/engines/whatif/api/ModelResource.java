/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 * @class ModelTransformer
 *
 * Services that manage the model:
 * <ul>
 * <li>/model/mdx/{mdx}: executes the mdx query</li>
 * </ul>
 *
 */
package it.eng.spagobi.engines.whatif.api;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.exception.WhatIfPersistingTransformationException;
import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformation;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformationsStack;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.AllocationAlgorithmFactory;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.DefaultWeightedAllocationAlgorithm;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.IAllocationAlgorithm;
import it.eng.spagobi.engines.whatif.parser.Lexer;
import it.eng.spagobi.engines.whatif.parser.parser;
import it.eng.spagobi.engines.whatif.version.VersionManager;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.writeback4j.mondrian.CacheManager;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.axis.utils.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.olap4j.OlapDataSource;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.export.poi.ExcelExporter;

@Path("/1.0/model")
public class ModelResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(ModelResource.class);
	public static transient Logger auditlogger = Logger.getLogger("audit.stack");
	private static final String VERSION_FAKE_DESCR = "sbiNoDescription";

	// input parameters
	public static final String EXPRESSION = "expression";

	private static final String exportFileName = "SpagoBIOlapExport";
	private VersionManager versionManager;

	private VersionManager getVersionBusiness() {
		WhatIfEngineInstance ei = getWhatIfEngineInstance();

		if (versionManager == null) {
			versionManager = new VersionManager(ei);
		}
		return versionManager;
	}

	/**
	 * Executes the mdx query. If the mdx is null it executes the query of the model
	 *
	 * @param mdx
	 *            the query to execute
	 * @return the htm table representing the cellset
	 */
	@POST
	@Path("/{mdx}")
	@Produces("text/html; charset=UTF-8")
	public String setMdx(@PathParam("mdx") String mdx) {
		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();

		if (!isNullOrEmpty(mdx)) {
			logger.debug("Updating the query in the model");
			model.setMdx(mdx);
		} else {
			logger.debug("No query found");
		}

		String table = renderModel(model);
		logger.debug("OUT");
		return table;

	}

	@POST
	@Path("/setValue/{ordinal}")
	@Produces("text/html; charset=UTF-8")
	public String setValue(@PathParam("ordinal") int ordinal) {
		logger.debug("IN : ordinal = [" + ordinal + "]");
		logOperation("Set value");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();
		String expression = null;

		// check if a version has been selected in the cube
		((SpagoBIPivotModel) ei.getPivotModel()).getActualVersionSlicer(ei.getModelConfig());

		try {
			JSONObject json = RestUtilities.readBodyAsJSONObject(getServletRequest());
			expression = json.getString(EXPRESSION);
		} catch (Exception e) {
			throw new SpagoBIEngineRestServiceRuntimeException("generic.error", this.getLocale(), e);
		}
		logger.debug("expression = [" + expression + "]");
		SpagoBICellSetWrapper cellSetWrapper = (SpagoBICellSetWrapper) model.getCellSet();
		SpagoBICellWrapper cellWrapper = (SpagoBICellWrapper) cellSetWrapper.getCell(ordinal);
		OlapDataSource olapDataSource = ei.getOlapDataSource();

		Double value = null;
		try {
			Lexer lex = new Lexer(new java.io.StringReader(expression));
			parser par = new parser(lex);
			par.setWhatIfInfo(cellWrapper, model, olapDataSource, ei);
			value = (Double) par.parse().value;
		} catch (Exception e) {
			logger.debug("Error parsing What-if metalanguage expression", e);
			String errorMessage = e.getMessage().replace(": Couldn't repair and continue parse", "");
			throw new SpagoBIEngineRestServiceRuntimeException(errorMessage, this.getLocale(), e);
		}

		String algorithm = ei.getAlgorithmInUse();
		logger.debug("Resolving the allocation algorithm");
		logger.debug("The class of the algorithm is [" + algorithm + "]");
		IAllocationAlgorithm allocationAlgorithm;

		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(DefaultWeightedAllocationAlgorithm.ENGINEINSTANCE_PROPERTY, ei);
			allocationAlgorithm = AllocationAlgorithmFactory.getAllocationAlgorithm(algorithm, ei, properties);
		} catch (SpagoBIEngineException e) {
			logger.error(e);
			throw new SpagoBIEngineRestServiceRuntimeException("sbi.olap.writeback.algorithm.definition.error", getLocale(), e);
		}

		CellTransformation transformation = new CellTransformation(value, cellWrapper.getValue(), cellWrapper, allocationAlgorithm);
		cellSetWrapper.applyTranformation(transformation);
		String table = renderModel(model);

		logTransormations();
		logger.debug("OUT");
		return table;
	}

	@POST
	@Path("/persistTransformations")
	@Produces("text/html; charset=UTF-8")
	public String persistTransformations() {
		logger.debug("IN");
		logOperation("Save");

		Connection connection;
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		OlapDataSource olapDataSource = ei.getOlapDataSource();
		PivotModel model = ei.getPivotModel();

		SpagoBIPivotModel modelWrapper = (SpagoBIPivotModel) model;

		logger.debug("Persisting the modifications..");

		IDataSource dataSource = ei.getDataSource();
		try {
			logger.debug("Getting the connection to DB");
			connection = dataSource.getConnection(null);
		} catch (Exception e) {
			logger.error("Error opening connection to datasource " + dataSource.getLabel());
			throw new SpagoBIRuntimeException("Error opening connection to datasource " + dataSource.getLabel(), e);
		}
		try {

			// check if a version has been selected in the cube
			((SpagoBIPivotModel) ei.getPivotModel()).getActualVersionSlicer(ei.getModelConfig());

			// Persisting the pending modifications
			modelWrapper.persistTransformations(connection);
		} catch (WhatIfPersistingTransformationException e) {
			logger.debug("Error persisting the modifications", e);
			logErrorTransformations(e.getTransformations());
			throw new SpagoBIEngineRestServiceRuntimeException(e.getLocalizationmessage(), modelWrapper.getLocale(), "Error persisting modifications", e);
		} finally {
			logger.debug("Closing the connection used to persist the modifications");
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("Error closing the connection to the db");
				throw new SpagoBIEngineRestServiceRuntimeException(getLocale(), e);
			}
			logger.debug("Closed the connection used to persist the modifications");
		}

		logger.debug("Modification persisted...");

		logger.debug("Cleaning the cache and restoring the model");
		CacheManager.flushCache(olapDataSource);
		String mdx = modelWrapper.getCurrentMdx();
		modelWrapper.setMdx(mdx);
		modelWrapper.initialize();
		logger.debug("Finish to clean the cache and restoring the model");

		String table = renderModel(model);

		logOperation("Transormations stack cleaned");
		logTransormations();

		logger.debug("OUT");
		return table;
	}

	/**
	 * Service to increase Version
	 *
	 * @return
	 *
	 */
	@POST
	@Path("/saveAs/{name}/{descr}")
	@Produces("text/html; charset=UTF-8")
	public String increaseVersion(@PathParam("name") String name, @PathParam("descr") String descr) {
		logger.debug("IN");
		logOperation("Save As");

		if (name.equals(VERSION_FAKE_DESCR)) {
			name = null;
		}

		if (descr.equals(VERSION_FAKE_DESCR)) {
			descr = null;
		}

		PivotModel model;
		try {
			model = getVersionBusiness().persistNewVersionProcedure(name, descr);
		} catch (WhatIfPersistingTransformationException e) {
			logErrorTransformations(e.getTransformations());
			logger.error("Error persisting the trasformations in the new version a new version", e);
			throw new SpagoBIEngineRestServiceRuntimeException("versionresource.generic.error", getLocale(), e);
		}

		logTransormations();
		logger.debug("OUT");
		return renderModel(model);
	}

	@POST
	@Path("/undo")
	@Produces("text/html; charset=UTF-8")
	public String undo() {
		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		model.undo();
		String table = renderModel(model);
		logOperation("Undo");
		logTransormations();
		logger.debug("OUT");
		return table;
	}

	/**
	 * Gets the active mdx statement
	 *
	 * @return the mdx active statement
	 */
	@GET
	public String getMdx() {
		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();

		String mdx = model.getCurrentMdx();

		if (mdx == null) {
			mdx = "";
		}

		logger.debug("OUT");
		return mdx;

	}

	/**
	 * Exports the actual model in a xls format.. Since it takes the actual model, it takes also the pending transformations (what you see it's what you get)
	 *
	 * @return the response with the file embedded
	 */
	@GET
	@Path("/export/{format}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response export() {

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ExcelExporter exporter = new ExcelExporter(out);

		// adds the calculated fields before rendering the model
		model.applyCal();

		exporter.render(model);
		// restore the query without calculated fields
		model.restoreQuery();
		byte[] outputByte = out.toByteArray();

		Date d = new Date();
		String fileName = exportFileName + d.getYear() + d.getMonth() + d.getDay() + "_" + d.getHours() + d.getMinutes() + ".xls";

		return Response.ok(outputByte, MediaType.APPLICATION_OCTET_STREAM).header("content-disposition", "attachment; filename = " + fileName).build();
	}

	/**
	 * Exports the actual model in a xls format.. Since it takes the actual model, it takes also the pendingg transformations (what you see it's what you get)
	 *
	 * @return the response with the file embedded
	 */
	@GET
	@Path("/serialize")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response serializeModel() {

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream stream = null;
		try {
			stream = new ObjectOutputStream(out);
			Serializable state = model.saveState();
			stream.writeObject(state);
		} catch (IOException e) {
			logger.error("Error while serializing model", e);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
				logger.error("Impossible to close the stream for the serialization of the model");
				throw new SpagoBIEngineRuntimeException("Impossible to close the stream for the serialization of the model");

			}
		}

		byte[] outputByte = out.toByteArray();

		Date d = new Date();
		String fileName = exportFileName + "_" + d.getYear() + d.getMonth() + d.getDay() + d.getHours() + d.getMinutes() + ".txt";

		return Response.ok(outputByte, MediaType.APPLICATION_OCTET_STREAM).header("content-disposition", "attachment; filename = " + fileName).build();
	}

	public void logTransormations() {
		logTransormations(null);
	}

	public void logTransormations(String info) {
		if (info != null) {
			auditlogger.info(info);
		}
		auditlogger.info("Pending transformations: ");
		auditlogger.info(((SpagoBIPivotModel) getWhatIfEngineInstance().getPivotModel()).getPendingTransformations().toString());
	}

	public void logOperation(String info) {
		auditlogger.info("OPERATION PERFORMED: " + info);
	}

	public void logErrorTransformations(CellTransformationsStack remaningTransformations) {
		auditlogger.info("Error persisting the these modifications " + remaningTransformations.toString());
	}
}
