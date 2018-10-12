package it.eng.spagobi.tools.dataset.common.datareader;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

public class FileDatasetXlsxDataReader extends AbstractExcelDataReader {

	private static transient Logger logger = Logger.getLogger(FileDatasetXlsxDataReader.class);

	public FileDatasetXlsxDataReader(JSONObject jsonConf) {
		super();

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
			} catch (JSONException e) {
				throw new RuntimeException("Error Deserializing File Dataset Options", e);
			}
		} else {
			logger.error("Error jsonConf is not present for FileDatasetXlsxDataReader");
			throw new RuntimeException("Error jsonConf is not present for FileDatasetXlsxDataReader");
		}
	}

	@Override
	public IDataStore read(Object data) {
		logger.debug("IN");
		DataStore dataStore = null;
		InputStream inputDataStream;

		inputDataStream = (InputStream) data;

		try {
			dataStore = readXlsx(inputDataStream);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception e) {
			logger.error("Can not read XLSX file", e);
		}
		logger.debug("OUT");
		return dataStore;
	}

	private DataStore readXlsx(InputStream inputDataStream) throws Exception {
		logger.debug("IN");

		DataStore dataStore = null;
		int maxResults = this.getMaxResults();
		boolean checkMaxResults = false;
		if ((maxResults > 0)) {
			checkMaxResults = true;
		}

		dataStore = new DataStore();

		try {
			XSSFWorkbook wb = new XSSFWorkbook(inputDataStream);
			XSSFSheet sheet = getSheet(wb);

			int initialRow = 0;

			if ((getSkipRows() != null) && (!(getSkipRows().isEmpty()))) {
				initialRow = Integer.parseInt(getSkipRows());
				logger.debug("Skipping first " + getSkipRows() + " rows");
			}

			int rowsLimit;
			if ((getLimitRows() != null) && (!(getLimitRows().isEmpty()))) {
				rowsLimit = initialRow + Integer.parseInt(getLimitRows());

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

			for (int r = 0; r <= rowsLimit; r++) {
				// check if there is a limit for the rows to fetch in preview
				if (r > initialRow || r == 0) {

					XSSFRow row = sheet.getRow(r);
					if (checkIfRowIsEmpty(row)) {
						continue;
					}

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
		} catch (Exception e) {
			logger.error("Error while reading XLSX file", e);
			throw new RuntimeException("Impossible to parse XLSX file", e);
		}
		logger.debug("OUT");
		return dataStore;
	}

	private XSSFSheet getSheet(XSSFWorkbook workbook) {
		XSSFSheet sheet;

		int numberOfSheets = workbook.getNumberOfSheets();
		if ((getXslSheetNumber() != null) && (!(getXslSheetNumber().isEmpty()))) {

			int sheetNumber = Integer.parseInt(getXslSheetNumber()) - 1;
			if (sheetNumber > numberOfSheets) {
				logger.error("[XLSX] Wrong sheet number, using first sheet as default");
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

	private MetaData parseHeader(DataStore dataStore, XSSFRow row) {
		MetaData dataStoreMeta = new MetaData();

		int cells = row.getPhysicalNumberOfCells();
		setNumberOfColumns(cells);
		logger.debug("\nROW " + row.getRowNum() + " has " + cells + " cell(s).");
		for (int c = 0; c < cells; c++) {
			// get single cell
			XSSFCell cell = row.getCell(c);

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

	private IRecord parseRow(DataStore dataStore, XSSFRow row) {

		IRecord record = new Record(dataStore);

		int cells = row.getPhysicalNumberOfCells();

		int lastColumn = getNumberOfColumns();

		logger.debug("\nROW " + row.getRowNum() + " has " + cells + " cell(s).");
		for (int c = 0; c < lastColumn; c++) {
			// get single cell
			XSSFCell cell = row.getCell(c);

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

	private Object parseCell(XSSFCell cell) {
		Object valueField = null;

		if (cell == null)
			return "";

		switch (cell.getCellType()) {
		case XSSFCell.CELL_TYPE_FORMULA:
			valueField = cell.getCellFormula().toString();
			break;

		case XSSFCell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				/**
				 * XSSFCell.getCellStyle().getDataFormatString() returns Cell date format (even if it is a Custom format), but it will be Excel's Date format
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

		case XSSFCell.CELL_TYPE_STRING:
			valueField = cell.getStringCellValue();
			break;

		case XSSFCell.CELL_TYPE_BLANK:
			valueField = "";
			break;

		default:
		}

		return valueField;
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
