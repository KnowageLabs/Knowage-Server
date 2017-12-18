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
package it.eng.spagobi.engines.qbe.exporter;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class QbeXLSXExporter extends QbeXLSExporter {
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(QbeXLSXExporter.class);
	
	public QbeXLSXExporter(IDataStore dataStore, Locale locale) {
		super(dataStore, locale);
	}
	
	protected Workbook instantiateWorkbook() {
		Workbook workbook = new XSSFWorkbook();
		return workbook;
	}
    
	protected int getCellTypeNumeric () {
		return XSSFCell.CELL_TYPE_NUMERIC;
	}
	
	protected int getCellTypeString () {
		return XSSFCell.CELL_TYPE_STRING;
	}
	
	protected int getCellTypeBoolean () {
		return XSSFCell.CELL_TYPE_BOOLEAN;
	}
	
	protected short getBuiltinFormat (String formatStr) {
		short format = (short) BuiltinFormats.getBuiltinFormat(formatStr); 
		return format;
	}
	
}
