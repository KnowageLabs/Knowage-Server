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
package it.eng.spagobi.tools.dataset.bo;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.FileDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.IDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.FileDatasetCsvDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.FileDatasetXlsDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.iterator.CsvIteratorBuilder;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.iterator.ExcelIteratorBuilder;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.json.JSONUtils;

/**
 * @authors Angelo Bernabei angelo.bernabei@eng.it Giulio Gavardi giulio.gavardi@eng.it Andrea Gioia andrea.gioia@eng.it Davide Zerbetto davide.zerbetto@eng.it
 *
 */
public class FileDataSet extends ConfigurableDataSet {

	public static String DS_TYPE = "SbiFileDataSet";
	public static final String FILE_NAME = "fileName";
	public static final String FILE_TYPE = "fileType";
	public static final String RESOURCE_PATH = "resourcePath";

	public static final String CSV_FILE_DELIMITER_CHARACTER = "csvDelimiter";
	public static final String CSV_FILE_QUOTE_CHARACTER = "csvQuote";
	public static final String CSV_FILE_ENCODING = "csvEncoding";

	public static final String EXCEL_FILE_SKIP_ROWS = "skipRows";
	public static final String EXCEL_FILE_SHEET_NUMBER = "xslSheetNumber";

	public String fileType;

	public boolean useTempFile = false; // if true we use a file in resources\dataset\files\temp for reading

	private int maxResults = -1; // number of rows to read in a file, default -1 equals to no limits

	private static transient Logger logger = Logger.getLogger(FileDataSet.class);

	/**
	 * Instantiates a new empty file data set.
	 */
	public FileDataSet() {
	}

