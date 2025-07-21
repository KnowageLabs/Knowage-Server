package it.eng.knowage.engine.api.excel.export.dashboard;

import it.eng.knowage.engine.api.excel.export.dashboard.models.Style;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.eng.knowage.engine.api.excel.export.dashboard.StaticLiterals.EXCEL_ERROR;
import static it.eng.knowage.engine.api.excel.export.oldcockpit.exporters.CrossTabExporter.DEFAULT_FONT_NAME;
import static org.apache.poi.xssf.usermodel.XSSFFont.DEFAULT_FONT_SIZE;

public class StyleProvider extends Common {
    private static final Logger LOGGER = LogManager.getLogger(StyleProvider.class);

    private static final String STATIC_CUSTOM_STYLE = "static";
    private static final String CONDITIONAL_STYLE = "conditional";
    private static final String ALL_COLUMNS_STYLE = "all";
    private final JSONObjectUtils jsonObjectUtils;

    public StyleProvider (JSONObjectUtils jsonObjectUtils) {
        this.jsonObjectUtils = jsonObjectUtils;
    }

    public void adjustColumnWidth(Sheet sheet, String imageB64) {
        try {
            ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
            Row row = sheet.getRow(sheet.getLastRowNum());
            if (row != null) {
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    sheet.autoSizeColumn(i);
                    if (StringUtils.isNotEmpty(imageB64) && (i == 0 || i == 1)) {
                        // first or second column
                        int colWidth = 25;
                        if (sheet.getColumnWidthInPixels(i) < (colWidth * 256))
                            sheet.setColumnWidth(i, colWidth * 256);
                    }
                }
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException(EXCEL_ERROR, e);
        }
    }

    public CellStyle buildCellStyle(Sheet sheet, boolean bold, HorizontalAlignment alignment, VerticalAlignment verticalAlignment, short headerFontSizeShort) {

        // CELL
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();

        // alignment
        cellStyle.setAlignment(alignment);
        cellStyle.setVerticalAlignment(verticalAlignment);

        // FONT
        Font font = sheet.getWorkbook().createFont();

        font.setFontHeightInPoints(headerFontSizeShort);

        font.setBold(bold);

        cellStyle.setFont(font);
        return cellStyle;
    }

    void applyWholeRowStyle(int c, List<Boolean> styleCanBeOverriddenByWholeRowStyle, Row row, CellStyle cellStyle) {
        for (int previousCell = c - 1; previousCell >= 0; previousCell--) {
            if (styleCanBeOverriddenByWholeRowStyle.get(previousCell).equals(Boolean.TRUE)) {
                row.getCell(previousCell).setCellStyle(cellStyle);
            }
        }
    }

    JSONObject getRowStyle(JSONObject settings) {
        JSONObject style = jsonObjectUtils.getStyleFromSettings(settings);
        if (style != null && style.has("rows")) {
            JSONObject rows = style.optJSONObject("rows");
            return rows.optJSONObject("alternatedRows");
        }
        return null;
    }

    String getDefaultRowBackgroundColor(JSONObject altenatedRows, boolean rowIsEven) {
        try {
            if (altenatedRows != null && altenatedRows.getBoolean("enabled")) {
                if (rowIsEven) {
                    return altenatedRows.optString("evenBackgroundColor");
                } else {
                    return altenatedRows.optString("oddBackgroundColor");
                }
            }
        } catch (JSONException e) {
            LOGGER.error("Error while getting current row background color", e);
        }
        return "";
    }

    CellStyle getCellStyleByStyleKey(Workbook wb, Sheet sheet, String styleKey, Map<String, CellStyle> columnsCellStyles, JSONObject theRightStyle, String defaultRowBackgroundColor) {
        CellStyle cellStyle;
        if (columnAlreadyHasTheRightStyle(styleKey, columnsCellStyles)) {
            cellStyle = columnsCellStyles.get(styleKey);
        } else {
            Style styleCustomObj = getStyleCustomObjFromProps(sheet, theRightStyle, defaultRowBackgroundColor);
            cellStyle = buildPoiCellStyle(styleCustomObj, (XSSFFont) wb.createFont(), wb);
            columnsCellStyles.put(styleKey, cellStyle);
        }
        return cellStyle;
    }

    private boolean columnAlreadyHasTheRightStyle(String styleKey, Map<String, CellStyle> stylesMap) {
        return stylesMap.containsKey(styleKey);
    }

    boolean styleCanBeOverridden(JSONObject theRightStyle) throws JSONException {
        return (theRightStyle.has("type") && theRightStyle.getString("type").equals(STATIC_CUSTOM_STYLE)) || !theRightStyle.has("type");
    }

    String getStyleKey(JSONObject column, JSONObject theRightStyle, String rawCurrentNumberType) throws JSONException {
        return column.optString("id").concat(theRightStyle.getString("type").concat(theRightStyle.getString("styleIndex").concat(rawCurrentNumberType)));
    }

