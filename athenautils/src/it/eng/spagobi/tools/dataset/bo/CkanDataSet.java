package it.eng.spagobi.tools.dataset.bo;

/*
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 */

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.CkanDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.IDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.FileDatasetCsvDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.FileDatasetXlsDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.json.JSONUtils;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CkanDataSet extends ConfigurableDataSet {

	public static String DS_TYPE = "SbiCkanDataSet";
	public static final String FILE_NAME = "fileName";
	public static final String FILE_TYPE = "fileType";
	public static final String RESOURCE_PATH = "resourcePath";
	public static final String CKAN_ID = "ckanId";
	public static final String CKAN_URL = "ckanUrl";

	public String fileType;
	public String ckanId;
	public String ckanUrl;

	private int maxResults = -1; // number of rows to read in a file, default -1 equals to no limits

	private static transient Logger logger = Logger.getLogger(CkanDataSet.class);

	/**
	 * Instantiates a new empty file data set.
	 */
	public CkanDataSet() {
		super();
	}

	public CkanDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

		logger.debug("IN");
		try {
			// JSONObject jsonConf =
			// ObjectUtils.toJSONObject(dataSetConfig.getConfiguration());
			String config = JSONUtils.escapeJsonString(dataSetConfig.getConfiguration());
			JSONObject jsonConf = ObjectUtils.toJSONObject(config);
			String fileName = (jsonConf.get(FILE_NAME) != null) ? jsonConf.get(FILE_NAME).toString() : "";
			if (fileName == null || fileName.length() == 0) {
				throw new IllegalArgumentException("fileName member of SpagoBiDataSet object parameter cannot be null or empty"
						+ "while creating a FileDataSet. If you whant to create an empty FileDataSet use the proper constructor.");
			}
			this.setFileName((jsonConf.get(FILE_NAME) != null) ? jsonConf.get(FILE_NAME).toString() : "");
			logger.info("File name: " + fileName);

			this.setResourcePath(StringUtilities.isNotEmpty(jsonConf.optString(CKAN_URL)) ? jsonConf.optString(CKAN_URL) : "");
			logger.info("Resource path: " + this.getResourcePath());
			if (this.dataProxy != null)
				dataProxy.setResPath(this.getResourcePath());
		} catch (Exception e) {
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
		}
		// setFileName( dataSetConfig.getFileName() );

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
	 * @param fileName
	 *            the target filename
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
			// setDataReader( new CsvDataReader() );
			setDataReader(new FileDatasetCsvDataReader(jsonConf));
		} else if ("XLS".equalsIgnoreCase(fileType)) {
			logger.info("File format: [XLS Office 2003]");
			setDataReader(new FileDatasetXlsDataReader(jsonConf));
		}
		// else if ("xml".equalsIgnoreCase( fileExtension ) || "txt".equalsIgnoreCase( fileExtension )) {
		// logger.info("File format: [XML]");
		// setDataReader( new XmlDataReader() );
		// }

		else {
			throw new IllegalArgumentException("[" + fileExtension + "] is not a supported file type");
		}
	}

	@Override
	public CkanDataProxy getDataProxy() {
		IDataProxy dataProxy;

		dataProxy = super.getDataProxy();

		if (dataProxy == null) {
			setDataProxy(new CkanDataProxy(this.getResourcePath()));
			dataProxy = getDataProxy();
		}

		if (!(dataProxy instanceof CkanDataProxy))
			throw new RuntimeException("DataProxy cannot be of type [" + dataProxy.getClass().getName() + "] in CKANDataSet");

		return (CkanDataProxy) dataProxy;
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
	 * @param fileType
	 *            the fileType to set
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getCkanId() {
		return ckanId;
	}

	public void setCkanId(String ckanId) {
		this.ckanId = ckanId;
	}

	public String getCkanUrl() {
		return ckanUrl;
	}

	public void setCkanUrl(String ckanUrl) {
		this.ckanUrl = ckanUrl;
	}

	@Override
	public String getSignature() {
		// return this.getDataProxy().getMD5Checksum();
		return this.getDataProxy().getFileName();
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
}