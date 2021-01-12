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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.axis.utils.ByteArrayOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapDataSource;
import org.olap4j.OlapException;
import org.pivot4j.PivotModel;
import org.pivot4j.ui.collector.NonInternalPropertyCollector;
import org.pivot4j.ui.collector.PropertyCollector;
import org.pivot4j.ui.fop.FopExporter;
import org.pivot4j.ui.poi.ExcelExporter;
import org.pivot4j.ui.table.TableRenderer;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.exception.WhatIfPersistingTransformationException;
import it.eng.spagobi.engines.whatif.export.ExportConfig;
import it.eng.spagobi.engines.whatif.export.KnowageExcelExporter;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.model.Util;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformation;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformationsStack;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.AllocationAlgorithmDefinition;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.AllocationAlgorithmFactory;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.AllocationAlgorithmSingleton;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.DefaultWeightedAllocationAlgorithm;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.IAllocationAlgorithm;
import it.eng.spagobi.engines.whatif.parser.Lexer;
import it.eng.spagobi.engines.whatif.parser.parser;
import it.eng.spagobi.engines.whatif.version.VersionManager;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.writeback4j.mondrian.CacheManager;

@Path("/1.0/model")
@ManageAuthorization

public class ModelResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(ModelResource.class);
	public static transient Logger auditlogger = Logger.getLogger("audit.stack");
	private static final String VERSION_FAKE_DESCR = "sbiNoDescription";
	private static final String TEMPLATE_MONDRIAN_SCHEMA = "mondranSchema";

	@Context
	private HttpServletResponse response;

	// input parameters
	public static final String EXPRESSION = "expression";

	private static final String exportFileName = "KnowageOlapExport";
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
	 * @param mdx the query to execute
	 * @return the htm table representing the cellset
	 * @throws OlapException
	 */
	@POST
	@Path("/")
	@Produces("text/html; charset=UTF-8")

	public String setMdx() throws OlapException {
		logger.debug("IN");
		String table = "";

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();

		CellSet cellSet = model.getCellSet();

		// Axes of the resulting query.
		List<CellSetAxis> axes = cellSet.getAxes();

		// The ROWS axis
		CellSetAxis rowsOrColumns = axes.get(1);
		String requestBody = "";

		try {
			requestBody = RestUtilities.readBodyXSSUnsafe(getServletRequest());
		} catch (IOException e) {
			String errorMessage = e.getMessage().replace(": Couldn't read request body", "");
			throw new SpagoBIEngineRestServiceRuntimeException(errorMessage, this.getLocale(), e);
		}

		if (!isNullOrEmpty(requestBody)) {
			logger.debug("Updating the query in the model");
			model.setMdx(requestBody);
		} else {
			logger.debug("No query found");
		}

		table = renderModel(model);
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
			connection = dataSource.getConnection();
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
	@Path("/saveAs")
	@Produces("text/html; charset=UTF-8")

	public String increaseVersion() {
		logger.debug("IN");
		logOperation("Save As");
		String name;
		String descr;

		JSONObject json;
		try {
			json = RestUtilities.readBodyAsJSONObject(getServletRequest());
			name = json.getString("name");
			descr = json.getString("descr");
		} catch (IOException e1) {
			logger.error("Error loading the parameters from the request", e1);
			throw new SpagoBIEngineRestServiceRuntimeException(getLocale(), e1);
		} catch (JSONException e1) {
			logger.error("Error loading the parameters from the request", e1);
			throw new SpagoBIEngineRestServiceRuntimeException(getLocale(), e1);
		}

		Monitor totalTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.api.ModelResource.increaseVersion.totalTime");
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
		String toReturn = renderModel(model);
		totalTime.stop();
		return toReturn;
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
	@Path("/")
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
	@Path("/export/excel")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportExcel() {

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		ModelConfig modelConfig = ei.getModelConfig();

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ExcelExporter exporter = new KnowageExcelExporter(out, getLocale());
		ExportConfig exportConfig = WhatIfEngineConfig.getInstance().getExportProperties();
		if (exportConfig.getFontFamily() != null)
			exporter.setFontFamily(exportConfig.getFontFamily());
		if (exportConfig.getFontSize() != null)
			exporter.setFontSize(exportConfig.getFontSize());

		TableRenderer render = new TableRenderer();

		// adds the calculated fields before rendering the model
		model.applyCal();

		applyConfiguration(modelConfig, model, render);
		render.setPropertyCollector(new NonInternalPropertyCollector());
		render.render(model, exporter);

		// restore the query without calculated fields
		model.restoreQuery();
		byte[] outputByte = out.toByteArray();
		String fileName = getExportFileName() + ".xls";

		return Response.ok(outputByte, MediaType.APPLICATION_OCTET_STREAM).header("content-disposition", "attachment; filename = " + fileName).build();
	}

	@GET
	@Path("/export/pdf")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportPdf() {

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		ModelConfig modelConfig = ei.getModelConfig();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		FopExporter exporter = new FopExporter(out);

		ExportConfig exportConfig = WhatIfEngineConfig.getInstance().getExportProperties();
		if (exportConfig.getFontFamily() != null)
			exporter.setFontFamily(exportConfig.getFontFamily());
		if (exportConfig.getFontSize() != null)
			exporter.setFontSize(exportConfig.getFontSize().toString());
		if (exportConfig.getOrientation() != null)
			exporter.setOrientation(exportConfig.getOrientation());

		TableRenderer render = new TableRenderer();

		// adds the calculated fields before rendering the model
		model.applyCal();
		applyConfiguration(modelConfig, model, render);
		render.render(model, exporter);

		// restore the query without calculated fields
		model.restoreQuery();
		byte[] outputByte = out.toByteArray();
		String fileName = getExportFileName() + ".pdf";

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

	@GET
	@Path("/exceledit")
	public void excelFillExample(@Context ServletContext context) throws IOException, Exception {

		File result = exportExcelForMerging();
		String EXCELL_TEMPLATE_FILE_NAME = "export_dataset_template.xlsm";
		OutputStream out = null;
		try {
			URL resourceLocation = Thread.currentThread().getContextClassLoader().getResource(EXCELL_TEMPLATE_FILE_NAME);
			logger.debug("Resource is: " + resourceLocation);
			Assert.assertNotNull(resourceLocation, "Could not find " + EXCELL_TEMPLATE_FILE_NAME + " in java resources");
			FileInputStream fileInputStream1 = new FileInputStream(new File(resourceLocation.toURI().getPath()));
			FileInputStream fileInputStream2 = new FileInputStream(result);

			XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream1);
			HSSFWorkbook exportedOlapWorkbook = new HSSFWorkbook(fileInputStream2);

			workbook = Util.merge(workbook, exportedOlapWorkbook.getSheetAt(0));
			workbook = writeParamsToExcel(context, workbook);
			try {
				Date date = new Date();
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_hh:mm");
				String ime = "OlapTableExport" + "_" + format.format(date);

				getServletResponse().setHeader("Content-Disposition", "attachment" + "; filename=\"" + ime + ".xlsm" + "\";");
				response.setContentType("application/vnd.ms-excel.sheet.macroEnabled.12");
				out = getServletResponse().getOutputStream();
				workbook.write(out);
				getServletResponse().getOutputStream().flush();
				getServletResponse().getOutputStream().close();
			} catch (IOException e) {
				logger.error("write output file stream error " + e.getMessage());
				throw new SpagoBIServiceException("test", "Impossible to write output file xls error", e);
			}

		} catch (Exception e) {
			throw new SpagoBIEngineServiceException(getClass().getName(), "Error while downloading edit excel file", e);
		}

	}

	private String getExportFileName() {
		String fileName = "";
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_hh:mm");

		fileName = exportFileName + "_" + format.format(date);
		return fileName;
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

	private HttpServletResponse getServletResponse() {
		return response;
	}

	private File exportExcelForMerging() {

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ExcelExporter exporter = new ExcelExporter(out);
		ExportConfig exportConfig = WhatIfEngineConfig.getInstance().getExportProperties();
		if (exportConfig.getFontFamily() != null)
			exporter.setFontFamily(exportConfig.getFontFamily());
		if (exportConfig.getFontSize() != null)
			exporter.setFontSize(exportConfig.getFontSize());

		TableRenderer render = new TableRenderer();

		// adds the calculated fields before rendering the model
		model.applyCal();
		model.removeSubset();
		render.render(model, exporter);

		// restore the query without calculated fields
		model.restoreQuery();
		byte[] outputByte = out.toByteArray();

		File file = new File(System.getProperty("java.io.tmpdir") + "\\table.xls");
		try {
			FileUtils.writeByteArrayToFile(file, outputByte);
		} catch (IOException e) {
			logger.error("Impossible to write to file");
		}
		return file;
	}

	private XSSFWorkbook writeParamsToExcel(ServletContext context, XSSFWorkbook workbook) throws OlapException {

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		Map map = getEnv();

		IEngUserProfile profile = (IEngUserProfile) map.get("ENV_USER_PROFILE");
		if (profile instanceof UserProfile) {
			UserProfile spagoBIUserProfile = (UserProfile) profile;
			map.put("tenant", spagoBIUserProfile.getOrganization());

		}
		map.put("MDX", model.getMdx());
		map.put("document", getEnv().get("DOCUMENT_ID").toString());

		String url = context.getContextPath() + "/restful/olap/startwhatif/editxls/?";
		String mdx = "";
		int axisRows = model.getCellSet().getAxes().get(1).getPositionCount();
		int axisColumns = model.getCellSet().getAxes().get(0).getPositionCount();
		int maxOrdinal = axisRows * axisColumns;
		Iterator it = map.entrySet().iterator();
		int index = 0;
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();

			if (pair.getKey().toString().equalsIgnoreCase("DOCUMENT_LABEL") || pair.getKey().toString().equalsIgnoreCase("SBI_ARTIFACT_ID")
					|| pair.getKey().toString().equalsIgnoreCase("SBI_ARTIFACT_VERSION_ID") || pair.getKey().toString().equalsIgnoreCase("document")
					|| pair.getKey().toString().equalsIgnoreCase("user_id") || pair.getKey().toString().equalsIgnoreCase("tenant")) {
				++index;
				if (index != 1) {
					url += "&";
				}
				url += pair.getKey() + "=" + pair.getValue();

			} else if (pair.getKey().toString().equalsIgnoreCase("MDX")) {
				mdx = pair.getValue().toString();
			}

			// it.remove();

		}
		XSSFSheet params = workbook.getSheetAt(1);
		XSSFRow urlRow = params.createRow(0);
		XSSFRow mdxRow = params.createRow(1);
		XSSFRow axisRow = params.createRow(2);
		XSSFRow algorithms = params.createRow(3);
		XSSFRow editCube = params.createRow(4);
		XSSFCell urlCell = urlRow.createCell(0);
		XSSFCell mdxCell = mdxRow.createCell(0);
		XSSFCell axisRowsCell = axisRow.createCell(0);
		XSSFCell axisColumnsCell = axisRow.createCell(1);
		XSSFCell editCubeCell = editCube.createCell(0);

		int keyIndex = 0;
		Map<String, AllocationAlgorithmDefinition> allocationAlgorithms = AllocationAlgorithmSingleton.getInstance().getAllocationAlgorithms();
		Iterator ita = allocationAlgorithms.entrySet().iterator();
		while (ita.hasNext()) {
			Map.Entry pair = (Map.Entry) ita.next();

			if (!pair.getKey().toString().equalsIgnoreCase("Fix values")) {

				XSSFCell algorithmsCell = algorithms.createCell(keyIndex++);
				algorithmsCell.setCellValue(pair.getKey().toString());
			}
			// ita.remove();
		}
		Map<String, AllocationAlgorithmDefinition> allocationAlgorithms2 = AllocationAlgorithmSingleton.getInstance().getAllocationAlgorithms();

		urlCell.setCellValue(url);
		mdxCell.setCellValue(mdx);
		axisRowsCell.setCellValue(axisRows);
		axisColumnsCell.setCellValue(axisColumns);
		try {
			editCubeCell.setCellValue(ei.getEditCubeName());
		} catch (NullPointerException e) {
			editCubeCell.setCellValue("");
		}

		XSSFSheet cellsToChange = workbook.getSheetAt(2);
		XSSFRow ccrow = cellsToChange.createRow(0);
		XSSFCell ordinalCell = ccrow.createCell(0);
		XSSFCell ordinalCellValue = ccrow.createCell(1);
		XSSFCell defaultAlgorithm = ccrow.createCell(2);

		boolean finished = false;
		for (int i = 0; i < maxOrdinal && !finished; i++) {
			Cell c = model.getCellSet().getCell(i);
			if (c.isEmpty()) {
				continue;
			} else {
				ordinalCell.setCellValue(c.getOrdinal());
				ordinalCellValue.setCellValue(c.getDoubleValue());
				finished = true;

			}

		}

		defaultAlgorithm.setCellValue("Proportional");

		return workbook;
	}

	private void applyConfiguration(ModelConfig modelConfig, SpagoBIPivotModel model, TableRenderer render) {
		applyConfiguration(modelConfig, render);
		applyConfiguration(modelConfig, model);
	}

	private void applyConfiguration(ModelConfig modelConfig, TableRenderer renderer) {

		applyShowParentMembersConfiguration(modelConfig, renderer);
		applyHideSpansConfiguration(modelConfig, renderer);
		applyShowPropertyConfiguration(modelConfig, renderer);

	}

	private void applyShowPropertyConfiguration(ModelConfig modelConfig, TableRenderer renderer) {
		Boolean showProperties = modelConfig.getShowProperties();
		PropertyCollector propertyCollector = showProperties ? new NonInternalPropertyCollector() : null;
		renderer.setPropertyCollector(propertyCollector);
	}

	private void applyHideSpansConfiguration(ModelConfig modelConfig, TableRenderer renderer) {
		Boolean hideSpans = modelConfig.getHideSpans();
		renderer.setHideSpans(hideSpans);
	}

	private void applyShowParentMembersConfiguration(ModelConfig modelConfig, TableRenderer renderer) {
		Boolean showParentMembers = modelConfig.getShowParentMembers();
		renderer.setShowParentMembers(showParentMembers);
	}
}
