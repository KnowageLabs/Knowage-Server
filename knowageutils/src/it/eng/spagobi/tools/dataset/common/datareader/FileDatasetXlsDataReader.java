/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.datareader;

import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marco Cortella marco.cortella@eng.it
 */
public class FileDatasetXlsDataReader extends AbstractDataReader {

	private static transient Logger logger = Logger.getLogger(FileDatasetXlsDataReader.class);
	public static final String XSL_FILE_SKIP_ROWS = "skipRows";
	public static final String XSL_FILE_LIMIT_ROWS = "limitRows";
	public static final String XSL_FILE_SHEET_NUMBER = "xslSheetNumber";
	private String skipRows;
	private String limitRows;
	private String xslSheetNumber;
	private int numberOfColumns = 0;

	public FileDatasetXlsDataReader(JSONObject jsonConf) {
		super();

		// Get File Dataset Configuration Options
		if (jsonConf != null) {
			try {
				if (jsonConf.get(XSL_FILE_SKIP_ROWS) != null) {
					skipRows = jsonConf.get(XSL_FILE_SKIP_ROWS).toString();
				} else {
					skipRows = "";
				}

				if (jsonConf.get(XSL_FILE_LIMIT_ROWS) != null) {
					limitRows = jsonConf.get(XSL_FILE_LIMIT_ROWS).toString();
				} else {
					limitRows = "";
				}

				if (jsonConf.get(XSL_FILE_SHEET_NUMBER) != null) {
					xslSheetNumber = jsonConf.get(XSL_FILE_SHEET_NUMBER).toString();
				} else {
					xslSheetNumber = "";
				}
			} catch (JSONException e) {
				throw new RuntimeException("Error Deserializing File Dataset Options", e);
			}
		} else {
			logger.error("Error jsonConf is not present for FileDatasetXlsDataReader");
			throw new RuntimeException("Error jsonConf is not present for FileDatasetXlsDataReader");
		}
	}

