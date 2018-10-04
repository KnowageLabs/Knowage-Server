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
package it.eng.spagobi.tools.dataset.common.datareader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.StringUtils;

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

	@Override
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

				/**
				 * This line is commented, since we need an absolute value of the last row that should be taken while now the 0th row is always taken into count
				 * and always as a header, not as an effective data row. In terms of the existing implementation: number of rows to take from an XLS file should
				 * be an offset of value 'limitRows' relative to the 'initialRow' that is now the real effective row (data, not header) from which we start
				 * counting.
				 *
				 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				// ORIGINAL CODE (danristo)
				// rowsLimit = initialRow + Integer.parseInt(limitRows) - 1;
				// MODIFIED CODE (danristo)
				rowsLimit = initialRow + Integer.parseInt(limitRows);

				// if the calculated limit exceed the physical number of rows or is equal to zero, just read all the rows
				if ((rowsLimit > sheet.getPhysicalNumberOfRows()) || rowsLimit == 0) {
					rowsLimit = sheet.getPhysicalNumberOfRows();
				}
			} else {
				rowsLimit = sheet.getPhysicalNumberOfRows();
			}

			boolean paginated = false;
			logger.debug("Reading data ...");
			if (isPaginationSupported() && getOffset() >= 0 && getFetchSize() >= 0) {
				logger.debug("Offset is equal to [" + getOffset() + "] and fetchSize is equal to [" + getFetchSize() + "]");
				paginated = true;
			} else {
				logger.debug("Offset and fetch size not set");
			}

			int rowFetched = 0;

			/**
			 * Starting point when picking rows from the XLS file is ALWAYS the 0th row - the header of the file (metadata - the names of the columns of the
			 * file dataset). Inside the for-loop we will check if the row is the header (0th) and if it is, treat it accordingly. Otherwise, skip all rows that
			 * are between this one and the one that we get as a final row ('rowsLimit').
			 *
			 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			// ORIGINAL CODE (danristo)
			// for (int r = initialRow; r <= rowsLimit; r++) {
			// MODIFIED CODE (danristo)
			for (int r = 0; r <= rowsLimit; r++) {
				// check if there is a limit for the rows to fetch in preview

				/**
				 * If we are in between 0th and final row of the XLS file, while skipping all rows that user specified as the ones that should be skipped, take
				 * all metadata (for initial, zeroth) and data available in their columns.
				 *
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				if (r > initialRow || r == 0) {

					HSSFRow row = sheet.getRow(r);
					if (checkIfRowIsEmpty(row)) {
						continue;
					}

					/**
					 * The zeroth row will always be the header of the XLS file.
					 *
					 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					// ORIGINAL CODE (danristo)
					// if (r == initialRow) {
					// MODIFIED CODE (danristo)
					if (r == 0) {
						try {
							MetaData dataStoreMeta = parseHeader(dataStore, row);
							dataStore.setMetaData(dataStoreMeta);
						} catch (Throwable t) {
							throw new RuntimeException("Impossible to parse header row", t);
						}
					} else {
						try {
							if ((!paginated && (!checkMaxResults || (rowFetched < maxResults)))
									|| ((paginated && (rowFetched >= offset) && (rowFetched - offset < fetchSize))
											&& (!checkMaxResults || (rowFetched - offset < maxResults)))) {
								IRecord record = parseRow(dataStore, row);
								dataStore.appendRecord(record);
							}
							rowFetched++;

						} catch (Throwable t) {
							throw new RuntimeException("Impossible to parse row [" + r + "]", t);
						}
					}

				}

			}
			logger.debug("Read [" + rowFetched + "] records");
			logger.debug("Insert [" + dataStore.getRecordsCount() + "] records");

			if (this.isCalculateResultNumberEnabled()) {
				logger.debug("Calculation of result set number is enabled");
				dataStore.getMetaData().setProperty("resultNumber", new Integer(rowFetched));
			} else {
				logger.debug("Calculation of result set number is NOT enabled");
			}
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to parse XLS file", t);
		} finally {
			logger.debug("OUT");
		}

		return dataStore;
	}

	private boolean checkIfRowIsEmpty(Row row) {
		if (row == null) {
			return true;
		}
		if (row.getLastCellNum() <= 0) {
			return true;
		}
		for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
			Cell cell = row.getCell(cellNum);
			if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK && org.apache.commons.lang.StringUtils.isNotBlank(cell.toString())) {
				return false;
			}
		}
		return true;
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

			Object valueField = null;
			try {
				valueField = parseCell(cell);
			} catch (Throwable t) {
				throw new RuntimeException("Impossible to parse cell [" + c + "]", t);
			}

			FieldMetadata fieldMeta = new FieldMetadata();
			if (valueField instanceof String) {
				String fieldName = StringUtils.escapeForSQLColumnName((String) valueField);
				fieldMeta.setName(fieldName);
				fieldMeta.setType(String.class);
			}
			if (!valueField.equals("")) {
				dataStoreMeta.addFiedMeta(fieldMeta);
			}
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

			Object valueField = null;
			try {
				valueField = parseCell(cell);
			} catch (Throwable t) {
				throw new RuntimeException("Impossible to parse cell [" + c + "]", t);
			}
			// update metadata type in order with the real value's type (default was string)
			if (valueField instanceof String) {
				if (NumberUtils.isNumber((String) valueField)) {
					((FieldMetadata) dataStore.getMetaData().getFieldMeta(c)).setType(BigDecimal.class);
					valueField = new BigDecimal(String.valueOf(valueField));
				}
			}
			if (valueField instanceof Date) {
				if (valueField instanceof Timestamp) {
					((FieldMetadata) dataStore.getMetaData().getFieldMeta(c)).setType(Timestamp.class);
				} else {
					((FieldMetadata) dataStore.getMetaData().getFieldMeta(c)).setType(Date.class);
				}
			}

			IField field = new Field(valueField);

			record.appendField(field);
		}

		return record;
	}

	private Object parseCell(HSSFCell cell) {
		Object valueField = null;

		if (cell == null)
			return "";

		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_FORMULA:
			valueField = cell.getCellFormula().toString();
			break;

		case HSSFCell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				/**
				 * HSSFCell.getCellStyle().getDataFormatString() returns Cell date format (even if it is a Custom format), but it will be Excel's Date format
				 * that is DIFFERENT from Java's
				 */
				// String formatString = cell.getCellStyle().getDataFormatString();
				Date date = cell.getDateCellValue();

				// If date object doesn't contain Hours, Minutes and Seconds return Date object, otherwise create Timestamp
				if (date.getHours() == 0 && date.getMinutes() == 0 && date.getSeconds() == 0) {
					return date;
				} else {
					return new Timestamp(date.getTime());
				}
			} else {
				Double numericValue = cell.getNumericCellValue();
				// testing if the double is an integer value
				if ((numericValue == Math.floor(numericValue)) && !Double.isInfinite(numericValue)) {
					// the number is an integer, this will remove the .0 trailing zeros
					int numericInt = numericValue.intValue();
					valueField = String.valueOf(numericInt);
				} else {
					valueField = String.valueOf(cell.getNumericCellValue());
				}
			}
			break;

		case HSSFCell.CELL_TYPE_STRING:
			valueField = cell.getStringCellValue();
			break;

		case HSSFCell.CELL_TYPE_BLANK:
			valueField = "";
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

	@Override
	public boolean isOffsetSupported() {
		return true;
	}

	@Override
	public boolean isFetchSizeSupported() {
		return true;
	}

	@Override
	public boolean isMaxResultsSupported() {
		return true;
	}

}
