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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

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
	private static String THEAD_TAG = "THEAD";
	private static String TBODY_TAG = "TBODY";
	private static String ROW_TAG = "TR";
	private static String COLUMN_TAG = "TD";
	private static String ICON_TAG = "I";
	private static String COLUMN_DIV = "DIV";
	private static String CLASS_ATTRIBUTE = "class";
	private static String STYLE_ATTRIBUTE = "style";
	private static String TITLE_ATTRIBUTE = "title";
	private static String ID_ATTRIBUTE = "id";
	private static String FIXED_COLUMN_ATTRIBUTE = "pivot";
	private static String ROWSPAN_ATTRIBUTE = "rowspan";
	private static String STARTING_ROWSPAN_ATTRIBUTE = "start-span";
	private static String COLSPAN_ATTRIBUTE = "colspan";
	private static final String NG_CLICK_ATTRIBUTE = "ng-click";
	private static final String EMPTY_FIELD_PLACEHOLDER = "empty_field";

	private static String MEMBER_CLASS = "member";
	private static String HIDDEN_CLASS = "hidden";
	private static String LEVEL_CLASS = "level";
	private static String EMPTY_CLASS = "empty";
	private static String HEADER_CLASS = "crosstab-header-text";
	private static String MEASURES_CLASS = "measures-header-text";

	private static String MINUS_BUTTON_ICON = "far fa-minus-square";
	private static String PLUS_BUTTON_ICON = "far fa-plus-square";

	private static String DEFAULT_BG_TOTALS = "background:rgba(59, 103, 140, 0.8);";
	private static String DEFAULT_BG_SUBTOTALS = "background:rgba(59, 103, 140, 0.45);";
	private static String DEFAULT_COLOR_TOTALS = "color:white;";
	private static String DEFAULT_STYLE = " font-style:normal!important;";
	private static String DEFAULT_HEADER_STYLE = " color:#3b678c; font-weight: 600;";
	private static String DEFAULT_CENTER_ALIGN = "text-align:center;";

	private Map<String, String> customStylesMap = new HashMap<String, String>();
	private List<Integer> rowsToBeHidden;

	private Locale locale = null;
	private final Integer myGlobalId;
	private final Map<Integer, NodeComparator> columnsSortKeysMap;
	private final Map<Integer, NodeComparator> rowsSortKeysMap;
	private final Map<Integer, NodeComparator> measuresSortKeysMap;

	Monitor serializeTimeMonitor = null;
	Monitor errorHitsMonitor = null;
	private final JSONObject variables;

	private static Logger logger = Logger.getLogger(CrossTabHTMLSerializer.class);

	public CrossTabHTMLSerializer(Locale locale, Integer myGlobalId, Map<Integer, NodeComparator> columnsSortKeysMap,
			Map<Integer, NodeComparator> rowsSortKeysMap, Map<Integer, NodeComparator> measuresSortKeysMap, JSONObject variables) {
		this.columnsSortKeysMap = columnsSortKeysMap;
		this.rowsSortKeysMap = rowsSortKeysMap;
		this.measuresSortKeysMap = measuresSortKeysMap;
		this.locale = locale;
		this.myGlobalId = myGlobalId;
		if (variables == null) {
			this.variables = new JSONObject();
		} else {
			this.variables = variables;
		}
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
		if (crossTab.isHideZeroRows())
			rowsToBeHidden = getNullRowsIndexList(crossTab);
		else
			rowsToBeHidden = new ArrayList<Integer>();

		Monitor htmlserializeTopLeftCornerMonitor = null;
		Monitor htmlserializeRowsHeaderssMonitor = null;
		Monitor htmlmergeVerticallyTopLeftMonitor = null;
		Monitor htmlserializeColumnsHeadersMonitor = null;
		Monitor htmlmergeHorizontallyHeadMonitor = null;
		Monitor htmlserializeRowsMembersMonitor = null;
		Monitor htmlserializeDataMonitor = null;
		Monitor htmlmergeHorizontallyBodyMonitor = null;
		Monitor htmlmergeVerticallyFinalCTMonitor = null;

		htmlserializeTopLeftCornerMonitor = MonitorFactory.start("CockpitEngine.CrossTabHTMLSerializer.htmlserializeTopLeftCornerMonitor");
		SourceBean emptyTopLeftCorner = this.serializeTopLeftCorner(crossTab);
		htmlserializeTopLeftCornerMonitor.stop();

		htmlserializeRowsHeaderssMonitor = MonitorFactory.start("CockpitEngine.CrossTabHTMLSerializer.htmlserializeRowsHeaderssMonitor");
		SourceBean rowsHeaders = this.serializeRowsHeaders(crossTab);
		htmlserializeRowsHeaderssMonitor.stop();

		htmlmergeVerticallyTopLeftMonitor = MonitorFactory.start("CockpitEngine.CrossTabHTMLSerializer.htmlmergeVerticallyTopLeftMonitor");
		SourceBean topLeftCorner = this.mergeVertically(emptyTopLeftCorner, rowsHeaders);
		htmlmergeVerticallyTopLeftMonitor.stop();

		htmlserializeColumnsHeadersMonitor = MonitorFactory.start("CockpitEngine.CrossTabHTMLSerializer.htmlserializeColumnsHeadersMonitor");
		SourceBean columnsHeaders = this.serializeColumnsHeaders(crossTab);
		htmlserializeColumnsHeadersMonitor.stop();

		htmlmergeHorizontallyHeadMonitor = MonitorFactory.start("CockpitEngine.CrossTabHTMLSerializer.htmlmergeHorizontallyHeadMonitor");
		SourceBean head = this.mergeHorizontally(topLeftCorner, columnsHeaders, crossTab);
		htmlmergeHorizontallyHeadMonitor.stop();

		htmlserializeRowsMembersMonitor = MonitorFactory.start("CockpitEngine.CrossTabHTMLSerializer.htmlserializeRowsMembersMonitor");
		SourceBean rowsMember = this.serializeRowsMembers(crossTab);
		htmlserializeRowsMembersMonitor.stop();

		htmlserializeDataMonitor = MonitorFactory.start("CockpitEngine.CrossTabHTMLSerializer.htmlserializeDataMonitor");
		SourceBean data = this.serializeData(crossTab);
		htmlserializeDataMonitor.stop();

		htmlmergeHorizontallyBodyMonitor = MonitorFactory.start("CockpitEngine.CrossTabHTMLSerializer.htmlmergeHorizontallyBodyMonitor");
		SourceBean body = this.mergeHorizontally(rowsMember, data, crossTab);
		htmlmergeHorizontallyBodyMonitor.stop();

		htmlmergeVerticallyFinalCTMonitor = MonitorFactory.start("CockpitEngine.CrossTabHTMLSerializer.htmlmergeVerticallyFinalCTMonitor");
		SourceBean crossTabSB = this.mergeHeadWithBody(head, body);
		htmlmergeVerticallyFinalCTMonitor.stop();

		toReturn.setAttribute(crossTabSB);

		return crossTabSB;
	}

	private String escapeAll(String text) {
		if (text == null) {
			return null;
		}

		return StringEscapeUtils.escapeHtml(text).replaceAll("'", "&apos;").replaceAll("\\\\", "&#92;").replaceAll("/", "&#47;");
	}

	private String getTextAndLevel(Node node, String text) {
		if (text == null || text.equals("")) {
			text = EMPTY_FIELD_PLACEHOLDER;
		}
		if (node.isSubTotal()) {
			return text + (node.getDistanceFromRoot() - 1);
		} else {
			return text + node.getDistanceFromRoot();
		}
	}

	private SourceBean serializeRowsMembers(CrossTab crossTab) throws SourceBeanException, JSONException {
		SourceBean table = new SourceBean(TABLE_TAG);
		List<Node> nodes = crossTab.getRowsRoot().getLeafs();
		int leaves = nodes.size();
		JSONObject config = crossTab.getCrosstabDefinition().getConfig();
		Boolean columnsTotals = config.optBoolean("calculatetotalsonrows");
		Boolean noSelectedColumn = crossTab.getCrosstabDefinition().getRows().isEmpty();
		String labelTotal = (!config.optString("rowtotalLabel").equals("")) ? config.optString("rowtotalLabel") : CrossTab.TOTAL;
		List<Node> nodesToBeIgnored = new ArrayList<Node>();

		List<SourceBean> rows = new ArrayList<SourceBean>();
		int levels = crossTab.getRowsRoot().getDistanceFromLeaves();
		if (crossTab.isMeasureOnRow()) {
			levels--;
		}

		// nodes to be ignored at last level (leaves)
		for (int i = 0; i < leaves; i++) {
			Node n = nodes.get(i);
			if (rowsToBeHidden.contains(i)) {
				nodesToBeIgnored.add(n);
			}
		}
		// nodes to be ignored at upper levels
		for (int i = levels - 2; i >= 0; i--) {
			List<Node> levelNodes = crossTab.getRowsRoot().getLevel(i + 1);
			for (int j = 0; j < levelNodes.size(); j++) {
				Node node = levelNodes.get(j);
				List<Node> children = new ArrayList<Node>(node.getLeafs());
				if (nodesToBeIgnored.containsAll(children)) {
					nodesToBeIgnored.add(node);
				}
			}
		}

		// initialize all rows (without columns)
		for (int i = 0; i < leaves; i++) {
			if (rowsToBeHidden.contains(i)) {
				continue;
			}
			SourceBean aRow = new SourceBean(ROW_TAG);

			Node node = nodes.get(i);
			boolean isSubtotal = node.getValue().equals(CrossTab.SUBTOTAL);
			if (crossTab.isExpandCollapseRows()) {
				Map<String, String> hierarchicalAttributes = getHierarchicalAttributes(crossTab, node, isSubtotal);
				for (Map.Entry<String, String> entry : hierarchicalAttributes.entrySet()) {
					aRow.setAttribute(entry.getKey(), entry.getValue());
				}
			}

			if (columnsTotals && noSelectedColumn) {
				SourceBean aColumn = new SourceBean(COLUMN_TAG);
				aRow.setAttribute(aColumn);
			}
			table.setAttribute(aRow);
			rows.add(aRow);
		}

		// just if there is total on column BUT no columns are required add a row for the total
		if (columnsTotals && noSelectedColumn) {
			SourceBean aRow = new SourceBean(ROW_TAG);
			SourceBean aColumn = new SourceBean(COLUMN_TAG);
			aColumn.setCharacters(labelTotal);
			aColumn.setAttribute(CLASS_ATTRIBUTE, "totals");
			aRow.setAttribute(aColumn);
			table.setAttribute(aRow);
			rows.add(aRow);
			return table;
		}

		boolean addedLabelTotal = false;
		for (int i = 0; i < levels; i++) {
			List<Node> allLevelNodes = crossTab.getRowsRoot().getLevel(i + 1);
			List<Node> filteredLevelNodes = filterNodes(allLevelNodes, nodesToBeIgnored);
			int counter = 0;
			for (int j = 0; j < filteredLevelNodes.size(); j++) {
				SourceBean aRow = rows.get(counter);
				Node aNode = filteredLevelNodes.get(j);
				if (isNodeToBeIgnored(aNode, nodesToBeIgnored)) {
					continue;
				}
				SourceBean aColumn = new SourceBean(COLUMN_TAG);

				String text = null;
				if (crossTab.getCrosstabDefinition().isMeasuresOnRows() && i + 1 == levels) {
					String measureAlias = aNode.getDescription();
					text = MeasureScaleFactorOption.getScaledName(measureAlias, crossTab.getMeasureScaleFactor(measureAlias), this.locale);
				} else {
					text = aNode.getDescription();
					if (text.equalsIgnoreCase(labelTotal)) {
						if (addedLabelTotal) {
							// if label total was already added or just total is the node (no column) : doesn't show label
							text = "";
						} else
							addedLabelTotal = true;
					}
				}
				// Get specific columns configuration (format, bgcolor, icon visualization,..)
				String style;
				boolean appliedStyle = false;
				List<Row> rowsDef = crossTab.getCrosstabDefinition().getRows();
				if (rowsDef != null && rowsDef.size() > 0) {
					Row row = rowsDef.get(i);

					JSONObject rowConfig = row.getConfig();
					style = customStylesMap.get(rowConfig.get("id"));
					if (style == null || style.equals("")) {
						// loading and caching style
						style = getConfiguratedElementStyle(null, null, rowConfig, crossTab);
						customStylesMap.put(rowConfig.getString("id"), style);
					}

					if (!style.equals(DEFAULT_STYLE) && !style.equals(DEFAULT_HEADER_STYLE)) {
						if (!aNode.isTotal() && !aNode.isSubTotal()) {
							aColumn.setAttribute(STYLE_ATTRIBUTE, style);
							appliedStyle = true;
						} else {
							// get only the alignment from the detail configuration cells
							String totStyle = getConfiguratedElementStyle(null, null, rowConfig, crossTab, "text-align");
							aColumn.setAttribute(STYLE_ATTRIBUTE, totStyle);
						}
					}
				}
				if (!appliedStyle)
					aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
				else
					aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS + "NoStandardStyle");

				if (!aNode.isTotal() && !aNode.isSubTotal() && crossTab.getCrosstabDefinition().getRows().size() > 0)
					aColumn.setAttribute(NG_CLICK_ATTRIBUTE, "selectRow('" + crossTab.getCrosstabDefinition().getRows().get(i).getEntityId() + "','"
							+ StringEscapeUtils.escapeJavaScript(text) + "')");
				aColumn.setCharacters(text);

				List<Node> allChildren = new ArrayList<Node>(aNode.getLeafs());
				List<Node> filteredChildren = filterNodes(allChildren, nodesToBeIgnored);
				int rowSpan = filteredChildren.size();

				if (rowSpan > 1) {
					aColumn.setAttribute(ROWSPAN_ATTRIBUTE, rowSpan);
					aColumn.setAttribute(STARTING_ROWSPAN_ATTRIBUTE, rowSpan);
					if (i < levels - 1 && crossTab.isExpandCollapseRows()) { // attach collapse button
						SourceBean aButton = new SourceBean(ICON_TAG);
						aButton.setAttribute(CLASS_ATTRIBUTE, MINUS_BUTTON_ICON);
						JSONObject hierarchyJson = new JSONObject(getHierarchicalAttributes(crossTab, aNode.getParentNode(), false));
						String columnName = crossTab.getColumnNameFromAlias(crossTab.getCrosstabDefinition().getRows().get(i).getAlias());
						String collapseLabel = getTextAndLevel(aNode, text);
						aButton.setAttribute(NG_CLICK_ATTRIBUTE,
								"collapse($event,'" + escapeAll(columnName) + "','" + escapeAll(collapseLabel) + "'," + hierarchyJson + ")");
						aColumn.setAttribute(aButton);
					}
				}
				aColumn.setAttribute(TITLE_ATTRIBUTE, escapeAll(text));
				String idLabel = getTextAndLevel(aNode, aNode.getValue());
				aColumn.setAttribute(ID_ATTRIBUTE, escapeAll(idLabel));
				if (crossTab.isFixedColumn())
					aColumn.setAttribute(FIXED_COLUMN_ATTRIBUTE, aNode.getDistanceFromRoot() - 1);

				boolean isSubtotal = aNode.getValue().equals(CrossTab.SUBTOTAL);
				if (isSubtotal && crossTab.isExpandCollapseRows()) { // create subtotal hidden row (used when collapsing aggregations)
					SourceBean subtotalHiddenColumn = new SourceBean(COLUMN_TAG);
					subtotalHiddenColumn.setAttribute(CLASS_ATTRIBUTE, HIDDEN_CLASS);
					style = customStylesMap.get(crossTab.getColumnAliasFromName(aNode.getParentNode().getColumnName()));
					if (style == null) {
						Row row = rowsDef.get(i);
						JSONObject rowConfig = row.getConfig();
						style = customStylesMap.get(rowConfig.get("id"));
					}
					subtotalHiddenColumn.setAttribute(STYLE_ATTRIBUTE, style);
					text = aNode.getParentNode().getValue();
					subtotalHiddenColumn.setAttribute(TITLE_ATTRIBUTE, escapeAll(text));
					String hiddenIdLabel = getTextAndLevel(aNode, text);
					subtotalHiddenColumn.setAttribute(ID_ATTRIBUTE, escapeAll(hiddenIdLabel));
					String parentEntityId = crossTab.getColumnAliasFromName(aNode.getParentNode().getColumnName());
					subtotalHiddenColumn.setAttribute(NG_CLICK_ATTRIBUTE,
							"selectRow('" + parentEntityId + "','" + StringEscapeUtils.escapeJavaScript(text) + "')");
					subtotalHiddenColumn.setCharacters(text);
					// attach expand button
					SourceBean aButton = new SourceBean(ICON_TAG);
					aButton.setAttribute(CLASS_ATTRIBUTE, PLUS_BUTTON_ICON);
					JSONObject hierarchyJson = new JSONObject(getHierarchicalAttributes(crossTab, aNode.getParentNode().getParentNode(), false));
					String expandLabel = getTextAndLevel(aNode, text);
					aButton.setAttribute(NG_CLICK_ATTRIBUTE,
							"expand($event,'" + escapeAll(aNode.getParentNode().getColumnName()) + "','" + escapeAll(expandLabel) + "'," + hierarchyJson + ")");
					if (crossTab.isFixedColumn()) {
						subtotalHiddenColumn.setAttribute(FIXED_COLUMN_ATTRIBUTE, aNode.getDistanceFromRoot() - 2);
					}
					subtotalHiddenColumn.setAttribute(aButton);
					aRow.setAttribute(subtotalHiddenColumn);
				}

				aRow.setAttribute(aColumn);
				counter = counter + rowSpan;
			}
		}

		return table;
	}

	private boolean isNodeToBeIgnored(Node node, List<Node> nodesToBeIgnored) {
		for (Node toBeIgnored : nodesToBeIgnored) {
			if (node == toBeIgnored) {
				return true;
			}
		}
		return false;
	}

	private List<Node> filterNodes(List<Node> allNodes, List<Node> nodesToBeRemoved) {
		List<Node> filteredNodes = new ArrayList<>();
		boolean remove;
		for (Node n1 : allNodes) {
			remove = false;
			for (Node n2 : nodesToBeRemoved) {
				if (n1 == n2) {
					remove = true;
					break;
				}
			}
			if (!remove) {
				filteredNodes.add(n1);
			}
		}
		return filteredNodes;
	}

	private Map<String, String> getHierarchicalAttributes(CrossTab crossTab, Node aNode, boolean isSubtotal) {
		Map<String, String> hierarchicalAttributes = new HashMap<String, String>();
		Node curNode = aNode;
		if (isSubtotal) {
			Map<String, String> toReturn = new HashMap<String, String>();
			toReturn.put(CrossTab.SUBTOTAL, "true");
			Node parentNode = curNode.getParentNode();
			boolean isParentSubtotal = parentNode.getValue().equals(CrossTab.SUBTOTAL);
			toReturn.putAll(getHierarchicalAttributes(crossTab, parentNode, isParentSubtotal));
			return toReturn;
		} else {
			while (curNode.getColumnName() != null && !curNode.getColumnName().equals("null")) {
				String attribute = curNode.getColumnName();
				String value = getTextAndLevel(curNode, curNode.getValue());
				hierarchicalAttributes.put(escapeAll(attribute), escapeAll(value));
				curNode = curNode.getParentNode();
			}
		}
		return hierarchicalAttributes;
	}

	private SourceBean serializeColumnsHeaders(CrossTab crossTab) throws SourceBeanException, JSONException {
		SourceBean table = new SourceBean(TABLE_TAG);
		String parentStyle = null;
		List<String> categoriesValues = null;
		JSONObject crossConfig = crossTab.getCrosstabDefinition().getConfig();
		String labelTotal = (!crossConfig.optString("columntotalLabel").equals("")) ? crossConfig.optString("columntotalLabel") : CrossTab.TOTAL;
		String labelSubTotal = (!crossConfig.optString("columnsubtotalLabel").equals("")) ? crossConfig.optString("columnsubtotalLabel") : CrossTab.SUBTOTAL;
		int levels = crossTab.getColumnsRoot().getDistanceFromLeaves();

		if (levels == 0) {
			// nothing on columns
			SourceBean aRow = new SourceBean(ROW_TAG);
			SourceBean aColumn = new SourceBean(COLUMN_TAG);
			aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
			aColumn.setCharacters(EngineMessageBundle.getMessage("sbi.crosstab.runtime.headers.data", this.getLocale()));
			aRow.setAttribute(aColumn);
			table.setAttribute(aRow);
			return table;
		}

		int measureNumber = crossTab.getCrosstabDefinition().getMeasures().size();
		List<String> lastLevelValues = new ArrayList<String>();
		int colSpanSubTot = 0;
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
				String textVariable = null;
				String style = "";

				if (crossTab.getCrosstabDefinition().isMeasuresOnColumns() && i + 1 == levels) {
					String measureAlias = aNode.getDescription();
					text = MeasureScaleFactorOption.getScaledName(measureAlias, crossTab.getMeasureScaleFactor(measureAlias), this.locale);
					// check header visibility for measures
					showHeader = isMeasureHeaderVisible(crossTab);
					if (this.variables != null) {
						List<Measure> measures = crossTab.getCrosstabDefinition().getMeasures();
						for (int c = 0; c < measures.size(); c++) {
							Measure col = measures.get(c);
							if (col.getAlias().equals(text)) {
								textVariable = col.getVariable();
							}

						}
					}
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

							textVariable = col.getVariable();
							style = customStylesMap.get(columnConfig.get("id"));
							if (style == null || style.equals("")) {
								// loading and caching style
								style = getConfiguratedElementStyle(null, null, columnConfig, crossTab);
								customStylesMap.put(columnConfig.getString("id"), style);
							}

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
					int idxEl = i / 2; // just for columns headers divide the position of cell in couple (name + value)
					aColumn.setAttribute(NG_CLICK_ATTRIBUTE, "orderPivotTable('" + idxEl + "','1'," + myGlobalId + ")");

					Integer direction = 1;
					if (columnsSortKeysMap != null && columnsSortKeysMap.get(idxEl) != null) {
						direction = columnsSortKeysMap.get(idxEl).getDirection();
					}

					if (parentStyle != null)
						style = parentStyle;
					if (!text.equalsIgnoreCase(labelTotal) && !text.equalsIgnoreCase(labelSubTotal)) {
						aColumn.setAttribute(addSortArrow(aRow, text, style, null, direction, false, textVariable));
					}
					aColumn.setAttribute(STYLE_ATTRIBUTE, style);
					aColumn.setAttribute(CLASS_ATTRIBUTE, HEADER_CLASS);

				} else {
					boolean parentIsLevel = !((i) % 2 == 0 || (i) == levels);
					if (parentIsLevel) {
						if (!text.equalsIgnoreCase(labelTotal) && !text.equalsIgnoreCase(labelSubTotal)) {
							Column columnObj = ((Column) getCategoryConfByLabel(crossTab, crossTab.getColumnsRoot().getLevel(i).get(0).getValue(), "columns"));
							String columnName = (columnObj != null) ? columnObj.getEntityId() : "";
							aColumn.setAttribute(NG_CLICK_ATTRIBUTE, "selectRow('" + columnName + "','" + StringEscapeUtils.escapeJavaScript(text) + "')");
						}
						if (crossTab.getCrosstabDefinition().isMeasuresOnColumns() && i + 2 == levels) {
							String completeText = CrossTab.PATH_SEPARATOR;
							Node tmpNode = aNode;
							int idx = 0;
							while (idx < (levels - 1) / 2) {
								Node parentNode = tmpNode.getParentNode();
								int maxParentIdx = ((levels - 1) / 2) - idx;
								boolean workingOnParent = false;
								while (maxParentIdx > 1) {
									if (parentNode != null && parentNode.getParentNode() != null) {
										parentNode = parentNode.getParentNode();
										workingOnParent = true;
									}
									maxParentIdx--;
								}
								if (parentNode != null && !parentNode.isRoot()) {
									Node grandparentNode = parentNode.getParentNode();
									if (workingOnParent && grandparentNode != null && !grandparentNode.isRoot())
										completeText += grandparentNode.getDescription() + CrossTab.PATH_SEPARATOR;
									completeText += parentNode.getDescription() + CrossTab.PATH_SEPARATOR;
								}
								idx++;
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
								if (StringUtils.isNotBlank(textVariable)) {
									if (this.variables.has(textVariable)) {
										divEl.setCharacters(this.variables.getString(textVariable));
									}
								} else {
									divEl.setCharacters(text);
								}
								divEl.setAttribute(TITLE_ATTRIBUTE, text);
								divEl.setAttribute(STYLE_ATTRIBUTE, measureStyle);
								aColumn.setAttribute(divEl);
							} else {
								if (StringUtils.isNotBlank(textVariable)) {
									if (this.variables.has(textVariable)) {
										aColumn.setCharacters(this.variables.getString(textVariable));
									}
								} else {
									aColumn.setCharacters(text);
								}
							}
						} else {
							if (StringUtils.isNotBlank(textVariable)) {
								if (this.variables.has(textVariable)) {
									aColumn.setCharacters(this.variables.getString(textVariable));
								}
							} else {
								aColumn.setCharacters(text);
							}
						}

						if (parentStyle != null) {
							// add default color for header
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
							if (measureParentValue.indexOf(labelTotal) < 0 && measureParentValue.indexOf(labelSubTotal) < 0) {
								if (measuresSortKeysMap != null && measuresSortKeysMap.get(j) != null) {
									direction = measuresSortKeysMap.get(j).getDirection();
								}
							}
						}
						if (levels == 1 || (!text.equalsIgnoreCase(labelTotal) && !text.equalsIgnoreCase(labelSubTotal))) {
							aColumn.setAttribute(addSortArrow(aRow, text, parentStyle, measureStyle, direction, true, textVariable));
							aColumn.setAttribute(NG_CLICK_ATTRIBUTE,
									"orderPivotTable('" + j + "','1'," + myGlobalId + ", '" + text + "' , '" + measureParentValue + "')");
						}
					}
				}

				int colSpan = aNode.getLeafsNumber();
				if (text.equalsIgnoreCase(labelSubTotal)) {
					colSpanSubTot = colSpan;
					if (i < levels - 2)
						aColumn.setCharacters("");
				}
				if (text.equalsIgnoreCase(labelTotal)) {
					if (colSpanSubTot > 0)
						colSpan = colSpanSubTot;
					else if (!crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
						colSpan = crossTab.getCrosstabDefinition().getMeasures().size();
					}
					if (i < levels - 2)
						aColumn.setCharacters("");
				}
				if (colSpan > 1) {
					aColumn.setAttribute(COLSPAN_ATTRIBUTE, colSpan);
				}
				aRow.setAttribute(aColumn);
			}
			if (showHeader)
				table.setAttribute(aRow);
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
				String width = getConfiguratedElementStyle(null, CellType.DATA, measureConfig, crossTab, "width");
				if (!width.equals("")) {
					if (width.indexOf("%") >= 0)
						width = ""; // set width only with pixel values (for div)
					String display = " overflow:hidden; text-overflow:ellipsis;";
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
		List<String> toReturn = new ArrayList<String>();

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

	private List<Integer> getNullRowsIndexList(CrossTab crossTab) {
		List<Integer> nullRows = new ArrayList<Integer>();
		String[][] data = crossTab.getDataMatrix();
		for (int i = 0; i < data.length; i++) {
			String[] values = data[i];
			if (isNullOrZeroRow(values)) {
				nullRows.add(i);
			}
		}
		return nullRows;
	}

	private boolean isNullOrZeroRow(String[] values) {
		for (int i = 0; i < values.length; i++) {
			String curVal = values[i];
			if (curVal != null && !curVal.equals("")) {
				Double doubleVal = Double.parseDouble(curVal);
				if (doubleVal != 0)
					return false;
			}
		}
		return true;
	}

	private SourceBean serializeData(CrossTab crossTab) throws SourceBeanException, JSONException {
		SourceBean table = new SourceBean(TABLE_TAG);
		String[][] data = crossTab.getDataMatrix();
		List<MeasureInfo> measuresInfo = crossTab.getMeasures();
		List<SourceBean> measureHeaders = new ArrayList<SourceBean>();
		List<String> columnsSpecification = crossTab.getColumnsSpecification();
		boolean isDataNoStandardStyle = false;

		Monitor internalserializeData1 = null;
		Monitor internalserializeData2 = null;
		Monitor internalserializeData3 = null;
		Monitor internalserializeData4 = null;
		Monitor internalserializeData5 = null;

		internalserializeData1 = MonitorFactory.start("CockpitEngine.serializeData.columnsSpecificationForSelect");
		if (columnsSpecification.size() > 0) {
			// defines columns specification with totals and subtotals if required (for action #7 selection function setting)
			if (crossTab.isMeasureOnRow()) {
				List<CellType> columnsTypes = crossTab.getCelltypeOfColumns();
				List<String> columnsSpecificationWithTotals = new ArrayList<String>();
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
				List<String> columnsSpecificationWithTotals = new ArrayList<String>();
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
		internalserializeData1.stop();

		if (crossTab.isMeasureOnRow()) {
			// adds the measure label
			for (MeasureInfo measureInfo : crossTab.getMeasures()) {
				SourceBean aMeasureHeader = new SourceBean(COLUMN_TAG);
				// Get specific columns configuration (format, bgcolor, icon visualization,..)
				aMeasureHeader.setAttribute(CLASS_ATTRIBUTE, MEASURES_CLASS);
				aMeasureHeader.setCharacters(measureInfo.getName());

				measureHeaders.add(aMeasureHeader);
			}
		}

		MeasureFormatter measureFormatter = new MeasureFormatter(crossTab);
		int measureHeaderSize = crossTab.getMeasures().size();

		// get measures of subtotals column
		List<Measure> allMeasures = crossTab.getCrosstabDefinition().getMeasures();
		List<Measure> subtotalMeasures = getSubtotalsMeasures(allMeasures);
		int nPartialSumRow = 0;
		int nPartialLevels = 0;
		for (int i = 0; i < data.length; i++) {
			SourceBean aRow = new SourceBean(ROW_TAG);

			if (crossTab.isMeasureOnRow() && measureHeaders.size() > 0) {
				aRow.setAttribute(measureHeaders.get(i % measureHeaderSize));
			}

			if (rowsToBeHidden.contains(i))
				continue;

			String[] values = data[i];
			int totalsCounter = 0;
			int pos;
			for (int j = 0; j < values.length; j++) {
				String text = values[j];
				SourceBean aColumn = new SourceBean(COLUMN_TAG);
				CellType cellType = crossTab.getCellType(i, j);
				String cellTypeValue = cellType.getValue();
				if (j == 0 && cellTypeValue.equalsIgnoreCase("partialsum")) {
					nPartialSumRow++;
					nPartialLevels++;
				}

				JSONObject measureConfig = new JSONObject();
				try {
					internalserializeData2 = MonitorFactory.start("CockpitEngine.serializeData.getMeasureConfigAndThreshold");
					// 1. Get specific measure configuration (format, bgcolor, icon visualization,..)
					if (crossTab.isMeasureOnRow()) {
						pos = i % measuresInfo.size();
						measureConfig = allMeasures.get(pos).getConfig();
					} else {
						pos = crossTab.getOffsetInColumnSubtree(j) % measuresInfo.size();
						if (crossTab.isCellFromSubtotalsColumn(j)) {
							measureConfig = subtotalMeasures.get(pos).getConfig();
						} else if (crossTab.isCellFromTotalsColumn(j)) {
							measureConfig = subtotalMeasures.get(totalsCounter).getConfig();
							totalsCounter++;
						} else {
							measureConfig = allMeasures.get(pos).getConfig();
						}
					}

					String visType = (measureConfig.isNull("visType")) ? "Text" : measureConfig.getString("visType");
					SourceBean iconSB = null;

					Double value = (!text.equals("")) ? Double.parseDouble(text) : null;
					JSONObject threshold = getThreshold(value, measureConfig.optJSONArray("ranges"));

					if (cellType.getValue().equalsIgnoreCase("data") && threshold.has("icon")) {
						iconSB = new SourceBean(ICON_TAG);
						iconSB.setAttribute(CLASS_ATTRIBUTE, threshold.getString("icon"));
						iconSB.setAttribute(STYLE_ATTRIBUTE, "color:" + threshold.optString("color", "black"));
					}
					internalserializeData2.stop();

					String classType = cellType.getValue();

					internalserializeData3 = MonitorFactory.start("CockpitEngine.serializeData.setStyle");
					// 2. style and alignment management
					if (cellType.getValue().equalsIgnoreCase("data")) {
						String dataStyle = customStylesMap.get(measureConfig.get("id"));
						if (dataStyle == null || dataStyle.equals("")) {
							dataStyle = getConfiguratedElementStyle(value, cellType, measureConfig, crossTab);
							// load and caching data style
							customStylesMap.put((String) measureConfig.get("id"), dataStyle);
						}

						if (value != null && cellTypeValue.equalsIgnoreCase("data") && !measureConfig.isNull("ranges")) {
							// background management through threshold (optional)
							String backgroundColor = threshold.optString("background-color");
							if (backgroundColor != null && !backgroundColor.isEmpty())
								dataStyle += "background-color:" + backgroundColor + ";";
							String textColor = threshold.optString("color");
							if (textColor != null && !textColor.isEmpty())
								dataStyle += "color:" + textColor + ";";
						}
						// if (!dataStyle.equals(DEFAULT_STYLE + DEFAULT_HEADER_STYLE + DEFAULT_CENTER_ALIGN) ) {
						if (!isStandardStyle(dataStyle)) {
							aColumn.setAttribute(STYLE_ATTRIBUTE, dataStyle);
							classType += "NoStandardStyle";
							isDataNoStandardStyle = true;
							aColumn.setAttribute(CLASS_ATTRIBUTE, "dataNoStandardStyle");
						} else {
							isDataNoStandardStyle = false;
							aColumn.setAttribute(CLASS_ATTRIBUTE, "data");
						}

					} else {
						String totalStyle = getConfiguratedElementStyle(null, cellType, measureConfig, crossTab, "text-align");
						totalStyle += getConfiguratedElementStyle(null, cellType, measureConfig, crossTab, "padding");
						totalStyle += getConfiguratedElementStyle(null, cellType, measureConfig, crossTab, "width");
						aColumn.setAttribute(STYLE_ATTRIBUTE, totalStyle);
						aColumn.setAttribute(CLASS_ATTRIBUTE, classType);
					}

					if (value == null) {
						aRow.setAttribute(aColumn);
						continue;
					}
					internalserializeData3.stop();

					// 3. define value (number) and its final visualization
					internalserializeData4 = MonitorFactory.start("CockpitEngine.serializeData.formatNumberAndPS");
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
					internalserializeData4.stop();
					// add icon html if required
					if (iconSB != null) {
						actualText += " ";
						aColumn.setAttribute(iconSB);
					}

					// 6. set value
					if (measureConfig.has("excludeFromTotalAndSubtotal") && measureConfig.getBoolean("excludeFromTotalAndSubtotal")
							&& (cellTypeValue.equalsIgnoreCase("partialsum") || cellTypeValue.equalsIgnoreCase("totals")))
						aColumn.setCharacters("");
					else
						aColumn.setCharacters(actualText);
					aColumn.setAttribute(TITLE_ATTRIBUTE, actualText);

					// 7. set selection function with the parent references (on row and column)
					internalserializeData5 = MonitorFactory.start("CockpitEngine.serializeData.defineClickFunction");
					if (cellTypeValue.equalsIgnoreCase("data") || cellTypeValue.equalsIgnoreCase("partialsum") || cellTypeValue.equalsIgnoreCase("totals")) {
						String rowCord = "";
						String rowHeaders = "";
						String measureRef = "";
						if (crossTab.getRowsSpecification().size() > 0) {
							List<String> rowsDef = crossTab.getRowsHeaderIdList();
							for (int r = 0; r < rowsDef.size(); r++) {
								rowHeaders += CrossTab.PATH_SEPARATOR + rowsDef.get(r);
							}
							if (!crossTab.isMeasureOnRow()) {
								int posRow = i - nPartialSumRow;
								if (posRow < crossTab.getRowsSpecification().size()) {
									if (cellTypeValue.equalsIgnoreCase("data")) {
										nPartialLevels = 0; // reset partialLevels count for new row
									}
									rowCord = getRowCordContent(crossTab, nPartialLevels, Integer.valueOf(posRow));
								}

							} else {
								int idx = i - nPartialSumRow;
								int posRow = (measuresInfo.size() == 1) ? idx : idx / measuresInfo.size();
								if (posRow < crossTab.getRowsSpecification().size()) {
									rowCord = crossTab.getRowsSpecification().get(posRow);
								}

							}

							// reset rowCord for totals and subtotals
							if (rowCord.equalsIgnoreCase("TOTALS") || rowCord.equalsIgnoreCase("PARTIALSUM") || rowCord.equals("")) {
								rowCord = "";
								rowHeaders = "";
							}

						}
						String columnCord = "";
						String columnsHeaders = "";

						if (columnsSpecification.size() > 0) {
							List<String> columnsDef = crossTab.getColumnsHeaderIdList();
							for (int c = 0; c < columnsDef.size(); c++) {
								columnsHeaders += CrossTab.PATH_SEPARATOR + columnsDef.get(c);
							}
							if (!crossTab.isMeasureOnRow()) {
								int posColumn = (measuresInfo.size() == 1) ? j : j / measuresInfo.size();
								columnCord = columnsSpecification.get(posColumn);
							} else {
								int posColumn = j;
								columnCord = columnsSpecification.get(posColumn);
							}

							// reset columnCord for totals and subtotals
							if (columnCord.equalsIgnoreCase("TOTALS") || columnCord.equalsIgnoreCase("PARTIALSUM") || columnCord.equals("")) {
								columnCord = "";
								columnsHeaders = "";
							}

						}

						// add the reference to the measure that's clicked (_S_<id>_S_<label>)
						measureRef = CrossTab.PATH_SEPARATOR + measuresInfo.get(pos).getId() + CrossTab.PATH_SEPARATOR + measuresInfo.get(pos).getName();

						aColumn.setAttribute(NG_CLICK_ATTRIBUTE,
								"selectMeasure('" + StringEscapeUtils.escapeJavaScript(rowHeaders) + "','" + StringEscapeUtils.escapeJavaScript(rowCord) + "','"
										+ StringEscapeUtils.escapeJavaScript(columnsHeaders) + "','" + StringEscapeUtils.escapeJavaScript(columnCord) + "','"
										+ StringEscapeUtils.escapeJavaScript(measureRef) + "')");
						internalserializeData5.stop();
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

	boolean isStandardStyle(String style) {
		if (style.equals(DEFAULT_STYLE))
			return true;
		else {
			// we consider the style to be standard even if the measure format is set
			String remainder = style.replace(DEFAULT_STYLE, "");
			if (remainder.equals("format:#.###,##") || remainder.equals("format:#,###.##"))
				return true;
		}
		return false;
	}

	List<Measure> getSubtotalsMeasures(List<Measure> allMeasures) throws JSONException {
		List<Measure> toReturn = new ArrayList<Measure>();
		for (int k = 0; k < allMeasures.size(); k++) {
			if (!allMeasures.get(k).getConfig().has("excludeFromTotalAndSubtotal")
					|| !allMeasures.get(k).getConfig().getBoolean("excludeFromTotalAndSubtotal")) {
				toReturn.add(allMeasures.get(k));
			}
		}
		return toReturn;
	}

	/**
	 * getRowCordContent: check the content of rowSpecification to understand which values are available
	 *
	 * @param crossTab
	 * @param levels
	 * @param row
	 * @return the string with row specification available for the row
	 */
	private String getRowCordContent(CrossTab crossTab, Integer levels, Integer row) {
		String toReturn = "";
		String rowCord = crossTab.getRowsSpecification().get(row);

		if (levels < 1)
			return rowCord; // returns complete row specification for data's row

		String[] rowCordElems = rowCord.split(CrossTab.PATH_SEPARATOR);
		for (int n = 1; n < rowCordElems.length - levels; n++) {
			toReturn += CrossTab.PATH_SEPARATOR + rowCordElems[n];
		}

		if (toReturn.equals(""))
			toReturn = rowCord;

		return toReturn;
	}

	private JSONObject getThreshold(Double value, JSONArray colorThrJ) throws JSONException {
		JSONObject toReturn = new JSONObject();
		if (value == null || colorThrJ == null)
			return toReturn;
		boolean isConditionVerified = false;

		for (int c = 0; c < colorThrJ.length(); c++) {
			JSONObject thrCond = (JSONObject) colorThrJ.get(c);
			if (!thrCond.has("value")) {
				continue;
			}
			String condition = thrCond.getString("operator");
			Double conditionValue = thrCond.getDouble("value");
			isConditionVerified = verifyThresholdCondition(condition, conditionValue, value);

			if (isConditionVerified)
				return thrCond;
		}
		return toReturn;
	}

	private boolean verifyThresholdCondition(String condition, double value, double valueToTest) {
		boolean isConditionVerified = false;

		switch (condition) {
		case "=":
			if (valueToTest == value)
				isConditionVerified = true;
			break;
		case "==":
			if (valueToTest == value)
				isConditionVerified = true;
			break;
		case "<":
			if (valueToTest < value)
				isConditionVerified = true;
			break;
		case ">":
			if (valueToTest > value)
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

		// searching style within style map, if it's not found gets it directly from configuration object
		dataStyle = customStylesMap.get(config.get("id"));
		if (dataStyle == null || dataStyle.equals("")) {
			dataStyle = getConfiguratedElementStyle(value, cellType, config, crossTab);
			customStylesMap.put(config.getString("id"), dataStyle);
		}

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
		String dataStyle = "";
		String cellTypeValue = (cellType == null) ? "" : cellType.getValue();

		// cellType is null for rows and columns header
		if (cellTypeValue.equals("") || !config.isNull("style")) {
			JSONObject styleJ = (config.isNull("style")) ? new JSONObject() : config.getJSONObject("style");

			// style management:
			Iterator<String> keys = styleJ.keys();
			while (keys.hasNext()) {
				String keyStyle = keys.next();
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
						if (cellTypeValue.equalsIgnoreCase("partialSum") || cellTypeValue.equalsIgnoreCase("totals"))
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
				// default font color for headers
				if (cellTypeValue.equals("") && dataStyle.indexOf("color") < 0)
					dataStyle += DEFAULT_HEADER_STYLE;
				// default text-align
				if (cellTypeValue.equals("") && dataStyle.indexOf("text-align") < 0)
					dataStyle += DEFAULT_CENTER_ALIGN;
				// add contextual properties if width is defined
				if (keyStyle.equals("width")) {
					dataStyle += " overflow:hidden; text-overflow:ellipsis;";
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
			Iterator<String> keys = config.keys();
			while (keys.hasNext()) {
				String keyStyle = keys.next();
				Object valueStyle = config.get(keyStyle);
				toReturn += " " + keyStyle + ":" + String.valueOf(valueStyle) + ";";
			}
		}
		return toReturn;
	}

//	private SourceBean getIconSB(double value, JSONObject condition) throws JSONException, SourceBeanException {
//		SourceBean toReturn = null;
//		double condValue = Double.parseDouble(condition.getString("value"));
//		String condType = condition.getString("condition");
//		boolean isConditionVerified = false;
//
//		switch (condType) {
//		case "<":
//			if (value < condValue)
//				isConditionVerified = true;
//			break;
//		case ">":
//			if (value > condValue)
//				isConditionVerified = true;
//			break;
//		case "=":
//			if (value == condValue)
//				isConditionVerified = true;
//			break;
//		case ">=":
//			if (value >= condValue)
//				isConditionVerified = true;
//			break;
//		case "<=":
//			if (value <= condValue)
//				isConditionVerified = true;
//			break;
//		case "!=":
//			if (value != condValue)
//				isConditionVerified = true;
//			break;
//		case "none":
//			break;
//		default:
//			toReturn = null;
//		}
//
//		if (isConditionVerified) {
//			toReturn = new SourceBean(ICON_TAG);
//			toReturn.setAttribute(CLASS_ATTRIBUTE, condition.getString("icon"));
//			toReturn.setAttribute(STYLE_ATTRIBUTE, "color:" + condition.getString("color"));
//		}
//
//		return toReturn;
//	}

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
			if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
				return 100 * value / getRowTotal(entries, crossTab, rowSumStartColumn, i);
			} else {
				return 100 * value / getRowTotal(entries, crossTab, offset + rowSumStartColumn, i);
			}
		} else {
			if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
				return 100 * value / getColumnTotal(entries, crossTab, offset + columnSumStartRow, j);
			} else {
				return 100 * value / getColumnTotal(entries, crossTab, columnSumStartRow, j);
			}
		}
	}

	private Double getColumnTotal(String[][] matrix, CrossTab crossTab, int totalsIdx, int colIdx) {
		if (crossTab.isCalculateTotalsOnColumns()) { // if i have the totals row i can read the total value from there
			return Double.parseDouble(matrix[totalsIdx][colIdx]);
		} else { // otherwise i need to compute it
			Double total = 0.0;
			for (int i = 0; i < matrix.length; i++) {
				Double value;
				try {
					value = Double.parseDouble(matrix[i][colIdx]);
				} catch (NumberFormatException e) {
					// if conversion fails it is an empty string
					value = 0.0;
				}
				total += value;
			}
			return total;
		}
	}

	private Double getRowTotal(String[][] matrix, CrossTab crossTab, int totalsIdx, int rowIdx) {
		if (crossTab.isCalculateTotalsOnRows()) { // if i have the totals column i can read the total value from there
			return Double.parseDouble(matrix[rowIdx][totalsIdx]);
		} else { // otherwise i need to compute it
			Double total = 0.0;
			for (int j = 0; j < matrix[rowIdx].length; j++) {
				Double value;
				try {
					value = Double.parseDouble(matrix[rowIdx][j]);
				} catch (NumberFormatException e) {
					// if conversion fails it is an empty string
					value = 0.0;
				}
				total += value;
			}
			return total;
		}
	}

	private SourceBean mergeHorizontally(SourceBean left, SourceBean right, CrossTab crossTab) throws SourceBeanException {

		SourceBean table = new SourceBean(TABLE_TAG);
		List leftRows = left.getAttributeAsList(ROW_TAG);
		List rightRows = right.getAttributeAsList(ROW_TAG);

		if (leftRows.size() < rightRows.size()) {
			// no categories on rows: forces fake ones to continue
			for (int i = leftRows.size(); i < rightRows.size(); i++) {
				leftRows.add(new SourceBean(ROW_TAG));
			}
		}

		if (rightRows.size() < leftRows.size()) {
			// no categories on columns: forces fake ones to continue
			for (int i = rightRows.size(); i < leftRows.size(); i++) {
				rightRows.add(new SourceBean(ROW_TAG));
			}
		}

		if (leftRows.size() != rightRows.size()) {
			throw new SpagoBIEngineRuntimeException("Cannot merge horizontally 2 tables with a different number of rows");
		}
		for (int i = 0; i < leftRows.size(); i++) {
			SourceBean aLeftRow = (SourceBean) leftRows.get(i);
			SourceBean aRightRow = (SourceBean) rightRows.get(i);
			SourceBean merge = new SourceBean(aLeftRow);
			merge.delAttribute(COLUMN_TAG);
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

	private SourceBean mergeHeadWithBody(SourceBean head, SourceBean body) throws SourceBeanException {
		SourceBean table = new SourceBean(TABLE_TAG);
		List topRows = head.getAttributeAsList(ROW_TAG);
		List bottomRows = body.getAttributeAsList(ROW_TAG);

		if (topRows == null) {
			topRows = new ArrayList();
		}
		if (bottomRows == null) {
			bottomRows = new ArrayList();
		}

		SourceBean tableHeaders = new SourceBean(THEAD_TAG);
		for (int i = 0; i < topRows.size(); i++) {
			SourceBean aRow = (SourceBean) topRows.get(i);
			tableHeaders.setAttribute(aRow);
		}

		SourceBean tableBody = new SourceBean(TBODY_TAG);
		for (int i = 0; i < bottomRows.size(); i++) {
			SourceBean aRow = (SourceBean) bottomRows.get(i);
			tableBody.setAttribute(aRow);
		}

		table.setAttribute(tableHeaders);
		table.setAttribute(tableBody);

		return table;
	}

	private SourceBean addSortArrow(SourceBean aRow, String alias, String parentStyle, String divStyle, Integer direction, boolean isMeasureHeader,
			String variable) throws SourceBeanException, JSONException {

		String headerText = alias;
		if (this.variables.has(variable)) {
			headerText = this.variables.getString(variable);
		}
		SourceBean div1 = new SourceBean(COLUMN_DIV);
		if (divStyle != null && !divStyle.equals("")) {
			div1.setAttribute(STYLE_ATTRIBUTE, divStyle);
		}
		div1.setAttribute(TITLE_ATTRIBUTE, headerText);

		SourceBean icon = new SourceBean("i");
		SourceBean text = new SourceBean("span");

		// Defining text...
		text.setCharacters(headerText);
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
			aColumn.setAttribute(CLASS_ATTRIBUTE, HEADER_CLASS);
			if (crossTab.isFixedColumn())
				aColumn.setAttribute(FIXED_COLUMN_ATTRIBUTE, "header" + i);
			aColumn.setAttribute(NG_CLICK_ATTRIBUTE, "orderPivotTable('" + i + "','0'," + myGlobalId + ")");
			aColumn.setAttribute(addSortArrow(aRow, aRowDef.getAlias(), style, widthStyle, direction, false, aRowDef.getVariable()));
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

//			if (crossTab.getCrosstabDefinition().getConfig().optBoolean("calculatetotalsoncolumns")) {
//				// add fake TD just for total column values (if required)
//				aColumn = new SourceBean(COLUMN_TAG);
//				aColumn.setAttribute(CLASS_ATTRIBUTE, LEVEL_CLASS);
//				aRow.setAttribute(aColumn);
//			}
		}

		if (crossTab.getCrosstabDefinition().getRows().size() == 0 && crossTab.getCrosstabDefinition().getConfig().optBoolean("calculatetotalsonrows")) {
			// add fake TD just for total column values (if required)
			addRow = true;
			SourceBean aColumn = new SourceBean(COLUMN_TAG);
			aColumn.setAttribute(CLASS_ATTRIBUTE, EMPTY_CLASS);
			aRow.setAttribute(aColumn);
		}

		if (addRow) {
			table.setAttribute(aRow);
		}
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
			if (crossTab.isFixedColumn())
				emptyColumn.setAttribute(FIXED_COLUMN_ATTRIBUTE, "header0");
//			emptyColumn.setAttribute(STYLE_ATTRIBUTE, "border:0;");
			if (!crossTab.isMeasureOnRow()) {
				if (crossTab.getCrosstabDefinition().getRows().size() == 0 && !crossTab.getCrosstabDefinition().getConfig().optBoolean("calculatetotalsonrows"))
					// if (rows.size() == 0)
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

	private Object getCategoryConfByLabel(CrossTab crossTab, String text, String type) throws JSONException {
		if (type.equalsIgnoreCase("rows")) {
			for (int e = 0; e < crossTab.getCrosstabDefinition().getRows().size(); e++) {
				Row ris = crossTab.getCrosstabDefinition().getRows().get(e);

				if (text == null || ris.getAlias().equals(text)) {
					return ris;
				}
			}
		} else if (type.equalsIgnoreCase("columns")) {
			for (int e = 0; e < crossTab.getCrosstabDefinition().getColumns().size(); e++) {
				Column cis = crossTab.getCrosstabDefinition().getColumns().get(e);

				if (text == null || cis.getAlias().equals(text)) {
					return cis;
				}
			}
		}
		return null;
	}
}
