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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import sun.misc.BASE64Encoder;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JRImageBase64Exporter extends JRImageExporter {

	public void exportReport() throws JRException {
		byte[] bytes;
		List bufferedImages;
		try {
			bytes = new byte[0];
			String message = "<IMAGES>";
			JasperReport report = (JasperReport)getParameter(JRImageExporterParameter.JASPER_REPORT);
			JasperPrint jasperPrint = (JasperPrint)getParameter(JRExporterParameter.JASPER_PRINT);
			
			bufferedImages = generateReportImages(report, jasperPrint);
			Iterator iterImgs = bufferedImages.iterator();
			int count = 1;
			while(iterImgs.hasNext()){
				message += "<IMAGE page=\""+count+"\">";
				BufferedImage image = (BufferedImage)iterImgs.next();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(baos);
				JPEGEncodeParam encodeParam = encoder.getDefaultJPEGEncodeParam(image);
				encodeParam.setQuality(1.0f, true);
				encoder.setJPEGEncodeParam(encodeParam);
				encoder.encode(image);
				byte[] byteImg = baos.toByteArray();
				baos.close();
				BASE64Encoder encoder64 = new BASE64Encoder();
				String encodedImage = encoder64.encode(byteImg);
				message += encodedImage;
				message += "</IMAGE>";
				count ++;
			}
			message += "</IMAGES>";
			bytes = message.getBytes();
			
			OutputStream out = (OutputStream)getParameter(JRExporterParameter.OUTPUT_STREAM);
			out.write(bytes);
		} catch (Throwable t) {
			throw new RuntimeException("Error while producing byte64 encoding of the report images", t);
		}
	}

}