    Map<String, JSONArray> getStylesMap(JSONObject settings) {
        Map<String, JSONArray> stylesMap = new HashMap<>();
        try {
            JSONObject columns = jsonObjectUtils.getStyleFromSettings(settings).getJSONObject("columns");

            if (columns.getBoolean("enabled")) {
                JSONArray styles = columns.getJSONArray("styles");
                buildStylesMap(stylesMap, styles);
            }

            if (settings.has("conditionalStyles") && settings.getJSONObject("conditionalStyles").getBoolean("enabled")) {
                JSONObject conditionalStyles = settings.getJSONObject("conditionalStyles");
                JSONArray conditions = conditionalStyles.getJSONArray("conditions");

                buildStylesMap(stylesMap, conditions);
            }

        } catch (JSONException e) {
            LOGGER.debug("No styles found in settings", e);
            return stylesMap;
        }
        return stylesMap;
    }

    private void buildStylesMap(Map<String, JSONArray> stylesMap, JSONArray styles) throws JSONException {

        if (stylesMap == null) {
            stylesMap = new HashMap<>();
        }
        for (int i = 0; i < styles.length(); i++) {
            JSONObject style = styles.getJSONObject(i);
            JSONObject props = new JSONObject();
            if (style.has("condition")) {
                JSONObject condition = style.getJSONObject("condition");
                boolean applyToWholeRow = style.getBoolean("applyToWholeRow");
                props.put("applyToWholeRow", applyToWholeRow);
                props.put("condition", condition);
            }
            props.put("properties", style.getJSONObject("properties"));

            JSONArray target = getTarget(styles, i);

            for (int j = 0; j < target.length(); j++) {
                if (stylesMap.containsKey(target.getString(j))) {
                    stylesMap.get(target.getString(j)).put(props);
                } else {
                    JSONArray propertiesArray = new JSONArray();
                    propertiesArray.put(props);
                    stylesMap.put(target.getString(j), propertiesArray);
                }
            }
        }
    }

    private JSONArray getTarget(JSONArray styles, int i) throws JSONException {
        JSONArray target;
        try {
            target = styles.getJSONObject(i).getJSONArray("target");
        } catch (JSONException e) {
            target = new JSONArray();
            target.put(styles.getJSONObject(i).getString("target"));
        }
        return target;
    }

    JSONObject getTheRightStyleByColumnIdAndValue(Map<String, JSONArray> styles, String stringifiedValue, String columnId) throws JSONException {
        JSONObject customStyle = getTheStyleByValueAndColumnId(styles, stringifiedValue, columnId);

        if (customStyle.has("properties") && customStyle.getJSONObject("properties").length() == 0) {
            return getTheStyleByValueAndColumnId(styles, stringifiedValue, ALL_COLUMNS_STYLE);
        }
        return customStyle;
    }

    private JSONObject getTheStyleByValueAndColumnId(Map<String, JSONArray> styles, String stringifiedValue, String columnId) {
        try {
            JSONObject nonConditionalProps = new JSONObject();
            if (styles != null) {
                JSONArray columnStyles = styles.get(columnId);

                if (columnStyles == null) {
                    if (styles.get(ALL_COLUMNS_STYLE) == null) {
                        return getStyleObject(nonConditionalProps, STATIC_CUSTOM_STYLE, 0, false);
                    } else {
                        columnStyles = styles.get(ALL_COLUMNS_STYLE);
                    }
                }

                for (int i = 0; i < columnStyles.length(); i++) {
                    JSONObject style = columnStyles.getJSONObject(i);
                    JSONObject condition = style.optJSONObject("condition");
                    if (style.has("condition") && conditionIsApplicable(stringifiedValue, condition.optString("operator"), condition.getString("value"))) {
                        return getStyleObject(style.getJSONObject("properties"), CONDITIONAL_STYLE, i, style.getBoolean("applyToWholeRow"));
                    } else if (!style.has("condition")) {
                        nonConditionalProps = style.getJSONObject("properties");
                    }
                }
            } else {
                return getStyleObject(nonConditionalProps, STATIC_CUSTOM_STYLE, 0, false);
            }
            return getStyleObject(nonConditionalProps, STATIC_CUSTOM_STYLE, 0, false);
        } catch (JSONException e) {
            LOGGER.error("Error while checking if conditional style applies", e);
            throw new SpagoBIRuntimeException("Error while checking if conditional style applies", e);
        }
    }

    private JSONObject getStyleObject(JSONObject properties, String type, int styleIndex, boolean applyToWholeRow) {
        try {
            JSONObject style = new JSONObject();
            style.put("properties", properties);
            style.put("type", type);
            style.put("styleIndex", styleIndex);

            if (style.get("type").equals(CONDITIONAL_STYLE)) {
                style.put("applyToWholeRow", applyToWholeRow);
            }

            return style;
        } catch (JSONException e) {
            LOGGER.error("Error while building default non conditional style", e);
            throw new SpagoBIRuntimeException("Error while building default non conditional style", e);
        }
    }

