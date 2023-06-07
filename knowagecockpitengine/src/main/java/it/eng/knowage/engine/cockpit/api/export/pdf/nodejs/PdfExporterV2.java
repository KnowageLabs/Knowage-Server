package it.eng.knowage.engine.cockpit.api.export.pdf.nodejs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import it.eng.knowage.export.pdf.ExportDetails;
import it.eng.knowage.export.pdf.PDFCreator;
import it.eng.knowage.export.wrapper.beans.RenderOptions;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;

public class PdfExporterV2 extends AbstractNodeJSBasedExporter {

	private static final String OUTPUT_PDF = "output.pdf";
	private static final Logger LOGGER = Logger.getLogger(PdfExporterV2.class);

	public PdfExporterV2(int documentId, String userId, String requestUrl, RenderOptions renderOptions, String pageOrientation, boolean pdfFrontPage,
			boolean pdfBackPage) {
		super(documentId, userId, requestUrl, renderOptions, pageOrientation, pdfFrontPage, pdfBackPage);
	}

	@Override
	protected byte[] handleFile(final Path outputDir, BIObject document, final List<InputStream> imagesInputStreams) throws IOException {
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
