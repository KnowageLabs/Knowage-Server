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
import java.io.InputStreamReader;
import java.math.BigDecimal;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Marco Cortella marco.cortella@eng.it
 */
public class FileDatasetCsvDataReader extends AbstractDataReader {

	private static transient Logger logger = Logger.getLogger(FileDatasetCsvDataReader.class);
	public static final String CSV_FILE_DELIMITER_CHARACTER = "csvDelimiter";
	public static final String CSV_FILE_QUOTE_CHARACTER = "csvQuote";
	public static final String CSV_FILE_ENCODING = "csvEncoding";
	public static final String CSV_FILE_DATE_FORMAT = "dateFormat";

	private String csvDelimiter;
	private String csvQuote;
	private String csvEncoding;
	private String dateFormat;

	public FileDatasetCsvDataReader(JSONObject jsonConf) {
		super();

		// Get File Dataset Configuration Options
		if (jsonConf != null) {
			try {
				if (jsonConf.get(CSV_FILE_DELIMITER_CHARACTER) != null) {
					csvDelimiter = jsonConf.get(CSV_FILE_DELIMITER_CHARACTER).toString();
				} else {
					csvDelimiter = "";
				}

				if (jsonConf.get(CSV_FILE_QUOTE_CHARACTER) != null) {
					csvQuote = jsonConf.get(CSV_FILE_QUOTE_CHARACTER).toString();
				} else {
					csvQuote = "";
				}
				if (jsonConf.has(CSV_FILE_ENCODING)) {
					if (jsonConf.get(CSV_FILE_ENCODING) != null) {
						csvEncoding = jsonConf.get(CSV_FILE_ENCODING).toString();
					} else {
						csvEncoding = "windows-1252"; // default
					}
				} else {
					csvEncoding = "windows-1252"; // default
				}
				if (jsonConf.has(CSV_FILE_DATE_FORMAT)) {
					if (jsonConf.get(CSV_FILE_DATE_FORMAT) != null) {
						dateFormat = jsonConf.get(CSV_FILE_DATE_FORMAT).toString();
					} else {
						dateFormat = "";
					}
				}

			} catch (JSONException e) {
				logger.error("Error Deserializing File Dataset Options");
				throw new RuntimeException("Error Deserializing File Dataset Options", e);
			}
		} else {
			logger.error("Error jsonConf is not present for FileDatasetCsvDataReader");
			throw new RuntimeException("Error jsonConf is not present for FileDatasetCsvDataReader");

		}

	}

	@Override
	public IDataStore read(Object data) {
		logger.debug("IN");
		DataStore dataStore = null;
		try {
			InputStream inputDataStream = (InputStream) data;
			dataStore = readWithCsvMapReader(inputDataStream);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		} finally {
			logger.debug("OUT");
		}
		return dataStore;
	}

	private DataStore readWithCsvMapReader(InputStream inputDataStream) throws Exception {

		InputStreamReader inputStreamReader = new InputStreamReader(inputDataStream, csvEncoding);
		DataStore dataStore = null;
		MetaData dataStoreMeta;
		dataStore = new DataStore();
		dataStoreMeta = new MetaData();
		dataStore.setMetaData(dataStoreMeta);
		int maxResults = this.getMaxResults();
		boolean checkMaxResults = false;
		if ((maxResults > 0)) {
			checkMaxResults = true;
		}

		ICsvMapReader mapReader = null;

		try {
			CsvPreference customPreference = null;
			if (csvDelimiter.length() > 1) {
				customPreference = new CsvPreference.Builder(csvQuote.charAt(0), '\t', "\n").build();
			} else {
				customPreference = new CsvPreference.Builder(csvQuote.charAt(0), csvDelimiter.charAt(0), "\n").build();
			}

			// mapReader = new CsvMapReader(inputStreamReader, CsvPreference.STANDARD_PREFERENCE);
			mapReader = new CsvMapReader(inputStreamReader, customPreference);
			// the header columns are used as the keys to the Map
			final String[] header = mapReader.getHeader(true);

			int columnsNumber = mapReader.length();

			// Create Datastore Metadata with header file
			for (int i = 0; i < header.length; i++) {
				FieldMetadata fieldMeta = new FieldMetadata();
				String fieldName = StringUtils.escapeForSQLColumnName(header[i]);
				fieldMeta.setName(fieldName);
				fieldMeta.setType(String.class);
				dataStoreMeta.addFiedMeta(fieldMeta);
			}

			final CellProcessor[] processors = new CellProcessor[columnsNumber];
			for (int i = 0; i < processors.length; i++) {
				processors[i] = null;
			}

			Map<String, Object> contentsMap;

			boolean paginated = false;
			logger.debug("Reading data ...");
			if (isPaginationSupported() && getOffset() >= 0 && getFetchSize() >= 0) {
				logger.debug("Offset is equal to [" + getOffset() + "] and fetchSize is equal to [" + getFetchSize() + "]");
				paginated = true;
			} else {
				logger.debug("Offset and fetch size not set");
			}

			int rowFetched = 0;
			while ((contentsMap = mapReader.read(header, processors)) != null) {

				if ((!paginated && (!checkMaxResults || (rowFetched < maxResults)))
						|| ((paginated && (rowFetched >= offset) && (rowFetched - offset < fetchSize))
								&& (!checkMaxResults || (rowFetched - offset < maxResults)))) {
					// Create Datastore data
					IRecord record = new Record(dataStore);

					for (int i = 0; i < header.length; i++) {
						logger.debug(header[i] + " = " + contentsMap.get(header[i]));
						IField field = null;
						if (contentsMap.get(header[i]) == null) {
							field = new Field();
						} else {
							field = new Field(contentsMap.get(header[i]));
							// update metadata type in order with the real value's type (default was string)
							if (NumberUtils.isNumber((String) field.getValue())) {
								((FieldMetadata) dataStore.getMetaData().getFieldMeta(i)).setType(BigDecimal.class);
								field.setValue(new BigDecimal(String.valueOf(field.getValue())));
							} 
							//check if it's a number using comma decimal separator
							else if (NumberUtils.isNumber(((String) field.getValue()).replace(",", "."))) {
								((FieldMetadata) dataStore.getMetaData().getFieldMeta(i)).setType(BigDecimal.class);
								field.setValue(new BigDecimal(((String) field.getValue()).replace(",", ".")));
							}
							//check if it's a Date
							else {
								try {
									DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat);
									LocalDate localDate = LocalDate.parse((String) field.getValue(), formatter);
									//Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
									Date date = localDate.toDate();
									((FieldMetadata) dataStore.getMetaData().getFieldMeta(i)).setType(Date.class);
									field.setValue(date);
									
								} catch (Exception ex){
									logger.debug((String) field.getValue()+" is not a date");
								}
								
							}
						}
						record.appendField(field);
					}
					dataStore.appendRecord(record);
				}
				rowFetched++;
			}
			logger.debug("Read [" + rowFetched + "] records");
			logger.debug("Insert [" + dataStore.getRecordsCount() + "] records");

			if (this.isCalculateResultNumberEnabled()) {
				logger.debug("Calculation of result set number is enabled");
				dataStore.getMetaData().setProperty("resultNumber", new Integer(rowFetched));
			} else {
				logger.debug("Calculation of result set number is NOT enabled");
			}

		} finally {
			if (mapReader != null) {
				mapReader.close();
			}
		}
		return dataStore;
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
