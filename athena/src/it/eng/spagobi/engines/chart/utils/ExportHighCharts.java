/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.utils;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.ImgTemplate;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * The Class ExportHighChartsAction.
 * 
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */
public class ExportHighCharts {

	// INPUT PARAMETERS
	public static String SVG = "svg";
	public static String OUTPUT_FORMAT = "type";

	public static String OUTPUT_FORMAT_PNG = "PNG";
	public static String OUTPUT_FORMAT_JPEG = "JPG";
	public static String OUTPUT_FORMAT_PDF = "PDF";
	public static String OUTPUT_FORMAT_SVG = "SVG+XML";

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(ExportHighCharts.class);
	private static final BASE64Decoder DECODER = new BASE64Decoder();

	public static final String ENGINE_NAME = "SpagoBIChartEngine";

	public static void transformSVGIntoPDF(InputStream inputStream, OutputStream outputStream) throws IOException, DocumentException {

		Rectangle pageSize = PageSize.A4;
		Document document = new Document(pageSize);
		int orientation = PageFormat.PORTRAIT;
		try {
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			document.open();

			double a4WidthInch = 8.26771654; // Equals 210mm
			double a4HeightInch = 11.6929134; // Equals 297mm

			Paper paper = new Paper();
			// 72 DPI
			paper.setSize((a4WidthInch * 72), (a4HeightInch * 72));
			// 1 inch margins
			paper.setImageableArea(72, 72, (a4WidthInch * 72 - 144), (a4HeightInch * 72 - 144));
			PageFormat pageFormat = new PageFormat();
			pageFormat.setPaper(paper);
			pageFormat.setOrientation(orientation);

			float width = ((float) pageFormat.getWidth());
			float height = ((float) pageFormat.getHeight());

			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate template = cb.createTemplate(width, height);
			Graphics2D g2 = template.createGraphics(width, height);

			PrintTranscoder prm = new PrintTranscoder();
			TranscoderInput ti = new TranscoderInput(inputStream);
			prm.transcode(ti, null);

			prm.print(g2, pageFormat, 0);
			g2.dispose();

			ImgTemplate img = new ImgTemplate(template);
			img.setWidthPercentage(100);
			img.setAlignment(Image.ALIGN_CENTER);

			document.add(img);

		} catch (DocumentException e) {
			logger.error("Error exporting Highcharts to PDF: " + e);
		}
		document.close();
	}

	/**
	 * Set the dimension of the image to fit the A4 page size The layout of the
	 * page should be horizontal
	 * 
	 * @param jpg
	 *            the image to fit
	 */
	public static void fitImage(Image jpg) {
		if (jpg.getWidth() > PageSize.A4.getHeight()) {
			float imgScaledWidth = PageSize.A4.getHeight() - 100;
			float imgScaledHeight = (imgScaledWidth / jpg.getWidth()) * jpg.getHeight();
			jpg.scaleAbsolute(imgScaledWidth, imgScaledHeight);
		}
		if (jpg.getHeight() > PageSize.A4.getWidth()) {
			float imgScaledHeight = PageSize.A4.getWidth() - 100;
			float imgScaledWidth = (imgScaledHeight / jpg.getHeight()) * jpg.getWidth();
			jpg.scaleAbsolute(imgScaledWidth, imgScaledHeight);
		}
	}

	public static void writeSVG(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buf = new byte[1024];
		int b = -1;
		while ((b = inputStream.read(buf)) != -1) {
			outputStream.write(buf, 0, b);
		}
	}

	public static void transformSVGIntoJPEG(InputStream inputStream, OutputStream outputStream) {
		// create a JPEG transcoder
		JPEGTranscoder t = new JPEGTranscoder();

		// set the transcoding hints
		t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(1));
		t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(1000));
		t.addTranscodingHint(JPEGTranscoder.KEY_ALLOWED_SCRIPT_TYPES, "*");
		t.addTranscodingHint(JPEGTranscoder.KEY_CONSTRAIN_SCRIPT_ORIGIN, new Boolean(true));
		t.addTranscodingHint(JPEGTranscoder.KEY_EXECUTE_ONLOAD, new Boolean(true));

		// create the transcoder input
		Reader reader = new InputStreamReader(inputStream);
		TranscoderInput input = new TranscoderInput(reader);

		// create the transcoder output
		TranscoderOutput output = new TranscoderOutput(outputStream);

		// save the image
		try {
			t.transcode(input, output);
		} catch (TranscoderException e) {
			logger.error("Impossible to convert svg to jpeg: " + e.getCause(), e);
			throw new SpagoBIEngineRuntimeException("Impossible to convert svg to jpeg: " + e.getCause(), e);
		}
	}

	public static void transformSVGIntoPNG(InputStream inputStream, OutputStream outputStream) {
		// create a PNG transcoder
		PNGTranscoder t = new PNGTranscoder();

		// set the transcoding hints
		t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, new Float(1000));
		t.addTranscodingHint(PNGTranscoder.KEY_ALLOWED_SCRIPT_TYPES, "*");
		t.addTranscodingHint(PNGTranscoder.KEY_CONSTRAIN_SCRIPT_ORIGIN, new Boolean(true));
		t.addTranscodingHint(PNGTranscoder.KEY_EXECUTE_ONLOAD, new Boolean(true));
		t.addTranscodingHint(PNGTranscoder.KEY_DEFAULT_FONT_FAMILY, "Arial");

		// create the transcoder input
		Reader reader = new InputStreamReader(inputStream);
		TranscoderInput input = new TranscoderInput(reader);

		// save the image
		try {
			// create the transcoder output
			TranscoderOutput output = new TranscoderOutput(outputStream);
			t.transcode(input, output);
		} catch (TranscoderException e) {
			logger.error("Impossible to convert svg to png: " + e.getCause(), e);
			throw new SpagoBIEngineRuntimeException("Impossible to convert svg to png: " + e.getCause(), e);
		}
	}

}