	public IDataStore read(Object data) {
		DataStore dataStore = null;

		InputStream inputDataStream;

		logger.debug("IN");

		inputDataStream = (InputStream) data;

		try {
			dataStore = readXls(inputDataStream);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dataStore;
	}

	private DataStore readXls(InputStream inputDataStream) throws Exception {

		DataStore dataStore = null;
		int maxResults = this.getMaxResults();
		boolean checkMaxResults = false;
		if ((maxResults > 0)) {
			checkMaxResults = true;
		}

		logger.debug("IN");

		dataStore = new DataStore();
		try {
			HSSFWorkbook wb = new HSSFWorkbook(inputDataStream);
			HSSFSheet sheet = getSheet(wb);

			int initialRow = 0;
			if ((skipRows != null) && (!skipRows.isEmpty())) {
				initialRow = Integer.parseInt(skipRows);
				logger.debug("Skipping first " + skipRows + " rows");

			}

			int rowsLimit;
			if ((limitRows != null) && (!limitRows.isEmpty())) {
				rowsLimit = initialRow + Integer.parseInt(limitRows) - 1;
				// if the calculated limit exceed the physical number of rows, just read all the rows
				if (rowsLimit > sheet.getPhysicalNumberOfRows()) {
					rowsLimit = sheet.getPhysicalNumberOfRows();
				}
			} else {
				rowsLimit = sheet.getPhysicalNumberOfRows();
			}
			int rowFetched = 0;

			for (int r = initialRow; r <= rowsLimit; r++) {
				// check if there is a limit for the rows to fetch in preview
				if (checkMaxResults) {
					if (rowFetched >= maxResults) {
						break;
					}
				}
				HSSFRow row = sheet.getRow(r);
				if (row == null) {
					continue;
				}

				if (r == initialRow) {
					try {
						MetaData dataStoreMeta = parseHeader(dataStore, row);
						dataStore.setMetaData(dataStoreMeta);
					} catch (Throwable t) {
						throw new RuntimeException("Impossible to parse header row", t);
					}
				} else {
					try {
						IRecord record = parseRow(dataStore, row);
						dataStore.appendRecord(record);
						rowFetched++;

					} catch (Throwable t) {
						throw new RuntimeException("Impossible to parse row [" + r + "]", t);
					}
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to parse XLS file", t);
		} finally {
			logger.debug("OUT");
		}

		return dataStore;
	}

	private HSSFSheet getSheet(HSSFWorkbook workbook) {
		HSSFSheet sheet;

		int numberOfSheets = workbook.getNumberOfSheets();
		if ((xslSheetNumber != null) && (!xslSheetNumber.isEmpty())) {

			int sheetNumber = Integer.parseInt(xslSheetNumber) - 1;
			if (sheetNumber > numberOfSheets) {
				logger.error("Wrong sheet number, using first sheet as default");
				// if not specified take first sheet
				sheet = workbook.getSheetAt(0);
			}
			sheet = workbook.getSheetAt(sheetNumber);

		} else {
			// if not specified take first sheet
			sheet = workbook.getSheetAt(0);

		}

		return sheet;
	}

	private MetaData parseHeader(DataStore dataStore, HSSFRow row) {
		MetaData dataStoreMeta = new MetaData();

		int cells = row.getPhysicalNumberOfCells();
		this.setNumberOfColumns(cells);
		logger.debug("\nROW " + row.getRowNum() + " has " + cells + " cell(s).");
		for (int c = 0; c < cells; c++) {
			// get single cell
			HSSFCell cell = row.getCell(c);

			String valueField = null;
			try {
				valueField = parseCell(cell);
			} catch (Throwable t) {
				throw new RuntimeException("Impossible to parse cell [" + c + "]", t);
			}

			FieldMetadata fieldMeta = new FieldMetadata();
			String fieldName = StringUtils.escapeForSQLColumnName(valueField);
			fieldMeta.setName(fieldName);
			fieldMeta.setType(String.class);
			dataStoreMeta.addFiedMeta(fieldMeta);
		}

		return dataStoreMeta;
	}

	private IRecord parseRow(DataStore dataStore, HSSFRow row) {

		IRecord record = new Record(dataStore);

		int cells = row.getPhysicalNumberOfCells();
		// int lastColumn = row.getLastCellNum();
		int lastColumn = this.getNumberOfColumns();

		logger.debug("\nROW " + row.getRowNum() + " has " + cells + " cell(s).");
		for (int c = 0; c < lastColumn; c++) {
			// get single cell
			HSSFCell cell = row.getCell(c);

			String valueField = null;
			try {
				valueField = parseCell(cell);
			} catch (Throwable t) {
				throw new RuntimeException("Impossible to parse cell [" + c + "]", t);
			}

			IField field = new Field(valueField);
			record.appendField(field);
		}

		return record;
	}

	private String parseCell(HSSFCell cell) {
		String valueField = null;

		if (cell == null)
			return "";

		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_FORMULA:
			valueField = cell.getCellFormula().toString();
			break;

		case HSSFCell.CELL_TYPE_NUMERIC:
			Double numericValue = cell.getNumericCellValue();
			// testing if the double is an integer value
			if ((numericValue == Math.floor(numericValue)) && !Double.isInfinite(numericValue)) {
				// the number is an integer, this will remove the .0 trailing zeros
				int numericInt = numericValue.intValue();
				valueField = String.valueOf(numericInt);
			} else {
				valueField = String.valueOf(cell.getNumericCellValue());

			}
			break;

		case HSSFCell.CELL_TYPE_STRING:
			valueField = cell.getStringCellValue();
			break;

		default:
		}

		return valueField;
	}

	/**
	 * @return the numberOfColumns
	 */
	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	/**
	 * @param numberOfColumns
	 *            the numberOfColumns to set
	 */
	public void setNumberOfColumns(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
	}

}
