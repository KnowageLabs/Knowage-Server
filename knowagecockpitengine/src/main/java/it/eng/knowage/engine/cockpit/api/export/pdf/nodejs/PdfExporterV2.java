/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2024 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.engine.cockpit.api.export.pdf.nodejs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;

import it.eng.knowage.export.pdf.ExportDetails;
import it.eng.knowage.export.pdf.PDFCreator;
import it.eng.knowage.export.wrapper.beans.RenderOptions;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;

public class PdfExporterV2 extends AbstractNodeJSBasedExporter {

	private static final String OUTPUT_PDF = "output.pdf";
	private static final Logger LOGGER = Logger.getLogger(PdfExporterV2.class);

	public PdfExporterV2(int documentId, String userId, String requestUrl, RenderOptions renderOptions,
			String pageOrientation, boolean pdfFrontPage, boolean pdfBackPage) throws EMFUserError, JSONException {
		super(documentId, userId, requestUrl, renderOptions, pageOrientation, pdfFrontPage, pdfBackPage);
	}

	@Override
	protected byte[] handleFile(final Path outputDir, BIObject document, final List<InputStream> imagesInputStreams)
			throws IOException {
		LOGGER.debug("IN");
		final Path outputFile = outputDir.resolve(OUTPUT_PDF);
		LOGGER.debug("OutputFile: " + OUTPUT_PDF);

		LOGGER.debug("createPDF - IN");
		PDFCreator.createPDF(imagesInputStreams, outputFile, false, false);
		LOGGER.debug("createPDF - OUT");

		LOGGER.debug("ExportDetails - getFrontpageDetails - IN");
		ExportDetails details = new ExportDetails(getFrontpageDetails(pdfFrontPage, document), null);
		LOGGER.debug("ExportDetails - getFrontpageDetails - OUT");

		LOGGER.debug("addInformation - IN");
		PDFCreator.addInformation(outputFile, details);
		LOGGER.debug("addInformation - OUT");

		byte[] byteArray = null;
		try (InputStream is = Files.newInputStream(outputFile, StandardOpenOption.DELETE_ON_CLOSE)) {
			LOGGER.debug("inputStreamToByteArray - IN");
			byteArray = IOUtils.toByteArray(is);
			LOGGER.debug("inputStreamToByteArray - OUT");
		}

		return byteArray;
	}

}
