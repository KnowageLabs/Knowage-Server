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
package it.eng.knowage.engine.cockpit.api.export.pdf;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import it.eng.knowage.export.pdf.ExportDetails;
import it.eng.knowage.export.pdf.PDFCreator;
import it.eng.knowage.slimerjs.wrapper.DeleteOnCloseFileInputStream;
import it.eng.knowage.slimerjs.wrapper.SlimerJS;
import it.eng.knowage.slimerjs.wrapper.SlimerJSConstants;
import it.eng.knowage.slimerjs.wrapper.beans.RenderOptions;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.DAOFactory;

/**
 * @authors Francesco Lucchi (francesco.lucchi@eng.it)
 *
 * @deprecated See {@link PdfExporterV2}
 *
 *             TODO : To delete and substitute with {@link PdfExporterV2}
 */
@Deprecated
public class PdfExporter extends AbstractPdfExporter {

	private static final Logger logger = Logger.getLogger(PdfExporter.class);

	public PdfExporter(int documentId, String userId, String requestUrl, RenderOptions renderOptions, String pageOrientation, boolean pdfFrontPage,
			boolean pdfBackPage) {
		super(documentId, userId, requestUrl, renderOptions, pageOrientation, pdfFrontPage, pdfBackPage);
	}

	@Override
	public byte[] getBinaryData() throws Exception {
		List<InputStream> images = null;
		try {
			Path output = Paths.get(SlimerJSConstants.TEMP_RENDER_DIR.toString(), UUID.randomUUID().toString() + ".pdf");

			BIObject document = DAOFactory.getBIObjectDAO().loadBIObjectById(documentId);
			int sheetCount = getSheetCount(document);
			int sheetHeight = getSheetHeight(document);
			RenderOptions renderOptionsWithFixedHeight = renderOptions.withDimensions(renderOptions.getDimensions().withHeight(sheetHeight));
			URL url = new URL(requestUrl);
			Map<String, String> authenticationHeaders = new HashMap<>(1);
			String encodedUserId = Base64.encodeBase64String(userId.getBytes("UTF-8"));
			authenticationHeaders.put("Authorization", "Direct " + encodedUserId);
			images = SlimerJS.render(url, sheetCount, renderOptionsWithFixedHeight);
			PDFCreator.createPDF(images, output, pdfFrontPage, pdfBackPage);

			// PageNumbering pageNumbering = new PageNumbering(!pdfFrontPage, true, !pdfBackPage);
			ExportDetails details = new ExportDetails(getFrontpageDetails(pdfFrontPage, document), null);
			PDFCreator.addInformation(output, details);
			try (InputStream is = new DeleteOnCloseFileInputStream(output.toFile())) {
				return IOUtils.toByteArray(is);
			}
		} finally {
			if (images != null) {
				for (InputStream is : images) {
					IOUtils.closeQuietly(is);
				}
			}
		}
	}
}
