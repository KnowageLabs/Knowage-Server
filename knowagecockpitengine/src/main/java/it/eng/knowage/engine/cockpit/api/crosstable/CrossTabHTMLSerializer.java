/*
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
package it.eng.knowage.engine.cockpit.api.crosstable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.api.crosstable.CrossTab.CellType;
import it.eng.knowage.engine.cockpit.api.crosstable.CrossTab.MeasureInfo;
import it.eng.knowage.engine.cockpit.api.crosstable.CrosstabDefinition.Column;
import it.eng.knowage.engine.cockpit.api.crosstable.CrosstabDefinition.Row;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.messages.EngineMessageBundle;

public class CrossTabHTMLSerializer {

	private static String TABLE_TAG = "TABLE";
	private static String ROW_TAG = "TR";
	private static String COLUMN_TAG = "TD";
	private static String ICON_TAG = "I";
	private static String COLUMN_DIV = "DIV";
	private static String CLASS_ATTRIBUTE = "class";
	private static String STYLE_ATTRIBUTE = "style";
	private static String TITLE_ATTRIBUTE = "title";
	private static String ROWSPAN_ATTRIBUTE = "rowspan";
	private static String COLSPAN_ATTRIBUTE = "colspan";
	private static final String ON_CLICK_ATTRIBUTE = "onClick";
	private static final String NG_CLICK_ATTRIBUTE = "ng-click";

	private static String MEMBER_CLASS = "member";
	private static String LEVEL_CLASS = "level";
	private static String NA_CLASS = "na";
	private static String EMPTY_CLASS = "empty";
	private static String HEADER_CLASS = "crosstab-header-text";
	private static String MEASURES_CLASS = "measures-header-text";

	private static String DEFAULT_BG_TOTALS = "background:rgba(59, 103, 140, 0.8);";
	private static String DEFAULT_BG_SUBTOTALS = "background:rgba(59, 103, 140, 0.45);";
	private static String DEFAULT_COLOR_TOTALS = "white;";
	private static String DEFAULT_STYLE = " font-style:normal!important;";

	private Locale locale = null;
	private final Integer myGlobalId;
	private final Map<Integer, NodeComparator> columnsSortKeysMap;
	private final Map<Integer, NodeComparator> rowsSortKeysMap;
	private final Map<Integer, NodeComparator> measuresSortKeysMap;

	private static Logger logger = Logger.getLogger(CrossTabHTMLSerializer.class);

	public CrossTabHTMLSerializer(Locale locale, Integer myGlobalId, Map<Integer, NodeComparator> columnsSortKeysMap,
			Map<Integer, NodeComparator> rowsSortKeysMap, Map<Integer, NodeComparator> measuresSortKeysMap) {
		this.columnsSortKeysMap = columnsSortKeysMap;
		this.rowsSortKeysMap = rowsSortKeysMap;
		this.measuresSortKeysMap = measuresSortKeysMap;
		this.locale = locale;
		this.myGlobalId = myGlobalId;
	}

	public Locale getLocale() {
		return this.locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;

	}

	public String serialize(CrossTab crossTab) {
		logger.debug("IN");
		String html = null;
		try {
			SourceBean sb = this.getSourceBean(crossTab);
			html = sb.toXML(false);
		} catch (Exception e) {
			logger.error("Error while serializing crossTab", e);
			throw new SpagoBIEngineRuntimeException("Error while serializing crossTab", e);
		}
		LogMF.debug(logger, "OUT : returning {0}", html);
		return html;
	}

	private SourceBean getSourceBean(CrossTab crossTab) throws SourceBeanException, JSONException {
		SourceBean toReturn = new SourceBean(COLUMN_DIV);

		SourceBean emptyTopLeftCorner = this.serializeTopLeftCorner(crossTab);
		SourceBean rowsHeaders = this.serializeRowsHeaders(crossTab);
		SourceBean topLeftCorner = this.mergeVertically(emptyTopLeftCorner, rowsHeaders);
		SourceBean columnsHeaders = this.serializeColumnsHeaders(crossTab);
		SourceBean head = this.mergeHorizontally(topLeftCorner, columnsHeaders);

		SourceBean rowsMember = this.serializeRowsMembers(crossTab);
		SourceBean data = this.serializeData(crossTab);
		SourceBean body = this.mergeHorizontally(rowsMember, data);

		SourceBean crossTabSB = this.mergeVertically(head, body);
		toReturn.setAttribute(crossTabSB);
		return crossTabSB;
	}

	private SourceBean serializeRowsMembers(CrossTab crossTab) throws SourceBeanException, JSONException {
		SourceBean table = new SourceBean(TABLE_TAG);
		int leaves = crossTab.getRowsRoot().getLeafsNumber();

		List<SourceBean> rows = new ArrayList<SourceBean>();
		// initialize all rows (with no columns)
		for (int i = 0; i < leaves; i++) {
			SourceBean aRow = new SourceBean(ROW_TAG);
			table.setAttribute(aRow);
			rows.add(aRow);
		}

		int levels = crossTab.getRowsRoot().getDistanceFromLeaves();
		if (crossTab.isMeasureOnRow()) {
			levels--;
		}
		boolean addedLabelTotal = false;
		for (int i = 0; i < levels; i++) {
			List<Node> levelNodes = crossTab.getRowsRoot().getLevel(i + 1);
			int counter = 0;
			for (int j = 0; j < levelNodes.size(); j++) {
				SourceBean aRow = rows.get(counter);
				Node aNode = levelNodes.get(j);
				SourceBean aColumn = new SourceBean(COLUMN_TAG);

				String text = null;
				if (crossTab.getCrosstabDefinition().isMeasuresOnRows() && i + 1 == levels) {
					String measureAlias = aNode.getDescription();
					text = MeasureScaleFactorOption.getScaledName(measureAlias, crossTab.getMeasureScaleFactor(measureAlias), this.locale);
				} else {
					text = aNode.getDescription();
					if (text.equalsIgnoreCase("Total")) {
						if (addedLabelTotal) {
							text = "";
						} else
							addedLabelTotal = true;
					}
				}
				// Get specific columns configuration (format, bgcolor, icon visualization,..)
				String style;
				boolean appliedStyle = false;
				List<Row> rowsDef = crossTab.getCrosstabDefinition().getRows();
				Row row = rowsDef.get(i);

				JSONObject rowConfig = row.getConfig();
				style = getConfiguratedElementStyle(null, null, rowConfig, crossTab);
				if (!style.equals(DEFAULT_STYLE)) {
					 if (!text.equalsIgnoreCase("Total") && !text.equalsIgnoreCase("SubTotal")) {
						aColumn.setAttribute(STYLE_ATTRIBUTE, style);
						appliedStyle = true;
					 } else {
						 // get only the alignment from the detail configuration cells
						 String totStyle = getConfiguratedElementStyle(null, null, rowConfig, crossTab, "text-align");
						 aColumn.setAttribute(STYLE_ATTRIBUTE, totStyle);
					 }
				}
//				if (text.equalsIgnoreCase("Total")) {
//					aColumn.setAttribute(CLASS_ATTRIBUTE, "totals");
//				}else if (text.equalsIgnoreCase("SubTotal")) {
//					aColumn.setAttribute(CLASS_ATTRIBUTE, "partialsum");
//				}else{
//					if (!appliedStyle)
//						aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
//					else
//						aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS + "NoStandardStyle");
//				}
				if (!appliedStyle)
					aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
				else
					aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS + "NoStandardStyle");

				aColumn.setAttribute(NG_CLICK_ATTRIBUTE, "selectRow('" + crossTab.getCrosstabDefinition().getRows().get(i).getEntityId() + "','" + text + "')");
				aColumn.setCharacters(text);
				int rowSpan = aNode.getLeafsNumber();
				if (rowSpan > 1) {
					aColumn.setAttribute(ROWSPAN_ATTRIBUTE, rowSpan);
				}
				aColumn.setAttribute(TITLE_ATTRIBUTE, text);
				aRow.setAttribute(aColumn);
				counter = counter + rowSpan;
			}
		}

		return table;
	}

	private SourceBean serializeColumnsHeaders(CrossTab crossTab) throws SourceBeanException, JSONException {
		SourceBean table = new SourceBean(TABLE_TAG);
		String parentStyle = null;
		List<String> categoriesValues = null;

		int levels = crossTab.getColumnsRoot().getDistanceFromLeaves();
		if (levels == 0) {
			// nothing on columns
			SourceBean aRow = new SourceBean(ROW_TAG);
			SourceBean aColumn = new SourceBean(COLUMN_TAG);
			aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
			aColumn.setCharacters(EngineMessageBundle.getMessage("sbi.crosstab.runtime.headers.data", this.getLocale()));
			aRow.setAttribute(aColumn);
			table.setAttribute(aRow);
		} else {
			int measureNumber = crossTab.getCrosstabDefinition().getMeasures().size();
			List<String> levelValues = new ArrayList();
			List<String> lastLevelValues = new ArrayList();
			for (int i = 0; i < levels; i++) {
				boolean showHeader = true;
				SourceBean aRow = new SourceBean(ROW_TAG);
				List<Node> levelNodes = crossTab.getColumnsRoot().getLevel(i + 1);

				for (int j = 0; j < levelNodes.size(); j++) {
					Node aNode = levelNodes.get(j);

					SourceBean aColumn = new SourceBean(COLUMN_TAG);
					// odd levels are levels (except the last one, since it
					// contains measures' names)
					boolean isLevel = !((i + 1) % 2 == 0 || (i + 1) == levels);

					String className = !isLevel ? MEMBER_CLASS : LEVEL_CLASS;
					aColumn.setAttribute(CLASS_ATTRIBUTE, className);

					String text = null;
					String style = "";
					if (crossTab.getCrosstabDefinition().isMeasuresOnColumns() && i + 1 == levels) {
						String measureAlias = aNode.getDescription();
						text = MeasureScaleFactorOption.getScaledName(measureAlias, crossTab.getMeasureScaleFactor(measureAlias), this.locale);
						// check header visibility for measures
						showHeader = isMeasureHeaderVisible(crossTab);
					} else {
						// categories headers
						text = aNode.getDescription();
						// Set specific columns configuration style
						List<Column> columns = crossTab.getCrosstabDefinition().getColumns();
						for (int c = 0; c < columns.size(); c++) {
							Column col = columns.get(c);
							if (col.getAlias().equals(text)) {
								JSONObject columnConfig = col.getConfig();
								if (isLevel && !columnConfig.isNull("showHeader"))
									showHeader = columnConfig.getBoolean("showHeader");
								style = getConfiguratedElementStyle(null, null, columnConfig, crossTab);
								if (style.equals(DEFAULT_STYLE))
									style = ""; // clean from default ... just for the categories headers
								else {
									aColumn.setAttribute(STYLE_ATTRIBUTE, style);
									parentStyle = style;
									break;
								}
							}
						}
					}

					if (isLevel) {
						aColumn.setAttribute(NG_CLICK_ATTRIBUTE, "orderPivotTable('" + i + "','1'," + myGlobalId + ")");

						Integer direction = 1;
						if (columnsSortKeysMap != null && columnsSortKeysMap.get(i) != null) {
							direction = columnsSortKeysMap.get(i).getDirection();
						}

						if (parentStyle != null)
							style = parentStyle;
						aColumn.setAttribute(addSortArrow(aRow, text, style, null, direction, false));
						aColumn.setAttribute(STYLE_ATTRIBUTE, style);
						aColumn.setAttribute(CLASS_ATTRIBUTE, HEADER_CLASS);

					} else {
						boolean parentIsLevel = !((i) % 2 == 0 || (i) == levels);
						if (parentIsLevel) {
							aColumn.setAttribute(NG_CLICK_ATTRIBUTE,
									"selectRow('" + crossTab.getColumnsRoot().getLevel(i).get(0).getValue() + "','" + text + "')");
							levelValues.add(text);
							if (crossTab.getCrosstabDefinition().isMeasuresOnColumns() && i + 2 == levels) {
								String completeText = CrossTab.PATH_SEPARATOR;
								if (aNode.getParentNode() != null && !aNode.getParentNode().getDescription().equalsIgnoreCase("rootC")
										&& !aNode.getParentNode().getDescription().equalsIgnoreCase("rootR")) {
									completeText += aNode.getParentNode().getDescription() + CrossTab.PATH_SEPARATOR;
								}
								completeText += text;
								lastLevelValues.add(completeText);
							}
							// set width column if measure doesn't show header. Get values from measure settings
							if (!isMeasureHeaderVisible(crossTab) && i + 2 == levels) {
								// if not measure are visible set
								String measureStyle = getMeasureWidthStyle(crossTab, null);
								if (!measureStyle.equals("")) {
									aColumn.setAttribute(STYLE_ATTRIBUTE, measureStyle);
									// ONLY in this case (unique measure without header) add a div to force width if it's defined
									SourceBean divEl = new SourceBean(COLUMN_DIV);
									divEl.setCharacters(text);
									divEl.setAttribute(TITLE_ATTRIBUTE, text);
									divEl.setAttribute(STYLE_ATTRIBUTE, measureStyle);
									aColumn.setAttribute(divEl);
								} else
									aColumn.setCharacters(text);
							} else
								aColumn.setCharacters(text);

							if (parentStyle != null) {
								aColumn.setAttribute(STYLE_ATTRIBUTE, parentStyle);
								aColumn.setAttribute(CLASS_ATTRIBUTE, "memberNoStandardStyle");
							} else
								aColumn.setAttribute(CLASS_ATTRIBUTE, HEADER_CLASS);
						} else {
							// Set specific measures configuration style
							String measureStyle = getMeasureWidthStyle(crossTab, text);
							if (!measureStyle.equals(""))
								aColumn.setAttribute(STYLE_ATTRIBUTE, measureStyle);
							table.setAttribute("table-layout", "fixed;");
							String measureParentValue = "";
							Integer direction = null;
							if (categoriesValues == null)
								categoriesValues = getCompleteCategoriesValues(measureNumber, lastLevelValues);
							if (categoriesValues.size() > 0) {
								measureParentValue = categoriesValues.get(j);
								if (measureParentValue.indexOf(CrossTab.TOTAL) < 0 && measureParentValue.indexOf(CrossTab.SUBTOTAL) < 0) {
									if (measuresSortKeysMap != null && measuresSortKeysMap.get(j) != null) {
										direction = measuresSortKeysMap.get(j).getDirection();
									}
								}
							}
							aColumn.setAttribute(addSortArrow(aRow, text, parentStyle, measureStyle, direction, true));
							aColumn.setAttribute(NG_CLICK_ATTRIBUTE,
									"orderPivotTable('" + j + "','1'," + myGlobalId + ", '" + text + "' , '" + measureParentValue + "')");
						}
					}

					int colSpan = aNode.getLeafsNumber();
					if (colSpan > 1) {
						aColumn.setAttribute(COLSPAN_ATTRIBUTE, colSpan);
					}
					aRow.setAttribute(aColumn);
				}
				if (showHeader)
					table.setAttribute(aRow);
			}
		}
		return table;
	}

	private String getMeasureWidthStyle(CrossTab crossTab, String text) throws JSONException {
		String measureStyle = "";
		List<Measure> measures = crossTab.getCrosstabDefinition().getMeasures();
		for (int m = 0; m < measures.size(); m++) {
			Measure mis = measures.get(m);
			if (text == null || mis.getAlias().equals(text)) {
				JSONObject measureConfig = mis.getConfig();
				String width = getConfiguratedElementStyle(null, null, measureConfig, crossTab, "width");
				if (!width.equals("")) {
					if (width.indexOf("%") >= 0)
						width = ""; // set width only with pixel values (for div)
					String display = " overflow:hidden; text-overflow:ellipses;";
					measureStyle = width + display;
					break;
				}
			}
		}
		return measureStyle;
	}

	private boolean isMeasureHeaderVisible(CrossTab crossTab) throws JSONException {
		boolean showHeader = true;
		// check header visibility for measures
		List<Measure> measures = crossTab.getCrosstabDefinition().getMeasures();
		if (measures.size() == 1) {
			Measure measure = measures.get(0);
			JSONObject measureConfig = measure.getConfig();
			if (!measureConfig.isNull("showHeader"))
				showHeader = measureConfig.getBoolean("showHeader");
		} else
			// for default with 2 or more measures the header is always visible
			showHeader = true;

		return showHeader;
	}

	private List<String> getCompleteCategoriesValues(int measuresNumber, List<String> levelValues) {
		List<String> toReturn = new ArrayList();

		if (levelValues == null)
			return toReturn;

		if (measuresNumber == 1)
			return levelValues;

		// normalization levels list: propagates each level's element for all measures
		for (String l : levelValues) {
			for (int i = 0; i < measuresNumber; i++) {
				toReturn.add(l);
			}
		}

		return toReturn;
	}

	private SourceBean serializeData(CrossTab crossTab) throws SourceBeanException, JSONException {
		SourceBean table = new SourceBean(TABLE_TAG);
		String[][] data = crossTab.getDataMatrix();
		List<MeasureInfo> measuresInfo = crossTab.getMeasures();
		List<SourceBean> measureHeaders = new ArrayList<SourceBean>();
		List<String> columnsSpecification = crossTab.getColumnsSpecification();
		boolean isDataNoStandardStyle = false;

		if (columnsSpecification.size() > 0) {
			// defines columns specification with totals and subtotals if required (for action #7 selection function setting)
			if (crossTab.isMeasureOnRow()) {
				List<CellType> columnsTypes = crossTab.getCelltypeOfColumns();
				List<String> columnsSpecificationWithTotals = new ArrayList();
				if (columnsTypes.size() > columnsSpecification.size()) {
					int nTotal = 0;
					for (int c = 0; c < columnsTypes.size(); c++) {
						if (columnsTypes.get(c).getValue().equalsIgnoreCase("data")) {
							columnsSpecificationWithTotals.add(columnsSpecification.get(c - nTotal));
						} else {
							columnsSpecificationWithTotals.add(columnsTypes.get(c).getValue());
							nTotal++;
						}
					}
					columnsSpecification = columnsSpecificationWithTotals;
				}
			} else {
				List<CellType> columnsTypes = crossTab.getCelltypeOfColumns();
				List<String> columnsSpecificationWithTotals = new ArrayList();
				if (columnsTypes.size() > columnsSpecification.size()) {
					int nTotal = 0;
					for (int c = 0; c < columnsTypes.size(); c = c + (measuresInfo.size())) {
						if (columnsTypes.get(c).getValue().equalsIgnoreCase("data")) {
							columnsSpecificationWithTotals.add(columnsSpecification.get((c / measuresInfo.size()) - nTotal));
						} else {
							columnsSpecificationWithTotals.add(columnsTypes.get(c).getValue().toUpperCase());
							nTotal++;
						}
					}
					columnsSpecification = columnsSpecificationWithTotals;
				}
			}
		}

		if (crossTab.isMeasureOnRow()) {
			// adds the measure label
			for (MeasureInfo measureInfo : crossTab.getMeasures()) {
				SourceBean aMeasureHeader = new SourceBean(COLUMN_TAG);
				// Get specific columns configuration (format, bgcolor, icon visualization,..)
				List<Measure> measures = crossTab.getCrosstabDefinition().getMeasures();
				boolean showHeader = true;
				aMeasureHeader.setAttribute(CLASS_ATTRIBUTE, MEASURES_CLASS);
				if (showHeader)
					aMeasureHeader.setCharacters(measureInfo.getName());
				measureHeaders.add(aMeasureHeader);
			}
		}

		MeasureFormatter measureFormatter = new MeasureFormatter(crossTab);
		// int measureHeaderSize = measureHeaders.size();
		int measureHeaderSize = crossTab.getMeasures().size();

		// // ONLY FOR TEST
		// System.out.println("measureCord.length: " + crossTab.getMeasuresCordinates().size() + " - rowsCordi.length: " + crossTab.getRowCordinates().size()
		// + " - columnCord.length: " + crossTab.getColumnCordinates().size());
		// for (int m = 0; m < crossTab.getColumnCordinates().size(); m++) {
		// String tmp = crossTab.getColumnCordinates().get(m);
		// System.out.println("** ColumnCord[" + m + "]: " + tmp);
		// }
		// for (int m = 0; m < crossTab.getRowCordinates().size(); m++) {
		// String tmp = crossTab.getRowCordinates().get(m);
		// System.out.println("** RowCord[" + m + "]: " + tmp);
		// }
		// // FINE TEST
		int nPartialSum = 0;
		for (int i = 0; i < data.length; i++) {
			SourceBean aRow = new SourceBean(ROW_TAG);

			if (crossTab.isMeasureOnRow() && measureHeaders.size() > 0) {
				aRow.setAttribute(measureHeaders.get(i % measureHeaderSize));
			}
			String[] values = data[i];
			int pos;
			boolean hasPartialSum = false;
			for (int j = 0; j < values.length; j++) {
				String text = values[j];
				SourceBean aColumn = new SourceBean(COLUMN_TAG);
				CellType cellType = crossTab.getCellType(i, j);

				String classType = "";
				JSONObject measureConfig = new JSONObject();
				try {
					// 1. Get specific measure configuration (format, bgcolor, icon visualization,..)
					if (crossTab.isMeasureOnRow()) {
						pos = i % measuresInfo.size();
					} else {
						pos = j % measuresInfo.size();
					}
					measureConfig = crossTab.getCrosstabDefinition().getMeasures().get(pos).getConfig();
					String visType = (measureConfig.isNull("visType")) ? "Text" : measureConfig.getString("visType");
					boolean showIcon = false;
					SourceBean iconSB = null;

					if (cellType.getValue().equalsIgnoreCase("data") && !measureConfig.isNull("scopeFunc")) {
						// check indicator configuration (optional)
						JSONObject indicatorJ = measureConfig.getJSONObject("scopeFunc");
						JSONArray indicatorConditionsJ = indicatorJ.getJSONArray("condition");
						if (!text.equals("")) {
							for (int c = 0; c < indicatorConditionsJ.length(); c++) {
								JSONObject condition = indicatorConditionsJ.getJSONObject(c);
								if (iconSB == null && !condition.isNull("value")) {
									// gets icon html
									showIcon = true;
									iconSB = getIconSB(Double.parseDouble(text), condition);
								}
							}
						}
					}

					classType = cellType.getValue();
					Double value = (!text.equals("")) ? Double.parseDouble(text) : null;
					// 2. style and alignment management
					if (cellType.getValue().equalsIgnoreCase("data") ) {
						String dataStyle = getConfiguratedElementStyle(value, cellType, measureConfig, crossTab);
						if (!dataStyle.equals(DEFAULT_STYLE)) {
							aColumn.setAttribute(STYLE_ATTRIBUTE, dataStyle);
							classType += "NoStandardStyle";
							isDataNoStandardStyle = true;
							aColumn.setAttribute(CLASS_ATTRIBUTE, "dataNoStandardStyle");
						} else {
							isDataNoStandardStyle = false;
							aColumn.setAttribute(CLASS_ATTRIBUTE, "data");
						}
					}else {
						String align = getConfiguratedElementStyle(null, null, measureConfig, crossTab, "text-align");
						aColumn.setAttribute(STYLE_ATTRIBUTE, align);
						aColumn.setAttribute(CLASS_ATTRIBUTE,classType);
					}

					if (value == null) {
						aRow.setAttribute(aColumn);
						continue;
					}

					// 3. define value (number) and its final visualization
					String actualText = "";
					if (visType.indexOf("Text") >= 0) {
						String patternFormat = null;
						int patternPrecision = 2; // default
						String prefix = null;
						String suffix = null;

						if (!measureConfig.isNull("style") && !measureConfig.getJSONObject("style").isNull("prefix")) {
							prefix = measureConfig.getJSONObject("style").getString("prefix");
						}

						if (!measureConfig.isNull("style") && !measureConfig.getJSONObject("style").isNull("suffix")) {
							suffix = measureConfig.getJSONObject("style").getString("suffix");
						}

						if (!measureConfig.isNull("style") && !measureConfig.getJSONObject("style").isNull("format")) {
							patternFormat = measureConfig.getJSONObject("style").getString("format");
						}

						if (!measureConfig.isNull("style") && !measureConfig.getJSONObject("style").isNull("precision")) {
							patternPrecision = measureConfig.getJSONObject("style").getInt("precision");
						}
						// 4. formatting value...
						actualText = measureFormatter.format(value, patternFormat, patternPrecision, i, j, this.locale);

						String percentOn = crossTab.getCrosstabDefinition().getConfig().optString("percenton");
						if ("row".equals(percentOn) || "column".equals(percentOn)) {
							Double percent = calculatePercent(value, i, j, percentOn, crossTab);
							if (!percent.equals(Double.NaN) && !percent.equals(Double.POSITIVE_INFINITY) && !percent.equals(Double.NEGATIVE_INFINITY)) {
								String percentStr = measureFormatter.formatPercent(percent, this.locale);
								actualText += " (" + percentStr + "%)";
							}
						}

						// 5. prefix and suffix management ...
						if (prefix != null) {
							actualText = prefix + actualText;
						}
						if (suffix != null) {
							actualText += suffix;
						}
					}

					// add icon html if required
					if (showIcon && iconSB != null) {
						actualText += " ";
						aColumn.setAttribute(iconSB);
					}

					// 6. set value
					// aColumn.setAttribute(CLASS_ATTRIBUTE, classType);
					aColumn.setCharacters(actualText);
					aColumn.setAttribute(TITLE_ATTRIBUTE, actualText);

					// 7. set selection function with the parent references (on row and column)
					String cellTypeValue = cellType.getValue();
					if (cellTypeValue.equalsIgnoreCase("data")) {
						String rowCord = "";
						String rowHeaders = "";
						if (crossTab.getRowsSpecification().size() > 0) {
							List rowsDef = crossTab.getRowsHeaderList();
							for (int r = 0; r < rowsDef.size(); r++) {
								rowHeaders += crossTab.PATH_SEPARATOR + rowsDef.get(r);
							}
							if (!crossTab.isMeasureOnRow()) {
//								int posRow = i - nPartialSum;
								int posRow = (i < nPartialSum) ? i : i - nPartialSum;
								if (posRow < crossTab.getRowsSpecification().size())
									rowCord = (crossTab.getRowsSpecification().get(posRow) != null) ? crossTab.getRowsSpecification().get(posRow) : null;
							} else {
								int idx  =  (i < nPartialSum) ? i : i - nPartialSum;
								int posRow = (measuresInfo.size() == 1) ? idx: idx / measuresInfo.size();
								if (posRow < crossTab.getRowsSpecification().size())
									rowCord = (crossTab.getRowsSpecification().get(posRow) != null) ? crossTab.getRowsSpecification().get(posRow) : null;
							}
						}
						String columnCord = "";
						String columnsHeaders = "";

						if (columnsSpecification.size() > 0) {
							List<String> columnsDef = crossTab.getColumnsHeaderList();
							for (int c = 0; c < columnsDef.size(); c++) {
								columnsHeaders += crossTab.PATH_SEPARATOR + columnsDef.get(c);
							}
							if (!crossTab.isMeasureOnRow()) {
								int posColumn = (measuresInfo.size() == 1) ? j : j / measuresInfo.size();
								columnCord = (columnsSpecification.get(posColumn) != null) ? columnsSpecification.get(posColumn) : null;
							} else {
								int posColumn = j;
								columnCord = (columnsSpecification.get(posColumn) != null) ? columnsSpecification.get(posColumn) : null;
							}
						}

						aColumn.setAttribute(NG_CLICK_ATTRIBUTE,
								"selectMeasure('" + rowHeaders + "','" + rowCord + "','" + columnsHeaders + "','" + columnCord + "')");
					} else if (!hasPartialSum && cellTypeValue.equalsIgnoreCase("partialsum")) {
						nPartialSum++; // update contator of subtotals (1 or more for row)
						hasPartialSum = true;
					}

				} catch (NumberFormatException e) {
					logger.debug("Text " + text + " is not recognized as a number");
					if (isDataNoStandardStyle)
						aColumn.setAttribute(CLASS_ATTRIBUTE, "dataNoStandardStyle");
					else
						aColumn.setAttribute(CLASS_ATTRIBUTE, "data");

					aColumn.setCharacters(text);
				}
				aRow.setAttribute(aColumn);
			}
			table.setAttribute(aRow);
		}
		return table;
	}

	private String getThresholdColor(double value, JSONObject colorThrJ) throws JSONException {
		String toReturn = "";
		JSONArray thresholdConditions = colorThrJ.getJSONArray("condition");
		JSONObject thresholdConditionValues = (colorThrJ.isNull("conditionValue")) ? null : colorThrJ.getJSONObject("conditionValue");
		JSONArray thresholdConditions2 = colorThrJ.optJSONArray("condition2");
		JSONObject thresholdConditionValues2 = (colorThrJ.isNull("conditionValue2")) ? null : colorThrJ.getJSONObject("conditionValue2");
		JSONObject thresholdColors = (colorThrJ.isNull("color")) ? null : colorThrJ.getJSONObject("color");
		boolean isConditionVerified = false;

		for (int c = 0; c < thresholdConditions.length(); c++) {
			String thrCond = (String) thresholdConditions.get(c);
			if (!thrCond.equalsIgnoreCase("none")) {
				double thrCondValue = thresholdConditionValues.getDouble(String.valueOf(c));
				isConditionVerified = verifyThresholdCondition(thrCond, thrCondValue, value);
				if (isConditionVerified && thresholdConditions2 != null) {
					// check if there is also a second condition that MUST be true
					String thrCond2 = (String) thresholdConditions2.get(c);
					if (!thrCond2.equalsIgnoreCase("none")) {
						double thrCondValue2 = thresholdConditionValues2.getDouble(String.valueOf(c));
						boolean isCondition2Verified = verifyThresholdCondition(thrCond2, thrCondValue2, value);
						isConditionVerified = isConditionVerified && isCondition2Verified;
					}
				}
			}
			if (isConditionVerified)
				return thresholdColors.getString(String.valueOf(c));
		}
		return toReturn;
	}

	private boolean verifyThresholdCondition(String condition, double value, double valueToTest) {
		boolean isConditionVerified = false;

		switch (condition) {
		case "<":
			if (valueToTest < value)
				isConditionVerified = true;
			break;
		case ">":
			if (valueToTest > value)
				isConditionVerified = true;
			break;
		case "=":
			if (valueToTest == value)
				isConditionVerified = true;
			break;
		case ">=":
			if (valueToTest >= value)
				isConditionVerified = true;
			break;
		case "<=":
			if (valueToTest <= value)
				isConditionVerified = true;
			break;
		case "!=":
			if (valueToTest != value)
				isConditionVerified = true;
			break;
		default:
			break;
		}
		return isConditionVerified;
	}

	// get the specific prop from the style definition if it's valorized
	private String getConfiguratedElementStyle(Double value, CellType cellType, JSONObject config, CrossTab crossTab, String prop) throws JSONException {
		String toReturn = "";
		String dataStyle = "";
		dataStyle = getConfiguratedElementStyle(value, cellType, config, crossTab);

		if (dataStyle.equals(""))
			return toReturn;

		String[] props = dataStyle.split(";");
		for (int p = 0; p < props.length; p++) {
			String property = props[p];
			String propKey = property.substring(0, property.indexOf(":"));
			if (propKey.trim().equalsIgnoreCase(prop)) {
				toReturn = property + ";";
				break;
			}
		}
		return toReturn;
	}

	private String getConfiguratedElementStyle(Double value, CellType cellType, JSONObject config, CrossTab crossTab) throws JSONException {
		JSONObject colorThrJ = null;
		boolean bgColorApplied = false;
		String dataStyle = "";
		String cellTypeValue = (cellType == null) ? "" : cellType.getValue();

		if (value != null && cellTypeValue.equalsIgnoreCase("data") && !config.isNull("colorThresholdOptions")) {
			// background management through threshold (optional)
			double dValue = value.doubleValue();
			colorThrJ = config.getJSONObject("colorThresholdOptions");
			String bgThrColor = getThresholdColor(dValue, colorThrJ);
			if (bgThrColor != null && !bgThrColor.equals("")) {
				dataStyle += "background-color:" + bgThrColor + ";";
				bgColorApplied = true;
			}
		}
		// cellType is null for rows and columns header
		if (cellTypeValue.equals("") || !config.isNull("style")) {
			JSONObject styleJ = (config.isNull("style")) ? new JSONObject() : config.getJSONObject("style");

			// style management:
			Iterator keys = styleJ.keys();
			while (keys.hasNext()) {
				String keyStyle = (String) keys.next();
				Object valueStyle = styleJ.get(keyStyle);
				if (valueStyle != null && !valueStyle.equals("")) {
					// normalize label properties
					switch (keyStyle) {
					case "textVerticalAlign":
						keyStyle = "vertical-align";
						break;
					case "textAlign":
						keyStyle = "text-align";
						break;
					case "fontWeight":
						keyStyle = "font-weight";
						break;
					case "fontSize":
						keyStyle = "font-size";
						break;
					case "fontStyle":
						keyStyle = "font-style";
						break;
					case "size":
						keyStyle = "width";
						break;
					case "background":
						if (bgColorApplied || cellTypeValue.equalsIgnoreCase("partialSum") || cellTypeValue.equalsIgnoreCase("totals"))
							continue;
					case "color":
						if (cellTypeValue.equalsIgnoreCase("partialSum") || cellTypeValue.equalsIgnoreCase("totals"))
							continue;
					}
					dataStyle += " " + keyStyle + ":" + String.valueOf(valueStyle) + ";";
				}

				// normalize padding for text-align
				if (keyStyle.equals("text-align")) {
					if (!valueStyle.equals("center"))
						dataStyle += " padding:0 5 0 0 !important;";
					else
						dataStyle += " padding:0 3 0 3 !important;";
				}
				// default font-style
				if (dataStyle.indexOf("font-style") < 0)
					dataStyle += DEFAULT_STYLE;
				// add contextual properties if width is defined
				if (keyStyle.equals("width")) {
					dataStyle += " overflow:hidden; text-overflow:ellipses;";
					dataStyle += " max-width:" + String.valueOf(valueStyle) + ";";
				}
			}
		}

		dataStyle += getTotalConfiguration(cellTypeValue, crossTab);

		return dataStyle;
	}

	private String getTotalConfiguration(String cellType, CrossTab crossTab) throws JSONException {
		String toReturn = "";

		if (!cellType.equalsIgnoreCase("partialSum") && !cellType.equalsIgnoreCase("totals"))
			return toReturn;

		JSONObject genericConfig = (!crossTab.getJSONCrossTab().isNull("config")) ? crossTab.getJSONCrossTab().getJSONObject("config") : null;
		JSONObject styleConfig = (genericConfig != null & !genericConfig.isNull("style")) ? genericConfig.getJSONObject("style") : null;

		if (null == styleConfig) {
			if (cellType.equalsIgnoreCase("partialSum"))
				return DEFAULT_BG_SUBTOTALS + DEFAULT_COLOR_TOTALS;
			if (cellType.equalsIgnoreCase("totals"))
				return DEFAULT_BG_TOTALS + DEFAULT_COLOR_TOTALS;
		}

		JSONObject config = new JSONObject();
		if (cellType.equalsIgnoreCase("partialSum")) {
			config = (!styleConfig.isNull("subTotals")) ? styleConfig.getJSONObject("subTotals") : null;
			if (null == config) {
				return DEFAULT_BG_SUBTOTALS + DEFAULT_COLOR_TOTALS;
			}
		}

		if (cellType.equalsIgnoreCase("totals")) {
			config = (!styleConfig.isNull("totals")) ? styleConfig.getJSONObject("totals") : null;
			if (null == config) {
				return DEFAULT_BG_TOTALS + DEFAULT_COLOR_TOTALS;
			}
		}

		if (null != config) {
			Iterator keys = config.keys();
			while (keys.hasNext()) {
				String keyStyle = (String) keys.next();
				Object valueStyle = config.get(keyStyle);
				toReturn += " " + keyStyle + ":" + String.valueOf(valueStyle) + ";";
			}
		}
		return toReturn;
	}

	private SourceBean getIconSB(double value, JSONObject condition) throws JSONException, SourceBeanException {
		SourceBean toReturn = null;
		double condValue = Double.parseDouble(condition.getString("value"));
		String condType = condition.getString("condition");
		boolean isConditionVerified = false;

		switch (condType) {
		case "<":
			if (value < condValue)
				isConditionVerified = true;
			break;
		case ">":
			if (value > condValue)
				isConditionVerified = true;
			break;
		case "=":
			if (value == condValue)
				isConditionVerified = true;
			break;
		case ">=":
			if (value >= condValue)
				isConditionVerified = true;
			break;
		case "<=":
			if (value <= condValue)
				isConditionVerified = true;
			break;
		case "!=":
			if (value != condValue)
				isConditionVerified = true;
			break;
		case "none":
			break;
		default:
			toReturn = null;
		}

		if (isConditionVerified) {
			toReturn = new SourceBean(ICON_TAG);
			toReturn.setAttribute(CLASS_ATTRIBUTE, condition.getString("icon"));
			toReturn.setAttribute(STYLE_ATTRIBUTE, "color:" + condition.getString("iconColor"));
		}

		return toReturn;
	}

	private Double calculatePercent(double value, int i, int j, String percentOn, CrossTab crossTab) {
		String[][] entries = crossTab.getDataMatrix();
		int rowSumStartColumn, columnSumStartRow;
		List<MeasureInfo> measures = crossTab.getMeasures();
		int measuresNumber = measures.size();
		if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
			rowSumStartColumn = entries[0].length - 1;
			columnSumStartRow = entries.length - measuresNumber;
		} else {
			rowSumStartColumn = entries[0].length - measuresNumber;
			columnSumStartRow = entries.length - 1;
		}

		int offset;

		if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
			offset = i % measuresNumber;
		} else {
			offset = j % measuresNumber;
		}

		if (percentOn.equals("row")) {
			if (!crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
				return 100 * value / Double.parseDouble(entries[i][offset + rowSumStartColumn]);
			} else {
				return 100 * value / Double.parseDouble(entries[i][rowSumStartColumn]);
			}
		} else {
			if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
				return 100 * value / Double.parseDouble(entries[offset + columnSumStartRow][j]);
			} else {
				return 100 * value / Double.parseDouble(entries[columnSumStartRow][j]);
			}
		}
	}

	private SourceBean mergeHorizontally(SourceBean left, SourceBean right) throws SourceBeanException {

		SourceBean table = new SourceBean(TABLE_TAG);
		List leftRows = left.getAttributeAsList(ROW_TAG);
		List rightRows = right.getAttributeAsList(ROW_TAG);
		if (leftRows.size() == 0) {
			// no categories on rows: forces fake ones to continue
			for (int i = 0; i < rightRows.size(); i++) {
				leftRows.add(new SourceBean(ROW_TAG));
			}
		}
		if (leftRows.size() != rightRows.size()) {
			throw new SpagoBIEngineRuntimeException("Cannot merge horizontally 2 tables with a different number of rows");
		}
		for (int i = 0; i < leftRows.size(); i++) {
			SourceBean aLeftRow = (SourceBean) leftRows.get(i);
			SourceBean aRightRow = (SourceBean) rightRows.get(i);
			SourceBean merge = new SourceBean(ROW_TAG);
			List aLeftRowColumns = aLeftRow.getAttributeAsList(COLUMN_TAG);
			for (int j = 0; j < aLeftRowColumns.size(); j++) {
				SourceBean aColumn = (SourceBean) aLeftRowColumns.get(j);
				merge.setAttribute(aColumn);
			}
			List aRightRowColumns = aRightRow.getAttributeAsList(COLUMN_TAG);
			for (int j = 0; j < aRightRowColumns.size(); j++) {
				SourceBean aColumn = (SourceBean) aRightRowColumns.get(j);
				merge.setAttribute(aColumn);
			}
			table.setAttribute(merge);
		}

		return table;
	}

	private SourceBean mergeVertically(SourceBean top, SourceBean bottom) throws SourceBeanException {
		SourceBean table = new SourceBean(TABLE_TAG);
		List topRows = top.getAttributeAsList(ROW_TAG);
		List bottomRows = bottom.getAttributeAsList(ROW_TAG);
		if (topRows == null) {
			topRows = new ArrayList();
		}
		if (bottomRows == null) {
			bottomRows = new ArrayList();
		}
		topRows.addAll(topRows.size(), bottomRows);
		for (int i = 0; i < topRows.size(); i++) {
			SourceBean aRow = (SourceBean) topRows.get(i);
			table.setAttribute(aRow);
		}

		return table;
	}

	private SourceBean addSortArrow(SourceBean aRow, String alias, String parentStyle, String divStyle, Integer direction, boolean isMeasureHeader)
			throws SourceBeanException {

		SourceBean div1 = new SourceBean(COLUMN_DIV);
		if (divStyle != null && !divStyle.equals("")) {
			div1.setAttribute(STYLE_ATTRIBUTE, divStyle);
		}
		div1.setAttribute(TITLE_ATTRIBUTE, alias);

		SourceBean icon = new SourceBean("i");
		SourceBean text = new SourceBean("span");

		// Defining text...
		text.setCharacters(alias);
		if (isMeasureHeader) {
			text.setAttribute(CLASS_ATTRIBUTE, MEASURES_CLASS);
		} else {
			if (parentStyle != null && !parentStyle.equals(""))
				text.setAttribute(STYLE_ATTRIBUTE, parentStyle);
			else
				text.setAttribute(CLASS_ATTRIBUTE, HEADER_CLASS);
		}

		// Defining icon...
		if (direction != null) {
			if (direction > 0) {
				icon.setAttribute(CLASS_ATTRIBUTE, "fa fa-arrow-up");
			} else {
				icon.setAttribute(CLASS_ATTRIBUTE, "fa fa-arrow-down");
			}
			icon.setAttribute(new SourceBean("fake"));
		}
		div1.setAttribute(icon);
		div1.setAttribute(text);

		return div1;
	}

	private SourceBean serializeRowsHeaders(CrossTab crossTab) throws SourceBeanException, JSONException {
		List<Row> rows = crossTab.getCrosstabDefinition().getRows();
		SourceBean table = new SourceBean(TABLE_TAG);
		SourceBean aRow = new SourceBean(ROW_TAG);
		boolean addRow = false;
		boolean appliedStyle = false;
		String style = null;
		for (int i = 0; i < rows.size(); i++) {
			addRow = true;
			Row aRowDef = rows.get(i);
			SourceBean aColumn = new SourceBean(COLUMN_TAG);

			Integer direction = 1;
			if (rowsSortKeysMap != null && rowsSortKeysMap.get(i) != null) {
				direction = rowsSortKeysMap.get(i).getDirection();
			}

			// Set specific rows configuration layout: NOT for the header: they uses general style
			JSONObject rowsConfig = rows.get(i).getConfig();
			String widthStyle = getConfiguratedElementStyle(null, null, rowsConfig, crossTab, "width");
			if (widthStyle.indexOf("%") >= 0)
				widthStyle = ""; // set width only with pixel values (for div)
			if (!rowsConfig.isNull("showHeader") && !rowsConfig.getBoolean("showHeader")) {
				// ADD AN EMPTY TD IF THERE IS A HEADER FOR THE MEASURE with the level class
				if (crossTab.getCrosstabDefinition().getMeasures().size() > 1)
					aColumn.setAttribute(CLASS_ATTRIBUTE, HEADER_CLASS);
				aRow.setAttribute(aColumn);
				continue; // skips header if not required
			}
			// aColumn.setAttribute(CLASS_ATTRIBUTE, LEVEL_CLASS);
			aColumn.setAttribute(CLASS_ATTRIBUTE, HEADER_CLASS);

			aColumn.setAttribute(NG_CLICK_ATTRIBUTE, "orderPivotTable('" + i + "','0'," + myGlobalId + ")");
			aColumn.setAttribute(addSortArrow(aRow, aRowDef.getAlias(), style, widthStyle, direction, false));
			aRow.setAttribute(aColumn);
		}
		if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
			addRow = true;
			SourceBean aColumn = new SourceBean(COLUMN_TAG);
			if (!appliedStyle)
				aColumn.setAttribute(CLASS_ATTRIBUTE, LEVEL_CLASS);
			else
				aColumn.setAttribute(STYLE_ATTRIBUTE, style);

			aRow.setAttribute(aColumn);
		}

		if (addRow)
			table.setAttribute(aRow);
		return table;
	}

	private SourceBean serializeTopLeftCorner(CrossTab crossTab) throws SourceBeanException, JSONException {

		int columnHeadersVerticalDepth = crossTab.getColumnsRoot().getDistanceFromLeaves();
		int rowHeadersHorizontalDepth = crossTab.getRowsRoot().getDistanceFromLeaves();
		// check the columns header visibility on columns (and uses rows count to manage colspan property)
		List<Column> columns = crossTab.getCrosstabDefinition().getColumns();
		List<Row> rows = crossTab.getCrosstabDefinition().getRows();

		for (int c = 0; c < columns.size(); c++) {
			JSONObject colConf = columns.get(c).getConfig();
			if (!colConf.isNull("showHeader") && !colConf.getBoolean("showHeader")) {
				columnHeadersVerticalDepth--;
			}
		}
		// for measures hide header only if there is only one
		boolean hideHeaderMeasure = false;

		List<Measure> measures = crossTab.getCrosstabDefinition().getMeasures();
		if (measures.size() == 1) {
			JSONObject colMeasure = measures.get(0).getConfig();
			if (!colMeasure.isNull("showHeader") && !colMeasure.getBoolean("showHeader")) {
				hideHeaderMeasure = true;
			} else {
				hideHeaderMeasure = false;
			}
		}
		if (!crossTab.isMeasureOnRow()) {
			if (hideHeaderMeasure)
				columnHeadersVerticalDepth--;
		}
		int numberOfEmptyRows = columnHeadersVerticalDepth - 1; // one row is
																// dedicated to
																// rows' headers

		SourceBean table = new SourceBean(TABLE_TAG);
		for (int i = 0; i < numberOfEmptyRows; i++) {
			SourceBean emptyRow = new SourceBean(ROW_TAG);
			SourceBean emptyColumn = new SourceBean(COLUMN_TAG);
			emptyColumn.setAttribute(CLASS_ATTRIBUTE, EMPTY_CLASS);
			if (!crossTab.isMeasureOnRow()) {
				if (rows.size() == 0)
					break;
				emptyColumn.setAttribute(COLSPAN_ATTRIBUTE, rows.size());
			} else {
				int rowSpan = rowHeadersHorizontalDepth;
				emptyColumn.setAttribute(COLSPAN_ATTRIBUTE, rowSpan);
			}
			emptyRow.setAttribute(emptyColumn);

			table.setAttribute(emptyRow);
		}
		return table;
	}
}
