/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.exporter;

import it.eng.spagobi.engines.qbe.exporter.QbeXLSXExporter;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;

/**
 * Exports the crosstab data (formatted as a JSON object in input) into a XLS
 * file. The JSON object should have this structure (a node is {node_key:"Text",
 * node_childs:[...]}): columns: {...} contains tree node structure of the
 * columns' headers rows: {...} contains tree node structure of the rows'
 * headers data: [[...], [...], ...] 2-dimensional matrix containing crosstab
 * data
 * 
 * @author Chiara Chiarelli
 */
public class WorkSheetXLSXExporter extends WorkSheetXLSExporter {

	/** Logger component. */
	public static transient Logger logger = Logger
			.getLogger(WorkSheetXLSXExporter.class);

	@Override
	public Workbook createNewWorkbook() {
		XSSFWorkbook workbook = new XSSFWorkbook();
		return workbook;
	}
	
	public void designTableInWorksheet(Sheet sheet,Workbook wb, CreationHelper createHelper, 
			  IDataStore dataStore, int startRow, Locale locale) throws JSONException {
		
		QbeXLSXExporter qbeXLSExporter = new QbeXLSXExporter(dataStore, locale);
		qbeXLSExporter.setProperty(QbeXLSXExporter.PROPERTY_HEADER_FONT_SIZE, TABLE_HEADER_FONT_SIZE);
		qbeXLSExporter.setProperty(QbeXLSXExporter.PROPERTY_CELL_FONT_SIZE, TABLE_CELL_CONTENT_FONT_SIZE);
		qbeXLSExporter.setProperty(QbeXLSXExporter.PROPERTY_FONT_NAME, FONT_NAME);
		qbeXLSExporter.fillSheet(sheet, wb, createHelper, startRow);
	}
	
	public int getCellTypeNumeric () {
		return XSSFCell.CELL_TYPE_NUMERIC;
	}
	
	public int getCellTypeString () {
		return XSSFCell.CELL_TYPE_STRING;
	}
	
	public int getCellTypeBoolean () {
		return XSSFCell.CELL_TYPE_BOOLEAN;
	}
	
	public int getImageType(String imgNameUpperCase) {
		int impgType = 0;
		if (imgNameUpperCase.contains(".PNG")) {
			impgType = XSSFWorkbook.PICTURE_TYPE_PNG;
		} else if (imgNameUpperCase.contains(".JPG")
				|| imgNameUpperCase.contains(".JPEG")) {
			impgType = XSSFWorkbook.PICTURE_TYPE_JPEG;
		} else if (imgNameUpperCase.contains(".DIB")
				|| imgNameUpperCase.contains(".BMP")) {
			impgType = XSSFWorkbook.PICTURE_TYPE_DIB;
		} else if (imgNameUpperCase.contains(".EMF")) {
			impgType = XSSFWorkbook.PICTURE_TYPE_EMF;
		} else if (imgNameUpperCase.contains(".PICT")
				|| imgNameUpperCase.contains(".PCT")
				|| imgNameUpperCase.contains(".PIC")) {
			impgType = XSSFWorkbook.PICTURE_TYPE_PICT;
		} else if (imgNameUpperCase.contains(".WMF")
				|| imgNameUpperCase.contains(".WMZ")) {
			impgType = XSSFWorkbook.PICTURE_TYPE_WMF;
		}
		return impgType;
	}
	
	@Override
	protected ClientAnchor getClientAnchor(int col, int colend, int sheetRow,
			int height, int dx1, int dy1, int dx2, int dy2) {
		XSSFClientAnchor anchor = new XSSFClientAnchor(dx1, dy1, dx2, dy2,
				(short) col, sheetRow, (short) colend, sheetRow + height);
		return anchor;
	}
	
}
