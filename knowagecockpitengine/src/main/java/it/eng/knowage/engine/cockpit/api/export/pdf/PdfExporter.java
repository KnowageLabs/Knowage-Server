/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.engine.cockpit.api.export.pdf;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;
import it.eng.knowage.engine.cockpit.api.export.AbstractFormatExporter;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class PdfExporter extends AbstractFormatExporter {

	static private Logger logger = Logger.getLogger(PdfExporter.class);
	private static final float POINTS_PER_INCH = 72;
	private static final float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;
	private final static int DEFAULT_COLUMN_WIDTH = 150;
	private float totalColumnsWidth = 0;
	private float[] columnPercentWidths;
	private List<Integer> pdfHiddenColumns;

	public PdfExporter(String userUniqueIdentifier, JSONObject body) {
		super(userUniqueIdentifier, body);
	}

	public byte[] getBinaryData(Integer documentId, String documentLabel, String templateString) throws JSONException, SerializationException {
		if (templateString == null) {
			ObjTemplate template = null;
			String message = "Unable to get template for document with id [" + documentId + "] and label [" + documentLabel + "]";
			try {
				if (documentId != null && documentId.intValue() != 0)
					template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(documentId);
				else if (documentLabel != null && !documentLabel.isEmpty())
					template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplateByLabel(documentLabel);

				if (template == null)
					throw new SpagoBIRuntimeException(message);

				templateString = new String(template.getContent());
			} catch (EMFAbstractError e) {
				throw new SpagoBIRuntimeException(message, e);
			}
		}

		try (PDDocument document = new PDDocument()) {
			long widgetId = body.getLong("widget");

			exportTableWidget(document, templateString, widgetId);

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			document.save(byteArrayOutputStream);
			document.close();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Unable to generate output file", e);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot export data to PDF", e);
		}

	}

	private void exportTableWidget(PDDocument document, String templateString, long widgetId) {
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			JSONObject settings = widget.optJSONObject("settings");
			JSONObject style = widget.optJSONObject("style");

			int offset = 0;
			int fetchSize = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
			JSONObject dataStore = this.getDataStoreForWidget(template, widget, offset, fetchSize);
			int totalNumberOfRows = dataStore.getInt("results");
			PDPage page = createPage(settings, widget);
			document.addPage(page);
			while (offset < totalNumberOfRows) {
				this.fillPageWithData(dataStore, document, page, style, settings);
				offset += fetchSize;
				dataStore = this.getDataStoreForWidget(template, widget, offset, fetchSize);
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Unable to export generic widget: " + widgetId, e);
		}
	}

	protected void fillPageWithData(JSONObject dataStore, PDDocument pdDoc, PDPage pdPage, JSONObject style, JSONObject settings) {
		try {

			JSONObject metadata = dataStore.getJSONObject("metaData");
			JSONArray columns = metadata.getJSONArray("fields");
			columns = filterDataStoreColumns(columns);
			JSONArray rows = dataStore.getJSONArray("rows");

			JSONObject widgetData = dataStore.getJSONObject("widgetData");
			JSONObject widgetContent = widgetData.getJSONObject("content");
			HashMap<String, String> arrayHeader = new HashMap<String, String>();
			for (int i = 0; i < widgetContent.getJSONArray("columnSelectedOfDataset").length(); i++) {
				JSONObject column = widgetContent.getJSONArray("columnSelectedOfDataset").getJSONObject(i);
				String key;
				if (column.optBoolean("isCalculated") && !column.has("name")) {
					key = column.getString("alias");
				} else {
					key = column.getString("name");
				}
				arrayHeader.put(key, column.getString("aliasToShow"));
			}

			hiddenColumns = getHiddenColumnsList(widgetContent.getJSONArray("columnSelectedOfDataset"));
			JSONArray columnsOrdered = getTableOrderedColumns(widgetContent.getJSONArray("columnSelectedOfDataset"), columns);
			pdfHiddenColumns = getPdfHiddenColumnsList(columnsOrdered, widgetContent.getJSONArray("columnSelectedOfDataset"));

			JSONObject[] columnStyles = getColumnsStyles(columnsOrdered, widgetContent);
			String[] columnDateFormats = getColumnDateFormats(columnsOrdered, widgetContent);
			initColumnWidths(columnStyles, columnsOrdered.length());

			BaseTable table = createBaseTable(pdDoc, pdPage);
			Row<PDPage> headerRow = table.createRow(15f);

			for (int i = 0; i < columnsOrdered.length(); i++) {

				if (pdfHiddenColumns.contains(i))
					continue;

				JSONObject column = columnsOrdered.getJSONObject(i);
				String columnName = column.getString("header");
				if (arrayHeader.get(columnName) != null) {
					columnName = arrayHeader.get(columnName);
				}

				Cell<PDPage> headerCell = headerRow.createCell(columnPercentWidths[i], columnName, HorizontalAlignment.get("center"),
						VerticalAlignment.get("top"));
				if (style != null && style.has("th") && style.getJSONObject("th").optBoolean("enabled")) {
					JSONObject headerStyle = style.getJSONObject("th");
					headerCell.setFont(PDType1Font.HELVETICA_BOLD);
					if (headerStyle.has("font-size")) {
						float size = getFontSizeFromString(headerStyle.getString("font-size"));
						if (size != 0)
							headerCell.setFontSize(size);
					}
					headerCell.setFillColor(getColorFromString(headerStyle.optString("background-color"), Color.WHITE));
					headerCell.setTextColor(getColorFromString(headerStyle.optString("color"), Color.BLACK));
				}
			}

			table.addHeaderRow(headerRow);

			DateFormat inputDateFormat = new SimpleDateFormat(DATE_FORMAT, getLocale());

			for (int r = 0; r < rows.length(); r++) {
				JSONObject rowObject = rows.getJSONObject(r);
				Row<PDPage> row = table.createRow(10);

				for (int c = 0; c < columnsOrdered.length(); c++) {

					if (pdfHiddenColumns.contains(c))
						continue;

					JSONObject column = columnsOrdered.getJSONObject(c);
					String type = column.getString("type");
					String colIndex = column.getString("name"); // column_1, column_2, column_3...
					Object value = rowObject.get(colIndex);
					if (value != null) {
						String valueStr = value.toString();
						if (type.equalsIgnoreCase("float") && columnStyles[c] != null && columnStyles[c].has("precision")) {
							int precision = columnStyles[c].optInt("precision");
							int pos = valueStr.indexOf(".");
							// offset = 0 se devo tagliare fuori anche la virgola ( in caso precision fosse 0 )
							int offset = (precision == 0 ? 0 : 1);
							if (pos != -1 && valueStr.length() >= pos + precision + offset) {
								try {
									valueStr = valueStr.substring(0, pos + precision + offset);
								} catch (Exception e) {
									// value stays as it is
									logger.error("Cannot format value according to precision", e);
								}
							} else {
								logger.warn("Cannot format raw value {" + valueStr + "} with precision {" + precision + "}");
							}
						}
						if (type.equalsIgnoreCase("date")) {
							try {
								DateFormat outputDateFormat = new SimpleDateFormat(columnDateFormats[c], getLocale());
								Date date = inputDateFormat.parse(valueStr);
								valueStr = outputDateFormat.format(date);
							} catch (Exception e) {
								// value stays as it is
								logger.warn("Cannot format date {" + valueStr + "} according to format {" + columnDateFormats[c] + "}", e);
							}
						}
						Cell<PDPage> cell = row.createCell(columnPercentWidths[c], valueStr, HorizontalAlignment.get("center"), VerticalAlignment.get("top"));
						// first of all set alternate rows color
						if (settings != null && settings.has("alternateRows")) {
							JSONObject alternateRows = settings.getJSONObject("alternateRows");
							if (alternateRows.optBoolean("enabled")) {
								cell.setFont(PDType1Font.HELVETICA);
								if (r % 2 == 0) {
									cell.setFillColor(getColorFromString(alternateRows.optString("evenRowsColor"), Color.WHITE));
								} else {
									cell.setFillColor(getColorFromString(alternateRows.optString("oddRowsColor"), Color.WHITE));
								}
							}
						}
						// then override it with custom column color (if set)
						Color textColor = getColumnTextColor(columnStyles, c);
						Color backgroundColor = getColumnBackgroundColor(columnStyles, c);
						if (textColor != null)
							cell.setTextColor(textColor);
						if (backgroundColor != null)
							cell.setFillColor(backgroundColor);
					}
				}
			}

			table.draw();

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot write data to PDF file", e);
		}
	}

	private String[] getColumnDateFormats(JSONArray columnsOrdered, JSONObject widgetContent) {
		try {
			String[] toReturn = new String[columnsOrdered.length() + 10];
			JSONArray columns = widgetContent.getJSONArray("columnSelectedOfDataset");
			for (int i = 0; i < columnsOrdered.length(); i++) {
				JSONObject orderedCol = columnsOrdered.getJSONObject(i);
				for (int j = 0; j < columns.length(); j++) {
					JSONObject col = columns.getJSONObject(j);
					if (orderedCol.getString("header").equals(col.getString("aliasToShow"))) {
						if (col.has("dateFormat")) {
							String angularDateFormat = col.getString("dateFormat");
							if (getLocale().equals(Locale.ITALY)) {
								switch (angularDateFormat) {
								case "LLLL":
									toReturn[i] = "EEEE d MMMM yyyy h:mm:ss a";
									break;
								case "llll":
									toReturn[i] = "EEEE d MMMM yyyy h:mm:ss a";
									break;
								case "LLL":
									toReturn[i] = "d MMMM yyyy h:mm:ss a";
									break;
								case "lll":
									toReturn[i] = "d MMMM yyyy h:mm:ss a";
									break;
								case "DD/MM/YYYY HH:mm:SS":
									toReturn[i] = "dd/MM/YYYY HH:mm:SS";
									break;
								case "DD/MM/YYYY HH:mm":
									toReturn[i] = "dd/MM/YYYY HH:mm";
									break;
								case "LL":
									toReturn[i] = "dd MMMM yyyy";
									break;
								case "ll":
									toReturn[i] = "dd MMMM yyyy";
									break;
								case "L":
									toReturn[i] = "dd/MM/YYYY";
									break;
								case "l":
									toReturn[i] = "d/MM/YYYY";
									break;
								case "LT":
									toReturn[i] = "HH:mm a";
									break;
								case "LTS":
									toReturn[i] = "HH:mm:SS a";
									break;
								default:
									break;
								}
							} else {
								switch (angularDateFormat) {
								case "LLLL":
									toReturn[i] = "EEEE, MMMM d, yyyy h:mm:ss a";
									break;
								case "llll":
									toReturn[i] = "EEEE, MMMM d, yyyy h:mm:ss a";
									break;
								case "LLL":
									toReturn[i] = "MMMM d, yyyy h:mm:ss a";
									break;
								case "lll":
									toReturn[i] = "MMMM d, yyyy h:mm:ss a";
									break;
								case "DD/MM/YYYY HH:mm:SS":
									toReturn[i] = "dd/MM/YYYY HH:mm:SS";
									break;
								case "DD/MM/YYYY HH:mm":
									toReturn[i] = "dd/MM/YYYY HH:mm";
									break;
								case "LL":
									toReturn[i] = "MMMM d, yyyy";
									break;
								case "ll":
									toReturn[i] = "MMMM d, yyyy";
									break;
								case "L":
									toReturn[i] = "MM/dd/YYYY";
									break;
								case "l":
									toReturn[i] = "MM/d/YYYY";
									break;
								case "LT":
									toReturn[i] = "HH:mm a";
									break;
								case "LTS":
									toReturn[i] = "HH:mm:SS a";
									break;
								default:
									break;
								}
							}
						}
					}
				}
			}
			return toReturn;
		} catch (Exception e) {
			logger.error("Error while retrieving table columns date formats.", e);
			return new String[columnsOrdered.length() + 10];
		}
	}

	private List<Integer> getPdfHiddenColumnsList(JSONArray columnsOrdered, JSONArray columns) {
		List<Integer> pdfHiddenColumns = new ArrayList<Integer>();
		try {
			for (int i = 0; i < columnsOrdered.length(); i++) {
				JSONObject orderedCol = columnsOrdered.getJSONObject(i);
				for (int j = 0; j < columns.length(); j++) {
					JSONObject col = columns.getJSONObject(j);
					if (orderedCol.getString("header").equals(col.getString("alias"))) {
						if (col.has("style")) {
							JSONObject style = col.optJSONObject("style");
							if (style.has("hideFromPdf")) {
								if (style.getString("hideFromPdf").equals("true")) {
									pdfHiddenColumns.add(i);
								}
							}
						}
					}
				}
			}
			return pdfHiddenColumns;
		} catch (Exception e) {
			logger.error("Error while getting PDF hidden columns list");
			return new ArrayList<Integer>();
		}
	}

	private void initColumnWidths(JSONObject[] columnStyles, int numOfColumns) {
		columnPercentWidths = new float[numOfColumns + 10];
		for (int i = 0; i < numOfColumns; i++) {
			if (columnStyles[i] != null && columnStyles[i].optInt("width") != 0) {
				if (!pdfHiddenColumns.contains(i)) {
					totalColumnsWidth += columnStyles[i].optInt("width");
				}
				columnPercentWidths[i] = columnStyles[i].optInt("width");
			} else {
				if (!pdfHiddenColumns.contains(i)) {
					totalColumnsWidth += DEFAULT_COLUMN_WIDTH;
				}
				columnPercentWidths[i] = DEFAULT_COLUMN_WIDTH;
			}
		}
		for (int i = 0; i < numOfColumns; i++) {
			columnPercentWidths[i] = (columnPercentWidths[i] / totalColumnsWidth) * 100;
		}
	}

	private Color getColumnBackgroundColor(JSONObject[] columnStyles, int c) {
		try {
			String rgbColor = columnStyles[c].optString("background-color");
			Color color = getColorFromString(rgbColor, null);
			return color;
		} catch (Exception e) {
			return null;
		}
	}

	private Color getColumnTextColor(JSONObject[] columnStyles, int c) {
		try {
			String rgbColor = columnStyles[c].optString("color");
			Color color = getColorFromString(rgbColor, null);
			return color;
		} catch (Exception e) {
			return null;
		}
	}

	private float getFontSizeFromString(String fontSize) {
		try {
			String sizeStr = fontSize.split("px")[0];
			return Integer.parseInt(sizeStr);
		} catch (Exception e) {
			logger.error("Cannot get size from string {" + fontSize + "}. Default size will be used.", e);
			return 0;
		}
	}

	private Color getColorFromString(String rgbColor, Color defaultColor) {
		try {
			if (rgbColor == null || rgbColor.isEmpty())
				return defaultColor;
			String[] colors = rgbColor.substring(4, rgbColor.length() - 1).split(",");
			int r = Integer.parseInt(colors[0].trim());
			int g = Integer.parseInt(colors[1].trim());
			int b = Integer.parseInt(colors[2].trim());
			return new Color(r, g, b);
		} catch (Exception e) {
			logger.error("Cannot create color from string {" + rgbColor + "}. Default color {" + defaultColor + "} will be used", e);
			return defaultColor;
		}
	}

	private PDPage createPage(JSONObject settings, JSONObject widgetConf) {
		try {
			JSONObject exportPdf = settings.optJSONObject("exportpdf");
			if (exportPdf == null) {
				return new PDPage(calculateTableDimensions(widgetConf));
			} else if (exportPdf.optBoolean("a4portrait")) {
				return new PDPage(PDRectangle.A4);
			} else if (exportPdf.optBoolean("a4landscape")) {
				return new PDPage(new PDRectangle(297 * POINTS_PER_MM, 210 * POINTS_PER_MM));
			} else if (exportPdf.has("custom") && exportPdf.getJSONObject("custom").optBoolean("enabled")) {
				int width = exportPdf.getJSONObject("custom").getInt("width");
				int height = exportPdf.getJSONObject("custom").getInt("height");
				return new PDPage(new PDRectangle(width * POINTS_PER_MM, height * POINTS_PER_MM));
			} else {
				return new PDPage(calculateTableDimensions(widgetConf));
			}
		} catch (Exception e) {
			logger.error("Cannot instantiate custom page. Default A4 format will be used.", e);
			return new PDPage(PDRectangle.A4);
		}
	}

	private PDRectangle calculateTableDimensions(JSONObject widgetConf) {
		try {
			int totalWidth = 0;
			JSONArray columns = widgetConf.getJSONObject("content").getJSONArray("columnSelectedOfDataset");
			for (int i = 0; i < columns.length(); i++) {
				int width;
				try {
					width = columns.getJSONObject(i).getJSONObject("style").getInt("width");
				} catch (Exception e) {
					width = DEFAULT_COLUMN_WIDTH;
				}
				totalWidth += width;
			}
			return new PDRectangle(totalWidth, 210 * POINTS_PER_MM);
		} catch (Exception e) {
			logger.error("Error while calculating dimensions. Default A4 format will be used.", e);
			return PDRectangle.A4;
		}
	}

	private BaseTable createBaseTable(PDDocument doc, PDPage page) {
		try {
			float margin = 20;
			// starting y position is whole page height subtracted by top and bottom margin
			float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
			float bottomMargin = 20;
			float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
//			float tableWidth = totalColumnsWidth;
			// y position is your coordinate of top left corner of the table
			Assert.assertTrue(tableWidth > 0, "Page dimension is too small!");
			float yPosition = 550;
			return new BaseTable(yPosition, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot create PDF Base Table object:", e);
		}
	}

	@Override
	protected JSONObject getCockpitSelectionsFromBody(JSONObject widget) {
		JSONObject cockpitSelections = new JSONObject();
		if (body == null || body.length() == 0)
			return cockpitSelections;
		try {

			cockpitSelections = body.getJSONObject("COCKPIT_SELECTIONS");
			forceUniqueHeaders(cockpitSelections);
		} catch (Exception e) {
			logger.error("Cannot get cockpit selections", e);
			return new JSONObject();
		}
		return cockpitSelections;
	}

}
