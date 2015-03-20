/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.crosstable;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.engines.qbe.crosstable.CrossTab.CellType;
import it.eng.spagobi.engines.qbe.crosstable.CrossTab.MeasureInfo;
import it.eng.spagobi.engines.worksheet.bo.Measure;
import it.eng.spagobi.engines.worksheet.bo.MeasureScaleFactorOption;
import it.eng.spagobi.engines.worksheet.services.export.MeasureFormatter;
import it.eng.spagobi.engines.worksheet.widgets.CrosstabDefinition.Row;
import it.eng.spagobi.utilities.messages.EngineMessageBundle;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class CrossTabHTMLSerializer {
	
	private static String TABLE_TAG = "TABLE";
	private static String ROW_TAG = "TR";
	private static String COLUMN_TAG = "TD";
	private static String CLASS_ATTRIBUTE = "class";
	private static String ROWSPAN_ATTRIBUTE = "rowspan";
	private static String COLSPAN_ATTRIBUTE = "colspan";
	
	private static String EMPTY_CLASS = "empty";
	private static String MEMBER_CLASS = "member";
	private static String LEVEL_CLASS = "level";
	private static String NA_CLASS = "na";
	
	private Locale locale = null;
	
	private static Logger logger = Logger.getLogger(CrossTabHTMLSerializer.class);
	
	public CrossTabHTMLSerializer(Locale locale) {
		this.locale = locale;
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
		
		SourceBean emptyTopLeftCorner = this.serializeTopLeftCorner(crossTab);
		SourceBean rowsHeaders = this.serializeRowsHeaders(crossTab);
		SourceBean topLeftCorner = this.mergeVertically(emptyTopLeftCorner, rowsHeaders);
		SourceBean columnsHeaders = this.serializeColumnsHeaders(crossTab);
		SourceBean head = this.mergeHorizontally(topLeftCorner, columnsHeaders);
		
		SourceBean rowsMember = this.serializeRowsMembers(crossTab);
		SourceBean data = this.serializeData(crossTab);
		SourceBean body = this.mergeHorizontally(rowsMember, data);
		
		SourceBean crossTabSB = this.mergeVertically(head, body);
		
		return crossTabSB;
	}

	private SourceBean serializeRowsMembers(CrossTab crossTab) throws SourceBeanException {
		SourceBean table = new SourceBean(TABLE_TAG);
		int leaves = crossTab.getRowsRoot().getLeafsNumber();
		
		if (leaves == 1) { // only root node exists 
			// no attributes on rows, maybe measures?
			if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
				List<Measure> measures = crossTab.getCrosstabDefinition().getMeasures();
				for (int i = 0; i < measures.size(); i++) {
					Measure measure = measures.get(i);
					SourceBean aRow = new SourceBean(ROW_TAG);
					SourceBean aColumn = new SourceBean(COLUMN_TAG);
					aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
					String measureAlias = measure.getAlias();
					String text = MeasureScaleFactorOption.getScaledName(
							measureAlias,
							crossTab.getMeasureScaleFactor(measureAlias),
							this.locale);
					aColumn.setCharacters(text);
					aRow.setAttribute(aColumn);
					table.setAttribute(aRow);
				}
			} else {
				// nothing on rows
				SourceBean aRow = new SourceBean(ROW_TAG);
				SourceBean aColumn = new SourceBean(COLUMN_TAG);
				aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
				aColumn.setCharacters(EngineMessageBundle.getMessage("sbi.crosstab.runtime.headers.data", this.getLocale()));
				aRow.setAttribute(aColumn);
				table.setAttribute(aRow);
				JSONObject config = crossTab.getCrosstabDefinition().getConfig();
				String rowsTotals =  config.optString("calculatetotalsoncolumns");
				if (rowsTotals != null && rowsTotals.equals("on")) {
					SourceBean totalRow = new SourceBean(ROW_TAG);
					SourceBean totalColumn = new SourceBean(COLUMN_TAG);
					totalColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
					totalColumn.setCharacters(CrossTab.TOTAL);
					totalRow.setAttribute(totalColumn);
					table.setAttribute(totalRow);
				}
			}
		} else {
			List<SourceBean> rows = new ArrayList<SourceBean>();
			// initialize all rows (with no columns)
			for (int i = 0 ; i < leaves; i++) {
				SourceBean aRow = new SourceBean(ROW_TAG);
				table.setAttribute(aRow);
				rows.add(aRow);
			}
			
			int levels = crossTab.getRowsRoot().getDistanceFromLeaves();
			for (int i = 0 ; i < levels; i++) {
				List<Node> levelNodes = crossTab.getRowsRoot().getLevel(i + 1);
				int counter = 0;
				for (int j = 0 ; j < levelNodes.size(); j++) {
					SourceBean aRow = rows.get(counter);
					Node aNode = levelNodes.get(j);
					SourceBean aColumn = new SourceBean(COLUMN_TAG);
					aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
					
					String text = null;
					if (crossTab.getCrosstabDefinition().isMeasuresOnRows() && i + 1 == levels) {
						String measureAlias = aNode.getDescription();
						text = MeasureScaleFactorOption.getScaledName(
							measureAlias,
							crossTab.getMeasureScaleFactor(measureAlias),
							this.locale);
					} else {
						text = aNode.getDescription();
					}
					
					aColumn.setCharacters(text);
					int rowSpan = aNode.getLeafsNumber();
					if (rowSpan > 1) {
						aColumn.setAttribute(ROWSPAN_ATTRIBUTE, rowSpan);
					}
					aRow.setAttribute(aColumn);
					counter = counter + rowSpan;
				}
			}
		}
		
		return table;
	}

	private SourceBean serializeColumnsHeaders(CrossTab crossTab) throws SourceBeanException {
		SourceBean table = new SourceBean(TABLE_TAG);
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
			for (int i = 0 ; i < levels; i++) {
				SourceBean aRow = new SourceBean(ROW_TAG);
				List<Node> levelNodes = crossTab.getColumnsRoot().getLevel(i + 1);
				for (int j = 0 ; j < levelNodes.size(); j++) {
					Node aNode = levelNodes.get(j);
					SourceBean aColumn = new SourceBean(COLUMN_TAG);
					// odd levels are levels (except the last one, since it contains measures' names)
					String className = (( i + 1 ) % 2 == 0 || ( i + 1 ) == levels) ? MEMBER_CLASS : LEVEL_CLASS;
					aColumn.setAttribute(CLASS_ATTRIBUTE, className);
					
					String text = null;
					if (crossTab.getCrosstabDefinition().isMeasuresOnColumns() && i + 1 == levels) {
						String measureAlias = aNode.getDescription();
						text = MeasureScaleFactorOption.getScaledName(
							measureAlias,
							crossTab.getMeasureScaleFactor(measureAlias),
							this.locale);
					} else {
						text = aNode.getDescription();
					}
					
					aColumn.setCharacters(text);
					int colSpan = aNode.getLeafsNumber();
					if (colSpan > 1) {
						aColumn.setAttribute(COLSPAN_ATTRIBUTE, colSpan);
					}
					aRow.setAttribute(aColumn);
				}
				table.setAttribute(aRow);
			}
		}
		return table;
	}

	private SourceBean serializeData(CrossTab crossTab) throws SourceBeanException, JSONException {
		SourceBean table = new SourceBean(TABLE_TAG);
		String[][] data = crossTab.getDataMatrix();
		
		MeasureFormatter measureFormatter = new MeasureFormatter(crossTab);
		for (int i = 0; i < data.length; i++) {
			SourceBean aRow = new SourceBean(ROW_TAG);
			String[] values = data[i];
			for (int j = 0 ; j < values.length; j++) {
				String text = values[j];
				SourceBean aColumn = new SourceBean(COLUMN_TAG);
				CellType cellType = crossTab.getCellType(i, j);
				try {
					double value = Double.parseDouble(text);
					String actualText = measureFormatter.format(value, i, j, this.locale);
					
					String percentOn = crossTab.getCrosstabDefinition().getConfig().optString("percenton");
					if ("row".equals(percentOn) || "column".equals(percentOn)) {
						Double percent = calculatePercent(value, i, j, percentOn, crossTab);
						String percentStr = measureFormatter.formatPercent(percent, this.locale);
						actualText += " (" + percentStr + "%)";
					}
					
					aColumn.setAttribute(CLASS_ATTRIBUTE, cellType.getValue());
					aColumn.setCharacters(actualText);
				} catch (NumberFormatException e) {
					logger.debug("Text " + text
							+ " is not recognized as a number");
					aColumn.setAttribute(CLASS_ATTRIBUTE, NA_CLASS);
					aColumn.setCharacters(text);
				}
				aRow.setAttribute(aColumn);
			}
			table.setAttribute(aRow);
		}
		return table;
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
				return 100
						* value
						/ Double.parseDouble(entries[i][offset
								+ rowSumStartColumn]);
			} else {
				return 100 * value
						/ Double.parseDouble(entries[i][rowSumStartColumn]);
			}
		} else {
			if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
				return 100
						* value
						/ Double.parseDouble(entries[offset
								+ columnSumStartRow][j]);
			} else {
				return 100 * value
						/ Double.parseDouble(entries[columnSumStartRow][j]);
			}
		}
	}

	private SourceBean mergeHorizontally(SourceBean left,
			SourceBean right) throws SourceBeanException {
		
		SourceBean table = new SourceBean(TABLE_TAG);
		List leftRows = left.getAttributeAsList(ROW_TAG);
		List rightRows = right.getAttributeAsList(ROW_TAG);
		if (leftRows.size() != rightRows.size()) {
			throw new SpagoBIEngineRuntimeException("Cannot merge horizontally 2 tables with a different number of rows");
		}
		for (int i = 0 ; i < leftRows.size(); i++) {
			SourceBean aLeftRow = (SourceBean) leftRows.get(i);
			SourceBean aRightRow = (SourceBean) rightRows.get(i);
			SourceBean merge = new SourceBean(ROW_TAG);
			List aLeftRowColumns = aLeftRow.getAttributeAsList(COLUMN_TAG);
			for (int j = 0 ; j < aLeftRowColumns.size(); j++) {
				SourceBean aColumn = (SourceBean) aLeftRowColumns.get(j);
				merge.setAttribute(aColumn);
			}
			List aRightRowColumns = aRightRow.getAttributeAsList(COLUMN_TAG);
			for (int j = 0 ; j < aRightRowColumns.size(); j++) {
				SourceBean aColumn = (SourceBean) aRightRowColumns.get(j);
				merge.setAttribute(aColumn);
			}
			table.setAttribute(merge);
		}
		
		return table;
	}

	private SourceBean mergeVertically(SourceBean top,
			SourceBean bottom) throws SourceBeanException {
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
		for (int i = 0 ; i < topRows.size(); i++) {
			SourceBean aRow = (SourceBean) topRows.get(i);
			table.setAttribute(aRow);
		}

		return table;
	}

	private SourceBean serializeRowsHeaders(CrossTab crossTab) throws SourceBeanException {
		List<Row> rows = crossTab.getCrosstabDefinition().getRows();
		SourceBean table = new SourceBean(TABLE_TAG);
		SourceBean aRow = new SourceBean(ROW_TAG);
		for (int i = 0 ; i < rows.size(); i++) {
			Row aRowDef = rows.get(i);
			SourceBean aColumn = new SourceBean(COLUMN_TAG);
			aColumn.setAttribute(CLASS_ATTRIBUTE, LEVEL_CLASS);
			aColumn.setCharacters(aRowDef.getAlias());
			aRow.setAttribute(aColumn);
		}
		if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
			SourceBean aColumn = new SourceBean(COLUMN_TAG);
			aColumn.setAttribute(CLASS_ATTRIBUTE, LEVEL_CLASS);
			aColumn.setCharacters(EngineMessageBundle.getMessage("sbi.crosstab.runtime.headers.measures", this.getLocale()));
			aRow.setAttribute(aColumn);
		}
		
		// if row is still empty (nothing on rows), add an empty cell
		if (!aRow.containsAttribute(COLUMN_TAG)) {
			SourceBean emptyColumn = new SourceBean(COLUMN_TAG);
			emptyColumn.setAttribute(CLASS_ATTRIBUTE, EMPTY_CLASS);
			aRow.setAttribute(emptyColumn);
		}
		
		table.setAttribute(aRow);
		return table;
	}

	private SourceBean serializeTopLeftCorner(CrossTab crossTab) throws SourceBeanException {
		int columnHeadersVerticalDepth = crossTab.getColumnsRoot().getDistanceFromLeaves();
		int rowHeadersHorizontalDepth = crossTab.getRowsRoot().getDistanceFromLeaves();
		int numberOfEmptyRows = columnHeadersVerticalDepth - 1; // one row is dedicated to rows' headers
		SourceBean table = new SourceBean(TABLE_TAG);
		for (int i = 0 ; i < numberOfEmptyRows; i++) {
			SourceBean emptyRow = new SourceBean(ROW_TAG);
			SourceBean emptyColumn = new SourceBean(COLUMN_TAG);
			emptyColumn.setAttribute(CLASS_ATTRIBUTE, EMPTY_CLASS);
			if (rowHeadersHorizontalDepth > 1) {
				emptyColumn.setAttribute(COLSPAN_ATTRIBUTE, rowHeadersHorizontalDepth);
			}
			emptyRow.setAttribute(emptyColumn);
			table.setAttribute(emptyRow);
		}
		return table;
	}
	
}
