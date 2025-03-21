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

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.olap4j.OlapException;
import org.pivot4j.PivotModel;
import org.pivot4j.ui.collector.NonInternalPropertyCollector;
import org.pivot4j.ui.collector.PropertyCollector;
import org.pivot4j.ui.fop.FopExporter;
import org.pivot4j.ui.poi.ExcelExporter;
import org.pivot4j.ui.table.TableRenderer;

import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.export.ExportConfig;
import it.eng.spagobi.engines.whatif.export.KnowageExcelExporter;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformationsStack;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/1.0/model")
@ManageAuthorization
public class ModelResource extends AbstractWhatIfEngineService {

	private static final Logger LOGGER = Logger.getLogger(ModelResource.class);
	private static final Logger AUDIT_LOGGER = Logger.getLogger("audit.stack");

	private static final String EXPRESSION = "expression";
	private static final String VERSION_FAKE_DESCR = "sbiNoDescription";
	private static final String EXPORT_FILE_NAME = "KnowageOlapExport";
	private static final String EXCELL_TEMPLATE_FILE_NAME = "export_dataset_template.xlsm";
	private static final DateTimeFormatter FILENAME_INSTANT_FORMATTER = new DateTimeFormatterBuilder()
			.parseCaseInsensitive().appendValue(YEAR, 4).appendValue(MONTH_OF_YEAR, 2).appendValue(DAY_OF_MONTH, 2)
			.appendValue(HOUR_OF_DAY, 2).appendValue(MINUTE_OF_HOUR, 2).toFormatter();

	@Context
	private HttpServletResponse response;


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
	public String setMdx() {
		LOGGER.debug("IN");
		String table = "";

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();

		String requestBody = "";

		try {
			requestBody = RestUtilities.readBodyXSSUnsafe(getServletRequest());
		} catch (IOException e) {
			String errorMessage = e.getMessage().replace(": Couldn't read request body", "");
			throw new SpagoBIEngineRestServiceRuntimeException(errorMessage, this.getLocale(), e);
		}

		if (!isNullOrEmpty(requestBody)) {
			LOGGER.debug("Updating the query in the model");
			model.setMdx(requestBody);
		} else {
			LOGGER.debug("No query found");
		}

		table = renderModel(model);
		LOGGER.debug("OUT");
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
		LOGGER.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();

		String mdx = model.getCurrentMdx();

		if (mdx == null) {
			mdx = "";
		}

		LOGGER.debug("OUT");
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
		if (exportConfig.getFontFamily() != null) {
			exporter.setFontFamily(exportConfig.getFontFamily());
		}
		if (exportConfig.getFontSize() != null) {
			exporter.setFontSize(exportConfig.getFontSize());
		}

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

		return Response.ok(outputByte, MediaType.APPLICATION_OCTET_STREAM)
				.header("content-disposition", "attachment; filename = " + fileName).build();
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
		if (exportConfig.getFontFamily() != null) {
			exporter.setFontFamily(exportConfig.getFontFamily());
		}
		if (exportConfig.getFontSize() != null) {
			exporter.setFontSize(exportConfig.getFontSize().toString());
		}
		if (exportConfig.getOrientation() != null) {
			exporter.setOrientation(exportConfig.getOrientation());
		}

		TableRenderer render = new TableRenderer();

		// adds the calculated fields before rendering the model
		model.applyCal();
		applyConfiguration(modelConfig, model, render);
		render.render(model, exporter);

		// restore the query without calculated fields
		model.restoreQuery();
		byte[] outputByte = out.toByteArray();
		String fileName = getExportFileName() + ".pdf";

		return Response.ok(outputByte, MediaType.APPLICATION_OCTET_STREAM)
				.header("content-disposition", "attachment; filename = " + fileName).build();
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
		try (ObjectOutputStream stream = new ObjectOutputStream(out)) {
			Serializable state = model.saveState();
			stream.writeObject(state);
		} catch (IOException e) {
			LOGGER.error("Error while serializing model", e);
		}

		byte[] outputByte = out.toByteArray();

		String fileName = EXPORT_FILE_NAME + "_" + FILENAME_INSTANT_FORMATTER.format(Instant.now()) + ".txt";

		return Response.ok(outputByte, MediaType.APPLICATION_OCTET_STREAM)
				.header("content-disposition", "attachment; filename = " + fileName).build();
	}

	private HSSFWorkbook getHSSFWorkbook(FileInputStream fileInputStream2) throws Exception {
		return new HSSFWorkbook(fileInputStream2);
	}

	private String getExportFileName() {
		String fileName = "";
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_hh:mm");

		fileName = EXPORT_FILE_NAME + "_" + format.format(date);
		return fileName;
	}

	public void logTransormations() {
		logTransormations(null);
	}

	public void logTransormations(String info) {
		if (info != null) {
			AUDIT_LOGGER.info(info);
		}
		AUDIT_LOGGER.info("Pending transformations: ");
		AUDIT_LOGGER.info(
				((SpagoBIPivotModel) getWhatIfEngineInstance().getPivotModel()).getPendingTransformations().toString());
	}

	public void logOperation(String info) {
		AUDIT_LOGGER.info("OPERATION PERFORMED: " + info);
	}

	public void logErrorTransformations(CellTransformationsStack remaningTransformations) {
		AUDIT_LOGGER.info("Error persisting the these modifications " + remaningTransformations.toString());
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
		if (exportConfig.getFontFamily() != null) {
			exporter.setFontFamily(exportConfig.getFontFamily());
		}
		if (exportConfig.getFontSize() != null) {
			exporter.setFontSize(exportConfig.getFontSize());
		}

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
			LOGGER.error("Impossible to write to file");
		}
		return file;
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
