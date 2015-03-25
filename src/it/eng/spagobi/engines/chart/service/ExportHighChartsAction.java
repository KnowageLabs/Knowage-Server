/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.service;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.chart.utils.ExportHighCharts;
import it.eng.spagobi.utilities.engines.AbstractEngineAction;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.mime.MimeUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;

/**
 * The Class ExportHighChartsAction.
 * 
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */
public class ExportHighChartsAction extends AbstractEngineAction {

	// INPUT PARAMETERS
	public static String SVG = "svg";
	public static String OUTPUT_FORMAT = "type";

	public static String OUTPUT_FORMAT_PNG = "PNG";
	public static String OUTPUT_FORMAT_JPEG = "JPG";
	public static String OUTPUT_FORMAT_PDF = "PDF";
	public static String OUTPUT_FORMAT_SVG = "SVG+XML";

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(ExportHighChartsAction.class);
	private static final BASE64Decoder DECODER = new BASE64Decoder();

	public static final String ENGINE_NAME = "SpagoBIChartEngine";

	@Override
	public void service(SourceBean request, SourceBean response) {

		logger.debug("IN");
		InputStream inputStream = null;
		OutputStream outputStream = null;

		try {
			super.service(request, response);

			freezeHttpResponse();

			String svg = this.getAttributeAsString(SVG);

			inputStream = new ByteArrayInputStream(svg.getBytes("UTF-8"));
			// inputStream = new
			// ByteArrayInputStream(svg.getBytes("ISO-8859-1"));
			String outputType = this.getAttributeAsString(OUTPUT_FORMAT);
			if (outputType == null || outputType.trim().equals("")) {
				logger.debug("Output format not specified, default is " + OUTPUT_FORMAT_JPEG);
				outputType = OUTPUT_FORMAT_JPEG;
			}

			File exportFile = null;
			String ext = null;
			if (outputType.equalsIgnoreCase(OUTPUT_FORMAT_PNG)) {
				ext = ".png";
				exportFile = File.createTempFile("chart", ext);
				outputStream = new FileOutputStream(exportFile);
				ExportHighCharts.transformSVGIntoPNG(inputStream, outputStream);
			} else if (outputType.equalsIgnoreCase(OUTPUT_FORMAT_JPEG)) {
				ext = ".jpg";
				exportFile = File.createTempFile("chart", ext);
				outputStream = new FileOutputStream(exportFile);
				ExportHighCharts.transformSVGIntoJPEG(inputStream, outputStream);
			} else if (outputType.equalsIgnoreCase(OUTPUT_FORMAT_PDF)) {
				ext = ".pdf";
				exportFile = File.createTempFile("chart", ext);
				outputStream = new FileOutputStream(exportFile);
				ExportHighCharts.transformSVGIntoPDF(inputStream, outputStream);
			} else if (outputType.equalsIgnoreCase(OUTPUT_FORMAT_SVG)) {
				ext = ".svg";
				exportFile = File.createTempFile("chart", ext);
				outputStream = new FileOutputStream(exportFile);
				ExportHighCharts.writeSVG(inputStream, outputStream);
			} else {
				throw new SpagoBIEngineRuntimeException("Output format [" + outputType + "] not supperted");
			}

			String mimetype = MimeUtils.getMimeType(exportFile);

			try {
				writeBackToClient(exportFile, null, false, exportFile.getName(), mimetype);
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

		} catch (Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
			logger.debug("OUT");
		}

	}
}
