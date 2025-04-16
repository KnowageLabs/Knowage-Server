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
import org.json.JSONException;

import it.eng.knowage.engine.cockpit.api.export.pdf.nodejs.AbstractNodeJSBasedExporter;
import it.eng.knowage.export.wrapper.beans.RenderOptions;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;

public class PngExporter extends AbstractNodeJSBasedExporter {

	private static final Logger LOGGER = Logger.getLogger(PngExporter.class);

	public PngExporter(int documentId, String userId, String requestUrl, RenderOptions renderOptions,
			String pdfPageOrientation, boolean pdfFrontPage, boolean pdfBackPage, String role, String organization) throws EMFUserError, JSONException {
		super(documentId, userId, requestUrl, renderOptions, pdfPageOrientation, pdfFrontPage, pdfBackPage, role, organization);
	}

	@Override
	protected byte[] handleFile(Path outputDir, BIObject document, List<InputStream> imagesInputStreams)
			throws IOException {
		LOGGER.debug("IN");
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
