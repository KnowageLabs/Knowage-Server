/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.engine.api.excel.export.oldcockpit.exporters;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import it.eng.knowage.engine.api.excel.export.IWidgetExporter;
import it.eng.knowage.engine.api.excel.export.oldcockpit.ExcelExporter;
import it.eng.knowage.engine.api.excel.export.oldcockpit.Threshold;
import it.eng.knowage.engine.api.excel.export.oldcockpit.crosstable.*;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.messages.EngineMessageBundle;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Exports the crosstab data (formatted as a JSON object in input) into a XLS file. The JSON object should have this structure (a node is {node_key:"Text",
 * node_childs:[...]}): columns: {...} contains tree node structure of the columns' headers rows: {...} contains tree node structure of the rows' headers data:
 * [[...], [...], ...] 2-dimensional matrix containing crosstab data
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it), Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class CrossTabExporter extends GenericWidgetExporter implements IWidgetExporter {

	/** Logger component. */
	private static final Logger LOGGER = Logger.getLogger(CrossTabExporter.class);

	/** Configuration properties */
	public static final String PROPERTY_HEADER_FONT_SIZE = "HEADER_FONT_SIZE";
	public static final String PROPERTY_HEADER_COLOR = "HEADER_COLOR";
	public static final String PROPERTY_HEADER_BACKGROUND_COLOR = "HEADER_BACKGROUND_COLOR";
	public static final String PROPERTY_HEADER_BORDER_COLOR = "HEADER_BORDER_COLOR";
	public static final String PROPERTY_CELL_FONT_SIZE = "CELL_FONT_SIZE";
	public static final String PROPERTY_CELL_COLOR = "CELL_COLOR";
	public static final String PROPERTY_CELL_BACKGROUND_COLOR = "CELL_BACKGROUND_COLOR";
	public static final String PROPERTY_CELL_BORDER_COLOR = "CELL_BORDER_COLOR";
	public static final String PROPERTY_DIMENSION_NAME_COLOR = "DIMENSION_NAME_COLOR";
	public static final String PROPERTY_DIMENSION_NAME_BACKGROUND_COLOR = "HEADER_DIMENSION_NAME_BACKGROUND_COLOR";
	public static final String PROPERTY_FONT_NAME = "FONT_NAME";

	public static final String PROPERTY_CALCULATED_FIELD_DECIMALS = "CALCULATED_FIELD_DECIMALS";

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
	public static final int DEFAULT_CALCULATED_FIELD_DECIMALS = 2;

	private static final String STRING_CELL_DEFAULT_FORMAT = "#,##0.00";

	private Properties properties;
	private Map<String, List<Threshold>> thresholdColorsMap;
	private JSONObject variables = new JSONObject();

	protected Map<Integer, XSSFCellStyle> formatHash2CellStyle = new HashMap<>();

	public CrossTabExporter(Properties properties, JSONObject variables) {
		if (properties == null) {
			this.properties = new Properties();
		} else {
			this.properties = properties;
		}
		this.variables = variables;
	}

	public CrossTabExporter(Properties properties, JSONObject variables,
			Map<String, List<Threshold>> thresholdColorsMap) {
		if (properties == null) {
			this.properties = new Properties();
		} else {
			this.properties = properties;
		}
		if (thresholdColorsMap == null) {
			this.thresholdColorsMap = new HashMap<>();
		} else {
			this.thresholdColorsMap = thresholdColorsMap;
		}
		this.variables = variables;
	}

	public CrossTabExporter(ExcelExporter excelExporter, String widgetType, String templateString, long widgetId,
			Workbook wb, JSONObject options) {
		super(excelExporter, widgetType, templateString, widgetId, wb, options);
	}

	public void setProperty(String propertyName, Object propertyValue) {
		this.properties.put(propertyName, propertyValue);
	}

	public Object getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}

	/*
	 * This method avoids cell style objects number to increase by rows number (see https://production.eng.it/jira/browse/KNOWAGE-6692 and
	 * https://production.eng.it/jira/browse/KNOWAGE-6693)
	 */
	protected final XSSFCellStyle getCellStyleByFormat(Workbook wb, CreationHelper helper, String format,
			Optional<FillPatternType> fillPatternTypeOpt, Optional<java.awt.Color> colorOpt) {
		Integer styleKey = getStyleKey(format, fillPatternTypeOpt, colorOpt);
		formatHash2CellStyle.computeIfAbsent(styleKey,
				key -> doCreateCellStyle(wb, helper, format, fillPatternTypeOpt, colorOpt));
		return formatHash2CellStyle.get(styleKey);
	}

	private final XSSFCellStyle doCreateCellStyle(Workbook wb, CreationHelper helper, String format,
			Optional<FillPatternType> fillPatternTypeOpt, Optional<java.awt.Color> colorOpt) {

		LOGGER.debug("New style created for format " + format + ", fill pattern" + fillPatternTypeOpt + " and color"
				+ colorOpt);

		XSSFCellStyle cellStyle = (XSSFCellStyle) wb.createCellStyle();
		cellStyle.setDataFormat(helper.createDataFormat().getFormat(format));
		fillPatternTypeOpt.ifPresent(cellStyle::setFillPattern);
		colorOpt.ifPresent(
				color -> cellStyle.setFillForegroundColor(new XSSFColor(color, new DefaultIndexedColorMap())));

		LOGGER.debug("New style is " + cellStyle);

		return cellStyle;
	}

	private final Integer getStyleKey(String format, Optional<FillPatternType> fillPatternTypeOpt,
			Optional<java.awt.Color> colorOpt) {
		Integer hashcode = format.hashCode();
		if (fillPatternTypeOpt.isPresent()) {
			FillPatternType fillPatternType = fillPatternTypeOpt.get();
			hashcode += fillPatternType.hashCode();
		}
		if (colorOpt.isPresent()) {
			java.awt.Color color = colorOpt.get();
			hashcode += color.hashCode();
		}

		LOGGER.debug(
				"Getting style for " + format + ", " + fillPatternTypeOpt + " and " + colorOpt + " return " + hashcode);

		return hashcode;
	}

	private int fillExcelSheetWithData(Sheet sheet, CrossTab cs, CreationHelper createHelper, int startRow,
									   Locale locale) throws JSONException {
		int columnsDepth = cs.getColumnsRoot().getSubTreeDepth();
		int rowsDepth = cs.getRowsRoot().getSubTreeDepth();

		// + 1 because there may be also the bottom row with the totals
		int totalRowsNumber = cs.getTotalNumberOfRows();

		for (int i = 0; i < totalRowsNumber; i++) {
			sheet.createRow(startRow + i);
		}

		CellStyle memberCellStyle = this.buildHeaderCellStyle(sheet);
		CellStyle dimensionCellStyle = this.buildDimensionCellStyle(sheet);
		CellStyle dataCellStyle = this.buildDataCellStyle(sheet);

		// build headers for column first ...
		Monitor buildColumnsHeaderMonitor = MonitorFactory
				.start("CockpitEngine.export.excel.CrossTabExporter.buildColumnsHeaderMonitor");
		buildColumnsHeader(sheet, cs, cs.getColumnsRoot().getChildren(), startRow, rowsDepth - 1, createHelper, locale,
				memberCellStyle, dimensionCellStyle, 0);
		buildColumnsHeaderMonitor.stop();

		// ... then build headers for rows ....
		Monitor buildRowsHeaderMonitor = MonitorFactory
				.start("CockpitEngine.export.excel.CrossTabExporter.buildRowsHeaderMonitor");
		buildRowsHeaders(sheet, cs, cs.getRowsRoot().getChildren(), columnsDepth - 1 + startRow, 0, createHelper,
				locale, memberCellStyle);
		buildRowsHeaderMonitor.stop();

		// then put the matrix data
		Monitor buildDataMatrixMonitor = MonitorFactory
				.start("CockpitEngine.export.excel.CrossTabExporter.buildDataMatrixMonitor");
		buildDataMatrix(sheet, cs, columnsDepth + startRow - 1, rowsDepth - 1, createHelper, dataCellStyle);
		buildDataMatrixMonitor.stop();

		// finally add row titles
		Monitor buildRowHeaderTitleMonitor = MonitorFactory
				.start("CockpitEngine.export.excel.CrossTabExporter.buildRowHeaderTitleMonitor");
		buildRowHeaderTitle(sheet, cs, columnsDepth - 2, 0, startRow, createHelper, locale, dimensionCellStyle);
		buildRowHeaderTitleMonitor.stop();

		return startRow + totalRowsNumber;
	}

	public int initSheet(Sheet sheet, CrossTab cs) {

		int columnsDepth = cs.getColumnsRoot().getSubTreeDepth();
		int rowsNumber = cs.getRowsRoot().getSubTreeDepth();
		// + 1 because there may be also the bottom row with the totals
		int totalRowsNumber = columnsDepth + rowsNumber + 1;
		for (int i = 0; i < totalRowsNumber + 4; i++) {
			sheet.createRow(i);
		}
		return totalRowsNumber + 4;
	}

	protected int buildDataMatrix(Sheet sheet, CrossTab cs, int rowOffset, int columnOffset,
			CreationHelper createHelper, CellStyle dataCellStyle) throws JSONException {
		MeasureFormatter measureFormatter = new MeasureFormatter(cs);
		String[][] dataMatrix = cs.getDataMatrix();
		CellStyle cellStyleForNA = buildNACellStyle(sheet, dataCellStyle);
		int rowNum = 0;
		int numOfMeasures = cs.getMeasures().size();
		List<Measure> allMeasures = cs.getCrosstabDefinition().getMeasures();
		List<Measure> subtotalMeasures = getSubtotalsMeasures(allMeasures);
		List<CrossTab.MeasureInfo> measuresInfo = cs.getMeasures();
		for (int i = 0; i < dataMatrix.length; i++) {
			rowNum = rowOffset + i;
			Row row = sheet.getRow(rowNum);
			if (row == null) {
				row = sheet.createRow(rowNum);
			}
			int totalsCounter = 0;
			int pos;

			for (int j = 0; j < dataMatrix[0].length; j++) {

				JSONObject measureConfig = new JSONObject();
				if (cs.isMeasureOnRow()) {
					pos = i % measuresInfo.size();
					measureConfig = allMeasures.get(pos).getConfig();
				} else {
					pos = cs.getOffsetInColumnSubtree(j) % measuresInfo.size();
					if (cs.isCellFromSubtotalsColumn(j)) {
						measureConfig = subtotalMeasures.get(pos).getConfig();
					} else if (cs.isCellFromTotalsColumn(j)) {
						measureConfig = subtotalMeasures.get(totalsCounter).getConfig();
						totalsCounter++;
					} else {
						measureConfig = allMeasures.get(pos).getConfig();
					}
				}
				String text = dataMatrix[i][j];
				int columnNum = columnOffset + j;
				Cell cell = row.createCell(columnNum);
				try {
					Monitor valueFormattedMonitor = MonitorFactory
							.start("CockpitEngine.export.excel.CrossTabExporter.buildDataMatrix.valueFormattedMonitor");
					double value = Double.parseDouble(text);
					Double valueFormatted = measureFormatter.applyScaleFactor(value, i, j);
					valueFormattedMonitor.stop();
					Monitor cellStyleMonitor = MonitorFactory
							.start("CockpitEngine.export.excel.CrossTabExporter.buildDataMatrix.cellStyleMonitor");
					int measureIdx = j % numOfMeasures;
					String measureId = getMeasureId(cs, measureIdx);
					int decimals = measureFormatter.getFormatXLS(i, j);
					CrossTab.CellType cellType = cs.getCellType(i, j);
					CellStyle style = getStyle(decimals, sheet, createHelper, cellType, measureId, value,
							dataCellStyle);
					cellStyleMonitor.stop();
					Monitor buildCellMonitor = MonitorFactory
							.start("CockpitEngine.export.excel.CrossTabExporter.buildDataMatrix.buildCellMonitor");
					String cellTypeValue = cellType.getValue();
					boolean insertValue = true;
					if (measureConfig.has("excludeFromTotalAndSubtotal")
							&& measureConfig.getBoolean("excludeFromTotalAndSubtotal")
							&& (cellTypeValue.equalsIgnoreCase("partialsum")
									|| cellTypeValue.equalsIgnoreCase("totals"))) {
						insertValue = false;
					}
					if (insertValue) {
						cell.setCellValue(valueFormatted);
						cell.setCellType(this.getCellTypeNumeric());
						cell.setCellStyle(style);
					}
					buildCellMonitor.stop();
				} catch (NumberFormatException e) {
					LOGGER.debug("Text " + text + " is not recognized as a number");
					cell.setCellValue(createHelper.createRichTextString(text));
					cell.setCellType(this.getCellTypeString());
					cell.setCellStyle(cellStyleForNA);
				}
			}
		}
		return rowNum;
	}

	List<Measure> getSubtotalsMeasures(List<Measure> allMeasures) throws JSONException {
		List<Measure> toReturn = new ArrayList<>();
		for (int k = 0; k < allMeasures.size(); k++) {
			if (!allMeasures.get(k).getConfig().has("excludeFromTotalAndSubtotal")
					|| !allMeasures.get(k).getConfig().getBoolean("excludeFromTotalAndSubtotal")) {
				toReturn.add(allMeasures.get(k));
			}
		}
		return toReturn;
	}

	private String getMeasureId(CrossTab cs, int index) {
		List<CrossTab.MeasureInfo> measures = cs.getMeasures();
		CrossTab.MeasureInfo measure = measures.get(index);
		String measureId = measure.getId();
		return measureId;
	}

	protected org.apache.poi.ss.usermodel.CellType getCellTypeNumeric() {
		return org.apache.poi.ss.usermodel.CellType.NUMERIC;
	}

	protected org.apache.poi.ss.usermodel.CellType getCellTypeString() {
		return org.apache.poi.ss.usermodel.CellType.STRING;
	}

	public CellStyle buildNACellStyle(Sheet sheet, CellStyle dataCellStyle) {
		CellStyle cellStyleForNA = dataCellStyle;
		cellStyleForNA.setAlignment(HorizontalAlignment.CENTER);
		return cellStyleForNA;
	}

	/**
	 * Builds the rows' headers recursively with this order: |-----|-----|-----| | | | 3 | | | |-----| | | 2 | 4 | | | |-----| | 1 | | 5 | | |-----|-----| | | | 7 |
	 * | | 6 |-----| | | | 8 | |-----|-----|-----| | | | 11 | | 9 | 10 |-----| | | | 12 | |-----|-----|-----|
	 *
	 * @param sheet        The sheet of the XLS file
	 * @param siblings     The siblings nodes of the headers structure
	 * @param rowNum       The row number where the first sibling must be inserted
	 * @param columnNum    The column number where the siblings must be inserted
	 * @param createHelper The file creation helper
	 * @throws JSONException
	 */
	protected void buildRowsHeaders(Sheet sheet, CrossTab cs, List<Node> siblings, int rowNum, int columnNum,
									CreationHelper createHelper, Locale locale, CellStyle memberCellStyle) throws JSONException {
		int rowsCounter = rowNum;

		for (int i = 0; i < siblings.size(); i++) {
			Node aNode = siblings.get(i);
			List<Node> childs = aNode.getChildren();
			Row row = sheet.getRow(rowsCounter);
			Cell cell = row.createCell(columnNum);
			String text = aNode.getDescription();

			if (cs.isMeasureOnRow() && (childs == null || childs.isEmpty())) {
				// apply the measure scale factor
				text = MeasureScaleFactorOption.getScaledName(text, cs.getMeasureScaleFactor(text), locale);
			}
			cell.setCellValue(createHelper.createRichTextString(text));
			cell.setCellType(this.getCellTypeString());

			cell.setCellStyle(memberCellStyle);

			int descendants = aNode.getLeafsNumber();
			if (descendants > 1) {
				sheet.addMergedRegion(new CellRangeAddress(rowsCounter, // first row (0-based)
						rowsCounter + descendants - 1, // last row (0-based)
						columnNum, // first column (0-based)
						columnNum // last column (0-based)
				));
			}

			if (childs != null && !childs.isEmpty()) {
				buildRowsHeaders(sheet, cs, childs, rowsCounter, columnNum + 1, createHelper, locale, memberCellStyle);
			}
			int increment = descendants > 1 ? descendants : 1;
			rowsCounter = rowsCounter + increment;
		}
	}

	protected void buildRowHeaderTitle(Sheet sheet, CrossTab cs, int columnHeadersNumber, int startColumn, int startRow,
			CreationHelper createHelper, Locale locale, CellStyle dimensionCellStyle) throws JSONException {
		List<String> titles = cs.getRowHeadersTitles();

		if (titles != null) {

			Row row = sheet.getRow(startRow + columnHeadersNumber);
			for (int i = 0; i < titles.size(); i++) {

				Cell cell = row.createCell(startColumn + i);
				CrosstabDefinition.Row aRowDef = cs.getCrosstabDefinition()
						.getRows().get(i);

				String text = titles.get(i);
				String variable = aRowDef.getVariable();
				if (variables.has(variable)) {
					text = variables.getString(variable);
				}

				cell.setCellValue(createHelper.createRichTextString(text));
				cell.setCellType(this.getCellTypeString());
				cell.setCellStyle(dimensionCellStyle);
			}
			if (cs.isMeasureOnRow()) {
				Cell cell = row.createCell(startColumn + titles.size());
				String text = "Measures";
				if (locale != null) {
					text = EngineMessageBundle.getMessage("worksheet.export.crosstab.header.measures", locale);
				}
				cell.setCellValue(createHelper.createRichTextString(text));
				cell.setCellType(this.getCellTypeString());
				cell.setCellStyle(dimensionCellStyle);
			}
		}
	}

	public CellStyle buildDimensionCellStyle(Sheet sheet) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		String headerBGColor = (String) this.getProperty(PROPERTY_DIMENSION_NAME_BACKGROUND_COLOR);
		LOGGER.debug("Header background color : " + headerBGColor);
		short backgroundColorIndex = headerBGColor != null ? IndexedColors.valueOf(headerBGColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_DIMENSION_NAME_BACKGROUND_COLOR).getIndex();
		cellStyle.setFillForegroundColor(backgroundColorIndex);

		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);

		String bordeBorderColor = (String) this.getProperty(PROPERTY_HEADER_BORDER_COLOR);
		LOGGER.debug("Header border color : " + bordeBorderColor);
		short borderColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(bordeBorderColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_HEADER_BORDER_COLOR).getIndex();

		cellStyle.setLeftBorderColor(borderColorIndex);
		cellStyle.setRightBorderColor(borderColorIndex);
		cellStyle.setBottomBorderColor(borderColorIndex);
		cellStyle.setTopBorderColor(borderColorIndex);

		Font font = sheet.getWorkbook().createFont();

		Short headerFontSize = (Short) this.getProperty(PROPERTY_HEADER_FONT_SIZE);
		LOGGER.debug("Header font size : " + headerFontSize);
		short headerFontSizeShort = headerFontSize != null ? headerFontSize.shortValue() : DEFAULT_HEADER_FONT_SIZE;
		font.setFontHeightInPoints(headerFontSizeShort);

		String fontName = (String) this.getProperty(PROPERTY_FONT_NAME);
		LOGGER.debug("Font name : " + fontName);
		fontName = fontName != null ? fontName : DEFAULT_FONT_NAME;
		font.setFontName(fontName);

		String color = (String) this.getProperty(PROPERTY_DIMENSION_NAME_COLOR);
		LOGGER.debug("Dimension color : " + color);
		short colorIndex = bordeBorderColor != null ? IndexedColors.valueOf(color).getIndex()
				: IndexedColors.valueOf(DEFAULT_DIMENSION_NAME_COLOR).getIndex();
		font.setColor(colorIndex);

		font.setBold(true);
		font.setItalic(true);
		cellStyle.setFont(font);
		return cellStyle;
	}

	public CellStyle buildHeaderCellStyle(Sheet sheet) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		String headerBGColor = (String) this.getProperty(PROPERTY_HEADER_BACKGROUND_COLOR);
		LOGGER.debug("Header background color : " + headerBGColor);
		short backgroundColorIndex = headerBGColor != null ? IndexedColors.valueOf(headerBGColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_HEADER_BACKGROUND_COLOR).getIndex();
		cellStyle.setFillForegroundColor(backgroundColorIndex);

		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);

		String bordeBorderColor = (String) this.getProperty(PROPERTY_HEADER_BORDER_COLOR);
		LOGGER.debug("Header border color : " + bordeBorderColor);
		short borderColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(bordeBorderColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_HEADER_BORDER_COLOR).getIndex();

		cellStyle.setLeftBorderColor(borderColorIndex);
		cellStyle.setRightBorderColor(borderColorIndex);
		cellStyle.setBottomBorderColor(borderColorIndex);
		cellStyle.setTopBorderColor(borderColorIndex);

		Font font = sheet.getWorkbook().createFont();

		Short headerFontSize = (Short) this.getProperty(PROPERTY_HEADER_FONT_SIZE);
		LOGGER.debug("Header font size : " + headerFontSize);
		short headerFontSizeShort = headerFontSize != null ? headerFontSize.shortValue() : DEFAULT_HEADER_FONT_SIZE;
		font.setFontHeightInPoints(headerFontSizeShort);

		String fontName = (String) this.getProperty(PROPERTY_FONT_NAME);
		LOGGER.debug("Font name : " + fontName);
		fontName = fontName != null ? fontName : DEFAULT_FONT_NAME;
		font.setFontName(fontName);

		String headerColor = (String) this.getProperty(PROPERTY_HEADER_COLOR);
		LOGGER.debug("Header color : " + headerColor);
		short headerColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(headerColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_HEADER_COLOR).getIndex();
		font.setColor(headerColorIndex);

		font.setBold(true);
		cellStyle.setFont(font);
		return cellStyle;
	}

	public CellStyle buildDataCellStyle(Sheet sheet) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
