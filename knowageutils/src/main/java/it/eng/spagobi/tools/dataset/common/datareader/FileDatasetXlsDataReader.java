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

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

	private static final String EXCEL_FILE_TYPE = "fileType";
	public static final String EXCEL_FILE_SKIP_ROWS = "skipRows";
	public static final String EXCEL_FILE_LIMIT_ROWS = "limitRows";
	public static final String EXCEL_FILE_SHEET_NUMBER = "xslSheetNumber";

	private String skipRows;
	private String limitRows;
	private String xslSheetNumber;
	private int numberOfColumns = 0;
	private String fileType;
	private DataFormatter formatter = null;

	public FileDatasetXlsDataReader(JSONObject jsonConf) {
		super();

		formatter = new DataFormatter();

		// Get File Dataset Configuration Options
		if (jsonConf != null) {
			try {
				if (jsonConf.get(EXCEL_FILE_SKIP_ROWS) != null) {
					setSkipRows(jsonConf.get(EXCEL_FILE_SKIP_ROWS).toString());
				} else {
					setSkipRows("");
				}

				if (jsonConf.get(EXCEL_FILE_LIMIT_ROWS) != null) {
					setLimitRows(jsonConf.get(EXCEL_FILE_LIMIT_ROWS).toString());
				} else {
					setLimitRows("");
				}

				if (jsonConf.get(EXCEL_FILE_SHEET_NUMBER) != null) {
					setXslSheetNumber(jsonConf.get(EXCEL_FILE_SHEET_NUMBER).toString());
				} else {
					setXslSheetNumber("");
				}
				if (jsonConf.get(EXCEL_FILE_TYPE) != null) {
					setFileType(jsonConf.get(EXCEL_FILE_TYPE).toString());
				} else {
					setFileType("");
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
			logger.error("Inaccessible file", e);
		} catch (IOException e) {
			logger.error("Cannot read the file", e);
		} catch (Exception e) {
			logger.error("Cannot read data", e);
		}

		return dataStore;
	}

	private DataStore readXls(InputStream inputDataStream) throws Exception {

		DataStore dataStore = null;
		int maxResults = this.getMaxResults();
		boolean checkMaxResults = false;
		if (maxResults > 0) {
			checkMaxResults = true;
		}

		logger.debug("IN");

		dataStore = new DataStore();

		try {
			ExcelDataReaderFactory excelFactory = new ExcelDataReaderFactory();
			Workbook wb = excelFactory.getWorkookInstance(getFileType(), inputDataStream);
			Sheet sheet = getSheet(wb);

			int initialRow = 0;
			int skipRows = 0;

			if ((getSkipRows() != null) && (!(getSkipRows().isEmpty())) && !(getSkipRows().equals(String.valueOf(0)))) {
				skipRows = Integer.parseInt(getSkipRows());
				initialRow = skipRows - 1;
				logger.debug("Skipping first " + getSkipRows() + " rows");
			}

			int rowsLimit;
			if ((getLimitRows() != null) && (!(getLimitRows().isEmpty()))) {
				rowsLimit = initialRow + Integer.parseInt(getLimitRows()) - 1;
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

			// Read Header
			for (int i = initialRow; i <= rowsLimit; i++) {

				Row headerRow = sheet.getRow(i);
				if (checkIfRowIsEmpty(headerRow)) {
					continue;
				}

				if (i == initialRow) {

					try {
						MetaData dataStoreMeta = parseHeader(dataStore, headerRow);
						dataStore.setMetaData(dataStoreMeta);
						if (paginated) {
							if (offset == 0) {
								initialRow = initialRow + 1;
							} else {
								initialRow = initialRow + offset + 1;
							}
						} else {
							initialRow = initialRow + 1;
						}
						break;
					} catch (Throwable t) {
						throw new RuntimeException("Impossible to parse header row", t);
					}
				}
			}

			// Read Records
			for (int r = initialRow; r < rowsLimit; r++) {
				Row row = sheet.getRow(r);
				if (checkIfRowIsEmpty(row)) {
					continue;
				}
				try {
					IRecord record = parseRow(dataStore, row);
					dataStore.appendRecord(record);
					rowFetched++;
					if (rowFetched == fetchSize || (checkMaxResults && rowFetched == maxResults))
						break;
				} catch (Throwable t) {
					throw new RuntimeException("Impossible to parse row [" + r + "]", t);
				}
			}

			logger.debug("Read [" + rowFetched + "] records");
			logger.debug("Insert [" + dataStore.getRecordsCount() + "] records");

			if (this.isCalculateResultNumberEnabled()) {
				logger.debug("Calculation of result set number is enabled");
				Integer result = rowsLimit - skipRows - 1;
				dataStore.getMetaData().setProperty("resultNumber", result);
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
			if (cell != null && cell.getCellType() != CellType.BLANK && org.apache.commons.lang.StringUtils.isNotBlank(cell.toString())) {
				return false;
			}
		}
		return true;
	}

	private Sheet getSheet(Workbook workbook) {
		Sheet sheet;

		int numberOfSheets = workbook.getNumberOfSheets();
		if ((getXslSheetNumber() != null) && (!(getXslSheetNumber().isEmpty()))) {

			int sheetNumber = Integer.parseInt(getXslSheetNumber()) - 1;
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

	private MetaData parseHeader(DataStore dataStore, Row row) {
		MetaData dataStoreMeta = new MetaData();

		int cells = row.getPhysicalNumberOfCells();
		int lastColumn = row.getLastCellNum();

		if (cells != lastColumn) {
			cells = lastColumn;
		}
		this.setNumberOfColumns(cells);
		logger.debug("\nROW " + row.getRowNum() + " has " + cells + " cell(s).");
		for (int c = 0; c < cells; c++) {
			// get single cell
			Cell cell = row.getCell(c);

			Object valueField = null;
			try {
				valueField = parseCell(cell);
			} catch (Throwable t) {
				throw new RuntimeException("Impossible to parse cell [" + c + "]", t);
			}

			if (valueField == null) {
				valueField = new String();
				valueField = "Column " + String.valueOf(c + 1);
			}
			FieldMetadata fieldMeta = new FieldMetadata();
			String fieldName = StringUtils.escapeForSQLColumnName(valueField.toString());
			fieldMeta.setName(fieldName);

			dataStoreMeta.addFiedMeta(fieldMeta);
		}

		return dataStoreMeta;
	}

	private IRecord parseRow(DataStore dataStore, Row row) {

		IRecord record = new Record(dataStore);

		int cells = row.getPhysicalNumberOfCells();
		// int lastColumn = row.getLastCellNum();
		int lastColumn = this.getNumberOfColumns();

		logger.debug("\nROW " + row.getRowNum() + " has " + cells + " cell(s).");
		for (int c = 0; c < lastColumn; c++) {
			// get single cell
			Cell cell = row.getCell(c);

			Object valueField = null;
			try {
				valueField = parseCell(cell);
			} catch (Throwable t) {
				throw new RuntimeException("Impossible to parse cell [" + c + "]", t);
			}

			FieldMetadata fieldMeta = ((FieldMetadata) dataStore.getMetaData().getFieldMeta(c));
			Class oldType = fieldMeta.getType();
			if (valueField != null && valueField instanceof Double) {
				fieldMeta.setType(getNewMetaType(oldType, Double.class));
			} else if (valueField != null && valueField instanceof BigDecimal) {
				fieldMeta.setType(getNewMetaType(oldType, BigDecimal.class));
			} else if (valueField != null && valueField instanceof Integer) {
				fieldMeta.setType(getNewMetaType(oldType, Integer.class));
			} else if (valueField != null && valueField instanceof Long) {
				fieldMeta.setType(getNewMetaType(oldType, Long.class));
			} else if (valueField != null && valueField instanceof Date) {
				if (valueField instanceof Timestamp) {
					fieldMeta.setType(getNewMetaType(oldType, Timestamp.class));
				} else {
					fieldMeta.setType(getNewMetaType(oldType, Date.class));
				}
			} else {
				fieldMeta.setType(getNewMetaType(oldType, String.class));
			}

			IField field = new Field(valueField);

			record.appendField(field);
		}

		return record;
	}

	private Object parseCell(Cell cell) {
		Object valueField = null;
		if (cell == null)
			return null;
		CellType cellType = cell.getCellType();

		if (cellType == CellType.FORMULA) {
			cellType = cell.getCachedFormulaResultType();
		}

		switch (cellType) {

		case NUMERIC:
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
				String formatedCell = formatter.formatCellValue(cell);
				if (formatedCell.contains(".") || formatedCell.contains(",")) {
					try {
						valueField = new Double(String.valueOf(formatedCell));
					} catch (NumberFormatException nfe) {
						try {
							valueField = new BigDecimal(String.valueOf(formatedCell));
						} catch (NumberFormatException e) {
							valueField = cell.getNumericCellValue();
						}
					}
				} else {
					try {
						valueField = new Integer(String.valueOf(formatedCell));
					} catch (NumberFormatException nfe) {
						try {
							valueField = new Long(String.valueOf(formatedCell));
						} catch (NumberFormatException e) {
							valueField = cell.getNumericCellValue();
						}
					}
				}
			}
			break;

		case STRING:
			if (org.apache.commons.lang.StringUtils.isBlank(cell.getStringCellValue())) {
				valueField = "";
			} else {
				valueField = cell.getStringCellValue();
			}

			break;

		case BLANK:
			valueField = null;
			break;
		default:
		}

		return valueField;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getSkipRows() {
		return skipRows;
	}

	public void setSkipRows(String skipRows) {
		this.skipRows = skipRows;
	}

	public String getLimitRows() {
		return limitRows;
	}

	public void setLimitRows(String limitRows) {
		this.limitRows = limitRows;
	}

	public String getXslSheetNumber() {
		return xslSheetNumber;
	}

	public void setXslSheetNumber(String xslSheetNumber) {
		this.xslSheetNumber = xslSheetNumber;
	}

	public int getNumberOfColumns() {
		return numberOfColumns;
	}

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