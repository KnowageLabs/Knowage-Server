package it.eng.knowage.engine.cockpit.api.export.pdf;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.export.pdf.FrontpageDetails;
import it.eng.knowage.slimerjs.wrapper.beans.RenderOptions;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Common class between PDF exporter.
 * 
 * @author Marco Libanori
 */
public abstract class AbstractPdfExporter {

	protected final int documentId;

	public abstract byte[] getBinaryData() throws Exception;

	protected final String userId;
	protected final String requestUrl;
	protected final String pageOrientation;
	protected final boolean pdfFrontPage;
	protected final boolean pdfBackPage;
	protected final RenderOptions renderOptions;

	public AbstractPdfExporter(int documentId, String userId, String requestUrl, RenderOptions renderOptions, String pageOrientation, boolean pdfFrontPage,
			boolean pdfBackPage) {
		super();
		this.documentId = documentId;
		this.userId = userId;
		this.requestUrl = requestUrl;
		this.renderOptions = renderOptions;
		this.pageOrientation = pageOrientation;
		this.pdfFrontPage = pdfFrontPage;
		this.pdfBackPage = pdfBackPage;
	}

	protected int getSheetCount(BIObject document) {
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

		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid template for document with id [" + documentId + "]", e);
		}
	}

	protected int getSheetHeight(BIObject document) {
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
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid template for document with id [" + documentId + "]", e);
		}
	}

	protected int getSheetWidth(BIObject document) {
		try {
			int sheetWidth = Integer.valueOf(renderOptions.getDimensions().getWidth());
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
							int widgetWidth = (row + sizeY) * 30 + sheetLabelHeigth; // scaling by cockpitModule_gridsterOptions.rowWidth
							sheetWidth = Math.max(sheetWidth, widgetWidth);
						}
					}
				}
				break;
			}
			return sheetWidth;
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid template for document with id [" + documentId + "]", e);
		}
	}

	protected FrontpageDetails getFrontpageDetails(boolean includeFrontPage, BIObject document) {
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