    Style getStyleCustomObjFromProps(Sheet sheet, JSONObject props, String defaultRowBackgroundColor) {
        Style style = new Style();
        style.setSheet(sheet);
        props = props.optJSONObject("properties") == null ? props : props.optJSONObject("properties");

        style.setAlignItems(props.optString("align-items"));
        style.setJustifyContent(props.optString("justify-content"));
        style.setBackgroundColor(props.optString("background-color").isEmpty() ? defaultRowBackgroundColor : props.optString("background-color"));
        style.setColor(props.optString("color"));
        style.setFontSize(props.optString("font-size"));
        style.setFontWeight(props.optString("font-weight"));
        style.setFontStyle(props.optString("font-style"));

        return style;
    }

    void buildHeaderCellStyle(Workbook wb, Sheet sheet, JSONObject settings, XSSFFont font, Cell cell) throws JSONException {
        CellStyle headerCellStyle;
        JSONObject styleJSONObject = jsonObjectUtils.getStyleFromSettings(settings);
        if (settings != null && settings.has("style") && styleJSONObject.has("headers")) {
            Style style = getStyleCustomObjFromProps(sheet, styleJSONObject.getJSONObject("headers"), "");
            headerCellStyle = buildPoiCellStyle(style, font, wb);
        } else {
            headerCellStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 11);
        }
        cell.setCellStyle(headerCellStyle);
    }

    public CellStyle buildPoiCellStyle(Style style, XSSFFont font, Workbook wb) {
        CellStyle cellStyle = wb.createCellStyle();

        if (stringIsNotEmpty(style.getFontSize())) {
            font.setFontHeightInPoints(Short.parseShort(getOnlyTheNumericValueFromString(style.getFontSize())));
        } else {
            font.setFontHeightInPoints(DEFAULT_FONT_SIZE);
        }

        if (stringIsNotEmpty(style.getColor())) {
            font.setColor(getXSSFColorFromRGBA(style.getColor()));
        }

        if (stringIsNotEmpty(style.getBackgroundColor())) {
            cellStyle.setFillForegroundColor(getXSSFColorFromRGBA(style.getBackgroundColor()));
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        if (stringIsNotEmpty(style.getFontFamily())) {
            font.setFontName(style.getFontFamily());
        } else {
            font.setFontName(DEFAULT_FONT_NAME);
        }

        if (stringIsNotEmpty(style.getFontWeight())) {
            font.setBold(style.getFontWeight().equals("bold"));
        }

        if (stringIsNotEmpty(style.getFontStyle())) {
            font.setItalic(style.getFontStyle().equals("italic"));
        }

        if (stringIsNotEmpty(style.getAlignItems())) {
            cellStyle.setAlignment(getHorizontalAlignment(style.getAlignItems().toUpperCase()));
        }

        if (stringIsNotEmpty(style.getJustifyContent())) {
            cellStyle.setVerticalAlignment(getVerticalAlignment(style.getJustifyContent().toUpperCase()));
        }

        cellStyle.setFont(font);
        return cellStyle;
    }

    private HorizontalAlignment getHorizontalAlignment(String alignItem) {
        return switch (alignItem) {
            case "CENTER" -> HorizontalAlignment.CENTER;
            case "FLEX-END" -> HorizontalAlignment.RIGHT;
            default -> HorizontalAlignment.LEFT;
        };
    }

    private VerticalAlignment getVerticalAlignment(String justifyContent) {
        return switch (justifyContent) {
            case "CENTER" -> VerticalAlignment.CENTER;
            case "FLEX-END" -> VerticalAlignment.BOTTOM;
            default -> VerticalAlignment.TOP;
        };
    }

    private String getOnlyTheNumericValueFromString(String string) {
        return string.replaceAll("[^0-9]", "");
    }

    private XSSFColor getXSSFColorFromRGBA(String colorStr) {
        String[] values = colorStr.replace(colorStr.contains("rgba(") ? "rgba(" : "rgb(", "").replace(")", "").split(",");
        int red = Integer.parseInt(values[0].trim());
        int green = Integer.parseInt(values[1].trim());
        int blue = Integer.parseInt(values[2].trim());

        // Handle alpha transparency
        if (values.length > 3) {
            float alpha = Float.parseFloat(values[3].trim());

            // For partial transparency, blend with white background
            // This simulates how transparent colors appear on a white Excel background
            if (alpha <= 1.0f) {
                red = (int) (red * alpha + 255 * (1 - alpha));
                green = (int) (green * alpha + 255 * (1 - alpha));
                blue = (int) (blue * alpha + 255 * (1 - alpha));
            }
        }

        // Ensure values are within valid range
        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        return new XSSFColor(new java.awt.Color(red, green, blue), new DefaultIndexedColorMap());
    }

    protected boolean stringIsNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }
}
