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
package it.eng.spagobi.engines.jasperreport.exporters;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;
import sun.misc.BASE64Encoder;

//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGEncodeParam;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JRImageBase64Exporter extends JRImageExporter {

	@Override
	public void exportReport() throws JRException {
		byte[] bytes;
		List bufferedImages;
		try {
			bytes = new byte[0];
			String message = "<IMAGES>";
			JasperReport report = (JasperReport) getParameter(JRImageExporterParameter.JASPER_REPORT);
			JasperPrint jasperPrint = (JasperPrint) getParameter(JRExporterParameter.JASPER_PRINT);

			bufferedImages = generateReportImages(report, jasperPrint);
			Iterator iterImgs = bufferedImages.iterator();
			int count = 1;
			while (iterImgs.hasNext()) {
				message += "<IMAGE page=\"" + count + "\">";
				BufferedImage image = (BufferedImage) iterImgs.next();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("jpeg").next();
				ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
				imageWriter.setOutput(ios);
				IIOMetadata imageMetaData = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(image), null);
				ImageWriteParam par = imageWriter.getDefaultWriteParam();
				par.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
				par.setCompressionQuality(1.0f);
				imageWriter.write(imageMetaData, new IIOImage(image, null, null), par);

				// JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(baos);
				// JPEGEncodeParam encodeParam = encoder.getDefaultJPEGEncodeParam(image);
				// encodeParam.setQuality(1.0f, true);
				// encoder.setJPEGEncodeParam(encodeParam);
				// encoder.encode(image);

				byte[] byteImg = baos.toByteArray();

				baos.close();
				BASE64Encoder encoder64 = new BASE64Encoder();
				String encodedImage = encoder64.encode(byteImg);

				message += encodedImage;
				message += "</IMAGE>";
				count++;

				imageWriter.dispose();
			}
			message += "</IMAGES>";
			bytes = message.getBytes();

			OutputStream out = (OutputStream) getParameter(JRExporterParameter.OUTPUT_STREAM);
			out.write(bytes);
		} catch (Throwable t) {
			throw new RuntimeException("Error while producing byte64 encoding of the report images", t);
		}
	}

	public ReportContext getReportContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setConfiguration(ReportExportConfiguration arg0) {
		// TODO Auto-generated method stub

	}

	public void setConfiguration(ExporterConfiguration arg0) {
		// TODO Auto-generated method stub

	}

	public void setExporterInput(ExporterInput arg0) {
		// TODO Auto-generated method stub

	}

	public void setExporterOutput(ExporterOutput arg0) {
		// TODO Auto-generated method stub

	}

	public void setReportContext(ReportContext arg0) {
		// TODO Auto-generated method stub

	}

}
