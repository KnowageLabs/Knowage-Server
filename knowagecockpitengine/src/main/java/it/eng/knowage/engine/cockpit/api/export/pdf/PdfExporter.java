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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.export.pdf.ExportDetails;
import it.eng.knowage.export.pdf.FrontpageDetails;
import it.eng.knowage.export.pdf.PDFCreator;
import it.eng.knowage.slimerjs.wrapper.DeleteOnCloseFileInputStream;
import it.eng.knowage.slimerjs.wrapper.SlimerJS;
import it.eng.knowage.slimerjs.wrapper.SlimerJSConstants;
import it.eng.knowage.slimerjs.wrapper.beans.RenderOptions;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

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

	private int getSheetCount(BIObject document) {
		try {
			int numOfPages = 0;
			switch (document.getEngineLabel()) {
			case "knowagechartengine":
				numOfPages = 1;
				return numOfPages;
			case "knowagecockpitengine":
				ObjTemplate objTemplate = document.getActiveTemplate();
				if (objTemplate == null) {
					throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
				}
				String templateString = new String(objTemplate.getContent());
				JSONObject template = new JSONObject(templateString);
				JSONArray sheets = template.getJSONArray("sheets");
				numOfPages = sheets.length();
				return numOfPages;

			default:
				return numOfPages;
			}

		} catch (EMFAbstractError e) {
			throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid template for document with id [" + documentId + "]", e);
		}
	}

	private int getSheetHeight(BIObject document) {
		try {
			int sheetHeight = Integer.valueOf(renderOptions.getDimensions().getHeight());
			switch (document.getEngineLabel()) {
			case "knowagechartengine":
				break;
			case "knowagecockpitengine":
				ObjTemplate objTemplate = document.getActiveTemplate();
				if (objTemplate == null) {
					throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
				}
				String templateString = new String(objTemplate.getContent());
				JSONObject template = new JSONObject(templateString);
				JSONArray sheets = template.getJSONArray("sheets");
				int sheetLabelHeigth = (sheets.length() > 0) ? 48 : 0;
				for (int sheetIndex = 0; sheetIndex < sheets.length(); sheetIndex++) {
					JSONObject sheet = (JSONObject) sheets.get(sheetIndex);
					if (sheet.has("widgets")) {
						JSONArray widgets = sheet.getJSONArray("widgets");
						for (int widgetIndex = 0; widgetIndex < widgets.length(); widgetIndex++) {
							JSONObject widget = (JSONObject) widgets.get(widgetIndex);
							int row = widget.getInt("row");
							int sizeY = widget.getInt("sizeY");
							int widgetHeight = (row + sizeY) * 30 + sheetLabelHeigth; // scaling by cockpitModule_gridsterOptions.rowHeight
							sheetHeight = Math.max(sheetHeight, widgetHeight);
						}
					}
				}
				break;
			}
			return sheetHeight;
		} catch (EMFAbstractError e) {
			throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid template for document with id [" + documentId + "]", e);
		}
	}

	private FrontpageDetails getFrontpageDetails(boolean includeFrontPage, BIObject document) {
		FrontpageDetails toReturn = null;

		if (includeFrontPage) {
			String name = document.getName();
			String description = document.getDescription();
			if (name == null || description == null) {
				throw new SpagoBIRuntimeException(
						"Unable to get name [" + name + "] or description [" + description + "] for document with id [" + documentId + "]");
			}
			toReturn = new FrontpageDetails(name, description, new Date());
		}
		return toReturn;
	}
}
