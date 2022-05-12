package it.eng.knowage.engine.cockpit.api.export.png;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import it.eng.knowage.engine.cockpit.api.export.pdf.nodejs.AbstractNodeJSBasedExporter;
import it.eng.knowage.export.wrapper.beans.RenderOptions;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;

public class PngExporter extends AbstractNodeJSBasedExporter {

	private static final Logger logger = Logger.getLogger(PngExporter.class);

	public PngExporter(int documentId, String userId, String requestUrl, RenderOptions renderOptions, String pdfPageOrientation, boolean pdfFrontPage,
			boolean pdfBackPage) {
		super(documentId, userId, requestUrl, renderOptions, pdfPageOrientation, pdfFrontPage, pdfBackPage);
	}

	@Override
	protected byte[] handleFile(Path outputDir, BIObject document, List<InputStream> imagesInputStreams) throws IOException {
		logger.debug("IN");
		byte[] bytes = null;
		if (imagesInputStreams.size() == 1) {
			bytes = IOUtils.toByteArray(imagesInputStreams.get(0));
		} else {
			bytes = zipBytes(imagesInputStreams);
		}

		return bytes;
	}

	public byte[] zipBytes(List<InputStream> imagesInputStreams) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		int i = 0;
		for (InputStream inputStream : imagesInputStreams) {

			ZipEntry ze = new ZipEntry("sheet_" + i++ + ".png");
			zos.putNextEntry(ze);
			byte[] imageBytes = new byte[1024];
			int count = inputStream.read(imageBytes);
			while (count > -1) {
				zos.write(imageBytes, 0, count);
				count = inputStream.read(imageBytes);
			}
			inputStream.close();
			zos.closeEntry();
		}

		zos.close();

		return baos.toByteArray();
	}

}