//		CreationHelper helper = wb.getCreationHelper();
//		XSSFCellStyle cellStyle = getCellStyleByFormat(wb, helper, STRING_CELL_DEFAULT_FORMAT, Optional.empty(), Optional.empty());
		cellStyle.setAlignment(HorizontalAlignment.RIGHT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		String cellBGColor = (String) this.getProperty(PROPERTY_CELL_BACKGROUND_COLOR);
		LOGGER.debug("Cell background color : " + cellBGColor);
		short backgroundColorIndex = cellBGColor != null ? IndexedColors.valueOf(cellBGColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_CELL_BACKGROUND_COLOR).getIndex();
		cellStyle.setFillForegroundColor(backgroundColorIndex);

		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);

		String bordeBorderColor = (String) this.getProperty(PROPERTY_CELL_BORDER_COLOR);
		LOGGER.debug("Cell border color : " + bordeBorderColor);
		short borderColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(bordeBorderColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_CELL_BORDER_COLOR).getIndex();

		cellStyle.setLeftBorderColor(borderColorIndex);
		cellStyle.setRightBorderColor(borderColorIndex);
		cellStyle.setBottomBorderColor(borderColorIndex);
		cellStyle.setTopBorderColor(borderColorIndex);

		Font font = sheet.getWorkbook().createFont();

		Short cellFontSize = (Short) this.getProperty(PROPERTY_CELL_FONT_SIZE);
		LOGGER.debug("Cell font size : " + cellFontSize);
		short cellFontSizeShort = cellFontSize != null ? cellFontSize.shortValue() : DEFAULT_CELL_FONT_SIZE;
		font.setFontHeightInPoints(cellFontSizeShort);

		String fontName = (String) this.getProperty(PROPERTY_FONT_NAME);
		LOGGER.debug("Font name : " + fontName);
		fontName = fontName != null ? fontName : DEFAULT_FONT_NAME;
		font.setFontName(fontName);

		String cellColor = (String) this.getProperty(PROPERTY_CELL_COLOR);
		LOGGER.debug("Cell color : " + cellColor);
		short cellColorIndex = cellColor != null ? IndexedColors.valueOf(cellColor).getIndex()
				: IndexedColors.valueOf(DEFAULT_CELL_COLOR).getIndex();
		font.setColor(cellColorIndex);

		cellStyle.setFont(font);
		return cellStyle;
	}

	/**
	 * Builds the columns' headers recursively with this order: |------------------------------------------| | 1 | 9 | |------------------------------------------|
	 * | 2 | 5 | 10 | |-----------|-----------------|------------| | 3 | 4 | 6 | 7 | 8 | 11 | 12 | |------------------------------------------|
	 *
	 * @param sheet              The sheet of the XLS file
	 * @param siblings           The siblings nodes of the headers structure
	 * @param rowNum             The row number where the siblings must be inserted
	 * @param columnNum          The column number where the first sibling must be inserted
	 * @param createHelper       The file creation helper
	 * @param dimensionCellStyle The cell style for cells containing dimensions (i.e. attributes' names)
	 * @param memberCellStyle    The cell style for cells containing members (i.e. attributes' values)
	 * @throws JSONException
	 */
	protected void buildColumnsHeader(Sheet sheet, CrossTab cs, List<Node> siblings, int rowNum, int columnNum,
									  CreationHelper createHelper, Locale locale, CellStyle memberCellStyle, CellStyle dimensionCellStyle,
									  int recursionLevel) throws JSONException {
		int columnCounter = columnNum;

		for (int i = 0; i < siblings.size(); i++) {
			Node aNode = siblings.get(i);
			List<Node> childs = aNode.getChildren();
			Row row = sheet.getRow(rowNum);
			Cell cell = row.createCell(columnCounter);

			String text = aNode.getDescription();
			// only odd levels are levels (except the last one, since it contains measures' names)
			boolean isLevel = isLevel(recursionLevel, aNode);
			if (isLevel) {
				if (!cs.getCrosstabDefinition().getColumns().isEmpty()) {
					CrosstabDefinition.Column aColDef = cs
							.getCrosstabDefinition().getColumns().get(recursionLevel / 2);
					String variable = aColDef.getVariable();
					if (variables.has(variable)) {
						text = variables.getString(variable);
					}
				}
			}
			if (!cs.isMeasureOnRow() && (childs == null || childs.isEmpty())) {
				// apply the measure scale factor
				text = MeasureScaleFactorOption.getScaledName(text, cs.getMeasureScaleFactor(text), locale);
			}

			cell.setCellValue(createHelper.createRichTextString(text));
			cell.setCellType(this.getCellTypeString());
			int descendants = aNode.getLeafsNumber();
			if (descendants > 1) {
				sheet.addMergedRegion(new CellRangeAddress(rowNum, // first row (0-based)
						rowNum, // last row (0-based)
						columnCounter, // first column (0-based)
						columnCounter + descendants - 1 // last column (0-based)
				));
			}

			/*
			 * Now we have to set the style properly according to the nature of the node: if it contains the name of a dimension or a member. Since the structure foresees
			 * that a list of members follows a dimension, we calculate the position of the node with respect to the leaves; in case it is odd, the cell contains a
			 * dimension; in case it is even, the cell contains a dimension.
			 */
			int distanceToLeaves = aNode.getDistanceFromLeaves();
			if (!cs.isMeasureOnRow()) {
				distanceToLeaves--;
			}
			boolean isDimensionNameCell = distanceToLeaves > 0 && (distanceToLeaves % 2) == 1;
			if (isDimensionNameCell) {
				cell.setCellStyle(dimensionCellStyle);
			} else {
				cell.setCellStyle(memberCellStyle);
			}

			if (childs != null && !childs.isEmpty()) {
				buildColumnsHeader(sheet, cs, childs, rowNum + 1, columnCounter, createHelper, locale, memberCellStyle,
						dimensionCellStyle, recursionLevel + 1);
			}
			int increment = descendants > 1 ? descendants : 1;
			columnCounter = columnCounter + increment;
		}
	}

	private boolean isLevel(int level, Node node) {
		if (level % 2 == 0) // only odd levels
			if (node.getDistanceFromLeaves() == 0) // discard measures
				return false;
			else
				return true;
		return false;
	}

	public CellStyle getStyle(int j, Sheet sheet, CreationHelper createHelper, CrossTab.CellType celltype, String measureId,
			Double value, CellStyle dataCellStyle) {

		if (celltype.equals(CrossTab.CellType.CF)) {
			j = this.getCalculatedFieldDecimals();
		}

		String decimals = "";

		for (int i = 0; i < j; i++) {
			decimals += "0";
		}

		DataFormat df = createHelper.createDataFormat();
		String format = "#,##0";
		if (decimals.length() > 0) {
			format += "." + decimals;
		}
		CellStyle cellStyle = dataCellStyle;
		cellStyle.setDataFormat(df.getFormat(format));

		if (celltype.equals(CrossTab.CellType.TOTAL)) {
			cellStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
		}
		if (celltype.equals(CrossTab.CellType.CF)) {
			cellStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.getIndex());
		}
		if (celltype.equals(CrossTab.CellType.SUBTOTAL)) {
			cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		}
		if (celltype.equals(CrossTab.CellType.DATA)) {
			((XSSFCellStyle) cellStyle).setFillForegroundColor(getThresholdColor(measureId, value));
		}

		return cellStyle;
	}

	private XSSFColor getThresholdColor(String measureId, Double value) {
		List<Threshold> thresholds = thresholdColorsMap.get(measureId);
		if (thresholds == null || thresholds.isEmpty())
			return new XSSFColor(new byte[] {(byte) 255, (byte) 255, (byte) 255});
		for (Threshold t : thresholds) {
			if (t.isConstraintSatisfied(value)) {
				XSSFColor backgroundColor = t.getXSSFColor();
				return backgroundColor;
			}
		}
		return new XSSFColor(new byte[] {(byte) 255, (byte) 255, (byte) 255});
	}

	public int getCalculatedFieldDecimals() {
		Integer decimals = (Integer) this.getProperty(PROPERTY_CALCULATED_FIELD_DECIMALS);
		if (decimals == null) {
			return DEFAULT_CALCULATED_FIELD_DECIMALS;
		}
		return decimals;
	}

	@Override
	public int export() {
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			String widgetName = getWidgetName(widget);

			JSONObject crosstabDefinition = optionsObj.getJSONObject("crosstabDefinition");
			CrossTab cs = buildCrossTab(crosstabDefinition);
			initExporter(crosstabDefinition);

			int totalRowsNumber = cs.getTotalNumberOfRows();
			int windowSize = Integer.parseInt(SingletonConfig.getInstance()
					.getConfigValue("KNOWAGE.DASHBOARD.EXPORT.EXCEL.STREAMING_WINDOW_SIZE"));
			if (totalRowsNumber <= windowSize || windowSize == -1) {
				// crosstab fits in memory
				String cockpitSheetName = getCockpitSheetName(template, widgetId);
				Sheet sheet = excelExporter.createUniqueSafeSheet(wb, widgetName, cockpitSheetName);
				((SXSSFSheet) sheet).setRandomAccessWindowSize(windowSize);
				fillExcelSheetWithData(sheet, cs, wb.getCreationHelper(), 0, excelExporter.getLocale());
				return 1;
			} else {
				// export crosstab as generic widget
				LOGGER.warn("Crosstab [" + widgetId
						+ "] has more rows than streaming windows size. It will be exported as a generic widget.");
				return super.export();
			}

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Unable to export crosstab widget: " + widgetId, e);
		}
	}

	private void initExporter(JSONObject crosstabDefinition) {
		JSONArray measures = crosstabDefinition.optJSONArray("measures");
		Map<String, List<Threshold>> thresholdColorsMap = getThresholdColorsMap(measures);
		JSONObject variables = optionsObj.optJSONObject("variables");
		this.properties = new Properties();
		this.variables = variables;
		this.thresholdColorsMap = thresholdColorsMap;
	}

	private Map<String, List<Threshold>> getThresholdColorsMap(JSONArray measures) {
		Map<String, List<Threshold>> toReturn = new HashMap<>();
		try {
			for (int i = 0; i < measures.length(); i++) {
				JSONObject measure = measures.getJSONObject(i);
				String id = measure.getString("id");
				if (!measure.has("ranges"))
					continue;
				JSONArray ranges = measure.getJSONArray("ranges");
				List<Threshold> allThresholds = new ArrayList<>();
				for (int j = 0; j < ranges.length(); j++) {
					JSONObject range = ranges.getJSONObject(j);
					String operator = range.getString("operator");
					if (!operator.equals("none")) {
						Double value = range.getDouble("value");
						String color = range.getString("background-color");
						Threshold threshold = new Threshold(operator, value, color);
						allThresholds.add(threshold);
					}
				}
				toReturn.put(id, allThresholds);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to build threshold color map", e);
			Map<String, List<Threshold>> emptyMap = new HashMap<>();
			return emptyMap;
		}
		return toReturn;
	}

	private CrossTab buildCrossTab(JSONObject crosstabDefinition) throws JSONException {
		// the id of the crosstab in the client configuration array
		JSONObject sortOptions = optionsObj.getJSONObject("sortOptions");
		JSONArray columnsSortKeysJo = sortOptions.optJSONArray("columnsSortKeys");
		JSONArray rowsSortKeysJo = sortOptions.optJSONArray("rowsSortKeys");
		JSONArray measuresSortKeysJo = sortOptions.optJSONArray("measuresSortKeys");
		int myGlobalId = sortOptions.optInt("myGlobalId");
		List<Map<String, Object>> columnsSortKeys = JSONUtils.toMap(columnsSortKeysJo);
		List<Map<String, Object>> rowsSortKeys = JSONUtils.toMap(rowsSortKeysJo);
		List<Map<String, Object>> measuresSortKeys = JSONUtils.toMap(measuresSortKeysJo);
		if (optionsObj != null) {
			LOGGER.debug("Export cockpit crosstab optionsObj.toString(): " + optionsObj.toString());
		}
		Map<Integer, NodeComparator> columnsSortKeysMap = toComparatorMap(columnsSortKeys);
		Map<Integer, NodeComparator> rowsSortKeysMap = toComparatorMap(rowsSortKeys);
		Map<Integer, NodeComparator> measuresSortKeysMap = toComparatorMap(measuresSortKeys);
		CrosstabBuilder builder = new CrosstabBuilder(excelExporter.getLocale(), crosstabDefinition,
				optionsObj.getJSONArray("jsonData"), optionsObj.getJSONObject("metadata"), null);

		CrossTab cs = builder.getSortedCrosstabObj(columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap,
				myGlobalId);
		return cs;
	}

	private Map<Integer, NodeComparator> toComparatorMap(List<Map<String, Object>> sortKeyMap) {
		Map<Integer, NodeComparator> sortKeys = new HashMap<>();

		for (int s = 0; s < sortKeyMap.size(); s++) {
			Map<String, Object> sMap = sortKeyMap.get(s);
			NodeComparator nc = new NodeComparator();

			nc.setParentValue((String) sMap.get("parentValue"));
			nc.setMeasureLabel((String) sMap.get("measureLabel"));
			if (sMap.get("direction") != null) {
				// the values in sMap sometimes have decimal part (es. "1.0"), so we need to parse them as double and then convert them to int
				nc.setDirection(Double.valueOf(sMap.get("direction").toString()).intValue());
				sortKeys.put(Double.valueOf(sMap.get("column").toString()).intValue(), nc);
			}
		}
		return sortKeys;
	}

}