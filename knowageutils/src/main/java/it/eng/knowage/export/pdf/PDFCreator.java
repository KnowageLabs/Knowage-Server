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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.ConfigurationConstants;
import it.eng.spagobi.commons.utilities.StringUtilities;

/**
 * @authors Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public abstract class PDFCreator {

	private static Logger logger = Logger.getLogger(PDFCreator.class);

	public final static String DEFAULT_FRONT_PAGE_RESOURCE_PATH = "it/eng/knowage/slimerjs/wrapper/Export_Front.pdf";
	public final static String DEFAULT_BACK_PAGE_RESOURCE_PATH = "it/eng/knowage/slimerjs/wrapper/Export_Back.pdf";

	private final static String TEMP_SUFFIX = ".temp.pdf";
	private final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private final static SimpleDateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

	private static void createPDF(List<InputStream> inputImages, Path output) throws IOException {
		PDDocument document = new PDDocument();
		try {
			for (InputStream is : inputImages) {
				BufferedImage bimg = ImageIO.read(is);
				float width = bimg.getWidth();
				float height = bimg.getHeight();
				PDPage page = new PDPage(new PDRectangle(width, height));
				document.addPage(page);

				PDImageXObject img = LosslessFactory.createFromImage(document, bimg);
				try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
					contentStream.drawImage(img, 0, 0);
				}
			}
			document.save(output.toFile());
		} finally {
			document.close();
		}
	}

	public static void createPDF(List<InputStream> inputImages, Path output, boolean front, boolean back) throws IOException {
		try (final InputStream frontpage = getFrontpage()) {
			try (final InputStream backapage = getBackpage()) {
				if (front && back) {
					createPDF(inputImages, output, frontpage, backapage);
				} else {
					if (front) {
						createPDF(inputImages, output, frontpage, null);
					} else {
						if (back) {
							createPDF(inputImages, output, null, backapage);
						} else {
							createPDF(inputImages, output);
						}
					}
				}
			}
		}
	}

	private static void createPDF(List<InputStream> inputImages, Path output, final InputStream front, final InputStream back) throws IOException {
		final String path = output.toString();
		final Path tempOutput = Paths.get(FilenameUtils.removeExtension(path) + TEMP_SUFFIX);
		createPDF(inputImages, tempOutput);
		try (final FileOutputStream fos = new FileOutputStream(output.toFile())) {
			try (final FileInputStream fis = new FileInputStream(tempOutput.toFile())) {
				mergePDF(fos, front, fis, back);
			}
			Files.deleteIfExists(tempOutput);
		}
	}

	private static void mergePDF(OutputStream output, InputStream... contents) throws IOException {
		// Instantiating PDFMergerUtility class
		PDFMergerUtility merger = new PDFMergerUtility();

		// Setting the destination file
		merger.setDestinationStream(output);

		for (int i = 0; i < contents.length; i++) {
			// adding the source files
			if (contents[i] != null) {
				merger.addSource(contents[i]);
			}
		}
		// Merging the documents
		merger.mergeDocuments(null);
	}

	/**
	 * create the second sample document from the PDF file format specification.
	 *
	 * @param input
	 *            The PDF path to add the information to.
	 * @param details
	 *            The details to be added.
	 *
	 * @throws IOException
	 *             If there is an error writing the data.
	 */
	public static void addInformation(Path input, ExportDetails details) throws IOException {
		try (PDDocument doc = PDDocument.load(input.toFile())) {
			if (details.getPageNumbering() != null) {
				writePageNumbering(doc, PDType1Font.HELVETICA_BOLD, 16.0f, details.getPageNumbering());
			}
			if (details.getFrontpageDetails() != null) {
				writeFrontpageDetails(doc, PDType1Font.HELVETICA_BOLD, 18.0f, details.getFrontpageDetails());
			}
			doc.save(input.toFile());
		}
	}

	private static void drawRect(PDPageContentStream contentStream, Color color, Rectangle rect, boolean fill) throws IOException {
		contentStream.addRect(rect.x, rect.y, rect.width, rect.height);
		if (fill) {
			contentStream.setNonStrokingColor(color);
			contentStream.fill();
		} else {
			contentStream.setStrokingColor(color);
			contentStream.stroke();
		}
	}

	private static void writeText(PDPageContentStream contentStream, Color color, PDFont font, float fontSize, boolean rotate, float x, float y, String... text)
			throws IOException {
		contentStream.beginText();
		// set font and font size
		contentStream.setFont(font, fontSize);
		// set text color
		contentStream.setNonStrokingColor(color.getRed(), color.getGreen(), color.getBlue());
		if (rotate) {
			// rotate the text according to the page rotation
			contentStream.setTextMatrix(Matrix.getRotateInstance(Math.PI / 2, x, y));
		} else {
			contentStream.setTextMatrix(Matrix.getTranslateInstance(x, y));
		}
		if (text.length > 1) {
			contentStream.setLeading(25f);

		}
		for (String line : text) {
			contentStream.showText(line);
			contentStream.newLine();
		}
		contentStream.endText();
	}

	private static void writePageNumbering(PDDocument doc, PDFont font, float fontSize, PageNumbering pageNumbering) throws IOException {
		int totalPages = doc.getNumberOfPages();
		int numberOfPages = pageNumbering.isLastIncluded() ? doc.getNumberOfPages() : doc.getNumberOfPages() - 1;
		for (int pageIndex = pageNumbering.isFirstIncluded() ? 0 : 1; pageIndex < numberOfPages; pageIndex++) {
			String footer = "Page " + (pageIndex + 1) + " of " + totalPages;
			PDPage page = doc.getPage(pageIndex);
			PDRectangle pageSize = page.getMediaBox();
			float stringWidth = font.getStringWidth(footer) * fontSize / 1000f;
			float stringHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() * fontSize / 1000f;

			int rotation = page.getRotation();
			boolean rotate = rotation == 90 || rotation == 270;
			float pageWidth = rotate ? pageSize.getHeight() : pageSize.getWidth();
			float pageHeight = rotate ? pageSize.getWidth() : pageSize.getHeight();
			float startX = rotate ? pageHeight / 2f : (pageWidth - stringWidth - stringHeight) / 2f;
			float startY = rotate ? (pageWidth - stringWidth) : stringHeight;

			// append the content to the existing stream
			try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true)) {

				// draw rectangle
				contentStream.setNonStrokingColor(255, 255, 255); // gray background
				// Draw a white filled rectangle
				drawRect(contentStream, Color.WHITE, new java.awt.Rectangle((int) startX, (int) startY - 3, (int) stringWidth + 2, (int) stringHeight), true);
				writeText(contentStream, new Color(4, 44, 86), font, fontSize, rotate, startX, startY, footer);
			}
		}
	}

	private static void writeFrontpageDetails(PDDocument doc, PDFont font, float fontSize, FrontpageDetails details) throws IOException {
		String name = "Name: " + details.getName();
		String description = "Description: " + details.getDescription();
		String date = "Date: " + DEFAULT_DATE_FORMATTER.format(details.getDate());
		PDPage page = doc.getPage(0);
		PDRectangle pageSize = page.getMediaBox();
		float stringWidth = font.getStringWidth(StringUtilities.findLongest(name, description, date)) * fontSize / 1000f;
		float stringHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() * fontSize / 1000f;

		int rotation = page.getRotation();
		boolean rotate = rotation == 90 || rotation == 270;
		float pageWidth = rotate ? pageSize.getHeight() : pageSize.getWidth();
		float pageHeight = rotate ? pageSize.getWidth() : pageSize.getHeight();
		float startX = rotate ? pageHeight / 3f : (pageWidth - stringWidth - stringHeight) / 3f;
		float startY = rotate ? (pageWidth - stringWidth) / 1f : pageWidth / 0.9f;

		// append the content to the existing stream
		try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true)) {
			// draw rectangle
			writeText(contentStream, new Color(4, 44, 86), font, fontSize, rotate, startX, startY, name, description, date);
		}
	}

	private static InputStream getFrontpage() throws FileNotFoundException {
		String path = SingletonConfig.getInstance().getConfigValue(ConfigurationConstants.DOCUMENT_EXPORTING_PDF_FRONT_PAGE);
		return (path == null || path.isEmpty())
				? Thread.currentThread().getContextClassLoader().getResourceAsStream(PDFCreator.DEFAULT_FRONT_PAGE_RESOURCE_PATH)
				: new FileInputStream(path);
	}

	private static InputStream getBackpage() throws FileNotFoundException {
		String path = SingletonConfig.getInstance().getConfigValue(ConfigurationConstants.DOCUMENT_EXPORTING_PDF_BACK_PAGE);
		return (path == null || path.isEmpty()) ? Thread.currentThread().getContextClassLoader().getResourceAsStream(PDFCreator.DEFAULT_BACK_PAGE_RESOURCE_PATH)
				: new FileInputStream(path);
	}
}
