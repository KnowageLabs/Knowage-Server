/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

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

package it.eng.knowage.export.pdf;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * @authors Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public abstract class PDFCreator {

	private static Logger logger = Logger.getLogger(PDFCreator.class);

	private final static String TEMP_SUFFIX = ".temp.pdf";

	public static void createPDF(List<InputStream> inputImages, Path output) throws IOException {
		PDDocument document = new PDDocument();
		try {
			for (InputStream is : inputImages) {
				BufferedImage bimg = ImageIO.read(is);
				float width = bimg.getWidth();
				float height = bimg.getHeight();
				PDPage page = new PDPage(new PDRectangle(width, height));
				document.addPage(page);

				PDImageXObject img = LosslessFactory.createFromImage(document, bimg);
				PDPageContentStream contentStream = new PDPageContentStream(document, page);
				contentStream.drawImage(img, 0, 0);
				contentStream.close();
				is.close();
			}
			document.save(output.toFile());
		} finally {
			document.close();
		}
	}

	public static void createPDF(List<InputStream> inputImages, Path output, Path front, Path back) throws IOException {
		String path = output.toString();
		Path tempOutput = Paths.get(FilenameUtils.removeExtension(path) + TEMP_SUFFIX);
		createPDF(inputImages, tempOutput);
		mergePDF(output, front, tempOutput, back);
		Files.deleteIfExists(tempOutput);
	}

	public static void mergePDF(Path output, Path... contents) throws IOException {

		// Instantiating PDFMergerUtility class
		PDFMergerUtility merger = new PDFMergerUtility();

		// Setting the destination file
		merger.setDestinationStream(new FileOutputStream(output.toFile()));

		for (int i = 0; i < contents.length; i++) {
			// adding the source files
			merger.addSource(contents[i].toFile());
		}
		// Merging the documents
		merger.mergeDocuments(null);

		logger.debug("Documents merged");
	}

}
