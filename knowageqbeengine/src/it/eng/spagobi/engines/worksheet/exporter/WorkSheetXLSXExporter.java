/*
* Knowage, Open Source Business Intelligence suite
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
