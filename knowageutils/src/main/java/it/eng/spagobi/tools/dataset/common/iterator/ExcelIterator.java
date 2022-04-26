/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.tools.dataset.common.iterator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.file.Path;
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

import it.eng.spagobi.tools.dataset.common.datareader.ExcelDataReaderFactory;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

public class ExcelIterator extends FileIterator implements DataIterator {

	private static transient Logger logger = Logger.getLogger(ExcelIterator.class);

	private DataFormatter formatter = new DataFormatter();
	private int rowIndex;
	private final int numberOfColumns;
	private final int numberOfRows;
	private final Sheet sheet;

	public ExcelIterator(IMetaData metadata, Path filePath, String fileType, int sheetNumber, int initialRow)
			throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		super(metadata, filePath);
		ExcelDataReaderFactory excelFactory = new ExcelDataReaderFactory();
		Workbook wb = excelFactory.getWorkookInstance(fileType, inputStream);
		sheet = wb.getSheetAt(sheetNumber - 1);
		numberOfRows = sheet.getLastRowNum();
		Row header = sheet.getRow(initialRow);
		rowIndex = initialRow + 1;
		numberOfColumns = header.getLastCellNum();
	}

	@Override
	public boolean hasNext() {
		return (rowIndex <= numberOfRows);
	}

	@Override
	public IRecord next() {
		IRecord record = new Record();
		Row row = sheet.getRow(rowIndex);
		int lastColumn = numberOfColumns;
		for (int c = 0; c < lastColumn; c++) {
			Cell cell = row.getCell(c);
			Object valueField = null;
			try {
				valueField = parseCell(cell);
			} catch (Throwable t) {
				throw new RuntimeException("Impossible to parse cell [" + c + "]", t);
			}
			IField field = new Field(valueField);
			record.appendField(field);
		}
		rowIndex++;
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

}
