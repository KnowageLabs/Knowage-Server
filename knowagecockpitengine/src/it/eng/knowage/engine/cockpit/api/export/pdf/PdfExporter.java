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

import it.eng.knowage.export.pdf.ExportDetails;
import it.eng.knowage.export.pdf.FrontpageDetails;
import it.eng.knowage.export.pdf.PDFCreator;
import it.eng.knowage.export.pdf.PageNumbering;
import it.eng.knowage.slimerjs.wrapper.SlimerJS;
import it.eng.knowage.slimerjs.wrapper.beans.CustomHeaders;
import it.eng.knowage.slimerjs.wrapper.beans.RenderOptions;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */

public class PdfExporter {

	private static final Logger logger = Logger.getLogger(PdfExporter.class);

	private final int documentId;
	private final String userId;
	private final String requestUrl;
	private final RenderOptions renderOptions;
	String pageOrientation;
	boolean pdfFrontPage;
	boolean pdfBackPage;

	public PdfExporter(int documentId, String userId, String requestUrl, RenderOptions renderOptions, String pageOrientation, boolean pdfFrontPage,
			boolean pdfBackPage) {
		this.documentId = documentId;
		this.userId = userId;
		this.requestUrl = requestUrl;
		this.renderOptions = renderOptions;
		this.pageOrientation = pageOrientation;
		this.pdfFrontPage = pdfFrontPage;
		this.pdfBackPage = pdfBackPage;
	}

	public byte[] getBinaryData() throws Exception {
		Path front = Paths.get("D:\\Knowage\\Workspace\\Trunk\\slimerjs-wrapper\\resources\\it\\eng\\knowage\\slimerjs\\wrapper\\Export_Front.pdf");
		Path back = Paths.get("D:\\Knowage\\Workspace\\Trunk\\slimerjs-wrapper\\resources\\it\\eng\\knowage\\slimerjs\\wrapper\\Export_Back.pdf");
		Path output = Paths.get("C:\\temp\\" + UUID.randomUUID().toString() + ".pdf");

		int sheetCount = getSheetCount();
		URL url = new URL(requestUrl);
		Map<String, String> authenticationHeaders = new HashMap<String, String>(1);
		String encodedUserId = Base64.encodeBase64String(userId.getBytes("UTF-8"));
		authenticationHeaders.put("Authorization", "Direct " + encodedUserId);
		List<InputStream> images = SlimerJS.render(url, sheetCount, RenderOptions.DEFAULT.withCustomHeaders(new CustomHeaders(authenticationHeaders))
				.withJavaScriptExecutionDetails(5000L, 15000L));
		PDFCreator.createPDF(images, output, front, back);
		ExportDetails details = new ExportDetails(new FrontpageDetails("Cool dashboard", "The most cool dashboard on earth", new Date()), PageNumbering.DEFAULT);
		PDFCreator.addInformation(output, details);
		return Files.readAllBytes(output);
	}

	private int getSheetCount() {
		try {
			ObjTemplate objTemplate = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(documentId);
			if (objTemplate == null) {
				throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
			}

			String templateString = new String(objTemplate.getContent());
			JSONObject template = new JSONObject(templateString);
			JSONArray sheets = template.getJSONArray("sheets");

			return sheets.length();
		} catch (EMFAbstractError e) {
			throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid template for document with id [" + documentId + "]", e);
		}
	}
}
