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
package it.eng.spagobi.engines.qbe.exporter;

import static org.apache.poi.ss.usermodel.BorderStyle.THIN;
import static org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT;
import static org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT;
import static org.apache.poi.ss.usermodel.VerticalAlignment.CENTER;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import it.eng.spagobi.engines.qbe.bo.MeasureScaleFactorOption;
import it.eng.spagobi.engines.qbe.query.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class QbeXLSXExporter {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(QbeXLSXExporter.class);

	/** Configuration properties */
	public static final String PROPERTY_HEADER_FONT_SIZE = "HEADER_FONT_SIZE";
	public static final String PROPERTY_HEADER_COLOR = "HEADER_COLOR";
	public static final String PROPERTY_HEADER_BACKGROUND_COLOR = "HEADER_BACKGROUND_COLOR";
	public static final String PROPERTY_HEADER_BORDER_COLOR = "HEADER_BORDER_COLOR";
	public static final String PROPERTY_CELL_FONT_SIZE = "CELL_FONT_SIZE";
	public static final String PROPERTY_CELL_COLOR = "CELL_COLOR";
	public static final String PROPERTY_CELL_BACKGROUND_COLOR = "CELL_BACKGROUND_COLOR";
	public static final String PROPERTY_CELL_BORDER_COLOR = "CELL_BORDER_COLOR";
	public static final String PROPERTY_FONT_NAME = "FONT_NAME";

	public static final short DEFAULT_HEADER_FONT_SIZE = 8;
	public static final String DEFAULT_HEADER_COLOR = "BLACK";
	public static final String DEFAULT_HEADER_BACKGROUND_COLOR = "GREY_25_PERCENT";
	public static final String DEFAULT_HEADER_BORDER_COLOR = "WHITE";
	public static final short DEFAULT_CELL_FONT_SIZE = 8;
	public static final String DEFAULT_CELL_COLOR = "BLACK";
	public static final String DEFAULT_CELL_BACKGROUND_COLOR = "WHITE";
	public static final String DEFAULT_CELL_BORDER_COLOR = "BLACK";
	public static final String DEFAULT_DIMENSION_NAME_COLOR = "BLACK";
	public static final String DEFAULT_DIMENSION_NAME_BACKGROUND_COLOR = "LIGHT_BLUE";
	public static final String DEFAULT_FONT_NAME = "Verdana";

	public static final int DEFAULT_DECIMAL_PRECISION = 2;

	public static final int DEFAULT_START_COLUMN = 0;

	public static final String ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR = "measureScaleFactor";

	private Locale locale;
	private Map<String, Object> properties;

	private DataIterator iterator = null;
	private List<?> extractedFields = null;
	private Map<Integer, CellStyle> decimalFormats = new HashMap<Integer, CellStyle>();
	private int maxNumOfRows = SpreadsheetVersion.EXCEL2007.getLastRowIndex();

	public QbeXLSXExporter(DataIterator iterator, Locale locale, int recordsLimit) {
		super();
		this.iterator = iterator;
		this.locale = locale;
		this.properties = new HashMap<String, Object>();
		this.maxNumOfRows = Math.min(SpreadsheetVersion.EXCEL2007.getLastRowIndex(), recordsLimit);
	}

	public Workbook export() {
		Workbook workbook = this.instantiateWorkbook();
		CreationHelper createHelper = workbook.getCreationHelper();
		Sheet sheet = workbook.createSheet("new sheet");
		for (int j = 0; j < 50; j++) {
			sheet.createRow(j);
		}
		fillSheet(sheet, workbook, createHelper, 0);
		return workbook;
	}

	private Workbook instantiateWorkbook() {
		return new SXSSFWorkbook();
	}

	private void fillSheet(Sheet sheet, Workbook wb, CreationHelper createHelper, int startRow) {
		if (iterator.hasNext()) {
			boolean overflow = false;

			CellStyle[] cellTypes = fillSheetHeader(sheet, wb, createHelper, startRow, DEFAULT_START_COLUMN);
			overflow = fillSheetData(sheet, wb, createHelper, cellTypes, startRow + 1, DEFAULT_START_COLUMN);

			if (overflow) {
				fillMessageHeader(sheet);
			}
		}
	}

	/**
	 *
	 * @param sheet
	 *            ...
	 * @param workbook
	 *            ...
	 * @param createHelper
	 *            ...
	 * @param beginRowHeaderData
	 *            header's vertical offset. Expressed in number of rows
	 * @param beginColumnHeaderData
	 *            header's horizontal offset. Expressed in number of columns
	 *
	 * @return ...
	 */
	private CellStyle[] fillSheetHeader(Sheet sheet, Workbook workbook, CreationHelper createHelper, int beginRowHeaderData, int beginColumnHeaderData) {

		CellStyle[] cellTypes;

		logger.trace("IN");

		try {

			IMetaData dataStoreMetaData = getMetadata();
			int colnumCount = getFieldCount();

			Row headerRow = sheet.getRow(beginRowHeaderData);
			CellStyle headerCellStyle = buildHeaderCellStyle(sheet);

			cellTypes = new CellStyle[colnumCount];
			for (int j = 0; j < colnumCount; j++) {
				Cell cell = headerRow.createCell(j + beginColumnHeaderData);
				cell.setCellType(getCellTypeString());
				String fieldName = dataStoreMetaData.getFieldAlias(j);
				IFieldMetaData fieldMetaData = dataStoreMetaData.getFieldMeta(j);
				String format = (String) fieldMetaData.getProperty("format");
				String alias = fieldMetaData.getAlias();
				String scaleFactorHeader = (String) fieldMetaData.getProperty(ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR);

				String header;
				if (extractedFields != null && j < extractedFields.size() && extractedFields.get(j) != null) {
					Field field = (Field) extractedFields.get(j);
					fieldName = field.getAlias();
					if (field.getPattern() != null) {
						format = field.getPattern();
					}
				}
				CellStyle aCellStyle = this.buildCellStyle(sheet);
				if (format != null) {
					short formatInt = this.getBuiltinFormat(format);
					aCellStyle.setDataFormat(formatInt);
					cellTypes[j] = aCellStyle;
				}

				if (alias != null && !alias.equals("")) {
					header = alias;
				} else {
					header = fieldName;
				}

				header = MeasureScaleFactorOption.getScaledName(header, scaleFactorHeader, locale);
				cell.setCellValue(createHelper.createRichTextString(header));

				cell.setCellStyle(headerCellStyle);

			}

		} catch (Exception t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while filling sheet header", t);
		} finally {
			logger.trace("OUT");
		}

		return cellTypes;
	}

	/**
	 * @return <code>false</code> if every record is written to the outp, <code>true</code> otherwise
	 */
	private boolean fillSheetData(Sheet sheet, Workbook wb, CreationHelper createHelper, CellStyle[] cellTypes, int beginRowData, int beginColumnData) {
		boolean overflow = false;

		CellStyle dCellStyle = this.buildCellStyle(sheet);
		short formatIndexInt = this.getBuiltinFormat("#,##0");
		CellStyle cellStyleInt = this.buildCellStyle(sheet); // cellStyleInt is the default cell style for integers
		cellStyleInt.cloneStyleFrom(dCellStyle);
		cellStyleInt.setDataFormat(formatIndexInt);

		CellStyle cellStyleDate = this.buildCellStyle(sheet); // cellStyleDate is the default cell style for dates
		cellStyleDate.cloneStyleFrom(dCellStyle);
		cellStyleDate.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy"));

		IMetaData d = getMetadata();

		int rownum = beginRowData;
		while (iterator.hasNext()) {

			if (rownum >= maxNumOfRows) {
				overflow = true;
				break;
			}

			Row rowVal = sheet.getRow(rownum);
			IRecord record = iterator.next();
			List fields = record.getFields();
			int length = fields.size();
			for (int fieldIndex = 0; fieldIndex < length; fieldIndex++) {
				IField f = (IField) fields.get(fieldIndex);
				if (f != null && f.getValue() != null) {

					Class c = d.getFieldType(fieldIndex);
					logger.debug("Column [" + (fieldIndex) + "] class is equal to [" + c.getName() + "]");
					if (rowVal == null) {
						rowVal = sheet.createRow(rownum);
					}
					Cell cell = rowVal.createCell(fieldIndex + beginColumnData);
					cell.setCellStyle(dCellStyle);
					if (Integer.class.isAssignableFrom(c) || Short.class.isAssignableFrom(c)) {
						logger.debug("Column [" + (fieldIndex + 1) + "] type is equal to [" + "INTEGER" + "]");
						IFieldMetaData fieldMetaData = d.getFieldMeta(fieldIndex);
						String scaleFactor = (String) fieldMetaData.getProperty(ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR);
						Number val = (Number) f.getValue();
						Double doubleValue = MeasureScaleFactorOption.applyScaleFactor(val.doubleValue(), scaleFactor);
						cell.setCellValue(doubleValue);
						cell.setCellType(this.getCellTypeNumeric());
						cell.setCellStyle((cellTypes[fieldIndex] != null) ? cellTypes[fieldIndex] : cellStyleInt);
					} else if (Number.class.isAssignableFrom(c)) {
						logger.debug("Column [" + (fieldIndex + 1) + "] type is equal to [" + "NUMBER" + "]");
						IFieldMetaData fieldMetaData = d.getFieldMeta(fieldIndex);
						String decimalPrecision = (String) fieldMetaData.getProperty(IFieldMetaData.DECIMALPRECISION);
						CellStyle cs;
						if (decimalPrecision != null) {
							cs = getDecimalNumberFormat(new Integer(decimalPrecision), sheet, createHelper, dCellStyle);
						} else {
							cs = getDecimalNumberFormat(DEFAULT_DECIMAL_PRECISION, sheet, createHelper, dCellStyle);
						}
						Number val = (Number) f.getValue();
						Double value = val.doubleValue();
						String scaleFactor = (String) fieldMetaData.getProperty(ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR);
						cell.setCellValue(MeasureScaleFactorOption.applyScaleFactor(value, scaleFactor));
						cell.setCellType(this.getCellTypeNumeric());
						cell.setCellStyle((cellTypes[fieldIndex] != null) ? cellTypes[fieldIndex] : cs);
					} else if (String.class.isAssignableFrom(c)) {
						logger.debug("Column [" + (fieldIndex + 1) + "] type is equal to [" + "STRING" + "]");
						String val = (String) f.getValue();
						cell.setCellValue(createHelper.createRichTextString(val));
						cell.setCellType(this.getCellTypeString());
					} else if (Boolean.class.isAssignableFrom(c)) {
						logger.debug("Column [" + (fieldIndex + 1) + "] type is equal to [" + "BOOLEAN" + "]");
						Boolean val = (Boolean) f.getValue();
						cell.setCellValue(val.booleanValue());
						cell.setCellType(this.getCellTypeBoolean());
					} else if (Date.class.isAssignableFrom(c)) {
						logger.debug("Column [" + (fieldIndex + 1) + "] type is equal to [" + "DATE" + "]");
						Date val = (Date) f.getValue();
						cell.setCellValue(val);
						cell.setCellStyle(cellStyleDate);
					} else {
						logger.warn("Column [" + (fieldIndex + 1) + "] type is equal to [" + "???" + "]");
						String val = f.getValue().toString();
						cell.setCellValue(createHelper.createRichTextString(val));
						cell.setCellType(this.getCellTypeString());
					}
				}
			}
			rownum++;
		}

		return overflow;
	}

	private void fillMessageHeader(Sheet sheet) {
		String message = "Query results are exceeding configured threshold, therefore only " + maxNumOfRows + " were exported.";

		Drawing<?> drawing = sheet.createDrawingPatriarch();

		// Magic numbers just to show a user friendly comment in a suitable position
		int dx1 = Units.pixelToEMU(25);
		int dy1 = Units.pixelToEMU(25);
		int dx2 = Units.pixelToEMU(800);
		int dy2 = Units.pixelToEMU(1200);

		// Magic numbers just to show a user friendly comment of suitable size
		ClientAnchor anchor = drawing.createAnchor(dx1, dy1, dx2, dy2, 0, 1, getFieldCount(), 5);
		Comment comment = drawing.createCellComment(anchor);

		comment.setAuthor("Knowage");
		comment.setString(new XSSFRichTextString(message));
		comment.setVisible(true);

	}

	private short getBuiltinFormat(String formatStr) {
		short format = (short) BuiltinFormats.getBuiltinFormat(formatStr);
		return format;
	}

	private Object getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}

	private CellStyle buildHeaderCellStyle(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setAlignment(LEFT);
		cellStyle.setVerticalAlignment(CENTER);

		String headerBGColor = (String) this.getProperty(PROPERTY_HEADER_BACKGROUND_COLOR);
		logger.debug("Header background color : " + headerBGColor);
		short backgroundColorIndex = headerBGColor != null ? IndexedColors.valueOf(headerBGColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_HEADER_BACKGROUND_COLOR).getIndex();
		cellStyle.setFillForegroundColor(backgroundColorIndex);

		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		cellStyle.setBorderBottom(THIN);
		cellStyle.setBorderLeft(THIN);
		cellStyle.setBorderRight(THIN);
		cellStyle.setBorderTop(THIN);

		String bordeBorderColor = (String) this.getProperty(PROPERTY_HEADER_BORDER_COLOR);
		logger.debug("Header border color : " + bordeBorderColor);
		short borderColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(bordeBorderColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_HEADER_BORDER_COLOR).getIndex();

		cellStyle.setLeftBorderColor(borderColorIndex);
		cellStyle.setRightBorderColor(borderColorIndex);
		cellStyle.setBottomBorderColor(borderColorIndex);
		cellStyle.setTopBorderColor(borderColorIndex);

		Font font = sheet.getWorkbook().createFont();

		Short headerFontSize = (Short) this.getProperty(PROPERTY_HEADER_FONT_SIZE);
		logger.debug("Header font size : " + headerFontSize);
		short headerFontSizeShort = headerFontSize != null ? headerFontSize.shortValue() : DEFAULT_HEADER_FONT_SIZE;
		font.setFontHeightInPoints(headerFontSizeShort);

		String fontName = (String) this.getProperty(PROPERTY_FONT_NAME);
		logger.debug("Font name : " + fontName);
		fontName = fontName != null ? fontName : DEFAULT_FONT_NAME;
		font.setFontName(fontName);

		String headerColor = (String) this.getProperty(PROPERTY_HEADER_COLOR);
		logger.debug("Header color : " + headerColor);
		short headerColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(headerColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_HEADER_COLOR).getIndex();
		font.setColor(headerColorIndex);

		font.setBold(true);
		cellStyle.setFont(font);
		return cellStyle;
	}

	private CellStyle buildCellStyle(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setAlignment(RIGHT);
		cellStyle.setVerticalAlignment(CENTER);

		String cellBGColor = (String) this.getProperty(PROPERTY_CELL_BACKGROUND_COLOR);
		logger.debug("Cell background color : " + cellBGColor);
		short backgroundColorIndex = cellBGColor != null ? IndexedColors.valueOf(cellBGColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_CELL_BACKGROUND_COLOR).getIndex();
		cellStyle.setFillForegroundColor(backgroundColorIndex);

		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		cellStyle.setBorderBottom(THIN);
		cellStyle.setBorderLeft(THIN);
		cellStyle.setBorderRight(THIN);
		cellStyle.setBorderTop(THIN);

		String bordeBorderColor = (String) this.getProperty(PROPERTY_CELL_BORDER_COLOR);
		logger.debug("Cell border color : " + bordeBorderColor);
		short borderColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(bordeBorderColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_CELL_BORDER_COLOR).getIndex();

		cellStyle.setLeftBorderColor(borderColorIndex);
		cellStyle.setRightBorderColor(borderColorIndex);
		cellStyle.setBottomBorderColor(borderColorIndex);
		cellStyle.setTopBorderColor(borderColorIndex);

		Font font = sheet.getWorkbook().createFont();

		Short cellFontSize = (Short) this.getProperty(PROPERTY_CELL_FONT_SIZE);
		logger.debug("Cell font size : " + cellFontSize);
		short cellFontSizeShort = cellFontSize != null ? cellFontSize.shortValue() : DEFAULT_CELL_FONT_SIZE;
		font.setFontHeightInPoints(cellFontSizeShort);

		String fontName = (String) this.getProperty(PROPERTY_FONT_NAME);
		logger.debug("Font name : " + fontName);
		fontName = fontName != null ? fontName : DEFAULT_FONT_NAME;
		font.setFontName(fontName);

		String cellColor = (String) this.getProperty(PROPERTY_CELL_COLOR);
		logger.debug("Cell color : " + cellColor);
		short cellColorIndex = cellColor != null ? IndexedColors.valueOf(cellColor).getIndex() : IndexedColors.valueOf(DEFAULT_CELL_COLOR).getIndex();
		font.setColor(cellColorIndex);

		cellStyle.setFont(font);
		return cellStyle;
	}

	public void setExtractedFields(List<?> extractedFields) {
		this.extractedFields = extractedFields;
	}

	private CellStyle getDecimalNumberFormat(int j, Sheet sheet, CreationHelper createHelper, CellStyle dCellStyle) {

		if (decimalFormats.get(j) != null)
			return decimalFormats.get(j);
		String decimals = "";
		for (int i = 0; i < j; i++) {
			decimals += "0";
		}

		CellStyle cellStyleDoub = this.buildCellStyle(sheet); // cellStyleDoub is the default cell style for doubles
		cellStyleDoub.cloneStyleFrom(dCellStyle);
		DataFormat df = createHelper.createDataFormat();
		String format = "#,##0";
		if (decimals.length() > 0) {
			format += "." + decimals;
		}
		cellStyleDoub.setDataFormat(df.getFormat(format));

		decimalFormats.put(j, cellStyleDoub);
		return cellStyleDoub;
	}

	private CellType getCellTypeNumeric() {
		return CellType.NUMERIC;
	}

	private CellType getCellTypeString() {
		return CellType.STRING;
	}

	private CellType getCellTypeBoolean() {
		return CellType.BOOLEAN;
	}

	private IMetaData getMetadata() {
		return iterator.getMetaData();
	}

	private int getFieldCount() {
		return getMetadata().getFieldCount();
	}


}
