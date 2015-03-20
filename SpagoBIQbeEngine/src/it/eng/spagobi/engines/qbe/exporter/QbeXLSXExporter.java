/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