	public FileDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);
		logger.debug("IN");
		try {
			String config = JSONUtils.escapeJsonString(dataSetConfig.getConfiguration());
			JSONObject jsonConf = ObjectUtils.toJSONObject(config);
			String fileName = (jsonConf.get(FILE_NAME) != null) ? jsonConf.get(FILE_NAME).toString() : "";
			if (fileName == null || fileName.length() == 0) {
				throw new IllegalArgumentException("fileName member of SpagoBiDataSet object parameter cannot be null or empty"
						+ "while creating a FileDataSet. If you whant to create an empty FileDataSet use the proper constructor.");
			}
			this.setFileName((jsonConf.get(FILE_NAME) != null) ? jsonConf.get(FILE_NAME).toString() : "");
			logger.info("File name: " + fileName);

			this.setResourcePath(StringUtilities.isNotEmpty(jsonConf.optString(RESOURCE_PATH)) ? jsonConf.optString(RESOURCE_PATH)
					: (SpagoBIUtilities.getResourcePath() == null ? "" : SpagoBIUtilities.getResourcePath()));
			logger.info("Resource path: " + this.getResourcePath());
			if (this.dataProxy != null)
				dataProxy.setResPath(this.getResourcePath());
		} catch (Exception e) {
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
		}
		logger.debug("OUT");
	}

	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd;
		sbd = super.toSpagoBiDataSet();
		sbd.setType(DS_TYPE);
		return sbd;
	}

	/**
	 * try to guess the proper dataReader to use depending on the file extension
	 *
	 * @param fileName the target filename
	 */
	public void setDataReader(String fileName) {
		JSONObject jsonConf = ObjectUtils.toJSONObject(this.getConfiguration());
		String fileExtension = fileName.lastIndexOf('.') > 0 ? fileName.substring(fileName.lastIndexOf('.') + 1) : null;
		logger.debug("File extension: [" + fileExtension + "]");
		String fileType = this.getFileType();

		if ((fileType != null) && (!fileType.isEmpty())) {
			logger.debug("File type is: [" + fileType + "]");
		} else {
			logger.debug("No file type specified, using file extension as file type: [" + fileExtension + "]");
			fileType = fileExtension;
		}

		if ("CSV".equalsIgnoreCase(fileType)) {
			logger.info("File format: [CSV]");
			setDataReader(new FileDatasetCsvDataReader(jsonConf));
		} else if ("XLS".equalsIgnoreCase(fileType) || "XLSX".equalsIgnoreCase(fileType)) {
			logger.info("File format: [XLS Office 2003] or [XLSX Office 2007+]");
			setDataReader(new FileDatasetXlsDataReader(jsonConf));
		} else {
			throw new IllegalArgumentException("[" + fileExtension + "] is not a supported file type");
		}
	}

	@Override
	public FileDataProxy getDataProxy() {
		IDataProxy dataProxy;

		dataProxy = super.getDataProxy();

		if (dataProxy == null) {
			setDataProxy(new FileDataProxy(this.getResourcePath()));
			dataProxy = getDataProxy();
			if (useTempFile) {
				if (dataProxy instanceof FileDataProxy) {
					((FileDataProxy) dataProxy).setUseTempFile(true);

				}
			}
		}

		if (!(dataProxy instanceof FileDataProxy))
			throw new RuntimeException("DataProxy cannot be of type [" + dataProxy.getClass().getName() + "] in FileDataSet");

		return (FileDataProxy) dataProxy;
	}

	public String getFileName() {
		return getDataProxy().getFileName();
	}

	public void setFileName(String fileName) {
		setFileName(fileName, true);
	}

	public void setFileName(String fileName, boolean updateFileFormat) {
		if (fileName == null || fileName.length() == 0) {
			throw new IllegalArgumentException("fileName argument cannot be null or an empty string");
		}
		getDataProxy().setFileName(fileName);

		if (updateFileFormat) {
			try {
				setDataReader(fileName);
			} catch (Exception e) {
				throw new RuntimeException("Missing right exstension", e);
			}
		}
	}

	/**
	 * @return the fileType
	 */
	public String getFileType() {
		return fileType;
	}

	@Override
	public void loadData() {
		super.loadData();
		this.adjustMetadata(this.getDataStore());
	}

	private void adjustMetadata(IDataStore iDataStore) {
		IMetaData metadata = iDataStore.getMetaData();
		try {
			IMetaData definedMetadata = this.getMetadata();
			int count = metadata.getFieldCount();
			for (int i = 0; i < count; i++) {
				IFieldMetaData fieldMetadata = metadata.getFieldMeta(i);
				String name = fieldMetadata.getName();
				int index = definedMetadata.getFieldIndex(name);
				if (index != -1) {
					IFieldMetaData aFieldMetaData = definedMetadata.getFieldMeta(index);
					fieldMetadata.setFieldType(aFieldMetaData.getFieldType());
				}

			}
		} catch (Exception e) {
			logger.error("Cannot adjust metadata", e);
		}

	}

	/**
	 * @param fileType the fileType to set
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	/**
	 * @return the useTempFile
	 */
	public boolean isUseTempFile() {
		return useTempFile;
	}

	/**
	 * @param useTempFile the useTempFile to set
	 */
	public void setUseTempFile(boolean useTempFile) {
		this.useTempFile = useTempFile;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	@Override
	public IDataSource getDataSource() {
		return null;
	}

	@Override
	public DataIterator iterator() {
		if ("CSV".equalsIgnoreCase(fileType)) {
			DataIterator iterator = CsvIteratorBuilder.newBuilder().filePath(getFilePath()).quote(this.getCsvQuote()).encoding(getCsvEncoding())
					.delimiter(this.getCsvDelimiter()).metadata(this.getMetadata()).build();
			return iterator;
		} else if ("XLSX".equalsIgnoreCase(fileType) || "XLS".equalsIgnoreCase(fileType)) {
			DataIterator iterator = ExcelIteratorBuilder.newBuilder().filePath(getFilePath()).fileType(fileType).initialRow(getExcelInitialRow())
					.sheetNumber(getExcelSheetNumber()).metadata(this.getMetadata()).build();
			return iterator;
		} else {
			throw new UnsupportedOperationException("File DataSet iterator has not been implemented for file type " + fileType);
		}
	}

	private int getExcelInitialRow() {
		try {
			JSONObject cfg = new JSONObject(this.getConfiguration());
			return cfg.optInt(EXCEL_FILE_SKIP_ROWS);
		} catch (Exception e) {
			logger.error("Cannot retrieve excel file initial row number from dataset config, will start from first row", e);
			return 0;
		}
	}

	private int getExcelSheetNumber() {
		try {
			JSONObject cfg = new JSONObject(this.getConfiguration());
			return cfg.optInt(EXCEL_FILE_SHEET_NUMBER);
		} catch (Exception e) {
			logger.error("Cannot retrieve excel file sheet number from dataset config, using first sheet as default", e);
			return 0;
		}
	}

	private String getCsvEncoding() {
		try {
			JSONObject cfg = new JSONObject(this.getConfiguration());
			return cfg.getString(CSV_FILE_ENCODING);
		} catch (Exception e) {
			logger.error("Cannot retrieve CSV file encoding from dataset config, using UTF-8 as default", e);
			return "UTF-8";
		}
	}

	private Path getFilePath() {
		return Paths.get(getResourcePath(), "dataset", "files", this.getFileName());
	}

	private String getCsvQuote() {
		try {
			JSONObject cfg = new JSONObject(this.getConfiguration());
			return cfg.getString(CSV_FILE_QUOTE_CHARACTER);
		} catch (Exception e) {
			logger.error("Cannot retrieve CSV file quote character from dataset config, using \"\" as default", e);
			return "\"\"";
		}
	}

	private String getCsvDelimiter() {
		try {
			JSONObject cfg = new JSONObject(this.getConfiguration());
			return cfg.getString(CSV_FILE_DELIMITER_CHARACTER);
		} catch (Exception e) {
			logger.error("Cannot retrieve CSV file delimiter from dataset config, using \",\" as default", e);
			return ",";
		}
	}

}
