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
	private static final Logger logger = Logger.getLogger(PdfExporterV2.class);

	public PdfExporterV2(int documentId, String userId, String requestUrl, RenderOptions renderOptions, String pageOrientation, boolean pdfFrontPage,
			boolean pdfBackPage) {
		super(documentId, userId, requestUrl, renderOptions, pageOrientation, pdfFrontPage, pdfBackPage);
	}

	@Override
	protected byte[] handleFile(final Path outputDir, BIObject document, final List<InputStream> imagesInputStreams) throws IOException {
		logger.debug("IN");
		final Path outputFile = outputDir.resolve(OUTPUT_PDF);
		logger.debug("OutputFile: " + OUTPUT_PDF);

		logger.debug("createPDF - IN");
		PDFCreator.createPDF(imagesInputStreams, outputFile, false, false);
		logger.debug("createPDF - OUT");

		logger.debug("ExportDetails - getFrontpageDetails - IN");
		ExportDetails details = new ExportDetails(getFrontpageDetails(pdfFrontPage, document), null);
		logger.debug("ExportDetails - getFrontpageDetails - OUT");

		logger.debug("addInformation - IN");
		PDFCreator.addInformation(outputFile, details);
		logger.debug("addInformation - OUT");

		byte[] byteArray = null;
		try (InputStream is = Files.newInputStream(outputFile, StandardOpenOption.DELETE_ON_CLOSE)) {
			logger.debug("inputStreamToByteArray - IN");
			byteArray = IOUtils.toByteArray(is);
			logger.debug("inputStreamToByteArray - OUT");
		}

		return byteArray;
	}

}
